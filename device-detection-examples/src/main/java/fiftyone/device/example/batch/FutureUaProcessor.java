package fiftyone.device.example.batch;

import fiftyone.mobile.detection.Match;
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
 * out large numbers of detections. It's fine for the 20k user agents distributed as a sample.
 * <p>
 */
public class FutureUaProcessor extends UaProcessor.Base {

    private class Result {
        String userAgent;
        String deviceId;
        Match match;
        long time;

        Result(String userAgent, long time, String deviceId, Match match) {
            this.userAgent = userAgent;
            this.match = match;
            this.time = time;
            this.deviceId = deviceId;
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
        ExecutorCompletionService<Result> ecs = new ExecutorCompletionService<Result>(es);

        try {
            // keep track of how many futures we have processed
            // in this implementation we don't actually process any results before the end
            int taken = 0;
            // record the wall-clock time now
            start = System.currentTimeMillis();

            // submit all test user agent strings for detection
            while (count < limit && useragents.ready()) {
                final String nextUserAgent = useragents.readLine();
                ecs.submit(new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        // measure the cpu time for detection
                                        long start = getCpuTime();
                                        // do the detection
                                        Match match = provider.match(nextUserAgent);
                                        return new Result(nextUserAgent, getCpuTime() - start, match.getDeviceId(), match);
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
                return diff != 0 ? diff : o1.userAgent.compareTo(o2.userAgent);
            }
        });

        long totalTime = 0;
        for (Result result : results) {
            totalTime += result.time;
        }
        long elapsedMillis = stop - start;
        output.printf("%,d Detections%n", results.size());
        output.printf("Elapsed clock time %,d millis on %d threads, %,d detections/sec, %,d micros average%n", elapsedMillis, numberOfThreads, results.size()*1000/elapsedMillis, elapsedMillis*1000/results.size());
        output.printf("Total cpu time %,d millis%n", totalTime/1000/1000);
        output.printf("Average cpu was %,d micros%n", totalTime/results.size()/1000);
        output.printf("Fastest cpu was %,d micros %s%n", results.get(0).time/1000, results.get(0).userAgent);
        output.printf("Slowest cpu was %,d micros %s%n", results.get(results.size() - 1).time / 1000, results.get(results.size() - 1).userAgent);
        super.printStats(output);
    }

    @Override
    public void writeResults(BufferedWriter writer) throws IOException {
        writeHeaders(writer);
        for (Result result: results) {
            writeMatch(writer, result.userAgent, result.time/1000, result.match);
        }
    }
}
