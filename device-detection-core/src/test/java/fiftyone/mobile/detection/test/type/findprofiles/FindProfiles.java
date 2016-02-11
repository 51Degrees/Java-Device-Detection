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

package fiftyone.mobile.detection.test.type.findprofiles;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.Filename;
import fiftyone.mobile.detection.cache.Cache;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.stream.FixedCacheList;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.detection.test.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

@Category(TestType.TypeApi.class)
public class FindProfiles extends DetectionTestSupport {

    /*
     * Checks that all profiles returned do match the requested property
     * and value.
     */
    @Test
    public void FalsePositives() throws Exception {
        Random rand = new Random();
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);

        for (Property property : dataset.getProperties()) {
            int valueindex = rand.nextInt(property.getValues().count());
            Value targetValue = property.getValues().get(valueindex);
            logger.debug("Testing " + property.getName() +
                " : " + targetValue.getName());
            List<Profile> profiles = dataset.findProfiles(property.getName(), targetValue.getName(), null);
            for (Profile profile : profiles) {
                assertTrue(profile.getValues(property).get(targetValue.getName()).equals(targetValue));
            }
        }
    }

    /*
     * Checks that all profiles that are expected to be matched
     * are indeed in the the returned profiles.
     */
    @Test
    public void FalseNegatives() throws Exception {
        Random rand = new Random();
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);

        for (Property property : dataset.getProperties()) {
            int valueindex = rand.nextInt(property.getValues().count());
            String valuename = property.getValues().get(valueindex).getName();
            logger.debug("Testing " + property.getName() +
                    " : " + valuename);
            List<Profile> profiles = dataset.findProfiles(property.getName(), valuename, null);
            List<Integer> expectedprofileindexes = property.getValues().get(valueindex).getProfileIndexes();
            List<Integer> profileindexes = new ArrayList<Integer>();
            for (Profile profile : profiles) {
                profileindexes.add(profile.getIndex());
            }
            for (Integer expectedprofileindex : expectedprofileindexes) {
                assertTrue(profileindexes.contains(expectedprofileindex));
            }
        }
    }

    /*
     * Checks for all properties that the profiles are not initialised,
     * runs FindProfiles on each, then checks again. The second time
     * the profiles should be initialised as FindProfiles has been used.
     */
    @Test
    public void CheckInitialised() throws Exception {
        Random rand = new Random();
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);

        for (Property property : dataset.getProperties()) {
            int valueindex = rand.nextInt(property.getValues().count());
            String valuename = property.getValues().get(valueindex).getName();
            assertFalse(property.isValueProfilesSet());
            dataset.findProfiles(property.getName(), valuename, null);
        }

        for (Property property : dataset.getProperties()) {
            assertTrue(property.isValueProfilesSet());
        }
    }

    /**
     * Checks the cache works properly by increasing the size when
     * values are initialised.
     *
     * @throws Exception
     */
    @Test
    public void Cache() throws Exception {
        Random rand = new Random();
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);

        if (dataset.values instanceof FixedCacheList) {
            for (Property property : dataset.getProperties()) {
                ((FixedCacheList) dataset.values).resetCache();
                int valueindex = rand.nextInt(property.getValues().count());
                String valuename = property.getValues().get(valueindex).getName();
                assertFalse(property.isValueProfilesSet());
                dataset.findProfiles(property.getName(), valuename, null);
                double cacheIncrease = ((FixedCacheList) dataset.values).getCacheMisses();
                assertTrue(cacheIncrease == property.getValues().count());

            }
        }
    }

    /*
     * Tries to run FindProfiles with an invalid property. The
     * expected outcome is an illegal argument exception.
     */
    @Test (expected = IllegalArgumentException.class)
    public void InvalidProperty() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        dataset.findProfiles("NOTAPROPERTY", "True", null);
        fail("NOTAPROPERTY should cause illegal argument exception");
    }

    /*
     * Tries to run FindProfiles with an empty property string. The
     * expected outcome is an illegal argument exception.
     */
    @Test (expected = IllegalArgumentException.class)
    public void EmptyProperty() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        dataset.findProfiles("", "True", null);
        fail("Empty property string should cause illegal argument exception");
    }

    /*
     * Tries to run FindProfiles with a null property string and object. The
     * expected outcome is an illegal argument exception.
     */
    @Test (expected = IllegalArgumentException.class)
    public void NullProperty() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        dataset.findProfiles((String)null, "True", null);
        fail("Empty property string should cause illegal argument exception");
        dataset.findProfiles((Property)null, "True", null);
        fail("Empty property string should cause illegal argument exception");
    }

    /*
     * Tries to run FindProfiles using an invalid value. The expected outcome
     * is an empty list.
     */
    @Test
    public void InvalidValue() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        for (Property property : dataset.getProperties()) {
            List<Profile> profiles = dataset.findProfiles(property.getName(), "NOTAVALUE", null);
            assertEquals(profiles.size(), 0);
        }
    }

    /*
     * Tries to run FindProfiles using an empty value string. The expected
     * outcome is an empty list.
     */
    @Test
    public void EmptyValue() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        for (Property property : dataset.getProperties()) {
            List<Profile> profiles = dataset.findProfiles(property.getName(), "", null);
            assertEquals(profiles.size(), 0);
        }
    }

    /*
     * Tries to run FindProfiles using a null value. The expected outcome
     * is an empty list.
     */
    @Test
    public void NullValue() throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        for (Property property : dataset.getProperties()) {
            List<Profile> profiles = dataset.findProfiles(property.getName(), null, null);
            assertEquals(profiles.size(), 0);
        }
    }

    /*
     * Runs FindProfiles for each property, then again on the results using
     * a property with the same component.
     */
    @Test
    public void SameComponent() throws Exception {
        int valueindex;
        Random rand = new Random();

        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        for (Property property : dataset.getProperties()) {
            valueindex = rand.nextInt(property.getValues().count());
            String valuename = property.getValues().get(valueindex).getName();
            List<Profile> profiles = dataset.findProfiles(property.getName(), valuename, null);
            int propertyindex =rand.nextInt(dataset.getProperties().size());
            Property secondproperty = dataset.getProperties().get(propertyindex);
            while (secondproperty.getComponent() != property.getComponent()) {
                propertyindex =rand.nextInt(dataset.getProperties().size());
                secondproperty = dataset.getProperties().get(propertyindex);
            }
            valueindex = rand.nextInt(property.getValues().count());
            String secondvaluename = property.getValues().get(valueindex).getName();
            profiles = dataset.findProfiles(secondproperty.getName(), secondvaluename, profiles);
            logger.debug(property.getName() + ":" + valuename
                    + " with " + secondproperty.getName() + ":" + secondvaluename
                    + " " + profiles.size() + " profiles found.");
            assertNotNull(profiles);
        }
    }

    /*
     * Runs FindProfiles for each property, then again on the results using
     * a property with a different component. The outcome should be an empty
     * list.
     */
    @Test
    public void DifferentComponent() throws Exception {
        Random rand = new Random();

        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32, false);
        for (Property property : dataset.getProperties()) {
            int valueindex = rand.nextInt(property.getValues().count());
            String valuename = property.getValues().get(valueindex).getName();
            List<Profile> profiles = dataset.findProfiles(property.getName(), valuename, null);
            int propertyindex =rand.nextInt(dataset.getProperties().size());
            Property secondproperty = dataset.getProperties().get(propertyindex);
            while (secondproperty.getComponent() == property.getComponent()) {
                propertyindex =rand.nextInt(dataset.getProperties().size());
                secondproperty = dataset.getProperties().get(propertyindex);
            }
            valueindex = rand.nextInt(property.getValues().count());
            String secondvaluename = property.getValues().get(valueindex).getName();
            profiles = dataset.findProfiles(secondproperty.getName(), secondvaluename, profiles);
            logger.debug(property.getName() + ":" + valuename
                    + " with " + secondproperty.getName() + ":" + secondvaluename
                    + " " + profiles.size() + " profiles found.");
            assertEquals(profiles.size(), 0);
        }
    }
}