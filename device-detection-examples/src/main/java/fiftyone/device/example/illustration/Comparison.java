/*            
Permission is hereby granted subject to the rights of third parties, to any 
person obtaining a copy of the software in this file (the "Software"), to deal 
in the Software without restriction, including without limitation the rights to 
use the Software, and to permit persons to whom the Software is furnished to do 
so, subject to the following conditions:

The above notice and this qualified permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

By using and/or deploying this software or assisting others to do so, you do not
do so in reliance on any statement made by third parties or us except as 
expressly provided expressly herein. We make no statement and give no warranty 
that use and/or deploy of the software will not infringe any other intellectual 
property or other rights of any other person. You are solely responsible for use
of the Software and indemnify those through whom you acquire the Software on 
demand against all claims, losses, damages and legal expenses arising from your 
use of the Software whether or not in tort, negligence, breach of duty or 
otherwise howsoever. Governed by English law and English courts have exclusive 
jurisdiction for disputes arising or connected to this licence, as well as 
non-contractual claims.
*/

package fiftyone.device.example.illustration;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.cache.IValueLoader;
import fiftyone.mobile.detection.cache.LruCache;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

/**
 * <!-- tutorial -->
 * <p>
 * Compares multiple device detection methods for accuracy and performance 
 * outputting a single CSV file where each row contains the results from one
 * or more solutions for each of the target User-Agents provided. 
 *
 * <h3>Expected Results</h3>
 * <table class="dnnGrid" border="1">
 *     <tr class="dnnGridHeader">
 * 	     <th>Provider</th>
 * 	     <td>Browscap</td>
 * 	     <td>51Degrees</td>
 *       </tr>
 *     <tr class="dnnGridItem">
 * 	     <th>Time to initialise provider (ms)</th>
 * 	     <td>7,571.2</td>
 * 	     <td>311.4</td>
 *     </tr>
 *     <tr class="dnnGridAltItem">
 * 	     <th>Average time per detection per thread (ms)</th>
 * 	     <td>0.0137752</td>
 * 	     <td>0.0108086</td>
 *     </tr>
 *   <caption>
 *        The comparison was performed using a single thread on a Lenovo G710
 *        Laptop with a Quad Core 2.2GHz CPU and 8GB of main memory.
 *        Input data: 51Degrees-Enterprise device data and a sample of
 *        <a href="https://raw.githubusercontent.com/51Degrees/Java-Device-Detection/master/data/20000%20User%20Agents.csv">
 *        20,000 User-Agents</a>
 *   </caption>
 * </table>
 *
 * <p>
 * <a href="https://browscap.org/">Browscap</a>, or Browser Capabilities project
 * , is an open source project which maintains and offers free downloads of a
 * browscap.ini file, a browser capabilities database. It is a list of all known
 * browsers and bots, along with their default capabilities and limitations.
 *
 * <p>
 * This example implements the Blueconic java library which is available on
 * <a href="https://github.com/blueconic/browscap-java">GitHub</a> and
 * <a href="https://mvnrepository.com/artifact/com.blueconic/browscap-java">Maven</a>.
 *
 * <p>
 * Note: the implementations for WURFL and DeviceAtlas has not been tested
 * by the original author as 51Degrees do not have access to the associated
 * source code or data files. They have been generated theoretically from the 
 * associated API documentation provided but are intended to be easy to modify. 
 * The code marked "UNCOMMENT" will need to be uncommented and the associated
 * packages and data files obtained from ScientiaMobile and / or DeviceAtlas
 * to enable the associated providers.
 *
 * <!-- tutorial -->
 * 
 */
public class Comparison {
    
    /**
     * Common interface supported by each of the solution vendors.
     */
    interface ComparisonProvider extends Closeable {
        
        /**
         * Populates the result with data found from the User-Agent.
         * 
         * @param userAgent target User-Agent
         * @param result to be populated
         * @throws Exception 
         */
        void calculateResult(String userAgent, Result result) throws Exception;
    }

    static class ResultIterator implements Iterator<Result> {

        private final LinkedList<LinkedList<Result>> queue; 
        
        private final float total;
        
        private float fetches;
        
