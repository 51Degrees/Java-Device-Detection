
Description:

    The 51Degrees Java API is designed to be easy and quick to deploy and use even for 
    low skill developers while providing accurate and reliable results in just a small 
    fraction of a second. The API features perks like automatic image optimiser, 
    sending detection results to the client side to integrate with existing JavaScript 
    logic, data file metadata and more.
    HomePage: <https://51degrees.com>

For automatic updates, tablet, operating system and screen dimension detection
see 51Degrees Premium Data at https://51degrees.com/products/device-detection

Benefits of pay-for data subscription include:

    -Automatic Weekly Updates
    -Bandwidth Monitoring
    -Device Type(such as Tablet)
    -Operating System
    -Screen Dimension
    -Input Method

    See https://51degrees.com/compare-data-options to compare the various device 
    data options.

Contents:
    -SRC: Contains the java files used to make the 51Degrees.mobi JAR files.
    -core: Contains the java files needed to make
      fiftyone.mobile.detection.jar. The core functionality for detection
      independent of web servers is contained in this package.
    -webapp: Contains the java files needed to make
      fiftyone.mobile.detection.webapp.jar. Used to support servlet web server
      environments.
    -console: An example console application to test the core and provide
      example code to perform detections and read property values.
    -batch: An example console application to read a file of user agents
      and return details about each one.
    -servlets: Example servlets used to demonstrate methods of using 
      51Degrees with a web server.
    -resources: Contains resources used by the example projects.
    -51Degrees-Lite.dat:  The free lite data used with the core package.
      Should be referenced as an embedded resource by the core package.
    -*.jpg: Example high resolution images used by the servlets Gallery example.
    -lib: External packages needed to build the fiftyone.mobile.detection.webapp
      package.
    -build.xml: An Apache Ant build script used to build the src. For more
      information see <http://ant.apache.org/>.
      To build the core files use "ant core". To build the webapp files a copy
      of servlet-api.jar is needed. Place the file in the same directory as the
      build script and use "ant" or "ant webapp" to build both JAR files.

For more information and documentation about this project please visit 
<https://51degrees.com/support/documentation/java>.

This project uses SLF4J, using the MIT licence.
http://www.slf4j.org/license.html

--------------------------------------------------------------------------------
Version 3.1.8.4
Changes:

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

Version 3.1.7.2
Changes:

Addressed the "Not yet implemented" exception that was thrown when trying to 
invoke the findProfile method of the dataset object. Exception was only thrown 
when Provider was constructed using the StreamFactory.

Addressed an issue where findProfile method would often return the same profile 
for different profile IDs.

Version 3.1.6.1
Changes:

Addressed a file lock exception when updating data.

Version 3.1.5.5
Changes:

Updated embedded data file.

Version 3.1.5.4
Changes:

Added missing descriptions in comments that prevented generating Javadoc.

Updated Lite data files (both Trie and Pattern) to the latest version.

Fixed an issue in AutoUpdate that would cause the amount of threads created by
the auto update to increase with each iteration, using up more memory with
every iteration.

The error message in AutoUpdate is now more informative. Server response code
is now printed along with the short message explaining what the cause might be.

Version 3.1.4.10

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

Version 3.1.3.2

Bug fixes:

Client side javascript no longer writes names with '/' characters in them which
would cause parser errors. They are now stripped out completely.

Fixed bug where Unix type systems would not cache images in the correct directory.

Version 3.1.2.16

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

Version 3.1.1.3

Important: version 3.0 format data files will not work with version 3.1 APIs.

Fixed user agent detection cache so that it works with the provider.

Upgraded detection algorithm to support version 3.1 data format which contains
presorted lists for Nearest and Closest methods improving performance.

Address minor issues with code formatting and comments.

Fixed intermittent issue when no cookies present with profile override.

Version 3.0.7.3

IMAGE_WIDTH_PARAM and IMAGE_HEIGHT_PARAM now default to 'w' and 'h' instead of
'width' and 'height', respectively.

Added IMAGE_DEFAULT_AUTO. If an image is requested with a width or height set to
'auto', the parameter will be changed to the value set in 'IMAGE_DEFAULT_AUTO'.
This is most useful for clients without javascript that should still be served
images, such as search crawlers. Defaults to 50.

Added a useragent detection cache.

Version 3.0.6.5

Summary:

