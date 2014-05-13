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

/**
 *
 * Enumeration use for defining the amount of detail of the share usage data.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public enum NewDeviceDetails {

    /**
     * Sends only UserAgent and UAProf header fields.
     */
    Minimum,
    /**
     * Sends all headers except cookies.
     */
    Maximum;
}
