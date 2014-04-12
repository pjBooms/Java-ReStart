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
The server gives classes/resources of an application on a request and the client downloads classes/resources of an application on demand (lazily) 
and executes the application in parallel with downloading.
The server has a very simple REST interface now:

```
GET /{application} -- returns a main class name of an application (entry point)
GET /{application}/?resource={resource} -- returns a class/resource of a referenced application
```

The client in turn has a very simple command line interface:

```
java com.excelsior.javarestart.Main <URL>
```

where URL has a form [Host]/[AppName].

First, the client asks the main class of an application then it downloads main class and loads it using a classloader that tries to emulate default JVM application classloader 
but instead of loading the classes from HDD it fetches them from URL using REST interface above. 
This way, only required classes/resources are downloaded by the client and application starts right from the first downloaded main class.

How to run 
=====
The sources come with a demo sustaining the concept.

First you need to run the server in an application server of your choice (Tomcat, Jetty, whatever).
Before the run you should setup demo applications: unzip apps.zip to a directory (say [AppRoot]) and 
correct server/src/main/resources/application.properties apps.path property pointing to [AppRoot].
apps.zip contains six Java UI applications with the descriptions (app.properties): 
  * Java2Demo - standard AWT/Java2D demo, 
  * SwingSet2 - standard Swing demo, 
  * SWT - demo showing SWT standard controls,
  * Jenesis - Sega Genesis emulator written using Java OpenGL (jogl),
  * BrickBreaker - JavaFX arcanoid game demo
  * Ensemble - standard JavaFX ensemble demo

After launching the server, you may run the apps using
```
java com.excelsior.javarestart.Main http://localhost:8080/<AppName> 
```
command (URL example -- http://localhost:8080/Java2Demo).

Or you may run JavaFX demo that in turn will run the demos above by itself (located in "demo" folder):
```
java com.excelsior.javarestart.demo.JavaRestartDemo
```

Run Notes:
Ensemble demo does not work with Java 8 now and with Java 7 it does not load all resources that are referenced by the demo (f.i. it does not load "close" button icon).
SWT, Jenesis can run only with 32-bit JRE on Windows (they are using 32-bit native libraries).
JavaRestartDemo is forking JVM to run demos and the way it forks JVM can work on Windows only.

Adding your own applications
=====
To add your own application that you would like to launch from Internet you need to put it to a subfolder of [AppRoot] folder 
and provide app.properties where you describe main class and classpath of your application (see apps.zip applications for example). 
After that you may launch it with the client:

```
java com.excelsior.javarestart.Main <Host>/<AppName>
```
TODO
=====
1. Implement caching of downloaded classes by the client 
2. Support versioning of the apps on the server (thus if a version is not changed on the server, the cached version can be taken safely)
3. Implement profiling class/resource retrieving sequence on the server and change the server REST interface to allow to upload frequently used classes/resources 
   by a single HTTP response with a aggressively packed stream with the right class order. This way we can drastically optimize the startup of applications 
   (no need to handle thousands of HTTP requests).
4. Improve application descriptions. Now only classpath and main is specified for an application but it is good also to provide VM properties, spalsh, etc.
5. Support custom classloading. If an application is loaded not only by application classloader but with its own custom classloaders (OSGi, Netbeans RCP classloaders)
   we should add support for such classloaders both to the server and to the client:
   The server should perform class references resolution that is defined by the classloaders used by an application. 
   It also must give to the client not original classloader but it's client reflection that will redirect class loading requests to the server.
   The REST API interface should also be changed to support custom classloaders class references 
   (besides class names, the server should also get a classloader ID to know by which classloader the class should be loaded).
6. Test the technology against Netbeans/IDEA/Eclipse
