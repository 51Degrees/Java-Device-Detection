package fiftyone.mobile.detection;

import fiftyone.mobile.detection.Match.MatchState;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

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

    private boolean recordDetectionTime = false;
    
    /**
     * The total number of detections performed by the data set.
     * @return total number of detections performed by this data set
     */
    public long getDetectionCount() {
        return detectionCount.longValue();
    }
    private AtomicLong detectionCount;
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
     * @deprecated since embedded data file was removed from package. Use the 
     * provider constructor that requires a dataset created by the factory 
     * instead.
     */
    @Deprecated
    public Provider() throws IOException {
        throw new UnsupportedOperationException("No longer supported. Please "
            + "use the provider constructors with Factory created datasets.");
    }

    /**
     * Builds a new provider with the embedded data set and a cache with the
     * service internal specified.
     *
     * @param cacheServiceInterval cache service internal in seconds.
     * @throws IOException indicates an I/O exception occurred
     * @deprecated since embedded data was removed from package. Use the 
     * provider constructor that requires a dataset created by the factory 
     * instead.
     */
    @Deprecated
    public Provider(int cacheServiceInterval) throws IOException {
        throw new UnsupportedOperationException("No longer supported. Please "
            + "use the provider constructors with Factory created datasets.");
    }
    
    /**
     * Reads the embedded data into a byte array to be used as a byte buffer in
     * the factory.
     *
     * @return
     * @throws IOException
     * @deprecated since embedded data was removed from the package.
     */
    @Deprecated
    private static byte[] getEmbeddedByteArray() throws IOException {
        throw new UnsupportedOperationException("No longer supported.");
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
     * @param dataSet Data set to use for device detection
     * @param cacheSize
     */
    public Provider(Dataset dataSet, int cacheSize) {
        this(dataSet, new Controller(), cacheSize);
    }

    /**
     * Constructs a new provider with the dataset, controller and cache 
     * specified.
     * @param dataSet
     * @param controller
     * @param cacheServiceInternal 
     */
    Provider(Dataset dataSet, Controller controller, int cacheSize) {
        this.detectionCount = new AtomicLong();
        this.dataSet = dataSet;
        this.controller = controller;
        this.methodCounts = new SortedList<MatchMethods, Long>();
        this.methodCounts.add(MatchMethods.CLOSEST, 0l);
        this.methodCounts.add(MatchMethods.NEAREST, 0l);
        this.methodCounts.add(MatchMethods.NUMERIC, 0l);
        this.methodCounts.add(MatchMethods.EXACT, 0l);
        this.methodCounts.add(MatchMethods.NONE, 0l);
        userAgentCache = cacheSize > 0 ? new Cache<String, MatchState>(cacheSize) : null;
    }

    public double getPercentageCacheMisses() {
        if (userAgentCache != null) {
            return userAgentCache.getPercentageMisses();
        } else {
            return 0;
        }
    }
    
    /**
     * Returns the number of times the user agents cache was switched.
     * @return the number of times the user agents cache was switched.
     */
    public long getCacheSwitches() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheSwitches();
        } else {
            return 0;
        }
    }
    
    public double getCacheRequests() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheRequests();
        } else {
            return -1;
        }
    }
    
    public long getCacheMisses() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheMisses();
        } else {
            return -1;
        }
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
        

        if (headers == null || headers.isEmpty()) {
            // Empty headers all default match result.
            controller.matchDefault(match);
        } else {
            // Check if the headers passed to this function are also found 
            // in the headers list of the dataset.
            ArrayList<String> importantHeaders = new ArrayList<String>();
            for (String datasetHeader : dataSet.getHttpHeaders()) {
                // Check that the header from the dataset also exists in the
                // provided list of headers.
                if (!headers.containsKey(datasetHeader)) {
                    // Now check if thi is a duplicate header.
                    if (!importantHeaders.contains(datasetHeader))
                        importantHeaders.add(datasetHeader);
                }
            }
            
            if (importantHeaders.size() == 1) {
                // If only 1 header is important then return a simple single match.
                match(headers.get(importantHeaders.get(0)), match);
            } else {
                // Create matches for each of the headers.
                Map<String, Match> matches = matchForHeaders(match, headers, importantHeaders);
                
                // A list of new profiles to use with the match.
                Profile[] newProfiles = new Profile[dataSet.components.size()];
                int componentIndex = 0;
                
                for(Component component : dataSet.components) {
                    // See if any of the headers can be used for this
                    // components profile. As soon as one matches then
                    // stop and don't look at any more. They are ordered
                    // in preferred sequence such that the first item is 
                    // the most preferred.
                    
                    for (String localHeader : component.getHttpheaders()) {
                        Match headerMatch = matches.get(localHeader);
                        if (headerMatch != null) {
                            // Update the statistics about the matching process.
                            match.signaturesCompared += headerMatch.signaturesCompared;
                            match.signaturesRead += headerMatch.signaturesRead;
                            match.stringsRead += headerMatch.stringsRead;
                            match.rootNodesEvaluated += headerMatch.rootNodesEvaluated;
                            match.nodesEvaluated += headerMatch.nodesEvaluated;
                            match.elapsed += headerMatch.elapsed;
                            
                            // Set the profile for this component.
                            for (Profile profile : headerMatch.profiles) {
                                if (profile.getComponent() == component) {
                                    newProfiles[componentIndex] = profile;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    // If no profile could be found for the component
                    // then use the default profile.
                    if (newProfiles[componentIndex] == null) {
                        newProfiles[componentIndex] = component.getDefaultProfile();
                    }
                    
                    // Move to the next array element.
                    componentIndex++;
                }
                
                // Reset any fields that relate to the profiles assigned
                // to the match result.
                match.setSignature(null);
                match.results = null;
                
                // Replace the match profiles with the new ones.
                match.profiles = newProfiles;
            }
        }
        return match;
    }

    /**
     * Matches each of the required headers.
     * @param match
     * @param headers
     * @param importantHeaders HTTP headers that exist in the dataset as well 
     * as in the list of headers that were passed to the function.
     * @return A map of Header => Match entries.
     * @throws IOException 
     */
    private Map<String, Match> matchForHeaders(
            Match match, Map<String, String> headers, ArrayList<String> importantHeaders)
            throws IOException {
        Map<String, Match> matches = new HashMap<String, Match>();
        Match currentMatch = match;
        for (int i = 0; i < importantHeaders.size(); i++) {
            matches.put(importantHeaders.get(i), currentMatch != null ? currentMatch : createMatch());
            currentMatch = null;
        }
        //TODO: add parallel executio.
        for (Entry m : matches.entrySet()) {
            // At this point we have a collection of the String => Match objects
            // where Match objects are empty. Perform the Match for each String 
            // hence making all matches correspond to the User Agents.
            m.setValue(match((String)m.getKey(), (Match)m.getValue()));
        }
        return matches;
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
            //Increase cache requests.
            userAgentCache.incrementRequestsByOne();
            
            state = userAgentCache.tryGetValue(targetUserAgent);
            if (state == null) {
                // The user agent has not been checked previously. Therefore perform
                // the match and store the results in the cache.
                match = matchNoCache(targetUserAgent, match);

                // Record the match state in the cache for next time.
                state = match.new MatchState(match);
                //userAgentCache.setActive(targetUserAgent, state);
                userAgentCache.active.put(targetUserAgent, state);
                
                //Implement Atomic increase in misses.
                userAgentCache.incrementMissesByOne();
            } else {
                // The state of a previous match exists so the match should
                // be configured based on the results of the previous state.
                match.setState(state);
            }
            userAgentCache.addRecent(targetUserAgent, state);
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
        detectionCount.incrementAndGet();
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
