/* *********************************************************************
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
 * ********************************************************************* */

package fiftyone.mobile.detection.helper;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.properties.MatchMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Minimal tests that some sensible values can be got from a provider
 */
public class ViableProvider {
    private static Logger logger = LoggerFactory.getLogger(ViableProvider.class.getName());

    private static final String TEST_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

    /**
     * Tests that a provider can match a couple of properties as a basic test
     * of viability - for Pattern
     * @param provider to test
     * @throws IOException - because provider.match can throw an exception
     */
    public static void ensureViableProvider(Provider provider) throws IOException {
        testString(provider);
        testMap(provider);
    }
    
    /**
     * Tests provider using a User-Agent string.
     * @param provider to test
     * @throws IOException - because provider.match can throw an exception
     */
    public static void testString(Provider provider) throws IOException {
        Match match = provider.match(TEST_USER_AGENT);
        logger.debug("string - " + TEST_USER_AGENT);
        assertEquals("Match method should be exact", MatchMethods.EXACT, match.getMethod());
        assertEquals("Is a mobile device", true, match.getValues("IsMobile").toBool());
        assertEquals("Screen width should be 640", 640.0, match.getValues("ScreenPixelsWidth").toDouble(),0);
    }
      
    /**
     * Tests provider using a HashMap containing a header and a User-Agent. 
     * @param provider to test
     * @throws IOException - because provider.match can throw an exception
     */
    public static void testMap(Provider provider) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("User-Agent", TEST_USER_AGENT);
        Match match = provider.match(map);
        logger.debug("map - " + TEST_USER_AGENT);
        assertEquals("Match method should be exact", MatchMethods.EXACT, match.getMethod());
        assertEquals("Is a mobile device", true, match.getValues("IsMobile").toBool());
        assertEquals("Screen width should be 640", 640.0, match.getValues("ScreenPixelsWidth").toDouble(),0);
    }
}