Image optimiser has been given more configuration options to restrict the number
of images that can be generated. The following 5 image options can now be
configured in web.xml:

IMAGE_MAX_WIDTH: integer - determines the maximum width an image can be resized
  to. Aspect ratio will be retained if possible to do so.
IMAGE_MAX_HEIGHT: integer - determines the maximum height an image can be
  resized too. Aspect ratio will be retained if possible to do so.
IMAGE_FACTOR: integer - controls a factor that image height and width must be
  resized too. For instance, if IMAGE_FACTOR is 2 then an image's height and
  width will be rounded down to the nearest size divisible by 2. This is too
  restrict images being made that are only very slightly different.
IMAGE_WIDTH_PARAM:* string - controls the width query string used in the url that
  the image resizer responds to.
IMAGE_HEIGHT_PARAM:* string - controls the width query string used in the url
  that the image resizer responds to.

*Note that if IMAGE_WIDTH_PARAM or IMAGE_HEIGHT_PARAM are changed from their
default then the new value must be placed in the FODIO constructor.
For instance, if they're changed to 'w' and 'h' then the FODIO constructor would
be new FODIO('w', 'h');

The data format has changed slightly to allow faster property value retrieval
from properties in a future version.

WebApp now creates temporary data files to work from in the WEB-INF folder when
in streaming mode. This means the data file is never locked by the web server so
the data file can be easily replaced. The original file is monitored by the
server and reloaded if the file is modified. This change was made to allow
automatic updates.

The API now also supported automatic data updates. The core package can fetch
updates with a new static method:
  AutoUpdate.update(String licenseKey, String dataFilePath);
Where licenseKey is a Premium or Ultimate license acquired from 51Degrees and
dataFilePath is that path to write the data too.
WebApp can also get data updates by specifying a data file path in web.xml
(BINARY_FILE_PATH) and placing a .lic license file in the site's WEB-INF folder.
New data is then automatically and regularly checked.

Fixed a bug where MEMORY_MODE was not respected in web.xml. Streaming was always
being used.

Breaking Changes:

The WebApp now looks in the WEB-INF folder for device data rather than the root
of the web site. A full path can also be specified in the web.xml for
BINARY_FILE_PATH.

The image optimiser no longer uses the 'src' query string. Instead, an image url
must be preceded by '51D/':
<img src="E.gif" data-src="51D/Images/Test.jpg&width=auto" />
The width and height query strings are unchanged.

The core js file is now accessed with:
51D/core.js

Version 3.0.3

Summary:

Device detection algorithm is over 100 times faster. Regular expressions and 
Levenshtein distance calculations are no longer used. The device detection 
algorithm performance is no longer limited by the number of device combinations
contained in the dataset.

Multi-threading is no longer used within the matching algorithm.

When used in a web environment the detection results are stored within the 
session when available and are no longer cached separately.

Two modes of operation are available:

1.  Memory  the detection data set is loaded into memory and there is no 
  continuous connection to the source data file. Slower initialisation time
  but faster detection performance.
2.  Stream  relevant parts of the data set are loaded into memory when required
  and cached to improve performance. Rapid initialisation time but 
  approximately 50% slower detection performance. This mode is used when 
  operated in a web environment.

JPG and PNG format images can be optimised to improve performance.*

Bandwidth and response times can be monitored to understand in real time the 
end users experience.#

Feature detection is used to override properties in the data set to provide
details such as iPhone model or the screen orientation. These values become 
available to the server from the 2nd request from the device onwards.#

* Automatic image optimisation available in paid for Ultimate version of data 
set only.

# feature available in paid for Ultimate version of data set only.

Breaking Changes:

The Confidence property has been removed and replaced with Difference which
returns a value which indicates the difference between the target user agent and
the signature found. Therefore a value of 0 means 100% confidence and greater
 values indicate increasingly lower confidence.  Any implementation using the 
 Confidence property needs to be revised to incorporate the new logic. 
 The Difference property associated with the detection result can be obtained 
 using the following method.
 
  Match match = p.match("USERAGENT");
  int difference = match.getDifference();

Core:

The entity model used for matching has been changed entirely. Any direct
references to these classes will need to be revised.

Two factories are now available to create detection data sets.

