/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 */

package fiftyone.mobile.detection.test.type.metadata.lite;

import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.type.metadata.MemoryBase;
import fiftyone.mobile.detection.Filename;
import java.io.IOException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(TestType.DataSetLite.class)
public class V31LiteMetadataMemoryTest extends MemoryBase {
    public V31LiteMetadataMemoryTest() {
        super(Filename.LITE_PATTERN_V31);
    }
            
    @Test
    @Category(TestType.DataSetLite.class)
    @Override
    public void retrieveComponents() throws IOException {
        super.retrieveComponents(); 
    }

    @Test
    @Override
    public void retrieveProperties() throws IOException { 
        super.retrieveProperties(); 
    }

    @Test
    @Override
    public void retrieveValues() throws IOException {
        super.retrieveValues(); 
    }

    @Test
    public void checkPropertyCount() { 
        super.checkPropertyCount(57); 
    }

    @Test
    @Override
    public void validatePropertiesHaveDescription() throws IOException { 
        super.validatePropertiesHaveDescription(); 
    }
}