        ResultIterator(LinkedList<Request> requests) {
            long localTotal = 0;
            SortedMap<Float, LinkedList<Result>> map = 
                    new TreeMap<Float, LinkedList<Result>>();
            
            // calculate the total number of detections to perform
            for(Request request : requests) {
                localTotal += request.frequency;
            }
            this.total = localTotal;
            
            // build the map of results
            for(Request request : requests) {
                float weight = (float)request.frequency / (float)localTotal;
                Result result = new Result(request);
                request.results.add(result);
                if (!map.containsKey(weight)) {
                    map.put(weight, new LinkedList<Result>());
                }
                map.get(weight).add(result);
            }
            
            // set the queue 
            this.queue = new LinkedList<LinkedList<Result>>();
            for(Entry<Float, LinkedList<Result>> entry : map.entrySet()) {
                queue.add(entry.getValue());
            }
        }
        
        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Result next() {
            LinkedList<Result> current = queue.remove();
            Result result = current.remove();
            result.count++;
            fetches++;
            if (result.count < result.request.frequency) {
                current.addLast(result);
            }
            if (current.size() > 0) {
                queue.addLast(current);
            }
            return result;
        }
        
        float getPercentageComplete() {
            return this.fetches / this.total;
        }
    }
    
    static class Result {
        
        /**
         * The request the result relates to.
         */
        final Request request;
        
        /**
         * Nano seconds spent in device detection.
         */
        final AtomicLong totalDetectionTime = new AtomicLong(0);
                
        /**
         * The average detection time.
         */
        double averageDetectionTimeMs;
        
        /**
         * True if the device is a mobile, otherwise false. Solution vendors
         * may have subtly different meta data associated with the populating
         * value.
         */
        boolean isMobile;
        
        /**
         * The name of the device brand, manufacturer or vendor.
         */
        String hardwareVendor;
        
        /**
         * The model of the device.
         */
        String hardwareModel;
        
        /**
         * Vendor of the browser.
         */
        String browserName;
        
        /**
         * Version of the browser.
         */
        String browserVersion;
                  
        /**
         * Type of the device as reported by the detection provider.
         */
        String deviceType;
        
        /**
         * When available the in the solution the difference between the result
         * found and the target User-Agent. Only 51Degrees supports this value.
         */
        int difference;
        
        /**
         * Number of detections carried out for the related User-Agent.
         */
        int count;
        
        Result(Request request) {
            this.request = request;
        }
        
        void setForWrite() {
            averageDetectionTimeMs = (double)totalDetectionTime.get() / (double)1000000 / (double)count;
        }
    }
    
    /**
     * Includes the requesting User-Agent and a list of results from each
     * of the providers used.
     */
    static class Request {
        
        /**
         * Target User-Agent used for the comparison.
         */
        final String userAgentString;
        
        /**
         * Number of times the User-Agent should be repeated in the comparison.
         */
        final int frequency;
               
        /**
         * Results for each of the providers. Must be in the same order for
         * every request.
         */
        final LinkedList<Result> results;
        
        Request(String userAgentString, int frequency) {
            this.userAgentString = userAgentString;
            this.frequency = frequency;
            results = new LinkedList<Result>();
        }
    }

    // Snippet Start
    
    /**
     * Uncomment the following code blocks to test with WURFL. The code will
     * need modification as the comparison has not been tested with the real
     * WURFL API. The code has been constructed based on the documentation
     * available on the ScientiaMobile web site at the following location.
     * 
     * https://docs.scientiamobile.com/documentation/onsite/onsite-java-api
     */
    static class WurflProvider implements ComparisonProvider {

        /** UNCOMMENT FOR WURFL **/
        // private GeneralWURFLEngine wurfl;
        
        WurflProvider(String dataFile, int cacheSize) {
            /** UNCOMMENT FOR WURFL **/
            /**
            this.wurfl = new GeneralWURFLEngine(dataFile);
            this.wurfl.setEngineTarget(EngineTarget.accuracy);
            
            // More work may be needed to determine how to setup the cache
            // and pass in the cache size value.
            this.wurfl.setCacheProvider(new LRUMapCacheProvider());
            
            // load method is available on API version 1.8.1.0 and above
            wurfl.load();
            **/
        }
        
        @Override
        public void calculateResult(String userAgent, Result result) 
                throws Exception {
            /** UNCOMMENT FOR WURFL **/
            /**
            Device device = this.wurfl.getDeviceForRequest(userAgent);
            result.isMobile = device.getCapabilityAsBool("is_wireless_device");
            result.hardwareVendor = device.getCapability("brand_name");
            result.hardwareModel = device.getCapability("model_name");
            result.deviceType = device.getCapability("form_factor");
            result.difference = -1;
            **/
        }

        @Override
        public void close() throws IOException {
            /** UNCOMMENT FOR WURFL **/
            //this.wurfl = null;
        }
    }

