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
package fiftyone.mobile.detection;


/**
 * A class that holds constant values of the 51Degrees property names.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class PropertyConstants {
    
			 /**
			  * Indicates what ajax request format should be used. May also return &#39;Unsupported&#39; or &#39;Unknown&#39;.
			  */
			 public static final String AjaxRequestType = "AjaxRequestType";
			 /**
			  * The number of data bits per pixel. May also return the string &#39;Unknown&#39;.
			  */
			 public static final String BitsPerPixel = "BitsPerPixel";
			 /**
			  * The name of the browser. Many mobile browsers come default with the OS. Unless specifically named, these browsers are named after the accompanying OS.
			  */
			 public static final String BrowserName = "BrowserName";
			 /**
			  * The company who created the browser.
			  */
			 public static final String BrowserVendor = "BrowserVendor";
			 /**
			  * The version number of the browser.
			  */
			 public static final String BrowserVersion = "BrowserVersion";
			 /**
			  * Indicates if the browser supports http cookies. May return &#39;Unknown&#39;.
			  */
			 public static final String CookiesCapable = "CookiesCapable";
			 /**
			  * Beta. Grouped devices that only differ by region/radio
			  */
			 public static final String HardwareFamily = "HardwareFamily";
			 /**
			  * The model name or number used primarily by the hardware vendor to identify the device. This is not always the name the device is most often known as. You should also use HardwareName for a list of popular device names.
			  */
			 public static final String HardwareModel = "HardwareModel";
			 /**
			  * The company who manufactured the hardware. Some devices (such as PCs) don&#39;t reveal this information and will return &#39;Unknown&#39;.
			  */
			 public static final String HardwareVendor = "HardwareVendor";
			 /**
			  * Indicates if the device has a camera capable of taking 3D images.
			  */
			 public static final String Has3DCamera = "Has3DCamera";
			 /**
			  * Indicates if the device has a screen capable of displaying 3D images.
			  */
			 public static final String Has3DScreen = "Has3DScreen";
			 /**
			  * Indicates if the device has a camera.
			  */
			 public static final String HasCamera = "HasCamera";
			 /**
			  * Indicates if the device has a physical numeric keypad.
			  */
			 public static final String HasKeypad = "HasKeypad";
			 /**
			  * Indicates whether the devices supports NFC
			  */
			 public static final String HasNFC = "HasNFC";
			 /**
			  * Indicates if the device has a physical qwerty keyboard.
			  */
			 public static final String HasQwertyPad = "HasQwertyPad";
			 /**
			  * Indicates if the device has a touchscreen.
			  */
			 public static final String HasTouchScreen = "HasTouchScreen";
			 /**
			  * Indicates if the device has a track pad or ball. eg - Nexus One, Blackberry Curve
			  */
			 public static final String HasTrackPad = "HasTrackPad";
			 /**
			  * Indicates if the device has a virtual on-screen qwerty pad.
			  */
			 public static final String HasVirtualQwerty = "HasVirtualQwerty";
			 /**
			  * Latest version of HyperText Markup Language (HTML) supported by the browser. May return &#39;Unknown&#39;.
			  */
			 public static final String HtmlVersion = "HtmlVersion";
			 /**
			  * Indicates if the device is neither a phone or a computer/tablet (ie a games console or television).

			  */
			 public static final String IsConsole = "IsConsole";
			 /**
			  * Indicates if the device is a crawler.
			  */
			 public static final String IsCrawler = "IsCrawler";
			 /**
			  * Indicates if the device is an e-reader.
			  */
			 public static final String IsEReader = "IsEReader";
			 /**
			  * The device&#39;s primary data connection is wireless and is designed to operate mostly from battery power (ie a mobile phone, smart phone or tablet).
			  */
			 public static final String IsMobile = "IsMobile";
			 /**
			  * Indicates if the device is mobile with a screen size less tha 2.5 inches.
			  */
			 public static final String IsSmallScreen = "IsSmallScreen";
			 /**
			  * Indicates if the device is a mobile (not a tablet) with a screen size greater than 2.5&quot; and running a modern OS (Android, iOS, Windows Phone etc)
			  */
			 public static final String IsSmartPhone = "IsSmartPhone";
			 /**
			  * The manufacturer of the device sells the device primarily as a tablet.
			  */
			 public static final String IsTablet = "IsTablet";
			 /**
			  * Indicates if the browser supports Javascript. May return &#39;Unknown&#39;.
			  */
			 public static final String Javascript = "Javascript";
			 /**
			  * Indicates if javascript can manipulate a page&#39;s CSS. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptCanManipulateCSS = "JavascriptCanManipulateCSS";
			 /**
			  * Indicates if Javascript can manipulate a page&#39;s Document Object Model. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptCanManipulateDOM = "JavascriptCanManipulateDOM";
			 /**
			  * Indicates if Javascript is able to access html elements from their Id&#39;s. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptGetElementById = "JavascriptGetElementById";
			 /**
			  * Indicates whether browser allows the registration of event listeners on event targets. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptSupportsEventListener = "JavascriptSupportsEventListener";
			 /**
			  * The Javascript events onload, onclick, onsubmit and onselect are all supported. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptSupportsEvents = "JavascriptSupportsEvents";
			 /**
			  * Indicates if javascript is able to insert html into a DIV tag. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptSupportsInnerHtml = "JavascriptSupportsInnerHtml";
			 /**
			  * The Javascript version the browser uses. The number refers to Javascript versioning, not ECMAscript or Jscript. May return &#39;Unknown&#39;.
			  */
			 public static final String JavascriptVersion = "JavascriptVersion";
			 /**
			  * The underlying technology behind the web browser.
			  */
			 public static final String LayoutEngine = "LayoutEngine";
			 /**
			  * The name of the software platform (operating system) the device is using. 
			  */
			 public static final String PlatformName = "PlatformName";
			 /**
			  * The company who created the OS.
			  */
			 public static final String PlatformVendor = "PlatformVendor";
			 /**
			  * The version of the software platform.
			  */
			 public static final String PlatformVersion = "PlatformVersion";
			 /**
			  * The month in which the device was released.
			  */
			 public static final String ReleaseMonth = "ReleaseMonth";
			 /**
			  * The year in which the device was released.
			  */
			 public static final String ReleaseYear = "ReleaseYear";
			 /**
			  * The diagonal size of the screen in inches.
			  */
			 public static final String ScreenInchesDiagonal = "ScreenInchesDiagonal";
			 /**
			  * The height of the screen in pixels.
			  */
			 public static final String ScreenPixelsHeight = "ScreenPixelsHeight";
			 /**
			  * The width of the screen in pixels.
			  */
			 public static final String ScreenPixelsWidth = "ScreenPixelsWidth";
			 /**
			  * List of the bearers supported by the device.
			  */
			 public static final String SupportedBearers = "SupportedBearers";
			 /**
			  * Indicates if the browser supports TLS or SSL, essential for secure protocols such as HTTPS.
			  */
			 public static final String SupportsTlsSsl = "SupportsTls/Ssl";

			 /**
			  * Adapters used with 51Degrees.mobi Framework
			  */
			 public static final String Adapters = "Adapters";
			 /**
			  * Indicates that the browser supports &#39;window.requestAnimationFrame()&#39;.
			  */
			 public static final String AnimationTiming = "AnimationTiming";
			 /**
			  * Indicates that the browser fully supports BlobBuilder, containing a BlobBuilder interface, a FileSaver interface, a FileWriter interface, and a FileWriterSync interface.
			  */
			 public static final String BlobBuilder = "BlobBuilder";
			 /**
			  * Stands for Composite Capability/Preference Profiles. Lists MIME types known to be supported. 3rd party applications may enable other MIME types to be supported which are not listed.
			  */
			 public static final String CcppAccept = "CcppAccept";
			 /**
			  * Indicates which version of the Connected Limited Device Configuration supports for use with Java ME.
			  */
			 public static final String CLDC = "CLDC";
			 /**
			  * Indicates if the browser supports CSS3 backgrounds. This allows styling of the border, the background of the box. The box may be given a shadow effect.
			  */
			 public static final String CssBackground = "CssBackground";
			 /**
			  * Indicates if the browser supports Border Images, allowing decoration of the border around a box.
			  */
			 public static final String CssBorderImage = "CssBorderImage";
			 /**
			  * Indicates if the browser can draw CSS images into a Canvas.
			  */
			 public static final String CssCanvas = "CssCanvas";
			 /**
			  * Indicates if the browser supports CSS 3 Color, allowing author control of the foreground colour anf opacity of an element.
			  */
			 public static final String CssColor = "CssColor";
			 /**
			  * Indicates if the browser supports CSS3 columns.
			  */
			 public static final String CssColumn = "CssColumn";
			 /**
			  * Indicates if the browser supports flexbox, allowing automatic reordering of elements. Useful for GUI design.
			  */
			 public static final String CssFlexbox = "CssFlexbox";
			 /**
			  * Indicates if CSS3 fonts are supported, including non standard fonts (@font-face).
			  */
			 public static final String CssFont = "CssFont";
			 /**
			  * Indicates if CSS3 images are supported, allowing for fallback images, gradients and other effects.
			  */
			 public static final String CssImages = "CssImages";
			 /**
			  * Indicates if the device supports MediaQueries for dynamic CSS.
			  */
			 public static final String CssMediaQueries = "CssMediaQueries";
			 /**
			  * Indicates if the browser supports the CSS &#39;min-width&#39;  and &#39;max-width&#39; properties.
			  */
			 public static final String CssMinMax = "CssMinMax";
			 /**
			  * Indicates if the browser supports overflowing of clipped blocks (blocks have scroll mechanisms).
			  */
			 public static final String CssOverflow = "CssOverflow";
			 /**
			  * Indicates if the browser supports CSS position, allowing for different box placement algorithms (static, relative, absolute, fixed).
			  */
			 public static final String CssPosition = "CssPosition";
			 /**
			  * Indicates if the browser supports CSS 3 text, allowing better support for non-Latin alphabets and grammar.
			  */
			 public static final String CssText = "CssText";
			 /**
			  * Indicates if the browser supports 2D transform in CSS 3.
			  */
			 public static final String CssTransforms = "CssTransforms";
			 /**
			  * Indicates if the browser supports CSS3 transitions.
			  */
			 public static final String CssTransitions = "CssTransitions";
			 /**
			  * Indicates if the browser supports various CSS UI stylings.
			  */
			 public static final String CssUI = "CssUI";
			 /**
			  * Indicates if the browser allows custom data attributes for th a site&#39;s own use. An attribute with the form &#39;data-*&#39; will not be interpreted by the browser engine.
			  */
			 public static final String DataSet = "DataSet";
			 /**
			  * Indicates if the browser allows encoded data to be contained in a url.
			  */
			 public static final String DataUrl = "DataUrl";
			 /**
			  * Indicates if the browser supports DOM events for device orientation (deviceorientation, devicemotion, compassneedscalibration).
			  */
			 public static final String DeviceOrientation = "DeviceOrientation";
			 /**
			  * Indicates if the browser supports file reading with events to show progress and errors.
			  */
			 public static final String FileReader = "FileReader";
			 /**
			  * Indicates if the browser supports Blobs to be saved to client machines with events to show progress and errors. Note that this property only shows browser support, not if the user allows it.
			  */
			 public static final String FileSaver = "FileSaver";
			 /**
			  * Indicates if the browser supprts files to be saved to client machines with events to show progress and errors. Note that this property only shows browser support, not if the user allows it.
			  */
			 public static final String FileWriter = "FileWriter";
			 /**
			  * Indicates if the browser supports the FormData object.
			  */
			 public static final String FormData = "FormData";
			 /**
			  * Indicates whether the browser supports fullscreen requests.
			  */
			 public static final String Fullscreen = "Fullscreen";
			 /**
			  * Indicates if the browser supports location finding. Note that the browser may be capable but the user may not want to give their position. Similar to the 51Degrees.mobi property &#39;W3C api&#39; value in the &#39;JavascriptPreferredGeoLocApi&#39; property.
			  */
			 public static final String GeoLocation = "GeoLocation";
			 /**
			  * A list of images associated with the device. The string contains the caption, followed by the full image url seperated with a tab character.
			  */
			 public static final String HardwareImages = "HardwareImages";
			 /**
			  * A list of marketing names associated with the device, seperated by a &#39;|&#39;. A device may be known by many names depending on region or network. Likewise, a device may not have a marketing name at all, leaving this empty. This property should be used in tandem with HardwareModel.
			  */
			 public static final String HardwareName = "HardwareName";
			 /**
			  * Indicates if the device has a click wheel.
			  */
			 public static final String HasClickWheel = "HasClickWheel";
			 /**
			  * Indicates if the browser stores the session history for a webpage to use.
			  */
			 public static final String History = "History";
			 /**
			  * Indicates if the browser can use media inputs (webcam, microphones etc) in script and as an input for forms (eg. &amp;lt;input type=&quot;file&quot; accept=&quot;image/*&quot; id=&quot;capture&quot;&amp;gt; would prompt image capturing software to open).
			  */
			 public static final String HtmlMediaCapture = "Html-Media-Capture";
			 /**
			  * Indicates if the browser supports the Iframe element.
			  */
			 public static final String Iframe = "Iframe";
			 /**
			  * Indicates if the browser supports an indexed local database.
			  */
			 public static final String IndexedDB = "IndexedDB";
			 /**
			  * Indicates what GeoLoc api Javascript supports.

			  */
			 public static final String JavascriptPreferredGeoLocApi = "JavascriptPreferredGeoLocApi";
			 /**
			  * The level of support the device has with the jQuery Mobile Framework, as posted by jQuery.
			  */
			 public static final String jQueryMobileSupport = "jQueryMobileSupport";
			 /**
			  * Indicates if the browser supports the JSON object.
			  */
			 public static final String Json = "Json";
			 /**
			  * Indicates if filter can be applied to images to change their shape or add visual effects (eg, grayscale, fading).		
			  */
			 public static final String Masking = "Masking";
			 /**
			  * Indicates what version of Mobile Information Device Profile the device supposts, used with Java ME and and CLDC.
			  */
			 public static final String MIDP = "MIDP";
			 /**
			  * The number of unique client IPs this device has been seen from.
			  */
			 public static final String Popularity = "Popularity";
			 /**
			  * Indicates if the browser supports messages between different documents.
			  */
			 public static final String PostMessage = "PostMessage";
			 /**
			  * Indicates if the browser supports progress reports, such as with HTTP requests.
			  */
			 public static final String Progress = "Progress";
			 /**
			  * Beta property. Indicates if the browser supports simple dialogs (windows.alert, window.confirm and window.prompt).
			  */
			 public static final String Prompts = "Prompts";
			 /**
			  * The height of the screen in inches.
			  */
			 public static final String ScreenInchesHeight = "ScreenInchesHeight";
			 /**
			  * The width of the screen in inches.
			  */
			 public static final String ScreenInchesWidth = "ScreenInchesWidth";
			 /**
			  * The diagonal size of physical screen in millimeters.
			  */
			 public static final String ScreenMMDiagonal = "ScreenMMDiagonal";
			 /**
			  * The screen height in millimeters.
			  */
			 public static final String ScreenMMHeight = "ScreenMMHeight";
			 /**
			  * The screen width in millimeters.
			  */
			 public static final String ScreenMMWidth = "ScreenMMWidth";
			 /**
			  * Indicates if the browser supports Selectors, used for more consice searching in DOM.
			  */
			 public static final String Selector = "Selector";
			 /**
			  * A list of MIME types the device can accept to stream. 3rd party applications may enable other MIME types to be supported which are not listed.
			  */
			 public static final String StreamingAccept = "StreamingAccept";
			 /**
			  * The suggested optimium height of a button in mms. Ensures the button is touchable on a touch screen, and not too large on a non-touch screen. (BETA)
			  */
			 public static final String SuggestedImageButtonHeightMms = "SuggestedImageButtonHeightMms";
			 /**
			  * The suggested optimium height of a button in pixels. Ensures the button is touchable on a touch screen, and not too large on a non-touch screen. Assumes the actual device DPI is being used. (BETA)
			  */
			 public static final String SuggestedImageButtonHeightPixels = "SuggestedImageButtonHeightPixels";
			 /**
			  * The suggested optimium height of a hyperlink in pixels. Ensures the link is touchable on a touch screen, and not too large on a non-touch screen. Assumes the actual device DPI is being used. (BETA)
			  */
			 public static final String SuggestedLinkSizePixels = "SuggestedLinkSizePixels";
			 /**
			  * The suggested optimium height of a hyperlink in points. Ensures the link is touchable on a touch screen, and not too large on a non-touch screen. (BETA)
			  */
			 public static final String SuggestedLinkSizePoints = "SuggestedLinkSizePoints";
			 /**
			  * Indicates if the browser supports SVG (scalable vector graphics), useful for 2D animations and applications as all objects within the SVG can be accessed via the DOM and can be assigned event listeners.
			  */
			 public static final String Svg = "Svg";
			 /**
			  * Indicates if the browser supports multiple touch events happening simultaneously.
			  */
			 public static final String TouchEvents = "TouchEvents";
			 /**
			  * Indicates if the browser supports text tracks being played with media, eg subtitles, captions.
			  */
			 public static final String Track = "Track";
			 /**
			  * Indicates if the browser supports the video element.
			  */
			 public static final String Video = "Video";
			 /**
			  * Indicates if the browser supports Viewport, to give control over view size and resolution.
			  */
			 public static final String Viewport = "Viewport";
}
