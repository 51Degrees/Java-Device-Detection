/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.DatasetBuilder.CacheType;
import fiftyone.mobile.detection.entities.*;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.memory.PropertiesList;
import fiftyone.mobile.detection.search.SearchBase;
import fiftyone.mobile.detection.search.SearchResult;
import fiftyone.properties.DetectionConstants;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Data set used for device detection and provide methods to work with device 
 * data. 
 * <p>
 * Dataset should not be constructed directly.
 * See {@link DatasetBuilder} for a convenient way to instantiate this class.
 * <p>
 * Alternatively use either
 * {@link fiftyone.mobile.detection.factories.StreamFactory} or
 * {@link fiftyone.mobile.detection.factories.MemoryFactory}.
 * <p>
 * Stream factory
 * returns a {@link IndirectDataset} which extends the this class and contains data members and methods
 * to access the data file on the "as needed" basis as well as supporting caching.
 * <p>
 * All information about the detector data set is exposed in this class 
 * including meta data and data used for device detection in the form of lists.
 * <p>
 * When you create a data set remember to {@link #close()} it to release 
 * resources and file locks.
 * <p>
 * For more information see https://51degrees.com/Support/Documentation/Java
 */
public class Dataset implements Closeable {
    public boolean FindProfilesInitialiseValueProfiles = false;

    /**
     * Age of the data in months when exported.
     */
    public int age;
    /**
     * A list of all the components the data set contains.
     */
    public MemoryFixedList<Component> components;
    /**
     * The copyright notice associated with the data set.
     */
    public volatile String copyright;
    /**
     * The offset for the copyright notice associated with the data set.
     */
    public int copyrightOffset;
    /**
     * The number of bytes to allocate to a buffer returning CSV format data 
     * for a match.
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
     * The name of the property map used to create the dataset.
     */
    public volatile String format;
    /**
     * The offset for the name of the property map used to create the dataset.
     */
    public int formatOffset;
    /**
     * The highest character the character trees can contain.
     */
    public byte highestCharacter;
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
     * bogus User-Agents which deliberately require so many signatures to be
     * checked that performance is degraded.
     */
    public int maxSignatures;
    /**
     * The maximum number of signatures that could possibly be returned during 
     * a closest match.
     */
    public int maxSignaturesClosest;
    /**
     * The maximum length of a User-Agent string.
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
    public volatile int maximumRank;
    /**
     * The minimum number of times a User-Agent should have been seen before it
     * was included in the dataset.
     */
    public int minUserAgentCount;
    /**
     * The minimum length of a User-Agent string.
     */
    public short minUserAgentLength;
    /**
     * The mode of operation the data set is using.
     */
    public final Modes mode;
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
    public ISimpleList nodeRankedSignatureIndexes;
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
     * Array of JavaScript properties.
     */
    private Property[] javaScriptProperties = null;
    /**
     * Array of JavaScript properties related to property value overrides.
     */
    private Property[] pvoProperties;
    /**
     * The date the data set was published.
     */
    public Date published;
    /**
     * A list of signature indexes ordered in ascending order of rank. Used by 
     * the node ranked signature indexes lists to identify the corresponding 
     * signature.
     */
    public ISimpleList rankedSignatureIndexes;
    /**
     * Nodes for each of the possible character positions in the User-Agent.
     */
    public IReadonlyList<Node> rootNodes;
    /**
     * The number of nodes each signature can contain.
     */
    public int signatureNodesCount;
     /**
     * List of integers that represent signature node offsets.
     */
    public ISimpleList signatureNodeOffsets;
    /**
     * The number of profiles each signature can contain.
     */
    public int signatureProfilesCount;
    /**
     * A list of all the signatures the data set contains.
     */
    public IReadonlyList<Signature> signatures;
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
     * The version of the data set as an Enumeration.
     */
    public DetectionConstants.FORMAT_VERSIONS versionEnum;
    /**
     * The number of bytes to allocate to a buffer returning XML format data for
     * a match.
     */
    public int xmlBufferLength;

    /**
     * Constructs a new data set ready to have lists of data assigned to it.
     * 
     * @param lastModified The date and time the source of the data was 
     *                     last modified.
     * @param mode The mode of operation the data set will be using.
     * @throws java.io.IOException signals an I/O exception occurred
     */
    public Dataset(Date lastModified, Modes mode) throws IOException {
        this.httpHeaders = null;
        this.maximumRank = 0;
        this.disposed = false;
        this.lastModified = Calendar.getInstance();
        this.lastModified.setTime(lastModified);
        this.mode = mode;
    }
    
    /**
     * Returns time that has elapsed since the data in the data set was current.
     * 
     * @return time in seconds between now and when data file was published.
     */
    public long getAge() {
        Date now = new Date();
        Date was = new Date((long)age);
        long difference = now.getTime() - was.getTime();
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(difference);
        return diffInSeconds;
    }

    /**
     * The largest rank value that can be returned. Maximum rank value can be 
     * useful when working with signature ranks to calculate the rank percentage 
     * and quickly sort.
     * 
     * @return The largest rank value that can be returned.
     */
    public int getMaximumRank() {
        int maxRank = maximumRank;
        if (maxRank == 0 && rankedSignatureIndexes != null) {
            synchronized(this) {
                maxRank = maximumRank;
                if (maxRank == 0 && rankedSignatureIndexes != null) {
                    maximumRank = maxRank = rankedSignatureIndexes.size();
                }
            }
        }
        return maxRank;
    }
    
    /**
     * Indicates if the data set has been disposed.
     * 
     * @return True if dataset has been disposed, False otherwise.
     */
    public boolean getDisposed() {
        return disposed;
    }
    private boolean disposed;

    /**
     * Returns the hardware {@link Component} that contains a set of 
     * {@link Property properties} and {@link Profile profiles} related to 
     * hardware (such as: IsCrawler property).
     * <p>
     * Note that for the 'Lite' data file this component returns only three 
     * properties: IsMobile, ScreenPixelsWidth and ScreenPixelsHeight.
     * <p>
     * Premium and Enterprise data files contain considerably more data, meaning 
     * more accurate detections and access to properties like DeviceType, 
     * IsTablet, DeviceRAM, HardwareName, PriceBand, ScreenInchesWidth and more.
     * <a href="https://51degrees.com/compare-data-options">
     * Compare data options</a>.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return hardware component for the hardware platform
     * @throws IOException signals an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getHardware() throws IOException {
        Component localHardware = hardware;
        if (localHardware == null) {
            synchronized (this) {
                localHardware = hardware;
                if (localHardware == null) {
                    hardware = localHardware = getComponent("HardwarePlatform");
                }
            }
        }
        return localHardware;
    }
    private volatile Component hardware;
    

    /**
     * Returns the software {@link Component} that contains a set of 
     * {@link Property properties} and {@link Profile profiles} related to 
     * software. Software component includes properties like: PlatformName and 
     * PlatformVersion.
     * <p>
     * Premium and Enterprise data files contain considerably more data, meaning 
     * more accurate detections and access to properties like PlatformVendor, 
     * CLDC and CcppAccept.
     * <a href="https://51degrees.com/compare-data-options">
     * Compare data options</a>.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return software component for the software platform
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getSoftware() throws IOException {
        Component localSoftware = software;
        if (localSoftware == null) {
            synchronized (this) {
                localSoftware = software;
                if (localSoftware == null) {
                    software = localSoftware = getComponent("SoftwarePlatform");
                }
            }
        }
        return localSoftware;
    }
    private volatile Component software;

    /**
     * Returns the browser {@link Component} that contains a set of 
     * {@link Property properties} and {@link Profile profiles} related to 
     * browsers.
     * <p>
     * Premium and Enterprise data files contain considerably more data, meaning 
     * more accurate detections and access to properties like AjaxRequestType,
     * IsWebApp, HtmlVersion and Javascript.
     * <a href="https://51degrees.com/compare-data-options">
     * Compare data options</a>.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return browser {@code Component}.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getBrowsers() throws IOException {
        Component localBrowsers = browsers;
        if (localBrowsers == null) {
            synchronized (this) {
                localBrowsers = browsers;
                if (localBrowsers == null) {
                    browsers = localBrowsers = getComponent("BrowserUA");
                }
            }
        }
        return localBrowsers;
    }
    private volatile Component browsers;

    /**
     * Returns the crawler {@link Component} that contains a set of 
     * {@link Property properties} and {@link Profile profiles} related to 
     * crawlers (such as: IsCrawler property).
     * <p>
     * Note that for the 'Lite' data file this component will not return any 
     * properties. Some profiles will be returned but no properties associated 
     * with this component will be available.
     * <p>
     * Premium and Enterprise data files contain considerably more data, meaning 
     * more accurate detections and access to the IsCrawler property.
     * <a href="https://51degrees.com/compare-data-options">
     * Compare data options</a>.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return crawler component
     * @throws IOException signals an I/O exception has occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getCrawlers() throws IOException {
        Component localCrawlers = crawlers;
        if (localCrawlers == null) {
            synchronized (this) {
                localCrawlers = crawlers;
                if (localCrawlers == null) {
                    crawlers = localCrawlers = getComponent("Crawler");
                }
            }
        }
        return localCrawlers;
    }
    private volatile Component crawlers;

    /**
     * The copyright notice associated with the data file.
     * 
     * @return copyright notice for the current data file as a string of text.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getCopyright() throws IOException {
        String localCopyright = copyright;
        if (localCopyright == null) {
            synchronized (this) {
                localCopyright = copyright;
                if (localCopyright == null) {
                    copyright = localCopyright = 
                            strings.get(copyrightOffset).toString();
                }
            }
        }
        return localCopyright;
    }

    /**
     * The common name of the data set such as 'Lite', 'Premium' and 
     * 'Enterprise'.
     * 
     * @return name of the data set as a string.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getName() throws IOException {
        String localName = name;
        if (localName == null) {
            synchronized (this) {
                localName = name;
                if (localName == null) {
                    name = localName = strings.get(nameOffset).toString();
                }
            }
        }
        return localName;
    }
    private volatile String name;

    /**
     * Major version of the data file backing the this data set.
     * 
     * @return major version of the data file.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getFormat() throws IOException {
        String localFormat = format;
        if (localFormat == null) {
            synchronized (this) {
                localFormat = format;
                if (localFormat == null) {
                    format = localFormat = strings.get(formatOffset).toString();
                }
            }
        }
        return localFormat;
    }
    
    /**
     * Used to search for a signature from a list of nodes.
     * 
     * @return search instance connected to the list of signatures
     */
    @SuppressWarnings("DoubleCheckedLocking")
    SearchSignatureByNodes getSignatureSearch() {
        SearchSignatureByNodes result = sigantureSearch;
        if (result == null) {
            synchronized (this) {
                result = sigantureSearch;
                if (result == null) {
                    sigantureSearch = result = new SearchSignatureByNodes(
                        signatures);
                }
            }
        }
        return result;
    }
    private volatile SearchSignatureByNodes sigantureSearch;

    /**
     * @return an instance of the profile offset search.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    private SearchProfileOffsetByProfileId getProfileOffsetSearch() {
        SearchProfileOffsetByProfileId result = profileOffsetSearch;
        if (result == null) {
            synchronized (this) {
                result = profileOffsetSearch;
                if (result == null) {
                    profileOffsetSearch = result = 
                            new SearchProfileOffsetByProfileId(profileOffsets);
                }
            }
        }
        return result;
    }
    private volatile SearchProfileOffsetByProfileId profileOffsetSearch;

    /**
     * Get the profiles associated with the {@link Property} name and
     * value which intersects with filterProfiles if provided.
     * @param propertyName the name of the {@link Property} to find as a string.
     * @param valueName the {@link Property Property's} {@link Value} to find
     *                  as a string.
     * @param filterProfiles the list of {@link Profile Profiles} to search in.
     *                       Or null to find all matching
     *                       {@link Profile Profiles}.
     * @return A read-only list of {@link Profile} objects in ascending index
     * order.
     * @throws IOException
     * @throws IllegalArgumentException if the property does not exist.
     */
    public List<Profile> findProfiles(String propertyName, String valueName, List<Profile> filterProfiles) throws IOException {

        Property property =  this.properties.get(propertyName);
        if (property == null) {
            throw new IllegalArgumentException(String.format(
                    "Property '%s' does not exist in the '%s' data set. " +
                            "Upgrade to a different data set which includes the property.",
                    propertyName,
                    this.name));
        }
        return findProfiles(property, valueName, filterProfiles);
    }

    /**
     * Get the profiles associated with the {@link Property} name and
     * value which intersects with filterProfiles if provided.
     * @param property the {@link Property} object to seach with.
     * @param valueName the {@link Property Property's} {@link Value} to find
     *                  as a string.
     * @param filterProfiles the list of {@link Profile Profiles} to search in.
     *                       Or null to find all matching
     *                       {@link Profile Profiles}.
     * @return A read-only list of {@link Profile} objects in ascending index
     * order.
     * @throws IOException
     */
    public List<Profile> findProfiles(Property property, String valueName, List<Profile> filterProfiles) throws IOException {
        return property.findProfiles(valueName, filterProfiles);
    }

    /**
     * Returns an iterable of all {@link Component Components} in the current 
     * data file.
     * 
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return iterable of {@code Component} entities in the current data file.
     */
    public IReadonlyList<Component> getComponents() {
        return components;
    }
    

    /**
     * Returns an iterable list of maps contained within the data file.
     * May contain multiple maps with the same name.
     * 
     * @return a read-only list of all maps contained in the data set.
     */
    public IReadonlyList<Map> getMaps() {
        return maps;
    }

    /**
     * A list of all {@link Property properties} the data set contains.
     * 
     * @return a read-only list of all properties contained in the data set
     */
    public IReadonlyList<Property> getProperties() {
        return properties;
    }
    
    /**
     * All the JavaScript type properties in the data set.
     * 
     * @return array of properties of type JavaScript.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Property[] getJavaScriptProperties() {
        if (javaScriptProperties == null) {
            synchronized(this) {
                if (javaScriptProperties == null) {
                    List<Property> tempList = new ArrayList<Property>();
                    for (Property property : getProperties()) {
                        if (property.valueType == 
                                Property.PropertyValueType.JAVASCRIPT) {
                            tempList.add(property);
                        }
                    }
                    Property[] localJavaScriptProperties = 
                            new Property[tempList.size()];
                    tempList.toArray(localJavaScriptProperties);
                    javaScriptProperties = localJavaScriptProperties;
                }
            }
        }
        return javaScriptProperties;
    }
    
    /**
     * All the JavaScript properties that relate to Profile Value Overrides.
     * 
     * @return array of properties of type JavaScript.
     * @throws java.io.IOException
     */    
    @SuppressWarnings("DoubleCheckedLocking")
    public Property[] getPropertyValueOverrideProperties() throws IOException {
        if (pvoProperties == null) {
            synchronized(this) {
                if (pvoProperties == null) { 
                    List<Property> tempList = new ArrayList<Property>(); 
                    for (Property property : getJavaScriptProperties()) {
                        if (property.getCategory().equals(
                            DetectionConstants.
                            PROPERTY_VALUE_OVERRIDE_CATEGORY)) {
                            tempList.add(property);
                        }
                    }
                    Property[] localPvoProperties = 
                            new Property[tempList.size()];
                    tempList.toArray(localPvoProperties);
                    pvoProperties = localPvoProperties;
                }
            }
        }
        return pvoProperties;
    }

    /**
     * A list of all {@link Value values} the data set contains. A value is 
     * the specific content of a {@link Property} for a specific {@link Profile}.
     * For example: IsMobile can have value 'True' for profile A but have a 
     * value 'False' for profile B.
     * 
     * @return a read-only list of values contained in the data set
     */
    public IReadonlyList<Value> getValues() {
        return values;
    }

    /**
     * List of {@link Signature signatures} the data set contains. Signatures 
     * contain one {@link Profile} per {@link Component} and rank values.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return a read-only list of all signatures contained in the data set
     */
    public IReadonlyList<Signature> getSignatures() {
        return signatures;
    }
    
    /**
     * List of nodes the data set contains.
     * 
     * @return a read-only list of nodes contained in the data set
     */
    public IReadonlyList<Node> getNodes() {
        return nodes;
    }

    /**
     * A list of all the possible {@link Profile profiles} contained in the 
     * current data set. Profiles contain multiple {@link Value values}. Each 
     * profile belongs to one {@link Component} and can be assigned to many 
     * {@link Signature signatures}.
     * <p>
     * For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     * 
     * @return a read-only list of all profiles contained in the data set
     */
    public IReadonlyList<Profile> getProfiles() {
        return profiles;
    }

    /**
     * The minimum length of a User-Agent string.
     * 
     * @return The minimum length of a User-Agent string.
     */
    public short getMinUserAgentLength() {
        return minUserAgentLength;
    }

    /**
     * Returns a list of integers that represent signature node offsets.
     * 
     * @return list of integers that represent signature node offsets.
     */
    public ISimpleList getSignatureNodeOffsets() {
        return signatureNodeOffsets;
    }
    
    /**
     * Returns a list of integers that represent ranked signature indexes.
     * 
     * @return a list of integers that represent ranked signature indexes.
     */
    public ISimpleList getNodeRankedSignatureIndexes() {
        return nodeRankedSignatureIndexes;
    }
    
    /**
     * Creates a list of HTTP headers if one does not already exist.
     * Most devices can be identified by examining the HTTP User-Agent header 
     * alone while other devices and (or) browsers sometimes set an extra 
     * header that can be used to improve detection. This list represents 
     * the headers that are important to device detection process.
     * 
     * @return list of HTTP headers as Strings.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String[] getHttpHeaders() throws IOException {
        String[] localHttpHeaders = httpHeaders;
        if (localHttpHeaders == null) {
            synchronized(this) {
                localHttpHeaders = httpHeaders;
                if (localHttpHeaders == null) {
                    List<String> tempList = new ArrayList<String>();
                    for (Component c : components) {
                       for (String s : c.getHttpheaders()) {
                           if (!tempList.contains(s)) {
                               tempList.add(s);
                           }
                       }
                    }
                    localHttpHeaders = new String[tempList.size()];
                    tempList.toArray(localHttpHeaders);
                    httpHeaders = localHttpHeaders;
                }
            }
        }
        return localHttpHeaders;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile String[] httpHeaders;
    
    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     * <p>
     * Do not use this method as it is part of the internal logic.
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
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
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
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void initNodes() throws IOException {
        for (Node node : nodes) {
            node.init();
        }
    }

    /**
     * Preloads profiles to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void initProfiles() throws IOException {
        for (Profile profile : profiles) {
            profile.init();
        }
    }

    /**
     * Preloads components to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void initComponents() throws IOException {
        for (Component component : getComponents()) {
            component.init();
        }
    }

    /**
     * Preloads properties to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void initProperties() throws IOException {
        for (Property property : getProperties()) {
            property.init();
        }
    }

    /**
     * Preloads values to speed retrieval later at the expense of memory.
     * This method doesn't need to be used if {@code init()} has already been 
     * called.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void initValues() throws IOException {
        for (Value value : values) {
            value.init();
        }
    }

    /**
     * Retrieves {@link Component} by name if a component with such name exists.
     * Method can return null if no component with required name was found; use 
     * a null check when invoking.
     * <p>
     * {@code Component} contains a list of {@link Profile} and {@link Property} 
     * that are related to this component. For more information see: 
     * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
     * 51Degrees Pattern Data Model</a>
     *
     * @param componentName name of the required {@code Component} as string.
     * @return The {@code Component} matching the name, or null.
     * @throws IOException if there was a problem accessing data file.
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
     * found. Method is capable of returning a null, so add a check for null 
     * when invoking.
     * 
     * @param propertyName name of the property to find, should not be null.
     * @return Property object or null if no property with requested name exists
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Property get(String propertyName) throws IOException {
        return this.properties.get(propertyName);
    }

    /**
     * Returns the number of {@link Profile profiles} each signature can contain. 
     * The number of profiles should be equal to the number of 
     * {@link Component components}, in other words: each signature has one 
     * profile per component.
     * 
     * @return The number of profiles each signature can contain.
     */
    public int getProfilesCount() {
        return signatureProfilesCount;
    }

    
    /**
     * Returns the number of nodes each signature can contain.
     * 
     * @return The number of nodes each signature can contain.
     */
    public int getNodesCount() {
        return signatureNodesCount;
    }

    /**
     * Searches the list of profile IDs and returns the profile if the profile
     * id is valid.
     *
     * @param profileId Id of the profile to be found, not negative.
     * @return Profile related to the id, or null if none found.
     * @throws IOException signals an I/O exception occurred
     */
    public Profile findProfile(int profileId) throws IOException {
        int index = getProfileOffsetSearch().binarySearch(profileId);
        return index < 0 ? null : profiles.get(
                profileOffsets.get(index).getOffset());
    }

    /**
     * Disposes of the data set releasing any file locks.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public void close() throws IOException {
        disposed = true;
    }

    /**
     * Returns a list of signature indexes ordered in ascending order of rank.
     * 
     * @return A list of signature indexes ordered in ascending order of rank.
     */
    public ISimpleList getRankedSignatureIndexes() {
        return rankedSignatureIndexes;
    } 
    
    /**
     * Used to search for a signature from a list of nodes.
     */
    static class SearchSignatureByNodes 
        extends SearchBase<Signature, List<Node>, IReadonlyList<Signature>> {

        private final IReadonlyList<Signature> signatures;
        
        SearchSignatureByNodes(IReadonlyList<Signature> signatures) {
            this.signatures = signatures;
        }
        
        @Override
        protected int getCount(IReadonlyList<Signature> list) {
            return list.size();
        }

        @Override
        protected Signature getValue(IReadonlyList<Signature> list, int index) 
                throws IOException {
            return list.get(index);
        }

        @Override
        protected int compareTo(Signature item, List<Node> nodes) 
                throws IOException {
            return item.compareTo(nodes);
        }
        
        SearchResult binarySearchResults(List<Node> nodes) throws IOException {
            return super.binarySearchResults(signatures, nodes);
        }
    }
    
    /**
     * Used to search for a profile offset from a profile id.
     */
    private static class SearchProfileOffsetByProfileId 
        extends SearchBase<ProfileOffset, Integer, IReadonlyList<ProfileOffset>> {

        private final IReadonlyList<ProfileOffset> profileOffsets;
        
        SearchProfileOffsetByProfileId(
                IReadonlyList<ProfileOffset> profileOffsets) {
            this.profileOffsets = profileOffsets;
        }
        
        @Override
        protected int getCount(IReadonlyList<ProfileOffset> list) {
            return list.size();
        }

        @Override
        protected ProfileOffset getValue(
                IReadonlyList<ProfileOffset> list, 
                int index) 
                throws IOException {
            return list.get(index);
        }

        @Override
        protected int compareTo(ProfileOffset item, Integer profileId) 
                throws IOException {
            return item.getProfileId() - profileId;
        }
        
        int binarySearch(Integer profileId) throws IOException {
            return super.binarySearch(profileOffsets, profileId);
        }
    }

    /**
     * Reset any caches in use for this Dataset. Only {@link IndirectDataset}
     * contains caches.
     */
    public void resetCache() {
        //Do nothing in this implementation.
    }


    // <editor-fold defaultstate="collapsed" desc="Deprecated methods">
    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    private static long getSwitches(Object list) {
        return 0;
    }
    
    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getRankedSignatureCacheSwitches() {
        return getSwitches(rankedSignatureIndexes);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageRankedSignatureCacheMisses() {
        return getPercentageMisses(rankedSignatureIndexes);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getSignatureCacheSwitches() {
        return 0;
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getNodeCacheSwitches() {
        return getSwitches(nodes);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getStringsCacheSwitches() {
        return getSwitches(strings);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getProfilesCacheSwitches() {
        return getSwitches(profiles);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public long getValuesCacheSwitches() {
        return getSwitches(values);
    }
    
    /**
     * Method is deprecated and should not be used. Use get( StringpropertyName)
     * instead.
     * Method searches for a property with the given name and returns the 
     * Property if found. Returns null otherwise.
     * @param propertyName name of the property to find as a string.
     * @return Property object or null if no property with requested name exists
     * @throws IOException if there was a problem accessing data file.
     */
    @Deprecated
    public Property getPropertyByName(String propertyName) throws IOException {
        return this.properties.get(propertyName);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    private static double getPercentageMisses(Object list) {
        return 0;
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageSignatureCacheMisses() {
        return getPercentageMisses(signatures);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageNodeCacheMisses() {
        return getPercentageMisses(nodes);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageStringsCacheMisses() {
        return getPercentageMisses(strings);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageProfilesCacheMisses() {
        return getPercentageMisses(profiles);
    }

    /**
     * This method no longer supported. (See {@link IndirectDataset#getCache(CacheType)}.
     */
    @Deprecated
    public double getPercentageValuesCacheMisses() {
        return getPercentageMisses(values);
    }    
    
    //</editor-fold>
}