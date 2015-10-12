/*
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
 */

package fiftyone.mobile.detection.test.common;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.properties.MatchMethods;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

public class Results {

    public final long startTime;

    public AtomicInteger count = new AtomicInteger();

    public AtomicLong checkSum = new AtomicLong();

    public final HashMap<MatchMethods, AtomicInteger> methods;

    public long getElapsedTime() {
        synchronized(this) {
            if (elapsedTime == 0) {
               elapsedTime = Calendar.getInstance().getTimeInMillis() - startTime; 
            }
        }
        return elapsedTime;
    }
    private long elapsedTime = 0;

    public long getAverageTime() {
        return getElapsedTime() / count.intValue();
    }

    public Results() {
        this.methods = new HashMap<MatchMethods, AtomicInteger>();
        this.methods.put(MatchMethods.CLOSEST, new AtomicInteger());
        this.methods.put(MatchMethods.EXACT, new AtomicInteger());
        this.methods.put(MatchMethods.NEAREST, new AtomicInteger());
        this.methods.put(MatchMethods.NONE, new AtomicInteger());
        this.methods.put(MatchMethods.NUMERIC, new AtomicInteger());
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public static Results detectLoopSingleThreaded(Provider provider, Iterable<String> userAgents, MatchProcessor processor) throws IOException {
        Results results = new Results();
        provider.dataSet.resetCache();
        Match match = provider.createMatch();
        for(String userAgent : userAgents) {
            processor.prepare();
            processor.process(provider.match(userAgent, match), results);
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
                java.util.logging.Logger.getLogger(Results.class.getName()).log(Level.SEVERE, null, ex);
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
                    java.util.logging.Logger.getLogger(Results.class.getName()).log(Level.SEVERE, null, ex);
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
                            processor.prepare();
                            Match match = provider.match(userAgent);
                            processor.process(match, results);
                            results.count.incrementAndGet();
                            results.methods.get(match.method).incrementAndGet();
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(getClass().getName()).log(
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
                    java.util.logging.Logger.getLogger(Results.class.getName()).log(Level.SEVERE, null, ex);
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

    public double getMethodPercentage(MatchMethods method) {
        return (double)methods.get(method).intValue() / (double)count.intValue();
    }
}