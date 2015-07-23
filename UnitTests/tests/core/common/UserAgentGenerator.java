package tests.core.common;

import java.text.Normalizer;
import java.util.Random;

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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
/**
 *
 */
public class UserAgentGenerator {
    private static final String[] userAgents = null;
    
    private static Random random = new Random();
    
    public static UserAgentIterator<String> getEnumerable(int count, String pattern) {
        int counter = 0;
        return null;
    }
    
    /**
     * Returns a random user agent which may also have been randomised.
     * @return a random user agent which may also have been randomised.
     */
    private static String getRandomUserAgent(int randomness) {
        String value = userAgents[random.nextInt(userAgents.length)];
        if (randomness > 0) {
            byte[] bytes = value.getBytes();
            for (int i = 0; i < randomness; i++) {
                int indexA = random.nextInt(value.length());
                int indexB = random.nextInt(value.length());
                byte temp = bytes[indexA];
                bytes[indexA] = bytes[indexB];
                bytes[indexB] = temp;
            }
            value = Normalizer.normalize(new String(bytes), Normalizer.Form.NFD)
                              .replaceAll("[^\\p{ASCII}]", "");
        }
        return value;
    }
    
    public static void getRandomUserAgents() {
        
    }
}
