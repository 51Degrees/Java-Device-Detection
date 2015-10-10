package Performance;

import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.common.Results;
import fiftyone.mobile.detection.common.UserAgentGenerator;
import fiftyone.properties.MatchMethods;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import java.util.Calendar;
import static org.junit.Assert.fail;
import org.junit.Before;

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

public abstract class ArrayBase extends Base {

    public ArrayBase(String dataFile) {
        super(dataFile);
    }

    /**
     * Creates the data set to be used for the tests.
     */
    @Before
    public void setUp() {
        assertFileExists(super.dataFile);
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            super.dataSet = StreamFactory.create(
                    readAllBytes(super.dataFile));
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        super.setUpTime = (int)(Calendar.getInstance().getTimeInMillis() - 
                startTime);
        super.setUp();
    }
    
    protected Results badUserAgentsMulti(Iterable<Property> properties,
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsMulti(
            UserAgentGenerator.getBadUserAgents(), properties, maxDetectionTime);
        assertTrueMethodLessThan(results, MatchMethods.EXACT, 0.2);
        DetectionTestSupport.AssertCacheMissesBad(super.dataSet);
        return results;
    }

    protected Results badUserAgentsSingle(Iterable<Property> properties,
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsSingle(
            UserAgentGenerator.getBadUserAgents(), properties, maxDetectionTime);
        assertTrueMethodLessThan(results, MatchMethods.EXACT, 0.2);
        DetectionTestSupport.AssertCacheMissesBad(super.dataSet);
        return results;
    }

    protected Results randomUserAgentsMulti(Iterable<Property> properties,
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsMulti(
            UserAgentGenerator.getRandomUserAgents(), properties, maxDetectionTime);
        assertTrueMethodGreaterThan(results, MatchMethods.EXACT, 0.95);
        DetectionTestSupport.AssertCacheMissesGood(super.dataSet);
        return results;
    }

    protected Results randomUserAgentsSingle(Iterable<Property> properties, 
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsSingle(
            UserAgentGenerator.getRandomUserAgents(), properties, maxDetectionTime);
        assertTrueMethodGreaterThan(results, MatchMethods.EXACT, 0.95);
        DetectionTestSupport.AssertCacheMissesGood(super.dataSet);
        return results;
    }

    protected Results uniqueUserAgentsMulti(Iterable<Property> properties,
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsMulti(
            UserAgentGenerator.getUniqueUserAgents(), properties, maxDetectionTime);
        assertTrueMethodGreaterThan(results, MatchMethods.EXACT, 0.95);
        DetectionTestSupport.AssertCacheMissesGood(super.dataSet);
        return results;
    }

    protected Results uniqueUserAgentsSingle(Iterable<Property> properties,
            int maxDetectionTime) throws IOException {
        Results results = super.userAgentsSingle(
            UserAgentGenerator.getUniqueUserAgents(), properties, maxDetectionTime);
        assertTrueMethodGreaterThan(results, MatchMethods.EXACT, 0.95);
        DetectionTestSupport.AssertCacheMissesGood(super.dataSet);
        return results;
    }    
}