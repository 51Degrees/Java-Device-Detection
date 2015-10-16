package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to persist the match results to the cache. Used with the SetState
 * method of the match class to retrieve the state.
 */
class MatchState {
    final MatchMethods method;
    final int nodesEvaluated;
    final Profile[] profiles;
    final int rootNodesEvaluated;
    final Signature signature;
    final int signaturesCompared;
    final int signaturesRead;
    final int stringsRead;
    final int lowestScore;
    final String targetUserAgent;
    final byte[] targetUserAgentArray;
    final ArrayList<Node> nodes;
    final int closestSignaturesCount;

    /**
     * Creates the state based on the match provided.
     *
     * @param match Match object to update with results.
     */
    MatchState(Match match) throws IOException {
        method = match.getMethod();
        nodesEvaluated = match.getNodesEvaluated();
        profiles = match.getProfiles().clone();
        rootNodesEvaluated = match.getRootNodesEvaluated();
        signature = match.getSignature();
        signaturesCompared = match.getSignaturesCompared();
        signaturesRead = match.getSignaturesRead();
        stringsRead = match.getStringsRead();
        closestSignaturesCount = match.getClosestSignaturesCount();
        lowestScore = match.getLowestScore();
        targetUserAgent = match.getTargetUserAgent();
        targetUserAgentArray = match.getTargetUserAgentArray();
        nodes = new ArrayList<Node>(match.getNodes());
    }
}


