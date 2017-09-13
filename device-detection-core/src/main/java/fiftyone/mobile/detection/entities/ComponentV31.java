/* *********************************************************************
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
 * ********************************************************************* */
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;

/**
 * Extends {@link Component} by providing implementation for the 
 * {@link #getHttpheaders()} method. Headers for version 3.1 are hard coded 
 * into the Constants.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class ComponentV31 extends Component {
    
    /**
     * Constructs a new instance of ComponentV31. Reads the string offsets to 
     * the HTTP Headers during the constructor.
     * 
     * @param dataSet The Dataset being created.
     * @param index Index of the component within the list.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public ComponentV31(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index, reader);
        this.httpHeaders = null;
    }

    /**
     * Implements {@code getHttpheaders()} method. For version 3.1 a list of 
     * HTTP headers is retrieved from {@link DetectionConstants}.
     * 
     * @return List of HTTP headers as strings.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    @Override
    public String[] getHttpheaders() throws IOException {
        String[] localHttpHeaders = httpHeaders;
        if (localHttpHeaders == null) {
            synchronized(this) {
                localHttpHeaders = httpHeaders;
                if (localHttpHeaders == null) {
                    // Implemented as "if else" for 1.6 compatibility.
                    String cName = super.getName(); 
                    if (cName.equals("HardwarePlatform")) {
                        httpHeaders = localHttpHeaders =
                                DetectionConstants.DEVICE_USER_AGENT_HEADERS;
                    } else if (cName.equals("SoftwarePlatform")) {
                        httpHeaders = localHttpHeaders =
                                DetectionConstants.DEVICE_USER_AGENT_HEADERS;
                    } else if (cName.equals("BrowserUA")) {
                        httpHeaders = localHttpHeaders =
                                new String[] {
                                    DetectionConstants.USER_AGENT_HEADER
                                };
                    } else if (cName.equals("Crawler")) {
                        httpHeaders = localHttpHeaders =
                                new String[] {
                                    DetectionConstants.USER_AGENT_HEADER
                                };
                    }
                }
            }
        }
        return localHttpHeaders;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile String[] httpHeaders;
    
}
