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

package fiftyone.device.example.illustration;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class AllProfilesTest {
    AllProfiles example;
    File outputFile;

    @Before
    public void setUp() throws IOException {
        example = new AllProfiles();
        outputFile = File.createTempFile("test-",".tmp");
    }

    @After
    public void tearDown() throws IOException {
        example.close();
        assertTrue("Could not delete output file", outputFile.delete());
    }

    @Test
    public void testAllProfiles() throws Exception {
        assertEquals(outputFile.length(), 0);
        example.run(outputFile.getAbsolutePath());
        assertNotEquals(outputFile.length(), 0);
    }
}