    /**
     * Uncomment the following code blocks to test with DeviceAtlas. The code 
     * will need modification as the comparison has not been tested with the 
     * real DeviceAtlas API. The code has been constructed based on the
     * documentation available on the DeviceAtlas web site at the following
     * location.
     * 
     * https://docs.deviceatlas.com/apis/enterprise/java/2.1.1/README.DeviceApi.html
     */
    static class DeviceAtlasProvider implements ComparisonProvider {
        
        /** UNCOMMENT FOR DEVICE ATLAS **/
        //private DeviceApi da;
        
        DeviceAtlasProvider(String dataFile, int cacheSize) {
            /** UNCOMMENT FOR DEVICE ATLAS **/
            /**
            this.da = new DeviceApi();
            this.da.loadDataFromFile(dataFile);
            **/
             
            // Some configuration of a User-Agent cache may be possible to
            // improve performance in the second pass.
        }
        
        @Override
        public void calculateResult(String userAgent, Result result) 
                throws Exception {
            /** UNCOMMENT FOR DEVICE ATLAS **/
            /**
            Properties properties = this.da.getProperties(userAgent);
            result.isMobile = properties.get("mobileDevice").asBoolean();
            result.hardwareVendor = properties.get("vendor").asString();
            result.hardwareModel = properties.get("model").asString();
            result.deviceType = properties.get("primaryHardwareType").asString();
            result.difference = -1;
            **/
        }
        
        @Override
        public void close() throws IOException {
            /** UNCOMMENT FOR DEVICE ATLAS **/
            //this.da = null;
        }
    }
    
    static abstract class FiftyOneDegreesBaseProvider 
            implements ComparisonProvider {

        private final Dataset dataset;
        private Provider provider;
        private final Property isMobile;
        private final Property hardwareVendor;
        private final Property hardwareModel;
        private final Property browserName;
        private final Property browserVersion;
        private final Property deviceType;
        
        FiftyOneDegreesBaseProvider(Dataset dataset, int cacheSize) 
                throws IOException {
            this.dataset = dataset;
            this.provider = new Provider(dataset, cacheSize);
            isMobile = provider.dataSet.properties.get("IsMobile");
            hardwareVendor = provider.dataSet.properties.get("HardwareVendor");
            hardwareModel = provider.dataSet.properties.get("HardwareModel");
            browserName = provider.dataSet.properties.get("BrowserName");
            browserVersion = provider.dataSet.properties.get("BrowserVersion");
            deviceType = provider.dataSet.properties.get("DeviceType");
              
            System.out.printf("51Degrees '%s %s' published '%s'\r\n",
                dataset.getName(),
                dataset.getFormat(),
                dataset.published.toString());
        }
                
        /**
         *
         * @param userAgent
         * @return
         * @throws IOException
         */
        @Override
        public void calculateResult(String userAgent, Result result) 
                throws IOException {
            Match match = provider.match(userAgent);
            result.isMobile = isMobile != null ? 
                    match.getValues(isMobile).toBool() : false;
            result.browserName = browserName != null ? 
                    match.getValues(browserName).toString() : UNSUPPORTED_VALUE;
            result.browserVersion = browserVersion != null ? 
                    match.getValues(browserVersion).toString() : 
                    UNSUPPORTED_VALUE;
            result.hardwareVendor = hardwareVendor != null ? 
                    match.getValues(hardwareVendor).toString() : 
                    UNSUPPORTED_VALUE;
            result.hardwareModel = hardwareModel != null ? 
                    match.getValues(hardwareModel).toString() : 
                    UNSUPPORTED_VALUE;
            result.deviceType = deviceType != null ? 
                    match.getValues(deviceType).toString() : UNSUPPORTED_VALUE;
            result.difference = match.getDifference();
        }

        @Override
        public void close() throws IOException {
            System.out.printf("%.2f%% cache misses\r\n", 
                    provider.getPercentageCacheMisses() * 100);
            System.out.printf("%d total detections\r\n",
                    provider.getDetectionCount());
            provider = null;
            dataset.close();
        }
    }
    
    /**
     * Loads all the data into initialised data structures. Fast with a longer
     * initialisation time.
     */
    static class FiftyOneDegreesMemoryProvider 
            extends FiftyOneDegreesBaseProvider {
    
        FiftyOneDegreesMemoryProvider(String dataFile, int cacheSize) 
                throws IOException {
            super(MemoryFactory.create(dataFile, true), cacheSize);
            System.out.println(
                    "Created 51Degrees memory provider");
        }
    }
    
