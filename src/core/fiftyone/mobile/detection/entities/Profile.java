package fiftyone.mobile.detection.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
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
 * Represents a collection of properties and values relating to a profile which
 * in turn relates to a component.
 * <p>
 * Each Signature relates to one profile for each component.
 */
public class Profile extends BaseEntity implements Comparable<Profile> {
	
	/**
	 * Comparator used to order the properties by descending display order.
	 */
	private static final Comparator<Value> propertyComparator = new Comparator<Value>() {
		public int compare(Value v1, Value v2) {
			try {
				if (v1.getProperty().displayOrder < v2.getProperty().displayOrder)
					return 1;
				if (v1.getProperty().displayOrder > v2.getProperty().displayOrder)
					return -1;
				return 0;
			} catch (IOException e) {
				return 0;
			}
		}
	};
	
	/**
	 * Unique Id of the profile. Does not change between different data sets.
	 */
	public final int profileId;

	/**
	 * A list of the indexes of the values associated with the profile.
	 */
	private final int[] valueIndexes;

	/**
	 * Gets the values associated with the property name.
	 * 
	 * @param propertyName
	 *            Name of the property whose values are required
	 * @return Array of the values associated with the property, or null if the
	 *         property does not exist
	 * @throws IOException 
	 */
	public Values getValues(String propertyName) throws IOException {
		return getValues(getDataSet().get(propertyName));
	}

	/**
	 * Gets the values associated with the property.
	 * 
	 * @param property
	 *            The property whose values are required
	 * @return Array of the values associated with the property, or null if the
	 *         property does not exist
	 */
	public Values getValues(Property property) throws IOException {
		// Does the storage structure already exist?
		if (nameToValues == null) {
			synchronized (this) {
				if (nameToValues == null) {
					nameToValues = new SortedList<String, Values>();
				}
			}
		}

		// Do the values already exist for the property?
		synchronized (nameToValues) {
			// Values values;
			Values values = nameToValues.get(property.getName());
			if (values != null) {
				return values;
			}

			// Create the list of values.
			List<Value> vals = new ArrayList<Value>();
			for (Value value : getValues()) {
				if (value.getProperty() == property) {
					vals.add(value);
				}
			}
			values = new Values(property, vals);

			if (values.size() == 0)
				values = null;

			// Store for future reference.
			nameToValues.add(property.getName(), values);

			return values;
		}
	}

	private SortedList<String, Values> nameToValues;

	/**
	 * Array of signatures associated with the profile.
	 */
	public Signature[] Signatures() throws IOException {
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
	private final int[] signatureIndexes;

	/**
	 * The component the profile belongs to.
	 */
	public Component getComponent() throws IOException {
		if (component == null) {
			synchronized (this) {
				if (component == null) {
					component = getDataSet().getComponents()
							.get(componentIndex);
				}
			}
		}
		return component;
	}

	private Component component;
	private final int componentIndex;

	/**
	 * An array of values associated with the profile.
	 */
	public Value[] getValues() throws IOException {
		if (values == null) {
			synchronized (this) {
				if (values == null) {
					values = GetValues();
				}
			}
		}
		return values;
	}

	private Value[] values;

	/**
	 * An array of properties associated with the profile.
	 */
	public Property[] getProperties() throws IOException {
		if (properties == null) {
			synchronized (this) {
				if (properties == null) {
					properties = GetProperties();
				}
			}
		}
		return properties;
	}

	private Property[] properties = null;

	/**
	 * Constructs a new instance of the Profile
	 * 
	 * @param dataSet
	 *            The data set the profile will be contained with in
	 * @param offset
	 *            The offset of the profile in the source data structure
	 */
	public Profile(Dataset dataSet, int offset, BinaryReader reader) {
		super(dataSet, offset);
		this.componentIndex = reader.readByte();
		this.profileId = reader.readInt32();
		int valueIndexesCount = reader.readInt32();
		int signatureIndexesCount = reader.readInt32();
		this.valueIndexes = BaseEntity.readIntegerArray(reader,	valueIndexesCount);
		this.signatureIndexes = BaseEntity.readIntegerArray(reader, signatureIndexesCount);
	}

	/**
	 * If storage of object references is enabled initialises the arrays of
	 * related properties and values.
	 */
	public void init() throws IOException {
		properties = GetProperties();
		values = GetValues();
		signatures = GetSignatures();
		component = getDataSet().getComponents().get(componentIndex);
	}

	/**
	 * Returns an array of signatures the profile relates to.
	 * 
	 * @return
	 */
	private Signature[] GetSignatures() throws IOException {
		// return SignatureIndexes.Select(i =>
		// DataSet.Signatures[i]).ToArray();
		List<Signature> signatures = new ArrayList<Signature>();

		for (int index : signatureIndexes) {
			signatures.add(getDataSet().getSignatures().get(index));
		}

		return signatures.toArray(new Signature[signatures.size()]);
	}

	/**
	 * Returns an array of properties the profile relates to.
	 * 
	 * @return
	 */
	private Property[] GetProperties() throws IOException {
		Set<Property> properties = new TreeSet<Property>(
				new Comparator<Property>() {
					public int compare(Property o1, Property o2) {
						try
						{
							return o1.getName().compareTo(o2.getName());
						}
						catch(IOException ex)
						{
							return 0;
						}
					}
				});

		for (Value value : getValues()) {
			properties.add(value.getProperty());
		}

		return properties.toArray(new Property[properties.size()]);
	}

	/**
	 * Returns an array of values the profile relates to.
	 * 
	 * @return
	 */
	private Value[] GetValues() throws IOException {
		List<Value> values = new ArrayList<Value>();
		for (int valueIndex : getValueIndexes()) {
			values.add(getDataSet().getValues().get(valueIndex));
		}
		return values.toArray(new Value[values.size()]);
	}

	/**
	 * Compares this profile to another using the numeric ProfileId field.
	 * 
	 * @param other
	 *            The component to be compared against
	 * @return Indication of relative value based on ProfileId field
	 */
	public int compareTo(Profile other) {
		return profileId - other.profileId;
	}

	/**
	 * A string representation of the profiles display values.
	 * 
	 * @return the profile as a string
	 */
	@Override
	public String toString() {
		if (stringValue == null) {
			synchronized(this) {
				if (stringValue == null) {
					List<Value> values = new ArrayList<Value>();
					try {
						for(int i = 0; i < getValues().length; i++) {
							Value value = getValues()[i];
							if (value.getProperty().displayOrder > 0 &&
								value.getName().contains("Unknown") == false) {
								int index = Collections.binarySearch(values, value, propertyComparator);
								if (index < 0)
									values.add(~index, value);
							}
						}
						if (values.size() > 0) {
							// Values with a display order were found. Sort 
							// them and then concatenate before returning.
							StringBuilder sb = new StringBuilder();
							for(int i = 0; i < values.size(); i++) {
								sb.append(values.get(i).toString());
								if (i < values.size() - 1)
									sb.append("/");
							}
							stringValue = sb.toString();
						}
						else {
							stringValue = "Blank";
						}
					}
					catch (IOException e) {
						stringValue = "Blank";
					}
				}
			}
		}
		return stringValue;
	}

	private String stringValue = null;
		
	public int[] getValueIndexes() {
		return valueIndexes;
	}

	public int[] getSignatureIndexes() {
		return signatureIndexes;
	}

	private static final int MIN_LENGTH = 1 + 4 + 4 + 4;

	public int getLength() {
		return MIN_LENGTH + (valueIndexes.length * 4)
				+ (signatureIndexes.length * 4);
	}
}
