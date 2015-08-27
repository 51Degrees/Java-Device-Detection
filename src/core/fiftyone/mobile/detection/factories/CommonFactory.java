/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Guid;
import fiftyone.mobile.detection.entities.Version;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.util.Calendar;
import java.util.Date;

/**
 * Extension methods used to load data into the data set entity.
 */
public class CommonFactory {
    /**
     * Loads the data set headers information.
     * @param dataSet The data set to be loaded.
     * @param reader Reader positioned at the beginning of the data source.
     */
    public static void loadHeader(Dataset dataSet, BinaryReader reader) {
        //Check for an exception which would indicate the file is the 
        //wrong type for the API.
        try {
            dataSet.version = new Version(
                reader.readInt32(),
                reader.readInt32(),
                reader.readInt32(),
                reader.readInt32());
        } catch (Exception ex) {
            StringBuilder message = new StringBuilder();
            message.append("Data file is invalid. Check that the data file ");
            message.append("is decompressed and is the latest version format:");
            message.append(
                    DetectionConstants.supportedPatternFormatVersions.toString()
            );
            throw new Error(message.toString());
        }
        
        if (!DetectionConstants.supportedPatternFormatVersions.
                contains(dataSet.version)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Version mismatch. Data is version ");
            sb.append(dataSet.version);
            sb.append("for ");
            sb.append(DetectionConstants.supportedPatternFormatVersions.toString());
            sb.append("reader");
            throw new IllegalArgumentException(sb.toString());
        }
        
        // Set the enum format version value for easier if logic.
        dataSet.versionEnum = DetectionConstants.
                supportedPatternFormatVersions.getEnum(dataSet.version);
        
        // Read the common header fields.
        dataSet.tag = new Guid(reader.readBytes(16));
        switch (dataSet.versionEnum) {
            case PatternV32:
                dataSet.export =new Guid(reader.readBytes(16));
                break;
        }
        dataSet.copyrightOffset = reader.readInt32();
        dataSet.age = reader.readInt16();
        dataSet.minUserAgentCount = reader.readInt32();
        dataSet.nameOffset = reader.readInt32();
        dataSet.formatOffset = reader.readInt32();
        dataSet.published = readDate(reader);
        dataSet.nextUpdate = readDate(reader);
        dataSet.deviceCombinations = reader.readInt32();
        dataSet.maxUserAgentLength = reader.readInt16();
        dataSet.minUserAgentLength = reader.readInt16();
        dataSet.lowestCharacter = reader.readByte();
        dataSet.highestCharacter = reader.readByte();
        dataSet.maxSignatures = reader.readInt32();
        dataSet.signatureProfilesCount = reader.readInt32();
        dataSet.signatureNodesCount = reader.readInt32();
        dataSet.maxValues = reader.readInt16();
        dataSet.csvBufferLength = reader.readInt32();
        dataSet.jsonBufferLength = reader.readInt32();
        dataSet.xmlBufferLength = reader.readInt32();
        dataSet.maxSignaturesClosest = reader.readInt32();
        
        // Read the V32 headers specifically.
        if (dataSet.versionEnum == DetectionConstants.FORMAT_VERSIONS.PatternV32) {
            dataSet.maximumRank = reader.readInt32();
        }
    }
    
    /**
     * Reads a date in year, month and day order from the reader.
     *
     * @param reader Reader positioned at the start of the date
     * @return A date time with the year, month and day set from the reader
     */
    private static Date readDate(BinaryReader reader) {
        int year = reader.readInt16();
        int month = reader.readByte() - 1;
        int day = reader.readByte();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, day);
        return cal.getTime();
    }
}
