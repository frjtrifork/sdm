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
package com.trifork.stamdata.authorization;

import com.google.common.collect.ImmutableSet;

import dk.nsi.stamdata.testing.MockSecureTokenService;
import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.model.*;
import dk.sosi.seal.pki.SOSITestFederation;
import dk.sosi.seal.vault.ClasspathCredentialVault;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

import static com.google.common.collect.Lists.newArrayList;
import static com.trifork.stamdata.authorization.SOSITestConstants.*;
import static dk.sosi.seal.model.AuthenticationLevel.VOCES_TRUSTED_SYSTEM;
import static dk.sosi.seal.model.constants.SubjectIdentifierTypeValues.CVR_NUMBER;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RequestProcessorIntegrationTest {

	private static IDCard idCard;
	private static SOSIFactory factory;
	private static JAXBContext context;

	@Mock
	AuthorizationDao authorizationDao;

	@BeforeClass
	public static void fetchIDCard() throws Exception {

	    context = JAXBContext.newInstance(AuthorizationRequestStructure.class, AuthorizationResponseStructure.class);
	    idCard = MockSecureTokenService.createSignedSystemIDCard(TEST_CVR);
	    factory = MockSecureTokenService.createFactory();
	}

	@Test
	public void should_allow_authorized_requests_with_zero_assosiated_authorizations() throws Exception {

		Request request = factory.createNewRequest(false, null);
		request.setIDCard(idCard);

		AuthorizationRequestStructure authorizationRequest = new AuthorizationRequestStructure();
		authorizationRequest.cpr = "1234567890";

		Document replyXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		context.createMarshaller().marshal(authorizationRequest, replyXML);

		request.setBody(replyXML.getDocumentElement());

		StringWriter w = new StringWriter();
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(request.serialize2DOMDocument()), new StreamResult(w));
		
		RequestProcessor processor = new RequestProcessor(factory, ImmutableSet.of(TEST_CVR), context.createMarshaller(), context.createUnmarshaller(), authorizationDao);

		Reply reply = processor.process(request);

		assertFalse(reply.isFault());

		AuthorizationResponseStructure authorizationResponse = (AuthorizationResponseStructure) context.createUnmarshaller().unmarshal(reply.getBody());

		assertThat(authorizationResponse.cpr, equalTo(authorizationRequest.cpr));
		assertThat(authorizationResponse.authorizations, nullValue());
	}

	@Test
	public void should_return_multiple_assosiated_authorizations() throws Exception {

		Request request = factory.createNewRequest(false, null);
		request.setIDCard(idCard);

		AuthorizationRequestStructure authorizationRequest = new AuthorizationRequestStructure();
		authorizationRequest.cpr = "1234567890";

		when(authorizationDao.getAuthorizations("1234567890")).thenReturn(newArrayList(new Authorization("1234567890", "Anders", "Petersen", "12345", "1234"), new Authorization("1234567890", "Anders", "Petersen", "23456", "5151")));

		Document replyXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		context.createMarshaller().marshal(authorizationRequest, replyXML);

		request.setBody(replyXML.getDocumentElement());

		RequestProcessor processor = new RequestProcessor(factory, ImmutableSet.of(TEST_CVR), context.createMarshaller(), context.createUnmarshaller(), authorizationDao);

		Reply reply = processor.process(request);

		printXML(reply.getBody());

		AuthorizationResponseStructure authorizationResponse = (AuthorizationResponseStructure) context.createUnmarshaller().unmarshal(reply.getBody());

		assertThat(authorizationResponse.firstName, equalTo("Anders"));
		assertThat(authorizationResponse.lastName, equalTo("Petersen"));
		assertThat(authorizationResponse.cpr, equalTo("1234567890"));
		assertThat(authorizationResponse.authorizations.size(), equalTo(2));
		assertThat(authorizationResponse.authorizations.get(0).authorizationCode, equalTo("12345"));
		assertThat(authorizationResponse.authorizations.get(0).educationCode, equalTo("1234"));
		assertThat(authorizationResponse.authorizations.get(1).authorizationCode, equalTo("23456"));
		assertThat(authorizationResponse.authorizations.get(1).educationCode, equalTo("5151"));
		assertThat(authorizationResponse.authorizations.get(1).educationName, equalTo("Fysioterapeut"));
	}

	public void printXML(Element doc) throws TransformerException {

		// Set up the transformer to write the output string
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty("indent", "yes");
		StringWriter sw = new StringWriter();
		Result result = new StreamResult(sw);

		Source source = new DOMSource(doc);

		// Do the transformation and output
		transformer.transform(source, result);
	}

	public static String send(String urlString, Node node) throws Exception {

		URL url = new URL(urlString);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Prepare for SOAP

		connection.setRequestMethod("POST");
		connection.setRequestProperty("SOAPAction", "\"\"");
		connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8;");

		// Send the request XML.

		connection.setDoOutput(true);
				
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(node), new StreamResult(connection.getOutputStream()));

		// Read the response.

		if (connection.getResponseCode() < 400) {
			return streamToString(connection.getInputStream());
		}
		else {
			throw new RuntimeException(streamToString(connection.getErrorStream()));
		}
	}

	public static String streamToString(InputStream in) {
		return new Scanner(in).useDelimiter("\\A").next();
	}
}
