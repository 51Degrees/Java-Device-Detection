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
package fiftyone.mobile.detection;

import fiftyone.properties.DetectionConstants;
import java.util.regex.Matcher;

/**
 *
 * Class to hold and validate a 51Degrees.mobi Licence key.
 *
 */
public class LicenceKey {

    /**
     * String to hold the licence key.
     */
    private static String key = null;
    
    /**
     * Assigns the given license key to the License key object if it is a valid
     * key.
     *
     * @param licenceKey The license key to add to the collection.
     */
    public static void setKey(final String licenceKey) {
        if (licenceKey != null) {
            if (validate(licenceKey)) {
                key = licenceKey;
            } else {
                /*_logger.info(
                        String.format(
                        "Licence key '%s' was invalid.",
                        licenceKey)); */              
            }
        }
    }

    /**
     * Returns the license key held by this object, or null if no valid key has
     * been set.
     *
     * @return The license key as a string.
     */
    public static String getKey() {
        return key;
    }

    /**
     *
     * Method to validate a license key before it is added to the Object.
     *
     * @param licenceKey the License key to validate.
     * @return true if valid, else false.
     */
    private static boolean validate(final String licenceKey) {
        if (licenceKey != null) {
            final Matcher m = DetectionConstants.LICENSE_KEY_VALIDATION_REGEX.matcher(licenceKey);
            return m.matches();
        }
        return false;
    }
}
