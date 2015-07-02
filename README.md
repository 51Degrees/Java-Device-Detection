![51Degrees](https://51degrees.com/Portals/0/Logo.png "THE Fasstest and Most Accurate Device Detection")**Device Detection for Java**

[Recent Changes](#recent-changes "Review recent major changes") | [Supported Databases](https://51degrees.com/compare-data-options?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Different device databases which can be used with 51Degrees device detection") | [Java Developer Documention](https://51degrees.com/support/documentation/java?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Full getting started guide and advanced developer documentation") | [Available Properties](https://51degrees.com/resources/property-dictionary?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "View all available properties and values")


**Server Side and Offline projects:** Initialize detector like...

```java
Provider p new Provider();
Match match = p.match("User agnet string");
```

Use like...

```java
boolean isMobile = match.getValues("IsTablet").toBool();
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
boolean isMobile = Boolean.parseBoolean(getProperty(request,"IsTablet"));
```

... to add device detection to your servlet.

**[Review All Properties](https://51degrees.com/resources/property-dictionary?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "View all available properties and values")**

## What's needed?

The simplest method of deploying 51Degrees device detection to a Java project is with Maven. Just search for [51Degrees on Maven](http://search.maven.org/#search|ga|1|51degrees "51Degrees Packages on Maven").

This GitHub repository and Maven include 51Degrees free Lite device database. The Lite data is updated monthly by our professional team of analysts. 

Data files which are updated weekly and daily, automatically, and with more properties and device combinationsare also available.

**[Compare Device Databases](https://51degrees.com/compare-data-options?utm_source=github&utm_medium=repository&utm_content=home-menu&utm_campaign=java-open-source "Compare different data file options for 51Degrees device detection")**


## Recent Changes
