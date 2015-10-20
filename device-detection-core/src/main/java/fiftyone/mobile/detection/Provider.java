/* *********************************************************************
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
 * ********************************************************************* */
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.cache.Cache;
import fiftyone.properties.MatchMethods;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Profile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provider used to perform a detection based on a user agent string.
 */
public class Provider {

    /**
     * A cache for user agents if required.
     */
    private Cache<String, MatchResult, MatchState> userAgentCache = null;

    /**
     * True if the detection time should be recorded in the Elapsed property
     * of the DetectionMatch object.      * 
     */
    private final boolean recordDetectionTime;
    
    /**
     * The total number of detections performed by the data set.
     * @return total number of detections performed by this data set
     */
    public long getDetectionCount() {
        return detectionCount.longValue();
    }
    private final AtomicLong detectionCount = new AtomicLong(0);
    
    /**
     * The number of detections performed using the method.
     */
    private final SortedList<MatchMethods, Long> methodCounts;
    
    /**
     * The data set associated with the provider.
     */
    public final Dataset dataSet;

    /**
     * Constructs a new Provider using the data set without a cache.
     * @param dataSet to use for device detection
     */
    public Provider(Dataset dataSet) {
        this(dataSet, 0);
    }

    /**
     * Constructs a new Provider using the data set, with a cache of the size 
     * provided.
     * @param dataSet to use for device detection
     * @param cacheSize to be used with the provider, 0 for no cache
     */
    public Provider(Dataset dataSet, int cacheSize) {
        this(dataSet, false, cacheSize);
    }

    /**
     * Constructs a new Provider using the data set, with a cache of the size 
     * provided, and recording detection time if flag set.
     * @param dataSet to use for device detection
     * @param recordDetectionTime true if the detection time should be recorded
     * @param cacheSize to be used with the provider, 0 for no cache
     */
    Provider(Dataset dataSet, boolean recordDetectionTime, int cacheSize) {
        this.recordDetectionTime = recordDetectionTime;
        this.dataSet = dataSet;

        // Initialise HashMap with default size and a rirective to re-hash only
        // when capacity exceeds initial.
        int numberOfMethods = MatchMethods.values().length;
        this.methodCounts = new SortedList<MatchMethods, Long>(numberOfMethods, 1);
        this.methodCounts.add(MatchMethods.CLOSEST, 0l);
        this.methodCounts.add(MatchMethods.NEAREST, 0l);
        this.methodCounts.add(MatchMethods.NUMERIC, 0l);
        this.methodCounts.add(MatchMethods.EXACT, 0l);
        this.methodCounts.add(MatchMethods.NONE, 0l);
        
        userAgentCache = cacheSize > 0 ? new Cache<String, MatchResult, MatchState>(cacheSize) : null;
    }

    /**
     * @return the percentage of requests for user agents which were not already
     * contained in the cache.
     */
    public double getPercentageCacheMisses() {
        if (userAgentCache != null) {
            return userAgentCache.getPercentageMisses();
        } else {
            return 0;
        }
    }
    
