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


package dk.nsi.stamdata.views.doseringsforslag;

import static javax.persistence.TemporalType.TIMESTAMP;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import dk.nsi.stamdata.views.View;
import dk.nsi.stamdata.views.ViewPath;

@Entity
@XmlRootElement
@ViewPath("doseringsforslag/drugdosagestructurerelation/v1")
public class DrugDosageStructureRelation extends View
{

	@Id
	@Column(name = "DrugDosageStructureRelationPID")
	@XmlTransient
	@GeneratedValue
	protected BigInteger recordID;

	@Column(length = 22)
	protected String id;

	// Reference til releaseNumber i Version. Obligatorisk. Heltal, 15 cifre.
	protected long releaseNumber;

	// Lægemidlets drug id. Reference til drugId i drugs. Obligatorisk. Heltal,
	// 11 cifre.
	protected long drugId;

	// Reference til code i dosageStructure. Obligatorisk. Heltal, 11 cifre.
	@Column(length = 11)
	protected long dosageStructureCode;

	@Temporal(TIMESTAMP)
	protected Date validFrom;

	@Temporal(TIMESTAMP)
	protected Date validTo;

	@XmlTransient
	@Temporal(TIMESTAMP)
	protected Date modifiedDate;

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public BigInteger getRecordID()
	{
		return recordID;
	}

	@Override
	public Date getUpdated()
	{
		return modifiedDate;
	}
}
