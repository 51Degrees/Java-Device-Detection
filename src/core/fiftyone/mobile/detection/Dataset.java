package fiftyone.mobile.detection;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import fiftyone.mobile.detection.entities.AsciiString;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Guid;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.ProfileOffset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.Version;
import fiftyone.mobile.detection.entities.stream.ICacheList;
import fiftyone.mobile.detection.readers.BinaryReader;

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
 * Data set used for device detection created by the reader classes.
 * <p>
 * The Memory.Reader and Stream.Reader factories should be used to create
 * detector data sets. They can not be constructed directly from external code.
 * <p>
 * All information about the detector data set is exposed in this class
 * including meta data and data used for device detection in the form of lists.
 * <p>
 * Detector data sets created using the <see cref="Stream.Reader"/> factory
 * using a file must be disposed of to ensure any readers associated with the
 * file are closed elegantly.
 * <p>
 * For more information see http://51degrees.mobi/Support/Documentation/Java
 */
public class Dataset implements Disposable {
	/**
	 * The percentage of requests for signatures which were not already
	 * contained in the cache.
	 * <p>
	 * A value is only returned when operating in Stream mode.
	 */
	public double getPercentageSignatureCacheMisses() {
		if (signatures instanceof ICacheList)
			return ((ICacheList)signatures).getPercentageMisses();
		return 0;
	}

	/**
	 * The percentage of requests for nodes which were not already contained in
	 * the cache.
	 * <p>
	 * A value is only returned when operating in Stream mode.
	 */
	public double getPercentageNodeCacheMisses() {
		if (nodes instanceof ICacheList)
			return ((ICacheList)nodes).getPercentageMisses();
		return 0;
	}

	/**
	 * The percentage of requests for strings which were not already contained
	 * in the cache.
	 * <p>
	 * A value is only returned when operating in Stream mode.
	 */
	public double getPercentageStringsCacheMisses() {
		if (strings instanceof ICacheList)
			return ((ICacheList)strings).getPercentageMisses();
		return 0;
	}

	/**
	 * The percentage of requests for profiles which were not already contained
	 * in the cache.
	 * <p>
	 * A value is only returned when operating in Stream mode.
	 */
	public double getPercentageProfilesCacheMisses() {
		if (profiles instanceof ICacheList)
			return ((ICacheList)profiles).getPercentageMisses();
		return 0;
	}

	/**
	 * The percentage of requests for values which were not already contained in
	 * the cache.
	 * <p>
	 * A value is only returned when operating in Stream mode.
	 */
	public double getPercentageValuesCacheMisses() {
		if (values instanceof ICacheList)
			return ((ICacheList)values).getPercentageMisses();
		return 0;
	}

	/**
	 * Indicates if the data set has been disposed.
	 */
	public boolean getDisposed() {
		return disposed;
	}
	private boolean disposed = false;
	
	/**
	 * The date the data set was published.
	 */
	public final Date published;

	/**
	 * The date the data set is next expected to be updated by 51Degrees.
	 */
	public final Date nextUpdate;

	/**
	 * The minimum number of times a user agent should have been seen before it
	 * was included in the dataset.
	 */
	public final int minUserAgentCount;

	/**
	 * The version of the data set.
	 */
	public final Version version;

	/**
	 * The maximum length of a user agent string.
	 */
	public final short maxUserAgentLength;

	/**
	 * The minimum length of a user agent string.
	 */
	private final short minUserAgentLength;

	/**
	 * The lowest character the character trees can contain.
	 */
	public final byte lowestCharacter;

	/**
	 * The highest character the character trees can contain.
	 */
	public final byte highestCharacter;

	/**
	 * The number of unique device combinations available in the data set.
	 */
	public final int deviceCombinations;

	/**
	 * The maximum number of signatures that can be checked. Needed to avoid
	 * bogus user agents which deliberately require so many signatures to be
	 * checked that performance is degraded.
	 */
	public final int maxSignatures;

	/**
	 * The maximum number of values that can be returned by a profile
	 * and a property supporting a list of values.
	 */
    public final short maxValues;

    /**
     * The number of bytes to allocate to a buffer returning
     * CSV format data for a match.
     */
    public final int csvBufferLength;

    /**
     * The number of bytes to allocate to a buffer returning
     * JSON format data for a match.
     */
    public final int jsonBufferLength;

    /**
     * The number of bytes to allocate to a buffer returning
     * XML format data for a match.
     */
    public final int xmlBufferLength;
	
    /**
     * The maximum number of signatures that could possibly
     * be returned during a closest match.
     */
    public final int maxSignaturesClosest;
    
