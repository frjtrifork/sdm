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


package dk.nsi.stamdata.views.dkma;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import dk.nsi.stamdata.views.View;
import dk.nsi.stamdata.views.ViewPath;


@Entity
@XmlRootElement
@ViewPath("dkma/dosisdispensering/v1")
public class OplysningerOmDosisdispensering extends View {

	@Id
	@GeneratedValue
	@Column(name = "OplysningerOmDosisdispenseringPID")
	@XmlTransient
	private BigInteger recordID;

	@Column(name = "Varenummer")
	protected BigInteger id; // TODO: BigInt Why?

	@Column(name = "DrugID")
	protected BigInteger drugId; // TODO: BigInt Why?

	@Column(name = "LaegemidletsSubstitutionsgruppe")
	protected String substitutionsgruppe;

	@Column(name = "MindsteAIPPrEnhed")
	protected BigInteger mindsteAIPPrEnhed;

	@Column(name = "MindsteRegisterprisEnh")
	protected BigInteger mindsteRegisterprisEnhed;

	@Column(name = "TSPPrEnhed")
	protected BigInteger tspPrEnhed;

	@Column(name = "BilligsteDrugid")
	protected BigInteger billigsteDrugid;

	@Column(name = "KodeForBilligsteDrugid")
	// TODO: Why VARCHAR(1)?
	protected String KodeForBilligsteDrugid;

	// Metadata

	@XmlTransient
	@Column(name = "ModifiedDate")
	private Date modifiedDate;

	@Column(name = "ValidFrom")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date validFrom;

	@Column(name = "ValidTo")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date validTo;

	@Override
	public String getId() {

		return id.toString();
	}

	@Override
	public Date getUpdated() {

		return modifiedDate;
	}

	@Override
	public BigInteger getRecordID() {

		return recordID;
	}
}
