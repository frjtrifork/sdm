// The contents of this file are subject to the Mozilla Public
// License Version 1.1 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of
// the License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS
// IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
// implied. See the License for the specific language governing
// rights and limitations under the License.
//
// Contributor(s): Contributors are attributed in the source code
// where applicable.
//
// The Original Code is "Stamdata".
//
// The Initial Developer of the Original Code is Trifork Public A/S.
//
// Portions created for the Original Code are Copyright 2011,
// Lægemiddelstyrelsen. All Rights Reserved.
//
// Portions created for the FMKi Project are Copyright 2011,
// National Board of e-Health (NSI). All Rights Reserved.

package com.trifork.stamdata.importer.jobs.takst.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trifork.stamdata.importer.jobs.takst.TakstEntity;
import com.trifork.stamdata.importer.persistence.Dataset;
import com.trifork.stamdata.importer.persistence.Id;
import com.trifork.stamdata.importer.persistence.Output;


@Output(name = "Laegemiddel")
public class Laegemiddel extends TakstEntity
{

	private Long drugid;
	private String varetype; // Udfyldt med SP (Specialiteter)
	private String varedeltype; // Udfyldt med LM (lægemiddel, reg.)
	private String alfabetSekvensplads;
	private Long specNummer; // D.sp.nr. (decentrale) - Alm. nr (centrale)
	private String navn; // Evt. forkortet
	private String laegemiddelformTekst; // Evt. forkortet
	private String formKode; // Ref. t. LMS22, felt 01
	private String kodeForYderligereFormOplysn; // Feltet er tomt pt.
	private String styrkeKlarTekst;
	private Long styrkeNumerisk;
	private String styrkeEnhed; // Ref. t. LMS15, enhedstype 3
	private Long mTIndehaver; // Ref. t. LMS09
	private Long repraesentantDistributoer; // Ref. t. LMS09
	private String aTC; // Ref. t. LMS12
	private String administrationsvej; // 4 x 2 kar. (Ref. t. LMS11)
	private String trafikadvarsel; // 2 muligh.: J eller blank
	private String substitution; // 2 muligh.: G eller blank
	private String laegemidletsSubstitutionsgruppe; // Substitutionsgruppenr. på
	// Drugid-niveau
	private String egnetTilDosisdispensering; // 2 muligh.: D eller blank
	private String datoForAfregistrAfLaegemiddel; // Format: ååååmmdd
	private String karantaenedato; // Format: ååååmmdd

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public List<Administrationsvej> getAdministrationsveje()
	{
		List<Administrationsvej> adminveje = new ArrayList<Administrationsvej>();
		for (int idx = 0; idx < administrationsvej.length(); idx += 2)
		{
			String avKode = administrationsvej.substring(idx, idx + 2);
			Administrationsvej adminVej = takst.getEntity(Administrationsvej.class, avKode);
			if (adminVej == null)
				logger.warn("Administaritonvej not found for kode: '" + avKode + "'");
			else
				adminveje.add(adminVej);
		}
		return adminveje;
	}

	@Output
	public String getAdministrationsvejKode()
	{
		return administrationsvej;
	}

	@Output
	public String getAlfabetSekvensplads()
	{
		return this.alfabetSekvensplads;
	}

	@Output(name = "ATCKode")
	public String getATC()
	{
		return aTC;
	}

	@Output(name = "ATCTekst")
	public String getATCTekst()
	{
		ATCKoderOgTekst atcObj = takst.getEntity(ATCKoderOgTekst.class, aTC);
		if (atcObj == null) return null;
		return atcObj.getTekst();
	}

