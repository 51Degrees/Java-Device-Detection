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

package fiftyone.mobile.test.common;

import fiftyone.mobile.Filename;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Package local class to bootstrap User-Agent strings for {@link UserAgentGenerator}
 * <p/>
 * Tests that use User-Agents will skip if no User-Agent file was found on initial load
 */
class UserAgentGeneratorLoader {
    private static Logger logger = LoggerFactory.getLogger(UserAgentGenerator.class);
    private static ArrayList<String> privateUserAgents = new ArrayList<String>();

    static {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(Filename.GOOD_USERAGENTS_FILE));
            String line = reader.readLine();
            while (line != null) {
                privateUserAgents.add(line);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            logger.error("Error reading the user agent test set {}", Filename.GOOD_USERAGENTS_FILE, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.error("Error closing the user agent test set {}", Filename.GOOD_USERAGENTS_FILE, ex);
                }
            }
        }
    }

    /**
     * Gets the User-Agent from the data source for the tests.
     *
     * @return array of User-Agents.
     */
    protected static ArrayList<String> getUserAgents() {
        Assert.assertFalse("There were no user agent values with which to carry out the tests", privateUserAgents.size() == 0);
        return privateUserAgents;
    }
}
