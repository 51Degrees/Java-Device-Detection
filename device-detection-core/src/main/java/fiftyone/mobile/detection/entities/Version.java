/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.detection.entities;

/**
 * Class implements version used for reading from the data set.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class Version {

    /**
     * Major version. I.e., 3.x.x.x.
     */
    public final int major;
    /**
     * Minor version. I.e., x.2.x.x.
     */
    public final int minor;
    /**
     * Build number. I.e., x.x.5.x.
     */
    public final int build;
    /**
     * Revision number. I.e., x.x.x.6.
     */
    public final int revision;

    public Version(int major, int minor, int build, int revision) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;
    }

    /**
     * @return version as a string.
     */
    @Override
    public String toString() {
        return String.format("%s.%s.%s.%s",
                major,
                minor,
                build,
                revision);
    }
}
