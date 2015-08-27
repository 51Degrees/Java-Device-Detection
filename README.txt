------------------------------- 1 INTRODUCTION --------------------------------

The 51Degrees Java API is designed to be easy and quick to deploy and use even 
for low skill developers while providing accurate and reliable results in just 
a fraction of a second. The API features perks like automatic image optimiser, 
sending detection results to the client side to integrate with existing 
JavaScript logic, data file meta data and more.
HomePage: <https://51degrees.com>
Documentation: <https://51degrees.com/support/documentation/java>
Support: <https://51degrees.com/support/forum>
Repository: <https://github.com/51Degrees/Java-Device-Detection>

For automatic updates, tablet, operating system and physical screen size
see 51Degrees Premium Data at <https://51degrees.com/products/device-detection>

Benefits of pay-for data subscription include:

    -Automatic Weekly Updates
    -Bandwidth Monitoring
    -Device Type(such as Tablet)
    -Operating System
    -Screen Dimension
    -Input Method

    See <https://51degrees.com/compare-data-options> to compare the various 
	device data options.

------------------------------- 2 QUICK START ---------------------------------

2.1. Obtaining a copy of the API:
- Sourceforge: <http://sourceforge.net/projects/fiftyone-java/>
- Maven: <http://mvnrepository.com/search?q=51degrees>
- Source: <https://github.com/51Degrees/Java-Device-Detection>

2.2. Obtaining a 51Degrees data file:
- Codeplex: <http://51degrees.codeplex.com/releases/view/616372>
- Github: <https://github.com/51Degrees/51Degrees.mobi-Lite-Binary>
- Premium and Enterprise data files: <https://51degrees.com/compare-data-options>

2.3. Using the Core API:

51Degrees provides two detection algorithms: Pattern and Trie. In short: Pattern 
is more memory efficient with smaller data files while Trie is faster but 
requires more memory and the data files are considerably larger. For more info 
on the algorithms please see: 
<https://51degrees.com/support/documentation/how-device-detection-works>

2.3.1. Pattern.

	Pattern relies on the Provider object that interacts with device data through 
	the dataset. The Dataset in turn can be created using either StreamFactory:
	
	Provider p = new Provider(StreamFactory.create("path/to/data/file.dat", false));
	
	or using MemoryFactory:
	
	Provider p = new Provider(MemoryFactory.create("path/to/data/file.dat"));

	StreamFactory creates a dataset that interacts directly with the data file, 
	so all detection results are retrieved from the data file on each request.
	MemoryFactory fully loads the data file in to memory so that the data file 
	is not used for detection at all.
	
	Detection takes place in the match method of the Provider object:
	
	Match match = p.match("User agent string goes here.");
	
	You can then access detection results via the match object as follows:
	
	match.getValues("IsMobile");
	
	By default all results are returned as Strings.
	
2.3.2. Trie.
	
	Trie relies on the TrieProvider to interact with the Trie data file to 
	retrieve detection results. Trie provider can be created as follows:
	
	TrieProvider trp = TrieFactory.create("path\\to\\file.trie");
	
	Note that the Pattern data file (.dat) will not work with the Trie (.trie) 
	Provider and vice versa.
	
	To obtain the device information you need to first find the index of the 
	device as follows:
	
	int index = trp.getDeviceIndex("User agent");
	
	and then retrieve the property values for the found index:
	
	String isMobile = provider.getPropertyValue(index, "IsMobile");

2.4. Using the Servlet API.

	Will be added soon.
	
--------------------------- 3 REPOSITORY CONTENTS -----------------------------

	
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

This project uses SLF4J, using the MIT licence.
http://www.slf4j.org/license.html

----------------------------- 4 BREAKING CHANGES ------------------------------

As of version 3.2 the following changes have been implemented that will break 
any of the existing implementations:

3.1 The provider object: you can no longer instantiate a provider object 
without providing the dataset object. So the 
	Provider p = new Provider(); 
will no longer work. Instead you will need to do one of the following:

Provider p = new Provider(StreamFactory.create("path/to/data/file.dat", false));
or:
Provider p = new Provider(MemoryFactory.create("path/to/data/file.dat"));

Please note that the StreamFactory now takes in at least 2 parameters:
 - Path to data file.
 - boolean to indicate whether the data file provided is temporary. This will 
   matter when the dispose method is invoked. If the flag is set to true, the 
   API will attempt to delete the data file upon dispose.
 
--------------------------------  CHANGELOG -----------------------------------

Version 3.2.1.9-beta
- Automatic update function no longer uses the memory to store data downloaded 
  from 51degrees.com update server. Instead a temporary file is used. 
  This should significantly reduce the memory impact of the auto update process.
- The API now supports 51Degrees data files of version 3.2 as well as 3.1 data 
  files. The data files of version 3.2 are on average 20% smaller than the 3.1 
  data files due to the changes to the internal data structure.
- The core API (both Pattern and Trie) has been updated to perform device 
  detection with multiple HTTP headers.
- The API has been updated to implement caching for all major components that 
  require lookup/detection. This change reflects the fact that in the real-world 
  applications/websites subsequent requests are probable and that some user 
  agents that are encountered more often than others. This change should improve 
  detection times even further.
- Breaking change: The embedded data file no longer exists. This makes the JAR 
  very light but you will need to obtain the data file separately.
- Breaking change: Provider object can no longer be instantiated without 
  specifying the dataset to be used. See the breaking changes section.

-------------------------------------------------------------------------------