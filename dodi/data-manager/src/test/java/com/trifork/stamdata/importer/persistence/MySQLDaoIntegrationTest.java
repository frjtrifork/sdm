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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.junit.Before;
import org.junit.Test;

import com.trifork.stamdata.importer.config.ConnectionManager;
import com.trifork.stamdata.importer.util.Dates;
import com.trifork.stamdata.models.TemporalEntity;


public class MySQLDaoIntegrationTest extends AbstractMySQLIntegrationTest
{
	@Before
	public void setupTable() throws SQLException
	{
		Connection con = new ConnectionManager().getAutoCommitConnection();

        try
		{
			Statement stmt = con.createStatement();

			stmt.executeUpdate("DROP TABLE IF EXISTS SDE");
			stmt.executeUpdate("CREATE TABLE SDE(id VARCHAR(20) NOT NULL, data VARCHAR(20), date DATETIME, ModifiedDate DATETIME NOT NULL, ValidFrom DATETIME, ValidTo DATETIME, CreatedDate DATETIME)");
		}
		finally
		{
			ConnectionManager.closeQuietly(con);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDataset() throws Exception
	{
		CompleteDataset<SDE> dataset = new CompleteDataset<SDE>(SDE.class, t0, t1);
		dataset.add(new SDE(t0, Dates.THE_END_OF_TIME));
		Connection con = new ConnectionManager().getAutoCommitConnection();
		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset);
		DatabaseTableWrapper<SDE> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, t1));
		assertEquals(table.getCurrentRowValidFrom(), t0);
		assertEquals(table.getCurrentRowValidTo(), Dates.THE_END_OF_TIME);
		assertFalse(table.moveToNextRow());
		con.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDatasetX2() throws Exception
	{
		CompleteDataset<SDE> dataset = new CompleteDataset<SDE>(SDE.class, t0, t1);
		dataset.add(new SDE(t0, Dates.THE_END_OF_TIME));
		Connection con = new ConnectionManager().getAutoCommitConnection();
		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset);
		dao.persistCompleteDataset(dataset);
		DatabaseTableWrapper<?> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, t1));
		assertEquals(table.getCurrentRowValidFrom(), t0);
		assertEquals(table.getCurrentRowValidTo(), Dates.THE_END_OF_TIME);
		assertFalse(table.moveToNextRow());
		con.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDatasetChangedStringSameValidity() throws Exception
	{
		CompleteDataset<SDE> dataset1 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		CompleteDataset<SDE> dataset2 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		dataset1.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "a"));
		dataset2.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "b"));
		Connection con = new ConnectionManager().getAutoCommitConnection();
		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset1);
		dao.persistCompleteDataset(dataset2);
		DatabaseTableWrapper<?> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, t1));
		assertEquals(table.getCurrentRowValidFrom(), t0);
		assertEquals(Dates.THE_END_OF_TIME, table.getCurrentRowValidTo());
		assertEquals("b", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());
		con.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDatasetChangedDateSameValidity() throws Exception
	{
		CompleteDataset<SDE> dataset1 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		CompleteDataset<SDE> dataset2 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		dataset1.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "a", t3));
		dataset2.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "a", t4));
		Connection con = new ConnectionManager().getAutoCommitConnection();
		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset1);
		dao.persistCompleteDataset(dataset2);
		DatabaseTableWrapper<?> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, t1));
		assertEquals(table.getCurrentRowValidFrom(), t0);
		assertEquals(Dates.THE_END_OF_TIME, table.getCurrentRowValidTo());
		assertEquals(t4.getTime(), table.getCurrentRS().getTimestamp("date").getTime());
		assertFalse(table.moveToNextRow());
		con.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDatasetChangedDataNewValidFrom() throws Exception
	{
		CompleteDataset<SDE> dataset1 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		CompleteDataset<SDE> dataset2 = new CompleteDataset<SDE>(SDE.class, t1, t2);
		dataset1.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "a"));
		dataset2.add(new SDE(t1, Dates.THE_END_OF_TIME, "1", "b"));
		Connection con = new ConnectionManager().getAutoCommitConnection();
		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset1);
		dao.persistCompleteDataset(dataset2);
		DatabaseTableWrapper<?> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, t0)); // Get the old version
		assertEquals(t0, table.getCurrentRowValidFrom());
		assertEquals(t1, table.getCurrentRowValidTo());
		assertEquals("a", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());
		assertTrue(table.fetchEntitiesInRange(t2, t2)); // Get the new version
		assertEquals(table.getCurrentRowValidFrom(), t1);
		assertEquals(Dates.THE_END_OF_TIME, table.getCurrentRowValidTo());
		assertEquals("b", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());
		con.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistCompleteDatasetChangedDataNewValidToNoDataChange() throws Exception
	{
		CompleteDataset<SDE> dataset1 = new CompleteDataset<SDE>(SDE.class, t0, t1);
		CompleteDataset<SDE> dataset2 = new CompleteDataset<SDE>(SDE.class, t1, t2);
		CompleteDataset<SDE> dataset3 = new CompleteDataset<SDE>(SDE.class, t2, t1000);

		// Normal t0 -> THE_END_OF_TIME

		dataset1.add(new SDE(t0, Dates.THE_END_OF_TIME, "1", "a"));

		// Limit validTo to T1 no data change.

		dataset2.add(new SDE(t0, t1, "1", "a"));

		// Extend validTo to after THE_END_OF_TIME
		dataset3.add(new SDE(t0, t1000, "1", "a"));

		Connection con = new ConnectionManager().getAutoCommitConnection();

		AuditingPersister dao = new AuditingPersister(con);
		dao.persistCompleteDataset(dataset1);
		DatabaseTableWrapper<?> table = dao.getTable(SDE.class);
		assertTrue(table.fetchEntitiesInRange(t0, Dates.THE_END_OF_TIME));
		assertEquals(t0, table.getCurrentRowValidFrom());
		assertEquals(Dates.THE_END_OF_TIME, table.getCurrentRowValidTo());
		assertEquals("a", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());

		// Persist validTo = T1
		dao.persistCompleteDataset(dataset2);
		assertTrue(table.fetchEntitiesInRange(t0, Dates.THE_END_OF_TIME));
		assertEquals(t0, table.getCurrentRowValidFrom());
		assertEquals(t1, table.getCurrentRowValidTo());
		assertEquals("a", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());

		// Persist validTo = T1000
		dao.persistCompleteDataset(dataset3);
		assertTrue(table.fetchEntitiesInRange(t0, Dates.THE_END_OF_TIME));
		assertEquals(t0, table.getCurrentRowValidFrom());
		assertEquals(t1000, table.getCurrentRowValidTo());
		assertEquals("a", table.getCurrentRS().getString("data"));
		assertFalse(table.moveToNextRow());
		con.close();
	}


	@Entity
	public static class SDE implements TemporalEntity
	{
		Date validfrom, validto;
		String id = "1"; // default value
		String data = "a"; // default value
		Date date = Dates.toDate(2001, 1, 1, 1, 2, 3);

		public SDE(Date validFrom, Date validTo)
		{
			this.validfrom = validFrom;
			this.validto = validTo;
		}

		public SDE(Date validFrom, Date validTo, String id, String data)
		{
			this.validfrom = validFrom;
			this.validto = validTo;
			this.data = data;
			this.id = id;
		}

		public SDE(Date validFrom, Date validTo, String id, String data, Date date)
		{
			this.validfrom = validFrom;
			this.validto = validTo;
			this.data = data;
			this.id = id;
			this.date = date;
		}

		@Override
		public Date getValidFrom()
		{
			return validfrom;
		}

		@Override
		public Date getValidTo()
		{
			return validto;
		}

		@Column
		public String getData()
		{
			return data;
		}

		@Id
		@Column(name = "id")
		public Object getID()
		{
			return id;
		}

		@Column
		public Date getDate()
		{
			return date;
		}
	}
}