    /**
     * @return the number of times the user agents cache was switched.
     */
    public long getCacheSwitches() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheSwitches();
        } else {
            return 0;
        }
    }
    
    /**
     * @return number of requests to the cache.
     */
    public double getCacheRequests() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheRequests();
        } else {
            return 0;
        }
    }
    
    /**
     * @return number of cache misses.
     */
    public long getCacheMisses() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheMisses();
        } else {
            return -1;
        }
    }
    
    /**
     * Creates a new match instance to be used for matching.
     * @return a match instance ready to be used with the Match methods 
     */
    public Match createMatch() {
        return new Match(this);
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
    public Match match(final Map<String, String> headers, Match match) 
            throws IOException {
        
        if (headers == null || headers.isEmpty()) {
            // Empty headers all default match result.
            Controller.matchDefault(match.state);
        } else {
            // Check if the headers passed to this function are also found 
            // in the headers list of the dataset.
            ArrayList<String> importantHeaders = new ArrayList<String>();
            for (String datasetHeader : dataSet.getHttpHeaders()) {
                // Check that the header from the dataset also exists in the
                // provided list of headers.
                if (headers.containsKey(datasetHeader)) {
                    // Now check if this is a duplicate header.
                    if (!importantHeaders.contains(datasetHeader)) {
                        importantHeaders.add(datasetHeader);
                    }
                }
            }
            
            if (importantHeaders.size() == 1) {
                // If only 1 header is important then return a simple single match.
                match(headers.get(importantHeaders.get(0)), match);
            } else {
                // Create matches for each of the headers.
                Map<String, MatchState> matches = matchForHeaders(match, headers, importantHeaders);
                
                // Set the profile for each component from the headers provided.
                for(Component component : dataSet.components) {
                    // Get the profile for the component.                    
                    Profile profile = getMatchingHeaderProfile(match.state, matches, component);
                    
                    // Add the profile found, or the default one if not found.
                    match.state.getExplicitProfiles().add(profile == null ? 
                            component.getDefaultProfile() : profile);
                }
                
                // Reset any fields that relate to the profiles assigned
                // to the match result or that can't contain a value when
                // HTTP headers are used.
                match.state.setSignature(null);
                match.state.setTargetUserAgent(null);
            }
        }
        return match;
    }

    /**
     * See if any of the headers can be used for this components profile. As
     * soon as one matches then stop and don't look at any more. They are 
     * ordered in preferred sequence such that the first item is the most 
     * preferred.
     * @param masterState current working state of the matching process
     * @param matches map of HTTP header names and match states
     * @param component component to be retrieved
     * @return Profile for the component provided from the matches for each 
     * header
     */
    private static Profile getMatchingHeaderProfile(MatchState masterState, 
            Map<String, MatchState> matches, Component component) 
            throws IOException {
        Profile result = null;
        for (String header : component.getHttpheaders())
        {
            MatchState headerState = matches.get(header);
            if (headerState != null &&
                headerState.getSignature() != null)
            {
                result = processMatchedHeaderProfile(
                        masterState, headerState, component);
            }
        }
        return result;
    }
    
    /**
     * Updates the masterState with the header masterState and returns the 
     * profile for the component requested.
     * @param masterState current working state of the matching process
     * @param headerState state for the specific header
     * @param component the profile returned should relate to
     * @return profile related to the component from the header state
     * @throws IOException 
     */
    private static Profile processMatchedHeaderProfile(MatchState masterState, 
            MatchState headerState, Component component) 
            throws IOException {
        
        Profile result = null;
        
        // Merge the header state with the master state.
        masterState.merge(headerState);

        // Return the profile for this component.
        int profileIndex = 0; 
        Profile[] profiles = headerState.getSignature().getProfiles();
        while (result == null &&
               profileIndex < profiles.length) {
            if (profiles[profileIndex].getComponent() == component) {
                result = profiles[profileIndex];
            }
            profileIndex++;
        }    
        
        return result;
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
        match.setResult(match(targetUserAgent, match.state));
        return match;
    }

    /**
     * Sets the state to the result of the match for the target User-Agent.
     * @param targetUserAgent User-Agent to be matched
     * @param state current working state of the matching process
     * @throws IOException 
     */
    void matchNoCache(String targetUserAgent, MatchState state) 
            throws IOException {
        
        long startNanoseconds = 0;
        
        state.reset(targetUserAgent);

        if (recordDetectionTime) {
            startNanoseconds = System.nanoTime();
        }
        
        Controller.match(state);
        
        if (recordDetectionTime) {
            state.setElapsed(System.nanoTime() - startNanoseconds);
        }

        // Update the counts for the provider.
        detectionCount.incrementAndGet();
        synchronized (methodCounts) {
            MatchMethods method = state.getMethod();
            methodCounts.put(method, methodCounts.get(method) + 1);
        }
    }
    
    /**
     * For each of the important HTTP headers provides a mapping to a 
     * match result.
     * @param match The single match instance passed into the match method.
     * @param headers The HTTP headers available for matching.
     * @param importantHeaders HTTP header names important to the match process.
     * @return A map of HTTP headers and match instances containing results 
     * for them.
     * @throws IOException 
     */
    private Map<String, MatchState> matchForHeaders(
            Match match, Map<String, String> headers, ArrayList<String> importantHeaders)
            throws IOException {
        // Relates HTTP header names to match resutls.
        Map<String, MatchState> matches = new HashMap<String, MatchState>();
        
        // Set the header name and match state for each
        // important header.        
        for(String headerName : importantHeaders) {
            matches.put(headerName, new MatchState(
                    match, headers.get(headerName)));
        }
        
        // Using each of the match instances pass the value to the match method 
        // and set the results.
        for (Entry<String, MatchState> m : matches.entrySet()) {
            // At this point we have a collection of the String => Match objects
            // where Match objects are empty. Perform the Match for each String 
            // hence making all matches correspond to the User Agents.
            match(headers.get(m.getKey()), m.getValue());
        }
        return matches;
    }

    /**
     * For a given user agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent The user agent string to use as the target
     * @param state information used to process the match
     * @return a match containing information about the capabilities of the 
     * device and it's components
     * @throws IOException indicates and I/O exception occurred
     */
    private IMatchResult match(String targetUserAgent, MatchState state) 
            throws IOException {
        IMatchResult result;
        if (userAgentCache != null) {
            // Fetch the item using the cache.
            result = userAgentCache.get(targetUserAgent, state);
        } else {
            // The cache does not exist so call the non caching method.
            matchNoCache(targetUserAgent, state);
            result = state;
        }
        return result;
    }    
}
