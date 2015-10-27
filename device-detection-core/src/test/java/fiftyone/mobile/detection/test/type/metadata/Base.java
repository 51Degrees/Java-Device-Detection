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

package fiftyone.mobile.detection.test.type.metadata;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.test.TestType;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeMetadata.class)
public abstract class Base extends DetectionTestSupport {
    
    /**
     * Data set used to perform the tests on.
     */
    protected Dataset dataSet;
    
    /** 
     * The path to the data file to use to create the dataset.
     */
    protected final String dataFile;
    
    public Base(String dataFile) {
        this.dataFile = dataFile;
    }
    
    /**
     * Ensures the data set is disposed of correctly at the end of the test.
     * @throws Exception 
     */
    @After
    public void tearDown() throws Exception {
        disposing(true);
    }
    
    /**
     * Ensures the data set is disposed of correctly.
     * @throws Throwable 
     * @throws java.io.IOException 
     */
    @Override
    protected void finalize() throws Throwable, IOException {
        disposing(false);
        super.finalize();
    }

    /**
     * Ensures resources used by the data set are closed and memory released.
     * @param disposing 
     * @throws java.io.IOException 
     */
    protected void disposing(boolean disposing) throws IOException {
        if (dataSet != null) {
            dataSet.close();
            dataSet = null;
        }
    }
    
    public void retrieveComponents() throws IOException
    {
        for (Component component : dataSet.components)
        {
            System.out.printf("Testing Component '%s'\r\n", component);
            System.out.printf("Default Profile '%s'\r\n", component.getDefaultProfile());
            
            int propertiesChecksum = 0;
            for(Property property : component.getProperties())
            {
                propertiesChecksum += property.toString().hashCode();
            }
            System.out.printf("Properties Checksum '%d'\r\n", propertiesChecksum);
                   
            int profilesChecksum = 0;
            for(Profile profile : component.getProfiles())
            {
                profilesChecksum += profile.toString().hashCode();
            }
            System.out.printf("Profiles Checksum '%d'\r\n", profilesChecksum);
        }
    }

    public void validatePropertiesHaveDescription() throws IOException
    {
        for (Property prop : dataSet.getProperties())
        {
            assertNotNull(prop.getDescription());
        }
    }

    public void retrieveProperties() throws IOException
    {
        for(Property property : dataSet.properties)
        {
            System.out.printf("Testing Property '%s'\r\n", property);
            System.out.printf("Category '%s'\r\n", property.getCategory());
            System.out.printf("Component '%s'\r\n", property.getComponent());
            System.out.printf("Default Value '%s'\r\n", property.getDefaultValue());                
            System.out.printf("Description '%s'\r\n", property.getDescription());
            System.out.printf("Display Order '%s'\r\n", property.displayOrder);
            System.out.printf("IsList '%s'\r\n", property.isList);
            System.out.printf("IsMandatory '%s'\r\n", property.isMandatory);
            System.out.printf("IsObsolete '%s'\r\n", property.isObsolete);
            System.out.printf("JavaScriptName '%s'\r\n", property.getJavaScriptName());
            StringBuilder maps = new StringBuilder();
            for(int i = 0; i < property.getMaps().length; i++) {
                maps.append(property.getMaps()[i]);
                if (i < property.getMaps().length - 1) {
                    maps.append(", ");
                }
            }
            System.out.printf("Maps '%s'\r\n", maps);
            System.out.printf("Show '%s'\r\n", property.show);
            System.out.printf("ShowValues '%s'\r\n", property.showValues);
            System.out.printf("Url '%s'\r\n", property.getUrl());
            System.out.printf("Property Type '%s'\r\n", property.valueType);

            System.out.println("Values:");
            int valuesChecksum = 0;
            for(Value value : property.getValues().getAll())
            {
                valuesChecksum += value.toString().hashCode();
            }
            System.out.printf("Values Checksum '%d'\r\n", valuesChecksum);
        }
    }

    public void checkPropertyCount(int expectedProperties)
    {
        assertTrue(String.format("Property count lower than '%s'.", expectedProperties),
                dataSet.getProperties().size() >= expectedProperties);
    }

    public void retrieveValues() throws IOException
    {
        for(Value value : dataSet.getValues())
        {
            System.out.printf("Testing Value '%s'\r\n", value);
            System.out.printf("Property Name '%s'\r\n", value.getProperty());
            System.out.printf("IsDefault '%s'\r\n", value.getIsDefault());
        }
    }
}