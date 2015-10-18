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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.cache.Cache;
import fiftyone.mobile.detection.cache.SwitchingCache;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.properties.MatchMethods;

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
     * The data set associated with the provider.
     */
    public final Dataset dataSet;
    /**
     * The number of detections performed using the method.
     */
    private final SortedList<MatchMethods, AtomicLong> methodCounts;
    /**
     * A cache for user agents.
     */
    private Cache<String, DetectionResult> userAgentCache = null;
    private AtomicLong detectionCount;
    private Controller controller;

    /**
     * Constructs a new provided using the data set.
     *
     * @param dataSet Data set to use for device detection
     */
    public Provider(Dataset dataSet) {
        this(dataSet, 0);
    }

    /**
     * Constructs a new provided using the data set and the default cache
     *
     * @param dataSet   Data set to use for device detection
     * @param cacheSize Size in bytes of cache to use
     */
    public Provider(Dataset dataSet, int cacheSize) {
        this(dataSet, new Controller(), cacheSize > 0 ? new SwitchingCache<String, DetectionResult>(cacheSize) : null);
    }

    /**
     * Constructs a new provided using the data set.
     *
     * @param dataSet Data set to use for device detection
     * @param cache A cache to use
     */
    public Provider(Dataset dataSet, Cache<String, DetectionResult> cache) {
        this(dataSet, new Controller(), cache);
    }

    /**
     * Constructs a new provider with the dataset, controller and cache
     * specified.
     *
     * @param dataSet Dataset to use
     * @param controller Controller to use
     * @param cache a user agent cache
     */
    Provider(Dataset dataSet, Controller controller, Cache<String, DetectionResult> cache) {
        this.detectionCount = new AtomicLong();
        this.dataSet = dataSet;
        this.controller = controller;
        // Initialise HashMap with default size and a directive to re-hash only
        // when capacity exceeds initial.
        int numberOfMethods = MatchMethods.values().length;
        this.methodCounts = new SortedList<MatchMethods, AtomicLong>(numberOfMethods, 1);
        this.methodCounts.add(MatchMethods.CLOSEST, new AtomicLong(0));
        this.methodCounts.add(MatchMethods.NEAREST, new AtomicLong(0));
        this.methodCounts.add(MatchMethods.NUMERIC, new AtomicLong(0));
        this.methodCounts.add(MatchMethods.EXACT, new AtomicLong(0));
        this.methodCounts.add(MatchMethods.NONE, new AtomicLong(0));

        userAgentCache = cache;
        if (cache != null) {
            userAgentCache.setLoader(new Cache.Loader<String, DetectionResult>() {
                @Override
                public DetectionResult load(String key) {
                    return load(key, createMatch());
                }

                @Override
                public DetectionResult load(String key, DetectionResult resultInstance) {
                    try {
                        return new DetectionResult(matchNoCache(key, (Match) resultInstance));
                    } catch (IOException e) {
                        return null;
                    }
                }
            });
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
     * information about the capabilities of the device and its components.
     *
     * @param headers List of HTTP headers to use for the detection
     * @return a match for the target headers provided
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(final Map<String, String> headers) throws IOException {
        return match(headers, createMatch());
    }
    /**
     * For a given user agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent string representing the user agent to be identified
     * @return a match result for the target user agent
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(final String targetUserAgent) throws IOException {
        return match(targetUserAgent, createMatch());
    }

    public DetectionResult detect(String targetUserAgent) throws IOException {
        if (userAgentCache != null && targetUserAgent != null) {
            return userAgentCache.get(targetUserAgent);
        }
        return  matchNoCache(targetUserAgent, createMatch());
    }

    /**
     * For a given user agent returns a match containing information about the
     * capabilities of the device and it's components.
     *
     * @param targetUserAgent The user agent string to use as the target
     * @param match           A match object created by a previous match, or via the
     *                        CreateMatch method.
     * @return a match containing information about the capabilities of the
     * device and it's components
     * @throws IOException indicates and I/O exception occurred
     */
    public Match match(final String targetUserAgent, final Match match) throws IOException {

        if (userAgentCache != null && targetUserAgent != null) {
            DetectionResult state = userAgentCache.get(targetUserAgent, match);
            match.setState(state);
        } else {
            // The cache does not exist so call the non caching method.
            matchNoCache(targetUserAgent, match);
        }
        return match;
    }

    private Match matchNoCache(String targetUserAgent, Match match) throws IOException {
        // prepare the match for reuse with this user agent
        // TODO find a way of not initialising a new match
        match.reset(targetUserAgent);
        // perform the match
        controller.match(match);
        // Update the counts for the provider.
        detectionCount.incrementAndGet();
        methodCounts.get(match.getMethod()).incrementAndGet();

        return match;
    }

    /**
     * For a given collection of HTTP headers returns a match containing
     * information about the capabilities of the device and it's components.
     *
     * @param headers List of HTTP headers to use for the detection
     * @param match   object created to store the results of the match
     * @return a match for the target headers provided
     * @throws IOException indicates an I/O exception occurred
     */
    public Match match(final Map<String, String> headers, Match match) throws IOException {

        if (headers == null || headers.isEmpty()) {
            // Empty headers all default match result.
            Controller.matchDefault(match);
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
                Map<String, Match> matches = matchForHeaders(match, headers, importantHeaders);

                // A list of new profiles to use with the match.
                Profile[] newProfiles = new Profile[dataSet.components.size()];
                int componentIndex = 0;

                for (Component component : dataSet.components) {
                    // See if any of the headers can be used for this
                    // components profile. As soon as one matches then
                    // stop and don't look at any more. They are ordered
                    // in preferred sequence such that the first item is
                    // the most preferred.

                    for (String localHeader : component.getHttpheaders()) {
                        Match headerMatch = matches.get(localHeader);
                        if (headerMatch != null) {
                            // Update the statistics about the matching process
                            // if this header isn't the match instance passed
                            // to the method.
                            if (match != headerMatch) {
                                match.signaturesCompared += headerMatch.signaturesCompared;
                                match.signaturesRead += headerMatch.signaturesRead;
                                match.stringsRead += headerMatch.stringsRead;
                                match.rootNodesEvaluated += headerMatch.rootNodesEvaluated;
                                match.nodesEvaluated += headerMatch.nodesEvaluated;
                                match.elapsed += headerMatch.elapsed;
                                match.setLowestScore(match.getLowestScore() + headerMatch.getDifference());
                            }

                            // If the header match used is worst than the
                            // current one then update the method used for the
                            // match returned.
                            if (headerMatch.method.getMatchMethods() > match.method.getMatchMethods()) {
                                match.method = headerMatch.method;
                            }

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
                // to the match result or that can't contain a value when
                // HTTP headers are used.
                match.setSignature(null);
                match.results = null;
                match.setTargetuserAgent(null);

                // Replace the match profiles with the new ones.
                match.profiles = newProfiles;
            }
        }
        return match;
    }

    /**
     * For each of the important HTTP headers provides a mapping to a
     * match result.
     *
     * @param match            The single match instance passed into the match method.
     * @param headers          The HTTP headers available for matching.
     * @param importantHeaders HTTP header names important to the match process.
     * @return A map of HTTP headers and match instances containing results
     * for them.
     * @throws IOException
     */
    private Map<String, Match> matchForHeaders(
            Match match, Map<String, String> headers, ArrayList<String> importantHeaders)
            throws IOException {
        // Relates HTTP header names to match resutls.
        Map<String, Match> matches = new HashMap<String, Match>();
        // Make the first match used the match passed into the method.
        // Subsequent matches will use a new instance.
        Match currentMatch = match;
        // Iterates through the important header names.
        for (String importantHeader : importantHeaders) {
            matches.put(importantHeader, currentMatch != null ? currentMatch : createMatch());
            currentMatch = null;
        }

        // Using each of the match instances pass the value to the match method
        // and set the results.
        for (Entry<String, Match> m : matches.entrySet()) {
            // At this point we have a collection of the String => Match objects
            // where Match objects are empty. Perform the Match for each String
            // hence making all matches correspond to the User Agents.
            match(headers.get(m.getKey()), m.getValue());
        }
        return matches;
    }

    /**
     * The total number of detections performed by the data set.
     *
     * @return total number of detections performed by this data set
     */
    @SuppressWarnings("unused")
    public long getDetectionCount() {
        return detectionCount.longValue();
    }

    public double getPercentageCacheMisses() {
        if (userAgentCache != null && userAgentCache instanceof SwitchingCache) {
            return ((SwitchingCache) userAgentCache).getPercentageMisses();
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of times the user agents cache was switched.
     *
     * @return the number of times the user agents cache was switched.
     */
    public long getCacheSwitches() {
        if (userAgentCache != null && userAgentCache instanceof SwitchingCache) {
            return ((SwitchingCache) userAgentCache).getCacheSwitches();
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public double getCacheRequests() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheRequests();
        } else {
            return -1;
        }
    }

    @SuppressWarnings("unused")
    public long getCacheMisses() {
        if (userAgentCache != null) {
            return userAgentCache.getCacheMisses();
        } else {
            return -1;
        }
    }
}
