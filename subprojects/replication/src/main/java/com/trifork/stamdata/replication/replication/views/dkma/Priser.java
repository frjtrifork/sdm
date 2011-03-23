package com.trifork.stamdata.replication.replication.views.dkma;

import static javax.persistence.TemporalType.TIMESTAMP;

import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import com.trifork.stamdata.replication.replication.views.View;
import com.trifork.stamdata.replication.util.Namespace;


@Entity(name = "dkma/pris/v1")
@XmlRootElement(namespace = Namespace.STAMDATA_3_0)
@XmlAccessorType(XmlAccessType.FIELD)
public class Priser extends View {

	@Id
	@GeneratedValue
	@Column(name = "PriserPID")
	@XmlTransient
	private BigInteger recordID;

	@Column(name = "Varenummer")
	protected String varenummer;

	@Column(name = "apoteketsIndkoebspris")
	protected BigInteger apoteketsIndkoebspris;

	@Column(name = "Registerpris")
	protected BigInteger registerpris;

	@Column(name = "ekspeditionensSamledePris")
	protected BigInteger ekspeditionensSamledePris;

	@Column(name = "tilskudspris")
	protected BigInteger tilskudspris;

	@Column(name = "LeveranceprisTilHospitaler")
	protected BigInteger leveranceprisTilHospitaler;

	@Column(name = "IkkeTilskudsberettigetDel")
	protected BigInteger ikkeTilskudsberettigetDel;

	// Metadata

	@XmlTransient
	@Column(name = "ModifiedDate")
	private Date modifiedDate;

	@Column(name = "ValidFrom")
	@Temporal(TIMESTAMP)
	protected Date validFrom;

	@Column(name = "ValidTo")
	@Temporal(TIMESTAMP)
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
