package fiftyone.mobile.detection.helper;

import fiftyone.mobile.detection.Match;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * helper for matches
 */
public class MatchHelper {
    public static void matchEquals(Match match1, Match match2) throws IOException {
        assertEquals(match1.getClosestSignaturesCount(), match2.getClosestSignaturesCount());
        assertEquals(match1.getMethod(), match2.getMethod());
        assertEquals(match1.getDeviceId(), match2.getDeviceId());
        assertEquals(match1.getDifference(), match2.getDifference());
    }


}
