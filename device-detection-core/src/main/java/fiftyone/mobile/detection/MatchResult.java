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

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.properties.MatchMethods;
import java.io.IOException;

/**
 * Used to persist the results of a match for future use.
 */
public class MatchResult {

    /**
     * @return The elapsed time for the match.
     */
    public long getElapsed() {
        return elapsed;
    }
    protected long elapsed;

    /**
     * @return The method used to obtain the match. MatchMethods provides 
     * descriptions of the possible return values. When used with multi HTTP 
     * headers the worst method used for all the HTTP headers.
     */    
    public MatchMethods getMethod() {
        return method;
    }
    protected MatchMethods method;

    /**
     * @return The number of nodes checked.
     */    
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }
    protected int nodesEvaluated;

    /**
     * @return The number of root nodes checked against the target User-Agent.
     */
    public int getRootNodesEvaluated() {
        return rootNodesEvaluated;
    }
    protected int rootNodesEvaluated;

    /**
     * The signature with the closest match to the target User-Agent provided.
     * @return {@link Signature} for the current match.
     */    
    public Signature getSignature() {
        return signature;
    }
    protected Signature signature;

    /**
     * @return The number of signatures that were compared against the target 
     * User-Agent if the Closest match method was used.
     */
    public int getSignaturesCompared() {
        return signaturesCompared;
    }
    protected int signaturesCompared;

    /**
     * @return The number of signatures read during the detection.
     */    
    public int getSignaturesRead() {
        return signaturesRead;
    }
    protected int signaturesRead;

    /**
     * @return  The number of strings that were read from the data structure 
     * for the match.
     */    
    public int getStringsRead() {
        return stringsRead;
    }
    protected int stringsRead;

    /**
     * @return The number of closest signatures returned for evaluation.
     */    
    public int getClosestSignaturesCount() {
        return closestSignaturesCount;
    }
    protected int closestSignaturesCount;

    /**
     * @return The lowest score recorded for the signature that was found.
     */    
    public int getLowestScore() {
        return lowestScore;
    }
    protected int lowestScore;

    /**
     * @return The target User-Agent string used for the detection where a 
     * single User-Agent was provided. If multiple HTTP headers were provided 
     * then this value will be null.
     */
    public String getTargetUserAgent() {
        return targetUserAgent;
    }
    protected String targetUserAgent;

    /**
     * @return An array of the nodes associated with the match result. Used for 
     * further analysis of the results and gaining a string representation of 
     * the match.
     */    
    Node[] getNodes() {
        return nodes;
    }
    protected Node[] nodes;

    /**
     * @return Array of profiles associated with the device that was found.
     * @throws java.io.IOException if there was a problem accessing data file.
     */    
    public Profile[] getProfiles() throws IOException {
        return profiles;
    }
    protected Profile[] profiles;

    /**
     * Creates a default instance of MatchState.
     */
    protected MatchResult() {}
    
    /**
     * Creates a copy of the MatchState provided.
     * @param source result of a previous match
     */
    MatchResult(MatchState source) throws IOException {
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
        profiles = source.getProfiles().clone();
        nodes = source.getNodes();
    } 
}
