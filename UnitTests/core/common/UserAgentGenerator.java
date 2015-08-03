package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import static org.junit.Assert.fail;

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

public class UserAgentGenerator {

    private static ArrayList<String> privateUserAgents;
    
    private static final Object lock = new Object();
    
    private static final Random random = new Random();
    
    private static ArrayList<String> getUserAgents() {
        if (privateUserAgents == null) {
            synchronized(lock) {
                if (privateUserAgents == null) {
                    ArrayList<String> userAgents = new ArrayList<String>();
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(
                            new FileReader("../../data/20000 User Agents.csv"));
                        String line = reader.readLine();
                        while (line != null) {
                            userAgents.add(line);
                            line = reader.readLine();
                        }
                    }
                    catch(IOException ex) {
                        fail(ex.getMessage());
                    }
                    finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                                fail(ex.getMessage());
                            }
                        }
                    }
                    privateUserAgents = userAgents;
                }
            }
        }
        return privateUserAgents;
    }
        
    /**
     * Returns a random user agent which may also have been randomised.
     * @return a random user agent which may also have been randomised.
     */
    public static String getRandomUserAgent(int randomness) {
        String value = getUserAgents().get(random.nextInt(getUserAgents().size()));
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
    
    public static Iterable<String> getRandomUserAgents() {
        return getUserAgentsIterable(0);
    }

    public static Iterable<String> getUniqueUserAgents()
    {
        return getUserAgents();
    }

    public static Iterable<String> getBadUserAgents() {
        return getUserAgentsIterable(10);
    }
    
    public static Iterable<String> getUserAgentsIterable(final int randomness)
    {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int count = 0;
                    @Override
                    public boolean hasNext() {
                        return count < UserAgentGenerator.getUserAgents().size();
                    }
                    @Override
                    public String next() {
                        count++;
                        return UserAgentGenerator.getRandomUserAgent(randomness);
                    }
                    @Override
                    public void remove() {
                        // Do nothing.
                    }
                };
            }
        };
    }
}
