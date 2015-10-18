package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.SignatureV32;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to persist the match results to the cache. Used with the SetState
 * method of the match class to retrieve the state.
 */
public class MatchState {
    /*final*/ protected  MatchMethods method;
    /*final*/ protected  int nodesEvaluated;
    /*final*/ protected  volatile Profile[] profiles;
    /*final*/ protected  int rootNodesEvaluated;
    /*final*/ protected  Signature signature;
    /*final*/ protected  int signaturesCompared;
    /*final*/ protected  int signaturesRead;
    /*final*/ protected  int stringsRead;
    /*final*/ protected  int lowestScore;
    /*final*/ protected  String targetUserAgent;
    /*final*/ protected  byte[] targetUserAgentArray;
    /*final*/ protected  ArrayList<Node> nodes;
    /*final*/ protected  int closestSignaturesCount;

    /**
     * Creates the state based on the one provided.
     * <p>this is an independent copy and isintended to be immutable.</p>
     *
     * @param matchState Match object to update with results.
     */
    protected MatchState(MatchState matchState) throws IOException {
        method = matchState.getMethod();
        nodesEvaluated = matchState.getNodesEvaluated();
        profiles = matchState.getProfiles().clone();
        rootNodesEvaluated = matchState.getRootNodesEvaluated();
        signature = matchState.getSignature();
        signaturesCompared = matchState.getSignaturesCompared();
        signaturesRead = matchState.getSignaturesRead();
        stringsRead = matchState.getStringsRead();
        closestSignaturesCount = matchState.getClosestSignaturesCount();
        lowestScore = matchState.getLowestScore();
        targetUserAgent = matchState.getTargetUserAgent();
        targetUserAgentArray = matchState.getTargetUserAgentArray();
        nodes = new ArrayList<Node>(matchState.getNodes());
    }

    public MatchState() {
        method = MatchMethods.EXACT;
        nodesEvaluated = 0;
        profiles = null;
        rootNodesEvaluated = 0;
        signature = null;
        signaturesCompared = 0;
        signaturesRead = 0;
        stringsRead = 0;
        closestSignaturesCount = 0;
        lowestScore = 0;
        targetUserAgent = "";
        targetUserAgentArray = new byte[0];
        nodes = new ArrayList<Node>();
    }

    /**
     * The method used to obtain the match.
     * @return method used to obtain match
     */
    public MatchMethods getMethod() {
        return method;
    }

    /**
     * The number of signatures read during the detection.
     * @return integer representing number of signatures read during detection
     */
    public int getSignaturesRead() {
        return signaturesRead;
    }

    /**
     * The number of signatures that were compared against the target user agent
     * if the Closest match method was used.
     * @return integer representing number of signatures compared against user
     * agent if closest match method was used
     */
    public int getSignaturesCompared() {
        return signaturesCompared;
    }

    /**
     * The number of root nodes checked against the target user agent.
     * @return integer representing number of root node checked
     */
    public int getRootNodesEvaluated() {
        return rootNodesEvaluated;
    }

    /**
     * The number of nodes checked.
     * @return integer representing the number of nodes checked
     */
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

    /**
     * The number of strings that were read from the data structure for the
     * match.
     * @return integer representing number of strings read for the match
     */
    public int getStringsRead() {
        return stringsRead;
    }

    /**
     * The number of closest signatures returned for evaluation.
     * @return integer representing number of closest signatures returned for
     * evaluation
     */
    public int getClosestSignaturesCount() {
        return closestSignaturesCount;
    }

    /**
     * The user agent string as an ASCII byte array.
     * @return byte array representing user agent string
     */
    public byte[] getTargetUserAgentArray() {
        return targetUserAgentArray;
    }

    /**
     * The target user agent string used for the detection.
     * @return target user agent string used for detection
     */
    public String getTargetUserAgent() {
        return targetUserAgent;
    }

    /**
     * The signature with the closest match to the user agent provided.
     * @return signature with closest match to the user agent provided
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Array of profiles associated with the device that was found.
     * @return array of profiles associated with the device that was found
     * @throws IOException indicates an I/O exception occurred
     */
    public Profile[] getProfiles() throws IOException {
        Profile[] localProfiles = profiles;
        if (localProfiles == null && getSignature() != null) {
            synchronized (this) {
                localProfiles = profiles;
                if (localProfiles == null) {
                    profiles = localProfiles = getSignature().getProfiles();
                }
            }
        }
        return localProfiles;
    }

    public int getLowestScore() {
        return lowestScore;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
}


