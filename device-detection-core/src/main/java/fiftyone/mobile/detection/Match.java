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
package fiftyone.mobile.detection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.properties.DetectionConstants;
import fiftyone.properties.MatchMethods;

/**
 * Contains all the information associated with the device detection and matched
 * result.
 * 
 * The match property can be used to request results from the match using 
 * the accessor provided with a Property or the string name of the property. 
 * 
 * The Signature the target device match against can be returned along with 
 * the associated profiles. 
 * 
 * Statistics associated with the match can also be returned. For example: the 
 * Elapsed property returns the time taken to perform the match. The Confidence 
 * property provides a value to indicate the differences between the match 
 * result and the target user agent.
 *
 * For more information see http://51degrees.mobi/Support/Documentation/Java
 */
/**
 * Generate when a device detection is requested to include the signature
 * matched, the confidence of the match and the method used to obtain the match.
 */
public class Match {

    /**
     * Instance of the provider used to create the match.
     */
    final Provider provider;
    
    /**
     * Current working state of the matching process.
     */
    final MatchState state;
    
    /**
     * Sets the result of the match explicitly.
     * @param value of the match result to set.
     */
    void setResult(MatchResult value) {
        matchResult = value;
    }
    MatchResult getResult() {
        return matchResult;
    }
    private MatchResult matchResult;
    
    /**
     * @return dataset used to create the match.
     */
    public Dataset getDataSet() {
        return provider.dataSet;
    }
    
    /**
     * @return target user agent string used for detection
     */
    public String getTargetUserAgent() {
        return getResult().getTargetUserAgent();
    }

    /**
     * @return the elapsed time for the match.
     */
    public long getElapsed() {
        return getResult().getElapsed();
    }
    
    /**
     * @return signature with closest match to the user agent provided.
     */
    public Signature getSignature() {
        return getResult().getSignature();
    }
    
    /**
     * @return method used to obtain match.
     */
    public MatchMethods getMethod() {
        return getResult().getMethod();
    }
    
    /**
     * @return number of closest signatures returned for evaluation.
     */
    public int getClosestSignaturesCount() {
        return getResult().getClosestSignaturesCount();
    }    
        
    /**
     * @return integer representing number of signatures compared against 
     * the User-Agent if closest match method was used.
     */
    public int getSignaturesCompared() {
        return getResult().getSignaturesCompared();
    }    
    
    /**
     * @return integer representing number of signatures read during detection
     */
    public int getSignaturesRead() {
        return getResult().getSignaturesRead();
    }
    
    /**
     * @return integer representing number of root node checked
     */
    public int getRootNodesEvaluated() {
        return getResult().getRootNodesEvaluated();
    }
    
    /**
     * @return integer representing the number of nodes checked
     */
    public int getNodesEvaluated() {
        return getResult().getNodesEvaluated();
    }    
    
    /**
     * @return integer representing number of strings read for the match
     */
    public int getStringsRead() {
        return getResult().getStringsRead();
    }    
    
    /**
     * Array of profiles associated with the device that was found.
     * @return array of profiles associated with the device that was found
     * @throws IOException indicates an I/O exception occurred
     */
    public Profile[] getProfiles() throws IOException {
        return overriddenProfiles == null ? 
                getResult().getProfiles() : 
                getOverriddenProfiles();
    }
    
    /**
     * Array of profiles associated with the device that may have been 
     * overridden for this instance of match.
     * This property is needed to ensure that other references to the instance 
     * of MatchResult are not altered when overriding profiles.
     * @return profiles set specifically for this match.
     * @throws IOException 
     */
    Profile[] getOverriddenProfiles() throws IOException {
        Profile[] result = overriddenProfiles;
        if (result == null && getSignature() != null) {
            synchronized (this) {
                result = overriddenProfiles;
                if (result == null) {
                    result = new Profile[getResult().getProfiles().length];
                    System.arraycopy(
                            getResult().getProfiles(), 0,
                            result, 0, result.length);
                    overriddenProfiles = result;
                }
            }
        }
        return result;
    }
    volatile private Profile[] overriddenProfiles;       
        
    /**
     * The numeric difference between the target user agent and the 
     * match. Numeric sub strings of the same length are compared 
     * based on the numeric value. Other character differences are 
     * compared based on the difference in ASCII values of the two
     * characters at the same positions.
     * @return numeric difference
     */    
    public int getDifference() {
        int score = getResult().getLowestScore();
        return score >= 0 ? score : 0;
    }    
    
