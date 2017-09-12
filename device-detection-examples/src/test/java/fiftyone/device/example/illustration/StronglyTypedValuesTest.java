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

package fiftyone.device.example.illustration;

import java.io.IOException;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the example program prior to release. See illustration class for 
 * documentation and guidance.
 */
public class StronglyTypedValuesTest {
    
    static StronglyTypedValues gs;
    
    @BeforeClass
    public static void setUp() throws IOException {
        gs = new StronglyTypedValues();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        gs.close();
    }
    
    @Test
    public void testLiteGettingStartedMobileUA() throws IOException {
        assertTrue(gs.isMobile(gs.mobileUserAgent));
    }
    
    @Test
    public void testLiteGettingStartedDesktopUA() throws IOException {
        assertFalse(gs.isMobile(gs.desktopUserAgent));
    }
    
    @Test
    public void testLiteGettingStartedMediahubUA() throws IOException {
        assertFalse(gs.isMobile(gs.mediaHubUserAgent));
    }
}
