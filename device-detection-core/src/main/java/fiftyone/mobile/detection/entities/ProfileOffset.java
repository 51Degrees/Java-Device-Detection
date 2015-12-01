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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Maps a profile id to its position in the data file.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class ProfileOffset extends BaseEntity {

    /**
     * The length in bytes of the profile offset record in the data file.
     */
    public static final int RECORD_LENGTH = (4 * 2);

    /**
     * Constructs a new ProfileOffset that maps profile Id to the corresponding 
     * position in the data file.
     * 
     * @param dataSet DataSet object to provide to the super class.
     * @param offset the location in the data file to read from.
     * @param reader BinaryReader to use to extract data from the data file.
     */
    public ProfileOffset(Dataset dataSet, int offset,
            BinaryReader reader) {
        super(dataSet, offset);
        profileId = reader.readInt32();
        this.offset = reader.readInt32();
    }
    
    /**
     * @return Id of the profile as integer.
     */
    public int getProfileId() {
        return profileId;
    }
    private final int profileId;
    
    /**
     * @return The position within the data file that the profile can be 
     * read from.
     */
    public int getOffset() {
        return offset;
    }
    private final int offset;
}
