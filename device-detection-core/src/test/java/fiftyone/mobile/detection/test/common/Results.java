/*
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
 */

package fiftyone.mobile.detection.test.common;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.properties.MatchMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Results {

    protected static Logger logger = LoggerFactory.getLogger(Results.class);

    public final long startTime;

    private long elapsedTime = 0;

    public AtomicInteger count = new AtomicInteger();

    public AtomicLong checkSum = new AtomicLong();

    public final HashMap<MatchMethods, AtomicInteger> methods;

    public long getElapsedTime() {
        synchronized(this) {
            if (elapsedTime == 0) {
               elapsedTime = System.currentTimeMillis() - startTime;
            }
        }
        return elapsedTime;
    }

    public long getAverageTime() {
        return getElapsedTime() / count.intValue();
    }

    public double getMethodPercentage(MatchMethods method) {
        return (double)methods.get(method).intValue() / (double)count.intValue();
    }

    public Results() {
        this.methods = new HashMap<MatchMethods, AtomicInteger>();
        this.methods.put(MatchMethods.CLOSEST, new AtomicInteger());
        this.methods.put(MatchMethods.EXACT, new AtomicInteger());
        this.methods.put(MatchMethods.NEAREST, new AtomicInteger());
        this.methods.put(MatchMethods.NONE, new AtomicInteger());
        this.methods.put(MatchMethods.NUMERIC, new AtomicInteger());
        this.startTime = System.currentTimeMillis();
    }

    public static Results detectLoopSingleThreaded(Provider provider, 
            Iterable<String> userAgents, MatchProcessor processor) throws IOException {
        Results results = new Results();
        provider.dataSet.resetCache();
        PatternDetector patternDetector = new PatternDetector(processor, provider, results);
        
        for(String userAgent : userAgents) {
            try {
                if (!patternDetector.newDetector(userAgent).call()){
                    fail("Detection failed");
                }
            } catch (Exception ex) {
                fail(ex.getClass().getName() + " " + ex.getMessage());
            }
        }
        return results;
    }

    public static Results detectLoopSingleThreaded(TrieProvider provider, Iterable<String> userAgents) {
        Results results = new Results();
        TrieDetector trieDetector = new TrieDetector(provider, results);
        for (String userAgent : userAgents) {
            try {
                if (!trieDetector.newDetector(userAgent).call()) {
                    fail("Detection failed");
                }
            } catch (Exception ex) {
                fail(ex.getClass().getName() + " " + ex.getMessage());
            }
        }
        return results;
    }

    public static Results detectLoopMultiThreaded(TrieProvider provider,
                                                  Iterable<String> userAgents) {
        final Results results = new Results();
        int detections = executeDetectionLoop(userAgents, new TrieDetector(provider, results));

        assertTrue(String.format(
                        "Loop performed '%d' iterations but results show '%d'.",
                        detections,
                        results.count.intValue()),
                detections == results.count.intValue());

        return results;
    }

    public static Results detectLoopMultiThreaded(final Provider provider,
                                                  Iterable<String> userAgents,
                                                  final MatchProcessor processor) throws IOException {
        final Results results = new Results();
        provider.dataSet.resetCache();

        int detections = executeDetectionLoop(userAgents,  new PatternDetector(processor, provider, results));

        assertTrue(String.format(
                        "Loop performed '%d' iterations but results show '%d'.",
                        detections,
                        results.count.intValue()),
                detections == results.count.intValue());
        return results;
    }

    /**
     * A detector that can be be submitted to {@link #executeDetectionLoop(Iterable, CallableDetector)}
     */
    private static abstract class CallableDetector implements Callable<Boolean> {
        protected String userAgent;

        /**
         * return a new Callable with the User-Agent to test
         * @param userAgent the User-Agent to test
         * @return a CallableDetector of the appropriate class
         * @throws Exception if a detector could not be created
         */
        public abstract CallableDetector newDetector(String userAgent) throws Exception;
    }

    /**
     * Trie Callable Detector
     */
    public static  class TrieDetector extends CallableDetector {
        private final Results results;
        private final TrieProvider provider;

        public TrieDetector(TrieProvider provider, Results results) {
            this.provider = provider;
            this.results = results;
        }

        @Override
        public CallableDetector newDetector(String userAgent) throws Exception {
            TrieDetector instance = new TrieDetector(provider, results);
            instance.userAgent = userAgent;
            return instance;
        }

        @Override
        public Boolean call() throws Exception {
            provider.getDeviceIndex(userAgent);
            results.count.incrementAndGet();
            return true;
        }
    }

    /**
     * Pattern Callable Detector
     */
    public static class PatternDetector extends  CallableDetector {
        private final MatchProcessor processor;
        private final Provider provider;
        private final Results results;

        public PatternDetector(MatchProcessor processor, Provider provider, Results results) {
            this.processor = processor;
            this.provider = provider;
            this.results = results;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                processor.prepare();
                Match match = provider.match(userAgent);
                processor.process(match, results);
                results.count.incrementAndGet();
                results.methods.get(match.getMethod()).incrementAndGet();
                return true;
            } catch (Throwable t) {
                logger.error("Match failed with exception", t);
                return false;
            }
        }

        @Override
        public CallableDetector newDetector(String userAgent) throws Exception {
            // create new instance of self
            CallableDetector instance = new PatternDetector(processor, provider, results);
            // set the User-Agent up
            instance.userAgent = userAgent;
            return instance;
        }
    }

    /**
     * Carry out repeated detections on the passed User-Agents in a multi-threaded way
     * @param userAgents the User-Agents to test
     * @param detector a {@link fiftyone.mobile.detection.test.common.Results.CallableDetector} to do the
     *                 detections with
     * @return the number of detections carried out
     */
    private static int executeDetectionLoop(final Iterable<String> userAgents, CallableDetector detector) {
        final ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final List<Future<Boolean>> outcomes = new ArrayList<Future<Boolean>>();
        try {
            try {
                for (final String userAgent : userAgents) {
                    CallableDetector callableDetector = detector.newDetector(userAgent);
                    Future<Boolean> future = e.submit(callableDetector);
                    outcomes.add(future);
                }
            } catch (Exception e1) {
                fail("Could not submit task for execution " + e1.getMessage() + Arrays.asList(e1.getStackTrace()));
            }

            // Wait for all the futures to complete.
            // milliseconds per test which is generally the longest
            // anything should take even on a very slow system or where
            // memory checks are being performed.
            try {
                for (Future<Boolean> outcome: outcomes) {
                    assertTrue("A detection did not complete before timeout",
                            outcome.get(25000, TimeUnit.MILLISECONDS));
                }
            } catch (Exception e1) {
                fail("Submitted tasks did not complete " + e1.getClass().getName());

            }
        } finally {
            e.shutdownNow();
            try {
                assertTrue("Execution pool failed to shut down  before timeout",
                        e.awaitTermination(outcomes.size() * 20, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e1) {
                fail("Execution pool shut down interrupted");
            }
        }
        return outcomes.size();
    }
}