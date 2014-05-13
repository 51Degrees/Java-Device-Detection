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

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.handlers.SegmentHandler;
import fiftyone.mobile.detection.matchers.Algorithms;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class containing the matching logic for Segmenting and the RegexSegment
 * handler.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Matcher extends fiftyone.mobile.detection.matchers.Matcher {
    
    /**
    * Holds the method to service Regular Expression Handler requests.
    */
    static class ServiceRequest implements Runnable {

        /**
         * The request Object.
         */
        private Request _request;

        ServiceRequest(final Request request) {
            _request = request;
        }

        /**
         * Algorithm used to carry out a RegexSegment Handler check.
         */
        @Override
        public void run() {
            int index;
            long runningScore, score;
            final String userAgent = _request.getUserAgent();
            final int[][] rows = new int[userAgent.length() + 1][userAgent.length() + 1];
            BaseDeviceInfo current = _request.next();
            while (current != null) {
                // Reset the counters.
                runningScore = 0;
                index = 0;

                while (index < _request.getTarget().size()
                        && runningScore <= _request.getResults().LowestScore) {
                    // Get the next segment for the comparision.
                    final List<Segment> compare = _request.getHandler().createSegments(
                            current, index);

                    if (compare != null) {
                        // The two results are not equal in length so do not consider
                        // this User Agent as a possible match.
                        if (_request.getTarget().get(index).size() != compare.size()) {
                            runningScore = Long.MAX_VALUE;
                            break;
                        }

                        // Work out the score for each of the returned segments.
                        for (int segmentIndex = 0;
                                segmentIndex < _request.getTarget().get(index).size();
                                segmentIndex++) {
                            // If the two are equal then set to zero.
                            if (_request.getTarget().get(index).get(segmentIndex).getValue().equals(compare.get(segmentIndex).getValue())) {
                                score = 0;
                            } else {
                                score = (long) Algorithms.EditDistance(rows,
                                        _request.getTarget().get(index).get(segmentIndex).getValue(),
                                        compare.get(segmentIndex).getValue(),
                                        fiftyone.mobile.detection.Constants.MAX_INT)
                                        * (long) _request.getTarget().get(index).get(segmentIndex).getWeight();
                            }

                            // Update the counters.
                            compare.get(segmentIndex).setScore(score);
                            runningScore += score;
                        }
                    }
                    index++;
                }

                if (runningScore <= _request.getResults().LowestScore) {
                    synchronized (_request.getResults()) {
                        if (runningScore == _request.getResults().LowestScore) {
                            _request.getResults().add(current, _request.getHandler(), runningScore, userAgent);
                        } else if (runningScore < _request.getResults().LowestScore) {
                            _request.getResults().LowestScore = runningScore;
                            _request.getResults().clear();
                            _request.getResults().add(current, _request.getHandler(), runningScore, userAgent);
                        }
                    }
                }
                current = _request.next();
            }
        }
    }
    
    /**
     * Creates a _logger for this class
     */
    static final Logger _logger = LoggerFactory.getLogger(Matcher.class);

    /**
     * Routine for matching when only one processor is being used.
     *
     * @param userAgent User Agent being matched.
     * @param handler The handler associated with the matching request.
     * @return The best matched User Agent.
     */
    public static Results matchSingleProcessor(
            final String userAgent, 
            final SegmentHandler handler) {
        return matchSingleProcessor(new Request(userAgent, handler));
    }

    /**
     * Routine for matching when only one processor is being used.
     * @param request configured with userAgent and handler
     * @return The best devices matching the userAgent.
     */
    private static Results matchSingleProcessor(final Request request) {
        // Process the request.
        new ServiceRequest(request).run();
        // Return the results.
        return request.getResults();
    }
    
    /**
     * Routine for matching when multiple processors are being used.
     *
     * @param userAgent User Agent being matched.
     * @param handler The handler associated with the matching request.
     * @param threadPool The Provider Object's thread pool.
     * @return The best matched User Agent.
     * @throws InterruptedException
     */
    public static Results matchMultiProcessor(
            final String userAgent, 
            final SegmentHandler handler, 
            final ThreadPoolExecutor threadPool) throws InterruptedException {
        // Provide an object to signal when the request has completed.

        // Create the request.
        final Request request = new Request(userAgent, handler);
        if (request.getCount() > 0) {
            try {
                int poolSize = Matcher.getThreadCount(threadPool);
                // Create a task for each processor, submitting the task for 
                // execution.
                final List<Future<Object>> futures = 
                        new ArrayList<Future<Object>>(poolSize);
                final Callable<Object> c = 
                        Executors.callable(new ServiceRequest(request));
                for (int i = 0; i < poolSize; i++) {
                    futures.add(threadPool.submit(c));
                }
                // Wait for all the tasks to finish executing before returning 
                // the results.
                for(Future<Object> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException ex) {
                        _logger.warn(
                        "Multi thread segment matcher execution exception.",
                        ex);
                    }
                }
            }
            catch (RejectedExecutionException ex) {
                _logger.warn(
                    "Multi thread segment matcher execution rejected. " +
                    "Falling back to single threaded matching.",
                    ex);
                return matchSingleProcessor(request);
            }
        }
        // Return the results.
        return request.getResults();
    }
}
