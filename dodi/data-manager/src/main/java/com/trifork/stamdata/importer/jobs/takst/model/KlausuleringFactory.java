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


package com.trifork.stamdata.importer.jobs.takst.model;

import org.apache.commons.lang.StringUtils;

import com.trifork.stamdata.importer.jobs.takst.FixedLengthParserConfiguration;



public class KlausuleringFactory implements FixedLengthParserConfiguration<Klausulering>
{
	private static final String NO_CLAUSE_IDENTIFIER = StringUtils.repeat(" ", 5);

	@Override
	public String getFilename()
	{
		return "lms17.txt";
	}

	@Override
	public int getLength(int fieldNo)
	{
		switch (fieldNo)
		{
		case 0:
			return 5;
		case 1:
			return 50;
		case 2:
			return 500;
		default:
			return -1;
		}
	}

	@Override
	public int getNumberOfFields()
	{
		return 3;
	}

	@Override
	public int getOffset(int fieldNo)
	{
		switch (fieldNo)
		{
		case 0:
			return 0;
		case 1:
			return 5;
		case 2:
			return 55;
		default:
			return -1;
		}
	}

	@Override
	public void setFieldValue(Klausulering obj, int fieldNo, String value)
	{
		switch (fieldNo)
		{
		case 0:
			if (value == null)
			{
				value = NO_CLAUSE_IDENTIFIER;
			}
			obj.setKode(value);
			break;
		case 1:
			obj.setKortTekst(value);
			break;
		case 2:
			obj.setTekst(value);
			break;
		default:
			break;
		}
	}
}
