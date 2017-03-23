/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * This Source Code Form is ?Incompatible With Secondary Licenses?, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection;

/**
 * Utility methods shared across 51Degrees packages.
 */
public class Utilities {
    
    /**
     * Method joins given number of strings separating each by the specified 
     * separator. Used to construct the update URL.
     * 
     * @param seperator what separates the strings.
     * @param strings strings to join.
     * @return all of the strings combined in to one and separated by separator.
     */
    public static String joinString(final String seperator, 
                                    final String[] strings) {
        final StringBuilder sb = new StringBuilder();
        int size = strings.length;
        for (int i = 0; i < size; i++) {
            sb.append(strings[i]);
            if (i < size - 1) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }
}