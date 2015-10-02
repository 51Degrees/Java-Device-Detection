package fiftyone.mobile.detection.webapp;

import java.util.ArrayList;
import java.util.List;

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
public class Stats extends ArrayList<Stat> {

    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    long lastId = 0;

    public long getLastResponseTime() {
        Stat stat = getLastComplete();
        return stat == null ? -1 : stat.getResponseTime();
    }

    void removeOld() {
        long now = System.currentTimeMillis();
        List<Stat> toRemove = new ArrayList<Stat>();
        for (Stat stat : this) {
            if (now - stat.serverTimeRecieved > FIVE_MINUTES) {
                toRemove.add(stat);
            }
        }
        removeAll(toRemove);
    }

    public long getLastCompletionTime() {
        Stat stat = getLastComplete();
        return stat == null ? -1 : stat.getCompletionTime();
    }

    // will get minor rounding errors
    public long getAverageResponseTime() {
        List<Stat> all = getAllComplete();
        if (all.size() > 0) {
            long total = 0;
            for (Stat stat : all) {
                total += stat.getResponseTime();
            }
            return total / all.size();
        } else {
            return 0;
        }
    }

    // minor rounding errors
    public long getAverageCompletionTime() {
        List<Stat> all = getAllComplete();
        if (all.size() > 0) {
            long total = 0;
            for (Stat stat : all) {
                total += stat.getCompletionTime();
            }
            return total / all.size();
        } else {
            return 0;
        }
    }

    // minor rounding issues
    public int getAverageBandwidth() {
        List<Stat> all = getAllComplete();
        if (all.size() > 0) {
            int total = 0;
            for (Stat stat : all) {
                total += stat.getBandwidth();
            }
            return total / all.size();
        } else {
            return 0;
        }
    }

    private List<Stat> getAllComplete() {
        List<Stat> result = new ArrayList<Stat>();

        for (Stat stat : this) {
            if (stat.isComplete()) {
                result.add(stat);
            }
        }

        return result;
    }

    private Stat getLastComplete() {
        for (int i = size() - 1; i >= 0; i--) {
            Stat current = get(i);
            if (current.isComplete()) {
                return current;
            }
        }
        return null;
    }

    public Stat fetch(long id) {
        for (Stat stat : this) {
            if (id == stat.id) {
                return stat;
            }
        }
        return null;
    }
}
