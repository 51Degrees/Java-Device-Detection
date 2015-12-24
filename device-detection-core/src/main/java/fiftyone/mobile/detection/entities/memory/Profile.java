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
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * All data is loaded into memory when the entity is constructed. Extends 
 * memory version of {@link fiftyone.mobile.detection.entities.Profile}.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see:
 * <a href="https://51degrees.com/support/documentation/pattern">
 * how Pattern device detection works</a>.
 * 
 */
public class Profile extends fiftyone.mobile.detection.entities.Profile {
    /**
     * Constructs a new instance of the Profile.
     * 
     * @param dataSet The data set whose profile list the profile will be 
     * contained within.
     * @param offset The offset position in the data structure to the profile.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public Profile(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        int valueIndexesCount = reader.readInt32();
        int signatureIndexesCount = reader.readInt32();
        this.valueIndexes = 
                BaseEntity.readIntegerArray(reader, valueIndexesCount);
        this.signatureIndexes = 
                BaseEntity.readIntegerArray(reader, signatureIndexesCount);
    }

    /**
     * Get array of value indexes associated with the profile.
     * 
     * @return Array of value indexes associated with the profile.
     */
    @Override
    public int[] getValueIndexes() {
        return valueIndexes;
    }

    /**
     * Get array of signature indexes associated with the profile.
     * 
     * @return Array of signature indexes associated with the profile.
     */
    @Override
    public int[] getSignatureIndexes() {
        return signatureIndexes;
    }
}
