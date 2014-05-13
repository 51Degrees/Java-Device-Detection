/* *********************************************************************
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

import fiftyone.mobile.detection.NewDeviceDetails;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to record usage information which is essential to ensuring 
 * 51Degrees.mobi is optimised for performance and accuracy for current 
 * devices on the web.
 *
 * @author 51Degrees.mobi
 * @version 2.2.1.6
 */
public class ShareUsage implements Runnable {

    /**
     * Used to signal the thread to check weather to send data.
     */
    private final Object _wait = new Object();
    /**
     * Used to hold information from the jar file, default values set below.
     */
    private static String _versionNumber = "0.0",
            _productName = "51Degrees.mobi - Detection - Java";
    /**
     * Indicates if the manifest file has been read already.
     */
    private static boolean _manifestRead = false;
    /**
     * Used to stop the thread.
     */
    private boolean _stop = false;
    /**
     * URL to send new device data to.
     */
    private final String _newDevicesUrl;
    /**
     * Enumeration to specify the amount of detail to record about a device.
     */
    private final NewDeviceDetails _newDeviceDetail;
    /**
     * Queue to hold new device detail.
     */
    private final LinkedBlockingQueue<byte[]> _queue;
    /**
     * The format wanted for dates that are generated.
     */
    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Used to detect local devices.
     */
    private static final String[] _local = new String[]{"127.0.0.1", "0:0:0:0:0:0:0:1"};
    
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
    public ShareUsage(
            final String newDevicesUrl, 
            final NewDeviceDetails newDeviceDetail) {
        //Set TimeZone to UTC
        _dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        _newDevicesUrl = newDevicesUrl;
        _newDeviceDetail = newDeviceDetail;
        _queue = new LinkedBlockingQueue<byte[]>();
    }

    /**
     * Attempts to send any device data that is still yet to be sent and then
     * terminates the Thread.
     */
    public void destroy() {
        logger.trace("Destroying 51Degrees.mobi ShareUsage");                
        _stop = true;
        synchronized (_wait) {
            _wait.notifyAll();
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
    public void recordNewDevice(final HttpServletRequest request) throws XMLStreamException, IOException {
        final Enumeration HeaderNames = request.getHeaderNames();
        final HashMap<String, String> Headers = new HashMap<String, String>();

        while (HeaderNames.hasMoreElements()) {
            final String n = (String) HeaderNames.nextElement();
            Headers.put(n, request.getHeader(n));
        }
        recordNewDevice(Headers, request.getRemoteAddr(), request.getRequestURI());
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
    public void recordNewDevice(final HashMap<String, String> Headers, final String HostAddress,
            String URI) throws XMLStreamException, IOException {
        // Get the new device details.
        byte[] data = getContent(Headers, HostAddress, URI, _newDeviceDetail);

        if (data != null && data.length > 0) {
            // Add the new details to the queue for later processing.
            _queue.add(data);
            synchronized (_wait) {
                // Signal the background thread to check to see if it should
                // send queued data.
                _wait.notify();
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
    private void sendData(OutputStream stream) throws IOException, XMLStreamException {
        GZIPOutputStream compressed = new GZIPOutputStream(stream);
        PrintWriter pw = new PrintWriter(compressed);

        pw.print("<?xml version=\"1.0\" ?>");
        pw.print("<Devices>");
        while (_queue.size() > 0) {
            byte[] item = _queue.poll();
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
                synchronized (_wait) {
                    // Wait for something to happen.
                    _wait.wait();
                }
                // If there are enough items in the queue, or the thread is being
                // stopped send the data.
                int size = _queue.size();
                if (size >= Constants.NEW_DEVICE_QUEUE_LENGTH
                        || (_stop == true && _queue.size() > 0)) {
                    // Prepare the request including all currently queued items.
                    HttpURLConnection request = 
                            (HttpURLConnection)new URL(_newDevicesUrl).openConnection();
                    request.setReadTimeout(Constants.NEW_URL_TIMEOUT);
                    request.setRequestMethod("POST");
                    request.setUseCaches(false);
                    request.setRequestProperty("Content-Type", "text/xml; charset=ISO-8859-1");
                    request.setRequestProperty("Content-Encoding", "gzip");
                    request.setDoInput(true);
                    request.setDoOutput(true);
                    try {
                        sendData(request.getOutputStream());
                    } catch (UnknownHostException ex) {
                        _stop = true;
                    }

                    // Get the response and record the content if it's valid. If it's
                    // not valid consider turning off the functionality.

                    BufferedReader response = new BufferedReader(new InputStreamReader(request.getInputStream()));
                    if (response != null) {
                        switch (request.getResponseCode()) {
                            case 200://OK
                                //Ok responce,do nothing
                                break;
                            case 408://Request Timeout
                                // Could be temporary, do nothing.
                                break;
                            default:
                                // Turn off functionality.
                                _stop = true;
                                break;
                        }
                    }
                    // Release the HttpWebResponse
                    response.close();
                }
            } catch (SecurityException ex) {
                _stop = true;
            } catch (XMLStreamException ex) {
                _stop = true;
            } catch (IOException ex) {
                _stop = true;
            } catch (InterruptedException ex) {
                _stop = true;
            }

        } while (_stop == false);
    }

    /**
     *
     * Indicates if the IP address is local.
     *
     * @param address Ip address to examine.
     * @return true if local, else false.
     */
    private static boolean isLocal(String address) {
        for (String s : _local) {
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
     * @param HostAddress The address ip Address of the host.
     * @param URI The URI information.
     * @param newDeviceDetail How much information to be recorded.
     * @return The XML data as a byte array.
     * @throws XMLStreamException
     * @throws IOException
     */
    private static byte[] getContent(HashMap<String, String> Headers, String HostAddress,
            String URI, NewDeviceDetails newDeviceDetail) throws XMLStreamException, IOException {
        // If the headers contain 51D as a setting or the request is to a web service then do not send the data.
        boolean ignore = Headers.get("51D") != null
                || URI.endsWith("asmx");

        if (ignore == false) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            ByteArrayOutputStream ms = new ByteArrayOutputStream();
            XMLStreamWriter writer = factory.createXMLStreamWriter(ms);

            //Start writing the device
            writer.writeStartElement("Device");
            //Write the current date and time
            writer.writeStartElement("DateSent");
            writer.writeCharacters(_dateFormat.format(new Date()).toString());
            writer.writeEndElement();

            writer.writeStartElement("Version");
            writer.writeCharacters(fiftyone.mobile.detection.Constants.VERSION);
            writer.writeEndElement();

            writer.writeStartElement("Product");
            writer.writeCharacters(_productName);
            writer.writeEndElement();


            // Record either the IP address of the client if not local or the IP
            // address of the machine.
            //if it contains dots then its an IPV4
            if (!isLocal(HostAddress)) {
                writer.writeStartElement("ClientIP");
                writer.writeCharacters(HostAddress);
                writer.writeEndElement();
            }


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
                if (newDeviceDetail == NewDeviceDetails.Maximum
                        || key.equals("user-agent")
                        || key.equals("host")
                        || key.contains("profile")
                        || blank) {
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