1.  FiftyOne.Foundation.Mobile.Detection.Factories.MemoryFactory  
  provides Create methods to build data sets held in memory. An optional
  parameter can be specified to initialise the data set where all references
  to related objects are set after the data set has been loaded into memory.
  This parameter increases initialisation time and memory consumption but will
  further improve initial performance. Recommended for use in offline analysis
  of log files or centralised web services where memory overhead and 
  initialisation time are less important.

2.  FiftyOne.Foundation.Mobile.Detection.Factories.StreamFactory  
  provides Create methods to build data sets that retain a connection to the 
  data set source and load required data only when needed. Recommended for web
  environments. Default operation when deployed into a web project.

The Provider constructor requires an initialised data set using one of the 
factories. Therefore the method to create a Provider instance where the source
is either a file name string or a byte array becomes.

Provider p = new Provider(StreamFactory.create("FILENAME"));

Detection is now initiated using the Match method of the provider where the 
evidence parameter can either be a User-Agent string or a collection of HTTP 
header values. 

Match match = p.match(EVIDENCE);

The resulting match class has a method called getValues which can take a 
property name as key and returns the values associated with the device for the
property provided. The following code would return the values for the property
IsMobile.

Values values = match.getValues("IsMobile");

The values class can be used to retrieve the value or values in different 
strongly typed variables. For example to return the result of IsMobile as a
boolean the following method would be used.

boolean isMobile = match.getValues("IsMobile").toBool();

The match methods of the Provider can also be provided with an existing match 
object returned from a previous match, or via the CreateMatch method. The Match 
object provided is updated with the results of the match. These methods reduce 
strain on the garbage collector when very high volumes of detections are being
performed as the memory allocated to the Match instance can be reused and new
memory does not need to be allocated for every match.

Match Class:

The Match class contains all the information associated with the detection. It
provides the following properties which will be relevant to many developers.

Property  Description
Signature  If a specific signature could be identified the signature instance.
Results    SortedList of properties and values for the match.
Difference  The numeric difference between the target user agent and the match.
      Numeric sub strings of the same length are compared based on the
      numeric value. Other character differences are compared based on the
      difference in ASCII values of the two characters at the same
      positions.
DeviceId  A unique string Id for the device returned made up of the profile 
      Ids which form the device.
Profiles  An array of the profiles returned from the match.
ProfileIds  A dictionary of profile Ids when the key is the component Id the
      profile relates to.
UserAgent  The user agent of the matching signature with the irrelevant
      characters removed.

The following properties are also exposed for performance tuning and diagnostics
purposes.

Property      Description
Method        The method used to complete the detection.
          Exact - The signature returned matches precisely with the
          target user agent and was the only signature evaluated.
          Numeric - The signature returned matches the target user
          agent with only minor differences between numeric numbers.
          Nearest - The Nearest match method will return results for 
          sub strings that are precisely present in the target user
          agent but offset by 1 or more characters. The signature with
          the lowest difference in sub string positions is returned. 
          The method may not find a matching signature. 
          Closest - No signature matched precisely and some relevant
          characters may be different between the returned signature
          and the target user agent. The Difference property should be
          used to determine the degree of difference.
          None  A match could not be determined.
SignaturesCompared  The number of signatures compared to the target to identify
          a Closest or Nearest match. Will return zero for any other
          methods.
SignaturesRead    The number of signatures read from the data set in
          processing the match.
RootNodesEvaluated  The number of root nodes checked in the data set in 
          processing the match.
NodesEvaluated    The number of nodes read from the data set in processing
          the match.
StringsRead      The number of strings read from the data set in processing
          the match.

Webapp:

In the example web app the two stages outlined above are inserted into the 
Servlet lifecycle.

-Initialisation-
The core is initialised on a ServletContextListener.  During 
contextInitialized(ServletContextEvent contextEvent) the Provider object is
created. The type of Reader used is defined in the web.xml file of the Servlet 
container.  The Provider instance is then pushed into the global ServletContext 
object.

-Query-
The querying of the created Provider is then queried on the example Serlvet.
The Servlet fetches the Provider from the global ServletContext and then queries
it. This produces a Match result that can be inspected its public API.

-Configuration-
The type of Reader (memory or streaming) that is used to create the Provide is
defined in the web.xml file.  The <param-name> is MEMORY_MODE and the two 
potential values are TRUE or FALSE depending of required behaviour.


-Image Optimiser-

