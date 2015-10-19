package fiftyone.mobile.detection.cache;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DetectionResult;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 */
@SuppressWarnings("unused")
public class NoOpCache <K> implements Cache <K,DetectionResult> {
    DetectionResult detectionResult;

    public NoOpCache() {
        this.detectionResult = new DetectionResult((Dataset) null) {{
            method = MatchMethods.EXACT;
            nodesEvaluated = 0;
            profiles = new Profile[0];
            rootNodesEvaluated = 0;
            signature = new Signature(null, 0, null) {
                @Override
                public int[] getNodeOffsets() {
                    return new int[0];
                }

                @Override
                protected int getSignatureLength() {
                    return 0;
                }

                @Override
                public int getRank() {
                    return 0;
                }
            };
            signaturesCompared = 0;
            signaturesRead = 0;
            stringsRead = 0;
            closestSignaturesCount = 0;
            lowestScore = 0;
            targetUserAgent = "";
            targetUserAgentArray = new byte[0];
            nodes = new ArrayList<Node>();
        }};
    }

    @Override
    public void resetCache() {

    }

    @Override
    public void clearRequests() {

    }

    @Override
    public void clearMisses() {

    }

    @Override
    public void setLoader(Loader<K, DetectionResult> loader) {

    }

    @Override
    public long getCacheRequests() {
        return 0;
    }

    @Override
    public long getCacheMisses() {
        return 0;
    }

    @Override
    public boolean enableStats(boolean enable) {
        return false;
    }

    @Override
    public DetectionResult get(K key) {
        return detectionResult;
    }

    @Override
    public DetectionResult get(K key, DetectionResult resultInstance) {
        return resultInstance;
    }

    @Override
    public DetectionResult getIfCached(K key) {
        return detectionResult;
    }

    @Override
    public DetectionResult get(K key, Loader<K, DetectionResult> loader) {
        return detectionResult;
    }
}
