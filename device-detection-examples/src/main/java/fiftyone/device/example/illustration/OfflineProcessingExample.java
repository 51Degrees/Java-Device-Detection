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

package fiftyone.device.example.illustration;

import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.*;

/**
 * <!-- tutorial -->
 * Example of using 51Degrees Pattern Detection to process a file containing 
 * User-Agent header values and output a CSV file containing the same header 
 * values with various properties detected by 51Degrees.
 * <p>
 * The example illustrates:
 * <ol>
 *  <li>Loading a Provider from a Disk-based (Stream) Pattern Dataset
 *  <code><pre class="prettyprint lang-java">
 *      provider = new Provider(StreamFactory.create
 *      (Shared.getLitePatternV32(), false));
 *  </pre></code>
 *  <li>Matching a User-Agent header value
 *  <ol>
 *      <li>By creating a match and using it repeatedly (for efficiency)
 *      <code><pre class="prettyprint lang-java">
 *          Match match = provider.createMatch();
 *      </pre></code>
 *      <code><pre class="prettyprint lang-java">
 *          provider.match(userAgentString, match);
 *      </pre></code>
 *      <li>By having the provider create a new Match for each detection
 *      <code><pre class="prettyprint lang-java">
 *          Match match = provider.match(userAgentString);
 *      </pre></code>
 *  </ol>
 *  <li>Getting the values for some properties of the matched User-Agent header
 *  <code><pre class="prettyprint lang-java">
 *      Values isMobile = match.getValues("IsMobile");
 *  </pre></code>
 *  <p>
 *  A property may have multiple values. Helper methods convert the list of 
 *  values into a Boolean, Double etc.
 *  For example: 
 *  <code><pre class="prettyprint lang-java">
 *      isMobile.toBool();
 *  </pre></code>
 * </ol>
 * You can run {@link MetadataExample#main} for a listing of which properties 
 * are available in the dataset supplied with this distribution.
 * <!-- tutorial -->
 * <p>
 * The <a href="https://51degrees.com/resources/property-dictionary">
 * 51 Degrees Property Dictionary</a> contains a description of each of the 
 * properties and the editions in which they are available.
 * <p>
 * main assumes it is being run with a working directory at root of 
 * project or of this module.
 */
public class OfflineProcessingExample implements Closeable {
    // Snippet Start
    // output file in current working directory
    public String outputFilePath = "batch-processing-example-results.csv";
    // pattern detection matching provider
    private final Provider provider;

    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @throws IOException if there was a problem reading from the data file.
     */
    public OfflineProcessingExample() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), false));
     }

    /**
     * Reads a CSV file containing User-Agents and adds the IsMobile, 
     * PlatformName and PlatformVersion information for the first 20 lines.
     * For a full list of properties and the files they are available in please 
     * see: <a href="https://51degrees.com/resources/property-dictionary">
     * Property Dictionary</a>
     * 
     * @param inputFileName the CSV file to read from.
     * @param outputFilename where to save the file with extra entries.
     * @throws IOException if there was a problem reading from the data file.
     */
    public void processCsv(String inputFileName, String outputFilename) 
            throws IOException {
        BufferedReader bufferedReader = 
                new BufferedReader(new FileReader(inputFileName));
        try {
            FileWriter fileWriter = new FileWriter(outputFilename);
            try {
                // it's more efficient over the long haul to create a match 
                // once and reuse it in multiple matches
                Match match = provider.createMatch();
                // there are 20k lines in supplied file, we'll just do a couple 
                // of them!
                for (int i = 0; i < 20; i++) {

                    // read next line
                    String userAgentString = bufferedReader.readLine();

                    // ask the provider to match the UA using match we created
                    provider.match(userAgentString, match);

                    // get some property values from the match
                    Values isMobile = match.getValues("IsMobile");
                    Values platformName = match.getValues("PlatformName");
                    Values platformVersion = match.getValues("PlatformVersion");

                    // write result to file
                    fileWriter.append("\"")
                            .append(userAgentString)
                            .append("\", ")
                            .append(getValueForDisplay(isMobile))
                            .append(", ")
                            .append(getValueForDisplay(platformName))
                            .append(", ")
                            .append(getValueForDisplay(platformVersion))
                            .append('\n')
                            .flush();
                }
            } finally {
                fileWriter.close();
            }
        } finally {
            bufferedReader.close();
        }
    }

    /**
     * Match values may be null. A helper method to get something displayable
     * @param values a Values to render
     * @return a non-null String
     */
    protected String getValueForDisplay(Values values) {
        return values == null ? "N/A": values.toString();
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
        System.out.println("Starting Offline Processing Example");
        OfflineProcessingExample offlineProcessingExample = 
                new OfflineProcessingExample();
        try {
            offlineProcessingExample.processCsv(Shared.getGoodUserAgentsFile(), 
                    offlineProcessingExample.outputFilePath);
            System.out.println("Output written to " + 
                    offlineProcessingExample.outputFilePath);
        } finally {
            offlineProcessingExample.close();
        }
    }
    // Snippet End
}
