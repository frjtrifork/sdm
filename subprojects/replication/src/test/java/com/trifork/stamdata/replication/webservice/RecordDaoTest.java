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

package com.trifork.stamdata.replication.webservice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.trifork.stamdata.replication.DatabaseHelper;
import com.trifork.stamdata.views.View;
import com.trifork.stamdata.views.cpr.Folkekirkeoplysninger;

public class RecordDaoTest
{
	private static StatelessSession session;
	private long nextTick = new Date().getTime();
	private RecordDao dao;

	@BeforeClass
	public static void init() throws Exception
	{
		DatabaseHelper db = new DatabaseHelper(Folkekirkeoplysninger.class);
		session = db.openStatelessSession();
		session.createQuery("delete from Folkekirkeoplysninger").executeUpdate();
	}

	@Before
	public void setUp() {
		dao = new RecordDao(session);
		session.beginTransaction();
	}

	@After
	public void tearDown() {
		session.getTransaction().rollback();
	}

	@Test
	public void findsAllRecordsIfWithinLimit() {
		createFolkekirkeoplysninger();
		createFolkekirkeoplysninger();
		createFolkekirkeoplysninger();
		createFolkekirkeoplysninger();
		
		List<Folkekirkeoplysninger> result = findPage(Folkekirkeoplysninger.class, "0", yesterday(), null, 5);
		
		assertEquals(4, result.size());
	}
	
	@Test
	public void cutsOffAtGivenLimit() {
		for (int i=0; i<10; i++) {
			createFolkekirkeoplysninger();
		}
		
		List<Folkekirkeoplysninger> result = findPage(Folkekirkeoplysninger.class, "0", yesterday(), null, 5);
		
		assertEquals(5, result.size());
	}

	@SuppressWarnings("unchecked")
	public <T extends View> List<T> findPage(Class<T> type, String recordId, Date modifiedDate, String clientId, int limit) {
		ScrollableResults scrollableResults = dao.findPage(type, recordId, modifiedDate, limit);
		List<T> result = new ArrayList<T>();
		while (!scrollableResults.isLast()) {
			scrollableResults.next();
			result.add((T) scrollableResults.get(0));
		}
		return result;
	}

	private void createFolkekirkeoplysninger() {
		Folkekirkeoplysninger folkekirkeoplysninger = new Folkekirkeoplysninger();
		folkekirkeoplysninger.cpr = "1234567890";
		folkekirkeoplysninger.modifiedDate = nextDate();
		folkekirkeoplysninger.createdDate = nextDate();
		folkekirkeoplysninger.validFrom = nextDate();
		folkekirkeoplysninger.forholdsKode = "A";
		session.insert(folkekirkeoplysninger);
	}
	
	private Date yesterday() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}
	
	private Date nextDate() {
		nextTick += 1000;
		return new Date(nextTick);
	}
}