	public final Guid guid;
	
	/**
	 * Age of the data in months when exported.
	 */
	public final int age;

	/**
	 * The hardware component.
	 * @throws IOException 
	 */
	public Component getHardware() throws IOException {
		if (hardware == null) {
			synchronized (this) {
				if (hardware == null) {
					hardware = getComponent("HardwarePlatform");
				}
			}
		}
		return hardware;
	}

	private Component hardware;

	/**
	 * The software component.
	 * @throws IOException 
	 */
	public Component getSoftware() throws IOException {
		if (software == null) {
			synchronized (this) {
				if (software == null) {
					software = getComponent("SoftwarePlatform");
				}
			}
		}
		return software;
	}

	private Component software;

	/**
	 * The browser component.
	 * @throws IOException 
	 */
	public Component getBrowsers() throws IOException {
		if (browsers == null) {
			synchronized (this) {
				if (browsers == null) {
					browsers = getComponent("BrowserUA");
				}
			}
		}
		return browsers;
	}

	private Component browsers;

	/**
	 * The crawler component.
	 * @throws IOException 
	 */
	public Component getCrawlers() throws IOException {
		if (crawlers == null) {
			synchronized (this) {
				if (crawlers == null) {
					crawlers = getComponent("Crawler");
				}
			}
		}
		return crawlers;
	}

	private Component crawlers;

	/**
	 * The copyright notice associated with the data set.
	 * @throws IOException 
	 */
	public String getCopyright() throws IOException {
		if (copyright == null) {
			synchronized (this) {
				if (copyright == null) {
					copyright = strings.get(copyrightOffset).toString();
				}
			}
		}
		return copyright;
	}

	protected String copyright;
	protected final int copyrightOffset;

	/**
	 * The common name of the data set.
	 * @throws IOException 
	 */
	public String getName() throws IOException {
		if (name == null) {
			synchronized (this) {
				if (name == null) {
					name = strings.get(nameOffset).toString();
				}
			}
		}
		return name;
	}

	protected final int nameOffset;
	protected String name;

	/**
	 * The name of the property map used to create the dataset.
	 * @throws IOException 
	 */
	public String getFormat() throws IOException {
		if (format == null) {
			synchronized (this) {
				if (format == null) {
					format = strings.get(formatOffset).toString();
				}
			}
		}
		return format;
	}

	protected final int formatOffset;
	protected String format;

	/**
	 * A list of all the components the data set contains.
	 */
	public ReadonlyList<Component> getComponents() {
		return components;
	}

	public ReadonlyList<Component> components;

        /**
         * A list of all property maps the data set contains.
         */
        public ReadonlyList<Map> getMaps() {
            return maps;
        }
        
        public ReadonlyList<Map> maps;
        
	/**
	 * A list of all properties the data set contains.
	 */
	public ReadonlyList<Property> getProperties() {
		return properties;
	}

	public ReadonlyList<Property> properties;

	/**
	 * A list of all property values the data set contains.
	 */
	public ReadonlyList<Value> getValues() {
		return values;
	}

	public ReadonlyList<Value> values;

	/**
	 * List of signatures the data set contains.
	 */
	public ReadonlyList<Signature> getSignatures() {
		return signatures;
	}

	/**
	 * A list of all the signatures the data set contains.
	 */
	public ReadonlyList<Signature> signatures;

	/**
	 * A list of all the possible profiles the data set contains.
	 */
	public ReadonlyList<Profile> profiles;

	/**
	 * List of nodes the data set contains.
	 */
	public ReadonlyList<Node> nodes;

	/**
	 * Nodes for each of the possible character positions in the user agent.
	 */
	public ReadonlyList<Node> rootNodes;

	/**
	 * List of profile offsets the data set contains.
	 */
	public ReadonlyList<ProfileOffset> profileOffsets;
	
	/**
	 * A list of ASCII byte arrays for strings used by the dataset.
	 */
	public ReadonlyList<AsciiString> strings;

	private int signatureProfilesCount;

	private int signatureNodesCount;

