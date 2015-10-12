/*
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
 */

package fiftyone.mobile.detection.test.common;

import java.text.Normalizer;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;

public class UserAgentGenerator extends UserAgentGeneratorLoader {

    // Provides access to random numbers when randomising User-Agents.
    private static final Random random = new Random();

    /**
     * Returns a random user agent which may also have been randomised.
     *
     * @param randomness a randomness factor
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
            // TODO should this not be done on load? why not needed for all other tests?
            value = Normalizer.normalize(new String(bytes), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
        }
        return value;
    }

    /**
     * Gets the User-Agents in a random order and may include duplicates.
     *
     * @return iterator returning random valid User-Agents.
     */
    public static Iterable<String> getRandomUserAgents() {
        return getUserAgentsIterable(0);
    }

    /**
     * Gets the User-Agents in the order they appear in the source data file.
     *
     * @return iterator configured to return User-Agents in order.
     */
    public static Iterable<String> getUniqueUserAgents() {
        return getUserAgents();
    }

    /**
     * Invalid User-Agents for testing bad input data which is close to but
     * not quite a real User-Agent.
     *
     * @return iterator configured to return random bad User-Agents.
     */
    public static Iterable<String> getBadUserAgents() {
        return getUserAgentsIterable(10);
    }

    /**
     * Returns an iterator for the available number of random User-Agent each
     * with an amount of randomness applied to them.
     *
     * @param randomness number of random characters to alter.
     * @return iterator to return random User-Agents.
     */
    public static Iterable<String> getUserAgentsIterable(final int randomness) {
        return getUserAgentsIterable(getUserAgents().size(), randomness);
    }

    /**
     * Returns an iterator for the required number of random User-Agent each
     * with an amount of randomness applied to them.
     *
     * @param requiredCount number of User-Agents required.
     * @param randomness    number of random characters to alter.
     * @return iterator to return random User-Agents.
     */
    public static Iterable<String> getUserAgentsIterable(final int requiredCount, final int randomness) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int count = 0;

                    @Override
                    public boolean hasNext() {
                        return count < requiredCount;
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

    /**
     * Returns user agents that match the pattern provided.
     *
     * @param pattern regular expression used to filter the user agents
     *                returned.
     * @return a string iterable returning user agents that match the pattern.
     */
    public static Iterable<String> getUserAgentsIterable(final String pattern) {
        // TODO it would be quicker if the pattern were supplied precompiled by the caller
        final Pattern regex = Pattern.compile(pattern);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int nextStartIndex = 0;
                    String nextUserAgent = fetchNextUserAgent();

                    String fetchNextUserAgent() {
                        for (int index = nextStartIndex;
                             index < UserAgentGenerator.getUserAgents().size();
                             index++) {
                            String userAgent = UserAgentGenerator.getUserAgents().get(index);
                            if (regex.matcher(userAgent).matches()) {
                                nextStartIndex = index + 1;
                                return userAgent;
                            }
                        }
                        return null;
                    }

                    @Override
                    public boolean hasNext() {
                        return nextUserAgent != null;
                    }

                    @Override
                    public String next() {
                        String userAgent = nextUserAgent;
                        nextUserAgent = fetchNextUserAgent();
                        return userAgent;
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
