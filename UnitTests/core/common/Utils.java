package common;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.properties.MatchMethods;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.TrieProvider;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

public class Utils {
    
    /**
     * Asserts that the file exists. Used at the beginning of each test.
     * @param dataFile 
     */
    public static void checkFileExists(String dataFile) {
        assertTrue(String.format(
                "Data file '%s' could not be found. " +
                "See https://51degrees.com/compare-data-options to complete this test.",
                dataFile),
            new File(dataFile).exists());
    }
    
    /**
     * Reads the contents of the file provided and returns a byte array.
     * @param file to be read
     * @return a byte array of the file content
     * @throws IOException if the file size is more than 2GB
     */
    public static byte[] readAllBytes(String file) throws IOException {
        return readAllBytes(new File(file));
    }

    /**
     * Reads the contents of the file provided and returns a byte array.
     * @param file to be read
     * @return a byte array of the file content
     * @throws IOException if the file size is more than 2GB
     */
    public static byte[] readAllBytes(File file) throws IOException {
        RandomAccessFile fileHandle = new RandomAccessFile(file, "r");
        try {
            long longlength = fileHandle.length();
            int length = (int)longlength;
            if (length != longlength)
                throw new IOException("File size cannot be greater than 2GB");
            byte[] array = new byte[length];
            fileHandle.readFully(array);
            return array;
        } finally {
            fileHandle.close();
        }
    }
    
    public static Results detectLoopSingleThreaded(Provider provider, Iterable<String> userAgents, MatchProcessor processor) throws IOException {
        Results results = new Results();
        provider.dataSet.resetCache();
        Match match = provider.createMatch();
        for(String userAgent : userAgents) {
            processor.Process(provider.match(userAgent, match), results);
            results.count.incrementAndGet();
            results.methods.get(match.method).incrementAndGet();
        }
        return results;
    }
    
    public static Results detectLoopSingleThreaded(TrieProvider provider, Iterable<String> userAgents) {
        Results results = new Results();
        for (String userAgent : userAgents) {
            try {
                int value = provider.getDeviceIndex(userAgent);
                results.count.incrementAndGet();
            } catch (Exception ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

    public static Results detectLoopMultiThreaded(TrieProvider provider, 
            Iterable<String> userAgents) {
        final Results results = new Results();
        ExecutorService e = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        int count = 0;
        try {
            for (final String userAgent : userAgents) {
                count++;
                e.submit(new Runnable() {

                    @Override
                    public void run() {
                        results.count.incrementAndGet();
                    }

                });
            }
        } catch(Exception ex) {
            
        } finally {
            if (e != null) {
                e.shutdown();
                try {
                    // Wait for all the threads to complete. Allow 20
                    // milliseconds per test which is generally the longest 
                    // anything should take even on a very slow system or where
                    // memory checks are being performed.
                    e.awaitTermination(count * 20, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        assertTrue(String.format(
                "Loop performed '%d' iterations but results show '%d'.", 
                count,
                results.count.intValue()), 
                count == results.count.intValue());
        return results;
    }
    
    public static Results detectLoopMultiThreaded(final Provider provider, 
        Iterable<String> userAgents, final MatchProcessor processor) throws IOException {
        final Results results = new Results();
        int count = 0;
        provider.dataSet.resetCache();
        ExecutorService e = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        try {
            for (final String userAgent : userAgents) {
                count++;
                e.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Match match = provider.match(userAgent);
                            processor.Process(match, results);
                            results.count.incrementAndGet();
                            results.methods.get(match.method).incrementAndGet();
                        } catch (IOException ex) {
                            Logger.getLogger(Utils.class.getName()).log(
                                    Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        } finally {
            if (e != null) {
                e.shutdown();
                try {
                    // Wait for all the threads to complete. Allow 20
                    // milliseconds per test which is generally the longest 
                    // anything should take even on a very slow system or where
                    // memory checks are being performed.
                    e.awaitTermination(count * 20, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        assertTrue(String.format(
                "Loop performed '%d' iterations but results show '%d'.", 
                count,
                results.count.intValue()), 
                count == results.count.intValue());
        return results;
    }
    
    public static void reportMethods(Map<MatchMethods, AtomicInteger> methods) {
        int total = 0;
        for(AtomicInteger value : methods.values()){
            total += value.intValue();
        }
        for(Entry<MatchMethods, AtomicInteger> method : methods.entrySet()) {
            System.out.printf("Method '%s' used '%.0f%%'\r\n",
                method.getKey(),
                (double)method.getValue().intValue() / (double)total * (double)100);
        }
    }

    public static void reportPool(fiftyone.mobile.detection.entities.stream.Dataset dataSet) {
        System.out.printf("Readers in queue '%d'\r\n", dataSet.getReadersQueued());
        System.out.printf("Readers created '%d'\r\n", dataSet.getReadersCreated());
    }

    public static void reportCache(Dataset dataSet) {
        System.out.printf("Node cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getNodeCacheSwitches(),
            dataSet.getPercentageNodeCacheMisses() * (double)100);
        System.out.printf("Profiles cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getProfilesCacheSwitches(),
            dataSet.getPercentageProfilesCacheMisses() * (double)100);
        System.out.printf("Ranked Signatures cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getRankedSignatureCacheSwitches(),
            dataSet.getPercentageRankedSignatureCacheMisses() * (double)100);
        System.out.printf("Signatures cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getSignatureCacheSwitches(),
            dataSet.getPercentageSignatureCacheMisses() * (double)100);
        System.out.printf("Strings cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getStringsCacheSwitches(),
            dataSet.getPercentageStringsCacheMisses() * (double)100);
        System.out.printf("Values cache switches '%d' with '%.0f%%' misses\r\n",
            dataSet.getValuesCacheSwitches(),
            dataSet.getPercentageValuesCacheMisses() * (double)100);
    }

    public static void reportTime(Results results) {
        System.out.printf("Total of '%.2f's for '%d' tests.\r\n",
            (double)results.getElapsedTime() / (double)1000,
            results.count.intValue());
        System.out.printf("Average '%.2f'ms per test.\r\n",
            (double)results.getElapsedTime() / results.count.doubleValue());
    }

    public static void assertPool(Provider provider) {
        if (provider.dataSet instanceof fiftyone.mobile.detection.entities.stream.Dataset) {
            fiftyone.mobile.detection.entities.stream.Dataset dataSet = 
                    (fiftyone.mobile.detection.entities.stream.Dataset)provider.dataSet;
            
            // Check the size of the reader queues for equality now time has 
            // passed and readers should have been returned to it.
            int queued = dataSet.getReadersQueued();
            int created = dataSet.getReadersCreated();
            assertTrue(
                String.format(
                    "DataSet pooled readers mismatched. '%d' created and '%d' queued.",
                    created,
                    queued),
                created == queued);
        }
    }

    public static void reportProvider(Provider provider) {
        System.out.printf("User-Agent cache switches '%d' with '%.0f%%' misses\r\n",
            provider.getCacheSwitches(),
            provider.getPercentageCacheMisses());
        if (provider.dataSet instanceof fiftyone.mobile.detection.entities.stream.Dataset)
        {
            reportCache(provider.dataSet);
            reportPool((fiftyone.mobile.detection.entities.stream.Dataset)provider.dataSet);
        }
    }    
}
