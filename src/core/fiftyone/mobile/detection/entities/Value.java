package fiftyone.mobile.detection.entities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fiftyone.mobile.detection.Dataset;
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
 * A value associated with a property and component within the dataset.
 * <p>
 * Every property can return one of many values, or multiple values if it's a list 
 * property. For example; SupportedBearers returns a list of the bearers that the
 * device can support.
 * <p>
 * The value class contains all the information associated with the value including
 * the display name, and also other information such as a description or URL to find
 * out additional information. These other properties can be used by UI developers to
 * provide users with more information about the meaning and intended use of a value.
 * <p>
 * For more information see http://51degrees.mobi/Support/Documentation/Java
 */

/**
 * A value associated with a property and component within the dataset.
 */
public class Value extends BaseEntity implements Comparable<Value> {
	
	public static final int RECORD_LENGTH = (4 * 3) + 2;
	
	/**
	 * The name of the value.
	 * @throws IOException 
	 */
	public String getName() throws IOException {
		if (name == null) {
			synchronized (this) {
				if (name == null) {
					name = getDataSet().strings.get(nameIndex).toString();
				}
			}
		}
		return name;
	}

	private String name;
	private final int nameIndex;

	/**
	 * Array containing the signatures that the value is associated with.
	 * @throws IOException 
	 */
	public Signature[] getSignatures() throws IOException {
		if (signatures == null) {
			synchronized (this) {
				if (signatures == null) {
					signatures = GetSignatures();
				}
			}
		}
		return signatures;
	}

	private Signature[] signatures;

	/**
	 * Array containing the profiles the value is associated with.
	 * @throws IOException 
	 */
	public Profile[] getProfiles() throws IOException {
		if (profiles == null) {
			synchronized (this) {
				if (profiles == null) {
					profiles = GetProfiles();
				}
			}
		}
		return profiles;
	}

	private Profile[] profiles;

	/**
	 * The property the value relates to.
	 * @throws IOException 
	 */
	public Property getProperty() throws IOException {
		if (property == null) {
			synchronized (this) {
				if (property == null) {
					property = getDataSet().getProperties().get(propertyIndex);
				}
			}
		}
		return property;
	}

	private Property property;
	final int propertyIndex;

	/**
	 * The component the value relates to.
	 * @throws IOException 
	 */
	public Component getComponent() throws IOException {
		return getProperty().getComponent();
	}

	/**
	 * A description of the value suitable to be displayed to end users via a
	 * user interface.
	 * @throws IOException 
	 */
	public String getDescription() throws IOException {
		if (descriptionIndex >= 0 && description == null) {
			synchronized (this) {
				if (description == null) {
					description = getDataSet().strings
							.get(descriptionIndex).toString();
				}
			}
		}
		return description;
	}

	private String description;
	private final int descriptionIndex;

	/**
	 * A url to more information about the value.
	 * @throws IOException 
	 */
	public URL getUrl() throws IOException {
		if (urlIndex >= 0 && url == null) {
			synchronized (this) {
				if (url == null) {
					url = new URL(getDataSet().strings.get(urlIndex)
							.toString());
				}
			}
		}
		return url;
	}

	private URL url;
	private final int urlIndex;

	/**
	 * Constructs a new instance of Value
	 * 
	 * @param dataSet
	 *            The data set the value is contained within
	 * @param index
	 *            The index in the data structure to the value
	 * @param reader
	 *            Reader connected to the source data structure and positioned
	 *            to start reading
	 */
	public Value(Dataset dataSet, int index, BinaryReader reader) {
		super(dataSet, index);

		this.propertyIndex = reader.readInt16();
		this.nameIndex = reader.readInt32();
		this.descriptionIndex = reader.readInt32();
		this.urlIndex = reader.readInt32();
	}

	/**
	 * Called after the entire data set has been loaded to ensure any further
	 * initialisation steps that require other items in the data set can be
	 * completed. The Profiles and Signatures are not initialised as they are
	 * very rarely used and take a long time to initialise.
	 * @throws IOException 
	 */
	public void init() throws IOException {
		name = getDataSet().strings.get(nameIndex).toString();
		property = getDataSet().getProperties().get(propertyIndex);
		if (descriptionIndex >= 0) {
			description = getDataSet().strings.get(descriptionIndex)
					.toString();
		}
		if (urlIndex >= 0) {
			try {
				url = new URL(getDataSet().strings.get(urlIndex)
						.toString());
			} catch (MalformedURLException e) {
				url = null;
			}
		}
	}

	/**
	 * Gets all the profiles associated with the value.
	 * 
	 * @return Returns the profiles from the component that relate to this value
	 * @throws IOException 
	 */
	private Profile[] GetProfiles() throws IOException {
		// return Component.Profiles.Where(i =>
		// BinarySearch(i.Values, this) >= 0).ToArray();
		List<Profile> profiles = new ArrayList<Profile>();

		for (Profile profile : getComponent().getProfiles()) {
			if (binarySearch(profile.getValues(), getIndex()) >= 0) {
				profiles.add(profile);
			}
		}

		return profiles.toArray(new Profile[profiles.size()]);
	}

	/**
	 * Gets all the signatures associated with the value.
	 * 
	 * @return Returns the signatures associated with the value
	 * @throws IOException 
	 */
	private Signature[] GetSignatures() throws IOException {
		// Get a distinct list of signatures.
		List<Integer> list = new ArrayList<Integer>();
		for (Profile profile : getProfiles()) {
			for (Integer signatureIndex : profile.getSignatureIndexes()) {
				int index = java.util.Collections.binarySearch(list, signatureIndex);
				if (index < 0)
					list.add(~index, signatureIndex);
			}
		}

		Signature[] result = new Signature[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = getDataSet().getSignatures().get(i);
		}
		return result;
	}

	/**
	 * Compares this value to another using the index field if they're in the
	 * same list other wise the name value.
	 * 
	 * @param other
	 *            The value to be compared against
	 * @return Indication of relative value based on index field
	 */
	public int compareTo(Value other) {
		if (getDataSet() == other.getDataSet()) {
			return getIndex() - other.getIndex();
		}
		try {
			return getName().compareTo(other.getName());
		} catch (IOException e) {
			return 0;
		}
	}

	/**
	 * Returns the value as a string.
	 * 
	 * @return the value name as a string
	 */
	@Override
	public String toString() {
		try {
			return getName();
		} catch (IOException e) {
			return super.toString();
		}
	}

	/**
	 * Returns the value as a number.
	 * @throws IOException 
	 */
	public double toDouble() throws IOException {
		if (asNumber == null) {
			synchronized (this) {
				if (asNumber == null) {
					try {
						asNumber = Double.parseDouble(getName());
					} catch (NumberFormatException e) {
						if (this != getProperty().getDefaultValue())
							asNumber = getProperty().getDefaultValue().toDouble();
						else
							asNumber = (double) 0;
					}
				}
			}
		}
		return (double) asNumber;
	}

	private Double asNumber;

	/**
	 * Returns the value as a boolean.
	 * @throws IOException 
	 */
	public boolean toBool() throws IOException {
		if (asBool == null) {
			synchronized (this) {
				if (asBool == null) {
					asBool = Boolean.parseBoolean(getName());
				}
			}
		}
		return (boolean) asBool;
	}

	private Boolean asBool;

	/**
	 * Returns true if the value is the null value for the property.
	 * If the property has no null value false is always returned.
	 * @return
	 * @throws IOException 
	 */
	public boolean getIsDefault() throws IOException {
		Value defaultValue = property.getDefaultValue();
		if (defaultValue != null)
			return this.getName() == defaultValue.getName();
		return false;
	}
}
