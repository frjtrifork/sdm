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
package com.trifork.stamdata.importer.jobs.sikrede;

import com.trifork.stamdata.importer.jobs.sikrede.RecordSpecification.SikredeType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class SikredeXmlGeneratorTest {

    private RecordSpecification exampleRecordSpecification;
    private RecordXmlSchemaGenerator exampleXmlGenerator;

    @Before
    public void initialiseVariables()
    {
        this.exampleRecordSpecification = RecordSpecification.newSikredeFields(
                "Foo", SikredeType.NUMERICAL, 2,
                "Bar", SikredeType.ALFANUMERICAL, 10);
        exampleXmlGenerator = new RecordXmlSchemaGenerator(exampleRecordSpecification);
    }
    
    @Test
    public void testSimpleXmlDocumentGeneration() throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        RecordBuilder recordBuilder = new RecordBuilder(exampleRecordSpecification);
        Record record = recordBuilder.field("Foo", 42).field("Bar", "ABCDEFGH").build();
        Document document = exampleXmlGenerator.generateXml(record);
        
        String expected = 
                "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n" + 
                        "<Record>"+
                        "<Foo>42</Foo>" + 
                        "<Bar>ABCDEFGH</Bar>" + 
                        "</Record>";
        String actual = serializeDomDocument(document);
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyRecordGeneration()
    {
        Record record = new Record();
        exampleXmlGenerator.generateXml(record);
    }
    
    @Test
    public void testEmptySchemaRecordGeneration() throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        exampleRecordSpecification = RecordSpecification.newSikredeFields();
        exampleXmlGenerator = new RecordXmlSchemaGenerator(exampleRecordSpecification);
        RecordBuilder recordBuilder = new RecordBuilder(exampleRecordSpecification);
        Record record = recordBuilder.build();
        Document document = exampleXmlGenerator.generateXml(record);
        
        String expected = 
                "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n" + 
                        "<Record/>";
        String actual = serializeDomDocument(document);
        
        Assert.assertEquals(expected, actual);
    }
    
    private String serializeDomDocument(Document document) throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        return writer.writeToString(document);
    }
}
