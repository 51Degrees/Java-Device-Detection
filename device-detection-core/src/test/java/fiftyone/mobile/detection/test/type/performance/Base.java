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

package fiftyone.mobile.detection.test.type.performance;

import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.test.common.MatchProcessor;
import fiftyone.mobile.detection.test.common.Results;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.test.common.RetrievePropertiesProcessor;
import fiftyone.properties.MatchMethods;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class Base extends DetectionTestSupport {
    
    @Rule 
    public TestName currentTestName = new TestName();
    
    /**
     * Data set used to perform the tests on.
     */
    protected Dataset dataSet;
    
    /** 
     * The path to the data file to use to create the dataset.
     */
    protected final String dataFile;
    
    /**
     * @return maximum amount of time to setup the data set.
     */
    protected abstract int getMaxSetupTime();
    
    /**
     * Time taken in milliseconds to initialise the dataset.
     */
    protected int setUpTime;
    
    public Base(String dataFile) {
        this.dataFile = dataFile;
    }
    
    /**
     * Ensures the data set is disposed of correctly at the end of the test.
     * @throws Exception 
     */
    @After
    public void tearDown() throws Exception {
        disposing(true);
        System.out.printf("Disposed of data set from file '%s'\r\n", this.dataFile);
    }
    
    /**
     * Ensures the data set is disposed of correctly.
     * @throws Throwable 
     * @throws java.io.IOException 
     */
    @Override
    protected void finalize() throws Throwable, IOException {
        disposing(false);
        super.finalize();
    }

    /**
     * Ensures resources used by the data set are closed and memory released.
     * @param disposing 
     * @throws java.io.IOException 
     */
    protected void disposing(boolean disposing) throws IOException {
        if (dataSet != null) {
            dataSet.close();
            dataSet = null;
        }
    }
    
    public void setUp() {
        System.out.println();
        System.out.printf("Test: %s\r\n", currentTestName.getMethodName());
        System.out.printf("Setup test with file '%s'\r\n", dataFile);
    }
    
    public void initializeTime() {
        assertTrue(String.format(
            "Initialisation time '%dms' greater than maximum allowed '%dms'",
            this.setUpTime,
            this.getMaxSetupTime()),
            this.setUpTime < this.getMaxSetupTime());
    }

    protected Results userAgentsSingle(Iterable<String> userAgents,
        MatchProcessor processor) throws IOException {
        System.out.printf("Processor: %s\r\n", processor.getClass().getSimpleName());
        Provider provider = new Provider(this.dataSet);
        Results results = Results.detectLoopSingleThreaded(
            provider,
            userAgents,
            processor);
        reportMethods(results.methods);
        reportTime(results);
        return results;
    }

    protected Results userAgentsMulti(Iterable<String> userAgents,
            MatchProcessor processor) throws IOException {
        System.out.printf("Processor: %s\r\n", processor.getClass().getSimpleName());
        Provider provider = new Provider(this.dataSet);
        Results results =  Results.detectLoopMultiThreaded(
            provider,
            userAgents,
            processor);
        assertPool(provider);
        reportTime(results);
        reportMethods(results.methods);
        reportProvider(provider);
        return results;      
    }

    protected Results userAgentsMulti(Iterable<String> userAgents, 
            Iterable<Property> properties, int guidanceTime) throws IOException {
        Results results = userAgentsMulti(userAgents, new RetrievePropertiesProcessor(properties));
        System.out.printf("Values check sum: '%d'\r\n", results.checkSum.longValue());
        assertTrue(
            String.format("Average time of '%d' ms exceeded guidance time of '%d' ms",
                results.getAverageTime(),
                guidanceTime),
            results.getAverageTime() <= guidanceTime);
        return results;
    }

    protected Results userAgentsSingle(Iterable<String> userAgents, Iterable<Property> properties, int guidanceTime) throws IOException {
        Results results = userAgentsSingle(userAgents, new RetrievePropertiesProcessor(properties));
        System.out.printf("Values check sum: '%d'\r\n", results.checkSum.longValue());
        assertTrue(
            String.format("Average time of '%d' ms exceeded guidance time of '%d' ms",
                results.getAverageTime(),
                guidanceTime),
            results.getAverageTime() <= guidanceTime);
        return results;
    }    
        
    protected void assertTrueMethodLessThan(Results results, MatchMethods method, double maxPercentage) {
        assertTrue(String.format(
                "%s used '%.0f%%' below minimum threshold '%.0f%%'", 
                method,
                results.getMethodPercentage(method),
                maxPercentage),
                results.getMethodPercentage(method) < maxPercentage);
    }
    
    protected void assertTrueMethodGreaterThan(Results results, MatchMethods method, double minPercentage) {
        assertTrue(String.format(
                "%s used '%.0f%%' below minimum threshold '%.0f%%'", 
                method,
                results.getMethodPercentage(method),
                minPercentage),
                results.getMethodPercentage(method) > minPercentage);
    }
}