/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.properties;

import fiftyone.mobile.detection.entities.Version;
import fiftyone.properties.DetectionConstants.FORMAT_VERSIONS;
import java.util.HashMap;

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
}
