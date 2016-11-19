package fiftyone.device.example.illustration;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class Benchmark implements Closeable {

    public static class Result {
        
        // the time benchmarking started.
        Date start = new Date();
        
        // number of User-Agents processed to determine the result.
        int count;
        
        // used to ensure compiler optimiser doesn't optimise out the very
        // method that the benchmark is testing.
        int checkSum;
        
        // the average time in milliseconds for a single detection.
        public double getAverageDetectionTime() {
            return getTotalDetectionTime() / count;
        }
        
        // the total time taken for the benchmark.
        public double getTotalDetectionTime() {
            return (double)(new Date().getTime() - start.getTime());
        }
        
        // the number of User-Agents included in the test.
        public int getCount() {
            return count;
        }
    }

    // pattern detection matching provider
    private final Provider provider;
    
    // path to the User-Agent data file.
    private final String userAgentFile;

    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @param deviceDataFile
     * @param userAgentFile
     * @throws IOException if there was a problem reading from the data file.
     */
    public Benchmark(String deviceDataFile, String userAgentFile) 
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
        provider = new Provider(StreamFactory.create(
                deviceDataFile, false));
        this.userAgentFile = userAgentFile;
    }

    /**
     * Reads a CSV file containing User-Agents performing detection on each
     * one using the provider. 
     * 
     * @throws IOException if there was a problem reading from the data file.
     * @return results from the benchmark exercise.
     */
    public Result run() 
            throws IOException {
        String userAgentString;
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(this.userAgentFile));
        Property isMobile = provider.dataSet.get("IsMobile");
        Result result = new Result();
        try {
            Match match = provider.createMatch();
            while ((userAgentString = bufferedReader.readLine()) != null) {
                provider.match(userAgentString, match);
                result.checkSum ^= match.getValues(isMobile).toString().hashCode();
                result.count++;
                if (result.count % 50000 == 0) {
                    System.out.println("===========================");
                    System.out.printf(
                        "Count: %d \r\n", 
                        result.count);
                    System.out.printf(
                        "getAverageDetectionTime: %f \r\n", 
                        result.getAverageDetectionTime());                    
                }
            }
        }
        finally {
            bufferedReader.close();
        }
        return result;
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
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Must provide two data files.");
        }
        System.out.println("Starting Bench Marking Example");
        Benchmark bm = new Benchmark(args[0], args[1]);
        try {
            Result result = bm.run();
            System.out.println("===========================");
            System.out.printf(
                "Average detections per second: %f \r\n", 
                result.getAverageDetectionTime());
            System.out.printf(
                "User-Agents processed: %d \r\n", 
                result.count);
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
        }
        finally {
            bm.close();
        }
    }
}
