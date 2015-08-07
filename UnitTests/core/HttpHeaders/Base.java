package HttpHeaders;

import Memory.*;
import common.Results;
import common.UserAgentGenerator;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.MatchMethods;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TestName;

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
    
    @Rule 
    public TestName currentTestName = new TestName();
    
    /**
     * Data set used to perform the tests on.
     */
    protected Dataset dataSet;
    
    /**
     * Used to monitor memory usage through the test.
     */
    protected Measurements memory;
    
    /** 
     * The path to the data file to use to create the dataset.
     */
    protected final String dataFile;
    
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
    }
    
    /**
     * Ensures the data set is disposed of correctly.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        disposing(false);
        super.finalize();
    }

    /**
     * Ensures resources used by the data set are closed and memory released.
     * @param disposing 
     */
    protected void disposing(boolean disposing) {
        if (dataSet != null) {
            dataSet.dispose();
            dataSet = null;
        }
    }
    
    public void setUp() {
        System.out.printf("Setup test with file '%s'\r\n", dataFile);
    }

    protected Results Process(String userAgentPattern, String devicePattern, Validation state) throws IOException
    {
        dataSet.resetCache();
        Provider provider = new Provider(dataSet);
        Match match = provider.createMatch();
        Results results = new Results();
        Random random = new Random(0);
        String httpHeaders[] = new String[dataSet.getHttpHeaders().length - 1];
        
        // Copy the HTTP headers from the data set to the local list ignoring
        // any which are the User-Agent header.
        int index = 0, dataSetIndex = 0;
        while (index < httpHeaders.length) {
            if (dataSet.getHttpHeaders()[dataSetIndex].equals("User-Agent") == false) {
                httpHeaders[index] = dataSet.getHttpHeaders()[dataSetIndex];
                index++;
            }
            dataSetIndex++;
        }

        // Loop through setting 2 user agent headers.
        Iterator<String> userAgentIterator = UserAgentGenerator.getUserAgentsIterable(userAgentPattern).iterator();
        Iterator<String> deviceIterator = UserAgentGenerator.getUserAgentsIterable(devicePattern).iterator();
        while(userAgentIterator.hasNext()&&
            deviceIterator.hasNext())
        {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(httpHeaders[random.nextInt(httpHeaders.length)], deviceIterator.next());
            headers.put("User-Agent", userAgentIterator.next());
            provider.match(headers, match);
            assertTrue("Signature not equal null", match.getSignature() == null);
            assertTrue("Match difference not equal to zero", match.getDifference() == 0);
            assertTrue("Match method not equal to Exact", match.method == MatchMethods.EXACT);
            validate(match, state);
            results.methods.get(match.method).incrementAndGet();
        }

        return results;
    }

    private static void validate(Match match, Validation validation) throws IOException
    {
        for(Entry<Property, Pattern> test : validation.entrySet())
        {
            String value = match.getValues(test.getKey()).toString();
            if (test.getValue().matcher(value).matches() == false)
            {
                fail(String.format(
                    "HttpHeader test failed for Property '%s' and test '%s' with result '%s'",
                    test.getKey(),
                    test.getValue(),
                    value));
            }
        }
    }
}