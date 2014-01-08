
Description:
		
    51Degrees Mobile Detection Software for Java 
    HomePage: <http://51Degrees.mobi?CATReferrer=1758>
    
For automatic updates, tablet, operating system and screen dimension detection
see 51Degrees.mobi Premium Data at
http://51degrees.mobi/Products/DeviceDetection.aspx

For additional features including:

    -Automatic Weekly Updates
    -Device Type(such as Tablet)
    -Operating System
    -Screen Dimension
    -Input Method
    
    See http://51degrees.mobi/Products/DeviceDetection.aspx for more information
    on our Premium Data.

Contents:
    -SRC: Contains the java files used to make the 51Degrees.mobi JAR files.
        -core: Contains the java files needed to make
               51Degrees.mobi.detection.core.jar
	-webapp: Contains the java files neeeded to make
                 51Degrees.mobi.detection.webapp.jar
    -JAVADOC: Contains the javadocs for the JAR files. Run by using "index.html"
        -core: Contains the javadoc for 51Degrees.mobi.detection.core.jar
        -webapp: Contains the javadoc for 51Degrees.mobi.detection.webapp.jar
    -EXAMPLE: Contains examples of how to use the JAR files.
    -DIST: Contains 51Degrees.mobi.detection.core.jar and 
        51Degrees.mobi.detection.webapp.jar. Add the core file to any Java
        application requiring detection and add the both to any Java web
        application (e.g. Servlet, jsp, Facelet) 
    -Build.xml: An Apache Ant build script used to build the src. For more
        information see <http://ant.apache.org/>.
        To build the core files use "ant core". To build the webapp files a
        copy of servlet-api.jar is needed. Place the file in the same
        directory as the build script and use "ant" or "ant webapp" to build
        both JAR files.

For more information and documentation about this project please visit 
<http://51degrees.mobi/Support/Documentation/Java.aspx?CATReferrer=1758>.

This project uses SLF4J, using the MIT licence.
http://www.slf4j.org/license.html

--------------------------------------------------------------------------------

Changes:

Version 2.2.9.1

Core Changes:

1. Fixed casting exception caused by a hash collision when processing the
   data file. Java installations prior to version 2.2.9.1 attempting to update
   to newest data may fail and should be updated to the latest version. Such
   installations will continue to use their current data without issue.

Version 2.2.8.9

Core Changes:

1. Fixed a rare crash affecting some useragents when detected by the trie
   method.  Users of the regular pattern matching data will not be affected
   by this change.

Version 2.2.8.8

Core Changes:

1. Fixed a crash when processing a Trie data file larger than around 2.5gB.
   Users of the regular pattern matching data will not be affected by this
   change.
   
Version 2.2.8.7

Core Changes:

1. Improved stability of the Trie data reader. The Reader.Create method now
   throws IO and FileNotFound exceptions rather than silently logging a fault.

Version 2.2.8.6

Core Changes:

1. Further updates to Matcher that was causing null detections with multiple
   threads.

Version 2.2.8.5

Core Changes:

1.  Updated Matcher so multi-core detection works properly when the thread
    pool is used for the first time.

Version 2.2.8.4

Core Changes:

1.  The update method now has a String parameter for the licence key. If a
    licence is not supplied in the initialize method one can be supplied here to
    control all data updates manually.
    
2.  Factory initialize methods have been overloaded to include a ThreadPool
    parameter. If a Thread Pool is supplied detection will run using multiple
    threads, providing a faster detection time. If a Thread Pool is not given
    detection will default to single thread mode.
    
3.  The constant FORCE_SINGLE_PROCESSOR has been removed. Multiple processes are
    now controlled by the Thread Pool supplied to the Factory.
    
4.  Fixed getNewProvider to prevent it from returning an uninitialised Provider.

Version 2.2.7.2

Core Changes:

1.  Addressed memory consumption issues associated with retaining references to
    all previously created providers from the factory.

2.  Removed the embeddedProvider property from the Provider class as the 
    dependent methods are no longer used.

Version 2.2.6.3

Core Changes:

1.  Matchers in multi threaded environments will now create a single service
    request wrapper for use across all threads. InvokeAll is no longer used to
    start the tasks, instead the task is started as soon as it's created.

2.  Factory will track all providers created and ensure they're all destroyed
    when the factory is destroyed. A single thread pool is shared across all
    providers and only shutdown when the last provider is destroyed. The
    finalize method of the provider will also destory the provider and ensure
    the thread pool is shutdown when the developer does not destory the provider
    explicitly and garbage collection is used to dispose of the provider.

