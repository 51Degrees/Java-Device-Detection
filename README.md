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

### Version 3.2.3.5 Changes:
Focus on improving tests, cleaning up documentation and API efficiency. Version highlights:

Efficiency: IntegerFactory and IntegerEntity removed. More efficient integer lists are used instead, hence reducing the number of objects and significantly reducing memory consumption in memory mode.

Build.xml has been removed as it no longer serves any purpose.

Tests: general improvements and more realistic values. Memory tests for array and memory modes now use a file size multiplier. New tests added to validate stream and memory detections produce the same results.

General: indentation and javadoc improvements.

IFixedList removed, ISimpleList is used instead. New interface allows to retrieve a reference to a range of values in a list.

### Version 3.2.2.20 Changes:
Project structure has been changed to Maven project.

The IDisposable interface has been removed, all classes that provide methods to free resources implement Closeable instead.

Provider supports retrieving match results using device IDs generated from previous matches.

Automatic update has been re-designed to be more efficient and resilient. Returns an update status instead of a boolean.

The cache has been upgraded to use a least recently used (LRU) design. This removes the need to service the cache in a background thread, and results in a more predictable performance under load.

Duplicate code has been consolidated with a focus on improving documentation and implementing recommendations from code analysis and peer reviews. Testing coverage has been included with initial unit tests for new features.

Consistent examples have been added in parallel with APIs in other languages. The examples are designed to highlight a specific use case for the API. They relate to example specific documentation on the 51Degrees web site under Support -> Documentation -> Java.

### Version 3.2.1.9 Changes:
Automatic update function no longer uses the memory to store data downloaded 
from 51degrees.com update server. Instead a temporary file is used. 
This should significantly reduce the memory impact of the auto update process.

The API now supports 51Degrees data files of version 3.2 as well as 3.1 data 
files. The data files of version 3.2 are on average 20% smaller than the 3.1 
data files due to the changes to the internal data structure.

The core API (both Pattern and Trie) has been updated to perform device 
detection with multiple HTTP headers.

The API has been updated to implement caching for all major components that 
require lookup/detection. This change reflects the fact that in the real-world 
applications/websites subsequent requests are probable and that some user 
agents that are encountered more often than others. This change should improve 
detection times even further.

Breaking change: The embedded data file no longer exists. This makes the JAR 
very light but you will need to obtain the data file separately.

Breaking change: Provider object can no longer be instantiated without 
specifying the dataset to be used. See the breaking changes section.

### Version 3.1.8.4 Changes:

Added several methods to pre-load data on the Dataset object:
  initSignatures()
  initNodes()
  initProfiles()
  initComponents()
  initProperties()
  initValues()
  initSignatureRanks()
  
Added iterators classes, StreamVariableListIterator and StreamFixedListIterator.
These fixed a NotSupportedOperation exception on some Dataset collections when
created with a StreamFactory.

Modified auto update logic where the auto update would always replace the
original data file regardless of the version due to an issue with file name
comparison logic. Now the data file will only be replaced if the new data file
has a more recent published date or a different name.

Modified the behaviour of the file output stream when writing the new data
to the original file. The output stream will now attempt to pen the file
5 times and wait for two seconds if the attempt was unsuccessful. This
is intended to help prevent exceptions caused by garbage collector not
being able to clear the dataset and remove file locks before the file is
written to.

### Version 3.1.7.2 Changes:

Addressed the "Not yet implemented" exception that was thrown when trying to 
invoke the findProfile method of the dataset object. Exception was only thrown 
when Provider was constructed using the StreamFactory.

Addressed an issue where findProfile method would often return the same profile 
for different profile IDs.

### Version 3.1.6.1 Changes:

Addressed a file lock exception when updating data.

### Version 3.1.5.5 Changes:

Updated embedded data file.

### Version 3.1.5.4 Changes:

Added missing descriptions in comments that prevented generating Javadoc.

Updated Lite data files (both Trie and Pattern) to the latest version.

