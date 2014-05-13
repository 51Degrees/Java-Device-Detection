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
package fiftyone.mobile.detection.handlers;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.matchers.Results;
import fiftyone.mobile.detection.matchers.segment.Segment;
import fiftyone.mobile.detection.matchers.segment.Segments;
import fiftyone.mobile.detection.matchers.segment.Matcher;
import java.util.List;

/**
 *
 * An abstract handler used by any handler that breaks a string down into
 * segments.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public abstract class SegmentHandler extends Handler {

    /**
     *
     * Constructs a new instance of SegmentHandler
     *
     * @param provider Reference to the provider instance the handler will be
     * associated with.
     * @param name Name of the handler for debugging purposes.
     * @param confidence The confidence this handler should be given compared to
     * others.
     * @param checkUAProfs True if UAProfs should be checked.
     */
    SegmentHandler(
            final Provider provider, 
            final String name, 
            final int confidence, 
            final boolean checkUAProfs) {
        super(provider, name, confidence, checkUAProfs);
    }

    /**
     *
     * Creates segments for all regular expressions.
     *
     * @param userAgent The User Agent segments should be returned for.
     * @return The list of segments.
     */
    public abstract Segments createAllSegments(final String userAgent);

    /**
     *
     * Creates segments for the regular expressions index provided.
     *
     * @param device The device to get the segments from.
     * @param index The index of the segment required.
     * @return The list of segments.
     */
    public abstract List<Segment> createSegments(
            final BaseDeviceInfo device, 
            final int index);
    
    /**
     *
     * Run the matching algorithm on the User Agent.
     *
     * @param userAgent User Agent to test.
     * @return the results of the match.
     * @throws InterruptedException
     */
    @Override
    public Results match(final String userAgent) throws InterruptedException {
        if(super._provider.getThreadPool() == null) {
            return Matcher.matchSingleProcessor(userAgent, this);
        }
        else {
            return Matcher.matchMultiProcessor(userAgent, this, super._provider.getThreadPool());
        }
    }
}
