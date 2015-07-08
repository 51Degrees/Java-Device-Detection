package fiftyone.mobile.detection;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import fiftyone.mobile.detection.entities.AsciiString;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Guid;
import fiftyone.mobile.detection.entities.IntegerEntity;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.Modes;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.ProfileOffset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.Version;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.memory.PropertiesList;
import fiftyone.mobile.detection.entities.stream.ICacheList;
import fiftyone.properties.DetectionConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * Data set used for device detection created by the reader classes. <p> The
 * Memory.Reader and Stream.Reader factories should be used to create detector
 * data sets. They can not be constructed directly from external code. <p> All
 * information about the detector data set is exposed in this class including
 * meta data and data used for device detection in the form of lists. <p>
 * Detector data sets created using the @see Stream#Reader factory
 * using a file must be disposed of to ensure any readers associated with the
 * file are closed elegantly. <p> For more information see
 * http://51degrees.com/Support/Documentation/Java
 */
public class Dataset implements IDisposable {
    /**
     * Age of the data in months when exported.
     */
    public int age;
    /**
     * The browser component.
     */
    private Component browsers;
    /**
     * A list of all the components the data set contains.
     */
    public MemoryFixedList<Component> components;
    /**
     * The copyright notice associated with the data set.
     */
    public String copyright;
    /**
     * The offset for the copyright notice associated with the data set.
     */
    public int copyrightOffset;
    /**
     * The crawler component.
     */
    private Component crawlers;
    /**
     * The number of bytes to allocate to a buffer returning CSV format data for
     * a match.
     */
    public int csvBufferLength;
    /**
     * The number of unique device combinations available in the data set.
     */
    public int deviceCombinations;
    /**
     * A unique Tag for the exported data.
     */
    public Guid export;
    /**
     * Flag to indicate if the dataset is disposed.
     */
    private boolean disposed;
    /**
     * The name of the property map used to create the dataset.
     */
    public String format;
    /**
     * The offset for the name of the property map used to create the dataset.
     */
    public int formatOffset;
    /**
     * The hardware component.
     */
    private Component hardware;
    /**
     * The highest character the character trees can contain.
     */
    public byte highestCharacter;
    /**
     * List of unique HTTP Headers that the data set needs to consider to 
     * perform the most accurate matches.
     */
    private String[] httpHeaders;
    /**
     * The number of bytes to allocate to a buffer returning JSON format data
     * for a match.
     */
    public int jsonBufferLength;
    /**
     * When the data was last modified.
     */
    public Calendar lastModified;
    /**
     * The lowest character the character trees can contain.
     */
    public byte lowestCharacter;
    /**
     * A list of all the maps the data set contains.
     */
    public MemoryFixedList<Map> maps;
    /**
     * The maximum number of signatures that can be checked. Needed to avoid
     * bogus user agents which deliberately require so many signatures to be
     * checked that performance is degraded.
     */
    public int maxSignatures;
    /**
     * The maximum number of signatures that could possibly be returned during a
     * closest match.
     */
    public int maxSignaturesClosest;
    /**
     * The maximum length of a user agent string.
     */
    public short maxUserAgentLength;
    /**
     * The maximum number of values that can be returned by a profile and a
     * property supporting a list of values.
     */
    public short maxValues;
    /**
     * The largest rank value that can be returned.
     */
    public int maximumRank;
    /**
     * The minimum number of times a user agent should have been seen before it
     * was included in the dataset.
     */
    public int minUserAgentCount;
    /**
     * The minimum length of a user agent string.
     */
    public short minUserAgentLength;
    /**
     * The mode of operation the data set is using.
     */
    public Modes mode;
    /**
     * The common name of the data set.
     */
    public String name;
    /**
     * The offset for the common name of the data set.
     */
    public int nameOffset;
    /**
     * The date the data set is next expected to be updated by 51Degrees.
     */
    public Date nextUpdate;
    /**
     * List of nodes the data set contains.
     */
    public IReadonlyList<Node> nodes;
    /**
     * List of integers that represent ranked signature indexes.
     */
    public IFixedList<IntegerEntity> nodeRankedSignatureIndexes;
    /**
     * The percentage of requests for signatures which were not already 
     * contained in the cache.
     */
    public double percentageSignatureCacheMisses;
    /**
     * A list of all the possible profiles the data set contains.
     */
    public IReadonlyList<Profile> profiles;
    /**
     * List of profile offsets the data set contains.
     */
    public IReadonlyList<ProfileOffset> profileOffsets;
    /**
     * A list of all properties the data set contains.
     */
    public PropertiesList properties;
    /**
     * The date the data set was published.
     */
    public Date published;
    /**
     * A list of signature indexes ordered in ascending order of rank. Used by 
     * the node ranked signature indexes lists to identify the corresponding 
     * signature.
     */
    public IFixedList<IntegerEntity> rankedSignatureIndexes;
    /**
     * Nodes for each of the possible character positions in the user agent.
     */
    public IReadonlyList<Node> rootNodes;
    /**
     * The number of nodes each signature can contain.
     */
    public int signatureNodesCount;
     /**
     * List of integers that represent signature node offsets.
     */
    public IFixedList<IntegerEntity> signatureNodeOffsets;
    /**
     * The number of profiles each signature can contain.
     */
    public int signatureProfilesCount;
    /**
     * A list of all the signatures the data set contains.
     */
    public IReadonlyList<Signature> signatures;
    /**
     * The software component.
     */
    private Component software;
    /**
     * A list of ASCII byte arrays for strings used by the dataset.
     */
    public IReadonlyList<AsciiString> strings;
    /**
     * A unique Tag for the data set.
     */
    public Guid tag;
    /**
     * A list of all property values the data set contains.
     */
    public IReadonlyList<Value> values;
    /**
     * The version of the data set.
     */
    public Version version;
    /**
     * The version of the data set as an enum.
     */
    public DetectionConstants.FORMAT_VERSIONS versionEnum;
    /**
     * The number of bytes to allocate to a buffer returning XML format data for
     * a match.
     */
    public int xmlBufferLength;
    
