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
package fiftyone.device.example.batch;

import fiftyone.mobile.detection.DetectionResult;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Values;

import java.io.*;

/**
 * Interface defines three phase processing of a set of User-Agent Header Values:
 * <p/>
 * <ul>
 *     <li>Process the headers {@link #process()}</li>
 *     <li>Print statistics relating the the last run of {@link #process()}</li>
 *     <li>Write the detailed results of the last run of {@link #process()}</li>
 * </ul>
 */
public interface UaProcessor {

    /**
     * Process the set of UA Headers
     * @throws Exception
     */
    void process() throws Exception;

    /**
     * Writes timings (overall) of of a detection test
     * @param output  a {@link PrintWriter} to report the timings on
     */
    void printStats(PrintWriter output);

    /**
     * Writes results (for each UA string) of detection test
     * @param writer a {@link BufferedWriter} to write the results to
     * @throws IOException
     */
    void writeResults(BufferedWriter writer) throws IOException;

    /**
     * Base implementation of {@link UaProcessor} accepting a {@link BufferedReader} from which UA Strings can be read
     * and a {@link Provider} to use to do detections
     */
    abstract class Base implements UaProcessor {
        protected final int limit;
        protected final int numberOfThreads;
        protected final BufferedReader useragents;
        protected final Provider provider;

        protected int count = 0; // the number of UAs processed so far
        protected long testStart; // the Unix time in millis that the process started
        protected long stop; // the Unix time in millis that the process completed

        public Base(BufferedReader useragents, Provider provider, int numberOfThreads, int limit) throws IOException {
            this.useragents = useragents;
            this.provider = provider;
            this.limit = limit;
            this.numberOfThreads = numberOfThreads;
        }

        @Override
        public void printStats(PrintWriter output) {
            if (provider.dataSet instanceof fiftyone.mobile.detection.entities.stream.Dataset) {
                output.printf("'%.2f' Node cache misses%n", provider.dataSet.getPercentageNodeCacheMisses());
                output.printf("'%.2f' Profiles cache misses%n", provider.dataSet.getPercentageProfilesCacheMisses());
                output.printf("'%.2f' Signature cache misses%n", provider.dataSet.getPercentageSignatureCacheMisses());
                output.printf("'%.2f' Strings cache misses%n", provider.dataSet.getPercentageStringsCacheMisses());
                output.printf("'%.2f' Values cache misses%n", provider.dataSet.getPercentageValuesCacheMisses());
            }
            output.flush();
        }


        /**
         * Utility for descendants to write a set of headers relating to a match
         * @param writer a writer to wrtie to
         * @throws IOException
         */
        protected static void writeHeaders(BufferedWriter writer) throws IOException {
            writer.write("UserAgent" + "\t"
                    + "time\t"
                    + "match type\t"
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
        }

        /**
         * Utility for descendants to write details relating to a match
         * @param writer a writer to wrtie to
         * @throws IOException
         */
        protected static void writeResult(BufferedWriter writer, String s, long time, DetectionResult match) throws IOException {
            writer.write(s + "\t" + time + "\t" + match.getMethod() + "\t"
                    + getValue(match, "PlatformName") + "\t"
                    + getValue(match, "PlatformVersion") + "\t"
                    + getValue(match, "IsMobile") + "\t"
                    + getValue(match, "IsTablet") + "\t"
                    + getValue(match, "IsSmartPhone") + "\t"
                    + getValue(match, "IsSmallScreen") + "\t"
                    + getValue(match, "IsEmailBrowser") + "\t"
                    + getValue(match, "IsCrawler") + "\t"
                    + getValue(match, "BrowserName") + "\t"
                    + getValue(match, "BrowserVersion") + "\t"
                    + getValue(match, "ScreenMMHeight") + "\t"
                    + getValue(match, "ScreenMMWidth") + "\t"
                    + getValue(match, "HasTouchScreen") + "\t"
                    + getValue(match, "HasVirtualQwerty") + "\t"
                    + getValue(match, "HasQwertyPad") + "\r\n");

        }

        /**
         * local utility to format a property from a match
         * @param result the match to format
         * @param propertyName the name of the property to format
         * @return the formatted value
         */
        private static String getValue(DetectionResult result, String propertyName) {
            try {
                if (result != null) {
                    Values value = result.getValues(propertyName);
                    if (value != null) {
                        return value.toString();
                    }
                }
            } catch (IOException ignored) {
            }
            return "";
        }
    }
}