	@Output
	public String getDatoForAfregistrAfLaegemiddel()
	{
		if (this.datoForAfregistrAfLaegemiddel == null || "".equals(this.datoForAfregistrAfLaegemiddel)) return null;
		SimpleDateFormat informat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat outformat = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			return outformat.format(informat.parse(this.datoForAfregistrAfLaegemiddel));
		}
		catch (ParseException e)
		{
			logger.error("Error converting DatoForAfregistrAfLaegemiddel to iso 8601 date format. Value was: \"" + this.datoForAfregistrAfLaegemiddel + "\". Returning unformated string: '" + this.datoForAfregistrAfLaegemiddel + "'");
			return this.datoForAfregistrAfLaegemiddel;
		}
	}

	/*
	 * tom@trifork.com: Doseringer vil jeg nok udelade lige nu, her er vi ved at
	 * lave store ændringer (evt. kan du bare udkommentere koden i første
	 * omgang, hvis vi fortryder)
	 */
	public List<Dosering> getDoseringer()
	{
		List<Dosering> result = new ArrayList<Dosering>();
		Dataset<Doseringskode> doseringskoder = takst.getDatasetOfType(Doseringskode.class);
		if (doseringskoder == null) return null;
		for (Doseringskode d : doseringskoder.getEntities())
		{
			if (this.drugid.equals(getDrugid()))
			{
				result.add(takst.getEntity(Dosering.class, d.getDoseringskode()));
			}
		}
		return result;
	}

	@Id
	@Output(name = "DrugID")
	public Long getDrugid()
	{
		return this.drugid;
	}

	@Output(name = "Dosisdispenserbar")
	public Integer getEgnetTilDosisdispensering()
	{
		return ("D".equals(this.egnetTilDosisdispensering)) ? 1 : 0;
	}

	public Boolean getEksperimentieltLaegemiddel()
	{
		return ("" + drugid).startsWith("2742");
	}

	@Output(name = "FormTekst")
	public String getForm()
	{
		LaegemiddelformBetegnelser lmfb = takst.getEntity(LaegemiddelformBetegnelser.class, formKode);
		if (lmfb == null)
		{
			return null;
		}
		return lmfb.getTekst();
	}

	@Output(name = "FormKode")
	public String getFormKode()
	{
		return this.formKode;
	}

	public List<Indholdsstoffer> getIndholdsstoffer()
	{
		List<Indholdsstoffer> result = new ArrayList<Indholdsstoffer>();
		Dataset<Indholdsstoffer> indholdsstoffer = takst.getDatasetOfType(Indholdsstoffer.class);
		if (indholdsstoffer == null) return null;
		for (Indholdsstoffer stof : indholdsstoffer.getEntities())
		{
			if (stof.getDrugID().equals(this.drugid))
			{

				if (!result.contains(stof))
				{
					result.add(stof);
				}

			}
		}

		return result;
	}

	public List<Indikation> getIndikationer()
	{
		List<Indikation> result = new ArrayList<Indikation>();
		Dataset<Indikationskode> indikationskode = takst.getDatasetOfType(Indikationskode.class);
		if (indikationskode == null) return null;
		for (Indikationskode i : indikationskode.getEntities())
		{
			if (i.getDrugID().equals(this.drugid))
			{
				result.add(takst.getEntity(Indikation.class, i.getIndikationskode()));
			}
		}
		return result;
	}

	@Output
	public String getKarantaenedato()
	{
		if (this.karantaenedato == null || "".equals(this.karantaenedato)) return null;
		SimpleDateFormat informat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat outformat = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			return outformat.format(informat.parse(this.karantaenedato));
		}
		catch (ParseException e)
		{
			logger.error("Error converting Karantaenedato to iso 8601 date format. Value was: \"" + this.karantaenedato + "\". Returning unformated string");
			return this.karantaenedato;
		}
	}

	@Output
	public String getKodeForYderligereFormOplysn()
	{
		return this.kodeForYderligereFormOplysn;
	}

	@Output
	public String getLaegemiddelformTekst()
	{
		return this.laegemiddelformTekst;
	}

	@Output
	public String getLaegemidletsSubstitutionsgruppe()
	{
		return this.laegemidletsSubstitutionsgruppe;
	}

	public Boolean getMagistreltLaegemiddel()
	{
		return ("" + drugid).startsWith("8");
	}

	@Output
	public Long getMTIndehaverKode()
	{
		return mTIndehaver;
	}

	public Firma getMTIndehaverRef()
	{
		return takst.getEntity(Firma.class, this.mTIndehaver);
	}

	@Output(name = "DrugName")
	public String getNavn()
	{
		if (this.navn == null || this.navn.trim().equals(""))
		{
			return "Ikke angivet";
		}
		return this.navn;
	}

	public List<Pakning> getPakninger()
	{
		Dataset<Pakning> pakninger = takst.getDatasetOfType(Pakning.class);
		if (pakninger == null) return null;
		List<Pakning> pakningerWithThisLaegemiddel = new ArrayList<Pakning>();
		for (Pakning pakning : pakninger.getEntities())
		{
			if (pakning.getDrugid().equals(this.drugid))
			{
				pakningerWithThisLaegemiddel.add(pakning);
			}
		}
		return pakningerWithThisLaegemiddel;
	}

	@Output
	public Long getRepraesentantDistributoerKode()
	{
		return repraesentantDistributoer;
	}

	public Firma getRepraesentantDistributoerRef()
	{
		return takst.getEntity(Firma.class, this.repraesentantDistributoer);
	}

	@Output
	public Long getSpecNummer()
	{
		return this.specNummer;
	}

	@Output(name = "StyrkeEnhed")
	public String getStyrke()
	{
		if (styrkeNumerisk == null || styrkeNumerisk == 0)
		{
			return null;
		}
		return styrkeEnhed;
	}

	@Output(name = "StyrkeTekst")
	public String getStyrkeKlarTekst()
	{
		return this.styrkeKlarTekst;
	}

	@Output(name = "StyrkeNumerisk")
	public Double getStyrkeNumerisk()
	{
		if (styrkeNumerisk == null || styrkeNumerisk == 0)
		{
			return null;
		}
		return this.styrkeNumerisk / 1000.0;
	}

	@Output
	public String getSubstitution()
	{
		return this.substitution;
	}

	@Output
	public Boolean getTrafikadvarsel()
	{
		return "J".equalsIgnoreCase(this.trafikadvarsel);
	}

	public List<UdgaaedeNavne> getUdgaaedeNavne()
	{
		List<UdgaaedeNavne> unavne = new ArrayList<UdgaaedeNavne>();
		Dataset<UdgaaedeNavne> unds = takst.getDatasetOfType(UdgaaedeNavne.class);
		if (unds == null) return null;
		for (UdgaaedeNavne un : unds.getEntities())
		{
			if (un.getDrugid().equals(drugid)) unavne.add(un);
		}
		return unavne;
	}

	@Output
	public String getVaredeltype()
	{
		return this.varedeltype;
	}

	@Output
	public String getVaretype()
	{
		return this.varetype;
	}

	public Boolean isTilHumanAnvendelse()
	{
		if (aTC == null) return null;
		return !aTC.startsWith("Q");
	}

	public void setAdministrationsvej(String administrationsvej)
	{
		this.administrationsvej = administrationsvej;
	}

	public void setAlfabetSekvensplads(String alfabetSekvensplads)
	{
		this.alfabetSekvensplads = alfabetSekvensplads;
	}

	public void setATC(String aTC)
	{
		this.aTC = aTC;
	}

	public void setDatoForAfregistrAfLaegemiddel(String datoForAfregistrAfLaegemiddel)
	{
		this.datoForAfregistrAfLaegemiddel = datoForAfregistrAfLaegemiddel;
	}

	public void setDrugid(Long drugid)
	{
		this.drugid = drugid;
	}

	public void setEgnetTilDosisdispensering(String egnetTilDosisdispensering)
	{
		this.egnetTilDosisdispensering = egnetTilDosisdispensering;
	}

	public void setFormKode(String formKode)
	{
		this.formKode = formKode;
	}

	public void setKarantaenedato(String karantaenedato)
	{
		this.karantaenedato = karantaenedato;
	}

	public void setKodeForYderligereFormOplysn(String kodeForYderligereFormOplysn)
	{
		this.kodeForYderligereFormOplysn = kodeForYderligereFormOplysn;
	}

	public void setLaegemiddelformTekst(String laegemiddelformTekst)
	{
		this.laegemiddelformTekst = laegemiddelformTekst;
	}

	public void setLaegemidletsSubstitutionsgruppe(String laegemidletsSubstitutionsgruppe)
	{
		this.laegemidletsSubstitutionsgruppe = laegemidletsSubstitutionsgruppe;
	}

	public void setMTIndehaver(Long mTIndehaver)
	{
		this.mTIndehaver = mTIndehaver;
	}

	public void setNavn(String navn)
	{
		this.navn = navn;
	}

	public void setRepraesentantDistributoer(Long repraesentantDistributoer)
	{
		this.repraesentantDistributoer = repraesentantDistributoer;
	}

	public void setSpecNummer(Long specNummer)
	{
		this.specNummer = specNummer;
	}

	public void setStyrkeEnhed(String styrkeEnhed)
	{
		this.styrkeEnhed = styrkeEnhed;
	}

	public void setStyrkeKlarTekst(String styrkeKlarTekst)
	{
		this.styrkeKlarTekst = styrkeKlarTekst;
	}

	public void setStyrkeNumerisk(Long styrkeNumerisk)
	{
		this.styrkeNumerisk = styrkeNumerisk;
	}

	public void setSubstitution(String substitution)
	{
		this.substitution = substitution;
	}

	public void setTrafikadvarsel(String trafikadvarsel)
	{
		this.trafikadvarsel = trafikadvarsel;
	}

	public void setVaredeltype(String varedeltype)
	{
		this.varedeltype = varedeltype;
	}

	public void setVaretype(String varetype)
	{
		this.varetype = varetype;
	}
}