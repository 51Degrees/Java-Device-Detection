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
package fiftyone.mobile.detection.binary;

/**
 *
 * An exception thrown by classes in the binary package.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class BinaryException extends Exception {

    /**
     *
     * Constructs a new instance of Binary Exception.
     *
     * @param message the error message associated with the exception.
     */
    public BinaryException(final String message) {
        super(message);
    }
}