3.  When a provider is updated the previous provider is not destroyed, just
    unreferenced. The garbage collector will then destroy the provider when
    ready. This ensures any reference to the provider stored in 3rd party code
    remains operational.

Version 2.2.5.2

Core Changes:

1.  Changed edit distance and segment matcher to trap for RejectedExecution
    exceptions in multi threaded operation and fall back to the current thread.
	
2.  Addressed a possible problem in multi threaded operation where the providers
    destroy method shuts down the thread pool whilst a detection is active.

Version 2.2.4.2

Core Changes:

1.	Added a new public method to Factory class, boolean Update(), that will 
    immediately check for, download and apply new device data with the licence 
    key provided in the initalise methods of the Factory instance.

2.	Added OutOfMemory exception handling around AutoUpdate and added a free 
    memory check before an update. The JVM must have at least 100mb of free 
    memory before updating. This can be changed using 
    'AUTO_UPDATE_REQUIRED_FREE_MEMORY' from the Constants class.

Version 2.2.3.5

Core Changes:

1.	Core package now uses SLF4J to log and bundles with SLF4J simple logging, 
    which logs to std err by default.

2.	Package now makes does more logging, particularly with automatic updates.

3.	Provider.create(String filePath) now throws an exception instead of silently
    using embedded data.

4.	Added a new static string 'Version' to the Constants class that indicates 
    what version of the API is being used.

5.	Fixed a bug where premium data would not be updated if the data did not 
    already exist.

Version 2.2.2.3:

Core Changes:

1.  Core package now checks the file system periodically for new data that has
    been updated by other server instances or processes. The core creates a 
    seperate thread that checks the data file's last modified data and compares 
    that the modified date of the data in memory. There is a new class, 
    FileAutoChecker, and ther are two new contants in the Contants class to 
    manage this:

        static long FILE_CHECK_DELAYED_START - Controls how long the task 
        will wait in milliseconds before starting. Default set to 1200000 
        (20 minutes).

        static long FILE_CHECK_SLEEP - Controls how long the task will sleep
        in between checks. Default set to 1200000 (20 minutes).

2.  Core package now contains 'PropertyContants' class. Ths contains string 
    constant of all Premium and Lite Propertis and their desciptions.

3.  Initial loading time has been improved.

Version 2.2.1.6:

Core Changes:

1.  Core package now includes base implementations of the Factory to create
    providers, and functionality to automatically update the locally store
    device data file when a Premium licence key is available. 

2.  Provider has now been consolidated with BaseProvider and includes a new
    method called getResult which will return a Result object for the request.
    The Request class encapsulates the matching logic required to handle
    secondary user agents like Stock-Device-UA. The Result class includes
    identical properties to the BaseDeviceInfo class for retrieving properties
    from the result.

3.  The Premium package and source code has been removed as the functionality
    is now included in the Core or WebApp packages.

4.  A number of minor changes to reduce duplication in the code and improve
    readability.

WebApp Changes (for HttpServlet deployments)

5.  A new Listener class has been created which handles initialising and
    destroying Providers as well as the Auto Update components when run within
    a web application. The new ServiceContextListener class must be included in
    the web.xml configuration file using the following lines.

    <listener>
        <listener-class>fiftyone.mobile.detection.webapp.Listener</listener-class>
    </listener>

    The listener inherits from the Core’s Factory and manages activities
    associated with starting and stopping the application. A reference to the
    Listener is placed into the ServiceContext attributes collection using the
    key "51D_LISTENER" and is used by the FiftyOneServlet class.

6.  A Servlet base class called FiftyOneServlet has been added which can only be
    used when the listener is present. This Servlet class includes methods for
    accessing properties. For example where the HttpServletRequest is available
    in the variable called request, the following will return the screen width
    in pixels of the requesting device.

    String width = getProperty(request, “ScreenPixelsWidth”);

    Using the FiftyOneServlet as a base class will reduce implementation
    complexity.
	
    This base can also be used to show the confidence of the match:
        Result result = super.getProvider().getResult(request);
        int confidence = result.getConfidence();
	
    A value between 1 and 255 where the lower the value the less confident we 
    are in the device properties provided. Exact matches will return 255, 
    fuzzy matches will return between 1 and 10. Values above 5 should be 
    considered reliable for most purposes. A value of 1 should be considered 
    unreliable. Lower values usually result from near random input where the 
    structure of headers provided is unlike anything we’ve seen in the real world.

--------------------------------------------------------------------------------