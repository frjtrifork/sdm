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


package com.trifork.stamdata.importer.jobs.takst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.trifork.stamdata.importer.jobs.takst.model.ATCKoderOgTekst;
import com.trifork.stamdata.importer.jobs.takst.model.ATCKoderOgTekstFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Administrationsvej;
import com.trifork.stamdata.importer.jobs.takst.model.AdministrationsvejFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Beregningsregler;
import com.trifork.stamdata.importer.jobs.takst.model.BeregningsreglerFactory;
import com.trifork.stamdata.importer.jobs.takst.model.DivEnheder;
import com.trifork.stamdata.importer.jobs.takst.model.DivEnhederFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Dosering;
import com.trifork.stamdata.importer.jobs.takst.model.DoseringFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Doseringskode;
import com.trifork.stamdata.importer.jobs.takst.model.DoseringskodeFactory;
import com.trifork.stamdata.importer.jobs.takst.model.EmballagetypeKoder;
import com.trifork.stamdata.importer.jobs.takst.model.EmballagetypeKoderFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Enhedspriser;
import com.trifork.stamdata.importer.jobs.takst.model.EnhedspriserFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Firma;
import com.trifork.stamdata.importer.jobs.takst.model.FirmaFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Indholdsstoffer;
import com.trifork.stamdata.importer.jobs.takst.model.IndholdsstofferFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Indikation;
import com.trifork.stamdata.importer.jobs.takst.model.IndikationFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Indikationskode;
import com.trifork.stamdata.importer.jobs.takst.model.IndikationskodeFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Klausulering;
import com.trifork.stamdata.importer.jobs.takst.model.KlausuleringFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Laegemiddel;
import com.trifork.stamdata.importer.jobs.takst.model.LaegemiddelAdministrationsvejRef;
import com.trifork.stamdata.importer.jobs.takst.model.LaegemiddelFactory;
import com.trifork.stamdata.importer.jobs.takst.model.LaegemiddelformBetegnelser;
import com.trifork.stamdata.importer.jobs.takst.model.LaegemiddelformBetegnelserFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Laegemiddelnavn;
import com.trifork.stamdata.importer.jobs.takst.model.LaegemiddelnavnFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Medicintilskud;
import com.trifork.stamdata.importer.jobs.takst.model.MedicintilskudFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Opbevaringsbetingelser;
import com.trifork.stamdata.importer.jobs.takst.model.OpbevaringsbetingelserFactory;
import com.trifork.stamdata.importer.jobs.takst.model.OplysningerOmDosisdispensering;
import com.trifork.stamdata.importer.jobs.takst.model.OplysningerOmDosisdispenseringFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Pakning;
import com.trifork.stamdata.importer.jobs.takst.model.PakningFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Pakningskombinationer;
import com.trifork.stamdata.importer.jobs.takst.model.PakningskombinationerFactory;
import com.trifork.stamdata.importer.jobs.takst.model.PakningskombinationerUdenPriser;
import com.trifork.stamdata.importer.jobs.takst.model.PakningskombinationerUdenPriserFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Pakningsstoerrelsesenhed;
import com.trifork.stamdata.importer.jobs.takst.model.Priser;
import com.trifork.stamdata.importer.jobs.takst.model.PriserFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Rekommandationer;
import com.trifork.stamdata.importer.jobs.takst.model.RekommandationerFactory;
import com.trifork.stamdata.importer.jobs.takst.model.SpecialeForNBS;
import com.trifork.stamdata.importer.jobs.takst.model.SpecialeForNBSFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Styrkeenhed;
import com.trifork.stamdata.importer.jobs.takst.model.Substitution;
import com.trifork.stamdata.importer.jobs.takst.model.SubstitutionAfLaegemidlerUdenFastPris;
import com.trifork.stamdata.importer.jobs.takst.model.SubstitutionAfLaegemidlerUdenFastPrisFactory;
import com.trifork.stamdata.importer.jobs.takst.model.SubstitutionFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Tidsenhed;
import com.trifork.stamdata.importer.jobs.takst.model.Tilskudsintervaller;
import com.trifork.stamdata.importer.jobs.takst.model.TilskudsintervallerFactory;
import com.trifork.stamdata.importer.jobs.takst.model.TilskudsprisgrupperPakningsniveau;
import com.trifork.stamdata.importer.jobs.takst.model.TilskudsprisgrupperPakningsniveauFactory;
import com.trifork.stamdata.importer.jobs.takst.model.UdgaaedeNavne;
import com.trifork.stamdata.importer.jobs.takst.model.UdgaaedeNavneFactory;
import com.trifork.stamdata.importer.jobs.takst.model.Udleveringsbestemmelser;
import com.trifork.stamdata.importer.jobs.takst.model.UdleveringsbestemmelserFactory;
import com.trifork.stamdata.importer.persistence.Dataset;
import com.trifork.stamdata.importer.util.Dates;


