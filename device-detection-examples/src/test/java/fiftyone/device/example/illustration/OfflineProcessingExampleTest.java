/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

package fiftyone.device.example.illustration;

import fiftyone.device.example.Shared;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests the example program prior to release. See illustration class for 
 * documentation and guidance.
 */
public class OfflineProcessingExampleTest {

    OfflineProcessingExample example;
    File outputFile;

    @Before
    public void setUp() throws IOException {
        example = new OfflineProcessingExample();
        outputFile = File.createTempFile("test-",".tmp");
    }

    @After
    public void tearDown() throws IOException {
        example.close();
        assertTrue("Could not delete output file", outputFile.delete());
    }

    @Test
    public void testProcessCsv() throws Exception {
        assertEquals(outputFile.length(), 0);
        example.processCsv(Shared.getGoodUserAgentsFile(), outputFile.getAbsolutePath());
        assertNotEquals(outputFile.length(), 0);
    }
}