    /**
     * Constructs a new data set ready to have lists of data assigned to it.
     * @param lastModified The date and time the source of the data was 
     * last modified.
     * @param mode The mode of operation the data set will be using.
     * @throws java.io.IOException signals an I/O exception occurred
     */
    public Dataset(Date lastModified, Modes mode) throws IOException {
        this.maximumRank = 0;
        this.disposed = false;
        this.lastModified = Calendar.getInstance();
        this.lastModified.setTime(lastModified);
        this.mode = mode;
    }
    
    /**
     * Returns time that has elapsed since the data in the data set was current.
     * @return time in seconds between now and when data file was published.
     */
    public long getAge() {
        Date now = new Date();
        Date was = new Date(age);
        long difference = now.getTime() - was.getTime();
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(difference);
        return diffInSeconds;
    }
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    
   
    
    /**
     * The percentage of requests for signatures which were not already
     * contained in the cache. <p> A value is only returned when operating in
     * Stream mode.
     * @return double representing percentage of requests for signatures not 
     * currently in cache, only for Stream Mode.
     */
    public double getPercentageSignatureCacheMisses() {
        return getPercentageMisses(signatures);
    }

    /**
     * The percentage of requests for nodes which were not already
     * contained in the cache. A value is only returned when operating in 
     * Stream mode.
     * @return double representing percentage of requests for nodes not already 
     * in cache. Stream Mode only.
     */
    public double getPercentageNodeCacheMisses() {
        return getPercentageMisses(nodes);
    }

    /**
     * The percentage of requests for strings which were not already contained
     * in the cache. <p> A value is only returned when operating in Stream mode.
     * @return double representing percentage of requests for strings that were 
     * not already in cache.
     */
    public double getPercentageStringsCacheMisses() {
        return getPercentageMisses(strings);
    }

    /**
     * The percentage of requests for profiles which were not already contained
     * in the cache. <p> A value is only returned when operating in Stream mode.
     * @return double representing percentage of requests for profiles that were 
     * not already in cache.
     */
    public double getPercentageProfilesCacheMisses() {
        return getPercentageMisses(profiles);
    }

    /**
     * The percentage of requests for values which were not already contained in
     * the cache. <p> A value is only returned when operating in Stream mode.
     * @return double representing percentage of requests for values that were 
     * not already in cache.
     */
    public double getPercentageValuesCacheMisses() {
        return getPercentageMisses(values);
    }

    /**
     * The largest rank value that can be returned.
     * @return The largest rank value that can be returned.
     */
    public int getMaximumRank() {
        if (maximumRank == 0 && rankedSignatureIndexes != null) {
            synchronized(this) {
                if (maximumRank == 0 && rankedSignatureIndexes != null) {
                    maximumRank = rankedSignatureIndexes.size();
                }
            }
        }
        return maximumRank;
    }
    
    /**
     * Indicates if the data set has been disposed.
     * @return True if dataset has been disposed, False otherwise.
     */
    public boolean getDisposed() {
        return disposed;
    }

