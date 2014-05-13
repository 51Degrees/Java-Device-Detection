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
package fiftyone.mobile.detection.matchers;

/**
 *
 * Contains major matching algorithms used by the solution.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Algorithms {

    /**
     *
     * Measures the amount of difference between two strings using the
     * Levenshtein Distance algorithm. This implementation uses a modified
     * version of the pseudo code found at <a
     * href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia</a>.
     * The logic has been modified to ignore string comparisons that will return
     * a value greater than the lowest one found so far. This significantly
     * improves performance as we can determine earlier if there is any point
     * completing the calculation.
     *
     * @param rows The array used in calculating the distance.
     * @param str1 1st string to compare.
     * @param str2 2nd string to compare.
     * @param maxValue The maximum value we're interested in. Anything higher
     * can be ignored.
     * @return The distance value.
     */
    public static int EditDistance(
            final int[][] rows, 
            final String str1, 
            final String str2, 
            final int maxValue) {
        int l1, l2,
                curRow, nextRow,
                value, lowest;


        // Get string lengths and check for zero length.
        l1 = str1.length();
        l2 = str2.length();

        if (l1 == 0) {
            return l2;
        }
        if (l2 == 0) {
            return l1;
        }

        // Initialise the data structures.
        curRow = 0;
        nextRow = 1;

        for (int x = 0; x <= l1; ++x) {
            rows[curRow][x] = x;
        }

        for (int y = 1; y <= l2; ++y) {
            lowest = fiftyone.mobile.detection.Constants.MAX_INT;
            rows[nextRow][0] = y;
            for (int x = 1; x <= l1; ++x) {
                // Calculate the edit distant value for the current cell.
                value = Math.min(rows[curRow][x] + 1, (Math.min(rows[nextRow][x - 1] + 1,
                        rows[curRow][x - 1] + ((str1.charAt(x - 1) == str2.charAt(y - 1)) ? 0 : 1))));
                rows[nextRow][x] = value;

                // Record the lowest value on this row.
                if (value < lowest) {
                    lowest = value;
                }
            }

            // If the lowest value found so far is greater than the maximum value
            // we're interested in return a large number that will be ignored.
            if (lowest > maxValue) {
                return fiftyone.mobile.detection.Constants.MAX_INT;
            }
            // Swap the current and next rows

            if (curRow == 0) {
                curRow = 1;
                nextRow = 0;
            } else {
                curRow = 0;
                nextRow = 1;
            }
        }

        return rows[curRow][l1];
    }
}
