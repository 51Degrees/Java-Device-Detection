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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Exposes several match methods to be used for device detection.
 * <p>
 * Provider requires a {@link Dataset} object connected to one of the 51Degrees 
 * device data files in order to perform device detection. Can be created like:
 * <ul>
 *  <li>For use with Stream factory:
 *  <p>{@code Provider p = new Provider(StreamFactory.create("path_to_file"));}
 *  <li>For use with Memory factory:
 *  <p>{@code Provider p = new Provider(MemoryFactory.create("path_to_file"));}
 * </ul>
 * For explanation on the difference between stream and memory see:
 * <a href="https://51degrees.com/support/documentation/pattern">
 * how device detection works</a> "Modes of operation" section.
 * <p>
 * Match methods return a {@link Match} object that contains detection results 
 * for a specific User-Agent string, collection of HTTP headers of a device Id.
 * Use it to retrieve detection results like: 
 * {@code match.getValues("IsMobile");}
 * <p>
 * You can access the underlying data set like: 
 * {@code provider.dataset.publised} to retrieve various meta information like 
 * the published date and the next update date as well as the list of various 
 * entities like {@link fiftyone.mobile.detection.entities.Profile 
 * profiles} and {@link fiftyone.mobile.detection.entities.Property properties}.
 * <p>
 * Remember to {@link Dataset#close() close} the underlying data set when 
 * program finishes to release resources and remove file lock.
 */
public class Provider {

    /**
     * A cache for User-Agents if required.
     */
    private Cache<String, MatchResult> userAgentCache = null;

    /**
     * True if the detection time should be recorded in the Elapsed property
     * of the DetectionMatch object.
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
        
        userAgentCache = 
                cacheSize > 0 ? new Cache<String, MatchResult>(cacheSize) : null;
    }

    /**
     * @return the percentage of requests for User-Agents which were not already
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
     * @return the number of times the User-Agents cache was switched.
     * 
     * Caching switching is no longer used.
     */
    @Deprecated
    public long getCacheSwitches() {
        return 0;
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
     * @return a match instance ready to be used with the Match methods.
     */
    public Match createMatch() {
        return new Match(this);
    }

    /**
     * For a given collection of HTTP headers returns a match containing
     * information about the capabilities of the device and it's components.
     * 
     * @param headers List of HTTP headers to use for the detection.
     * @return {@link Match} object with detection results.
     * @throws IOException if there was a problem accessing data file.
     */
    public Match match(final Map<String, String> headers) throws IOException {
        return match(headers, createMatch());
    }
    
    /**
     * For a given collection of HTTP headers returns a match containing
     * information about the capabilities of the device and it's components.
     * 
     * @param headers List of HTTP headers to use for the detection.
     * @param match A match object created by a previous match, or via the 
     * createMatch() method, not null.
     * @return {@link Match} object with detection results.
     * @throws IOException if there was a problem accessing data file.
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
                Map<String, MatchState> matches = 
                        matchForHeaders(match, headers, importantHeaders);
                
                // Set the profile for each component from the headers provided.
                for(Component component : dataSet.components) {
                    // Get the profile for the component.                    
                    Profile profile = 
                            getMatchingHeaderProfile(match.state, matches, component);
                    
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
     * For a given User-Agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent User-Agent string to be identified.
     * @return {@link Match} object with detection results.
     * @throws IOException if there was a problem accessing data file.
     */
    public Match match(String targetUserAgent) throws IOException {
        return match(targetUserAgent, createMatch());
    }

    /**
     * For a given User-Agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent User-Agent string to be identified.
     * @param match A match object created by a previous match, or via the
     * CreateMatch method.
     * @return {@link Match} object with detection results.
     * @throws IOException if there was a problem accessing data file.
     */
    public Match match(String targetUserAgent, Match match) throws IOException {
        match.setResult(match(targetUserAgent, match.state));
        return match;
    }
        
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param deviceIdArray Byte array representation of the device Id, not null.
     * @return {@link Match} object with detection results.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public  Match matchForDeviceId(byte[] deviceIdArray) throws IOException {
        return matchForDeviceId(deviceIdArray, createMatch());
    }
    
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param deviceId String representation of the device Id, not null.
     * @return {@link Match} object with detection results.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Match matchForDeviceId(String deviceId) throws IOException {
        return matchForDeviceId(deviceId, createMatch());
    }
    
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param profileIds List of profile IDs as integers, not null.
     * @return {@link Match} object with detection results.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Match matchForDeviceId(ArrayList<Integer> profileIds) 
                                                            throws IOException {
        return matchForDeviceId(profileIds, createMatch());
    }
    
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param deviceIdArray Byte array representation of the device Id.
     * @param match A match object created by a previous match, or via the 
     * createMatch() method, not null.
     * @return {@link Match} object with detection results.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Match matchForDeviceId(byte[] deviceIdArray, Match match) 
                                                            throws IOException {
        if (deviceIdArray.length == 0) {
            throw new IllegalArgumentException("Byte array containing device Id"
                    + " can not be empty.");
        }
        if (match == null) {
            throw new IllegalArgumentException("Match object can not be null");
        }
        ArrayList<Integer> profileIds = new ArrayList<Integer>();
        for (int i =0; i < deviceIdArray.length; i += 4) {
            // Get the relevant 4 bytes.
            byte[] byteId = Arrays.copyOfRange(deviceIdArray, i, i+4);
            // Convert relevant bytes to integer.
            Integer tempId = new BigInteger(byteId).intValue();
            profileIds.add(tempId);
        }
        return matchForDeviceId(profileIds, match);
    }
    
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param deviceId String representation of the device Id.
     * @param match A match object created by a previous match, or via the 
     * createMatch() method, not null.
     * @return {@link Match} object with detection results.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Match matchForDeviceId(String deviceId, Match match) 
                                                            throws IOException {
        if (deviceId.isEmpty()) {
            throw new IllegalArgumentException("String containing device Id "
                    + "can not be empty.");
        }
        if (match == null) {
            throw new IllegalArgumentException("Match object can not be null.");
        }
        String[] profileIdStrings = deviceId.split("-");
        ArrayList<Integer> profileIds = new ArrayList<Integer>();
        for (String profileIdString : profileIdStrings) {
            profileIds.add(Integer.parseInt(profileIdString));
        }
        return matchForDeviceId(profileIds, match);
    }
    
    /**
     * Returns the result of a match based on the device Id returned from a 
     * previous match operation.
     * 
     * @param profileIds List of profile IDs as integers.
     * @param match A match object created by a previous match, or via the 
     * createMatch() method.
     * @return {@link Match} object with detection results.
     * @throws IOException if there was a problem accessing data file.
     */
    public Match matchForDeviceId(ArrayList<Integer> profileIds, Match match) 
                                                            throws IOException {
        if (profileIds.isEmpty()) {
            throw new IllegalArgumentException("List of profile Ids can not be "
                    + "empty or null.");
        }
        if (match == null) {
            throw new IllegalArgumentException("Match object can not be null.");
        }
        match.reset();
        for (Integer profileId : profileIds) {
            Profile profile = dataSet.findProfile(profileId);
            if (profile != null) {
                match.state.getExplicitProfiles().add(profile);
            }
        }
        return match;
    }

    /**
     * Sets the state to the result of the match for the target User-Agent.
     * 
     * @param targetUserAgent User-Agent string to be identified.
     * @param state current working state of the matching process.
     * @throws IOException if there was a problem accessing data file.
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
     * 
     * @param match The single match instance passed into the match method.
     * @param headers The HTTP headers available for matching.
     * @param importantHeaders HTTP header names important to the match process.
     * @return A map of HTTP headers and match instances containing results 
     * for them.
     * @throws IOException if there was a problem accessing data file.
     */
    private Map<String, MatchState> matchForHeaders(Match match, 
                                                    Map<String, String> headers, 
                                                    ArrayList<String> importantHeaders)
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
            // hence making all matches correspond to the User-Agents.
            match(headers.get(m.getKey()), m.getValue());
        }
        return matches;
    }

    /**
     * For a given User-Agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent The User-Agent string to use as the target
     * @param state information used to process the match
     * @return a match containing information about the capabilities of the 
     * device and it's components
     * @throws IOException if there was a problem accessing data file.
     */
    private MatchResult match(String targetUserAgent, MatchState state) 
            throws IOException {
        MatchResult result;
        if (targetUserAgent == null) {
            targetUserAgent = "";
        }
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
    
    /**
     * See if any of the headers can be used for this components profile. As
     * soon as one matches then stop and don't look at any more. They are 
     * ordered in preferred sequence such that the first item is the most 
     * preferred.
     * 
     * @param masterState current working state of the matching process
     * @param matches map of HTTP header names and match states
     * @param component component to be retrieved
     * @return Profile for the component provided from the matches for each 
     * header
     */
    private static Profile getMatchingHeaderProfile(MatchState state, 
            Map<String, MatchState> matches, Component component) 
                                                            throws IOException {
        
        for (String header : component.getHttpheaders()) {
            MatchState headerMatchState;
            headerMatchState = matches.get(header);
            
            if (headerMatchState != null) {
                state.signaturesCompared += headerMatchState.signaturesCompared;
                state.signaturesRead += headerMatchState.signaturesRead;
                state.stringsRead += headerMatchState.stringsRead;
                state.rootNodesEvaluated += headerMatchState.rootNodesEvaluated;
                state.nodesEvaluated += headerMatchState.nodesEvaluated;
                state.elapsed += headerMatchState.elapsed;
                state.lowestScore += headerMatchState.lowestScore;
                
                // If the header match used is worse than the current one
                // then update the method used for the match returned.
                if (headerMatchState.method.getMatchMethods() > 
                        state.method.getMatchMethods()) {
                    state.method = headerMatchState.method;
                }
                
                if (headerMatchState.getSignature() != null) {
                    for (Profile profile : 
                            headerMatchState.getSignature().getProfiles()) {
                        if (profile.getComponent().equals(component)) {
                            return profile;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Updates the masterState with the header masterState and returns the 
     * profile for the component requested.
     * 
     * @param masterState current working state of the matching process
     * @param headerState state for the specific header
     * @param component the profile returned should relate to
     * @return profile related to the component from the header state
     * @throws IOException if there was a problem accessing data file.
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
}
