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


package com.trifork.stamdata.importer.jobs.sor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trifork.stamdata.importer.persistence.AbstractStamdataEntity;
import com.trifork.stamdata.importer.util.Dates;


@Entity
public class SygehusAfdeling extends AbstractStamdataEntity
{
	private String navn;
	private Long eanLokationsnummer;
	private String nummer;
	private String telefon;
	private String vejnavn;
	private String postnummer;
	private String bynavn;
	private String email;
	private String www;
	private Long afdelingTypeKode;
	private String afdelingTypeTekst;
	private Long hovedSpecialeKode;
	private String hovedSpecialeTekst;
	private Long sorNummer;
	private Long sygehusSorNummer;
	private Long overAfdelingSorNummer;
	private Long underlagtSygehusSorNummer;
	private Date validFrom;
	private Date validTo;
    public final Logger logger = LoggerFactory.getLogger(AbstractStamdataEntity.class);

	@Column
	public String getNavn()
	{
		return navn;
	}

	public void setNavn(String navn)
	{
		this.navn = navn;
	}

	@Column
	public Long getEanLokationsnummer()
	{
		return eanLokationsnummer;
	}

	public void setEanLokationsnummer(Long eanLokationsnummer)
	{
		this.eanLokationsnummer = eanLokationsnummer;
	}

	@Column
	public String getNummer()
	{
		return nummer;
	}

	public void setNummer(String nummer)
	{
		this.nummer = nummer;
	}

	@Column
	public String getTelefon()
	{
		return telefon;
	}

	public void setTelefon(String telefon)
	{
		this.telefon = telefon;
	}

	@Column
	public String getVejnavn()
	{
		return vejnavn;
	}

	public void setVejnavn(String vejnavn)
	{
		this.vejnavn = vejnavn;
	}

	@Column
	public String getPostnummer()
	{
		return postnummer;
	}

	public void setPostnummer(String postnummer)
	{
		this.postnummer = postnummer;
	}

	@Column
	public String getBynavn()
	{
		return bynavn;
	}

	public void setBynavn(String bynavn)
	{
		this.bynavn = bynavn;
	}

	@Column
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column
	public String getWww()
	{
		return www;
	}

	public void setWww(String www)
	{
		this.www = www;
	}

	@Column
	public Long getAfdelingTypeKode()
	{
		return afdelingTypeKode;
	}

	public void setAfdelingTypeKode(Long afdelingTypeKode)
	{
		this.afdelingTypeKode = afdelingTypeKode;
	}

	@Column
	public Long getHovedSpecialeKode()
	{
		return hovedSpecialeKode;
	}

	public void setHovedSpecialeKode(Long hovedSpecialeKode)
	{
		this.hovedSpecialeKode = hovedSpecialeKode;
	}

	@Column
	public String getHovedSpecialeTekst()
	{
		return hovedSpecialeTekst;
	}

	public void setHovedSpecialeTekst(String hovedSpecialeTekst)
	{
		this.hovedSpecialeTekst = hovedSpecialeTekst;
	}

	@Column
	public String getAfdelingTypeTekst()
	{
		return afdelingTypeTekst;
	}

	public void setAfdelingTypeTekst(String afdelingTypeTekst)
	{
		this.afdelingTypeTekst = afdelingTypeTekst;
	}

	@Id
	@Column
	public Long getSorNummer()
	{
		return sorNummer;
	}

	public void setSorNummer(Long sorNummer)
	{
		this.sorNummer = sorNummer;
	}

	@Column
	public Long getSygehusSorNummer()
	{
		return sygehusSorNummer;
	}

	public void setSygehusSorNummer(Long sygehusSorNummer)
	{
		this.sygehusSorNummer = sygehusSorNummer;
	}

	@Column
	public Long getOverAfdelingSorNummer()
	{

		return overAfdelingSorNummer;
	}

	public void setOverAfdelingSorNummer(Long overAfdelingSorNummer)
	{
		this.overAfdelingSorNummer = overAfdelingSorNummer;
	}

	@Column
	public Long getUnderlagtSygehusSorNummer()
	{
		return underlagtSygehusSorNummer;
	}

	public void setUnderlagtSygehusSorNummer(Long underlagtSygehusSorNummer)
	{
		this.underlagtSygehusSorNummer = underlagtSygehusSorNummer;
	}

	public void setValidFrom(Date validFrom)
	{
		this.validFrom = validFrom;
	}

	@Override
	public Date getValidFrom()
	{
		return validFrom;
	}

	@Override
	public Date getValidTo()
	{
		return (validTo != null) ? validTo : Dates.THE_END_OF_TIME;
	}

	public void setValidTo(Date validTo)
	{
		this.validTo = validTo;
	}
}
