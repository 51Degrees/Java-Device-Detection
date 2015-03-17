package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * Maps a profile id to its position in the data file.
 */
public class ProfileOffset extends BaseEntity {

    /**
     * The length in bytes of the profile offset record in the data file.
     */
    public static final int RECORD_LENGTH = (4 * 2);
    
    private int profileId;
    
    private int offset;
    
    public int getProfileId() {
        return profileId;
    }
    
    /**
     * The position within the data file that the profile can be read from.
     *
     * @return offset of the profile Id in the profiles data structure
     */
    public int getOffset() {
        return offset;
    }
   
    public ProfileOffset(Dataset dataSet, int offset,
            BinaryReader reader) {
        super(dataSet, offset);
        profileId = reader.readInt32();
        this.offset = reader.readInt32();
    }
}
