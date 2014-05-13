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
import java.util.ArrayList;

/**
 *
 * Holds a group of Result object. Used to store the results from the different
 * handlers.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Results extends ArrayList<Result> {

    /**
     * Default constructor.
     */
    public Results() {
        super();
    }
    
    /**
     * Constructs an instance of the result class.
     * 
     * @param device Initial device to be added.
     * @param handler Handler to be associated with the device.
     * @param score The score associated with the result.
     * @param userAgent The target user agent. 
     */
    public Results(
            final BaseDeviceInfo device, 
            final Handler handler, 
            final long score, 
            final String userAgent) {
        super();
        this.add(device, handler, score, userAgent);
    }

    /**
     * Adds a result to the result set.
     * 
     * @param device Initial device to be added.
     * @param handler Handler to be associated with the device.
     * @param score The score associated with the result.
     * @param userAgent The target user agent. 
     */    
    public final void add(
            final BaseDeviceInfo device, 
            final Handler handler, 
            final long score, 
            final String userAgent) {
        super.add(new Result(
            device.getProvider(),
            device,
            handler,
            score,
            userAgent));
    }

    /**
     * Adds a range of devices to the results, all associated
     * with the handler provided.
     * 
     * @param devices Array of all devices to be added.
     * @param handler Handler to be associated with the device.
     * @param score The score associated with the result.
     * @param userAgent The target user agent. 
     */
    public void addRange(
            final BaseDeviceInfo[] devices, 
            final Handler handler, 
            final long score, 
            final String userAgent) {
        for (BaseDeviceInfo device : devices) {
            add(device, handler, score, userAgent);
        }
    }    
}
