package com.trifork.stamdata.replication.replication.views.dkma;

import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import com.trifork.stamdata.replication.replication.views.View;


@Entity(name = "dkma/administrationsvej/v1")
public class Administrationsvej extends View {

	@Id
	@GeneratedValue
	@Column(name = "AdministrationsvejPID")
	@XmlTransient
	private BigInteger recordID;

	@Column(name = "AdministrationsvejKode")
	private String id;

	@Column(name = "AdministrationsvejTekst")
	protected String tekst;

	@Column(name = "AdministrationsvejKortTekst")
	protected String kortTekst;

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

		return id;
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
