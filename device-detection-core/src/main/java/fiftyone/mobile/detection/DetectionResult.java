package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.*;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to persist the match results to the cache. Used with the SetState
 * method of the match class to retrieve the state.
 */
public class DetectionResult {
    /**
     * The data set used for the detection.
     */
    protected Dataset dataSet;
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
    /*final*/ protected  ArrayList<Node> nodes = new ArrayList<Node>();
    /*final*/ protected  int closestSignaturesCount;

    public DetectionResult(Dataset dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * Creates the state based on the one provided.
     * <p>this is an independent copy and isintended to be immutable.</p>
     *
     * @param detectionResult Match object to update with results.
     */
    protected DetectionResult(DetectionResult detectionResult) throws IOException {
        dataSet = detectionResult.dataSet;
        method = detectionResult.getMethod();
        nodesEvaluated = detectionResult.getNodesEvaluated();
        profiles = detectionResult.getProfiles().clone();
        rootNodesEvaluated = detectionResult.getRootNodesEvaluated();
        signature = detectionResult.getSignature();
        signaturesCompared = detectionResult.getSignaturesCompared();
        signaturesRead = detectionResult.getSignaturesRead();
        stringsRead = detectionResult.getStringsRead();
        closestSignaturesCount = detectionResult.getClosestSignaturesCount();
        lowestScore = detectionResult.getLowestScore();
        targetUserAgent = detectionResult.getTargetUserAgent();
        targetUserAgentArray = detectionResult.getTargetUserAgentArray();
        nodes = new ArrayList<Node>(detectionResult.getNodes());
    }

    public Dataset getDataSet() {
        return dataSet;
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

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param property The property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(Property property) throws IOException {
        Values value = null;

        if (property != null) {
            // Get the property value from the profile returned
            // from the match.
            for (Profile profile : getProfiles()) {
                if (profile.getComponent().getComponentId()
                        == property.getComponent().getComponentId()) {
                    value = profile.getValues(property);
                    break;
                }
            }

            // If the value has not been found use the default profile.
            if (value == null) {
                value = property.getComponent().getDefaultProfile().getValues(property);
            }
        }

        return value;
    }

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param propertyName The property name whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(String propertyName) throws IOException {
        return getValues(dataSet.get(propertyName));
    }
}


