![51Degrees](https://51degrees.com/DesktopModules/FiftyOne/Distributor/Logo.ashx?utm_source=github&utm_medium=repository&utm_content=home&utm_campaign=java-open-source "THE Fastest and Most Accurate Device Detection")**Device Detection for Java**

[Recent Changes](#recent-changes "Review recent major changes") | [Supported Databases](https://51degrees.com/compare-data-options?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Different device databases which can be used with 51Degrees device detection") | [Java Developer Documention](https://51degrees.com/support/documentation/java?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Full getting started guide and advanced developer documentation") | [Available Properties](https://51degrees.com/resources/property-dictionary?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "View all available properties and values")

<sup>Need [C](https://github.com/51Degrees/Device-Detection "THE Fastest and most Accurate device detection for C") | [.NET](https://github.com/51Degrees/.NET-Device-Detection "THE Fastest and most Accurate device detection for .NET") | [PHP](https://github.com/51Degrees/Device-Detection) | [Python](https://github.com/51Degrees/Device-Detection "THE Fastest and most Accurate device detection for Python") | [Perl](https://github.com/51Degrees/Device-Detection "THE Fastest and most Accurate device detection for Perl") | [Node.js](https://github.com/51Degrees/Device-Detection "THE Fastest and most Accurate device detection for Node.js")?</sup>

**Important**

As of version 3.2.2.20-beta the Maven coordinates for the 51Degrees Java API have changed:

***Core***

```xml
<dependency>
    <groupId>com.51degrees</groupId>
    <artifactId>device-detection-core</artifactId>
    <version>3.2.2.20-beta</version>
</dependency>
```

***WebApp***

```xml
<dependency>
    <groupId>com.51degrees</groupId>
    <artifactId>device-detection-webapp</artifactId>
    <version>3.2.2.20-beta</version>
</dependency>
```

You can also get a package with examples and a package with WebApp example servlets. For more details see [Maven Central Repository](http://search.maven.org/#search|ga|1|g%3A%22com.51degrees%22).

**Server Side and Offline projects:** Initialize detector like...

```java
Provider p new Provider(StreamFactory.create("path_to_data_file", false));
Match match = p.match("HTTP User-Agent string");
```

Use like...

```java
boolean isTablet = match.getValues("IsTablet").toBool();
```

or 

```java
boolean isMobile = match.getValues("IsMobile").toBool();
```

... from within a web application server side to determine the requesting device type.

**Client Side:** Include...

```
https://[YOUR DOMAIN]/51Degrees.features.js?DeviceType&ScreenInchesDiagonal
```

... from Javascript to retrieve device type and physcial screen size information. Use Google Analytics custom dimensions to add this data for more granular analysis.

**Servlets:** Include...

```java
import fiftyone.mobile.detection.webapp.BaseServlet;
```

Extend...

```java
public class MyServlet extends BaseServlet {
```

Use...

```java
boolean isMobile = Boolean.parseBoolean(getProperty(request,"IsMobile"));
```

or

```java
boolean isTablet = Boolean.parseBoolean(getProperty(request,"IsTablet"));
```

... to add device detection to your servlet.

**[Review All Properties](https://51degrees.com/resources/property-dictionary?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "View all available properties and values")**

## What's needed?

The simplest method of deploying 51Degrees device detection to a Java project is with Maven. Just search for [51Degrees on Maven](http://search.maven.org/#search|ga|1|51degrees "51Degrees Packages on Maven").

This GitHub repository and Maven include 51Degrees free Lite device database. The Lite data is updated monthly by our professional team of analysts. 

Data files which are updated weekly and daily, automatically, and with more properties and device combinations are also available.

**[Compare Device Databases](https://51degrees.com/compare-data-options?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Compare different data file options for 51Degrees device detection")**


## Recent Changes

### Version 3.2.5.5 Changes:

* Added support for device data files which include JavaScript that can override property value results. Values returned as a result of JavaScript snippets are termed dynamic values as they are determined at runtime for the web browser rather than when the data file was generated. The screen width in pixels for a laptop or desktop computer calculated using JavaScript would be an example of a dynamic value. This feature requires a data file which includes JavaScript properties that provide dynamic property values. ScreenPixelsWidth and ScreenPixelsHeight are examples of properties that now support dynamic values.
* The auto update process now correctly handles 301 and 302 redirect response. 304 no data modified is also handled reliably.
* When enabled share usage will only send information for the very first request. The background thread is now associated with the WebProvider when enabled and is not shared across multiple providers.
* Stream factory now generates a data set that will close the underlying file source when closed.
* POMs have been changed to include title and version information in the JAR packages.