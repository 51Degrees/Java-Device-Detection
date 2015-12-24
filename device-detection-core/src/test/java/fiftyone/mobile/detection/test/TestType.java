/*
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
 */

package fiftyone.mobile.detection.test;

/**
 * Marker interfaces to identify different classes of tests. Allows skipping of tests
 * where appropriate data set is not available, e.g. in downloaded open source
 * where only the Lite data set is provided.
 * <p>
 * Note that when using these classes for configuration you must specify e.g. "TestType$DataSetEnterprise" as a reference
 * to the inner class name
 */
public interface TestType {
    /**
     * Marker interface for tests that use the Enterprise data set for use with {@link org.junit.experimental.categories.Category}
     */
    interface DataSetEnterprise {}

    /**
     * Marker interface for tests that use the Lite data set  for use with {@link org.junit.experimental.categories.Category}
     */
    interface DataSetLite {}

    /**
     * Marker interface for tests that use the Premium data set for use with {@link org.junit.experimental.categories.Category}
     */
    interface DataSetPremium {}

    /**
     * Marker interface for tests that take a long time
     */
    interface Lengthy {}

    /**
     * Marker Interface for API Tests  for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeApi {}

    /**
     * Marker Interface for HTTP Header Tests for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeHttpHeader {}

    /**
     * Marker Interface for Memory Tests  for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeMemory {}

    /**
     * Marker Interface for Metadata Tests  for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeMetadata {}

    /**
     * Marker Interface for Performance Tests  for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypePerformance {}
    
    /**
     * Marker Interface for Unit Tests for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeUnit{}
    
    /**
     * Marker Interface for Reconcile Tests for use with {@link org.junit.experimental.categories.Category}
     */
    interface TypeComparison{}
}
