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

import fiftyone.mobile.detection.entities.Signature;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class DynamicFiltersTest {
    
    static DynamicFilters df;
    
    @BeforeClass
    public static void setUp() throws IOException {
        df = new DynamicFilters();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        df.close();
    }
    
    @Test
    public void dynamicFilter() throws IOException {
        ArrayList<Signature> signatures;
        int signaturesOriginal = df.signatures.size();
        signatures = df.filterBy("IsMobile", "True", null);
        int signaturesIsMobile = signatures.size();
        signatures = df.filterBy("PlatformName", "Android", signatures);
        int signaturesPlatformname = signatures.size();
        signatures = df.filterBy("BrowserName", "Chrome", signatures);
        int signaturesBrowserName = signatures.size();
        
        assertTrue(signaturesOriginal >= signaturesIsMobile);
        assertTrue(signaturesIsMobile >= signaturesPlatformname);
        assertTrue(signaturesPlatformname >= signaturesBrowserName);
        
        for (Signature sig : signatures) {
            assertTrue(sig.getDeviceId() != null);
        }
    }
}
