package API;

import fiftyone.mobile.detection.Match;
import static fiftyone.properties.MatchMethods.EXACT;
import fiftyone.mobile.detection.Provider;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * ********************************************************************* */

public abstract class Base {
    
    protected static Provider provider;
    protected static Match match;
    
    public Base() throws IOException {
    }
    
    @Test
    public void providerInitTest() {
        assertTrue(provider != null);
    }
    
    @Test
    public void datasetnameTest() throws IOException {
        assertTrue(provider.dataSet.getName().equals("Lite"));
    }
    
    @Test
    public void numberOfPropertiesTest() {
        assertTrue(provider.dataSet.getProperties().size() > 50
        && provider.dataSet.getProperties().size() < 80);
    }
    
    @Test
    public void testKnownUserAgent() throws IOException {
        String userAgent = "Mozilla/5.0 (BlackBerry; U; BlackBerry 9900; en) "
                + "AppleWebKit/534.11+ (KHTML, like Gecko) Version/7.1.0.346 "
                + "Mobile Safari/534.11+";
        match = provider.match(userAgent);
        assertTrue(match.method == EXACT);
    }
    
    @Test
    public void testKnownUserAgentProperties() {
        
    }

    @Test
    public void NullUserAgent() throws IOException {
        match = null;
        match = provider.match((String)null);
        assertTrue(match != null);
    }
}
