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
package fiftyone.mobile.detection.matchers;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.handlers.Handler;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Abstract class created to handle requests for User Agents.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public abstract class Request {

    /**
     * The handler the User Agent has been assigned to.
     */
    private final Handler _handler;
    /**
     * The amount of devices left to be tested.
     */
    protected final LinkedBlockingQueue<BaseDeviceInfo> _queue;
    /**
     * The User Agent being processed.
     */
    protected final String _userAgent;

    /**
     *
     * @return The User Agent being processed.
     */
    public String getUserAgent() {
        return _userAgent;
    }

    /**
     *
     * @return The handler the User Agent has been assigned to.
     */
    public Handler getHandler() {
        return _handler;
    }

    /**
     *
     * @return The amount of devices left to be tested.
     */
    public int getCount() {
        return _queue.size();
    }

    /**
     *
     * Constructs a new instance of Request.
     *
     * @param userAgent User Agent to be tested.
     * @param handler Handler that is currently assigned to the User Agent.
     */
    public Request(final String userAgent, final Handler handler) {
        _userAgent = userAgent;
        _queue = createQueue(handler);
        _handler = handler;
    }

    /**
     *
     * @return The next device to be tested.
     */
    public BaseDeviceInfo next() {
        return _queue.poll();
    }

    /**
     *
     * Takes a handler and returns a queue containing the User Agent Strings.
     *
     * @param handler Handler containing devices.
     * @return A queue of devices.
     */
    private static LinkedBlockingQueue<BaseDeviceInfo> createQueue(final Handler handler) {
        final LinkedBlockingQueue<BaseDeviceInfo> queue = 
                new LinkedBlockingQueue<BaseDeviceInfo>();

        synchronized (handler.getDevices()) {
            for (int device : handler.getDevices().keySet()) {
                queue.addAll(handler.getDevices().get(device));
            }
        }
        return queue;
    }
}
