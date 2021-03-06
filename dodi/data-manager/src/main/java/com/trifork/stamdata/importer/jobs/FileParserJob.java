/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Contributors are attributed in the source code
 * where applicable.
 *
 * The Original Code is "Stamdata".
 *
 * The Initial Developer of the Original Code is Trifork Public A/S.
 *
 * Portions created for the Original Code are Copyright 2011,
 * Lægemiddelstyrelsen. All Rights Reserved.
 *
 * Portions created for the FMKi Project are Copyright 2011,
 * National Board of e-Health (NSI). All Rights Reserved.
 */


package com.trifork.stamdata.importer.jobs;

import static com.trifork.stamdata.Preconditions.checkArgument;
import static com.trifork.stamdata.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import com.trifork.stamdata.importer.parsers.ParserState;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.collect.Lists;
import com.trifork.stamdata.importer.config.ConnectionManager;
import com.trifork.stamdata.importer.persistence.AuditingPersister;

@Deprecated
public class FileParserJob implements ParserState, Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(FileParserJob.class);

	private DateTime stabilizationPeriodEnd;
	private long inputDirSignature;

	private boolean isRunning = false;

	private final Class<? extends FileParser> parser;
	private final File rootDir;

	private final int minimumImportFrequency;

    private final ConnectionManager connectionManager = new ConnectionManager();

	public FileParserJob(File rootDir, Class<? extends FileParser> parser, int minimumImportFrequency)
	{
		checkArgument(minimumImportFrequency > 0);
        
		this.minimumImportFrequency = minimumImportFrequency;
		this.rootDir = checkNotNull(rootDir);
		this.parser = checkNotNull(parser);
	}

	/**
	 * Checks if new files are present, and handle them if true.
	 */
	@Override
	public final void run()
	{
	    // This is the outermost most run loop for a parser.
	    // It is extremely important that exceptions are handled
	    // so they do not get swallowed by the void that is
	    // Runnable.class, and we never get notified.
	    
	    try
        {
            FileParser parser = createParserInstance();

	        // Set up a nice logging context so every log entry
	        // knows which parser we are running.
	        
	        MDC.put("parser", parser.identifier());
	        
	        internalRun(parser);
	    }
	    catch (Exception e)
        {
	        logger.error("Something went wrong while executing the parser.", e);
	    }
	    finally
        {
	        MDC.clear();
	    }
	}

    private FileParser createParserInstance()
    {
        try
        {
            return parser.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
	
	private final void internalRun(FileParser parser)
	{
        // Check for rejected files.
        // The parser is in a error state as long as there are files there.

        if (isLocked()) return;

        // Check if there are any files to import,
        // ignoring any unimportant files such as '.DS_Store'.

        if (getInputFiles().length == 0) return;

        isRunning = true;

        // If there are files to import wait a while and make sure the
        // files are stable.

        if (inputDirSignature != getDirSignature())
        {
            logger.info("Files discovered in the input directory. Making sure the files have been completly transfered before parsing will begin.");

            startStabilizationPeriod();
            return;
        }

        // Wait until the input files seem to be stable.

        if (stabilizationPeriodEnd.isAfterNow()) return;

        stabilizationPeriodEnd = null;
        inputDirSignature = -1;

        // Once stable check to see if all the expected files are there.

        File[] input = getInputFiles();

        if (!parser.validateInputStructure(input))
        {
            logger.error("Not all expected files could be found. Moving the input to the rejected folder.");

            moveAllFilesToRejected();

            return;
        }

        // If so parse and import them.

        runParser(parser);
	}

	/**
	 * Determines whether a file should be ignored when checking for input.
	 * 
	 * @param file the file to check.
	 * 
	 * @return true if the file can safely be ignored.
	 */
	private boolean isMundane(File file)
	{
		return file.getName().equals(".DS_Store");
	}

	/**
	 * Returns a signature of the input dir's contained files.
	 * 
	 * @return an hash of the input directory's contents.
	 */
	protected long getDirSignature()
	{
		long hash = 0;

		for (File file : getInputFiles())
		{
			if (isMundane(file)) continue;

			hash += file.getName().hashCode() * (file.lastModified() + file.length());
		}

		return hash;
	}

	private void startStabilizationPeriod()
	{
		stabilizationPeriodEnd = new DateTime().plusSeconds(30);

		inputDirSignature = getDirSignature();
	}

	private boolean moveInputToProcessing()
	{
		boolean success;

		try
		{
			for (File file : getInputFiles())
			{
				// Skip any OS file and the like.

				if (isMundane(file)) continue;

				// Move each file.

				FileUtils.moveToDirectory(file, getProcessingDir(), true);
			}

			success = true;
		}
		catch (IOException e)
		{
			logger.error("Could not move all input files to processing directory.", e);
			success = false;
		}

		return success;
	}

	/**
	 * Wraps an import in a database transaction, and handles any errors that
	 * might occur while parsing a set of files.
	 */
	private void runParser(FileParser parser)
	{
		moveInputToProcessing();

		Connection connection = null;

		try
		{
			logger.info("Starting import.");

			connection = connectionManager.getConnection();

			parser.parse(getProcessingFiles(), new AuditingPersister(connection), null);
			ImportTimeManager.setImportTime(parser.identifier(), new Date());

			connection.commit();

			logger.info("Import completed.");

			FileUtils.deleteQuietly(getProcessingDir());
		}
		catch (Exception e)
		{
			logger.error("Unhandled exception during import. Input files will be moved to the rejected folder.", e);

			try
			{
				connection.rollback();
			}
			catch (Exception ex)
			{
				logger.error("Could not rollback the connection.", ex);
			}

			moveAllFilesToRejected();
		}
		finally
		{
			ConnectionManager.close(connection);
			isRunning = false;
		}
	}

	protected void moveAllFilesToRejected()
	{
		try
		{
			for (File f : getProcessingFiles())
			{
				FileUtils.moveFileToDirectory(f, getRejectedDir(), true);
			}

			for (File f : getInputFiles())
			{
				FileUtils.moveFileToDirectory(f, getRejectedDir(), true);
			}
		}
		catch (Exception e)
		{
			logger.error("The files couldn't be moved to the rejected folder.", e);
		}
	}

	public boolean isRejectedDirEmpty()
	{
		return getRejectedFiles().length == 0;
	}

	public File getInputDir()
	{
		File file = new File(rootDir, identifier() + "/input");
		file.mkdirs();
		return file;
	}

	public File[] getInputFiles()
	{
		return filterOutMundaneFiles(getInputDir());
	}

	public File getRejectedDir()
	{
		File dir = new File(rootDir, identifier() + "/rejected");
		dir.mkdirs();
		return dir;
	}

	public File[] getProcessingFiles()
	{
		return filterOutMundaneFiles(getProcessingDir());
	}

	public File getProcessingDir()
	{
		File file = new File(rootDir, identifier() + "/processing");
		file.mkdirs();
		return file;
	}

	public File[] getRejectedFiles()
	{
		return filterOutMundaneFiles(getRejectedDir());
	}

	public File[] filterOutMundaneFiles(File root)
	{
		List<File> result = Lists.newArrayList();

		for (File file : root.listFiles())
		{
			if (isMundane(file)) continue;
			result.add(file);
		}

		return result.toArray(new File[] {});
	}

	public boolean isOverdue()
	{
		return hasBeenRun() && nextDeadline().isBeforeNow();
	}

    @Override
	public DateTime nextDeadline()
	{
		return latestRunTime().plusDays(minimumImportFrequency).toDateMidnight().toDateTime();
	}

    @Override
    public int minimumImportFrequency()
    {
        return minimumImportFrequency;
    }

	@Override
	public DateTime latestRunTime()
	{
		return ImportTimeManager.getLastImportTime(createParserInstance().identifier());
	}

    @Override
	public boolean hasBeenRun()
	{
		return latestRunTime() != null;
	}

	@Override
	public String identifier()
	{
		return createParserInstance().identifier();
	}

	@Override
	public boolean isLocked()
	{
		return !isRejectedDirEmpty();
	}

	@Override
	public boolean isInProgress()
	{
		return isRunning;
	}

	@Override
	public String name()
	{
		return createParserInstance().name();
	}
}
