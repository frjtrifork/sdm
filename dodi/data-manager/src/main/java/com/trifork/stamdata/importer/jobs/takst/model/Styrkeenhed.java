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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.trifork.stamdata.importer.jobs.takst.TakstEntity;


@Entity
public class Styrkeenhed extends TakstEntity
{
	private final DivEnheder enheder;

	public Styrkeenhed(DivEnheder enheder)
	{
		this.enheder = enheder;
	}

	@Override
	public String getKey()
	{
		return enheder.getKode();
	}

	@Id
	@Column
	public String getStyrkeenhedKode()
	{
		return enheder.getKode();
	}

	@Column
	public String getStyrkeenhedKortTekst()
	{
		return enheder.getKortTekst();
	}

	@Column
	public String getStyrkeenhedTekst()
	{
		return enheder.getTekst();
	}
}
