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
 * Interface used to enable both the MatchState and MatchResult classes to be 
 * used to provide the result.
 */
public interface IMatchResult {
    /**
     * @return The elapsed time for the match.
     */
    long getElapsed();
    
    /**
     * @return The method used to obtain the match. MatchMethods provides 
     * descriptions of the possible return values. When used with multi HTTP 
     * headers the worst method used for all the HTTP headers.
     */
    MatchMethods getMethod();
    
    /**
     * @return The number of nodes checked.
     */
    int getNodesEvaluated();
    
    /**
     * @return The number of root nodes checked against the target user agent.
     */
    int getRootNodesEvaluated();
    
    /**
     * @return The signature with the closest match to the target User-Agent 
     * provided.
     */
    Signature getSignature();
    
    /**
     * @return The number of signatures that were compared against the target 
     * User-Agent if the Closest match method was used.
     */
    int getSignaturesCompared();
    
    /**
     * @return The number of signatures read during the detection.
     */
    int getSignaturesRead();
    
    /**
     * @return  The number of strings that were read from the data structure 
     * for the match.
     */
    int getStringsRead();
    
    /**
     * @return The number of closest signatures returned for evaluation.
     */
    int getClosestSignaturesCount();
    
    /**
     * @return The lowest score recorded for the signature that was found.
     */
    int getLowestScore();
    
    /**
     * @return The target User-Agent string used for the detection where a 
     * single User-Agent was provided. If multiple HTTP headers were provided 
     * then this value will be null.
     */
    String getTargetUserAgent();
    
    /**
     * @return The target User-Agent represented as an array of bytes.
     */
    byte[] getTargetUserAgentArray();
    
    /**
     * @return An array of the nodes associated with the match result. Used for 
     * further analysis of the results and gaining a string representation of 
     * the match.
     */
    Node[] getNodes();
    
    /**
     * @return Array of profiles associated with the device that was found.
     * @throws IOException 
     */
    Profile[] getProfiles() throws IOException;
}
