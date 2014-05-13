/* *********************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.matchers.finalmatcher;

import fiftyone.mobile.detection.matchers.Algorithms;
import fiftyone.mobile.detection.matchers.Result;
import fiftyone.mobile.detection.matchers.Results;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * Class containing the final matcher algorithm to calculate which device out of
 * those found is the best match to a User Agent.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Matcher {

    /**
     *
     * Examines each string in the list to find the one that has the highest
     * number of initial matching characters. If only one is found this is
     * returned. If more than one have the same number of matches are found
     * further analysis is performed on the non matching parts or string tails.
     *
     * @param userAgent User Agent to be found.
     * @param results List of possible devices to match against.
     * @return The closest matching result.
     */
    public static Result matcher(final String userAgent, final Results results) {
        int pos = 0;
        int highestPosition = 0;
        final List<Result> subset = new ArrayList<Result>();
        for (Result result : results) {
            // Find the shortest length and compare characters
            // upto this point.
            final int length = result.getDevice().getUserAgent().length() > userAgent.length()
                    ? userAgent.length()
                    : result.getDevice().getUserAgent().length();
            // For each character check equality. If the characters
            // aren't equal record this position.
            for (pos = 0; pos < length; pos++) {
                if (userAgent.charAt(pos) != result.getDevice().getUserAgent().charAt(pos)) {
                    break;
                }
            }
            // If this position is greater than the highest position so
            // far than empty the list of subsets found and record this
            // result in addition to the new highest position.
            if (pos > highestPosition) {
                highestPosition = pos;
                subset.clear();
                subset.add(result);
            } 
            // If the position is the same as the best one found so far
            // then add it to the results.
            else if (pos == highestPosition) {
                subset.add(result);
            }
        }
        // If only one is found return it.
        if (subset.size() == 1) {
            return subset.get(0);
        }
        
        // If there is an exact match return it.
        if(highestPosition == userAgent.length()){
            for(Result r : subset){
                if(r.getDevice().getUserAgent().equals(userAgent)){
                    return r;
                }
            }
        }
        
        // If there are more than 1 find the best one based on the end
        // of the useragent strings.
        if (subset.size() > 1) {
            return matchTails(userAgent, highestPosition, subset);
        }
        return null;
    }

    /**
     *
     * Compare the tails of the devices using the Edit Distance algorithm.
     *
     * @param userAgent User Agent to be found.
     * @param pos position in String that denotes when all remaining devices
     * differ from the User Agent.
     * @param results List of possible results to match against.
     * @return The closest matching result.
     */
    private static Result matchTails(
            final String userAgent, 
            final int pos, 
            final List<Result> results) {
        int longestSubset = 0;
        final Queue<String> tails = new LinkedList<String>();

        // Get the tails of all the strings and add them to the queue.
        for (Result result : results) {
            final String tail = result.getDevice().getUserAgent().substring(pos);
            tails.add(tail);
            if (tail.length() > longestSubset) {
                longestSubset = tail.length();
            }
        }
        // Get the longest part of the tail needed.
        final String userAgentTail = userAgent.substring(pos,
                longestSubset + pos < userAgent.length()
                ? longestSubset + pos
                : userAgent.length());
        // Find the tail with the closest edit distance match.
        String closestTail = null;
        String current;
        int minDistance = fiftyone.mobile.detection.Constants.MAX_INT;
        //construct aarray needed for edit distance 
        final int[][] rows = new int[userAgentTail.length() + 1][userAgentTail.length() + 1];
        while (tails.size() > 0) {
            current = tails.poll();
            final int currentDistance = Algorithms.EditDistance(rows, userAgentTail, current, minDistance);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                closestTail = current;
            }
        }

        // Find the 1st matching useragent and return.
        Result result = null;

        for (Result res : results) {
            if (res.getDevice().getUserAgent().endsWith(closestTail)) {
                result = res;
                break;
            }
        }
        if (result != null) {
            return result;
        }

        //If nothing has returned yet, then results are very similar 
        //so just return the first
        return results.get(0);
    }
}
