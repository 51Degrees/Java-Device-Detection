/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Demonstration of 51Degrees detection, with benchmarking outputs.
 * <dl>
 * <dt>Prerequisites</dt>
 * <dd>useragents : A file containing User Agent strings to detect, one per line</dd>
 * <dd>detection : A detection dataset file</dd>
 * </dl>
 * <p>
 * An example test file and "Lite" detection file can be found in the <code>data/</code> directory in the root of this repo.
 * By default this example will try to find files <code>../data/20000 User Agents.csv</code> and
 * <code>../data/51Degrees-LiteV3.2.dat</code> relative to its working directory.
 * <p>
 * By default this example carries out 6 iterations of detecting the user agents in the supplied file. The first
 * iteration serves to warm up the JVM and will populate the 51 degrees data structures and cache if one is specified.
 * Subsequent iterations provide a better indication of actual detection performance in live operation.
 * <p>
 * A results file is generated on the last iteration. This records the detailed detection results of each
 * User Agent detected and by default is placed in the working directory.
 * <p>
 * For the Lite dataset a heap of 500 MBytes is recommended, for the Enterprise dataset 1GByte - i.e. run with -Xmx500m
 * or -Xmx1g as appropriate.
 * <p>
 * To compile, from the project root directory execute <code>mvn clean install</code> (or
 * equivalent in your IDE).
 * <p>
 * To run this example from the command line, first compile the source (above) switch to the
 * device-detection-examples directory, then <code>mvn exec:java@batch</code>. Maven 3.3 is required.
 * This will run the example in the same process as maven and hence shares its heap etc. If you
 * want to add command line options then e.g. <code>mvn exec:java@batch -Dexec.args="--cache=0 --iterations=10"</code>
 * <p>
 * In the following command line options can be abbreviated e.g. --cache=1000000 is the same as -c1000000
 * <p>
 * <pre>
   Option                  Description
   ------                  -----------
 --useragents &lt;File>       file path of useragents file (default: ../data/20000 User Agents.csv)
 --detection &lt;File>        file path of detection file (default: ../data/51Degrees-LiteV3.2.dat)
 --results &lt;File>          file path for results file (default: results-&lt;iso date time of test>.txt)
 --cache &lt;Integer>         size of detection cache in bytes (default: 50000)
 --iterations &lt;Integer>    number of times to do the detection (default: 6)                        
 --limit &lt;Integer>         max lines to read from source file (default: Integer.MAX_VALUE)
 --mode &lt;Controller$Mode>  memory or stream mode processing (default: memory)                   
 --preinit &lt;Boolean>       warm up the detection engine on load if mode = memory (default: false)
 --sleep &lt;Integer>         time to sleep between iterations in msec (default: 1000)
 --threads &lt;Integer>       number of threads to use when doing the detections (default: 2)         
 --wait &lt;Integer>          time to wait after initialization and before starting the tests in msec (default: 0)
                              increase if you want e.g. to run JConsole after starting the process
 </pre>
 *
 */
public class Controller {

    private int limit;  // maximum number of UAs to process
    private int numberOfThreads;  // the number of threads to use for processing
    private int iterations; // number of times to repeat the test suite
    private int wait;  // number of millis to wait after intialisation before starting, you can use this time to start e.g. JConsole
    private int sleep;  // number of millis to sleep between iterations
    private boolean initialise; // if mode is memory, whether to pre initialise all data structures
    private int cache; // how many bytes of cache to use
    private Mode mode; // memory mode or stream mode (see TODO description)
    private String testFileName; // the file containing the User-Agent strings to test, one per line, unquoted, etc.
    private String detectionFileName; // the file containing the 51Degrees detection data
    private File resultFile; // a file to store the output in

    // local enum describing which mode to do things in
    public enum Mode {memory, @SuppressWarnings("unused")stream}

    // ISO 8601 date formatter for the name of the default output file (this will appear in local time)
    // with - for : as appropriate
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

