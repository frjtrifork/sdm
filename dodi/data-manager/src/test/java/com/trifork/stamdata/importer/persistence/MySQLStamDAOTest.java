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


package com.trifork.stamdata.importer.persistence;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.trifork.stamdata.importer.jobs.takst.Takst;
import com.trifork.stamdata.importer.jobs.takst.TakstDataset;
import com.trifork.stamdata.importer.jobs.takst.model.DivEnheder;
import com.trifork.stamdata.importer.jobs.takst.model.Laegemiddel;
import com.trifork.stamdata.importer.persistence.AuditingPersister;
import com.trifork.stamdata.importer.persistence.DatabaseTableWrapper;
import com.trifork.stamdata.importer.persistence.DatabaseTableWrapper.StamdataEntityVersion;
import com.trifork.stamdata.importer.util.Dates;
import com.trifork.stamdata.models.TemporalEntity;


public class MySQLStamDAOTest
{

	private Takst takst;
	private Laegemiddel laegemiddel;
	private AuditingPersister dao;
	private DatabaseTableWrapper<?> laegemiddeltableMock;

	@Before
	public void setUp() throws Exception
	{

		takst = new Takst(Dates.toDate(2009, 7, 1), Dates.toDate(2009, 7, 14));
		// Add a dataset to the takst with one member
		List<Laegemiddel> list = new ArrayList<Laegemiddel>();
		laegemiddel = new Laegemiddel();
		laegemiddel.setDrugid(1l);
		laegemiddel.setNavn("Zymedolinatexafylitungebraekker");
		list.add(laegemiddel);
		TakstDataset<Laegemiddel> dataset = new TakstDataset<Laegemiddel>(takst, list, Laegemiddel.class);
		takst.addDataset(dataset, Laegemiddel.class);

		/*
		 * // Add an empty dataset to the takst (should be ignored)
		 * List<Pakning> tomListe = new ArrayList<Pakning>(); Dataset<Pakning>
		 * tomtDataset = new Dataset<Pakning>(takst, tomListe, Pakning.class);
		 * takst.addDataset(tomtDataset);
		 */
		// Add a dataset to the takst, which should be ignored because it is not
		// rootMember
		List<DivEnheder> enheder = new ArrayList<DivEnheder>();
		DivEnheder enhed = new DivEnheder();
		enhed.setTekst("millimol pr. gigajoule");
		TakstDataset<DivEnheder> hiddenDataset = new TakstDataset<DivEnheder>(takst, enheder, DivEnheder.class);
		takst.addDataset(hiddenDataset, DivEnheder.class);

		// ------ Setup database mocks -------
		Connection con = mock(Connection.class);
		AuditingPersister realDao = new AuditingPersister(con);
		dao = spy(realDao);
		laegemiddeltableMock = mock(DatabaseTableWrapper.class);
		doReturn(laegemiddeltableMock).when(dao).getTable(Laegemiddel.class);
	}

	@Test
	public void testPersistOneLaegemiddel() throws Exception
	{
		when(laegemiddeltableMock.fetchEntityConflicts(anyObject(), any(Date.class), any(Date.class))).thenReturn(false);
		// Simulate no existing entities

		dao.persistCompleteDataset(takst.getDatasets());

		// Verify that the new record is inserted
		verify(laegemiddeltableMock, times(1)).insertEntity(eq(laegemiddel), any(Date.class));
	}

	@Test
	public void testDeltaPutChanged() throws Exception
	{

		// Simulate that the entity is already present.
		when(laegemiddeltableMock.fetchEntityConflicts(anyObject(), any(Date.class), any(Date.class))).thenReturn(true);

		// Simulate that the existing row's validity range is 1950 to infinity.
		// So it must be updated.
		when(laegemiddeltableMock.getCurrentRowValidFrom()).thenReturn(Dates.toDate(1950, 01, 1));
		when(laegemiddeltableMock.getCurrentRowValidTo()).thenReturn(Dates.THE_END_OF_TIME);

		// Simulate that the entity has changed.
		when(laegemiddeltableMock.currentRowEquals(any(TemporalEntity.class))).thenReturn(false);

		dao.persistCompleteDataset(takst.getDatasets());

		// Verify that the new record is inserted
		verify(laegemiddeltableMock, times(1)).insertAndUpdateRow(eq(laegemiddel), any(Date.class));

		// Verify that the existing record is updated
		verify(laegemiddeltableMock, times(1)).updateValidToOnCurrentRow(eq(takst.getValidFrom()), any(Date.class));
	}

	@Test
	public void testDeltaPutUnchanged() throws Exception
	{
		// Simulate that the entity is already present.
		when(laegemiddeltableMock.fetchEntityConflicts(anyObject(), any(Date.class), any(Date.class))).thenReturn(true);

		// Simulate that the existing row's validity range is 1950 to infinity.
		when(laegemiddeltableMock.getCurrentRowValidFrom()).thenReturn(Dates.toDate(1950, 01, 1));
		when(laegemiddeltableMock.getCurrentRowValidTo()).thenReturn(Dates.THE_END_OF_TIME);

		// Simulate that the entity is unchanged.
		when(laegemiddeltableMock.currentRowEquals(any(TemporalEntity.class))).thenReturn(true);

		dao.persistCompleteDataset(takst.getDatasets());

		// Verify that the new record is inserted
		verify(laegemiddeltableMock, times(0)).insertEntity(eq(laegemiddel), any(Date.class));

		// Verify that the existing record is not updated
		verify(laegemiddeltableMock, times(0)).updateValidToOnCurrentRow(eq(takst.getValidFrom()), any(Date.class));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testDeltaPutRemoved() throws Exception
	{
		// An empty takst.

		takst = new Takst(Dates.toDate(2009, 7, 1), Dates.toDate(2009, 7, 14));
		// ..with an empty dataset
		TakstDataset<Laegemiddel> lmr = new TakstDataset(takst, new ArrayList<Laegemiddel>(), Laegemiddel.class);
		takst.addDataset(lmr, Laegemiddel.class);

		List<StamdataEntityVersion> sev = new ArrayList<StamdataEntityVersion>();
		// Simulate that there is one record
		StamdataEntityVersion sv = new StamdataEntityVersion();
		sv.id = 1;

		// Simulate that the existing row's validity range is 1950 to infinity.
		sv.validFrom = Dates.toDate(1950, 01, 1);
		sev.add(sv);

		when(laegemiddeltableMock.findEntitiesInRange(any(Date.class), any(Date.class))).thenReturn(sev);

		dao.persistCompleteDataset(takst.getDatasets());

		// Verify that the existing record is updated
		verify(laegemiddeltableMock, times(1)).updateValidToOnEntityVersion(eq(Dates.toDate(2009, 7, 1)), any(StamdataEntityVersion.class), any(Date.class));
	}
}
