/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package fiftyone.mobile.test.type.metadata.enterprise;

import fiftyone.mobile.TestType;
import fiftyone.mobile.test.type.metadata.MemoryBase;
import fiftyone.mobile.Filename;
import java.io.IOException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(TestType.DataSetEnterprise.class)
public class V31EnterpriseMetadataMemoryTest extends MemoryBase {
    public V31EnterpriseMetadataMemoryTest() {
        super(Filename.ENTERPRISE_PATTERN_V31);
    }
    
    @Test
    @Category(TestType.DataSetEnterprise.class)
    @Override
    public void retrieveComponents() throws IOException {
        super.retrieveComponents(); 
    }

    @Test
    public void EnterpriseV31Memory_RetrieveProperties() throws IOException { 
        super.retrieveProperties(); 
    }

    @Test
    @Override
    public void retrieveValues() throws IOException {
        super.retrieveValues(); 
    }

    @Test
    public void checkPropertyCount() { 
        super.checkPropertyCount(160); 
    }

    @Test
    @Override
    public void validatePropertiesHaveDescription() throws IOException { 
        super.validatePropertiesHaveDescription(); 
    }
}