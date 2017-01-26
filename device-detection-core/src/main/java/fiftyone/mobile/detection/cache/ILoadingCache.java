/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.cache;

import java.io.IOException;

/**
 * Extension of general cache contract to provide for getting a value with a particular
 * value loaded. Primarily used to allow the value loader to be an already instantiated value of the
 * type V to avoid construction costs of that value. (In other words the loader has the signature
 * "extends V implements IValueLoader").
 * <p>
 * Used only in UA Matching.
 * @see fiftyone.mobile.detection.Provider
 */
public interface ILoadingCache<K,V> extends ICache <K,V> {
    V get(K key, IValueLoader<K, V> loader) throws IOException;
}
