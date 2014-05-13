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
package fiftyone.mobile.detection;

/**
 * Class Run by the Timer object in the Factory51D Object. Used to prompt an
 * update in the Factory class.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class AutoUpdater extends java.util.TimerTask {

    /**
     * The factory used to construct the updater.
     */
    private Factory _factory = null;

    /**
     * Constructs a new instance of the updater for the factory provided.
     *
     * @param factory
     */
    public AutoUpdater(final Factory factory) {
        super();
        _factory = factory;
    }

    /**
     * Runs the Updater.
     */
    @Override
    public void run() {
        final long currentPublishedDate = _factory.getProvider().getPublishedDate().getTime();

        /*
         * Check the last accessed date of the binary file to determine if
         * it should be updated. Update will also proceed if lite data is in use
         */
        if (currentPublishedDate + Constants.AUTO_UPDATE_WAIT < System.currentTimeMillis()
                || !_factory.getProvider().getDataSetName().equals("Premium")) {
            _factory.update();
        }
    }
}