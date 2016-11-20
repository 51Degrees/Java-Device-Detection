package fiftyone.device.example.illustration;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.properties.MatchMethods;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Benchmark implements Closeable {

    public static class BenchmarkState {
        
        // the time benchmarking started.
        Date start = new Date();
        
        // number of User-Agents processed to determine the result.
        AtomicInteger count = new AtomicInteger();
        
        // used to ensure compiler optimiser doesn't optimise out the very
        // method that the benchmark is testing.
        int checkSum;
        
        // set to true when the queue has had all elements added to it.
        boolean addingComplete = false;
        
        // the number of threads running concurrently.
        int numberOfThreads;
        
        // queue of User-Agent strings for processing.
        LinkedBlockingQueue<String> queue;
        
        // provider to use for processing.
        Provider provider;
                
        // the average time in milliseconds for a single detection.
        public double getAverageDetectionTime() {
            return getTotalDetectionTime() / count.intValue();
        }
        
        public double getAverageDetectionTimePerThread() {
            return (getTotalDetectionTime() * numberOfThreads) / count.intValue();
        }
        
        // the total time taken for the benchmark.
        public double getTotalDetectionTime() {
            return (double)(new Date().getTime() - start.getTime());
        }
        
        // the number of User-Agents included in the test.
        public int getCount() {
            return count.intValue();
        }
    }
    
    private static class BenchmarkRunnable implements Runnable {
        
        private final BenchmarkState state;
        
        BenchmarkRunnable(
                BenchmarkState result)
                throws IOException {
            this.state = result;
        }
        
        @Override
        public void run() {
            try {
                int workerCheckSum = 0;
                Property isMobile = state.provider.dataSet.get("IsMobile");
                Match match = state.provider.createMatch();
                String userAgentString = state.queue.poll(1, TimeUnit.SECONDS);
                while (userAgentString != null || 
                       state.addingComplete == false) {
                    if (userAgentString != null) {
                        state.provider.match(userAgentString, match);
                        workerCheckSum ^= match.getValues(isMobile).toString().
                                hashCode();
                        int count = state.count.incrementAndGet();
                        if (count % 50000 == 0) {
                            System.out.println("===========================");
                            System.out.printf("Count: %d \r\n", count);
                            System.out.printf("Queue: %d \r\n", 
                                    state.queue.size());
                            System.out.printf(
                                    "getAverageDetectionTime: %f \r\n",
                                    state.getAverageDetectionTime());
                            System.out.printf(
                                    "getAverageDetectionTimePerThread: %f \r\n",
                                    state.getAverageDetectionTimePerThread());                            
                        }
                    }
                    userAgentString = state.queue.poll(1, TimeUnit.SECONDS);
                }
                synchronized(state) {
                    state.checkSum ^= workerCheckSum;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Benchmark.class.getName()).log(
                        Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Benchmark.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }

    private static final int defaultNumberOfThreads = 
            Runtime.getRuntime().availableProcessors();
    
    private static final int QUEUE_SIZE = 4096;
    
    // pattern detection matching provider
    private final Provider provider;
    
    // path to the User-Agent data file.
    private final String userAgentFile;
    
    // the number of concurrent detection threads.
    private final int numberOfThreads;

    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @param deviceDataFile
     * @param userAgentFile
     * @param numberOfThreads
     * @throws IOException if there was a problem reading from the data file.
     */
    public Benchmark(
            String deviceDataFile, 
            String userAgentFile,
            int numberOfThreads) 
            throws IOException, IllegalArgumentException {
        if (new File(deviceDataFile).exists() == false) {
            throw new IllegalArgumentException(String.format(
                    "File '%s' does not exist.",
                    deviceDataFile));
        }
        if (new File(userAgentFile).exists() == false) {
            throw new IllegalArgumentException(String.format(
                    "File '%s' does not exist.",
                    userAgentFile));
        }        
        System.out.printf("Creating provider from: %s\r\n",
                deviceDataFile);
        provider = new Provider(StreamFactory.create(
                deviceDataFile, false));
        this.userAgentFile = userAgentFile;
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Reads a CSV file containing User-Agents performing detection on each
     * one using the provider. 
     * 
     * @throws IOException if there was a problem reading from the data file.
     * @throws InterruptedException
     * @return results from the benchmark exercise.
     */
    public BenchmarkState run() 
            throws IOException, InterruptedException {
        String userAgentString;
        ExecutorService executor = Executors.newFixedThreadPool(
                numberOfThreads);
        BenchmarkState state = new BenchmarkState();
        state.numberOfThreads = numberOfThreads;
        state.queue = new LinkedBlockingQueue<String>(QUEUE_SIZE);
        state.provider = this.provider;
        for(int i = 0; i < numberOfThreads; i++) {
            executor.execute(new BenchmarkRunnable(state));
        }

        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(this.userAgentFile));
        try {
            while ((userAgentString = bufferedReader.readLine()) != null) {
                state.queue.put(userAgentString);
            }
            state.addingComplete = true;
            executor.shutdown();
            while (executor.isTerminated() == false) {
                // Do nothing.
            }
        }
        finally {
            bufferedReader.close();
        }
        return state;
    }

    /**
     * Closes the {@link fiftyone.mobile.detection.Dataset} by releasing data 
     * file readers and freeing the data file from locks. This method should 
     * only be used when the {@code Dataset} is no longer required, i.e. when 
     * device detection functionality is no longer required, or the data file 
     * needs to be freed.
     * 
     * @throws IOException if there was a problem accessing the data file.
     */
    @Override
    public void close() throws IOException {
        provider.dataSet.close();
    }

    /**
     * Instantiates this class and starts 
     * {@link #processCsv(java.lang.String, java.lang.String)} with default 
     * parameters.
     * 
     * @param args command line arguments.
     * @throws IOException if there was a problem accessing the data file.
     * @throws InterruptedException if the queue failed to be processed.
     */
    public static void main(String[] args) 
            throws IOException, InterruptedException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Must provide two data files.");
        }
        int numberOfThreads = args.length >= 3 ? 
                Integer.parseInt(args[2]) : defaultNumberOfThreads;
        System.out.println("Starting Bench Marking Example");
        Benchmark bm = new Benchmark(args[0], args[1], numberOfThreads);
        try {
            BenchmarkState result = bm.run();
            System.out.println("===========================");
            System.out.printf(
                "Average detections per second: %f \r\n", 
                result.getAverageDetectionTime());
            System.out.printf(
                "Average detections per second per thread: %f \r\n", 
                result.getAverageDetectionTimePerThread());                
            System.out.printf(
                "User-Agents processed: %d \r\n", 
                result.count.intValue());
            System.out.printf(
                "getPercentageNodeCacheMisses: %f \r\n", 
                bm.provider.dataSet.getPercentageNodeCacheMisses());            
            System.out.printf(
                "getPercentageProfilesCacheMisses: %f \r\n", 
                bm.provider.dataSet.getPercentageProfilesCacheMisses());
            System.out.printf(
                "getPercentageSignatureCacheMisses: %f \r\n", 
                bm.provider.dataSet.getPercentageSignatureCacheMisses());
            System.out.printf(
                "getPercentageStringsCacheMisses: %f \r\n", 
                bm.provider.dataSet.getPercentageStringsCacheMisses());
            System.out.printf(
                "getPercentageValuesCacheMisses: %f \r\n", 
                bm.provider.dataSet.getPercentageValuesCacheMisses());
            long[] counts = bm.provider.getMethodCounts();
            for(MatchMethods method : MatchMethods.values()) {
                System.out.printf(
                    "%s: %d \r\n", method, counts[method.ordinal()]);
            }
        }
        finally {
            bm.close();
        }
    }
}
