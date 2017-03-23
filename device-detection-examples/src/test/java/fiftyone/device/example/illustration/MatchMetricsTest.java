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

import fiftyone.mobile.detection.Match;
import static fiftyone.properties.MatchMethods.CLOSEST;
import static fiftyone.properties.MatchMethods.EXACT;
import static fiftyone.properties.MatchMethods.NEAREST;
import static fiftyone.properties.MatchMethods.NUMERIC;
import java.io.IOException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the example program prior to release. See illustration class for 
 * documentation and guidance.
 */
public class MatchMetricsTest {
    
    static MatchMetrics gs;
    Match match;
    
    @BeforeClass
    public static void setUp() throws IOException {
        gs = new MatchMetrics();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        gs.close();
    }
    
    @Test
    public void testLiteGettingStartedMatchMetricsMobileUA() throws IOException {
        match = gs.provider.match(gs.mobileUserAgent);
        assertTrue(gs.getDeviceId(match) != null);
        if (gs.getMethod(match) == EXACT) {
            assertTrue(gs.getDifference(match) == 0);
        } else if ( gs.getMethod(match) == CLOSEST || 
                    gs.getMethod(match) == NUMERIC || 
                    gs.getMethod(match) == NEAREST) {
            assertTrue(gs.getDifference(match) != 0);
        }
        assertTrue(gs.getRank(match) > 0);
    }
    
    @Test
    public void testLiteGettingStartedMatchMetricsDesktopUA() throws IOException {
        match = gs.provider.match(gs.desktopUserAgent);
        assertTrue(gs.getDeviceId(match) != null);
        if (gs.getMethod(match) == EXACT) {
            assertTrue(gs.getDifference(match) == 0);
        } else if ( gs.getMethod(match) == CLOSEST || 
                    gs.getMethod(match) == NUMERIC || 
                    gs.getMethod(match) == NEAREST) {
            assertTrue(gs.getDifference(match) != 0);
        }
        assertTrue(gs.getRank(match) > 0);
    }
    
    @Test
    public void testLiteGettingStartedMatchMetricsMediahubUA() throws IOException {
        match = gs.provider.match(gs.mediaHubUserAgent);
        assertTrue(gs.getDeviceId(match) != null);
        if (gs.getMethod(match) == EXACT) {
            assertTrue(gs.getDifference(match) == 0);
        } else if ( gs.getMethod(match) == CLOSEST || 
                    gs.getMethod(match) == NUMERIC || 
                    gs.getMethod(match) == NEAREST) {
            assertTrue(gs.getDifference(match) != 0);
        }
        assertTrue(gs.getRank(match) > 0);
    }
}
