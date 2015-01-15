package fiftyone.mobile.detection;

import fiftyone.mobile.detection.Match.MatchState;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * Provider used to perform a detection based on a user agent string.
 */
public class Provider {

    /**
     * A cache for user agents.
     */
    private Cache<String, MatchState> userAgentCache = null;

    /**
     * The total number of detections performed by the data set.
     * @return total number of detections performed by this data set
     */
    public long getDetectionCount() {
        return detectionCount;
    }
    private long detectionCount;
    /**
     * The number of detections performed using the method.
     */
    private final SortedList<MatchMethods, Long> methodCounts;
    /**
     * The data set associated with the provider.
     */
    public final Dataset dataSet;
    private Controller controller;

    /**
     * Builds a new provider with the embedded data set.
     *
     * @throws IOException indicates an I/O exception occurred
     */
    public Provider() throws IOException {
        this(MemoryFactory.read(new BinaryReader(getEmbeddedByteArray()), false), 0);
    }

    /**
     * Builds a new provider with the embedded data set and a cache with the
     * service internal specified.
     *
     * @param cacheServiceInterval cache service internal in seconds.
     * @throws IOException indicates an I/O exception occurred
     */
    public Provider(int cacheServiceInterval) throws IOException {
        this(MemoryFactory.read(
                new BinaryReader(getEmbeddedByteArray()), false), 
                cacheServiceInterval);
    }
    
    /**
     * Reads the embedded data into a byte array to be used as a byte buffer in
     * the factory.
     *
     * @return
     * @throws IOException
     */
    private static byte[] getEmbeddedByteArray() throws IOException {
        byte[] buffer = new byte[1048576];
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                DetectionConstants.EMBEDDED_DATA_RESOURCE_NAME);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count = input.read(buffer);
        while (count > 0) {
            output.write(buffer, 0, count);
            count = input.read(buffer);
        }
        return output.toByteArray();
    }

    /**
     * Constructs a new provided using the data set.
     *
     * @param dataSet Data set to use for device detection
     */
    public Provider(Dataset dataSet) {
        this(dataSet, new Controller(), 0);
    }

    /**
     * Constructs a new provided using the data set.
     *
     * @param cacheServiceInterval cache service internal in seconds.
     * @param dataSet Data set to use for device detection
     */
    public Provider(Dataset dataSet, int cacheServiceInterval) {
        this(dataSet, new Controller(), cacheServiceInterval);
    }

    /**
     * Constructs a new provider with the dataset, controller and cache 
     * specified.
     * @param dataSet
     * @param controller
     * @param cacheServiceInternal 
     */
    Provider(Dataset dataSet, Controller controller, int cacheServiceInternal) {
        this.dataSet = dataSet;
        this.controller = controller;
        this.methodCounts = new SortedList<MatchMethods, Long>();
        this.methodCounts.add(MatchMethods.CLOSEST, 0l);
        this.methodCounts.add(MatchMethods.NEAREST, 0l);
        this.methodCounts.add(MatchMethods.NUMERIC, 0l);
        this.methodCounts.add(MatchMethods.EXACT, 0l);
        this.methodCounts.add(MatchMethods.NONE, 0l);
        userAgentCache = cacheServiceInternal > 0 ? new Cache<String, MatchState>(cacheServiceInternal) : null;
    }

    /**
     * Creates a new match object to be used for matching.
     *
     * @return a match object ready to be used with the Match methods 
     */
    public Match createMatch() {
        return new Match(dataSet);
    }

    /**
     * For a given collection of HTTP headers returns a match containing
     * information about the capabilities of the device and it's components.
     * @param headers List of HTTP headers to use for the detection
     * @return a match for the target headers provided
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(final Map<String, String> headers) throws IOException {
        return match(headers, createMatch());
    }
    
    /**
     * For a given collection of HTTP headers returns a match containing
     * information about the capabilities of the device and it's components.
     * @param headers List of HTTP headers to use for the detection
     * @param match object created to store the results of the match
     * @return a match for the target headers provided
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(final Map<String, String> headers, Match match) throws IOException {
        // Get the match for the main user agent.
        match(headers.get(DetectionConstants.USER_AGENT_HEADER.toLowerCase()), match);
        
        // Get the user agent for the device if a secondary header is present.
        String deviceUserAgent = getDeviceUserAgent(headers);
        if (deviceUserAgent != null)
        {
            Match deviceMatch = match(deviceUserAgent);
            if (deviceMatch != null)
            {
                // Update the statistics about the matching process.
                match.signaturesCompared += deviceMatch.signaturesCompared;
                match.signaturesRead += deviceMatch.signaturesRead;
                match.stringsRead += deviceMatch.stringsRead;
                match.rootNodesEvaluated += deviceMatch.rootNodesEvaluated;
                match.nodesEvaluated += deviceMatch.nodesEvaluated;

                // Replace the Hardware and Software profiles with the ones from
                // the device match.
                for (int i = 0; i < match.getProfiles().length && i < deviceMatch.getProfiles().length; i++)
                {
                    if (match.getProfiles()[i].getComponent().getComponentId() <= 2 &&
                        match.getProfiles()[i].getComponent().getComponentId() == 
                            deviceMatch.getProfiles()[i].getComponent().getComponentId())
                    {
                        // Swap over the profiles if they're the same component.
                        match.getProfiles()[i] = deviceMatch.getProfiles()[i];
                    }
                }

                // Remove the signature as a single one is not being returned.
                match.setSignature(null);
            }
        }
        
        return match;
    }

    /**
     * For a given user agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent string representing the user agent to be identified
     * @return a match result for the target user agent
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(String targetUserAgent) throws IOException {
        return match(targetUserAgent, createMatch());
    }

    /**
     * For a given user agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent The user agent string to use as the target
     * @param match A match object created by a previous match, or via the
     * CreateMatch method.
     * @return a match containing information about the capabilities of the 
     * device and it's components
     * @throws IOException indicates and I/O exception occurred
     */
    public Match match(String targetUserAgent, Match match) throws IOException {
        MatchState state;

        if (userAgentCache != null && targetUserAgent != null) {
            state = userAgentCache.tryGetValue(targetUserAgent);
            if (state == null) {
                // The user agent has not been checked previously. Therefore perform
                // the match and store the results in the cache.
                match = matchNoCache(targetUserAgent, match);

                // Record the match state in the cache for next time.
                state = match.new MatchState(match);
                userAgentCache.setActive(targetUserAgent, state);
            } else {
                // The state of a previous match exists so the match should
                // be configured based on the results of the previous state.
                match.setState(state);
            }
            userAgentCache.setBackground(targetUserAgent, state);
        } else {
            // The cache does not exist so call the non caching method.
            matchNoCache(targetUserAgent, match);
        }
        return match;
    }

    private Match matchNoCache(String targetUserAgent, Match match) throws IOException {
        match.reset(targetUserAgent);

        controller.match(match);

        // Update the counts for the provider.
        detectionCount++;
        synchronized (methodCounts) {
            MatchMethods method = match.getMethod();
            Long count = methodCounts.get(method);
            long value = count.longValue();
            methodCounts.put(method, value++);
        }

        return match;
    }
    
    /**
     * Used to check other header fields in case a device user agent is being used
     * and returns the devices useragent string.
     * @param headers Collection of Http headers associated with the request.
     * @return the useragent string of the device.
     */
    private static String getDeviceUserAgent(Map<String, String> headers)
    {
        for(String current : DetectionConstants.DEVICE_USER_AGENT_HEADERS) {
            if (headers.get(current.toLowerCase()) != null) {
                return headers.get(current.toLowerCase());
            }
        }
        return null;
    }
}
