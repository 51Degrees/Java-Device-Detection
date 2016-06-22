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
package fiftyone.mobile.detection.webapp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shares device related data to with 51Degrees helping us improve the 
 * quality of device detection and to spot new devices. Controlled in the 
 * Web.xml file.
 * <p>
 * You should not access objects of this class directly or instantiate new 
 * objects using this class as they are part of the internal logic.
 */
class ShareUsage implements Runnable, Closeable {

    /**
     * Used to signal the thread to check whether to send data.
     */
    private final Object wait = new Object();
    /**
     * Used to stop the thread.
     */
    private volatile boolean stop = false;
    /**
     * URL to send new device data to.
     */
    private final String newDevicesUrl;
    /**
     * Enumeration to specify the amount of detail to record about a device.
     */
    private final NewDeviceDetails newDeviceDetail;
    /**
     * Queue to hold new device detail.
     */
    private final LinkedBlockingQueue<String> queue;
    /**
     * The format wanted for dates that are generated.
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Used to detect local devices.
     */
    private static final String[] local = new String[]{
        "127.0.0.1",
        "0:0:0:0:0:0:0:1"};
   
    /**
     * Creates a logger for this class
     */
    final Logger logger = LoggerFactory.getLogger(ShareUsage.class);

    /**
     * Sets the enabled state of the class.
     *
     * @param newDevicesUrl URL of potential new host.
     * @param newDeviceDetail Controls how much data is sent.
     */
    ShareUsage(final String newDevicesUrl,
            final NewDeviceDetails newDeviceDetail) {
        // Set TimeZone to UTC
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.newDevicesUrl = newDevicesUrl;
        this.newDeviceDetail = newDeviceDetail;
        this.queue = new LinkedBlockingQueue<String>();
    }

    /**
     * Attempts to send any device data that is still yet to be sent and then
     * terminates the Thread.
     */
    @Override
    public void close() {
        logger.debug("Destroying ShareUsage");
        stop = true;
        synchronized (wait) {
            wait.notifyAll();
        }
        logger.debug("Destroyed ShareUsage");
    }

    /**
     * Runs the thread. Used to send the devices data back to 51Degrees
     * after the NewDeviceQueueLength has been reached.
     */
    @Override
    public void run() {
        logger.debug("Share usage started.");
        do {
            try {
                synchronized (wait) {
                    // Wait for something to happen.
                    wait.wait();
                }
                // If there are enough items in the queue, or the thread is
                // being
                // stopped send the data.
                if (queue.size() >= Constants.NEW_DEVICE_QUEUE_LENGTH
                        || (stop == true && queue.size() > 0)) {
                    // Prepare the request including all currently queued items.
                    HttpURLConnection request = (HttpURLConnection) new URL(
                            newDevicesUrl).openConnection();
                    request.setReadTimeout(Constants.NEW_URL_TIMEOUT);
                    request.setRequestMethod("POST");
                    request.setUseCaches(false);
                    request.setRequestProperty("Content-Type",
                            "text/xml; charset=utf-8");
                    request.setRequestProperty("Content-Encoding", "gzip");
                    request.setDoInput(true);
                    request.setDoOutput(true);
                    try {
                        sendData(request.getOutputStream());
                    } catch (UnknownHostException ex) {
                        logger.debug(String.format(
                                "Stopping usage sharing as remote name '%s' " +
                                "generated UnknownHostException exception.",
                                newDevicesUrl));
                        stop = true;
                    }

                    // Get the response and record the content if it's valid. If
                    // it's not valid consider turning off the functionality.
                    InputStreamReader stream = 
                            new InputStreamReader(request.getInputStream());
                    if (stream.ready()) {
                        BufferedReader response = new BufferedReader(stream);
                        switch (request.getResponseCode()) {
                            case 200:// OK
                                // Ok response, do nothing
                                break;
                            case 408:// Request Timeout
                                // Could be temporary, do nothing.
                                break;
                            default:
                                // Turn off functionality.
                                logger.debug(String.format(
                                    "Stopping usage sharing as remote " +
                                    "name '%s' returned status " +
                                    "description '%s'.",
                                    newDevicesUrl, 
                                    response.readLine()));                                    
                                stop = true;
                                break;
                        }
                        // Release the HttpWebResponse
                        response.close();
                    }
                }
            } catch (SecurityException ex) {
                stop = true;
                logger.debug("Security Exception: " + ex);
            } catch (XMLStreamException ex) {
                stop = true;
                logger.debug("XML Stream Exception: " + ex);
            } catch (IOException ex) {
                stop = true;
                logger.debug("IO Exception: " + ex);
            } catch (InterruptedException ex) {
                stop = true;
                logger.debug("Interrupted Exception: " + ex);
            } catch (IllegalStateException ex) {
                // Probably means that the instance has stopped
                // so stop any more thread processing.
                stop = true;
                logger.debug("Illegal State Exception: " + ex);
            }
        } while (stop == false);
        logger.debug("Share usage stopped.");
    }
    
