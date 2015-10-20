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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.properties.MatchMethods;
import java.io.IOException;

/**
 * Used to persist the results of a match for future use.
 */
public class MatchResult implements IMatchResult {

    /**
     * @return The elapsed time for the match.
     */
    @Override
    public long getElapsed() {
        return elapsed;
    }
    private final long elapsed;

    /**
     * @return The method used to obtain the match. MatchMethods provides 
     * descriptions of the possible return values. When used with multi HTTP 
     * headers the worst method used for all the HTTP headers.
     */    
    @Override
    public MatchMethods getMethod() {
        return method;
    }
    private final MatchMethods method;

    /**
     * @return The number of nodes checked.
     */    
    @Override
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }
    private final int nodesEvaluated;

    /**
     * @return The number of root nodes checked against the target user agent.
     */
    @Override
    public int getRootNodesEvaluated() {
        return rootNodesEvaluated;
    }
    private final int rootNodesEvaluated;

    /**
     * The signature with the closest match to the target User-Agent provided.
     * @return 
     */    
    @Override
    public Signature getSignature() {
        return signature;
    }
    private final Signature signature;

    /**
     * @return The number of signatures that were compared against the target 
     * User-Agent if the Closest match method was used.
     */
    @Override
    public int getSignaturesCompared() {
        return signaturesCompared;
    }
    private final int signaturesCompared;

    /**
     * @return The number of signatures read during the detection.
     */    
    @Override
    public int getSignaturesRead() {
        return signaturesRead;
    }
    private final int signaturesRead;

    /**
     * @return  The number of strings that were read from the data structure 
     * for the match.
     */    
    @Override
    public int getStringsRead() {
        return stringsRead;
    }
    private final int stringsRead;

    /**
     * @return The number of closest signatures returned for evaluation.
     */    
    @Override
    public int getClosestSignaturesCount() {
        return closestSignaturesCount;
    }
    private final int closestSignaturesCount;

    /**
     * @return The lowest score recorded for the signature that was found.
     */    
    @Override
    public int getLowestScore() {
        return lowestScore;
    }
    private final int lowestScore;

    /**
     * @return The target User-Agent string used for the detection where a 
     * single User-Agent was provided. If multiple HTTP headers were provided 
     * then this value will be null.
     */
    @Override
    public String getTargetUserAgent() {
        return targetUserAgent;
    }
    private final String targetUserAgent;

    /**
     * @return The target User-Agent represented as an array of bytes.
     */    
    @Override
    public byte[] getTargetUserAgentArray() {
        return targetUserAgentArray;
    }
    private final byte[] targetUserAgentArray;

    /**
     * @return An array of the nodes associated with the match result. Used for 
     * further analysis of the results and gaining a string representation of 
     * the match.
     */    
    @Override
    public Node[] getNodes() {
        return nodes;
    }
    private final Node[] nodes;

    /**
     * @return Array of profiles associated with the device that was found.
     */    
    @Override
    public Profile[] getProfiles() {
        return profiles;
    }
    private final Profile[] profiles;
    
    /**
     * Creates a copy of the IMatchResult provided.
     * @param source result of a previous match
     * @return 
     */
    MatchResult(IMatchResult source) throws IOException {
        elapsed = source.getElapsed();
        method = source.getMethod();
        nodesEvaluated = source.getNodesEvaluated();
        rootNodesEvaluated = source.getRootNodesEvaluated();
        signature = source.getSignature();
        signaturesCompared = source.getSignaturesCompared();
        signaturesRead = source.getSignaturesRead();
        stringsRead = source.getStringsRead();
        closestSignaturesCount = source.getClosestSignaturesCount();
        lowestScore = source.getLowestScore();
        targetUserAgent = source.getTargetUserAgent();
        targetUserAgentArray = source.getTargetUserAgentArray();
        profiles = new Profile[source.getProfiles().length];
        System.arraycopy(source.getProfiles(), 0, profiles, 0, profiles.length);
        nodes = source.getNodes();
    } 
}
