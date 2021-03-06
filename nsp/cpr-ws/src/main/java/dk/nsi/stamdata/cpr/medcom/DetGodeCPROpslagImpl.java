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
package dk.nsi.stamdata.cpr.medcom;

import static com.trifork.stamdata.Preconditions.checkNotNull;

import java.sql.SQLException;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.ws.Holder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.xml.ws.developer.SchemaValidation;
import com.trifork.stamdata.Fetcher;
import com.trifork.stamdata.Nullable;
import com.trifork.stamdata.jaxws.GuiceInstanceResolver.GuiceWebservice;
import com.trifork.stamdata.persistence.Record;
import com.trifork.stamdata.persistence.RecordFetcher;
import com.trifork.stamdata.persistence.Transactional;
import com.trifork.stamdata.specs.SikredeRecordSpecs;
import com.trifork.stamdata.specs.YderregisterRecordSpecs;

import dk.nsi.stamdata.cpr.PersonMapper;
import dk.nsi.stamdata.cpr.PersonMapper.ServiceProtectionLevel;
import dk.nsi.stamdata.cpr.SoapUtils;
import dk.nsi.stamdata.cpr.models.Person;
import dk.nsi.stamdata.jaxws.generated.DGWSFault;
import dk.nsi.stamdata.jaxws.generated.DetGodeCPROpslag;
import dk.nsi.stamdata.jaxws.generated.GetPersonInformationIn;
import dk.nsi.stamdata.jaxws.generated.GetPersonInformationOut;
import dk.nsi.stamdata.jaxws.generated.GetPersonWithHealthCareInformationIn;
import dk.nsi.stamdata.jaxws.generated.GetPersonWithHealthCareInformationOut;
import dk.nsi.stamdata.jaxws.generated.Header;
import dk.nsi.stamdata.jaxws.generated.PersonInformationStructureType;
import dk.nsi.stamdata.jaxws.generated.PersonWithHealthCareInformationStructureType;
import dk.nsi.stamdata.jaxws.generated.Security;
import dk.sosi.seal.model.SystemIDCard;


@WebService(endpointInterface="dk.nsi.stamdata.jaxws.generated.DetGodeCPROpslag")
@GuiceWebservice
@SchemaValidation
public class DetGodeCPROpslagImpl implements DetGodeCPROpslag
{
	private static final Logger logger = LoggerFactory.getLogger(DetGodeCPROpslagImpl.class);

	private static final String NS_DET_GODE_CPR_OPSLAG = "http://rep.oio.dk/medcom.sundcom.dk/xml/wsdl/2007/06/28/";
	private static final String NS_DGWS_1_0 = "http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd";
	private static final String NS_WS_SECURITY = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

	private final Fetcher fetcher;
    private final RecordFetcher recordFetcher;
    private final PersonMapper personMapper;
	private final String clientCVR;

    @Inject
	DetGodeCPROpslagImpl(Fetcher fetcher, RecordFetcher recordFetcher, PersonMapper personMapper, SystemIDCard card)
	{
		this.fetcher = fetcher;
        this.recordFetcher = recordFetcher;
        this.personMapper = personMapper;
		this.clientCVR = card.getSystemInfo().getCareProvider().getID();
	}

	// TODO: Headers should be set to outgoing. See BRS for correct way of
	// setting these.
	@Override
	@Transactional
	public GetPersonInformationOut getPersonInformation(@WebParam(name = "Security", targetNamespace = NS_WS_SECURITY, mode = WebParam.Mode.INOUT, partName = "wsseHeader") Holder<Security> wsseHeader, @WebParam(name = "Header", targetNamespace = NS_DGWS_1_0, mode = WebParam.Mode.INOUT, partName = "medcomHeader") Holder<Header> medcomHeader, @WebParam(name = "getPersonInformationIn", targetNamespace = NS_DET_GODE_CPR_OPSLAG, partName = "parameters") GetPersonInformationIn input) throws DGWSFault
	{
		SoapUtils.setHeadersToOutgoing(wsseHeader, medcomHeader);

		// 1. Check the white list to see if the client is authorized.

		String pnr = input.getPersonCivilRegistrationIdentifier();

		logAccess(pnr);

		// 2. Validate the input parameters.

		checkInputParameters(pnr);

		// 3. Fetch the person from the database.

		Person person = fetchPersonWithPnr(pnr);

		// We now have the requested person. Use it to fill in
		// the response.

		GetPersonInformationOut output = new GetPersonInformationOut();
		PersonInformationStructureType personInformation;

		personInformation = personMapper.map(person, ServiceProtectionLevel.AlwaysCensorProtectedData, PersonMapper.CPRProtectionLevel.DoNotCensorCPR);

		output.setPersonInformationStructure(personInformation);

		return output;
	}


