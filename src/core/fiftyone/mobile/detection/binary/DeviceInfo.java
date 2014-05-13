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
package fiftyone.mobile.detection.binary;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.Provider;

/**
 *
 * Device info for the binary data type where the parent information is held as
 * a direct reference and not as a fallback device id.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class DeviceInfo extends BaseDeviceInfo {

    /**
     *
     * Constructs a new instance of the device information. Device and User
     * Agent are provided uses indexes in the providers strings collection.
     *
     * @param provider The provider the device will be assigned to.
     * @param uniqueDeviceID The unique device name.
     * @param userAgentStringIndex The string index of the user agent string.
     * @param parent The parent device, or null if no parent.
     */
    DeviceInfo(
            final Provider provider, 
            final String uniqueDeviceID, 
            final int userAgentStringIndex, 
            final DeviceInfo parent) {
        super(
                provider, 
                uniqueDeviceID, 
                userAgentStringIndex >= 0 ? 
                    provider.getStrings().get(userAgentStringIndex) 
                    : "", parent
                );
    }

    /**
     *
     * Constructs a new instance of the device information. Device and User
     * Agent are provided uses indexes in the providers strings collection.
     *
     * @param provider The provider the device will be assigned to.
     * @param uniqueDeviceID The unique device name.
     * @param parent The parent device, or null if no parent.
     */
    DeviceInfo(
            final Provider provider, 
            final String uniqueDeviceID, 
            final DeviceInfo parent) {
        super(provider, uniqueDeviceID, "", parent);
    }
}