    /**
     * Adds the request details to the queue for processing by the background
     * thread.
     *
     * @param request The current server request.
     * @throws XMLStreamException
     * @throws IOException
     */
    void recordNewDevice(final HttpServletRequest request)
            throws XMLStreamException, IOException {
        if (stop == false) {
            String xml = getContent(request, newDeviceDetail);
            if (xml != null && xml.length() > 0) {
                // Add the new details to the queue for later processing.
                queue.add(xml);
                synchronized (wait) {
                    // Signal the background thread to check to see if it should
                    // send queued data.
                    wait.notify();
                }
            }            
        }
    }

    /**
     * Adds the request details to the queue for processing by the background
     * thread.
     *
     * @param Headers HashMap containing the request headers.
     * @param HostAddress the Host Address.
     * @param URI The URI of the request.
     * @throws XMLStreamException
     * @throws IOException
     */
    void recordNewDevice(final HashMap<String, String> Headers,
            final String HostAddress, String URI) throws XMLStreamException,
            IOException {

    }

    /**
     * Sends all the data on the queue.
     *
     * @param stream Output stream to send data to.
     * @throws IOException
     * @throws XMLStreamException
     */
    private void sendData(OutputStream stream) throws IOException,
            XMLStreamException {
        GZIPOutputStream compressed = new GZIPOutputStream(stream);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    compressed,
                    "UTF-8"), true);
            try {
                pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                pw.print("<Devices>");
                String fragment = queue.poll();
                while (fragment != null) {
                    pw.print(fragment);
                    fragment = queue.poll();
                }
                pw.print("</Devices>");
            }
            finally {
                pw.close();
            }
        }
        finally {
            compressed.close();
        }
    }
    
    /**
     * Returns true if the field provided is one that should not have it's
     * contents sent to 51degrees for consideration a device matching piece of 
     * information.
     * @param field the name of the HTTP header field.
     * @return True if the field should be passed as blank.
     */
    private static Boolean isBlankField(String field) {
        for (String key : Constants.IGNORE_HEADER_FIELD_VALUES) {
            if (field.indexOf(key, 0) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates if the IP address is local.
     *
     * @param address IP address to examine.
     * @return true if local, else false.
     */
    private static boolean isLocal(String address) {
        for (String s : local) {
            if (s.equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Records the information as XML data and converts to a String for
     * storage.
     *
     * @param Headers A Map of the connection headers.
     * @param HostAddress The address IP address of the host.
     * @param URI The URI information.
     * @param newDeviceDetail How much information to be recorded.
     * @return The XML data as a String.
     * @throws XMLStreamException
     * @throws IOException if there was a problem accessing data file.
     */
    private static String getContent(final HttpServletRequest request, 
                                     NewDeviceDetails newDeviceDetail)
            throws XMLStreamException, IOException {
        String uri = request.getRequestURI();
        
        // If the headers contain 51D as a setting or the request is to a web
        // service then do not send the data.
        boolean ignore = 
                request.getHeader("51D") != null || 
                uri.endsWith("asmx");

        if (ignore == false) {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            StringWriter result = new StringWriter();
            XMLStreamWriter writer = factory.createXMLStreamWriter(result);

            // Start writing the device
            writer.writeStartElement("Device");
            // Write the current date and time
            writer.writeStartElement("DateSent");
            writer.writeCharacters(dateFormat.format(new Date()));
            writer.writeEndElement();

            writer.writeStartElement("Version");
            writer.writeCharacters(
                    ShareUsage.class.getPackage().getImplementationVersion());
            writer.writeEndElement();

            writer.writeStartElement("Product");
            writer.writeCharacters(
                    ShareUsage.class.getPackage().getImplementationTitle());
            writer.writeEndElement();

            // Record either the IP address of the client if not local or the IP
            // address of the machine.
            final String remoteAddress = request.getRemoteAddr();
            if (isLocal(remoteAddress) == false) {
                writer.writeStartElement("ClientIP");
                writer.writeCharacters(remoteAddress);
                writer.writeEndElement();
            }
            
            writer.writeStartElement("ServerIP");
            writer.writeCharacters(request.getLocalAddr());
            writer.writeEndElement();
            
            final Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                // Include all header values if maximumDetail is enabled, or
                // header values related to the useragent or any header
                // key containing profile or information helpful to determining
                // mobile devices.
                final String name = (String)headerNames.nextElement();
                if (newDeviceDetail == NewDeviceDetails.MAXIMUM ||
                        name.equals("user-agent") || 
                        name.equals("host") ||
                        name.contains("profile")) {
                    // Record the header content if it's not a cookie header.
                    writer.writeStartElement("Header");
                    writer.writeAttribute("Name", name);
                    if (isBlankField(name) == false) {
                        writer.writeCData(request.getHeader(name));
                    }
                    writer.writeEndElement();
                }                
            }            
            
            writer.writeEndElement();
            writer.close();
            return result.toString();
        }
        return null;
    }
}
