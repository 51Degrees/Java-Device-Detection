![51Degrees](https://51degrees.com/DesktopModules/FiftyOne/Distributor/Logo.ashx?utm_source=github&utm_medium=repository&utm_content=home&utm_campaign=java-open-source "THE Fastest and Most Accurate Device Detection")**Device Detection for Java**

**Important:** _51Degrees Lite data is no longer embedded into this assembly as of version 3.2. Make sure a device data file is referenced at runtime. Data files are available on [GitHub](../data) and from [51Degrees](https://51degrees.com/compare-data-options?utm_source=github&utm_medium=repository&utm_content=source-code&utm_campaign=java-open-source "Different device databases which can be used with 51Degrees device detection")._

**Maven**

As of version 3.2.2.20-beta the Maven coordinates for the WebApp package have changed:

```xml
<dependency>
    <groupId>com.51degrees</groupId>
    <artifactId>device-detection-webapp</artifactId>
    <version>3.2.2.20-beta</version>
</dependency>
```

Please remember to update your project POM files to reflect the change.

**Summary**

This package implements classes that should be used in a Web server environment.

To add device detection functionality to your servlet extend the BaseServlet like:

```java
public class MyServlet extends BaseServlet {
```

To configure the API and add listeners for the image optimiser and automatic data updates see the [Configuration Documentation](https://51degrees.com/Support/Documentation/APIs/Java-V32/Web-Apps/Configuration/Webxml?utm_source=github&utm_medium=repository&utm_content=source-code&utm_campaign=java-open-source).

For usage examples see the [Tutorials](https://51degrees.com/Support/Documentation/APIs/Java-V32/Tutorials?utm_source=github&utm_medium=repository&utm_content=source-code&utm_campaign=java-open-source) section of the documentation.