Fixed an issue in AutoUpdate that would cause the amount of threads created by
the auto update to increase with each iteration, using up more memory with
every iteration.

The error message in AutoUpdate is now more informative. Server response code
is now printed along with the short message explaining what the cause might be.

### Version 3.1.4.10 Changes:

getBinaryFilePath in webapp/WebProvider has been modified to address path issue on some systems.

Changed to use JDK 1.7 in the Netbeans project and the build.xml. Maven builds
will now use JDK 1.7.

Added a new property to the Signature.java entity class called Rank. This
property returns the rank of the signature, where 0 is the most popular
signature, to indicate the popularity of a signature compared to others.

Changed AutoUpdate so lite data always gets updated. Also fixed a bug where the
publishedDate was being queried rather than nextUpdate, which means update
requests would be made far more than necessary.

No longer uses session to store detection results.

Now only one temp data file is created at a time.

Changed getValues and getSignatures to avoid using a list before creating an
array to improve performance and reduce memory usage.

Fixed a defect in getSignatures where the wrong signatures were being returned.

Fixed error when building servlet java docs using the ant script.

Removed illegal characters that would sometimes cause compilation errors.

### Version 3.1.3.2 Changes:

Bug fixes:

Client side javascript no longer writes names with '/' characters in them which
would cause parser errors. They are now stripped out completely.

Fixed bug where Unix type systems would not cache images in the correct directory.

### Version 3.1.2.16

Known Issues:

Exception "Device data download failed: sun.security.validator.ValidatorException:
PKIX path building failed: 
sun.security.provider.certpath.SunCertPathBuilderException: 
unable to find valid certification path to requested target" thrown when auto 
updating the data file due to GoDaddy root authority SSL certificate used by
51Degrees not being present in some Java certificate stores.

Changes:

Added support for secondary HTTP headers such as Device-Stock-UA.

Changed the example project to use the Map<String, String[]> data structure
used to cache the results of a detection in the requests attribute list, and 
the session when available.

Temporary files used by the WebProvider are now cleaned up correctly when the 
WebProvider is destroyed and a new active provider is created.

Change the Disposable interface to no longer throw the IOException.

Refactored WebProvider, ProfileOverride, Listener and other classes to align
to the .NET reference implementation and address minor defects with inconsistent
servlet behaviour.

MD5 hash eTags now used for consistency with other implementations.

Client javascript and image optimiser classes are now internal, static and 
not publicly accessible.

Fixed defects in the 51Degrees.features.js javascript generation.

Added checks for malformed querystrings with image optimiser.

Moved functionality from the listener to the WebProvider.

Improved the quality of resized images from the optimiser.

Removed memory constraint checking code in auto updater as V3 does not need
to load the data file into memory in order to validate it.

Stopped bandwidth server processing when the bandwidth monitoring javascript 
is not included in the data set.

Added the empty gif resource to the webapp package for image optimiser.

The Performance Monitoring cookie now has a path set, preventing multiple
instance of the cookie from building up.

Performance Monitoring is no longer calculated on pages going through
the 51D filter, such as optimised images.

The Performance Monitor will now process all 51D_Bandwidth cookies until
a complete one is found.

### Version 3.1.1.3

Important: version 3.0 format data files will not work with version 3.1 APIs.

Fixed user agent detection cache so that it works with the provider.

Upgraded detection algorithm to support version 3.1 data format which contains
presorted lists for Nearest and Closest methods improving performance.

Address minor issues with code formatting and comments.

Fixed intermittent issue when no cookies present with profile override.

### Version 3.0.7.3 Changes:

IMAGE_WIDTH_PARAM and IMAGE_HEIGHT_PARAM now default to 'w' and 'h' instead of
'width' and 'height', respectively.

Added IMAGE_DEFAULT_AUTO. If an image is requested with a width or height set to
'auto', the parameter will be changed to the value set in 'IMAGE_DEFAULT_AUTO'.
This is most useful for clients without javascript that should still be served
images, such as search crawlers. Defaults to 50.

Added a useragent detection cache.
