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

import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.matchers.Results;
import fiftyone.mobile.detection.matchers.editdistance.Matcher;

/**
 *
 * Device detection handler using the EditDistance method of matching devices.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class EditDistanceHandler extends Handler {

    /**
     *
     * Constructs a new instance of the Edit Distance Handler Object.
     *
     * @param provider Reference to the provider instance the handler will be
     * associated with.
     * @param name Name of the handler for debugging purposes.
     * @param confidence The confidence this handler should be given compared to
     * others.
     * @param checkUAProfs True if UAProfs should be checked.
     */
    public EditDistanceHandler(
            final Provider provider, 
            final String name, 
            final int confidence, 
            final boolean checkUAProfs) {
        super(provider, name, confidence, checkUAProfs);
    }
    
    /**
     *
     * Runs the Matcher Algorithm on the given User Agent.
     *
     * @param userAgent The User Agent to match.
     * @return The Results.
     */
    @Override
    public Results match(final String userAgent) throws InterruptedException{
        Results result;
        if(super._provider.getThreadPool() == null) {
            result = Matcher.matchSingleProcessor(userAgent, this);
        }
        else {
            result = Matcher.matchMultiProcessor(userAgent, 
                    this, 
                    super._provider.getThreadPool());
        }
        return result;
    }

    /**
     *
     * Compares two Handlers.
     *
     * @param handler the handler to compare to.
     * @return The result of the comparison. e.g. whether the handlers are of
     * the same type.
     */
    @Override
    public int compareTo(final Handler handler) {
        return super.compareTo(handler);
    }
}
