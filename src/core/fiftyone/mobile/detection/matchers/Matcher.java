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
package fiftyone.mobile.detection.matchers;

import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * Super class that all matchers inherit from.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public abstract class Matcher {
    
    /**
     * Gets the number of threads the matcher should use with the
     * ThreadPoolExecutor.
     * @param threadPool
     * @return The number of threads to use.
     */
    protected static int getThreadCount(ThreadPoolExecutor threadPool) {
        int poolSize = threadPool.getCorePoolSize();
        //int poolSize = threadPool.getPoolSize();
        if(poolSize == 0) {
            poolSize = 4;
        }
        return poolSize;
    }
    
}