    /**
     * The hardware component.
     *
     * @return hardware component for the hardware platform
     * @throws IOException signals an I/O exception occurred
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
    

    /**
     * The software component.
     * @return software component for the software platform
     * @throws IOException signals an I/O exception occurred
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
    

    /**
     * The browser component.
     * @return browser component for browser
     * @throws IOException signals an I/O exception occurred
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

    /**
     * The crawler component.
     * @return crawler component
     * @throws IOException signals an I/O exception has occurred
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

    /**
     * The copyright notice associated with the data set.
     * @return string of text representing copyright notice for the data set
     * @throws IOException signals an I/O exception occurred
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

    /**
     * The common name of the data set.
     * @return name of the data set
     * @throws IOException signals an I/O exception occurred
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

    /**
     * The name of the property map used to create the dataset.
     * @return name of the property map used to create dataset
     * @throws IOException signals an I/O exception occurred
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


    /**
     * A list of all the components the data set contains.
     * @return a read-only list of all components contained in data set
     */
    public IReadonlyList<Component> getComponents() {
        return components;
    }
    

    /**
     * A list of all property maps the data set contains.
     * @return a read-only list of all maps contained in the data set
     */
    public IReadonlyList<Map> getMaps() {
        return maps;
    }

    /**
     * A list of all properties the data set contains.
     * @return a read-only list of all properties contained in the data set
     */
    public IReadonlyList<Property> getProperties() {
        return properties;
    }
    

    /**
     * A list of all property values the data set contains.
     * @return a read-only list of values contained in the data set
     */
    public IReadonlyList<Value> getValues() {
        return values;
    }
    

    /**
     * List of signatures the data set contains.
     * @return a read-only list of all signatures contained in the data set
     */
    public IReadonlyList<Signature> getSignatures() {
        return signatures;
    }
    
    /**
     * List of nodes the data set contains.
     * @return a read-only list of nodes contained in the data set
     */
    public IReadonlyList<Node> getNodes() {
        return nodes;
    }

    /**
     * A list of all the possible profiles the data set contains.
     * @return a read-only list of all profiles contained in the data set
     */
    public IReadonlyList<Profile> getProfiles() {
        return profiles;
    }

    public short getMinUserAgentLength() {
        return minUserAgentLength;
    }

    /**
     * Returns a list of integers that represent signature node offsets.
     * @return list of integers that represent signature node offsets.
     */
    public IFixedList<IntegerEntity> getSignatureNodeOffsets() {
        return signatureNodeOffsets;
    }
    
    /**
     * Returns a list of integers that represent ranked signature indexes.
     * @return a list of integers that represent ranked signature indexes.
     */
    public IFixedList<IntegerEntity> getNodeRankedSignatureIndexes() {
        return nodeRankedSignatureIndexes;
    }
    
    /**
     * Creates a list of HTTP headers if one does not already exist.
     * @return list of HTTP headers as Strings.
     */
    public String[] getHttpHeaders() {
        if (httpHeaders == null) {
            synchronized(httpHeaders) {
                if (httpHeaders == null) {
                    List<String> tempList = new ArrayList<String>();
                    for (Component c : components) {
                       for (String s : c.getHttpheaders()) {
                           if (!tempList.contains(s))
                               tempList.add(s);
                       }
                    }
                    httpHeaders = new String[tempList.size()];
                    httpHeaders = tempList.toArray(httpHeaders);
                }
            }
        }
        return httpHeaders;
    }
    
    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     *
     * @throws IOException signals an I/O exception occurred
     */
    public void init() throws IOException {

        // Set the string values of the data set.
        name = strings.get(nameOffset).toString();
        format = strings.get(formatOffset).toString();
        copyright = strings.get(copyrightOffset).toString();
        
        initComponents();
        initProperties();
        initValues();
        initProfiles();
        initNodes();
        initSignatures();

        // We no longer need the strings data structure as all dependent
        // data has been taken from it.
        strings = null;
    }

    /**
     * Preloads signatures to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initSignatures() throws IOException {
        // Initialise any objects that can be pre referenced to speed up
        // initial matching.
        for (Signature signature : signatures) {
            signature.init();
        }
    }

    /**
     * Preloads nodes to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initNodes() throws IOException {
        for (Node node : nodes) {
            node.init();
        }
    }

    /**
     * Preloads profiles to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initProfiles() throws IOException {
        for (Profile profile : profiles) {
            profile.init();
        }
    }

    /**
     * Preloads components to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initComponents() throws IOException {
        for (Component component : getComponents()) {
            component.init();
        }
    }

    /**
     * Preloads properties to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initProperties() throws IOException {
        for (Property property : getProperties()) {
            property.init();
        }
    }

    /**
     * Preloads values to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if init() has already been called.
     * @throws java.io.IOException
     */
    public void initValues() throws IOException {
        for (Value value : values) {
            value.init();
        }
    }

