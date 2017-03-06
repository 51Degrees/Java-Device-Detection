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

import java.io.IOException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the example program prior to release. See illustration class for 
 * documentation and guidance.
 */
public class GettingStartedTest {
    
    static GettingStarted gs;
    
    @BeforeClass
    public static void setUp() throws IOException {
        gs = new GettingStarted();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        gs.close();
    }
    
    @Test
    public void testLiteGettingStartedMobileUA() throws IOException {
        String result = gs.detect(gs.mobileUserAgent);
        System.out.println("Mobile User-Agent: " + gs.mobileUserAgent);
        System.out.println("IsMobile: " + result);
        assertTrue(result.equals("True"));
    }
    
    @Test
    public void testLiteGettingStartedDesktopUA() throws IOException {
        String result = gs.detect(gs.desktopUserAgent);
        System.out.println("Desktop User-Agent: " + gs.desktopUserAgent);
        System.out.println("IsMobile: " + result);        
        assertTrue(result.equals("False"));
    }
    
    @Test
    public void testLiteGettingStartedMediahubUA() throws IOException {
        String result = gs.detect(gs.mediaHubUserAgent);
        System.out.println("MediaHub User-Agent: " + gs.mediaHubUserAgent);
        System.out.println("IsMobile: " + result);        
        assertTrue(result.equals("False"));
    }
}
