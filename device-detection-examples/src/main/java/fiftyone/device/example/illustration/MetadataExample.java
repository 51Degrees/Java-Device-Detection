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
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.factories.MemoryFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Example of listing properties and possible values from a Dataset
 * <p>
 * The example illustrates:
 * <ol>
 *     <li>Loading a memory-resident Pattern Dataset</li>
 *     <p><code>dataset = MemoryFactory.create(Shared.getLitePatternV32(), true);</code></p>
 *     <li>Enumerating properties contained in the dataset loaded</li>
 *     <p><code>for(Property property : dataset.getProperties()) {</code></p>
 *     <li>Enumerating values that the property may have</li>
 *     <p><code>for (Value value : property.getValues().getAll()) {</code></p>
 * </ol>
 * The <a href="https://51degrees.com/resources/property-dictionary">51 Degrees Property Dictionary</a>
 * contains a description of each of the properties and the editions in which they are available.
 * <p>
 * <p>
 * {@link #main} assumes it is being run with a working directory at root of project or of this module.
 */
public class MetadataExample implements Closeable {

    private final Dataset dataset;

    public MetadataExample() throws IOException {
        dataset = MemoryFactory.create(Shared.getLitePatternV32(), true);
    }

    public void listProperties () throws IOException {
        // iterate over all properties in the dataset
        for(Property property : dataset.getProperties()) {
            //Get individual property, print name and description.
            System.out.format("%s - %s%n", property.getName(), property.getDescription());

            // collects name, values and their descriptions
            StringBuilder propertyOutput = new StringBuilder();

            // loop over all values for this property and list
            // add a description for this property value if there is one
            for (Value value : property.getValues().getAll()) {
                // add name of property
                propertyOutput.append("\t")
                        .append(truncateToNl(value.getName()));
                // add description if exists
                if (value.getDescription() != null) {
                    propertyOutput.append(" - ")
                            .append(value.getDescription());
                }
                propertyOutput.append("\n");
            }
            System.out.println(propertyOutput);
            propertyOutput.setLength(0);
        }
    }

    // truncate value if it contains newline (esp for the JavaScript property)
    private String truncateToNl(String s) {
        int i = s.indexOf('\n', 3);
        if (i == -1) {
            return s;
        }
        return s.substring(0, i+2) + " ...";
    }

    /**
     * Don't forget to close datasets when you are done with them
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        dataset.close();
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Starting Offline Processing Example");
        System.out.print("Loading dataset ...\r");
        MetadataExample example = new MetadataExample();
        try {
            example.listProperties();
        } finally {
            // close example so that dataset gets closed
            example.close();
        }
    }
}
