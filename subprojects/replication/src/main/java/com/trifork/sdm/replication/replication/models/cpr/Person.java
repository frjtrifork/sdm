package com.trifork.sdm.replication.replication.models.cpr;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import com.trifork.sdm.replication.replication.models.Record;

@Entity(name = "cpr/person/v1")
@Table(name = "Person")
public class Person extends Record {

	@Id
	@GeneratedValue
	@Column(name = "PersonPID")
	private BigInteger recordID;

	@Column(name = "Id")
	protected String id;

	@Column(name = "CPR")
	protected String cpr;

	@Column(name = "Koen")
	protected String koen;

	@Column(name = "Fornavn")
	protected String fornavn;

	@Column(name = "Mellemnavn")
	protected String mellemnavn;

	@Column(name = "Efternavn")
	protected String efternavn;

	@Column(name = "CoNavn")
	protected String coNavn;

	@Column(name = "Lokalitet")
	protected String lokalitet;

	@Column(name = "Vejnavn")
	protected String vejnavn;

	@Column(name = "Bygningsnummer")
	protected String bygningsnummer;

	@Column(name = "Husnummer")
	protected String husnummer;

	@Column(name = "Etage")
	protected String etage;

	@Column(name = "SideDoerNummer")
	protected String sideDoerNummer;

	@Column(name = "Bynavn")
	protected String bynavn;

	@Column(name = "Postnummer")
	protected BigInteger postnummer; // TODO: Why is this column BIGINT(12)?

	@Column(name = "PostDistrikt")
	protected String postdistrikt;

	@Column(name = "Status")
	protected String status;

	@Column(name = "GaeldendeCPR")
	protected String gaeldendeCPR;

	@Column(name = "Foedselsdato")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date foedselsdato;

	@Column(name = "Stilling")
	protected String stilling;

	@Column(name = "VejKode")
	protected BigInteger vejKode; // TODO: BIGINT(12)?

	@Column(name = "KommuneKode")
	protected BigInteger kommuneKode;

	@XmlTransient
	@Column(name = "ModifiedDate")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date modifiedDate;

	@XmlTransient
	@Column(name = "ValidFrom")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date validFrom;

	@XmlTransient
	@Column(name = "ValidTo")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date validTo;


	@Override
	public BigInteger getRecordID() {
		return recordID;
	}


	@Override
	public String getID() {
		return id;
	}


	@Override
	public Date getUpdated() {
		return modifiedDate;
	}
}