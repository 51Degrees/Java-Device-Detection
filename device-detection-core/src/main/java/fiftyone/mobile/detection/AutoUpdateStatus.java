/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
 *
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
 * The Enumeration contains all possible states that the AutoUpdate process can 
 * potentially be in. Used as the return type for the AutoUpdate.
 * <p>
 * Use the return status code to determine whether any further actions are 
 * necessary. For example: if the return code is AUTO_UPDATE_SUCCESS nothing 
 * else needs to be done as the update completed successfully.
 * If AUTO_UPDATE_NOT_NEEDED was returned and you know the next update date is 
 * today, then retry update in six hours. All other status codes will indicate 
 * a problem with the update. AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS for instance 
 * means that your licence key has been used too many times in the last half 
 * hour interval.
 * <p>
 * For more details please see:
 * <a href="https://51degrees.com/support/documentation/automatic-updates">
 * licence keys and automatic updates</a> general information.
 */
public enum AutoUpdateStatus {
    /**
     * Update completed successfully.
     */
    AUTO_UPDATE_SUCCESS,
    /**
     * HTTPS connection could not be established.
     */
    AUTO_UPDATE_HTTPS_ERR,
    /**
     * No need to perform update.
     */
    AUTO_UPDATE_NOT_NEEDED,
    /**
     * Update currently under way.
     */
    AUTO_UPDATE_IN_PROGRESS,
    /**
     * Path to master file is directory not file.
     */
    AUTO_UPDATE_MASTER_FILE_CANT_RENAME,
    /**
     * 51Degrees server responded with 429: too many attempts.
     */
    AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS,
    /**
     * 51Degrees server responded with 403 meaning key is blacklisted.
     */
    AUTO_UPDATE_ERR_403_FORBIDDEN,
    /**
     * MD5 validation failed.
     */
    AUTO_UPDATE_ERR_MD5_VALIDATION_FAILED,
    /**
     * The new data file can't be renamed to replace the previous one.
     */
    AUTO_UPDATE_NEW_FILE_CANT_RENAME
}
