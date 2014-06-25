package fiftyone.mobile.detection.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

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
 * If a copy of the MPL was not distributed with this cacheFile, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
class Cache {

    /**
     * The number of "chunks" the image cacheFile location should be broken 
     * into.
     */
    private static final int SPLIT_COUNT = 5;

    /**
     * Looks for the local image with the width and height in the cache.
     * @param imageLocal path to the local image cacheFile.
     * @param width of the image
     * @param height of the image
     * @return null if the image with the specified height and width does not
     * exist in the cache.
     * @throws IOException 
     */
    @SuppressWarnings("resource")
    synchronized static File lookup(String cacheDirectory, File physicalPath, 
            int width, int height) throws IOException {
        return getFile(cacheDirectory, physicalPath.getAbsolutePath(), width, height);
    }

    /**
     * Adds the image at the width and height provided to the cache.
     * @param imageLocal
     * @param width
     * @param height
     * @param imageAsStream
     * @throws IOException 
     */
    synchronized static void add(File physicalPath, File cacheFile, int width, 
            int height, InputStream imageAsStream) throws IOException {
        new File(cacheFile.getParent()).mkdirs();
        cacheFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(cacheFile);

        int read = 0;
        byte[] bytes = new byte[1024 ^ 2];

        while ((read = imageAsStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
    }

    /**
     * Gets a base 64 encoded version of the image URL.
     * @param imageLocal
     * @return
     * @throws UnsupportedEncodingException 
     */
    private static String getEncodedFile(String imageLocal) throws UnsupportedEncodingException {
        String encoded = Base64.encodeBase64String(imageLocal.getBytes("US-ASCII"));

        StringBuilder sb = new StringBuilder();
        int iteration = 0;
        int startIndex = iteration * SPLIT_COUNT;
        while (startIndex < encoded.length()) {
            int length = encoded.length() - startIndex;
            sb.append(encoded.substring(
                    startIndex,
                    (length > SPLIT_COUNT ? SPLIT_COUNT : length) + startIndex));
            iteration++;
            startIndex = iteration * SPLIT_COUNT;
            if (startIndex < encoded.length()) {
                sb.append("/");
            }
        }

        return sb.toString();
    }

    /**
     * Gets the full path to the image in the cache at the specified width and
     * height.
     * @param imageLocal
     * @param width
     * @param height
     * @return
     * @throws UnsupportedEncodingException 
     */
    private static File getFile(String cacheDirectory, String imageLocal, 
            int width, int height) 
            throws UnsupportedEncodingException {

        // Create the cached cacheFile based on the base 32 encoding of the 
        // local image cacheFile plus the width and height.
        File file = new File(String.format(
                "%s/%s/%s/%s.%s",
                cacheDirectory,
                width,
                height,
                getEncodedFile(imageLocal),
                imageLocal.substring(imageLocal.length() - 3)));

        // Make sure the directory is created before the cacheFile is returned for
        // use.
        File parent = file.getParentFile();
        if (parent.exists() == false) {
            parent.mkdirs();
        }

        return file;
    }
}