	/**
	 * Constructs a new data set ready to have lists of data assigned to it.
	 * 
	 * @param reader
	 *            Reader connected to the source data structure and positioned
	 *            to start reading
	 */
	public Dataset(BinaryReader reader) {
		// Read the detection data set headers.
		version = new Version(reader.readInt32(), reader.readInt32(),
				reader.readInt32(), reader.readInt32());
		guid = new Guid(reader.readBytes(16));
		copyrightOffset = reader.readInt32();
		age = reader.readInt16();
		minUserAgentCount = reader.readInt32();
		nameOffset = reader.readInt32();
		formatOffset = reader.readInt32();
		published = readDate(reader);
		nextUpdate = readDate(reader);
		deviceCombinations = reader.readInt32();
		maxUserAgentLength = reader.readInt16();
		minUserAgentLength = reader.readInt16();
		lowestCharacter = reader.readByte();
		highestCharacter = reader.readByte();
		maxSignatures = reader.readInt32();
		signatureProfilesCount = reader.readInt32();
		signatureNodesCount = reader.readInt32();
		maxValues = reader.readInt16();
        csvBufferLength = reader.readInt32();
        jsonBufferLength = reader.readInt32();
        xmlBufferLength = reader.readInt32();
        maxSignaturesClosest = reader.readInt32();
        
		nodes = null;
	}

	/**
	 * Reads a date in year, month and day order from the reader.
	 * 
	 * @param reader
	 *            Reader positioned at the start of the date
	 * @return A date time with the year, month and day set from the reader
	 */
	private static Date readDate(BinaryReader reader) {
		int year = reader.readInt16();
		int month = reader.readByte() - 1;
		int day = reader.readByte();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month, day);
		return cal.getTime();
	}

	/**
	 * List of nodes the data set contains.
	 */
	public ReadonlyList<Node> getNodes() {
		return nodes;
	}

	/**
	 * A list of all the possible profiles the data set contains.
	 */
	public ReadonlyList<Profile> getProfiles() {
		return profiles;
	}

	public short getMinUserAgentLength() {
		return minUserAgentLength;
	}

	/**
	 * Called after the entire data set has been loaded to ensure any further
	 * initialisation steps that require other items in the data set can be
	 * completed.
	 * @throws IOException 
	 */
	public void init() throws IOException {
		
		// Set the string values of the data set.
		name = strings.get(nameOffset).toString();
		format = strings.get(formatOffset).toString();
		copyright = strings.get(copyrightOffset).toString();

		// Initialise any objects that can be pre referenced to speed up
		// initial matching.
		for (Signature signature : signatures) {
			signature.init();
		}
		for (Node node : nodes) {
			node.init();
		}
		for (Profile profile : profiles) {
			profile.init();
		}
		for (Component component : getComponents()) {
			component.init();
		}
		for (Property property : getProperties()) {
			property.init();
		}
		for (Value value : values) {
			value.init();
		}

		// We no longer need the strings data structure as all dependent
		// data has been taken from it.
		strings.dispose();
		strings = null;
		
		// The list of profiles is no longer needed as they've been assigned
		// components and signatures.
		profiles.dispose();
		profiles = null;
	}

	/**
	 * Returns the Component associated with the name provided.
	 * 
	 * @param componentName
	 * @return The component matching the name, or null if no component is found
	 * @throws IOException 
	 * */
	public Component getComponent(String componentName) throws IOException {
		for (Component component : components) {
			if (componentName.equals(component.getName())) {
				return component;
			}
		}
		return null;
	}

	public Property get(String propertyName) throws IOException {
		for (Property property : properties) {
			if (propertyName.equals(property.getName())) {
				return property;
			}
		}
		return null;
	}

	public int getProfilesCount() {
		return signatureProfilesCount;
	}

	public int getNodesCount() {
		return signatureNodesCount;
	}

	/**
	 * Searches the list of profile Ids and returns the profile if the profile 
     * id is valid.
	 * @param profileId Id of the profile to be found
	 * @return Profile related to the id, or null if none found
	 * @throws IOException
	 */
    public Profile findProfile(int profileId) throws IOException
    {
        int lower = 0;
        int upper = profileOffsets.size() - 1;

        while (lower <= upper)
        {
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = profileOffsets.get(middle).getProfileId() - profileId;
            if (comparisonResult == 0)
                return profiles.get(profileOffsets.get(middle).getOffset());
            else if (comparisonResult > 0)
                upper = middle - 1;
            else
                lower = middle + 1;
        }

        return null;
    }
	
	@Override
	public void dispose() throws IOException {
        disposed = true;
        if (strings != null)
            strings.dispose();
        if (components != null)
            components.dispose();
        if (properties != null)
            properties.dispose();
        if (values != null)
            values.dispose();
        if (signatures != null)
            signatures.dispose();
        if (profiles != null)
            profiles.dispose();
        if (nodes != null)
            nodes.dispose();
        if (rootNodes != null)
            rootNodes.dispose();
	}
}