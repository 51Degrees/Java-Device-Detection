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

/**
 *
 * Mutable wrapper class for ints to allow pass by reference.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Num {

    /**
     * Holds the it value being wrapped.
     */
    private int _i;

    /**
     * Default constructor, sets value to 0.
     */
    public Num() {
        _i = 0;
    }

    /**
     * Constructs object and assigns x as the value.
     *
     * @param x value to set object to.
     */
    public Num(int x) {
        _i = x;
    }

    /**
     *
     * @return the int value stored in the wrapper.
     */
    public int get() {
        return _i;
    }

    /**
     * Sets the int value stored in the wrapper.
     *
     * @param x the value to set the wrapper to.
     */
    public void set(int x) {
        _i = x;
    }
}
