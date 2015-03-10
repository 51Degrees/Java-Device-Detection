package fiftyone.mobile.console;

import fiftyone.mobile.detection.AutoUpdateException;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.detection.factories.TrieFactory;
import java.io.File;
import java.io.IOException;

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
public class Command {

    /**
     * Array of user agents for detection.
     */
    private static final String[] USERAGENTS = new String[]{
        // Internet explorer
        "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
        // A set top box running Android, not a mobile device.
        "Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; Rikomagic MK802IIIS Build/JRO03H) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // Galaxy Note from Samsung. 
        "Mozilla/5.0 (Linux; U; Android 4.1.2; de-de; GT-N8020 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // A possible future version of the Galaxy Note to show how numeric fallback handling works.
        "Mozilla/5.0 (Linux; U; Android 4.1.2; de-de; GT-N8420 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30",
        // A Chrome user agent.
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36"
    };

    public static void main(String[] args) throws IOException, AutoUpdateException, Exception {

        System.out.println("Getting data file.");
        fiftyone.mobile.detection.AutoUpdate.update("999999R2DS76ZAJAB22GAWJMF8CR44RYRC6U466CWL8HMQN333JPLRTCQKRLFHZETTDVNRM36X8KRPBRYUW9QZA", "C:\\Users\\tom\\Desktop\\JavaData.dat");
        System.out.println("Get complete.");
        
        return;
        
    }
}
