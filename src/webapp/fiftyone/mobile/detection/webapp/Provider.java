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
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.webapp.Constants;
import fiftyone.mobile.detection.matchers.Result;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Represents all device data and capabilities.
 *
 * @author 51Degrees.mobi
 * @version 2.2.8.7
 */
public class Provider extends fiftyone.mobile.detection.Provider {
    /**
     * Thread that runs share usage.
     */
    private Thread _shareUsageThread;
    /**
     * Share usage object to be run.
     */
    private ShareUsage _shareUsage;
    
    /**
     * Creates a _logger for this class
     */
    final Logger _logger = LoggerFactory.getLogger(Provider.class);

    /**
     * Constructs a new instance of a webapp Provider.
     */
    public Provider() {
        super();

        if (Constants.SHARE_USAGE) {
            _shareUsage = new ShareUsage(
                    Constants.NEW_DEVICE_URL,
                    Constants.NEW_DEVICE_DETAIL);
            _shareUsageThread = new Thread(
                    _shareUsage,
                    "51Degrees.mobi Share Usage Thread");
            //Want it to loop for potentially infinite time and then be destroyed when program exited
            _shareUsageThread.setDaemon(true);
            //Set the thread to lowest priority and start it
            _shareUsageThread.setPriority(Thread.MIN_PRIORITY);
            _shareUsageThread.start();
        }
    }

    /**
     *
     * Used to enable usage sharing with 51Degrees.mobi. Sharing usage data
     * helps 51Degrees.mobi keep its device detection up to date and relevant.
     *
     * @param request An HttpServletRequest from a requesting device.
     */
    private void shareUsage(HttpServletRequest request) {
        try {
            // Record the details of the connecting device if the thread is Alive
            if (_shareUsageThread.isAlive()) {
                _shareUsage.recordNewDevice(request);
            }
        } catch (XMLStreamException ex) {
            _logger.error("Share usage exception", ex);
        } catch (IOException ex) {
            _logger.error("Share usage exception", ex);
        }
    }

    /**
     * Returns a match result for a servlet request.
     * @param request the current HttpServletRequest object.
     * @return the result of the match.
     */
    public Result getResult(HttpServletRequest request) {
        shareUsage(request);
        final Enumeration headerNames = request.getHeaderNames();
        final HashMap<String, String> headers = new HashMap<String, String>();
        while (headerNames.hasMoreElements()) {
            final String n = (String) headerNames.nextElement().toString().toLowerCase();
            headers.put(n, request.getHeader(n));
        }
        return super.getResult(headers);
    }
    
    /**
     *
     * Find the requesting device using an HttpServletRequest Object with share
     * usage data on.
     *
     * @param request The current HttpServletRequest object.
     * @return The closest matching device.
     */
    public BaseDeviceInfo getDeviceInfo(final HttpServletRequest request) {
        final Result result = getResult(request);
        if (result != null) {
            return result.getDevice();
        }
        return null;
    }

    /**
     * Disposes of the provider sending remaining Usage Data if Share
     * Usage is enabled.
     */
    public void destroy() {
        _logger.trace("Destroying 51Degrees.mobi Provider");            
        // If Share Usage is enabled and the thread has been started
        if (_shareUsageThread != null) {
            // If the thread is still alive 
            if (_shareUsageThread.isAlive()) {
                // Send remaining data and terminate
                _shareUsage.destroy();
                // Wait for at most 5 seconds for the thread to die.
                try {
                    _shareUsageThread.join(5000L);
                } catch (InterruptedException ex) {
                    _logger.warn(
                        "Exception joining share usage thread.",
                        ex);
                }
            }
        }
        _logger.trace("Destroyed 51Degrees.mobi Provider");         
    }
}
