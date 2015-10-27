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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of UaProcessor that uses ExecutorCompletionService to do detections.
 * <p>
 * All results are stored in memory, so while this class is fast to execute it cannot carry
 * out large numbers of detections. It's fine for the 20k HTTP User-Agents distributed as a sample.
 * <p>
 */
public class FutureUaProcessor extends UaProcessor.Base {

    private class Result {
        DetectionResult result;
        long time;

        Result(long time, DetectionResult result) {
            this.result = result;
            this.time = time;
        }
    }

    // the collection of results from detection
    List<Result> results;

    // for collection of cpu time consumed during detection
    final static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    final static boolean cpuTimeSupported = threadMXBean.isThreadCpuTimeSupported();

    /** Get CPU time in nanoseconds. The run time may not support resolutions finer than microseconds*/
    public static long getCpuTime( ) {
        return cpuTimeSupported ? threadMXBean.getCurrentThreadCpuTime( ) : 0L;
    }


    public FutureUaProcessor(BufferedReader useragents, Provider provider, int numberOfThreads, int limit) throws IOException {
        super(useragents, provider, numberOfThreads, limit);
    }


    @Override
    public void process() throws Exception {

        ExecutorService es = Executors.newFixedThreadPool(numberOfThreads);
        BlockingQueue<Future<Result>> completionQueue = new LinkedBlockingQueue<Future<Result>>(20000);
        ExecutorCompletionService<Result> ecs = new ExecutorCompletionService<Result>(es, completionQueue);

        try {
            // keep track of how many futures we have processed
            // in this implementation we don't actually process any results before the end
            int taken = 0;

            // record the wall-clock time now
            testStart = System.currentTimeMillis();

            // submit all test HTTP User-Agent strings for detection
            while (count < limit && useragents.ready()) {
                final String nextUserAgent = useragents.readLine();
                ecs.submit(new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        // measure the cpu time for detection
                                        long start = getCpuTime();
                                        // do the detection
                                        DetectionResult result = provider.detect(nextUserAgent);
                                        long time = getCpuTime() - start;
                                        return new Result(time, result);
                                    }
                                }
                );
                count++;
            }

            // wait for all submitted futures to complete and store result in memory
            results = new ArrayList<Result>(count);
            for (int i = 0; i < count-taken; i++) {
                Result result = ecs.take().get();
                results.add(result);
            }

            // all results are now in so we can stop the wall-clock time
            stop = System.currentTimeMillis();

        } finally {
            es.shutdown();
        }
    }

    @Override
    public void printStats(PrintWriter output) {
        // sort the results into increasing time to execute (or if equal, then alpha sort on ua)
        Collections.sort(results, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                int diff = (int) (o1.time / 1000 - o2.time / 1000);
                return diff != 0 ? diff : o1.result.getTargetUserAgent().compareTo(o2.result.getTargetUserAgent());
            }
        });

        long totalTime = 0;
        for (Result result : results) {
            totalTime += result.time;
        }
        long elapsedMillis = stop - testStart;
        output.printf("%,d Detections%n", results.size());
        output.printf("Total cpu time %,d millis, %,d detections/sec, %,d micros average%n", totalTime/1000/1000, ((long)results.size()*1000*1000*1000/totalTime), totalTime/results.size()/1000);
        output.printf("Elapsed clock time %,d millis, %,d detections/sec, %,d micros average on %d threads%n", elapsedMillis, results.size()*1000/elapsedMillis, elapsedMillis*1000/results.size(), numberOfThreads);
        output.printf("Fastest cpu was %,d micros %s%n", results.get(0).time/1000, results.get(0).result.getTargetUserAgent());
        output.printf("Slowest cpu was %,d micros %s%n", results.get(results.size() - 1).time / 1000, results.get(results.size() - 1).result.getTargetUserAgent());
        super.printStats(output);
    }

    @Override
    public void writeResults(BufferedWriter writer) throws IOException {
        writeHeaders(writer);
        for (Result result: results) {
            writeResult(writer, result.result.getTargetUserAgent(), result.time / 1000, result.result);
        }
    }
}
