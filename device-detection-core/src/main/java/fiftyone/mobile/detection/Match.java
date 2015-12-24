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
import java.nio.ByteBuffer;

/**
 * Contains detection results with all the information relevant to the matched 
 * device.
 * <p>
 * Access {@link Property} values using the 
 * {@link #getValues(java.lang.String)} method for property names and 
 * {@link #getValues(fiftyone.mobile.detection.entities.Property)} for property 
 * objects. For example: {@code match.getValues("IsMobile");}
 * <p>
 * All values are returned as strings unless you request a specific format:
 * {@code match.getValues("ScreenPixelsWidth").toDouble();}
 * <p>
 * If you want a more general device information like the rank you should use 
 * {@link #getSignature()} method which returns a {@link Signature} object this 
 * device relates to.
 * <p>
 * Match also provides various metrics properties: device Id, method used, 
 * difference and rank.
 * <ul>
 *  <li>Device id consists of four 
 *  {@link fiftyone.mobile.detection.entities.Component components} where each 
 *  component is the Id of the {@link Profile} matched by the detector. 
 *  Access like:
 *  {@code match.getDeviceId();}
 *  <li>Detection method refers to the algorithm used for this match. Use like: 
 *  {@code match.getMethod();}
 *  <p>For more information on the detection methods see: 
 *  <a href="https://51degrees.com/support/documentation/pattern">
 *  how Pattern device detection works</a>.
 *  <li>Difference indicates the level of confidence in the current detection 
 *  results. Used in conjunction with the detection method and only makes sense 
 *  if "Numeric", "Closest" or "Nearest" algorithm was used. The higher the 
 *  number the lower the confidence.
 *  <li>Rank provides information on the level of popularity of the User-Agent 
 *  used to create this match. The lower the rank the more popular the 
 *  User-Agent is. Popularity is determined by 51Degrees based on our internal 
 *  usage statistics.
 * </ul>
 * <p>
 * Keeping the data file up to date improves the overall quality of detection 
 * as the 51Degrees data team adds (on average) 200 new devices to our database 
 * each week. With Premium and Enterprise data files can benefit from the 
 * <a href="https://51degrees.com/compare-data-options">
 * automatic data updates</a> as well as a wider range of properties and more 
 * devices.
 * <p>
 * This object should not be created manually in the external code. Use one of 
 * the match methods in the {@link Provider} class to obtain a match object 
 * with data members initialised.
 * <p>
 * For more information see https://51degrees.com/Support/Documentation/Java
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
     * @return {@link Dataset} used to create the match.
     */
    public Dataset getDataSet() {
        return provider.dataSet;
    }
    
    /**
     * @return target User-Agent string used for detection
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
     * Returns a {@link Signature} that best fits the provided User-Agent string.
     * A signature can be used to retrieve {@link Profile profiles} and rank.
     * 
     * @return {@link Signature} with best match to the User-Agent provided.
     */
    public Signature getSignature() {
        return getResult().getSignature();
    }
    
    /**
     * Returns the detection method used to obtain this object. Method used 
     * reflects the confidence of the detector in the accuracy of the current 
     * match.
     * <ul>
     *  <li>"Exact" means the detector is confident the results are accurate. 
     *  <li>"None" means the User-Agent provided is fake.
     *  <li>"Numeric", "Closest" and "Nearest" will always return a result but 
     *  the {@link #getDifference()} should be used to assess the accuracy of 
     *  the detection. The higher the number the less confident the detector is.
     * </ul>
     * <p>
     * With Premium or Enterprise data files you will see more "Exact" 
     * detections as the number of device combinations available in these files 
     * is significantly larger than in the "Lite" data file.
     * <a href="https://51degrees.com/compare-data-options">
     * Compare data options</a>
     * <p>
     * For more information on detection methods see: 
     * <a href="https://51degrees.com/support/documentation/pattern">
     * how Pattern device detection works</a>
     * 
     * @return {@link MatchMethods} used to obtain match.
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
     * @return the number of nodes found by the match.
     */
    public int getNodesFound() {
        return getResult().getNodes().length;
    }
    
    /**
     * @return integer representing number of strings read for the match
     */
    public int getStringsRead() {
        return getResult().getStringsRead();
    }    
    
    /**
     * Array of {@link Profile profiles} associated with the device that was 
     * found. Profiles can then be used to retrieve {@link Property properties} 
     * and {@link Value values}.
     * 
     * @return array of {@link Profile profiles} associated with the device 
     * that was found.
     * @throws IOException if there was a problem accessing data file.
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
     * 
     * @return profiles set specifically for this match.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
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
    @SuppressWarnings("VolatileArrayField")
    private volatile Profile[] overriddenProfiles;       
        
    /**
     * The numeric difference between the target User-Agent and the match. 
     * Numeric sub strings of the same length are compared based on the numeric 
     * value. Other character differences are compared based on the difference 
     * in ASCII values of the two characters at the same positions.
     * 
     * @return numeric difference.
     */    
    public int getDifference() {
        int score = getResult().getLowestScore();
        return score >= 0 ? score : 0;
    }    
    
    /**
     * The unique id of the device represented by the match. Id is composed of 
     * several {@link Profile profiles} separated by hyphen symbol. One profile 
     * is chosen per each {@link fiftyone.mobile.detection.entities.Component 
     * component}.
     * <p>
     * Device Id can be stored for future use and the relevant 
     * {@link Property properties} and {@link Value values} restored using the 
     * {@link Provider#matchForDeviceId(java.lang.String)} method.
     * 
     * @return string representing unique id of device
     * @throws IOException if there was a problem accessing data file.
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
     * Id of the device represented as an array of bytes. Unlike the String Id 
     * this Id only contains integers and no hyphen separators.
     * <p>
     * To obtain the unique profile IDs wrap the byte array in a ByteBuffer and 
     * use {@code getInt()} repeatedly.
     * <p>
     * To obtain a {@link Match} with the corresponding 
     * {@link Property properties} and {@link Value values} use the 
     * {@link Provider#matchForDeviceId(byte[])}.
     * 
     * @return Profile Id represented as byte array. Note that Id separators 
     * such as "-" are not part of the byte array, only the integer IDs are.
     * @throws IOException if there was a problem accessing the data file.
     */
    public byte[] getDeviceIdAsByteArray() throws IOException {
        // Allocate enough bytes to store Id for every profile.
        // Integer.SIZE divided by 8 as the size is in bits.
        byte[] result = new byte[(getProfiles().length * Integer.SIZE / 8)];
        ByteBuffer bb = ByteBuffer.wrap(result);
        for (Profile tempProfile : getProfiles()) {
            bb.putInt(tempProfile.profileId);
        }
        return result;
    }
    
    /**
     * @return User-Agent of the matching device with irrelevant characters 
     * removed.
     */
    public String getUserAgent() {
        return getSignature() != null ? getSignature().toString() : null;
    }
    
    /**
     * This method is not memory efficient and should be avoided as the Match 
     * class now exposes an getValues methods keyed on property name.
     * 
     * @return the results of the match as a sorted list of property names 
     * and values.
     * @throws IOException if there was a problem accessing data file.
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
     * Gets the {@link Values} associated with the property name using the 
     * profiles found by the match. If matched profiles don't contain a value 
     * then the default profiles for each of the components are also checked.
     *
     * @param property The property whose values are required.
     * @return Array of the values associated with the property, or null if the
     * property does not exist.
     * @throws IOException if there was a problem accessing data file.
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
     * Gets the {@link Values} associated with the property name using the 
     * profiles found by the match. If matched profiles don't contain a value 
     * then the default profiles for each of the components are also checked.
     *
     * @param propertyName The property name whose values are required.
     * @return Array of the values associated with the property, or null if the
     * property does not exist.
     * @throws IOException if there was a problem accessing data file.
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
     * profiles associated with the target User-Agent.
     *
     * @param dataSet data set to be used for this match
     * @param targetUserAgent User-Agent to identify
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
     * A string representation of the nodes found from the target User-Agent.
     *
     * @return a string representation of the match.
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