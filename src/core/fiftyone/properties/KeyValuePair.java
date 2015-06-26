/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.properties;

import fiftyone.mobile.detection.entities.Version;
import fiftyone.properties.DetectionConstants.FORMAT_VERSIONS;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class implements mapping of a Format Version to the Version object.
 * This is equivalent of:
 * public static readonly KeyValuePair<FormatVersions, Version>[] 
 * SupportedPatternFormatVersions = 
 * new KeyValuePair<FormatVersions, Version>[] {
 * new KeyValuePair<FormatVersions, Version>
 *      (FormatVersions.PatternV31, new Version(3, 1, 0, 0)),
 * new KeyValuePair<FormatVersions, Version>
 *      (FormatVersions.PatternV32, new Version(3, 2, 0, 0))
 * }
 * in C#.
 */
public class KeyValuePair{

    /**
     * Contains the key value pairs.
     */
    private final HashMap<FORMAT_VERSIONS, Version> container;
    /**
     * Indicates if it's Trie or Pattern.
     */
    private final String matchTechnology;
    
    /**
     * Constructs a new Key Value pair where Format Version is mapped to the 
     * Version object.
     * @param matchTechnology 
     */
    public KeyValuePair(String matchTechnology) {
        this.container = new HashMap<FORMAT_VERSIONS, Version>();
        if(matchTechnology.toLowerCase().equals("pattern")) {
            this.matchTechnology = matchTechnology;
            container.put(FORMAT_VERSIONS.PatternV31, new Version(3,1,0,0));
            container.put(FORMAT_VERSIONS.PatternV32, new Version(3,2,0,0));
        } else if (matchTechnology.toLowerCase().equals("trie")) {
            this.matchTechnology = matchTechnology;
            container.put(FORMAT_VERSIONS.TrieV30, new Version(3,0,0,0));
        } else {
            throw new Error("No such match method: "+matchTechnology+"!");
        }
    }
    
    /**
     * Returns true if this KeyValue pair is for Pattern, false otherwise.
     * @return true if this KeyValue pair is for Pattern, false otherwise.
     */
    public boolean isPattern() {
        if (matchTechnology.equals("pattern"))
            return true;
        return false;
    }
    
    /**
     * Returns the number of elements in map.
     * @return the number of elements in map.
     */
    public int size() {
        if (container.isEmpty())
            return 0;
        else
            return container.size();
    }
    
    /**
     * Returns all versions as a string.
     * @return all versions as a string.
     */
    @Override
    public String toString() {
        Set<FORMAT_VERSIONS> formatVersions = container.keySet();
        StringBuilder sb = new StringBuilder();
        for (FORMAT_VERSIONS fv : formatVersions) {
            sb.append(container.get(fv));
            sb.append(" ");
        }
        return sb.toString();
    }
    
    /**
     * Check if a specific version is present in the list of values.
     * @param version Version object to check for.
     * @return True if provided Version is contained in the list of values, 
     * false otherwise.
     */
    public boolean contains(Version version) {
        return container.containsValue(version);
    }
    
    /**
     * Method iterates through the entry set of the HashMap searching for a 
     * specific version. If such version was found it returns the corresponding 
     * key. Null returned if this version is not in the corresponding map.
     * @param version
     * @return 
     */
    public FORMAT_VERSIONS getEnumIfExists(Version version) {
        if (container.containsValue(version)) {
            //We know the version is in the map. Find it.
            for (Entry<FORMAT_VERSIONS, Version> entry : container.entrySet()) {
                if (entry.getValue() == version)
                    return entry.getKey();
            }
        }
        return null;
    }
}
