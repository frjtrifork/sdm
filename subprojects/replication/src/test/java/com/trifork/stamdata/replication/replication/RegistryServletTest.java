package com.trifork.stamdata.replication.replication;

import static java.lang.Integer.parseInt;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Provider;
import com.trifork.stamdata.replication.mocks.MockEntity;
import com.trifork.stamdata.replication.replication.views.View;
import com.trifork.stamdata.replication.security.SecurityManager;


public class RegistryServletTest {

	private RegistryServlet servlet;

	private HttpServletRequest request;
	private HttpServletResponse response;

	private SecurityManager securityManager;
	private Map<String, Class<? extends View>> registry;
	private Map<String, Class<? extends View>> mappedClasses;
	private RecordDao recordDao;
	private AtomFeedWriter writer;
	private String requestPath;
	private String countParam;
	private List<MockEntity> records;
	private String acceptHeader;
	private boolean authorized;
	private String offsetParam;
	private String nextOffset;

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUp() throws Exception {

		registry = new HashMap<String, Class<? extends View>>();

		Provider securityManagerProvider = mock(Provider.class);
		securityManager = mock(SecurityManager.class);
		when(securityManagerProvider.get()).thenReturn(securityManager);

		Provider recordDaoProvider = mock(Provider.class);
		recordDao = mock(RecordDao.class);
		when(recordDaoProvider.get()).thenReturn(recordDao);

		Provider writerProvider = mock(Provider.class);
		writer = mock(AtomFeedWriter.class);
		when(writerProvider.get()).thenReturn(writer);

		servlet = new RegistryServlet(registry, securityManagerProvider, recordDaoProvider, writerProvider);

		setUpValidRequest();
	}

	@Test
	public void Should_accept_valid_request() throws Exception {

		get();

		verify(response).setStatus(200);
		verify(writer).write(eq("foo/bar/v1"), eq(records), any(OutputStream.class), eq(false));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void Should_deny_access_if_the_client_is_not_authorized_for_the_requested_view() throws Exception {

		authorized = false;
		get();
		verify(response).setStatus(401);
		verify(writer, never()).write(anyString(), any(List.class), any(OutputStream.class), Mockito.anyBoolean());
	}

	@Test
	public void Should_return_a_web_link_for_the_next_page_if_there_are_more_records() throws Exception {
		
		countParam = "2";
		get();
		verify(response).addHeader("Link", String.format("<stamdata://foo/bar/v1?offset=%s>; rel=\"next\"", nextOffset));
	}

	@Test
	public void Should_not_return_a_web_link_if_there_are_no_more_records() throws Exception {

		countParam = "2";

		records = new ArrayList<MockEntity>();
		records.add(mock(MockEntity.class)); // Only one record.

		get();

		verify(response, never()).setHeader(eq("Link"), anyString());
	}

	@Test
	public void Should_return_fast_infoset_if_the_user_requests_it() throws Exception {

		acceptHeader = "application/atom+fastinfoset";

		get();

		verify(response).setContentType("application/atom+fastinfoset");
		verify(writer).write(eq("foo/bar/v1"), eq(records), any(OutputStream.class), eq(true));
	}

	@Test
	public void Should_return_xml_if_the_user_requests_it() throws Exception {

		acceptHeader = "application/atom+xml";
		
		get();
		
		verify(response).setContentType("application/atom+xml");
		verify(writer).write(eq("foo/bar/v1"), eq(records), any(OutputStream.class), eq(false));
	}

	@Test
	public void Should_return_records_from_the_correct_offset() throws Exception {

		get();
		
		verify(recordDao).findPage(MockEntity.class, "2222222222", new Date(1111111111000L), 2);
	}

	// HELPER METHODS

	public void get() throws Exception {

		when(securityManager.authorize(request)).thenReturn(authorized);
		when(recordDao.findPage(MockEntity.class, "2222222222", new Date(1111111111000L), parseInt(countParam))).thenReturn(records);
		when(request.getPathInfo()).thenReturn(requestPath);
		when(request.getHeader("Accept")).thenReturn(acceptHeader);
		when(request.getParameter("offset")).thenReturn(offsetParam);
		when(request.getParameter("count")).thenReturn(countParam);
		registry.putAll(mappedClasses);

		servlet.doGet(request, response);
	}

	public void setUpValidRequest() throws Exception {

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		requestPath = "/foo/bar/v1";
		
		authorized = true;
		
		mappedClasses = new HashMap<String, Class<? extends View>>();
		mappedClasses.put("foo/bar/v1", MockEntity.class);

		countParam = "2";
		offsetParam = "11111111112222222222";
		nextOffset = "11111111113333333333";

		records = new ArrayList<MockEntity>();
		records.add(mock(MockEntity.class));
		records.add(mock(MockEntity.class));
		when(records.get(1).getOffset()).thenReturn(nextOffset);
		
		ServletOutputStream outputStream = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(outputStream);

		acceptHeader = "application/atom+xml";
	}
}