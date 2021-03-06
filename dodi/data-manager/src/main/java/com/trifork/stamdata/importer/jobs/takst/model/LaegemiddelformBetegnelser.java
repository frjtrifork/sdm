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

@Entity(name = "Formbetegnelse")
public class LaegemiddelformBetegnelser extends TakstEntity
{
	private String kode; // Ref. t. LMS01, felt 08
	private String tekst;
	private String aktivInaktiv; // A (Aktiv)=DLS o.l.-I (inaktiv)=Ikke
									// anerkendt term

	@Column(name = "Aktiv")
	public Boolean getAktivInaktiv()
	{
		return "A".equalsIgnoreCase(this.aktivInaktiv);
	}

	@Override
	public String getKey()
	{
		return this.kode;
	}

	@Id
	@Column
	public String getKode()
	{
		return this.kode;
	}

	@Column
	public String getTekst()
	{
		return tekst;
	}

	public void setAktivInaktiv(String aktivInaktiv)
	{
		this.aktivInaktiv = aktivInaktiv;
	}

	public void setKode(String kode)
	{
		this.kode = kode;
	}

	public void setTekst(String tekst)
	{
		this.tekst = tekst;
	}

}