    /**
     * Loads the data into a byte array and then initialises portions when 
     * needed. Still fast and a short initialisation time. Optimum balance for
     * many use cases.
     */    
    static class FiftyOneDegreesFileProvider 
            extends FiftyOneDegreesBaseProvider {
        
        FiftyOneDegreesFileProvider(String dataFile, int cacheSize) 
                throws IOException {
            super(DatasetBuilder.buffer()
                    .configureDefaultCaches()
                    .build(fileAsBytes(dataFile)), 
                    cacheSize);
            System.out.println(
                    "Created 51Degrees indirect / file / stream provider");
        }
        
        private static byte[] fileAsBytes(String deviceDataFile) 
                throws IOException {
            File file = new File(deviceDataFile);
            byte fileContent[] = new byte[(int) file.length()];
            FileInputStream fin = new FileInputStream(file);
            try {
                int bytesRead = fin.read(fileContent);
                if (bytesRead != file.length()) {
                    throw new IllegalStateException("File not completely read");
                }
            } finally {
                fin.close();
            }
            return fileContent;
        }
    }

    /**
     * Uncomment the following code blocks to test with 51Degrees Hash Trie.
     * The FiftyOneDegreesTrieV3 jar will need building and adding as a dependency
     * to this project. This can be found at:
     * https://github.com/51Degrees/Device-Detection
     */
    static class FiftyOneDegreesHashTrieProvider implements ComparisonProvider {
        /** UNCOMMENT FOR 51D HASH TRIE **/
        // private FiftyOneDegreesTrieV3.Provider provider;
        private ArrayList<String> availableProperties = new ArrayList<String>();
        String isMobile, hardwareVendor, hardwareModel, browserName,
                browserVersion, deviceType;

        FiftyOneDegreesHashTrieProvider(String dataFile) {
            /** UNCOMMENT FOR 51D HASH TRIE **/
            /**
            try {
                FiftyOneDegreesTrieV3.LibLoader.load("/FiftyOneDegreesTrieV3.dll");
            } catch (IOException e) {
                System.out.printf("Failed to load FiftyOneDegreesTrieV3.dll");
                e.printStackTrace();
            }
            provider = new FiftyOneDegreesTrieV3.Provider(
                    dataFile,
                    "IsMobile,BrowserName,BrowserVersion,HardwareName," +
                            "HardwareVendor,HardwareModel,DeviceType"
            );
            FiftyOneDegreesTrieV3.VectorString propertiesVector =  provider.getAvailableProperties();
            for (int i = 0; i < propertiesVector.size(); i++) {
                availableProperties.add(propertiesVector.get(i));
            }
            isMobile = availableProperties.contains("IsMobile") ?
                    "IsMobile" : null;
            hardwareVendor = availableProperties.contains("HardwareVendor") ?
                    "HardwareVendor" : null;
            hardwareModel = availableProperties.contains("HardwareModel") ?
                    "HarwareModel" : null;
            browserName = availableProperties.contains("BrowserName") ?
                    "BrowserName" : null;
            browserVersion = availableProperties.contains("BrowserVersion") ?
                    "BrowserVersion" : null;
            deviceType = availableProperties.contains("DeviceType") ?
                    "DeviceType" : null;
            **/
        }

        @Override
        public void calculateResult(String userAgent, Result result) throws Exception {
            /** UNCOMMENT FOR 51D HASH TRIE **/
            /**
            FiftyOneDegreesTrieV3.Match match = provider.getMatch(userAgent);

            result.isMobile = isMobile != null && match.getValue(isMobile) == "True" ?
                    true : false;
            result.browserName = browserName != null ?
                    match.getValue(browserName) : UNSUPPORTED_VALUE;
            result.browserVersion = browserVersion != null ?
                    match.getValue(browserVersion) :
                    UNSUPPORTED_VALUE;
            result.hardwareVendor = hardwareVendor != null ?
                    match.getValue(hardwareVendor) :
                    UNSUPPORTED_VALUE;
            result.hardwareModel = hardwareModel != null ?
                    match.getValue(hardwareModel) :
                    UNSUPPORTED_VALUE;
            result.deviceType = deviceType != null ?
                    match.getValue(deviceType) : UNSUPPORTED_VALUE;
            result.difference = -1;
             **/
        }

        @Override
        public void close() throws IOException {
            /** UNCOMMENT FOR 51D HASH TRIE **/
            // provider.delete();
        }
    }
    
