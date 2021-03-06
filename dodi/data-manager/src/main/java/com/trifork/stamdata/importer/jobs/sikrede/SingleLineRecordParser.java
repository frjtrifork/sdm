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

import com.trifork.stamdata.Preconditions;
import com.trifork.stamdata.persistence.Record;
import com.trifork.stamdata.persistence.RecordBuilder;
import com.trifork.stamdata.persistence.RecordSpecification;
import com.trifork.stamdata.persistence.RecordSpecification.FieldSpecification;
import com.trifork.stamdata.persistence.RecordSpecification.RecordFieldType;

import static com.trifork.stamdata.Preconditions.checkArgument;
import static java.lang.String.format;

public class SingleLineRecordParser
{
    private final RecordSpecification recordSpecification;
    
    public SingleLineRecordParser(RecordSpecification recordSpecification)
    {
        this.recordSpecification = recordSpecification;
    }
    
    public Record parseLine(String line)
    {
        checkArgument(line.length() == recordSpecification.acceptedTotalLineLength(),
                format("Supplied line had length %s but only lines of length %d are accepted", line.length(), recordSpecification.acceptedTotalLineLength()));

        RecordBuilder builder = new RecordBuilder(recordSpecification);

        int offset = 0;

        for (FieldSpecification fieldSpecification: recordSpecification.getFieldSpecs())
        {
            String subString = line.substring(offset, offset + fieldSpecification.length);
            
            if (fieldSpecification.type == RecordFieldType.ALPHANUMERICAL)
            {
                builder.field(fieldSpecification.name, subString.trim());
            }
            else if (fieldSpecification.type == RecordFieldType.NUMERICAL)
            {
                // This will potentially throw a runtime exception on bad input.
                //
                builder.field(fieldSpecification.name, Integer.parseInt(subString.trim()));
            }
            else
            {
                throw new AssertionError("Should match exactly one of the types alphanumerical or numerical.");
            }
            
            offset += fieldSpecification.length;
        }
        
        return builder.build();
    }
}
