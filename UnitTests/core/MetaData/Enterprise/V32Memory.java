package MetaData.Enterprise;

import MetaData.MemoryBase;
import Properties.Constants;
import java.io.IOException;
import org.junit.Test;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patent 
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

public class V32Memory extends MemoryBase {
    public V32Memory() {
        super(Constants.ENTERPRISE_PATTERN_V32);
    }
    
    @Test
    public void EnterpriseV32Memory_RetrieveComponents() throws IOException { 
        super.retrieveComponents(); 
    }

    @Test
    public void EnterpriseV32Memory_RetrieveProperties() throws IOException { 
        super.retrieveProperties(); 
    }

    @Test
    public void EnterpriseV32Memory_RetrieveValues() throws IOException {
        super.retrieveValues(); 
    }

    @Test
    public void EnterpriseV32Memory_CheckPropertyCount() { 
        super.checkPropertyCount(160); 
    }

    @Test
    public void EnterpriseV32Memory_ValidatePropertiesHaveDescription() throws IOException { 
        super.validatePropertiesHaveDescription(); 
    }       
}
