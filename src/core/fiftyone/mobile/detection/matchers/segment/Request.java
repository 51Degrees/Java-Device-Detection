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

import fiftyone.mobile.detection.handlers.SegmentHandler;

/**
 *
 * A class used for requesting the results for a RegexSegment handler.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Request extends fiftyone.mobile.detection.matchers.Request {

    /**
     * The results from this request.
     */
    private final Results _results;
    /**
     * The segment object currently being used.
     */
    private final Segments _target;

    /**
     *
     * @return The segment object currently being used.
     */
    Segments getTarget() {
        return _target;
    }

    /**
     *
     * @return The handler object.
     */
    @Override
    public SegmentHandler getHandler() {
        SegmentHandler sh = (SegmentHandler) super.getHandler();
        return sh;
    }

    /**
     *
     * @return The results from this request.
     */
    Results getResults() {
        return _results;
    }

    /**
     *
     * Constructs a new instance of Request.
     *
     * @param userAgent User Agent String being accessed.
     * @param handler The handler which is being requested.
     */
    Request(String userAgent, SegmentHandler handler) {
        super(userAgent, handler);
        _target = getHandler().createAllSegments(userAgent);
        _results = new Results();
    }
}