    /**
     * Returns the Component associated with the name provided.
     *
     * @param componentName string representing name of the component to retrieve
     * @return The component matching the name, or null if no component is found
     * @throws IOException signals an I/O exception occurred
     *
     */
    public Component getComponent(String componentName) throws IOException {
        for (Component component : components) {
            if (componentName.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }

    /**
     * Method searches for a property with the given name and returns one if 
     * found.
     * @param propertyName name of the property to find.
     * @return Property object or null if no property with requested name exists
     * @throws java.io.IOException
     */
    public Property get(String propertyName) throws IOException {
        return getPropertyByName(propertyName);
    }
    
    /**
     * Method searches for a property with the given name and returns the 
     * Property if found. Returns null otherwise.
     * @param propertyName name of the property to find as a string.
     * @return Property object or null if no property with requested name exists
     * @throws IOException 
     */
    public Property getPropertyByName(String propertyName) throws IOException {
        for (Property property : properties) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    /**
     * Returns the number of profiles each signature can contain.
     * @return The number of profiles each signature can contain.
     */
    public int getProfilesCount() {
        return signatureProfilesCount;
    }

    
    /**
     * Returns the number of nodes each signature can contain.
     * @return The number of nodes each signature can contain.
     */
    public int getNodesCount() {
        return signatureNodesCount;
    }

    /**
     * Searches the list of profile Ids and returns the profile if the profile
     * id is valid.
     *
     * @param profileId Id of the profile to be found
     * @return Profile related to the id, or null if none found
     * @throws IOException signals an I/O exception occurred
     */
    public Profile findProfile(int profileId) throws IOException {
        int lower = 0;
        int upper = profileOffsets.size() - 1;

        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = profileOffsets.get(middle).getProfileId() - profileId;
            if (comparisonResult == 0) {
                return profiles.get(profileOffsets.get(middle).getOffset());
            } else if (comparisonResult > 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }

        return null;
    }

    /**
     * Disposes of the data set.
     */
    @Override
    public void dispose() {
        disposed = true;
    }
    
    /**
     * The percentage of requests for ranked signatures which were not already
     * contained in the cache.
     * A value is only returned when operating in Stream mode.
     * @return The percentage of requests for ranked signatures which were 
     * not already contained in the cache.
     */
    public double getPercentageRankedSignatureCacheMisses() {
        return getPercentageMisses(rankedSignatureIndexes);
    }
    
    /**
     * Number of times the signature cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the signature cache was switched.
     */
    public long getSignatureCacheSwitches() {
        return getSwitches(signatures);
    }
    
    /**
     * Number of times the node cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the node cache was switched.
     */
    public long getNodeCacheSwitches() {
        return getSwitches(nodes);
    }
    
    /**
     * Number of times the strings cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the strings cache was switched.
     */
    public long getStringsCacheSwitches() {
        return getSwitches(strings);
    }
    
    /**
     * Number of times the profiles cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the profiles cache was switched.
     */
    public long getProfilesCacheSwitches() {
        return getSwitches(profiles);
    }
    
    /**
     * Number of times the values cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the values cache was switched.
     */
    public long getValuesCacheSwitches() {
        return getSwitches(values);
    }
    
    /**
     * Returns a list of signature indexes ordered in ascending order of rank.
     * @return A list of signature indexes ordered in ascending order of rank.
     */
    public IFixedList<IntegerEntity> getRankedSignatureIndexes() {
        return rankedSignatureIndexes;
    } 
    
    /**
     * Number of times the ranked signature cache was switched.
     * A value is only returned when operating in Stream mode.
     * @return Number of times the ranked signature cache was switched.
     */
    public long getRankedSignatureCacheSwitches() {
        return getSwitches(rankedSignatureIndexes);
    }
    
    /**
     * Returns the percentage of requests that weren't serviced by the cache.
     * @param list a Cache object to get percentage from.
     * @return 0 if object is not Cache, percentage otherwise.
     */
    private static double getPercentageMisses(Object list) {
        if (list instanceof ICacheList) {
            ICacheList c = (ICacheList)list;
            return c.getPercentageMisses();
        }
        return -1;
    }
    
    /**
     * Returns the number of times the cache lists were switched.
     * @param list a Cache object to get percentage from.
     * @return 0 if object is not Cache, percentage otherwise.
     */
    private static long getSwitches(Object list) {
        if (list instanceof ICacheList) {
            ICacheList c = (ICacheList)list;
            return c.getSwitches();
        }
        return -1;
    }
}