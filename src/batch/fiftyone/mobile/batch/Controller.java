package fiftyone.mobile.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;

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
public class Controller {

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String sourceFile = GetFile(args, 0);
        String useragentsFile = GetFile(args, 1);
        int limit = args.length > 2 ? Integer.parseInt(args[2]) : Integer.MAX_VALUE;
        int numberOfThreads = args.length > 3 ? Integer.parseInt(args[3]) : 4;

        if (sourceFile != null && useragentsFile != null) {
            BufferedReader useragents = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(useragentsFile), "UTF-8"));

            FileWriter fileWriter = new FileWriter(useragentsFile + "." + Integer.toString(numberOfThreads) + ".java");
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write("UserAgent" + "\t"
                    + "PlatformName" + "\t"
                    + "PlatformVersion" + "\t"
                    + "IsMobile" + "\t"
                    + "IsTablet" + "\t"
                    + "IsSmartPhone" + "\t"
                    + "IsSmallScreen" + "\t"
                    + "IsEmailBrowser" + "\t"
                    + "IsCrawler" + "\t"
                    + "BrowserName" + "\t"
                    + "BrowserVersion" + "\t"
                    + "ScreenMMHeight" + "\t"
                    + "ScreenMMWidth" + "\t"
                    + "HasTouchScreen" + "\t"
                    + "HasVirtualQwerty" + "\t"
                    + "HasQwertyPad" + "\r\n");

            long start = System.currentTimeMillis();
            
            // Enable the provide with a cache as there could be multiple
            // useragents in our test file and this will improve performance.
            Provider provider = new Provider(
                    // MemoryFactory.create(sourceFile),
                    StreamFactory.create(sourceFile), 
                    fiftyone.properties.DetectionConstants.CACHE_SERVICE_INTERVAL);
            
            System.out.println(String.format(
                    "'%dms' initialise time",
                    System.currentTimeMillis() - start));

            Process process = new Process(useragents, writer, provider, limit);

            Thread[] threads = new Thread[numberOfThreads];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(process);
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }

            long end = System.currentTimeMillis();
            System.out.println(String.format(
                    "'%.3fms' average detection time",
                    (double) (end - process.start) / (double) process.count));
            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentFormat.setMaximumFractionDigits(2);

            System.out.println(String.format(
                    "'%s' Node cache misses",
                    percentFormat.format(provider.dataSet.getPercentageNodeCacheMisses())));
            System.out.println(String.format(
                    "'%s' Profiles cache misses",
                    percentFormat.format(provider.dataSet.getPercentageProfilesCacheMisses())));
            System.out.println(String.format(
                    "'%s' Signature cache misses",
                    percentFormat.format(provider.dataSet.getPercentageSignatureCacheMisses())));
            System.out.println(String.format(
                    "'%s' Strings cache misses",
                    percentFormat.format(provider.dataSet.getPercentageStringsCacheMisses())));
            System.out.println(String.format(
                    "'%s' Values cache misses",
                    percentFormat.format(provider.dataSet.getPercentageValuesCacheMisses())));

            provider.dataSet.dispose();

            writer.close();
            fileWriter.close();
            useragents.close();
        }

        System.exit(0);
    }

    static String GetFile(String[] args, int index) {
        if (index < args.length) {
            String file = args[index];
            if (new File(file).exists()) {
                return file;
            }
        }
        return null;
    }
}
