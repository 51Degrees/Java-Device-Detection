package fiftyone.mobile.detection.webapp;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class ImageOptimizer {

    private static final String EMPTY_IMAGE_RESOURCE_NAME = "E.gif";
    private static final String SRC = "src";
    private static final int DEFAULT_BUFFER_SIZE = 10240;
    private static final String AUTO = "auto";
    private static final String SCREEN_PIXEL_WIDTH = "ScreenPixelsWidth";
    private static final String SCREEN_PIXEL_HEIGHT = "ScreenPixelsHeight";
    private String widthParam = "w";
    private String heightParam = "h";
    private int defaultAuto = 50;
    private int imageMaxWidth = Integer.MAX_VALUE;
    private int imageMaxHeight = Integer.MAX_VALUE;
    private int factor = 1;
    private ServletContext servletContext;
    private Cache cache;

    ImageOptimizer(ServletContext servletContext, Cache cache) {
        this.servletContext = servletContext;
        this.cache = cache;
        int maxWidthParam = GetIntegerParameter(servletContext, Constants.IMAGE_MAX_WIDTH);
        if (maxWidthParam != Integer.MIN_VALUE) {
            imageMaxWidth = maxWidthParam;
        }
        int maxHeightParam = GetIntegerParameter(servletContext, Constants.IMAGE_MAX_HEIGHT);
        if (maxHeightParam != Integer.MIN_VALUE) {
            imageMaxHeight = maxHeightParam;
        }
        int factorParam = GetIntegerParameter(servletContext, Constants.IMAGE_FACTOR);
        if (factorParam != Integer.MIN_VALUE) {
            factor = factorParam;
        }
        int defaultAutoParam = GetIntegerParameter(servletContext, Constants.DEFAULT_AUTO);
        if (defaultAutoParam != Integer.MIN_VALUE) {
            defaultAuto = defaultAutoParam;
        }

        String localWidthParam = servletContext.getInitParameter(Constants.IMAGE_WIDTH_PARAM);
        if (localWidthParam != null) {
            this.widthParam = localWidthParam;
        }

        String localHeightParam = servletContext.getInitParameter(Constants.IMAGE_HEIGHT_PARAM);
        if (localHeightParam != null) {
            this.heightParam = localHeightParam;
        }

    }

    void image(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        InputStream imageAsStream;
        if ("/Empty.gif".equals(request.getPathInfo())) {
            imageAsStream = serveEmpty(response);
        } else {

            // Get a list of parameters from with the query string.
            Map<String, String> map = new HashMap<String, String>();
            List<String> params = new ArrayList<String>();
            if (request.getQueryString() != null) {
                StringTokenizer ps = new StringTokenizer(request.getQueryString(), "&", false);
                while (ps.hasMoreTokens()) {
                    params.add(ps.nextToken());
                }
                for (String param : params) {
                    StringTokenizer st = new StringTokenizer(param, "=", false);
                    map.put(st.nextToken(), st.nextToken());
                }
            }

            String requestedImage = request.getPathInfo();
            if (requestedImage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String strWidth = map.get(widthParam);
            String strHeight = map.get(heightParam);
            if (strWidth == null && strHeight == null) {
                imageAsStream = servletContext
                        .getResourceAsStream(requestedImage);
            }
            else {
                int width = parseDimension(strWidth);
                int height = parseDimension(strHeight);

                if (width == 0 || height == 0) {
                    FiftyOneDegreesListener listener = (FiftyOneDegreesListener) servletContext
                            .getAttribute(Constants.WEB_PROVIDER_KEY);

                    Map<String, String[]> results = listener.getProvider()
                            .getResults(request);
                    String[] widthTmp = results.get(SCREEN_PIXEL_WIDTH);
                    String[] heightTmp = results.get(SCREEN_PIXEL_HEIGHT);
                    if (widthTmp != null && heightTmp != null) {
                        try {
                            width = Integer.parseInt(widthTmp[0]);
                            height = Integer.parseInt(heightTmp[0]);

                            width = Math.max(width, height);
                            height = 0;
                        } catch (NumberFormatException e1) {
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                }

                ImageSize size = new ImageSize(width, height, imageMaxWidth, imageMaxHeight, factor);
                size.resolveSize();

                imageAsStream = getImageAsStream(size.getWidth(), size.getHeight(),
                        getFullRequest(request), requestedImage);
                String contentType = servletContext.getMimeType(requestedImage);
                response.setContentType(contentType);
            }
        }
        if (imageAsStream == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Length",
                String.valueOf(imageAsStream.available()));

        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        try {
            input = new BufferedInputStream(imageAsStream, DEFAULT_BUFFER_SIZE);
            output = new BufferedOutputStream(response.getOutputStream(),
                    DEFAULT_BUFFER_SIZE);

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            close(imageAsStream);
            close(output);
            close(input);
        }
    }

    private int parseDimension(String str) {
        if (AUTO.equals(str))
        {
            return defaultAuto;
        }
        else
        {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    private InputStream getImageAsStream(int width, int height,
            String fullRequest, String imageLocal) throws IOException {
        InputStream cacheStream = cache.lookup(imageLocal, width, height);
        if (cacheStream == null) {
            InputStream contextStream = servletContext
                    .getResourceAsStream(imageLocal);
            if (contextStream == null) {
                return null;
            } else {
                InputStream imageAsStream;
                BufferedImage image = createBufferedImage(width, height,
                        contextStream);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(image, "image/jpeg".equals(servletContext
                        .getMimeType(imageLocal)) ? "jpg" : "png", os);
                imageAsStream = new ByteArrayInputStream(os.toByteArray());
                cache.add(imageLocal, width, height, imageAsStream);
                imageAsStream.close();
            }
            cacheStream = cache.lookup(imageLocal, width, height);
        }
        return cacheStream;
    }

    private BufferedImage createBufferedImage(int width, int height,
            InputStream imageAsStream) throws IOException {
        BufferedImage original = ImageIO.read(imageAsStream);
        int w = original.getWidth();
        int h = original.getHeight();

        double widthRatio = (double) width / (double) w;
        double heightRatio = (double) height / (double) h;

        boolean set = false;
        if (widthRatio > 0 && heightRatio > 0 && widthRatio <= 1
                && heightRatio <= 1) {
            width = (int) ((double) w * widthRatio);
            height = (int) ((double) h * heightRatio);
            set = true;
        } else if (widthRatio > 0 && heightRatio == 0 && widthRatio <= 1) {
            width = (int) ((double) w * widthRatio);
            height = (int) ((double) h * widthRatio);
            set = true;
        } else if (widthRatio == 0 && heightRatio > 0 && heightRatio <= 1) {
            width = (int) ((double) w * heightRatio);
            height = (int) ((double) h * heightRatio);
            set = true;
        }

        if (set) {
            BufferedImage resized = new BufferedImage(width, height,
                    original.getType());
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, width, height, 0, 0, w, h, null);
            g.dispose();
            return resized;
        } else {
            return original;
        }
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFullRequest(HttpServletRequest request) {
        return request.getRequestURI()
                + (request.getQueryString() != null ? "?"
                + request.getQueryString() : "");
    }

    private InputStream serveEmpty(HttpServletResponse response) {
        response.setContentType("image/gif");
        return getClass().getResourceAsStream(EMPTY_IMAGE_RESOURCE_NAME);
    }

    private int GetIntegerParameter(final ServletContext servletContext, final String parameterName) {
        String parameter = servletContext.getInitParameter(parameterName);
        if (parameter != null) {
            try {
                return Integer.parseInt(parameter);
            } catch (NumberFormatException ex) {
                // No value, nothing to do.
            }
        }
        return Integer.MIN_VALUE;
    }
}
