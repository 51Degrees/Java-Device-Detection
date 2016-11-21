package fiftyone.device.example.illustration;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.properties.MatchMethods;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Benchmark {

    /**
     * Encapsulates the logic that is executed in each thread of the benchmark.
     */
    private static class BenchmarkRunnable implements Runnable {
        
        // the benchmark that is being executed
        private final Benchmark bm;
        
        BenchmarkRunnable(
                Benchmark bm)
                throws IOException {
            this.bm = bm;
        }
        
        @Override
        public void run() {
            try {
                int workerCheckSum = 0;
                Property isMobile = bm.provider.dataSet.get("IsMobile");
                Match match = bm.provider.createMatch();
                String userAgentString = bm.queue.poll(1, TimeUnit.SECONDS);
                while (userAgentString != null || 
                       bm.addingComplete == false) {
                    if (userAgentString != null) {
                        // the benchmark is for detection time only
                        long start = System.nanoTime();
                        bm.provider.match(userAgentString, match);
                        bm.elapsedNano.addAndGet(
                            System.nanoTime() - start);
                        
                        // calculate a checksum to compare different runs on
                        // the same data
                        if (match.getSignature() != null) {
                            workerCheckSum += match.getSignature().getRank();
                        }
                        workerCheckSum += match.getValues(isMobile).
                                toStringArray().length;
                        bm.count.incrementAndGet();
                    }
                    userAgentString = bm.queue.poll(1, TimeUnit.SECONDS);
                }
                synchronized(bm) {
                    bm.checkSum += workerCheckSum;
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

    // the default number of threads if one is not provided.
    private static final int defaultNumberOfThreads = 
            Runtime.getRuntime().availableProcessors();
    
    // the maximum size of the queue of User-Agents to benchmark.
    private static final int QUEUE_SIZE = 512;
  
    // path to the User-Agent data file.
    private final String userAgentFile;
    
    // number of User-Agents processed to determine the result.
    private final AtomicInteger count = new AtomicInteger();
    
    // processing time in millseconds from all threads
    private final AtomicLong elapsedNano = new AtomicLong();
    
    // used to ensure compiler optimiser doesn't optimise out the very
    // method that the benchmark is testing.
    private int checkSum;

    // set to true when the queue has had all elements added to it.
    private boolean addingComplete = false;

    // queue of User-Agent strings for processing.
    private LinkedBlockingQueue<String> queue;

    // provider to use for processing.
    private Provider provider;
               
    // the average time in milliseconds for a single thread.
    public double getAverageDetectionTimePerThread() {
        return (elapsedNano.doubleValue() / 1000000) / getCount();
    }

    // the number of User-Agents included in the test.
    public int getCount() {
        return count.intValue();
    }    
    
    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @param userAgentFile
     * @throws IOException if there was a problem reading from the data file.
     */
    public Benchmark(String userAgentFile) 
            throws IOException, IllegalArgumentException {
        if (new File(userAgentFile).exists() == false) {
            throw new IllegalArgumentException(String.format(
                    "File '%s' does not exist.",
                    userAgentFile));
        }
        this.userAgentFile = userAgentFile;
        this.queue = new LinkedBlockingQueue<String>(QUEUE_SIZE);
    }

    /**
     * Reads a CSV file containing User-Agents performing detection on each
     * one using the provider.
     * 
     * @param dataSet to be used for the benchmark.
     * @param numberOfThreads
     * @throws IOException if there was a problem reading from the data file.
     * @throws InterruptedException
     */
    public void run(Dataset dataSet, int numberOfThreads)
            throws IOException, InterruptedException {
        String userAgentString;
        
        // initialise the benchmark variables
        this.provider = new Provider(dataSet);
        this.elapsedNano.set(0);
        this.count.set(0);
        this.checkSum = 0;
        
        // start multiple threads in a fixed pool
        ExecutorService executor = Executors.newFixedThreadPool(
                numberOfThreads);
        for(int i = 0; i < numberOfThreads; i++) {
            executor.execute(new BenchmarkRunnable(this));
        }

        // read each line of the User-Agents source file adding them to the
        // shared queue for each thread
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(this.userAgentFile));
        try {
            int userAgentsRead = 0;
            while ((userAgentString = bufferedReader.readLine()) != null && userAgentsRead < 250000) {
                userAgentsRead++;
                this.queue.put(userAgentString);
                if (userAgentsRead % 50000 == 0) {
                    System.out.print("+");
                }
            }
            System.out.print("\r\n");
            this.addingComplete = true;
            executor.shutdown();
            while (executor.isTerminated() == false) {
                // Do nothing.
            }
        }
        finally {
            bufferedReader.close();
        }
       
        // output the results from the benchmark to the console
        System.out.println();
        System.out.printf(
            "Average millseconds per detection per thread: %f \r\n", 
            getAverageDetectionTimePerThread());
        System.out.printf(
            "Concurrent threads: %d \r\n", 
            numberOfThreads);
        System.out.printf(
            "User-Agents processed: %d \r\n", 
            getCount());
        System.out.printf(
            "getPercentageNodeCacheMisses: %f \r\n", 
            dataSet.getPercentageNodeCacheMisses());            
        System.out.printf(
            "getPercentageProfilesCacheMisses: %f \r\n", 
            dataSet.getPercentageProfilesCacheMisses());
        System.out.printf(
            "getPercentageSignatureCacheMisses: %f \r\n", 
            dataSet.getPercentageSignatureCacheMisses());
        System.out.printf(
            "getPercentageStringsCacheMisses: %f \r\n", 
            dataSet.getPercentageStringsCacheMisses());
        System.out.printf(
            "getPercentageValuesCacheMisses: %f \r\n", 
            dataSet.getPercentageValuesCacheMisses());
        long[] counts = provider.getMethodCounts();
        for(MatchMethods method : MatchMethods.values()) {
            System.out.printf(
                "%s: %d \r\n", method, counts[method.ordinal()]);
        }
        System.out.printf(
            "Checksum: %d \r\n", 
            checkSum);        
    }

    /**
     * Runs the benchmark for the data set provided.
     * 
     * @param dataSet initialised and ready for device detection
     * @param userAgentFile path to a text file of User-Agents
     * @param numberOfThreads number of concurrent threads
     * @throws IOException
     * @throws InterruptedException 
     */
    private static void runBenchmark(
            Dataset dataSet, 
            String userAgentFile, 
            int numberOfThreads) throws IOException, InterruptedException {
        try {
            new Benchmark(userAgentFile).run(dataSet, numberOfThreads);
        }
        finally {
            dataSet.close();
        }
    }
        
    /**
     * Runs three different benchmarks for popular configurations.
     * 
     * @param deviceDataFile path to the 51Degrees device data file for testing
     * @param userAgentFile path to a text file of User-Agents
     * @param numberOfThreads number of concurrent threads
     * @throws IOException
     * @throws InterruptedException 
     */    
    private static void runBenchmarks(
        String deviceDataFile,
        String userAgentFile,
        int numberOfThreads) throws IOException, InterruptedException {
        
        System.out.printf("Benchmarking stream memory dataset: %s\r\n", 
                deviceDataFile);
        runBenchmark(
                StreamFactory.create(Files.readAllBytes(Paths.get(deviceDataFile))),
                userAgentFile,
                numberOfThreads);

        System.out.printf("Benchmarking stream file dataset: %s\r\n", 
                deviceDataFile);
        runBenchmark(
                StreamFactory.create(deviceDataFile, false),
                userAgentFile,
                numberOfThreads);
       
        System.out.printf("Benchmarking memory dataset: %s\r\n", 
                deviceDataFile);
        runBenchmark(
                MemoryFactory.create(deviceDataFile),
                userAgentFile,
                numberOfThreads);
    }
        
    /**
     * Instantiates this class and starts 
     * {@link #runBenchmarks(String, String, int)} with parameters from the 
     * command line.
     * 
     * @param args command line arguments.
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) 
            throws IOException, InterruptedException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Must provide two data files.");
        }
        if (new File(args[0]).exists() == false) {
            throw new IllegalArgumentException(String.format(
                    "File '%s' does not exist.",
                    args[0]));
        }
        if (new File(args[1]).exists() == false) {
            throw new IllegalArgumentException(String.format(
                    "File '%s' does not exist.",
                    args[1]));
        }
        
        // execute the benchmarks now the data has been gathered
        runBenchmarks(args[0], args[1], 
                args.length >= 3 ? 
                Integer.parseInt(args[2]) : defaultNumberOfThreads);
    }
}