The WebApp can be used to optimise your pages images, resizing to be more
appropriate for the viewing device. The optimiser works by passing an image
path and the desired size to a Java listener, which then resizes the image,
caches it and then serves it. The cache means that an image in a particular size
is only created once. To do this some 51Degrees javascript needs to be added to
the page and the img tags to be written in a slightly different way.

The following img tag:
<img src="Images/Test.jpg" />

Becomes:
<img src="E.gif" data-src="51D/Images/Test.jpg&w=auto" />

E.gif is 1x1 pixel place holder for the image, and the data-src attribute should
contain the location of the path of image preceded with '51D/'. The optimiser
script looks for img tags with the data-src attribute and calculates the size
the image should be.

The following script should then be included in your page after the body tag:
<script src="51D/core.js"></script>
<script>
  new FODIO();
</script>

Additional options can also be set for image resizing to restrict how many
images are created. All of these parameters are set in the 51Degrees.php file:

IMAGE_MAX_WIDTH: integer - determines the maximum width an image can be resized
  to. Aspect ratio will be retained if possible to do so.
IMAGE_MAX_HEIGHT: integer - determines the maximum height an image can be
  resized too. Aspect ratio will be retained if possible to do so.
IMAGE_FACTOR: integer - controls a factor that image height and width must be
  resized too. For instance, if IMAGE_FACTOR is 2 then an image's height and
  width will be rounded down to the nearest size divisible by 2. This is too
  restrict images being made that are only very slightly different.
IMAGE_WIDTH_PARAM:* string - controls the width query string used in the url
  that the image resizer responds to. Defaults to 'w'.
IMAGE_HEIGHT_PARAM:* string - controls the width query string used in the url
  that the image resizer responds to. Defaults to 'h'.
IMAGE_DEFAULT_AUTO: integer - If an image is requested with a width or height set to
  'auto', the parameter will be changed to the value set in 'DEFAULT_AUTO'.
  This is most useful for clients without javascript that should still be
  served images, such as search crawlers. Defaults to 50.

*Note that if IMAGE_WIDTH_PARAM or IMAGE_HEIGHT_PARAM are changed from their
default then the new value must be placed in the FODIO constructor.
For instance, if they're changed to 'w' and 'h' then the FODIO constructor would
be new FODIO('width', 'height');

A demonstration can be seen in the Gallery page in the example web site.

-Feature Detection-

Some device information is not available through just the web request alone,
so 51Degrees Feature Detection runs script on some devices to better identify
information from the device. To enable this feature, simply reference
51Degrees.core.js.php and call new FODFO();
<script src="51D/core.js"></script>
<script>
  new FODPO();
</script>

-Client Side Properties-
An optional include script can be added to the web page to provide information
to client side javascript concerning the device enabling decisions to be taken
in javascript. The javascript returned will create an object called FODF
(51Degrees Features) which can be used to request properties. For example the
following JavaScript would return the IsMobile value.
if (FODF.IsMobile) {
  // Do something for mobiles only.
}
The value returned will be of a type associated with the property. See the
Property Dictionary for details of property types.

The necessary javascript includes need to be added into the page by the
developer. The following needs to be added to the HTML page header before
accessing the FODF variable.

<script src="51D/features.js" type="text/javascript"></script>


-Example web.xml-

<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:web="http://java.sun.com/xml/ns/javaee" 
  xmlns="http://java.sun.com/xml/ns/javaee" 
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
  id="WebApp_ID" version="3.0">
  <display-name>51Degrees</display-name>
  <context-param>
    <param-name>MEMORY_MODE</param-name>
    <param-value>false</param-value>
  </context-param>
  <listener>
    <listener-class>fiftyone.mobile.detection.webapp.FiftyOneDegreesListener</listener-class>
  </listener>
  <servlet>
    <description>51Degrees Servlet</description>
    <display-name>FiftyOneDegreesServlet</display-name>
    <servlet-name>FiftyOneDegreesServlet</servlet-name>
    <servlet-class>fiftyone.mobile.detection.webapp.FiftyOneDegreesServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>FiftyOneDegreesServlet</servlet-name>
    <url-pattern>/51D/*</url-pattern>
  </servlet-mapping>
  <listener>
    <listener-class>
      fiftyone.mobile.detection.webapp.Listener
    </listener-class>
  </listener>
</web-app>
--------------------------------------------------------------------------------