    /**
     *
     * @param args Command line args as explained above
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        OptionSpec<File> testFilenameOption = parser.accepts("useragents", "file path of useragents file").withRequiredArg().ofType( File.class ).defaultsTo(new File("../data/20000 User Agents.csv"));
        OptionSpec<File> detectionFilenameOption = parser.accepts("detection", "file path of detection file").withRequiredArg().ofType( File.class ).defaultsTo(new File("../data/51Degrees-LiteV3.2.dat"));
        OptionSpec<File> resultFileOption = parser.accepts("results", "file path for results file").withRequiredArg().ofType( File.class ).defaultsTo(new File("results-" + formatter.format(new Date()) + ".txt"));
        OptionSpec<Mode> modeOption = parser.accepts("mode", "memory or stream mode processing").withRequiredArg().ofType(Mode.class).defaultsTo(Mode.memory);
        OptionSpec<Integer> cacheOption = parser.accepts("cache", "size of detection cache in bytes").withRequiredArg().ofType( Integer.class ).defaultsTo(50000);
        OptionSpec<Integer> threadsOption = parser.accepts("threads", "number of threads to use when doing the detections").withRequiredArg().ofType( Integer.class ).defaultsTo(2);
        OptionSpec<Boolean> preInitOption = parser.accepts("preinit", "warm up the detection engine on load").withRequiredArg().ofType( Boolean.class ).defaultsTo(false);
        OptionSpec<Integer> limitOption = parser.accepts("limit", "max lines to read from source file").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);
        OptionSpec<Integer> iterationOption = parser.accepts("iterations", "number of times to do the detection").withRequiredArg().ofType( Integer.class ).defaultsTo(6);
        OptionSpec<Integer> sleepOption = parser.accepts("sleep", "time to sleep between iterations in msec").withRequiredArg().ofType( Integer.class ).defaultsTo(1000);
        OptionSpec<Integer> waitOption = parser.accepts("wait", "time to wait after initialization and before starting the tests in msec").withRequiredArg().ofType( Integer.class ).defaultsTo(0);

        try {
            OptionSet options = parser.parse(args);

            Controller controller = new Controller();
            controller.limit = limitOption.value(options);
            controller.iterations = iterationOption.value(options);
            controller.numberOfThreads = threadsOption.value(options);
            controller.initialise = preInitOption.value(options);
            controller.cache = cacheOption.value(options);
            controller.wait = waitOption.value(options);
            controller.sleep = sleepOption.value(options);
            controller.mode = modeOption.value(options);
            controller.resultFile = resultFileOption.value(options);

            // test creation of output file
            //noinspection ResultOfMethodCallIgnored
            controller.resultFile.createNewFile();

            // try to find the test file
            try {
                File f = testFilenameOption.value(options);
                assert f.exists();
                controller.testFileName = f.getAbsolutePath();
            } catch (Exception e) {
                throw new Exception(String.format("Test file %s does not exist%n", testFilenameOption.value(options)));
            }

            // try to find the detection file
            try {
                File f = detectionFilenameOption.value(options);
                assert f.exists();
                controller.detectionFileName = f.getAbsolutePath();
            } catch (Exception e) {
                throw new Exception(String.format("Detection file %s does not exist%n", detectionFilenameOption.value(options)));
            }


            System.out.println("Running with options: " + options.asMap());

            controller.process();

        } catch (Exception e) {
            System.err.println(e.getClass() + ": " + e.getMessage());
            parser.printHelpOn(System.out);
            System.exit(1);
        }
    }

    private void process() throws Exception {

        System.out.print("Initialising ...\r");
        long start = System.currentTimeMillis();

        Dataset dataSet;
        if (mode == Mode.memory) {
            // todo has no stats
            dataSet = MemoryFactory.create(detectionFileName, initialise);
            // TODO Fails with NPE
            //MemoryFactory.create(detectionFileName, true);
        } else {
            dataSet = StreamFactory.create(detectionFileName, false);
        }

        Provider provider = new Provider(dataSet, cache);

        System.out.printf("Initialised in %,d millis%n", System.currentTimeMillis() - start);

        System.out.printf("Waiting %,d millis ...\r", wait);
        Thread.sleep(wait);

        try {
            for (int i=0; i < iterations; i++) {
                System.out.printf("Sleeping %,d millis ...\r", sleep);
                Thread.sleep(sleep);

                BufferedReader useragents = new BufferedReader(new InputStreamReader(new FileInputStream(testFileName), "UTF-8"));
                try {
                    System.out.printf("            %n%n+++ Run %d +++%n%n", i);

                    UaProcessor process = new FutureUaProcessor(useragents, provider, numberOfThreads, limit);

                    System.out.print("Running ...\r");
                    process.process();

                    process.printStats(new PrintWriter(System.out));

                    // write results only on the last iteration
                    if (i == iterations - 1) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
                        try {
                            process.writeResults(writer);
                        } finally {
                            writer.close();
                        }
                    }

                } finally {
                    useragents.close();
                }
            }

        } catch (InterruptedException ignored) {
        } finally {
            // TODO provider should have a close method
            provider.dataSet.close();
        }

        System.exit(0);
    }
}
