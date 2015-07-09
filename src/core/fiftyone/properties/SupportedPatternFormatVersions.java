package fiftyone.properties;

import fiftyone.mobile.detection.entities.Version;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class SupportedPatternFormatVersions {
    
    private final Map<DetectionConstants.FORMAT_VERSIONS, Version> patternVersions;
    
    /**
     * Create the SupportedPatternFormatVersions object.
     */
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
     * @param formatVersion FORMAT_VERSIONS Enum to check for.
     * @return True if such Enum exists amongst the keys.
     */
    public boolean contains(DetectionConstants.FORMAT_VERSIONS formatVersion) {
        return patternVersions.containsKey(formatVersion);
    }
    
    /**
     * Check if he given Version is present in the list.
     * @param version Version object to check for.
     * @return True if such Version object exists.
     */
    public boolean contains(Version version) {
        return patternVersions.containsValue(version);
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
     * @param version Version object to get FORMAT_VERSIONS Enum for.
     * @return FORMAT_VERSIONS Enum corresponding to the provided Version object.
     */
    public DetectionConstants.FORMAT_VERSIONS getEnum(Version version) {
        if (contains(version)) {
            for (Entry entry : patternVersions.entrySet()) {
                Version vT = (Version)entry.getValue();
                if (vT == version)
                    return (DetectionConstants.FORMAT_VERSIONS)entry.getKey();
            }
        }
        return null;
    }
}
