/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

import fiftyone.mobile.detection.cache.IValueLoader;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Used to persist the match results to the cache. Used with the SetState
 * method of the match class to retrieve the state.
 */
public class MatchState extends MatchResult 
    implements IValueLoader<String, MatchResult> {

    /**
     * Sets the elapsed time for the match.
     */    
    void setElapsed(long value) {
        super.elapsed = value;
    }

    /**
     * Sets the method used to obtain the match. MatchMethods provides 
     * descriptions of the possible return values. When used with multi HTTP 
     * headers the worst method used for all the HTTP headers.
     */    
    void setMethod(MatchMethods value) {
        super.method = value;
    }
   
    /**
     * Increments the nodes evaluated by one.
     */
    public void incrNodesEvaluated() {
        super.nodesEvaluated++;
    }    

    /**
     * Increments the number of root nodes checked against the target 
     * User-Agent by one.
     */        
    void incrRootNodesEvaluated() {
        super.rootNodesEvaluated++;
    }        

    /**
     * Sets the signature with the closest match to the target User-Agent 
     * provided.
     */
    void setSignature(Signature value) {
        super.signature = value;
    }

    /**
     * Increments the number of signatures that were compared against the target 
     * User-Agent if the Closest match method was used.
     */    
    void incrSignaturesCompared(int value) {
        super.signaturesCompared += value;
    }
    
    /**
     * Increments the number of signatures that were compared against the target 
     * User-Agent if the Closest match method was used by one.
     */        
    void incrSignaturesCompared() {
        super.signaturesCompared++;
    }

    /**
     * Increments the number of signatures read during the detection.
     */    
    void incrSignaturesRead(int value) {
        super.signaturesRead += value;
    }
    
    /**
     * Increments the number of signatures read during the detection by one.
     */       
    void incrSignaturesRead() {
        super.signaturesRead++;
    }    

    /**
     * Increments the strings read by one.
     */
    public void incrStringsRead() {
        super.stringsRead++;
    }

    /**
     * Increments the number of closest signatures returned for evaluation.
     */    
    void setClosestSignaturesCount(int value) {
        super.closestSignaturesCount = value;
    }

    /**
     * Increments the lowest score recorded for the signature that was found.
     * 
     * @param value to increment by.
     */    
    public void incrLowestScore(int value) {
        super.lowestScore += value;
    }
    
    /**
     * Sets the lowest score to the value provided.
     * @param value of new lowest score
     */
    public void setLowestScore(int value) {
        super.lowestScore = value;
    }

    /**
     * Sets the target User-Agent string used for the detection where a 
     * single User-Agent was provided. If multiple HTTP headers were provided 
     * then this value will be null.
     */    
    void setTargetUserAgent(String value) {
        super.targetUserAgent = value;
    }
   
    /**
     * @return An array of the nodes associated with the match result. Used for 
     * further analysis of the results and gaining a string representation of 
     * the match.
     */    
    @Override
    Node[] getNodes() {
        Node[] result = new Node[nodesList.size()];
        nodesList.toArray(result);
        return result;
    }
    public ArrayList<Node> getNodesList() {
        return nodesList;
    }
    final ArrayList<Node> nodesList = new ArrayList<Node>();

    /**
     * During the process of matching the profiles may vary, for example when
     * multiple HTTP headers are used. The property handle will default to 
     * the profiles associated with a Signature if available, or provides an
     * empty list for profiles to be added to.
     * @return profiles associated with the match state.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public ArrayList<Profile> getExplicitProfiles() throws IOException {
        ArrayList<Profile> result = explicitProfiles;
        if (result == null) {
            synchronized(this) {
                result = explicitProfiles;
                if (result == null) {
                    if (getSignature() != null) {
                        result = new ArrayList<Profile>();
                        result.addAll(Arrays.asList(
                                getSignature().getProfiles()));
                    }
                    else {
                        result = new ArrayList<Profile>();
                    }
                    explicitProfiles = result;
                }
            }
        }
        return result;
    }
    volatile private ArrayList<Profile> explicitProfiles;

    /**
     * @return Array of profiles associated with the device that was found.
     * @throws IOException if there was a problem accessing data file.
     */    
    @Override
    public Profile[] getProfiles() throws IOException {
        Profile[] result = new Profile[getExplicitProfiles().size()];
        getExplicitProfiles().toArray(result);
        return result;
    }
    
    /**
     * @return The target User-Agent represented as an array of bytes.
     */    
    public byte[] getTargetUserAgentArray() {
        return targetUserAgentArray;
    }
    private byte[] targetUserAgentArray;
    
    int nextCharacterPositionIndex;

    final ArrayList<Signature> signatures = new ArrayList<Signature>();

    final Match match;

    Dataset getDataSet() {
        return match.getDataSet();
    }
    
    /**
     * Constructs a new instance of MatchState.
     * @param match instance the state relates to
     */
    MatchState(Match match) {
        super(match.getDataSet());
        this.match = match;
        super.method = MatchMethods.NONE;
    }

    /**
     * Constructs a new instance of MatchState initialised with the target
     * User-Agent provided.
     * @param match instance the state relates to
     * @param targetUserAgent
     * @throws UnsupportedEncodingException 
     */
    MatchState(Match match, String targetUserAgent)
            throws UnsupportedEncodingException {
        this(match);
        init(targetUserAgent);
    }
    
    /**
     * Resets the fields to default values. Used to avoid having to reallocate 
     * memory for data structures when a lot of detections are being performed.
     */    
    void reset() {
        method = MatchMethods.NONE;
        nodesEvaluated = 0;
        rootNodesEvaluated = 0;
        signaturesCompared = 0;
        signaturesRead = 0;
        stringsRead = 0;
        signature = null;
        signatures.clear();
        nodesList.clear();
        explicitProfiles = null;
    }
    
    /**
     * Resets the match for the User-Agent returning all the fields to the
     * values they would have when the match was first constructed. Used to
     * avoid having to reallocate memory for data structures when a lot of
     * detections are being performed.
     *
     * @param targetUserAgent
     * @throws UnsupportedEncodingException
     */    
    void reset(String targetUserAgent) throws UnsupportedEncodingException {
        reset();
        init(targetUserAgent);
    }
    
    /**
     * Initialises the match object ready for detection.
     *
     * @param targetUserAgent
     * @throws UnsupportedEncodingException
     */
    final void init(String targetUserAgent) throws UnsupportedEncodingException {
        if (targetUserAgent != null && targetUserAgent.length() > 0) {
            this.targetUserAgentArray = targetUserAgent.getBytes("US-ASCII");
        } else {
            this.targetUserAgentArray = new byte[0];
        }
        
        // Null check to ensure no down stream problems.
        this.targetUserAgent = targetUserAgent == null ? "" : targetUserAgent;

        resetNextCharacterPositionIndex();
    }
    
    /**
     * Reset the next character position index based on the length of the target
     * User-Agent and the root nodes.
     */
    void resetNextCharacterPositionIndex() {
        // Start checking on the far right of the User-Agent.
        nextCharacterPositionIndex = Math.min(
                targetUserAgentArray.length - 1,
                getDataSet().rootNodes.size() - 1);
    }
    
    /**
     * Inserts the node into the list checking to find it's correct position in
     * the list first.
     *
     * @param node The node to be added to the match list
     * @return The index of the node inserted into the list
     */
    int insertNode(Node node) {
        int index = ~Collections.binarySearch(nodesList, node);
        nodesList.add(index, node);
        return index;
    }
    
    /**
     * Returns the start character position of the node within the target user
     * agent, or -1 if the node does not exist.
     *
     * @param node
     * @return
     * @throws IOException
     */
    int getIndexOf(Node node) throws IOException {
        byte[] characters = node.getCharacters();
        int finalIndex = characters.length - 1;
        for (int index = 0; index < getTargetUserAgentArray().length - characters.length; index++) {
            for (int nodeIndex = 0, targetIndex = index;
                    nodeIndex < characters.length && targetIndex < getTargetUserAgentArray().length;
                    nodeIndex++, targetIndex++) {
                if (characters[nodeIndex] != getTargetUserAgentArray()[targetIndex]) {
                    break;
                } else if (nodeIndex == finalIndex) {
                    return index;
                }
            }
        }
        return -1;
    }    
    
    /**
     * Merges the other match state with this instance. Used when processing
     * multiple HTTP headers to produce a state that includes data from all 
     * HTTP headers.
     * @param other state to be merged with this instance
     */
    void merge(MatchState other) {

        signaturesCompared += other.getSignaturesCompared();
        signaturesRead += other.getSignaturesRead();
        stringsRead += other.getStringsRead();
        rootNodesEvaluated += other.getRootNodesEvaluated();
        nodesEvaluated += other.getNodesEvaluated();
        elapsed += other.getElapsed();
        lowestScore += other.getLowestScore();

        // If the header match used is worst than the current one
        // then update the method used for the match returned.
        if (other.getMethod().getMatchMethods() > 
            method.getMatchMethods()) {
            method = other.getMethod();
        }        
    }

    @Override
    public MatchResult load(String key) throws IOException {
        match.provider.matchNoCache(key, this);
        return new MatchResult(this);
    }
}