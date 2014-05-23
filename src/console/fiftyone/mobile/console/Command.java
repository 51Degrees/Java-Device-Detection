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

        // Set this parameter to the data set file name.
        String patternFileName = args.length > 0 ? args[0] : "";
        String trieFileName = args.length > 1 ? args[1]: "";
        
        TrieProvider t = null;
        Provider p;
        
        // Construct the provider based on the file names provided.
        if (new File(patternFileName).exists()) 
        {
            // The file exists so use it to initialise the provider.
            p = new Provider(StreamFactory.create(patternFileName));
        } else {
            // Uses the free "Lite" data embedded in the Core package 
            // as the dataset. Additional data sets can be purchased
            // from http://51degrees.com/products/store
            p = new Provider();
        }
        
        if (new File(trieFileName).exists()) {
            t = TrieFactory.create(trieFileName);
        }
        
        System.out.println("\t\t\t*** Data Set Information ***");
        System.out.printf("Name\t\t\t%s\r\n", p.dataSet.getName());
        System.out.printf("Published\t\t%tc\r\n", p.dataSet.published);
        System.out.printf("Next Update\t\t%tc\r\n", p.dataSet.nextUpdate);
        System.out.printf("Signatures\t\t%d\r\n", p.dataSet.signatures.size());
        System.out.printf("Device Combinations\t%d\r\n", p.dataSet.deviceCombinations);

        for (String userAgent : USERAGENTS) {

            // Match the user agent to properties.
            Match patternMatch = p.match(userAgent);

            // Show the profiles that related to the patternMatch result.
            System.out.println("\r\n\t\t\t*** Pattern Detection Results ***");
            if (p.dataSet.getName().equals("Lite") == false) {
                System.out.print("Found\t\t\t");
                for (int i = 0; i < patternMatch.getProfiles().length; i++) {
                    System.out.printf("%s", patternMatch.getProfiles()[i].toString());
                    System.out.print(" ");
                }
            }

            // Show how the result was determined.
            System.out.print("\r\n");
            System.out.printf("Target User Agent\t%s\r\n", patternMatch.getTargetUserAgent());
            System.out.printf("Relevant Sub Strings\t%s\r\n", patternMatch.toString());
            System.out.printf("Closest Sub Strings\t%s\r\n", patternMatch.getUserAgent());
            System.out.printf("Difference\t\t%d\r\n", patternMatch.getDifference());
            System.out.printf("Method\t\t\t%s\r\n", patternMatch.method.toString());
            System.out.printf("Root Nodes Evaluated\t%d\r\n", patternMatch.getRootNodesEvaluated());
            System.out.printf("Nodes Evaluated\t\t%d\r\n", patternMatch.getNodesEvaluated());
            System.out.printf("Strings Read\t\t%d\r\n", patternMatch.getStringsRead());
            System.out.printf("Signatures Read\t\t%d\r\n", patternMatch.getSignaturesRead());
            System.out.printf("Signatures Compared\t%d\r\n", patternMatch.getSignaturesCompared());
            System.out.printf("Closest Signatures\t%d\r\n", patternMatch.getClosestSignaturesCount());

            // Demonstrate some example lite properties.
            System.out.println("\r\n\t\t\t*** Example Lite Properties ***");

            if (patternMatch.getValues("IsMobile") != null) {
                System.out.printf("IsMobile\t\t%b\r\n", 
                        patternMatch.getValues("IsMobile").toBool());
            }
            if (patternMatch.getValues("ScreenPixelsWidth") != null) {
                System.out.printf("ScreenPixelsWidth\t%f\r\n", 
                        patternMatch.getValues("ScreenPixelsWidth").toDouble());
            }
            if (patternMatch.getValues("ScreenPixelsHeight") != null) {
                System.out.printf("ScreenPixelsHeight\t%f\r\n", 
                        patternMatch.getValues("ScreenPixelsHeight").toDouble());
            }

            // Demonstrate some example Premium properties.	
            if (p.dataSet.getName().equals("Lite") == false) {
                System.out.println("\r\n\t\t\t*** Example Enhanced Properties ***");
                if (patternMatch.getValues("IsMediaHub") != null) {
                    System.out.printf("IsMediaHub\t\t%b\r\n", 
                            patternMatch.getValues("IsMediaHub").toBool());
                }
                if (patternMatch.getValues("ScreenMMWidth") != null) {
                    System.out.printf("ScreenMMWidth\t\t%f\r\n", 
                            patternMatch.getValues("ScreenMMWidth").toDouble());
                }
                if (patternMatch.getValues("ScreenMMHeight") != null) {
                    System.out.printf("ScreenMMHeight\t\t%f\r\n", 
                            patternMatch.getValues("ScreenMMHeight").toDouble());
                }
            }
            
            // Detect the device using the trie data if a provider is available.
            if (t != null) {
                
                System.out.println("\r\n\t\t\t*** Trie Detection Results ***");
                System.out.print("\r\n");
                System.out.printf("Target User Agent\t%s\r\n", 
                        userAgent);
                System.out.printf("Found User Agent \t%s\r\n", 
                        t.getUserAgent(userAgent));
                                
                int trieDeviceIndex = t.getDeviceIndex(userAgent);
                
                System.out.println("\r\n\t\t\t*** Example Lite Properties ***");
                
                if (t.PropertyNames().contains("IsMobile")) {
                    System.out.printf("IsMobile\t\t%s\r\n", 
                            t.getPropertyValue(trieDeviceIndex, "IsMobile"));
                }
                if (t.PropertyNames().contains("ScreenPixelsWidth")) {
                    System.out.printf("ScreenPixelsWidth\t%s\r\n", 
                            t.getPropertyValue(trieDeviceIndex, "ScreenPixelsWidth"));
                }
                if (t.PropertyNames().contains("ScreenPixelsHeight")) {
                    System.out.printf("ScreenPixelsHeight\t%s\r\n", 
                            t.getPropertyValue(trieDeviceIndex, "ScreenPixelsHeight"));
                }                
            }
        }

        System.exit(0);
    }
}
