package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * A readonly list of variable length entity types held on persistent storage
 * rather than in memory. <p> Entities in the underlying data structure are
 * either fixed length where the data that represents them always contains the
 * same number of bytes, or variable length where the number of bytes to
 * represent the entity varies. <p> This class uses the offset of the first byte
 * of the entities data in the underlying data structure in the accessor. As
 * such the list isn't being used as a traditional list because items are not
 * retrieved by their index in the list, but by there offset in the underlying
 * data structure. <p> The constructor will read the header information about
 * the underlying data structure. The data for each entity is only loaded when
 * requested via the accessor. A cache is used to avoid creating duplicate
 * objects when requested multiple times. <p> Data sources which don't support
 * seeking can not be used. Specifically compressed data structures can not be
 * used with these lists. <p> Should not be referenced directly.
 *
 * @param <T> The type of BaseEntity the list will contain
 */
public class StreamVariableList<T extends BaseEntity> extends BaseList<T> {

    /**
     * Constructs a new instance of VariableList of type T
     *
     * @param dataSet The DetectorDataSet being created
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param entityFactory Factory to build entities of type T
     * @param source Reference to the underlying data structure
     */
    public StreamVariableList(Dataset dataSet, BinaryReader reader,
            Source source, BaseEntityFactory<T> entityFactory) {
        super(dataSet, reader, source, entityFactory);
    }

    /**
     * Creates a new entity of type T.
     *
     * @param offset The offset of the entity being created
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @return A new entity of type T at the offset provided
     */
    @Override
    protected T createEntity(int offset, BinaryReader reader) throws IOException {
        reader.setPos(header.getStartPosition() + offset);
        return entityFactory.create(dataSet, offset, reader);
    }

    @Override
    public Iterator<T> iterator() {
        return new StreamVariableListIterator<T>(this);
    }
}
