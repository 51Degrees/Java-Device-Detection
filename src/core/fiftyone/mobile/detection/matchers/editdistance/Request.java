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
package fiftyone.mobile.detection.matchers.editdistance;

import fiftyone.mobile.detection.handlers.Handler;

/**
 *
 * A class used for requesting the results for an Edit Distance handler.
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
     *
     * @return The results from this request.
     */
    public synchronized Results getResults() {
        return _results;
    }

    /**
     *
     * Constructs a new instance of Request.
     *
     * @param userAgent User Agent String being accessed.
     * @param handler the handler which is being requested.
     */
    Request(final String userAgent, final Handler handler) {
        super(userAgent, handler);
        _results = new Results();
    }
}
