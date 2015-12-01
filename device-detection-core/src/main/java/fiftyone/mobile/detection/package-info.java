/* *********************************************************************
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
 * ********************************************************************* */
/**
 * Contains classes necessary to perform device detection and access device 
 * data.
 * <p>
 * Use like:
 * <br />
 * <code>Provider p = new Provider(StreamFactory.create("path_to_file", false));
 * <br />Match m = p.match("User-Agent string here");
 * <br />System.out.println(m.getValues("IsMobile"));
 * </code>
 * <br />Where {@code StreamFactory.create} initialises a 
 * {@link fiftyone.mobile.detection.Dataset Dataset} object which is responsible 
 * for interacting with device data.
 * <p>
 * Two factories are available: Stream and Memory.
 * <br />{@link fiftyone.mobile.detection.factories.StreamFactory Stream} 
 * factory creates a Dataset object that maintains a pool of binary readers. 
 * Each reader maintains an open channel to the data file and is used to look 
 * up device data when required.
 * <br />{@link fiftyone.mobile.detection.factories.MemoryFactory Memory} 
 * factory creates a Dataset where the data file is loaded into memory as 
 * either an array of bytes or a complete set of entities. Whether the data 
 * file is loaded fully or as an array of bytes is controlled by the boolean 
 * flag when invoking the 
 * {@code: MemoryFactory.create("path_to_file", false)};
 * <p>
 * The {@link fiftyone.mobile.detection.Provider Provider} object should be 
 * used to perform device detection. It exposes several {@code match()} methods 
 * that take in a User-Agent, a collection of HTTP headers and a device Id and 
 * return a {@link fiftyone.mobile.detection.Match Match} object with 
 * detection results.
 * <p>
 * Along with detection results {@code Match} objects also contain match 
 * metrics information such as the device Id, detection method, difference and 
 * rank. Match metrics information can be used for a number of things such as 
 * spotting fake User-Agents. 
 * <a href="https://51degrees.com/support/documentation/pattern">
 * How device detection works</a>.
 * <p>
 * Device detection uses LRU (Least Recently Used) caching algorithm to speed 
 * up detections for the more frequently occurring User-Agents. Caching is 
 * also used internally with the stream detection method to speed up the 
 * retrieval of various entities.
 * <p>
 * {@link fiftyone.mobile.detection.AutoUpdate AutoUpdate} class provides a 
 * method that should be used to retrieve the latest device data. The automatic 
 * update requires a licence key. 
 * <a href="https://51degrees.com/compare-data-options">Get a licence key</a>.
 * <p>
 * 51Degrees device data is supplied in the binary form but Java API can be 
 * used to work with data entities. The tutorials on the Web site as well as 
 * the examples in the example.illustration package (located in the different 
 * module of this Maven project) provide several examples of working with data.
 * For more information about the data model please see:
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * Pattern data model</a>.
 * <p>
 * For more information please see the 
 * <a href="https://51degrees.com/support/documentation/java">
 * Java documentation</a> pages.
 */
package fiftyone.mobile.detection;
