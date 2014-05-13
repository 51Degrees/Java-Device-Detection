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
import fiftyone.mobile.detection.matchers.reducedinitialstring.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Device detection handler using the reduced initial string method. The first
 * part of the strings are checked to determine a match.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class ReducedInitialStringHandler extends Handler {

    /**
     * Regular expression used to check the String.
     */
    private Pattern _tolerance = null;

    /**
     *
     * @return The regular expression used to determine the first X characters
     * to check of the String.
     */
    public Pattern getTolerance() {
        return _tolerance;
    }

    /**
     *
     * Constructs an instance of Reduced Initial String Handler.
     *
     * @param provider Reference to the provider instance the handler will be
     * associated with.
     * @param name Name of the handler for debugging purposes.
     * @param confidence The confidence this handler should be given compared to
     * others.
     * @param checkUAProfs True if UAProfs should be checked.
     * @param tolerance Regular expression used to calculate how many characters
     * should be matched at the beginning of the User Agent.
     */
    public ReducedInitialStringHandler(final Provider provider, 
            final String name, 
            final int confidence, 
            final boolean checkUAProfs, 
            final String tolerance) {
        super(provider, name, confidence, checkUAProfs);
        _tolerance = Pattern.compile(tolerance);
    }
    
    /**
     *
     * Runs the matcher algorithm on the given User Agent.
     *
     * @param userAgent User Agent to match.
     * @return The Results.
     */
    @Override
    public Results match(final String userAgent) throws InterruptedException{
        return Matcher.match(
                userAgent, 
                this, 
                (_tolerance.matcher(userAgent).start() - _tolerance.matcher(userAgent).end()));
    }
}
