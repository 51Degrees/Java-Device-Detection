/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <!-- tutorial -->
 * Example of using 51Degrees Pattern Detection to output a CSV file containing
 * the properties of all hardware devices in the data set being used.
 * <p>
 * The example illustrates:
 * <ol>
 *  <li>Loading a Provider from a Disk-based (Stream) Pattern Dataset
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      provider = new Provider(StreamFactory.create
 *      (Shared.getLitePatternV32(), false));
 *  </code>
 *  </pre>
 *  <li>Getting all hardware profiles as an array
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      Component hardwareComponent =
 *          provider.dataSet.getComponent("HardwarePlatform");
 *      Profile[] hardwareProfiles = hardwareComponent.getProfiles();
 *  </code>
 * </pre>
 * 
 *  <li>Getting the values for some properties of the device
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      Values isMobile = profile.getValues("IsMobile");
 *  </code>
 *  </pre>
 *  <p>
 *  A property may have multiple values. Helper methods convert the list of 
 *  values into a Boolean, Double etc.
 *  For example: 
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      isMobile.toBool();
 *  </code>
 *  </pre>
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
public class AllProfiles implements Closeable {
    // Snippet Start
    // Output file in current working directory.
    public String outputFilePath = "allProfilesOutput.csv";
    // Device detection provider which takes User-Agents and returns matches.
    protected final Provider provider;
    // The profile component relating to all hardware profiles.
    protected final Component hardwareComponent;
    // Array of hardware profiles for all devices.
    protected static Profile[] hardwareProfiles;
    // Array of properties that relate to device hardware.
    protected static Property[] hardwareProperties;

    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @throws IOException if there was a problem reading from the data file.
     */
    public AllProfiles() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), false));
        
        // Get the hardware component, this contains all hardware profiles.
        // There are also SoftwarePlatform, BrowserPlatform and Crawler
        // platforms.
        hardwareComponent = provider.dataSet.getComponent("HardwarePlatform");

        // Get all the hardware properties from the hardware component.
        hardwareProperties = hardwareComponent.getProperties();
        
        // Get all the hardware profiles from the hardware component.
        hardwareProfiles = hardwareComponent.getProfiles();
    }    
    
        /**
     * Writes a CSV file with all properties of all hardware devices in the
     * data set being used.
     * For a full list of properties and the files they are available in please 
     * see: <a href="https://51degrees.com/resources/property-dictionary">
     * Property Dictionary</a>
     * 
     * @param outputFilename where to save the file with extra entries.
     * @throws IOException if there was a problem reading from the data file.
     */
        public void run(String outputFilename)
            throws IOException {
            FileWriter fileWriter = new FileWriter(outputFilename);
            try {
                // Write the headers for the CSV file.
                fileWriter.append("Id");
                for (Property property : hardwareProperties) {
                    fileWriter.append(",").append(property.getName());
                }
                fileWriter.append("\n");
                
                // Loop over all devices.
                for (Profile profile : hardwareProfiles) {
                    // Write the device's profile id.
                    fileWriter.append(Integer.toString(profile.profileId));
                    for (Property property : hardwareProperties) {
                        // Get some property values from the match
                        Values values = profile.getValues(property.getName());
                        // Prevents big chunks of javascript overrides from
                        // being writen.
                        if (property.getName().startsWith("Javascript")) {
                            values = null;
                        }
                        // Write result to file
                        fileWriter.append(",").append(getValueForDisplay(values));
                    }
                    fileWriter.append("\n").flush();
                }
            } finally {
                fileWriter.close();
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
     * Instantiates this class and starts 
     * {@link #run(java.lang.String)} with default 
     * parameters.
     * 
     * @param args command line arguments.
     * @throws IOException if there was a problem accessing the data file.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting All Profiles Example");
        AllProfiles allProfiles = new AllProfiles();
        try {
            allProfiles.run(allProfiles.outputFilePath);
            System.out.println("Output written to " + 
                    allProfiles.outputFilePath);
        } finally {
            allProfiles.close();
        }
    }

    /**
     * Closes the {@link fiftyone.mobile.detection.Dataset} by releasing data
     * file readers and freeing the data file from locks. This method should
     * only be used when the {@code Dataset} is no longer required, i.e. when
     * device detection functionality is no longer required, or the data file
     * needs to be freed.
     *
     * @throws IOException if there is a problem accessing the data file.
     */
    @Override
    public void close() throws IOException {
        provider.dataSet.close();
    }
    // Snippet End
}