    /**
     * An MIT licence implementation of BrowsCap project.
     * 
     * https://github.com/blueconic/browscap-java
     */
    static class BrowsCapProvider implements 
            ComparisonProvider, 
            IValueLoader<String, Capabilities> {

        private LruCache<String, Capabilities> cache;
        private UserAgentParser browscap;
        
        BrowsCapProvider(int cacheSize) throws IOException, ParseException {
            this.cache = new LruCache<String, Capabilities>(cacheSize, this);
            this.browscap = new UserAgentService().loadParser();
        }
        
        @Override
        public void calculateResult(String userAgent, Result result) 
                throws Exception {
            Capabilities capabilities = this.cache.get(userAgent);
            String deviceType = capabilities.getDeviceType();
            result.isMobile = deviceType.equals("Mobile Phone") ||
                              deviceType.equals("Tablet");
            result.browserName = capabilities.getBrowser();
            result.browserVersion = capabilities.getBrowserMajorVersion();
            result.deviceType = capabilities.getDeviceType();
            result.hardwareVendor = UNSUPPORTED_VALUE;
            result.hardwareModel = UNSUPPORTED_VALUE;
            result.difference = -1;
        }

        @Override
        public void close() throws IOException {
            this.cache = null;
            this.browscap = null;
        }

        @Override
        public Capabilities load(String userAgent) throws IOException {
            return this.browscap.parse(userAgent);
        }
    }

    /**
     * Encapsulates the logic that is executed in each thread of the comparison.
     */
    private static class ComparisonRunnable implements Runnable {
        
        // the comparison that is being executed
        private final Comparison cp;
        
        ComparisonRunnable(
                Comparison cp)
                throws IOException {
            this.cp = cp;
        }
        
        @Override
        public void run() {
            try {
                Object guard = new Object();
                Result result = cp.queue.poll(1, TimeUnit.SECONDS);
                while (result != null || 
                       cp.addingComplete == false) {
                    if (result != null) {
                        
                        // Capture the start time for the deteciton.
                        long start = System.nanoTime();
                        
                        synchronized(guard) {
                            // Perform the detection and record the result.
                            cp.provider.calculateResult(
                                    result.request.userAgentString, 
                                    result);
                        }
                        
                        // Capture the end time for subsequent output.
                        long detectionTime = System.nanoTime() - start;
                        result.totalDetectionTime.addAndGet(detectionTime);
                        
                        // Update the total elapsed time.
                        cp.elapsedNano.addAndGet(detectionTime);
                        
                        // Increase the number of detection performed.
                        cp.count.incrementAndGet();
                    }
                    result = cp.queue.poll(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Benchmark.class.getName()).log(
                        Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Comparison.class.getName()).log(
                        Level.SEVERE, 
                        null, 
                        ex);
            }
        }
    }

    // the default number of threads if one is not provided.
    private static final int defaultNumberOfThreads = 1;
    
    // the maximum size of the queue of User-Agents for comparison.
    private static final int QUEUE_SIZE = 512;
    
    // multiplier used to determine the size of the provider's cache
    private static final float CACHE_MULTIPLIER = (float)0.25;
    
    private static final String UNSUPPORTED_VALUE = "NOT AVAILABLE";
    
    // separator to use with the input data source
    private static final String CSV_INPUT_REGEX = "\\s\\|\\s";
  
    // separator to use in the CSV file.
    private static final char CSV_SEPARATER = ',';
    
    // string quote to use in the CSV file.
    private static final char CSV_QUOTE = '"';
    
    // single quote in a CSV string.
    private static final String CSV_SINGLE_QUOTE = 
            new String(new char[] { CSV_QUOTE });
    
    // double quote to replace a single quote with in a string.
    private static final String CSV_DOUBLE_QUOTE = 
            new String(new char[] { CSV_QUOTE, CSV_QUOTE });
   
    // report progress every X milliseconds
    private static final int PROGRESS_REPORT_INTERVAL = 5000;
    
    // number of User-Agents processed to determine the result.
    private final AtomicInteger count = new AtomicInteger();
    
    // processing time in millseconds from all threads
    private final AtomicLong elapsedNano = new AtomicLong();

    // set to true when the queue has had all elements added to it.
    private boolean addingComplete = false;

    // queue of User-Agent strings for processing.
    private final LinkedBlockingQueue<Result> queue;

    // provider to use for processing.
    private ComparisonProvider provider;
               
    // the average time in milliseconds for a single thread.
    public double getAverageDetectionTimePerThread() {
        return (elapsedNano.doubleValue() / 1000000) / getCount();
    }

    // the number of User-Agents included in the test.
    public int getCount() {
        return count.intValue();
    }    
    
    public Comparison() {
        this.queue = new LinkedBlockingQueue<Result>(QUEUE_SIZE);
    }