    /**
     * The unique id of the device represented by the match.
     * @return string representing unique id of device
     * @throws IOException signals an I/O exception occurred
     */
    public String getDeviceId() throws IOException {
        String result;
        if (getSignature() != null) {
            result = getSignature().getDeviceId();
        }
        else {
            String[] profileIds = new String[getProfiles().length];
            for (int i = 0; i < profileIds.length; i++) {
                profileIds[i] = Integer.toString(getProfiles()[i].profileId);
            }
            result = Utilities.joinString(DetectionConstants.PROFILE_SEPARATOR, 
                                          profileIds);
        }
        return result;
    }
    
    /**
     * @return user agent of the matching device with irrelevant characters 
     * removed.
     */
    public String getUserAgent() {
        return getSignature() != null ? getSignature().toString() : null;
    }
    
    /**
     * This method is not memory efficient and should be avoided as the Match 
     * class now exposes an getValues methods keyed on property name.
     * @return the results of the match as a sorted list of property names 
     * and values.
     * @throws IOException
     * @deprecated use getValues methods
     */
    @Deprecated
    public Map<String, String[]> getResults() throws IOException {
        Map<String, String[]> results = new HashMap<String, String[]>();

        // Add the properties and values first.
        for (Profile profile : getProfiles()) {
            if (profile != null) {
                for (Property property : profile.getProperties()) {
                    Value[] values = profile.getValues();
                    List<String> strings = new ArrayList<String>();
                    for (Value value : values) {
                        if (value.getProperty() == property) {
                            strings.add(value.getName());
                        }
                    }
                    results.put(
                        property.getName(),
                        strings.toArray(new String[strings.size()]));
                }
            }
        }

        results.put(DetectionConstants.DIFFERENCE_PROPERTY,
                new String[]{Integer.toString(getDifference())});
        results.put(DetectionConstants.NODES,
                new String[]{toString()});

        // Add any other derived values.
        results.put(DetectionConstants.DEVICEID,
                new String[]{getDeviceId()});

        return results;
    }
    

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param property The property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(Property property) throws IOException {
        Values value = null;

        if (property != null) {
            // Get the property value from the profile returned
            // from the match.
            for (Profile profile : getProfiles()) {
                if (profile.getComponent().getComponentId()
                        == property.getComponent().getComponentId()) {
                    value = profile.getValues(property);
                    break;
                }
            }

            // If the value has not been found use the default profile.
            if (value == null) {
                value = property.getComponent().getDefaultProfile().getValues(property);
            }
        }

        return value;
    }

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param propertyName The property name whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(String propertyName) throws IOException {
        return getValues(getDataSet().get(propertyName));
    }    

    /**
     * Constructs a new detection match ready to be used.
     *
     * @param provider data set to be used for this match
     */
    Match(Provider provider) {
        this.provider = provider;
        this.state = new MatchState(this);
        matchResult = state;
    }

    /**
     * Constructs a new detection match ready to be used to identify the
     * profiles associated with the target user agent.
     *
     * @param dataSet data set to be used for this match
     * @param targetUserAgent user agent to identify
     * @throws UnsupportedEncodingException indicates an Unsupported Encoding 
     * exception occurred
     */
    Match(Provider provider, String targetUserAgent)
            throws UnsupportedEncodingException {
        this(provider);
        this.state.init(targetUserAgent);
    }
    
    /**
     * Resets the match instance ready for further matching.
     */
    void reset() {
        this.state.reset();
        this.overriddenProfiles = null;
    }
    
    /**
     * Override the profiles found by the match with the profileId provided.
     *
     * @param profileId The ID of the profile to replace the existing component
     * @throws IOException indicates an I/O exception occurred
     */
    public void updateProfile(int profileId) throws IOException {
        // Find the new profile from the data set.
        Profile newProfile = getDataSet().findProfile(profileId);
        if (newProfile != null) {
            // Loop through the profiles found so far and replace the
            // profile for the same component with the new one.
            for (int i = 0; i < getOverriddenProfiles().length; i++) {
                // Compare by component Id incase the stream data source is
                // used and we have different instances of the same component
                // being used.
                if (getOverriddenProfiles()[i].getComponent().getComponentId()
                        == newProfile.getComponent().getComponentId()) {
                    getOverriddenProfiles()[i] = newProfile;
                    break;
                }
            }
        }
    }
    
    
    /**
     * A string representation of the nodes found from the target user agent.
     *
     * @return a string representation of the match
     */
    @Override
    public String toString() {
        if (state.getNodesList() != null && state.getNodes().length > 0) {
            try {
                byte[] value = new byte[getTargetUserAgent().length()];
                for (Node node : state.getNodes()) {
                    node.addCharacters(value);
                }
                for (int i = 0; i < value.length; i++) {
                    if (value[i] == 0) {
                        value[i] = (byte) '_';
                    }
                }
                return new String(value, "US-ASCII");
            } catch (IOException e) {
                return super.toString();
            }
        }
        return super.toString();
    }
}