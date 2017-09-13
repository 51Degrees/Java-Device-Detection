/*
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
 */

package fiftyone.device.example;

import java.io.File;

/**
 * Contains various constants used in the examples and convenience methods for 
 * accessing example data files which accommodate the styles of various IDEs in 
 * starting programs with the module as the working directory or the root of the 
 * project as working directory.
 */
public class Shared {
    /**
     * A small collection of actual HTTP User-Agent header values for testing
     */
    public static final String[] USERAGENTS = new String[]{
        // Internet explorer
        "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
        // A set top box running Android, not a mobile device.
        "Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; Rikomagic MK802IIIS Build/JRO03H) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // Galaxy Note from Samsung.
        "Mozilla/5.0 (Linux; U; Android 4.1.2; de-de; GT-N8020 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // A possible future version of the Galaxy Note to show how numeric fallback handling works.
        "Mozilla/5.0 (Linux; U; Android 4.1.2; de-de; GT-N8420 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // A Chrome HTTP User-Agent.
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36"
    };

    public static String GOOD_USER_AGENTS_FILE = "20000 User Agents.csv";

    public static String LITE_PATTERN_V31 = "51Degrees-LiteV3.1.dat";

    public static String LITE_PATTERN_V32 = "51Degrees-LiteV3.2.dat";

    public static String LITE_TRIE_V30 = "51Degrees-LiteV3.0.trie";

    public static String LITE_TRIE_V32 = "51Degrees-LiteV3.2.trie";

    /** Assumes working directory is of this module. May be reset by static initialiser if working directory is project root */
    public static String BASE_DIRECTORY  = "../data/";
    // static initialiser looks to see whether the working directory is in the module or the root of the project
    static {
        // assume working directory is module
        if (!new File(getLitePatternV31()).exists()) {
            // assume working directory is root fo project
            BASE_DIRECTORY = "data/";
            if (!new File(getLitePatternV31()).exists()) {
                throw new RuntimeException("Cannot find example data files, please set the working directory to a module root or to the project root");
            }
        }
    }

    /**
     * get the name of the sample HTTP User-Agent file containing 20,000 HTTP User-Agent header values
     * @return the name relative to the working directory
     */
    public static String getGoodUserAgentsFile() {
        return BASE_DIRECTORY + GOOD_USER_AGENTS_FILE;
    }

    /**
     * get the name of the V3.1 Pattern Detection file
     * @return the name relative to the working directory
     */
    public static String getLitePatternV31() {
        return BASE_DIRECTORY + LITE_PATTERN_V31;
    }

    /**
     * get the name of the V3.2 Pattern Detection file
     * @return the name relative to the working directory
     */
    public static String getLitePatternV32() {
        return BASE_DIRECTORY + LITE_PATTERN_V32;
    }

    /**
     * get the name of the V3.0 Trie Detection file
     * @return the name relative to the working directory
     */
    public static String getLiteTrieV30() {
        return BASE_DIRECTORY + LITE_TRIE_V30;
    }

    /**
     * get the name of the V3.2 Trie Detection file
     * @return the name relative to the working directory
     */
    public static String getLiteTrieV32() {
        return BASE_DIRECTORY + LITE_TRIE_V32;
    }
}
