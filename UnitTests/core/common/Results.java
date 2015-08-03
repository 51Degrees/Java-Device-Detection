package common;

import fiftyone.mobile.detection.MatchMethods;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

public class Results {

    public final long startTime;

    public AtomicInteger count = new AtomicInteger();

    public AtomicLong checkSum = new AtomicLong();

    public final HashMap<MatchMethods, AtomicInteger> methods;

    public long getElapsedTime() {
        synchronized(this) {
            if (elapsedTime == 0) {
               elapsedTime = Calendar.getInstance().getTimeInMillis() - startTime; 
            }
        }
        return elapsedTime;
    }
    private long elapsedTime = 0;

    public long getAverageTime() {
        return getElapsedTime() / count.intValue();
    }

    public Results() {
        this.methods = new HashMap<MatchMethods, AtomicInteger>();
        this.methods.put(MatchMethods.CLOSEST, new AtomicInteger());
        this.methods.put(MatchMethods.EXACT, new AtomicInteger());
        this.methods.put(MatchMethods.NEAREST, new AtomicInteger());
        this.methods.put(MatchMethods.NONE, new AtomicInteger());
        this.methods.put(MatchMethods.NUMERIC, new AtomicInteger());
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public double getMethodPercentage(MatchMethods method) {
        return (double)methods.get(method).intValue() / (double)count.intValue();
    }
}