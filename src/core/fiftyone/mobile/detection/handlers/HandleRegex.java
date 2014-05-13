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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * A regular expression, and children, used to determine if a User Agent can be
 * matched by the associated handler. Not only does the regular expression
 * provided have to match by any one of the children.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class HandleRegex {

    /**
     * A list of children.
     */
    final private List<HandleRegex> _children = new ArrayList<HandleRegex>();
    /**
     * Regular expression pattern associated with this object
     */
    private Pattern _pattern;

    /**
     *
     * @return The list of child regular expressions
     */
    public List<HandleRegex> getChildren() {
        return _children;
    }

    /**
     *
     * Constructs a new instance of Handle regular expression
     *
     * @param pattern Regular expression pattern.
     */
    public HandleRegex(final String pattern) {
        _pattern = Pattern.compile(pattern);
    }

    /**
     *
     * Returns true if the regular expression and any one of it's child match.
     *
     * @param useragent The User Agent string to check.
     * @return True if a match is found.
     */
    boolean isMatch(final String useragent) {
        if (_pattern.matcher(useragent).find()) {
            if (_children.isEmpty()) {
                return true;
            }

            for (HandleRegex child : _children) {
                if (child.isMatch(useragent)) {
                    return true;
                }
            }
        }
        return false;
    }
}
