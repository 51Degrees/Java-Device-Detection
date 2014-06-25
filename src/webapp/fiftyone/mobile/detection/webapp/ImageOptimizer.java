package fiftyone.mobile.detection.webapp;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class ImageOptimizer {

    private final static Logger logger = LoggerFactory
        .getLogger(ImageOptimizer.class);
    
    private static final String IMAGE_MAX_WIDTH = "IMAGE_MAX_WIDTH";
    private static final String IMAGE_MAX_HEIGHT = "IMAGE_MAX_HEIGHT";
    private static final String IMAGE_FACTOR = "IMAGE_FACTOR";
    private static final String IMAGE_WIDTH_PARAM = "IMAGE_WIDTH_PARAM";
    private static final String IMAGE_HEIGHT_PARAM = "IMAGE_HEIGHT_PARAM";
    private static final String IMAGE_DEFAULT_AUTO = "IMAGE_DEFAULT_AUTO";
    private static final String EMPTY_IMAGE_RESOURCE_NAME = "E.gif";
    private static final int DEFAULT_BUFFER_SIZE = 10240;
    private static final String AUTO_STRING = "auto";
    private static final String SCREEN_PIXEL_WIDTH = "ScreenPixelsWidth";
    private static final String SCREEN_PIXEL_HEIGHT = "ScreenPixelsHeight";
    
    /**
     * Get a list of parameters from with the query string.
     * @param request
     * @return 
     */
    private static Map<String, String> getQueryString(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();
        List<String> params = new ArrayList<String>();
        if (request.getQueryString() != null) {
            StringTokenizer ps = new StringTokenizer(
                    request.getQueryString(), "&", false);
            while (ps.hasMoreTokens()) {
                params.add(ps.nextToken());
            }
            for (String param : params) {
                StringTokenizer st = new StringTokenizer(param, "=", false);
                if (st.countTokens() == 2) {
                    parameters.put(st.nextToken(), st.nextToken());
                }
            }
        }
        return parameters;
    }
    
    private static Size getRequiredSize(HttpServletRequest request) {
        int width = 0;
        int height = 0;
        Map<String, String> parameters = getQueryString(request);
        String widthParam = getWidthParam(request);
        String heightParam = getHeightParam(request);
        
        if (parameters.containsKey(getWidthParam(request))) {
            try {
                width = Integer.parseInt(parameters.get(widthParam));
            }
            catch (NumberFormatException ex) {
                if (AUTO_STRING.equalsIgnoreCase(parameters.get(widthParam))) {
                    width = getDefaultAuto(request);
                }
                else {
                    width = getImageMaxWidth(request);
                }
            }
        }
        
        if (parameters.containsKey(heightParam)) {
            try {
                height = Integer.parseInt(parameters.get(heightParam));
            }
            catch (NumberFormatException ex) {
                if (AUTO_STRING.equalsIgnoreCase(parameters.get(heightParam))) {
                    height = getDefaultAuto(request);
                }
                else {
                    height = getImageMaxHeight(request);
                }
            }
        }
        
        return new Size(width, height);
    }

    /**
     * Adjust the height of the image so that it is not larger than the maximum 
     * allowed height.
     * @param Size 
     */
    private static void resolveHeight(int imageMaxHeight, Size size)
    {
        if (size.height > imageMaxHeight)
        {
            double ratio = (double)imageMaxHeight / (double)size.height;
            size.height = imageMaxHeight;
            size.width = (int)((double)size.height * ratio);
        }
    }

    /**
     * Adjust the width of the image so that it is not larger than the maximum 
     * allowed width.
     * @param Size 
     */
    private static void resolveWidth(int imageMaxWidth , Size size)
    {
        if (size.width > imageMaxWidth)
        {
            double ratio = (double)imageMaxWidth / (double)size.width;
            size.width = imageMaxWidth;
            size.height = (int)((double)size.height * ratio);
        }
    }
    
    private static void resolveSize(HttpServletRequest request, Size size) {
        int imageMaxWidth = getImageMaxWidth(request);
        int imageMaxHeight = getImageMaxHeight(request);
        
        // Check that a size has been requested.
        if (size.width > 0 || size.height > 0)
        {
            // Check that no dimensions are above the specifed max.
            if (size.height > imageMaxHeight || 
                size.width > imageMaxWidth)
            {
                if (size.height > size.width)
                {
                    resolveWidth(imageMaxWidth, size);
                    resolveHeight(imageMaxHeight, size);
                }
                else
                {
                    resolveHeight(imageMaxHeight, size);
                    resolveWidth(imageMaxWidth, size);
                }
            }

            // Use the factor to adjust the size.
            int factor = getFactor(request);
            size.height = factor * (int)Math.floor((double)size.height / factor);
            size.width = factor * (int)Math.floor((double)size.width / factor);

            // If the image is 0x0 after factoring then set the width to
            // the factor so that a little image gets returned.
            if (size.height == 0 && size.width == 0) {
                size.width = factor;
            }
        }
    }
    
    /**
     * Sends the requested image, at the requested size as the response.
     * @param request
     * @param response
     * @throws IOException 
     */
    static void sendImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // Check to see if this is for an empty image.
        if ("/Empty.gif".equalsIgnoreCase(request.getPathInfo())) {
            sendEmpty(response);
            return;
        }

        File sourceFile = new File(request.getServletContext().getRealPath(
            request.getPathInfo()));
        
        // Check to see if the source file exists.
        if (sourceFile.exists() == false) {
            response.sendError(404);
            return;
        }
        
        Size size = getRequiredSize(request);
        String cacheDirectory = String.format(
                "%s/cache",
                request.getServletContext().getRealPath("WEB-INF"));
        
        if (size.width == 0 && size.height == 0) {
            Map<String, String[]> results = WebProvider.getResult(request);

            // Get the screen width and height.
            try {
                if (results.get(SCREEN_PIXEL_WIDTH) != null &&
                    results.get(SCREEN_PIXEL_WIDTH).length > 0) {
                    size.width = Integer.parseInt(results.get(SCREEN_PIXEL_WIDTH)[0]);
                }
            }
            catch (NumberFormatException ex) {
                size.width = 0;
            }
            
            try {
                if (results.get(SCREEN_PIXEL_HEIGHT) != null &&
                    results.get(SCREEN_PIXEL_HEIGHT).length > 0) {
                    size.height = Integer.parseInt(results.get(SCREEN_PIXEL_HEIGHT)[0]);
                }
            }
            catch (NumberFormatException ex) {
                size.height = 0;
            }

            // Use the larger of the two values as the width as there is no
            // way of knowing if the device is in landscape or portrait
            // orientation.
            size.width = Math.max(size.width, size.height);
            size.height = 0;
        }
        
        // Ensure the size is not larger than the maximum values.
        resolveSize(request, size);
        
        if (size.width > 0 || size.height > 0)
        {
            // Get the files and paths involved in the caching.

            File cachedFile = Cache.lookup(
                    cacheDirectory, sourceFile, size.width, size.height);
            
            logger.debug(String.format(
                "Image processor is responding with image '%s' of width '%d' and height '%d'",
                sourceFile,
                size.width,
                size.height));

            // If the cached file doesn't exist or is out of date
            // then create a new cached file and serve this in response
            // to the request by rewriting the requested URL to the 
            // static file.
            if ((cachedFile.exists() == false && cachedFile.isFile()) ||
                cachedFile.lastModified() < sourceFile.lastModified())
            {
                // Shrink the image and store in the cache.
                BufferedImage image = createBufferedImage(
                        size.width, 
                        size.height,
                        new FileInputStream(sourceFile));
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(image, 
                        "image/jpeg".equals(request.getServletContext().getMimeType(
                            sourceFile.getAbsolutePath())) 
                        ? "jpg" : "png", os);
                ByteArrayInputStream imageAsStream = new ByteArrayInputStream(os.toByteArray());
                Cache.add(sourceFile, cachedFile, size.width, size.height, imageAsStream);
                imageAsStream.close();
            }
            
            // Send the response from the cached file.
            File responseFile = cachedFile.exists() ? cachedFile : sourceFile;
            FileInputStream responseStream = new FileInputStream(responseFile);
            try {
                sendResponse(response, responseStream, responseFile.length());
            }
            finally {
                responseStream.close();
            }
        } 
        else {
            // No size has been calculated for the image so return the original 
            // one.
            FileInputStream responseStream = new FileInputStream(sourceFile);
            try {
                sendResponse(response, responseStream, sourceFile.length());
            }
            finally {
                responseStream.close();
            }            
        }
    } 
    
    private static void sendResponse(
            HttpServletResponse response, 
            InputStream imageResponse,
            long imageLength) throws IOException {
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Length",
                String.valueOf(imageLength));

        BufferedOutputStream output = null;

        try {
            output = new BufferedOutputStream(
                    response.getOutputStream(),
                    DEFAULT_BUFFER_SIZE);

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = imageResponse.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            close(output);
        }
    }
    
    private static int getDefaultAuto(HttpServletRequest request) {
        return getIntegerParameter(
                request, 
                Constants.DEFAULT_AUTO, 
                50);
    }
    
    private static int getFactor(HttpServletRequest request) {
        return getIntegerParameter(
                request, 
                Constants.IMAGE_FACTOR, 
                1);
    }
    
    private static int getImageMaxHeight(HttpServletRequest request) {
        return getIntegerParameter(
                request, 
                Constants.IMAGE_MAX_HEIGHT, 
                Integer.MAX_VALUE);
    }
    
    private static int getImageMaxWidth(HttpServletRequest request) {
        return getIntegerParameter(
                request, 
                Constants.IMAGE_MAX_WIDTH, 
                Integer.MAX_VALUE);
    }

    private static String getHeightParam(HttpServletRequest request) {
        return getStringParameter(
                request, 
                Constants.IMAGE_HEIGHT_PARAM, 
                "h");
    }
    
    private static String getWidthParam(HttpServletRequest request) {
        return getStringParameter(
                request, 
                Constants.IMAGE_WIDTH_PARAM, 
                "w");
    }    
    
    /**
     * Creates a buffered image based on the image stream provided as input.
     * @param width
     * @param height
     * @param imageAsStream
     * @return
     * @throws IOException 
     */
    private static BufferedImage createBufferedImage(int width, int height,
            InputStream imageAsStream) throws IOException {
        BufferedImage original = ImageIO.read(imageAsStream);
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double widthRatio = (double) width / (double) originalWidth;
        double heightRatio = (double) height / (double) originalHeight;

        // Calculate the size of the shrunk image.
        if (widthRatio > 0 && heightRatio > 0 && widthRatio <= 1
                && heightRatio <= 1) {
            width = (int) ((double) originalWidth * widthRatio);
            height = (int) ((double) originalHeight * heightRatio);
        } else if (widthRatio > 0 && heightRatio == 0 && widthRatio <= 1) {
            width = (int) ((double) originalWidth * widthRatio);
            height = (int) ((double) originalHeight * widthRatio);
        } else if (widthRatio == 0 && heightRatio > 0 && heightRatio <= 1) {
            width = (int) ((double) originalWidth * heightRatio);
            height = (int) ((double) originalHeight * heightRatio);
        } else {
            // No change so return the original.
            return original;
        }
        
        // Check for zero values dimensions to avoid exceptions in Java.
        if (width == 0) {
            width = 1;
        }
        if (height == 0) {
            height = 1;
        }

        BufferedImage resized = new BufferedImage(
                width, 
                height,
                original.getType());
        resized.getGraphics().drawImage(
                original.getScaledInstance(
                    width,
                    height,
                    BufferedImage.SCALE_SMOOTH), 0, 0, null);
        return resized;
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ex) {
                logger.debug(
                    "Exception closing resource",
                    ex);
            }
        }
    }
    
    private static void sendEmpty(HttpServletResponse response) 
            throws IOException {
        InputStream empty = ImageOptimizer.class.getResourceAsStream(
                EMPTY_IMAGE_RESOURCE_NAME);
        int length = 0;
        empty.mark(Integer.MAX_VALUE);
        while(empty.read() >= 0) {
            length++;
        }
        empty.reset();
        response.setContentType("image/gif");
        sendResponse(response, empty, length);
    }

    /**
     * Returns and string representation of the configuaration parameter
     * requested. If the parameter does not exist then the default value
     * is returned.
     * @param servletContext
     * @param parameterName
     * @param defaultValue
     * @return 
     */
    private static String getStringParameter(
                final HttpServletRequest request,
                final String parameterName,
                final String defaultValue) {
        String parameter = request.getServletContext().getInitParameter(parameterName);
        return parameter == null ? defaultValue : parameter;
    }
    
    /**
     * Returns and integer representation of the configuaration parameter
     * requested. If the parameter does not exist then the default value
     * is returned.
     * @param servletContext
     * @param parameterName
     * @param defaultValue
     * @return 
     */
    private static int getIntegerParameter(
                final HttpServletRequest request,
                final String parameterName,
                final int defaultValue) {
        String parameter = request.getServletContext().getInitParameter(parameterName);
        if (parameter != null) {
            try {
                return Integer.parseInt(parameter);
            } catch (NumberFormatException ex) {
                // No value, nothing to do.
            }
        }
        return defaultValue;
    }
    
    /**
     * Returns the image optimisation JavaScript for the current request.
     * @param request
     * @return
     * @throws IOException 
     */
    static String getJavascript(HttpServletRequest request) throws IOException {
        Map<String, String[]> results = WebProvider.getResult(request);
        if (results != null) {
            String[] values = results.get("JavascriptImageOptimiser");
            if (values != null &&
                values.length == 1) {
                return values[0];
            }
        }
        return null;
    }
}
