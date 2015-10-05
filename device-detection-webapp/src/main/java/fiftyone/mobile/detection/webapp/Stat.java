package fiftyone.mobile.detection.webapp;

import java.io.Serializable;

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
@SuppressWarnings("serial")
public class Stat implements Comparable<Stat>, Serializable {
    // Set when the server starts to send the headers.

    long serverTimeSent;
    // Set when the browser receives the response.
    long browserTimeRecieved;
    // Set when the browser finishes rendering the response.
    long browserTimeCompleted;
    // Set when the browser sends the request.
    long browserTimeSent;
    // Set when the stat is created.
    long serverTimeRecieved;
    // Length in bytes of the response.
    int responseLength;
    // Length in bytes of the request.
    int requestLength;
    // / <summary>
    // / The unique Id of the stat for the session.
    // / </summary>
    final long id;

    public Stat() {
        this.id = System.currentTimeMillis();
    }

    public long getServerProcessingTime() {
        if (serverTimeSent >= serverTimeRecieved) {
            return serverTimeSent - serverTimeRecieved;
        }
        return -1;
    }

    public long getResponseTime() {
        if (browserTimeRecieved >= browserTimeSent) {
            return browserTimeRecieved - browserTimeSent;
        }
        return -1;
    }

    public long getCompletionTime() {
        if (browserTimeCompleted >= browserTimeSent) {
            return browserTimeCompleted - browserTimeSent;
        }
        return -1;
    }

    public double getBandwidth() {
        if (getResponseTime() != -1 && getServerProcessingTime() != -1) {
            return ((double) (requestLength + responseLength))
                    / ((double) (getResponseTime() - getServerProcessingTime()))
                    / 1000;
        }
        return 0;
    }

    public boolean isComplete() {
        return browserTimeSent > 0
                && browserTimeRecieved >= browserTimeSent
                && browserTimeCompleted >= browserTimeSent
                && serverTimeRecieved > 0
                && serverTimeSent >= serverTimeRecieved;

    }

    @Override
    public int compareTo(Stat stat) {
        return id - stat.id > 0 ? 1 : -1;
    }
}
