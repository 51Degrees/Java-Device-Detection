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

package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.*;
import fiftyone.properties.MatchMethods;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for the instantiation of MemoryFactory
 */
public class MemoryFactoryTest extends DetectionTestSupport {

    private static final String TEST_USER_AGENT = 
            "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

    @Test
    public void testCreate() throws Exception {
        File testFile = new File(Filename.LITE_PATTERN_V31);
        FileInputStream fileInputStream = new FileInputStream(testFile);
        try {
            //Dataset dataset = StreamFactory.create(testFile.getAbsolutePath(), false);
            Dataset dataset = MemoryFactory.create(fileInputStream);
            try {
                Provider provider = new Provider(dataset);
                ensureViableProvider(provider);
            } finally {
                if (dataset != null) dataset.close();
            }
        } finally {
            // check the stream can still be read i.e.
            // that it is still open (as specified by the method contract)
            int available = fileInputStream.available();
            fileInputStream.close();
        }
    }

    /**
     * Tests that a provider can match a couple of properties as a basic test
     * of viability.
     * @param provider the provider to test
     * @throws IOException
     */
    private void ensureViableProvider(Provider provider) throws IOException {
        Match match = provider.match(TEST_USER_AGENT);
        System.out.println(TEST_USER_AGENT);
        assertEquals("Match method should be exact", MatchMethods.EXACT, match.getMethod());
        assertEquals("Is a mobile device", true, match.getValues("IsMobile").toBool());
        assertEquals("Screen width should be 640", 640.0, match.getValues("ScreenPixelsWidth").toDouble(),0);
    }
}