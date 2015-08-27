package fiftyone.mobile.detection.factories.stream;

import fiftyone.mobile.detection.entities.stream.Pool;

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
 * Factory used to create stream Node entities.
 */
public abstract class NodeStreamFactory 
                    extends fiftyone.mobile.detection.factories.NodeFactory {
    /**
     * Pool for the corresponding data set used to get readers.
     */
    protected final Pool pool;
    
    /**
     * Constructs a new instance of NodeStreamFactory.
     * @param pool Pool from the data set to be used when creating new entities.
     */
    public NodeStreamFactory(Pool pool) {
        this.pool = pool;
    }
}
