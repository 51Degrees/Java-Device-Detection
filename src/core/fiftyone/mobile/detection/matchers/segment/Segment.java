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
package fiftyone.mobile.detection.matchers.segment;

/**
 *
 * A Class used to hold segment data
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Segment {

    /**
     * Indicates if the segment is valid.
     */
    private boolean _isValid;
    /**
     * Get the score for this segment.
     */
    private long _score;
    /**
     * The segments weighting.
     */
    private int _weight;
    /**
     * The value of the Segment.
     */
    private String _value;

    /**
     *
     * @return the segments value
     */
    String getValue() {
        return _value;
    }

    /**
     *
     * @return The segments weighting.
     */
    int getWeight() {
        return _weight;
    }

    /**
     *
     * Constructs a new instance of segment.
     *
     * @param value The value of the segment.
     * @param weight The weighting of the segment (higher weighting = greater
     * importance).
     */
    public Segment(String value, int weight) {
        _value = value;
        _weight = weight;
    }

    /**
     *
     * @return Get the score for this segment.
     */
    long getScore() {
        return _score;
    }

    /**
     *
     * Set the segment score flag is valid as true.
     *
     * @param value The value to set the score to.
     */
    void setScore(long value) {
        _score = value;
        _isValid = true;
    }

    /**
     *
     * @return true if it is, false else.
     */
    boolean getIsValid() {
        return _isValid;
    }
}