	@Override
	@Transactional
	public GetPersonWithHealthCareInformationOut getPersonWithHealthCareInformation(@WebParam(name = "Security", targetNamespace = NS_WS_SECURITY, mode = WebParam.Mode.INOUT, partName = "wsseHeader") Holder<Security> wsseHeader, @WebParam(name = "Header", targetNamespace = NS_DGWS_1_0, mode = WebParam.Mode.INOUT, partName = "medcomHeader") Holder<Header> medcomHeader, @WebParam(name = "getPersonWithHealthCareInformationIn", targetNamespace = NS_DET_GODE_CPR_OPSLAG, partName = "parameters") GetPersonWithHealthCareInformationIn parameters) throws DGWSFault
	{
		SoapUtils.setHeadersToOutgoing(wsseHeader, medcomHeader);

		// 1. Check the white list to see if the client is authorized.

		String pnr = parameters.getPersonCivilRegistrationIdentifier();

		logAccess(pnr);

		// 2. Validate the input parameters.

		checkInputParameters(pnr);

		// 2. Fetch the person from the database.
		//
		// NOTE: Unfortunately the specification is defined so that we have to
		// return a fault if no person is found. We cannot change this to return
		// nil
		// which would be a nicer protocol.

		Person person = fetchPersonWithPnr(pnr);

		Record sikredeRecord = null;
		try 
		{
		    sikredeRecord = getSikredeRecord(pnr);
		}
		catch (SQLException e)
		{
            throw SoapUtils.newServerErrorFault(e);		    
		}
        
		Record yderRecord = null;
		if(sikredeRecord != null)
		{
		    try
		    {
		        yderRecord = getYderRecord((String) sikredeRecord.get("SYdernr"));
		    }
	        catch (SQLException e)
	        {
	            throw SoapUtils.newServerErrorFault(e);         
	        }
		}
		
		// If the relation is not found we have to use fake data to satisfy the
		// output format.
		
		GetPersonWithHealthCareInformationOut output = new GetPersonWithHealthCareInformationOut();
		PersonWithHealthCareInformationStructureType personWithHealthCareInformation = null;

		try
		{
			personWithHealthCareInformation = personMapper.map(person, sikredeRecord, yderRecord);
		}
		catch (DatatypeConfigurationException e)
		{
			throw SoapUtils.newServerErrorFault(e);
		}

		output.setPersonWithHealthCareInformationStructure(personWithHealthCareInformation);

		return output;
	}

	
	@Transactional
    private Record getSikredeRecord(String pnr) throws SQLException 
    {
        return recordFetcher.fetchCurrent(pnr, SikredeRecordSpecs.ENTRY_RECORD_SPEC, "CPRnr");
    }

	@Transactional
	private Record getYderRecord(String ydernummer) throws SQLException 
	{
	    return recordFetcher.fetchCurrent(ydernummer, YderregisterRecordSpecs.YDER_RECORD_TYPE, "YdernrYder");
	}

	// HELPERS

	private Person fetchPersonWithPnr(String pnr)
	{
		checkNotNull(pnr, "pnr");

		Person person;

		try
		{
			person = fetcher.fetch(Person.class, pnr);
		}
		catch (Exception e)
		{
			throw SoapUtils.newServerErrorFault(e);
		}

		// NOTE: Unfortunately the specification is defined so that we have to
		// return a
		// fault if no person is found. We cannot change this to return nil
		// which would
		// be a nicer protocol.

		if (person == null)
		{
			throw SoapUtils.newSOAPSenderFault(FaultMessages.NO_DATA_FOUND_FAULT_MSG);
		}

		return person;
	}


	// HELPERS

	private void checkInputParameters(@Nullable String pnr)
	{
		if (StringUtils.isBlank(pnr))
		{
			// This service should match functionality from a service that was
			// not DGWS protected.
			// Callers expect to be met with a SOAP sender fault.
			// Callers to the PVIT service should expect DGWS faults instead.
			throw SoapUtils.newSOAPSenderFault("PersonCivilRegistrationIdentifier was not set in request, but is required.");
		}
	}


	private void logAccess(String requestedCPR) throws DGWSFault
	{
		logger.info("type=auditlog, service=stamdata-cpr, client_cvr={}, requested_cpr={}", clientCVR, requestedCPR);
	}

}
