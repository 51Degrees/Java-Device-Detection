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
package fiftyone.mobile.detection.test.type.comparison;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.common.UserAgentGenerator;
import static fiftyone.properties.MatchMethods.EXACT;
import static fiftyone.properties.MatchMethods.NONE;
import java.io.IOException;
import java.util.Iterator;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 */
@Category(TestType.TypeComparison.class)
public abstract class ComparisonBase {
    
    public abstract Provider getStreamProvider();
    public abstract Provider getMemoryProvider();
    
    private Match matchStream = null;
    private Match matchMemory = null;
    
    @Before
    public void initTest() {
        matchStream = getStreamProvider().createMatch();
        matchMemory = getMemoryProvider().createMatch();
    }
    
    @After
    public void cleanupTest() {
        matchStream = null;
        matchMemory = null;
        System.gc();
    }
    
    @Test
    @Category(TestType.TypeComparison.class)
    public void uniqueUserAgentsSingle() throws IOException {
        Iterator ite = UserAgentGenerator.getUniqueUserAgents().iterator();
        while (ite.hasNext()) {
            String userAgent = (String)ite.next();
            getStreamProvider().match(userAgent, matchStream);
            getMemoryProvider().match(userAgent, matchMemory);
            compareMatchObjects(matchStream, matchMemory);
        }
    }
    
    @Test
    public void badUserAgentsSingle() throws IOException {
        Iterator ite = UserAgentGenerator.getBadUserAgents().iterator();
        while (ite.hasNext()) {
            String userAgent = (String)ite.next();
            getStreamProvider().match(userAgent, matchStream);
            getMemoryProvider().match(userAgent, matchMemory);
            compareMatchObjects(matchStream, matchMemory);
        }
    }
    
    private void compareMatchObjects(Match m1, Match m2) throws IOException {
        // One can not be null without the other being null as well.
        if (m1 == null && m2 != null) {
            fail("One of match objects was null");
        }
        if (m2 == null && m1 != null) {
            fail("One of match objects was null");
        }
        // Verify both datasets have the same number of properties.
        if (getStreamProvider().dataSet.getProperties().size() != 
                getMemoryProvider().dataSet.getProperties().size()) {
            fail("Number of properties differs in Memory and Stream datasets");
        }
        // Check that second dataset has exactly the same set of properties as 
        // the first dataset.
        for (Property property : getStreamProvider().dataSet.getProperties()) {
            if (getMemoryProvider().dataSet.get(property.getName()) == null) {
                fail(property.getName() + " was not present in the memory "
                        + "dataset.");
            }
        }        
        for (Property property : getStreamProvider().dataSet.getProperties()) {
            // Compare detection results.
            Values valuesOne = m1.getValues(property);
            Values valuesTwo = m2.getValues(property);
            if (valuesOne == null && valuesTwo != null) {
                fail("One of the values was null.");
            }
            if (valuesTwo == null && valuesOne != null) {
                fail("One of the values was null");
            }
            if (valuesOne != null && valuesTwo != null) {
                assertTrue(valuesOne.toString().equals(valuesTwo.toString()));
            }
            // Compare device ID.
            assertTrue(m1.getDeviceId().equals(m2.getDeviceId()));
            // Compare detection method.
            assertTrue(m1.getMethod() == m2.getMethod());
            if (m1.getMethod() == m2.getMethod() && 
                    (m1.getMethod() != EXACT || m1.getMethod() != NONE)) {
                // Difference only taken into account for CLOSEST, NEAREST and 
                // NUMERIC detection methods.
                assertTrue(m1.getDifference() == m2.getDifference());
            }
        }
    }
}
