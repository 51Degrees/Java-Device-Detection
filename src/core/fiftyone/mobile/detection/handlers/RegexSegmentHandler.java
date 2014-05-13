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
import fiftyone.mobile.detection.matchers.segment.Segment;
import fiftyone.mobile.detection.matchers.segment.Segments;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Device detection handler using regular expressions to segment strings before
 * matching specific segments.
 *
 * @author 51degrees.mobi
 * @version 2.2.9.1
 */
public class RegexSegmentHandler extends SegmentHandler {

    /**
     * Contains regular expression and weight to apply to each segment of the
     * User Agent String.
     */
    public static class RegexSegment {

        /**
         * The regular expression to use to get the segment.
         */
        private Pattern _pattern;
        /**
         * The weight that should be given to the segment.
         */
        private int _weight;

        /**
         *
         * @return The regular expression to use to get the segment.
         */
        public Pattern getPattern() {
            return _pattern;
        }

        /**
         *
         * @return The weight that should be given to the segment. The lower the
         * number the greater the significance.
         */
        public int getWeight() {
            return _weight;
        }

        /**
         *
         * Constructs a new instance of regular expression Segment.
         *
         * @param pattern The regular expression for the segment.
         * @param weight The relative weight to apply to the segment.
         */
        public RegexSegment(final String pattern, final int weight) {
            _pattern = Pattern.compile(pattern);
            _weight = weight;
        }
    }
    /**
     * A list of segments to be found and matched by the handler.
     */
    private List<RegexSegment> _segments = new ArrayList<RegexSegment>();

    /**
     *
     * @return A list of the regular expressions used to create segments.
     */
    public List<RegexSegment> getSegments() {
        return _segments;
    }

    /**
     *
     * Constructs a new instance of RegexSegmentHandler
     *
     * @param provider Reference to the provider instance the handler will be
     * associated with.
     * @param name Name of the handler for debugging purposes.
     * @param confidence The confidence this handler should be given compared to
     * others.
     * @param checkUAProfs True if UAProfs should be checked.
     */
    public RegexSegmentHandler(
            final Provider provider, 
            final String name, 
            final int confidence, 
            final boolean checkUAProfs) {
        super(provider, name, confidence, checkUAProfs);
    }

    /**
     *
     * Adds a new regular expression to the segment
     *
     * @param pattern The regular expression.
     * @param weight It's weighting (higher the waiting the more important).
     */
    void addSegment(
            final String pattern, 
            final int weight) {
        _segments.add(new RegexSegment(pattern, weight));
    }

    /**
     *
     * Returns true if the handler can match the requests User Agent string and
     * at least one valid segment is returned as a segment.
     *
     * @param userAgent User Agent to check.
     * @return True if it can be handled, false if not.
     */
    @Override
    public boolean canHandle(final String userAgent) {
        if (super.canHandle(userAgent) == false) {
            return false;
        }

        for (RegexSegment segment : _segments) {
            if (segment.getPattern().matcher(userAgent).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * Returns segments for the index specified checking in the stored results
     * first if the StoreSegmentResults constant is enabled.
     *
     * @param device The source User Agent String.
     * @param index The index of the regular expression to use to get the
     * segments.
     * @return The list of matching segments.
     */
    @Override
    public List<Segment> createSegments(final BaseDeviceInfo device, final int index) {
        List<Segment> segments;
        if (fiftyone.mobile.detection.Constants.STORE_SEGMENT_RESULTS) {
            // Get the handlers data from the device.
            final List<List<Segment>> cachedSegments = device.getHandlerData(this);

            // If the segment does not already exist then add it.
            if (cachedSegments.size() <= index) {
                while (cachedSegments.size() <= index) {
                    cachedSegments.add(new ArrayList<Segment>());
                }
                segments = createSegments(device.getUserAgent(), _segments.get(index));
                cachedSegments.set(index, segments);
            } else {
                segments = cachedSegments.get(index);
            }
        } else {
            segments = createSegments(device.getUserAgent(), _segments.get(index));
        }
        return segments;
    }

    /**
     *
     * Returns the segments from the source string. Where a segment returns
     * nothing a single empty segment will be added.
     *
     * @param source String to be matched.
     * @return A list of segments relating to the String.
     */
    @Override
    public Segments createAllSegments(final String source) {
        final Segments results = new Segments();
        for (RegexSegment segment : _segments) {
            results.add((ArrayList)createSegments(source, segment));
        }
        return results;
    }

    /**
     *
     * Finds all matches of the source against the segment. if no matches are
     * found then the empty String is returned.
     *
     * @param source the String to be compared.
     * @param segment the segment to be compared against.
     * @return All matches of the source against the segment.
     */
    private List<Segment> createSegments(
            final String source, 
            final RegexSegment segment) {
        boolean matched = false;
        final List<Segment> newSegments = new ArrayList<Segment>();
        final List<String> matches = new ArrayList<String>();
        final Matcher m = segment.getPattern().matcher(source);
        //for each occurence of a match, add the match to the results ArrayList
        while (m.find()) {
            matches.add(m.group());
        }
        // Add a segment for each match found.
        for (String match : matches) {
            newSegments.add(new Segment(match, segment.getWeight()));
            matched = true;
        }
        if (matched == false) {
            // Add an empty segment to avoid problems of missing segments
            // stopping others being compared correctly.
            newSegments.add(new Segment("", segment.getWeight()));
        }
        return newSegments;
    }
}
