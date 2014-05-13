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
package fiftyone.mobile.detection.matchers.reducedinitialstring;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.Num;
import fiftyone.mobile.detection.handlers.Handler;
import fiftyone.mobile.detection.matchers.Results;
import java.util.List;

/**
 *
 * Class containing the matching algorithm for a Reduced Initial String.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Matcher {

    /**
     *
     * Uses a Reduced Initial String matching routine to determine the results.
     *
     * @param userAgent The User Agent to be matched.
     * @param handler The handler performing the match.
     * @param tolerance The number of characters that need to be the same at the
     * beginning of the string for a match to have occurred.
     * @return All the devices that matched.
     */
    public static Results match(
            final String userAgent, 
            final Handler handler, 
            final int tolerance) {
        BaseDeviceInfo bestMatch = null;
        final Num maxInitialString = new Num();
        for (List<BaseDeviceInfo> devices : handler.getDevices().values()) {
            for (BaseDeviceInfo device : devices) {
                if (check(userAgent, maxInitialString, device)) {
                    bestMatch = device;
                }
            }
        }
        return maxInitialString.get() >= tolerance ? 
                new Results(bestMatch, handler, maxInitialString.get(), userAgent) : null;
    }

    /**
     *
     * Checks to see if the current device or the User Agent String contain each
     * other at the start.
     *
     * @param userAgent The User Agent being searched for.
     * @param maxInitialString The maximum number of characters that have
     * matched in the search so far.
     * @param current The current device being checked for a match.
     * @return true if they do, false else
     */
    private static boolean check(
            final String userAgent, 
            final Num maxInitialString,
            final BaseDeviceInfo current) {
        boolean result = false;
        if ((userAgent.startsWith(current.getUserAgent()))
                || current.getUserAgent().startsWith(userAgent)
                && maxInitialString.get() < current.getUserAgent().length()) {
            maxInitialString.set(current.getUserAgent().length());
            result = true;
        }
        return result;
    }
}