    public void run(
            ComparisonProvider provider, 
            LinkedList<Request> requests, 
            int numberOfThreads)
            throws IOException, InterruptedException {
        
        // initialise the comparison variables
        this.provider = provider;
        this.elapsedNano.set(0);
        this.count.set(0);

        // get the User-Agent result iterator
        ResultIterator iterator = new ResultIterator(requests);
        
        System.out.printf(
                "Starting processing %.0f requests from %d User-Agents\r\n", 
                iterator.total,
                requests.size());
        
        // start multiple threads in a fixed pool
        ExecutorService executor = Executors.newFixedThreadPool(
                numberOfThreads);
        for(int i = 0; i < numberOfThreads; i++) {
            executor.execute(new ComparisonRunnable(this));
        }
       
        // add the detections to the queue until exhausted
        long next = System.currentTimeMillis() + PROGRESS_REPORT_INTERVAL;
        while(iterator.hasNext()) {
            this.queue.put(iterator.next());
            if (System.currentTimeMillis() > next) {
                System.out.printf("%.2f%% complete\r\n", 
                        iterator.getPercentageComplete() * 100);
                next += PROGRESS_REPORT_INTERVAL;
            }   
        }
        
        this.addingComplete = true;
        executor.shutdown();
        while (executor.isTerminated() == false) {
            // Do nothing.
        }
       
        // output the results from the comparison to the console
        System.out.printf(
            "Average millseconds per detection per thread: %f \r\n", 
            getAverageDetectionTimePerThread());
        System.out.printf(
            "Concurrent threads: %d \r\n", 
            numberOfThreads);
        System.out.printf(
            "User-Agents processed: %d \r\n", 
            getCount());
    }
    
    private static void runComparison(
            ComparisonProvider provider,
            LinkedList<Request> requests,
            int numberOfThreads) throws IOException, InterruptedException {
        new Comparison().run(provider, requests, numberOfThreads);
    }
       
    private static LinkedList<Request> readUserAgents(String userAgentFile) 
            throws FileNotFoundException, IOException {
        String line;
        LinkedList<Request> requests = new LinkedList<Request>();
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(userAgentFile));
        while ((line = bufferedReader.readLine()) != null) {
            String[] values = line.split(CSV_INPUT_REGEX);
            Request request = new Request(
                    values[0], 
                    values.length > 1 ? Integer.parseInt(values[1]) : 4);
            requests.add(request);
        }
        bufferedReader.close();
        return requests;
    }
    
    private static void writeFirstString(
            BufferedWriter bufferedWriter, 
            String value) 
            throws IOException {
        writeString(bufferedWriter, value, true);
    }
    
    private static void writeString(BufferedWriter bufferedWriter, String value) 
            throws IOException {
        writeString(bufferedWriter, value, false);
    }
    
    private static void writeString(
            BufferedWriter bufferedWriter, 
            String value, 
            boolean isFirst) throws IOException {
        if (isFirst == false) {
            bufferedWriter.write(CSV_SEPARATER);
        }
        bufferedWriter.write(CSV_QUOTE);
        if (value != null) {
            bufferedWriter.write(value.replace(
                    CSV_SINGLE_QUOTE, 
                    CSV_DOUBLE_QUOTE));
        }
        bufferedWriter.write(CSV_QUOTE);
    }
    
    private static void writeBoolean(
            BufferedWriter bufferedWriter,
            boolean value) throws IOException {
        bufferedWriter.write(CSV_SEPARATER);
        bufferedWriter.write(Boolean.toString(value));
    }
    
    private static void writeDouble(
            BufferedWriter bufferedWriter, 
            double value) throws IOException {
        bufferedWriter.write(CSV_SEPARATER);
        bufferedWriter.write(Double.toString(value));
    }
    
    private static void writeInteger(
            BufferedWriter bufferedWriter, 
            int value) throws IOException {
        bufferedWriter.write(CSV_SEPARATER);
        bufferedWriter.write(Integer.toString(value));
    }
    
    private static void writeResult(
            BufferedWriter bufferedWriter,
            Result result) throws IOException {
        try {
            result.setForWrite();
            for(Field field : Result.class.getDeclaredFields()) {
                if (field.getType() == String.class) {
                        writeString(bufferedWriter, (String)field.get(result));
                }
                else if (field.getType() == int.class) {
                    writeInteger(bufferedWriter, field.getInt(result));
                }
                else if (field.getType() == boolean.class) {
                    writeBoolean(bufferedWriter, field.getBoolean(result));
                }
                else if (field.getType() == double.class) {
                    writeDouble(bufferedWriter, field.getDouble(result));
                }
                else if (field.getType() == float.class) {
                    writeDouble(bufferedWriter, field.getDouble(result));
                }                
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Comparison.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Comparison.class.getName()).log(
                    Level.SEVERE,
                    null, 
                    ex);
        }
    }
    