public class TakstParser
{
	private static final String SUPPORTED_TAKST_VERSION = "12.0";
	protected static Logger logger = LoggerFactory.getLogger(TakstParser.class);

	private <T extends TakstEntity> void add(Takst takst, FixedLengthFileParser parser, FixedLengthParserConfiguration<T> config, Class<T> type) throws Exception
	{
		List<T> entities = parser.parse(config, type);
		TakstDataset<T> ds = new TakstDataset<T>(takst, entities, type);
		takst.addDataset(ds, type);
	}

	private <T extends TakstEntity> void addOptional(File[] input, Takst takst, FixedLengthFileParser parser, FixedLengthParserConfiguration<T> config, Class<T> type) throws Exception
	{
		File file = getFileByName(config.getFilename(), input);

		if (file != null && file.isFile())
		{
			List<T> entities = parser.parse(config, type);
			takst.addDataset(new TakstDataset<T>(takst, entities, type), type);
		}
	}

	public Takst parseFiles(File[] input) throws Exception
	{
		// Parse required meta information first.

		String systemline = getSystemLine(input);
		String version = getVersion(systemline);

		if (!SUPPORTED_TAKST_VERSION.equals(version)) logger.warn("Parsing unsupported of the takst! supported={}, actual={}", version, SUPPORTED_TAKST_VERSION);

		Date fromDate = getValidFromDate(systemline);

		Takst takst = new Takst(fromDate, Dates.THE_END_OF_TIME);

		// Add the takst itself to the takst as a "meta entity" to represent
		// in DB that the takst was loaded.

		takst.setValidityWeekNumber(getValidWeek(systemline));
		takst.setValidityYear(getValidYear(systemline));

		List<Takst> takstMetaEntity = new ArrayList<Takst>();
		takstMetaEntity.add(takst);
		takst.addDataset(new TakstDataset<Takst>(takst, takstMetaEntity, Takst.class), Takst.class);

		// Now parse the required data files.

		FixedLengthFileParser parser = new FixedLengthFileParser(input);

		add(takst, parser, new LaegemiddelFactory(), Laegemiddel.class);
		add(takst, parser, new PakningFactory(), Pakning.class);
		add(takst, parser, new PriserFactory(), Priser.class);
		add(takst, parser, new SubstitutionFactory(), Substitution.class);
		add(takst, parser, new SubstitutionAfLaegemidlerUdenFastPrisFactory(), SubstitutionAfLaegemidlerUdenFastPris.class);
		add(takst, parser, new TilskudsprisgrupperPakningsniveauFactory(), TilskudsprisgrupperPakningsniveau.class);
		add(takst, parser, new FirmaFactory(), Firma.class);
		add(takst, parser, new UdgaaedeNavneFactory(), UdgaaedeNavne.class);
		add(takst, parser, new AdministrationsvejFactory(), Administrationsvej.class);
		add(takst, parser, new ATCKoderOgTekstFactory(), ATCKoderOgTekst.class);
		add(takst, parser, new BeregningsreglerFactory(), Beregningsregler.class);
		add(takst, parser, new EmballagetypeKoderFactory(), EmballagetypeKoder.class);
		add(takst, parser, new DivEnhederFactory(), DivEnheder.class);
		add(takst, parser, new MedicintilskudFactory(), Medicintilskud.class);
		add(takst, parser, new KlausuleringFactory(), Klausulering.class);
		add(takst, parser, new UdleveringsbestemmelserFactory(), Udleveringsbestemmelser.class);
		add(takst, parser, new SpecialeForNBSFactory(), SpecialeForNBS.class);
		add(takst, parser, new OpbevaringsbetingelserFactory(), Opbevaringsbetingelser.class);
		add(takst, parser, new TilskudsintervallerFactory(), Tilskudsintervaller.class);
		add(takst, parser, new OplysningerOmDosisdispenseringFactory(), OplysningerOmDosisdispensering.class);
		add(takst, parser, new IndikationskodeFactory(), Indikationskode.class);
		add(takst, parser, new IndikationFactory(), Indikation.class);
		add(takst, parser, new DoseringskodeFactory(), Doseringskode.class);
		add(takst, parser, new DoseringFactory(), Dosering.class);

		// Now parse optional files one at a time, to be robust to them not
		// being present.

		addOptional(input, takst, parser, new LaegemiddelnavnFactory(), Laegemiddelnavn.class);
		addOptional(input, takst, parser, new LaegemiddelformBetegnelserFactory(), LaegemiddelformBetegnelser.class);
		addOptional(input, takst, parser, new RekommandationerFactory(), Rekommandationer.class);
		addOptional(input, takst, parser, new IndholdsstofferFactory(), Indholdsstoffer.class);
		addOptional(input, takst, parser, new EnhedspriserFactory(), Enhedspriser.class);
		addOptional(input, takst, parser, new PakningskombinationerFactory(), Pakningskombinationer.class);
		addOptional(input, takst, parser, new PakningskombinationerUdenPriserFactory(), PakningskombinationerUdenPriser.class);

		// Post process the data.

		addTypedDivEnheder(takst);
		addLaegemiddelAdministrationsvejRefs(takst);
		
		return takst;
	}

