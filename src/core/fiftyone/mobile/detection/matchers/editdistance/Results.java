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
package fiftyone.mobile.detection.matchers.editdistance;

/**
 *
 * Class to store results from Edit Distance handlers.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Results extends fiftyone.mobile.detection.matchers.Results {

    /**
     * Stores the current minimum distance value. Initially set to max int value
     * so that the first attempt will not fail.
     */
    int MinDistance = fiftyone.mobile.detection.Constants.MAX_INT;
}
