package fiftyone.mobile.detection.webapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
class ShareUsage implements Runnable {

    /**
     * Used to signal the thread to check weather to send data.
     */
    private final Object wait = new Object();
    /**
     * Used to hold information from the jar file, default values set below.
     */
    private static String _productName = "51Degrees.mobi - Detection - Java";
    /**
     * Used to stop the thread.
     */
    private boolean stop = false;
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
    private final LinkedBlockingQueue<byte[]> queue;
    /**
     * The format wanted for dates that are generated.
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Used to detect local devices.
     */
    private static final String[] local = new String[]{"127.0.0.1",
        "0:0:0:0:0:0:0:1"};
    /**
     * Creates a logger for this class
     */
    final Logger logger = LoggerFactory.getLogger(ShareUsage.class);

    /**
     *
     * Sets the enabled state of the class.
     *
     * @param newDevicesUrl Url of potential new host.
     * @param newDeviceDetail Controls how much data is sent.
     */
    public ShareUsage(final String newDevicesUrl,
            final NewDeviceDetails newDeviceDetail) {
        // Set TimeZone to UTC
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.newDevicesUrl = newDevicesUrl;
        this.newDeviceDetail = newDeviceDetail;
        this.queue = new LinkedBlockingQueue<byte[]>();
    }

    /**
     * Attempts to send any device data that is still yet to be sent and then
     * terminates the Thread.
     */
    public void destroy() {
        logger.trace("Destroying 51Degrees.mobi ShareUsage");
        stop = true;
        synchronized (wait) {
            wait.notifyAll();
        }
        logger.trace("Destroyed 51Degrees.mobi ShareUsage");
    }

    /**
     *
     * Adds the request details to the queue for processing by the background
     * thread.
     *
     * @param request The current server request.
     * @throws XMLStreamException
     * @throws IOException
     */
    public void recordNewDevice(final HttpServletRequest request)
            throws XMLStreamException, IOException {
        final Enumeration<String> HeaderNames = request.getHeaderNames();
        final HashMap<String, String> Headers = new HashMap<String, String>();

        while (HeaderNames.hasMoreElements()) {
            final String n = (String) HeaderNames.nextElement();
            Headers.put(n, request.getHeader(n));
        }
        recordNewDevice(Headers, request.getRemoteAddr(),
                request.getRequestURI());
    }

    /**
     *
     * Adds the request details to the queue for processing by the background
     * thread.
     *
     * @param Headers HashMap containing the request headers.
     * @param HostAddress the Host Address.
     * @param URI The URI of the request.
     * @throws XMLStreamException
     * @throws IOException
     */
    public void recordNewDevice(final HashMap<String, String> Headers,
            final String HostAddress, String URI) throws XMLStreamException,
            IOException {
        // Get the new device details.
        byte[] data = getContent(Headers, HostAddress, URI, newDeviceDetail);

        if (data != null && data.length > 0) {
            // Add the new details to the queue for later processing.
            queue.add(data);
            synchronized (wait) {
                // Signal the background thread to check to see if it should
                // send queued data.
                wait.notify();
            }
        }
    }

    /**
     *
     * Sends all the data on the queue.
     *
     * @param stream Output stream to send data to.
     * @throws IOException
     * @throws XMLStreamException
     */
    private void sendData(OutputStream stream) throws IOException,
            XMLStreamException {
        GZIPOutputStream compressed = new GZIPOutputStream(stream);
        PrintWriter pw = new PrintWriter(compressed);

        pw.print("<?xml version=\"1.0\" ?>");
        pw.print("<Devices>");
        while (queue.size() > 0) {
            byte[] item = queue.poll();
            if (item != null && item.length > 0) {
                pw.print(new String(item));
            }
        }
        pw.print("</Devices>");

        pw.close();
        compressed.close();
    }

