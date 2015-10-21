/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection;

public enum AutoUpdateStatus {
    /* Update completed successfully. */
    AUTO_UPDATE_SUCCESS,
     /* HTTPS connection could not be established. */
    AUTO_UPDATE_HTTPS_ERR,
    /* The licence key is invalid. */
    AUTO_UPDATE_INVALID_KEY,
    /* Failed to write to data file. */
    AUTO_UPDATE_WRITE_DATA_FILE_ERROR,
    /* Something else went wrong. */
    AUTO_UPDATE_GENERIC_FAILURE,
    /* The stream does not contain valid data. */
    AUTO_UPDATE_DATA_INVALID,
    /* No need to perform update. */
    AUTO_UPDATE_NOT_NEEDED,
    /* Update currently under way. */
    AUTO_UPDATE_IN_PROGRESS,
    /* Path to master file is directory not file*/
    AUTO_UPDATE_MASTER_FILE_IS_DIR,
    /* 51Degrees server responded with 429: too many attempts. */
    AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS,
    /* 51Degrees server responded with 403 meaning key is blacklisted. */
    AUTO_UPDATE_ERR_403_FORBIDDEN,
    /* Used when IO oerations with input or output stream failed. */
    AUTO_UPDATE_ERR_READING_STREAM,
    /* MD5 validation failed */
    AUTO_UPDATE_ERR_MD5_VALIDATION_FAILED,
    /* When file rename fails. */
    AUTO_UPDATE_ERR_FILE_RENAME_FAILED
}
