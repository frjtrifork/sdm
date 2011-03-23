package com.trifork.stamdata.replication.replication.views.dkma;

import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import com.trifork.stamdata.replication.replication.views.View;
import com.trifork.stamdata.replication.util.Namespace;


@Entity(name = "dkma/substitutionaflaegemidlerudenfastpris/v1")
@XmlRootElement(namespace = Namespace.STAMDATA_3_0)
@XmlAccessorType(XmlAccessType.FIELD)
public class SubstitutionAfLaegemidlerUdenFastPris extends View {

	@Id
	@GeneratedValue
	@Column(name = "SubstitutionAfLaegemidlerUdenFastPrisPID")
	@XmlTransient
	private BigInteger recordID;

	@Column(name = "Varenummer")
	protected BigInteger varenummer;

	@Column(name = "Substitutionsgruppenummer")
	protected BigInteger substitutionsgruppenummer;

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

		return varenummer.toString();
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
