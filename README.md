Java ReStart
=====
"The Network is The Computer! Is not it?"

Java ReStart is a technology that allows to run Java applications instantly from Internet without installation, 
downloading necessary parts of an application dynamically at runtime and executing the application in parallel with downloading (analogy with YouTube and Web). 
Thus the application starts as fast as web applications and gains other advantages of the Web such as invisible autoupdates.
On the other hand, unlike to the Web, application will have native desktop/tablet look-n-feel and performance,
and a developer also has a rich choice of languages and technologies that are available on top of Java platform from JavaScript to Scala 
without compromising the performance. Also the technology is free from other limits and restrictions imposed by today's web technologies based on HTML/JavaScript.
Unlike Java Web Start (or Flash/Silverlight) an application comes to end-user not as a whole but partially, where only required parts are loaded, 
thus theoretically an application of any complexity can be deployed by this technology from rich IDEs to graphical designer/engineering tools.

How it works
=====
It is a client/server technology.
The server provides classes/resources of an application on a request and the client downloads classes/resources of an application on demand (lazily) 
and executes the application in parallel with downloading.
The server has a very simple REST interface now:

```
GET /{application} -- returns an application descriptor in JSON format
                      (main class name of the application (entry point), splash, etc.)
GET /{application}/{resource} -- returns a class/resource of a referenced application
GET /{application}?bundle=initial --- returns chunked collection of resources that were accessed last time
                                      (in the order it was accessed last time)
GET /{application}/{resource}?getAppDescriptor -- returns application descriptor (the same as GET/ {application})

```

The client in turn has a very simple command line interface:

```
java javarestart.JavaRestartLauncher <URL>
```

where URL has a form [BaseURL]/[AppName].

First, the client fetches an application descriptor, if a splash is scpecified in the descriptor the client downloads it
and immediatly shows,
then it downloads initial bundle and in parallel loads and executes main class of the application
using a classloader that tries to emulate default JVM application classloader
but instead of loading the classes from HDD it fetches them from initial bundle or URLs using REST interface above.
This way, only required classes/resources are downloaded by the client and application starts right from the first
downloaded main class.

How to run 
=====
The sources come with a demo sustaining the concept.

First you need to run the server in an application server of your choice (Tomcat, Jetty, whatever).
Before the run you should setup demo applications: 
correct server/src/main/resources/application.properties "apps.path" property pointing to apps directory.
apps contains the following Java UI applications with the descriptions (app.properties): 
  * Java2Demo - standard AWT/Java2D demo, 
  * SwingSet2 - standard Swing demo, 
  * SWT - demo showing SWT standard controls,
  * Jenesis - Sega Genesis emulator written using Java OpenGL (jogl),
  * BrickBreaker - JavaFX arcanoid game demo
  * Ensemble - standard JavaFX ensemble demo
  * Game2048 - JavaFX version of 2048 game written by Bruno Borges (https://github.com/brunoborges/fx2048)

After launching the server, you may run the apps using
```
java javarestart.JavaRestartLauncher http://localhost:8080/apps/<AppName> 
```
command (URL example -- http://localhost:8080/apps/Java2Demo).

Or you may run JavaFX demo that in turn will run the demos above by itself (located in "demo" folder):
```
java javarestart.demo.JavaRestartDemo
```

You can also run the samples from forked version of Bruno Borges WebFX browser 
(https://github.com/pjBooms/webfx): 
point the browser to http://localhost:8080 and click "Java Restart Demo" link.

Run Notes:
SWT, Jenesis can run only with 32-bit JRE on Windows (they are using 32-bit native libraries).

New protocols
=====
The Java ReStart client implements two new internet protocols:

`java://` and `wfx://`

both they are based on JavaRestartURLConnection that tries to use collected server
application usage profile thus prefetching required classes/resources with a single HTTP request.
Using these protocols the startup time is drastically improved in compare with usual http://
when you launch your application from really remote server. From my experiments the startup time
of all samples becomes comparable lo local startup time! It is important exploration, IMHO.
It means that all that stuff that you have on your desktop computers is not actually needed:
it can run from remote source as fast as locally!

Why are there two protocols?
  * `wfx://` protocol is used for loading the content into FXML page of a new tab of the WebFX browser.
  * `java://` protocol is used for launching remote applications using Java ReStart.

How to use the protocols: add `-Djava.protocol.handler.pkgs=javarestart.protocols` JVM property to
launching command to let JVM know the new protocols.

Adding your own applications
=====
To add your own application that you would like to launch from Internet you need to put it to a subfolder of apps folder 
and provide app.properties where you describe main class and classpath of your application (see other applications located in apps for example). 
After that you may launch it with the client:

```
java javarestart.JavaRestartLauncher <BaseURL>/<AppName>
```

Java ReStart and WebFX
=====
Java ReStart is integrated now with WebFX (https://github.com/pjBooms/webfx).

It means that:

1. You may launch Java ReStart applications from WebFX browser via java://`<BaseURL>` protocol.
   You may try this via cope&paste to WebFX browser the following URL: java://localhost:8080/apps/Java2Demo

2. Additionally Java ReStart app descriptor is extended to provide a main FXML of a WebFX application (instead of a main class). 
   This way, when you reference your application as wfx://`<BaseURL>` from WebFX browser, the main FXML page is loaded in a new tab of the WebFX browser
   (not as separate window as with java:// protocol). 
   And you may reference Java ReStart classes (classes that are located on Java ReStart server) from your FXML! 

   Check out this URL in the WebFX browser: 
   
   wfx://localhost:8080/apps/Game2048

   The demo is written in pure Java and behind the scence Java bytecode of the game is loaded that is referenced from the very simple main FXML page.
   This Java ReStart <-> WebFX integration allows you to write applications the same way as web applications but use FXML instead of HTML 
   and any programming language that is available on top of Java platform from Java, Scala to JavaScript, JRuby, etc., and without compromising performance or 
   to-JS-translated-that-I-do-not-know-how-to-work-and-why-it-does-not-work-when-it-does-not-work issues.
   
TODO
=====
1. Implement caching of downloaded classes by the client 
2. Support versioning of the apps on the server (thus if a version is not changed on the server, the cached version can be taken safely)
3. Implement profiling class/resource retrieving sequence on the server and change the server REST interface to allow to upload frequently used classes/resources 
   by a single HTTP response with a aggressively packed stream with the right class order. This way we can drastically optimize the startup of applications 
   (no need to handle thousands of HTTP requests).
4. Improve application descriptions. Now only classpath and main is specified for an application but it is good also to provide VM properties, splash, etc.
5. Support custom classloading. If an application is loaded not only by application classloader but with its own custom classloaders (OSGi, Netbeans RCP classloaders)
   we should add support for such classloaders both to the server and to the client:
   The server should perform class references resolution that is defined by the classloaders used by an application. 
   It also must give to the client not original classloader but it's client reflection that will redirect class loading requests to the server.
   The REST API interface should also be changed to support custom classloaders class references 
   (besides class names, the server should also get a classloader ID to know by which classloader the class should be loaded).
6. Test the technology against Netbeans/IDEA/Eclipse

Nominations
=====
The project has won in "Tech" nomination of HackDay #29 hackathon (www.hackday.ru). The pitch video is here (in Russian):
http://vk.com/video-45718857_166854884
