Description
Add mobile device detection to Java the easy way with 51Degrees.mobi. No cloud services, no external plug-ins, all Mozilla Public Licence source code. It's a great alternative to WURFL or DeviceAtlas.

(NOTE: Also available as a dependency through the maven central repository.)

1) Download the zip file and extract.
2) Add the core JAR file located in the "dist" directory to your java project.
3) Import the following packages:

import fiftyone.mobile.detection.*;
import fiftyone.mobile.detection.binary.*;

4) Use the following code to start detecting devices:

Provider p = Reader.create();

BaseDeviceInfo b = p.getDeviceInfo("<INSERT_USERAGENT_HERE>");

String result = b.getFirstPropertyValue("IsMobile");

if (result.equals("True")) {

System.out.println("This Is Mobile");
}

if (result.equals("False")) {
System.out.println("This Isn't Mobile");
}

p.destroy();