    /**
     * Runs the thread. Used to send the devices data back to 51Degrees.mobi
     * after the NewDeviceQueueLength has been reached.
     */
    @Override
    public void run() {
        do {
            try {
                synchronized (wait) {
                    // Wait for something to happen.
                    wait.wait();
                }
                // If there are enough items in the queue, or the thread is
                // being
                // stopped send the data.
                int size = queue.size();
                if (size >= Constants.NEW_DEVICE_QUEUE_LENGTH
                        || (stop == true && queue.size() > 0)) {
                    // Prepare the request including all currently queued items.
                    HttpURLConnection request = (HttpURLConnection) new URL(
                            newDevicesUrl).openConnection();
                    request.setReadTimeout(Constants.NEW_URL_TIMEOUT);
                    request.setRequestMethod("POST");
                    request.setUseCaches(false);
                    request.setRequestProperty("Content-Type",
                            "text/xml; charset=ISO-8859-1");
                    request.setRequestProperty("Content-Encoding", "gzip");
                    request.setDoInput(true);
                    request.setDoOutput(true);
                    try {
                        sendData(request.getOutputStream());
                    } catch (UnknownHostException ex) {
                        stop = true;
                    }

                    // Get the response and record the content if it's valid. If
                    // it's not valid consider turning off the functionality.
                    InputStreamReader stream = new InputStreamReader(request.getInputStream());
                    if (stream.ready()) {
                        BufferedReader response = new BufferedReader(stream);
                        if (response != null) {
                            switch (request.getResponseCode()) {
                                case 200:// OK
                                    // Ok response, do nothing
                                    break;
                                case 408:// Request Timeout
                                    // Could be temporary, do nothing.
                                    break;
                                default:
                                    // Turn off functionality.
                                    stop = true;
                                    break;
                            }
                        }
                        // Release the HttpWebResponse
                        response.close();
                    }
                }
            } catch (SecurityException ex) {
                stop = true;
            } catch (XMLStreamException ex) {
                stop = true;
            } catch (IOException ex) {
                stop = true;
            } catch (InterruptedException ex) {
                stop = true;
            } catch (IllegalStateException ex) {
                // Probably means that the instance has stopped
                // so stop any more thread processing.
                stop = true;
            }
        } while (stop == false);
    }

    /**
     *
     * Indicates if the IP address is local.
     *
     * @param address Ip address to examine.
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
     *
     * Records the information as XML data and converts to a byte array for
     * storage.
     *
     * @param Headers A Map of the connection headers.
     * @param HostAddress The address IP address of the host.
     * @param URI The URI information.
     * @param newDeviceDetail How much information to be recorded.
     * @return The XML data as a byte array.
     * @throws XMLStreamException
     * @throws IOException
     */
    private static byte[] getContent(HashMap<String, String> Headers,
            String HostAddress, String URI, NewDeviceDetails newDeviceDetail)
            throws XMLStreamException, IOException {
        // If the headers contain 51D as a setting or the request is to a web
        // service then do not send the data.
        boolean ignore = Headers.get("51D") != null || URI.endsWith("asmx");

        if (ignore == false) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            ByteArrayOutputStream ms = new ByteArrayOutputStream();
            XMLStreamWriter writer = factory.createXMLStreamWriter(ms);

            // Start writing the device
            writer.writeStartElement("Device");
            // Write the current date and time
            writer.writeStartElement("DateSent");
            writer.writeCharacters(dateFormat.format(new Date()).toString());
            writer.writeEndElement();

            writer.writeStartElement("Version");
            writer.writeCharacters(Constants.VERSION);
            writer.writeEndElement();

            writer.writeStartElement("Product");
            writer.writeCharacters(_productName);
            writer.writeEndElement();

            // Record either the IP address of the client if not local or the IP
            // address of the machine.
            // If it contains dots then its an IPV4
            if (!isLocal(HostAddress)) {
                writer.writeStartElement("ClientIP");
                writer.writeCharacters(HostAddress);
                writer.writeEndElement();
            }
            
            writer.writeStartElement("ServerIP");
            writer.writeCharacters(HostAddress);
            writer.writeEndElement();

            for (String key : Headers.keySet()) {
                // Determine if the field should be treated as a blank.
                boolean blank = false;
                for (String k : Constants.IGNORE_HEADER_FIELD_VALUES) {

                    blank = key.toLowerCase().contains(k.toLowerCase());
                    if (blank == true) {
                        break;
                    }
                }
                // Include all header values if maximumDetail is enabled, or
                // header values related to the useragent or any header
                // key containing profile or information helpful to determining
                // mobile devices.
                if (newDeviceDetail == NewDeviceDetails.MAXIMUM
                        || key.equals("user-agent") || key.equals("host")
                        || key.contains("profile") || blank) {
                    // Record the header content if it's not a cookie header.
                    if (blank) {
                        writer.writeStartElement("Header");
                        writer.writeAttribute("Name", key);
                        writer.writeEndElement();
                    } else {
                        writer.writeStartElement("Header");
                        writer.writeAttribute("Name", key);
                        writer.writeCData(Headers.get(key));
                        writer.writeEndElement();
                    }
                }
            }
            writer.writeEndElement();
            writer.close();
            return ms.toByteArray();
        }
        return null;
    }
}
