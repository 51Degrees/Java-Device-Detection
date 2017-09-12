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
package fiftyone.properties;

import fiftyone.mobile.detection.entities.Version;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides logic for storing the Enumeration values mapped to the Version 
 * objects.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class SupportedPatternFormatVersions {
    
    private final Map<DetectionConstants.FORMAT_VERSIONS, Version> patternVersions;
    
    /**
     * Create the SupportedPatternFormatVersions object.
     */
    @SuppressWarnings("MapReplaceableByEnumMap")
    public SupportedPatternFormatVersions() {
        patternVersions = new HashMap<DetectionConstants.FORMAT_VERSIONS, Version>();
        init();
    }
    
    /**
     * Add the necessary Enum : Version pairs.
     */
    private void init() {
        patternVersions.put(
                            DetectionConstants.FORMAT_VERSIONS.PatternV31, 
                            new Version(3,1,0,0));
        patternVersions.put(
                            DetectionConstants.FORMAT_VERSIONS.PatternV32, 
                            new Version(3,2,0,0));
    }
    
    /**
     * Check if the given format version Enum is present in the list.
     * 
     * @param formatVersion FORMAT_VERSIONS Enum to check for.
     * @return True if such Enum exists amongst the keys.
     */
    public boolean contains(DetectionConstants.FORMAT_VERSIONS formatVersion) {
        return patternVersions.containsKey(formatVersion);
    }
    
    /**
     * Check if he given Version is present in the list.
     * 
     * @param version Version object to check for.
     * @return True if such Version object exists.
     */
    public boolean contains(Version version) {
        for (Entry entry : patternVersions.entrySet()) {
            Version v = (Version)entry.getValue();
            if (isSameVersion(v, version))
                return true;
        }
        return false;
    }
    
    /**
     * Compare the two version objects. In order for the two versions to be the 
     * same they must have the same value for each part of the version.
     * 
     * @param one first Version object to compare to second.
     * @param two second Version object to compare to first.
     * @return True if all version parts match.
     */
    private boolean isSameVersion(Version one, Version two) {
        return (one.build == two.build && one.major == two.major 
                && one.minor == two.minor && one.revision == two.revision);
    }
    
    /**
     * Get the Version associated with the provided Enum.
     * @param formatVersion FORMAT_VERSIONS Enum to get Version for.
     * @return Version object corresponding to the provided FORMAT_VERSIONS.
     */
    public Version getVersion(DetectionConstants.FORMAT_VERSIONS formatVersion) {
        if (contains(formatVersion))
            return patternVersions.get(formatVersion);
        return null;
    }
    
    /**
     * Get the FORMAT_VERSIONS Enum associated with the provided Version object.
     * 
     * @param version Version object to get FORMAT_VERSIONS Enum for.
     * @return FORMAT_VERSIONS Enum corresponding to the provided Version object.
     */
    public DetectionConstants.FORMAT_VERSIONS getEnum(Version version) {
        if (contains(version)) {
            for (Entry entry : patternVersions.entrySet()) {
                Version vT = (Version)entry.getValue();
                if (isSameVersion(vT, version))
                    return (DetectionConstants.FORMAT_VERSIONS)entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Converts the list to string.
     * 
     * @return List as string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : patternVersions.entrySet()) {
            sb.append(entry.getKey().toString());
            sb.append(": ");
            sb.append(entry.getValue().toString());
            sb.append(" ");
        }
        return sb.toString();
    }
}
