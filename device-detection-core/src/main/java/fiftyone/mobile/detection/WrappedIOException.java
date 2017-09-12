/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

/**
 * Class wraps a RuntimeException. Used to throw an exception when it can not 
 * be pushed further up the call stack.
 * <p>
 * This class should not be called as it is part of the internal logic.
 */
public class WrappedIOException extends RuntimeException {
    
    /**
     * Creates a new instance of WrappedIOException.
     * 
     * @param message string of text describing the problem.
     */
    public WrappedIOException(final String message) {
        super();
        this.message = message;
    }

    /**
     * @return string of text describing the problem.
     */
    @Override
    public String getMessage() {
        return message;
    }
    private final String message;
}