	private String getVersion(String systemline)
	{
		return systemline.substring(2, 7).trim();
	}

	/**
	 * Extracts the routes of administration and adds them to the takst.
	 */
	private void addLaegemiddelAdministrationsvejRefs(Takst takst)
	{
		TakstDataset<Laegemiddel> lmr = takst.getDatasetOfType(Laegemiddel.class);
		List<LaegemiddelAdministrationsvejRef> lars = new ArrayList<LaegemiddelAdministrationsvejRef>();

		for (Laegemiddel lm : lmr.getEntities())
		{
			for (Administrationsvej av : getAdministrationsveje(lm, takst))
			{
				lars.add(new LaegemiddelAdministrationsvejRef(lm, av));
			}
		}

		takst.addDataset(new TakstDataset<LaegemiddelAdministrationsvejRef>(takst, lars, LaegemiddelAdministrationsvejRef.class), LaegemiddelAdministrationsvejRef.class);
	}


	private List<Administrationsvej> getAdministrationsveje(Laegemiddel drug, Takst takst)
	{
		List<Administrationsvej> adminveje = Lists.newArrayList();
		
		String routeOfAdministration = drug.getAdministrationsvejKode();
		
		for (String code : Splitter.fixedLength(2).split(routeOfAdministration))
		{
			Administrationsvej adminVej = takst.getEntity(code, Administrationsvej.class);

			if (adminVej == null)
			{
				logger.warn("Unknown route of administration, code not found in LMS11. code={}", code);
			}
			else
			{
				adminveje.add(adminVej);
			}
		}

		return adminveje;
	}

	
	/**
	 * Splits up the entity type 'DivEnhed' into three sub-catagories.
	 */
	private void addTypedDivEnheder(Takst takst)
	{
		List<Tidsenhed> timeUnits = new ArrayList<Tidsenhed>();
		List<Pakningsstoerrelsesenhed> packageUnits = new ArrayList<Pakningsstoerrelsesenhed>();
		List<Styrkeenhed> strengthUnits = new ArrayList<Styrkeenhed>();
		Dataset<DivEnheder> divEnheder = takst.getDatasetOfType(DivEnheder.class);

		for (DivEnheder enhed : divEnheder.getEntities())
		{
			if (enhed.isEnhedstypeTid())
			{
				timeUnits.add(new Tidsenhed(enhed));
			}
			else if (enhed.isEnhedstypePakning())
			{
				packageUnits.add(new Pakningsstoerrelsesenhed(enhed));
			}
			else if (enhed.isEnhedstypeStyrke())
			{
				strengthUnits.add(new Styrkeenhed(enhed));
			}
		}

		takst.addDataset(new TakstDataset<Tidsenhed>(takst, timeUnits, Tidsenhed.class), Tidsenhed.class);
		takst.addDataset(new TakstDataset<Pakningsstoerrelsesenhed>(takst, packageUnits, Pakningsstoerrelsesenhed.class), Pakningsstoerrelsesenhed.class);
		takst.addDataset(new TakstDataset<Styrkeenhed>(takst, strengthUnits, Styrkeenhed.class), Styrkeenhed.class);
	}

	private int getValidYear(String line)
	{
		return Integer.parseInt(line.substring(87, 91));
	}

	private int getValidWeek(String line)
	{
		return Integer.parseInt(line.substring(91, 93));
	}

	public Date getValidFromDate(String line) throws ParseException
	{
		String dateline = line.substring(47, 55);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		return df.parse(dateline);

	}

	private String getSystemLine(File[] input) throws Exception
	{
		return new BufferedReader(new FileReader(getFileByName("system.txt", input))).readLine();
	}

	public static File getFileByName(String filename, File[] files)
	{
		File result = null;

		for (File f : files)
		{
			if (f.getName().equalsIgnoreCase(filename))
			{
				result = f;
				break;
			}
		}

		return result;
	}
}
