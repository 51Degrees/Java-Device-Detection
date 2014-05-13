/* *********************************************************************
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A collection of string indexes.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Collection extends HashMap<Integer, List<Integer>> {

    /**
     * Strings object associated with the Collection Object
     */
    private final Strings _strings;

    /**
     *
     * Constructs the collection object.
     *
     * @param strings the Strings object you would like to associate with this
     * reference.
     */
    public Collection(final Strings strings) {
        super();
        _strings = strings;
    }

    /**
     *
     * Sets the capabilityName and Value in the collection.
     *
     * @param capabilityName Name of the capability being set.
     * @param values Values of the capability being set.
     */
    public void set(final String capabilityName, final String[] values) {
        final int capabilityNameIndex = _strings.add(capabilityName);
        if (capabilityNameIndex >= 0) {
            final List<Integer> stringIndexes = new ArrayList<Integer>();
            for (String value : values) {
                stringIndexes.add(_strings.add(value));
            }
            set(capabilityNameIndex, stringIndexes);
        }
    }

    /**
     *
     * Sets the capabilityName and Value in the collection.
     *
     * @param capabilityNameIndex String index of the capability being set.
     * @param values Value of the capability being set.
     */
    public void set(final int capabilityNameIndex, final List<Integer> values) {
        // Does this capability already exist in the list?
        if (this.containsKey(capabilityNameIndex) == false) {
            // No. Create a new value and add it to the list.
            super.put(capabilityNameIndex, values);
        } else {
            // Yes. Replace it's value with the current one.
            final List<Integer> list = super.get(capabilityNameIndex);
            for (int value : values) {
                if (list.contains(value) == false) {
                    list.add(value);
                }
            }
        }
    }

    /**
     *
     * Checks the other Collection object instance contains identical keys
     * and values as this one.
     *
     * @param other Other Collection object.
     * @return True if the object instances contain the same values.
     */
    public boolean equals(final Collection other) {
        for (int key : this.keySet()) {
            if (other.get(key) != this.get(key)) {
                return false;
            }
        }
        return true;
    }
}
