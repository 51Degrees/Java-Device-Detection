package fiftyone.mobile.detection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;

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

/**
 * Provider used to perform a detection based on a user agent string.
 */
public class Provider {

	private static final String USER_AGENT = "User-Agent";
	
	/**
	 * The total number of detections performed by the data set.
	 */
	public long getDetectionCount() {
		return detectionCount;
	}

	private long detectionCount;

	/**
	 * The number of detections performed using the method.
	 */
	private final SortedList<MatchMethods, Long> methodCounts;

	/**
	 * The data set associated with the provider.
	 */
	public final Dataset dataSet;

	private Controller controller;

	/**
	 * Builds a new provider with the embedded data set.
	 * @throws IOException
	 */
	public Provider() throws IOException {
		this(MemoryFactory.read(new BinaryReader(getEmbeddedByteArray()), false));
	}
	
	/**
	 * Reads the embedded data into a byte array to be used as a 
	 * byte buffer in the factory.
	 * @return
	 * @throws IOException
	 */
	private static byte[] getEmbeddedByteArray() throws IOException {
		byte[] buffer = new byte[1048576];
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				DetectionConstants.EMBEDDED_DATA_RESOURCE_NAME);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int count = input.read(buffer);
		while (count > 0) {
			output.write(buffer, 0, count);
			count = input.read(buffer);
		}
		return output.toByteArray();
	}
	
	/**
	 * Constructs a new provided using the data set.
	 * 
	 * @param dataSet
	 *            Data set to use for device detection
	 */
	public Provider(Dataset dataSet) {
		this(dataSet, new Controller());
	}

	Provider(Dataset dataSet, Controller controller) {
		this.dataSet = dataSet;
		this.controller = controller;
		this.methodCounts = new SortedList<MatchMethods, Long>();
		this.methodCounts.add(MatchMethods.CLOSEST, 0l);
		this.methodCounts.add(MatchMethods.NEAREST, 0l);
		this.methodCounts.add(MatchMethods.NUMERIC, 0l);
		this.methodCounts.add(MatchMethods.EXACT, 0l);
		this.methodCounts.add(MatchMethods.NONE, 0l);
	}

	/**
	 * Creates a new match object to be used for matching.
	 * @return a match object ready to be used with the Match methods
	 * @throws Exception
	 */
    public Match createMatch()
    {
        return new Match(dataSet);
    }
	
	/**
	 * For a given collection of HTTP headers returns a match containing
	 * information about the capabilities of the device and it's components.
	 * 
	 * @param headers
	 *            List of HTTP headers to use for the detection
	 * @return a match for the target headers provided
	 * @throws IOException 
	 */
	public Match match(final Map<String, String> headers) throws IOException {
		return match(headers.get(getUserAgentString()), createMatch());
	}

	protected String getUserAgentString() {
		return USER_AGENT;
	}
	
	/**
	 * For a given user agent returns a match containing information about the
	 * capabilities of the device and it's components.
	 * 
	 * @param targetUserAgent
	 * @return a match result for the target user agent
	 * @throws IOException 
	 */
	public Match match(String targetUserAgent) throws IOException {
		return match(targetUserAgent, createMatch());
	}

	/**
	 * For a given user agent returns a match containing 
	 * information about the capabilities of the device and 
	 * it's components.
	 * @param targetUserAgent The user agent string to use as the target
	 * @param match A match object created by a previous match, or via the 
     * CreateMatch method.
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public Match match(String targetUserAgent, Match match) throws IOException {
		
        match.reset(targetUserAgent);
		controller.match(match);

		// Update the counts for the provider.
		detectionCount++;
		synchronized (methodCounts) {
			MatchMethods method = match.getMethod();
			Long count = methodCounts.get(method);
			long value = count.longValue();
			methodCounts.put(method, value++);
		}
		
		return match;
	}
}