    private static void writeHeaders(
            BufferedWriter bufferedWriter,
            ArrayList<String> providerNames) throws IOException {
        writeFirstString(bufferedWriter, "User-Agent");
        for(String providerName : providerNames) {
            for(Field field : Result.class.getDeclaredFields()) {
                if (field.getType() == String.class ||
                    field.getType() == int.class ||
                    field.getType() == boolean.class ||
                    field.getType() == double.class ||
                    field.getType() == float.class) {
                    writeString(
                            bufferedWriter, 
                            providerName + "-" + field.getName());
                }
            }
        }
        bufferedWriter.newLine();
    }
    
    private static void writeResults(
            LinkedList<Request> requests, 
            ArrayList<String> providerNames,
            String outputFile) throws IOException {
        System.out.printf("Writing comparison CSV file: %s\r\n", 
                outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(outputFile));
        writeHeaders(bufferedWriter, providerNames);
        for (Request request : requests) {
            writeFirstString(bufferedWriter, request.userAgentString);
            for (Result result : request.results) {
                writeResult(bufferedWriter, result);
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }
    
    private static void runFiftyOneDegreesFile(
            ArrayList<String> providerNames,
            LinkedList<Request> requests,
            int cacheSize,
            int numberOfThreads,
            String dataFile) throws IOException, InterruptedException {
        if (dataFile != null && new File(dataFile).exists()) {
            providerNames.add("51D");
            System.out.printf("Processing 51Degrees file: %s\r\n", 
                    dataFile);
            long startTime = System.currentTimeMillis();
            ComparisonProvider fodf = new FiftyOneDegreesFileProvider(
                    dataFile,
                    cacheSize);
            long endTime = System.currentTimeMillis();
            System.out.printf(
                    "Initialised 51Degrees file provider in %d ms\r\n",
                    endTime - startTime);
            try {
                runComparison(fodf, requests, numberOfThreads);
            }
            finally {
                fodf.close();
            }
        }
    }

    private static void runFiftyOneDegreesHashTrie(
            ArrayList<String> providerNames,
            LinkedList<Request> requests,
            int numberOfThreads,
            String dataFile) throws IOException, InterruptedException {
        if (dataFile != null && new File(dataFile).exists()) {
            providerNames.add("51DHashTrie");
            System.out.printf("Processing 51Degrees Hash Trie File: %s\r\n",
                    dataFile);
            long startTime = System.currentTimeMillis();
            ComparisonProvider fodhtf = new FiftyOneDegreesHashTrieProvider(
                    dataFile);
            long endTime = System.currentTimeMillis();
            System.out.printf(
                    "Initialised 51Degrees Hash Trie file provider in %d ms\r\n",
                    endTime - startTime);
            try {
                runComparison(fodhtf, requests, numberOfThreads);
            }
            finally {
                fodhtf.close();
            }
        }
    }
    
    private static void runWurfl(
            ArrayList<String> providerNames,
            LinkedList<Request> requests,
            int cacheSize,
            int numberOfThreads,
            String dataFile) throws IOException, InterruptedException {
        if (dataFile != null && new File(dataFile).exists()) {
            providerNames.add("WURFL");
            System.out.printf("Processing WURFL: %s\r\n", 
                    dataFile);
            long startTime = System.currentTimeMillis();
            ComparisonProvider wurfl = new WurflProvider(
                    dataFile,
                    cacheSize); 
            long endTime = System.currentTimeMillis();
            System.out.printf(
                    "Initialised WURFL in %d ms\r\n",
                    endTime - startTime);
            try {
                runComparison(wurfl, requests, numberOfThreads);
            }
            finally {
                wurfl.close();
            }
        }
    }

    private static void runDeviceAtlas(
        ArrayList<String> providerNames,
        LinkedList<Request> requests,
        int cacheSize,
        int numberOfThreads,
        String dataFile) throws IOException, InterruptedException {
        if (dataFile != null && new File(dataFile).exists()) {
            providerNames.add("DA");
            System.out.printf("Processing DeviceAtlas: %s\r\n", 
                    dataFile);
            long startTime = System.currentTimeMillis();
            ComparisonProvider da = new DeviceAtlasProvider(
                    dataFile,
                    cacheSize);
            long endTime = System.currentTimeMillis();
            System.out.printf(
                    "Initialised DeviceAtlas in %d ms\r\n",
                    endTime - startTime);
            try {
                runComparison(da, requests, numberOfThreads);
            }
            finally {
                da.close();
            }
        }
    }
    
    private static void runBrowserCaps(
            ArrayList<String> providerNames,
            LinkedList<Request> requests, 
            int cacheSize, 
            int numberOfThreads) throws IOException, InterruptedException {
        providerNames.add("BC");
        System.out.println("Processing Browscap");
        ComparisonProvider bc = null;        
        try {
            long startTime = System.currentTimeMillis();
            bc = new BrowsCapProvider(cacheSize);
            long endTime = System.currentTimeMillis();
            System.out.printf(
                "Initialised Browscap provider in %d ms\r\n",
                endTime - startTime);
            runComparison(bc, requests, numberOfThreads);
        } catch (ParseException ex) {
            Logger.getLogger(Comparison.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bc.close();
        }
    }
    
    /**
     * Runs one of more comparison operations writing the results back out
     * to the list of requests.
     * @param userAgentsFile path to csv file containing User-Agents
     * @param fiftyOneDegreesFile path to 51Degrees provider data file
     * @param wurflFile path to WURFL data file
     * @param deviceAtlasFile path to DeviceAtlas data file
     * @param csvOutputFile path to csv output file for storing results of comparison
     * @param numberOfThreads number of concurrent threads
     */    
    private static void runComparisons(
        String userAgentsFile,
        String fiftyOneDegreesFile,
        String fiftyoneDegreesHashTrieFile,
        String wurflFile,
        String deviceAtlasFile,
        String csvOutputFile,
        int numberOfThreads) throws IOException, InterruptedException {
        
        // Check that the User-Agent file exists.
        if (new File(userAgentsFile).exists() == false) {
            throw new IllegalArgumentException(String.format(
                "File %s does not exist",
                userAgentsFile));
        }
        
        // Initialise the data for all the different providers.
        ArrayList<String> providerNames = new ArrayList<String>();
        LinkedList<Request> requests = readUserAgents(userAgentsFile);
        int cacheSize = (int)(requests.size() * CACHE_MULTIPLIER);
             
        // Call each of the providers in turn recording results and provider
        // names where a valid file has been provided.
        runBrowserCaps(
                providerNames,
                requests,
                cacheSize,
                numberOfThreads);
        runFiftyOneDegreesFile(
                providerNames,
                requests, 
                cacheSize, 
                numberOfThreads, 
                fiftyOneDegreesFile);
        runFiftyOneDegreesHashTrie(
                providerNames,
                requests,
                numberOfThreads,
                fiftyoneDegreesHashTrieFile);
        runWurfl(
                providerNames,
                requests,
                cacheSize,
                numberOfThreads,
                wurflFile);
        runDeviceAtlas(
                providerNames,
                requests,
                cacheSize,
                numberOfThreads,
                deviceAtlasFile);
        
        // Write the results to a CSV file.
        writeResults(requests, providerNames, csvOutputFile);
    }
    
    /**
     * Instantiates this class and starts 
     * {@link #runComparisons(String, String, String, String, String, String, int)}
     * with parameters from the command line. When run from the command line
     * the first and last file arguments must be a file of User-Agents and the
     * file that the comparison results will be written to. Files between the
     * first and last file are optional and provide the FiftyOneDegrees, 
     * WURFL and DeviceAtlas data files. A final optional argument of type 
     * integer can be provided to determine the number of concurrent threads 
     * to use during processing if the provider supports multi threaded 
     * operation.
     * 
     * @param args command line arguments.
     */
    public static void main(String[] args) 
            throws IOException, InterruptedException {
        int numberOfThreads = defaultNumberOfThreads;
        int existingFiles = 0;
        
        for(String arg : args) {
            try {
                numberOfThreads = Integer.parseInt(arg);
            }
            catch (NumberFormatException ex) {
                existingFiles++;
            }
        }
        
        if (existingFiles < 2) {
            throw new IllegalArgumentException(
                    "At least 2 valid files need to be provided");
        }
                
        /**
         * Execute the comparison now the data has been gathered.
         *
         * NOTE: Modify the following code to test with Device Atlas,
         * WURFL, and 51Degrees Hash Trie, replace null with the path
         * to the corresponding data file.
        **/
        runComparisons(
                args[0], // The file containing User-Agents
                args[1], // The 51Degrees data file
                null, // The 51Degrees Hash Trie data file
                null, // The WURFL data file
                null, // The DA data file
                args[2], // The output CSV file
                numberOfThreads);
    }
    // Snippet End
}