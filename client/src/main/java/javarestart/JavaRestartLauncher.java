/*
 * Copyright (c) 2013-2014, Nikita Lipsky, Excelsior LLC.
 *
 *  Java ReStart is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Java ReStart is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Java ReStart.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package javarestart;

import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Stream;

public final class JavaRestartLauncher {
    
    private JavaRestartLauncher() {
    }

    private static File splashLocation;

    public static void fork(final String ... args)  {

        String javaHome = System.getProperty("java.home");
        System.out.println(javaHome);
        if (splashLocation == null) {
            File codeSource = new File(JavaRestartLauncher.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm().substring(6));
            System.out.println(codeSource);
            if (codeSource.isDirectory()) {
                splashLocation = new File (codeSource, "defaultSplash.gif");
            } else {
                splashLocation = Utils.fetchResourceToTempFile("defaultSplash",
                        ".gif", JavaRestartLauncher.class.getClassLoader().getResource("defaultSplash.gif"));
            }
        }

        String classpath = System.getProperty("java.class.path");

        final File javaLauncherPath;
        switch (OS.get()) {
            case WINDOWS:
                javaLauncherPath = new File(javaHome, "\\bin\\javaw");
                break;
            case NIX: case MAC:
                javaLauncherPath = new File(javaHome,  "/bin/java");
                break;
            default:
                throw new UnsupportedOperationException();
        }

        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(javaLauncherPath.getAbsolutePath());
        cmdArgs.add("-splash:" + splashLocation.getAbsolutePath());
        cmdArgs.add("-Dbinary.css=false");
        cmdArgs.add("-Djava.protocol.handler.pkgs=javarestart.protocols");
        cmdArgs.add("-cp");
        cmdArgs.add(classpath);
        cmdArgs.add(JavaRestartLauncher.class.getName());

        for (final String arg: args) {
            cmdArgs.add(arg);
        }

        final String[] cmd = cmdArgs.toArray(new String[cmdArgs.size()]);
        (new Thread(){
            @Override
            public void run() {
                try {
                    final Process p = Runtime.getRuntime().exec(cmd);
                    Thread stdOutReader = new Thread() {
                        @Override
                        public void run() {
                            // read the output from the command
                            System.out.println("Here is the standard output of the command:\n");

                            BufferedReader stdInput = new BufferedReader(new
                                    InputStreamReader(p.getInputStream()));
                            String s;
                            try {
                                while ((s = stdInput.readLine()) != null) {
                                    System.out.println(s);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    Thread stdErrReader = new Thread(){
                        @Override
                        public void run() {
                            BufferedReader stdError = new BufferedReader(new
                                    InputStreamReader(p.getErrorStream()));
                            // read any errors from the attempted command
                            System.out.println("Here is the standard error of the command (if any):\n");
                            String s;
                            try {
                                while ((s = stdError.readLine()) != null) {
                                    System.out.println(s);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    };

                    stdOutReader.start();
                    stdErrReader.start();

                    p.waitFor();
                    stdOutReader.join();
                    stdErrReader.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void closeSplash() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        SplashScreen scr = SplashScreen.getSplashScreen();
                        if ((scr != null) && (scr.isVisible())) {
                            scr.close();
                        }
                    }
                }
        );
    }

    private static void printUsage() {
        System.out.println("Usage: <URL> {<MainClass>}");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            args = new String[]{"fork", "java://javarestart.com/apps/WebFX"};
        }

        if (Stream.of("?", "help").anyMatch(args[0]::equals)) {
            printUsage();
            return;
        }

        if ("fork".equals(args[0])) {
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args.length - 1);
            fork(args2);
            return;
        }

        System.setProperty("java.protocol.handler.pkgs","javarestart.protocols");

        final WebClassLoader loader = WebClassLoaderRegistry.resolveClassLoader(new URL(args[0]));
        Thread.currentThread().setContextClassLoader(loader);
        String main;
        final JSONObject obj = Utils.getJSON(args[0]);
        if (args.length < 2) {
            main = (String) obj.get("main");
        } else {
            main = args[1];
        }

        String splash = (String) obj.get("splash");
        if (splash != null) {
            SplashScreen scr = SplashScreen.getSplashScreen();
            if (scr != null) {
                URL url = loader.getResource(splash);
                scr.setImageURL(url);
            }
        }

        String splashCloseOnProp = (String) obj.get("splashCloseOn");
        if (splashCloseOnProp != null) {
            final String splashCloseOn = splashCloseOnProp.replace('.', '/');
            loader.addListener(new ClassLoaderListener() {
                @Override
                public void classLoaded(String classname) {
                    if (classname.replace('.', '/').equals(splashCloseOn)) {
                        closeSplash();
                    }
                }
            });
        }

        //auto close splash after 45 seconds
        Thread splashClose = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(45000);
                } catch (InterruptedException e) {
                }
                closeSplash();
            }
        };
        splashClose.setDaemon(true);
        splashClose.setPriority(Thread.MIN_PRIORITY);
        splashClose.start();

        Class mainClass = loader.loadClass(main);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.setAccessible(true);
        mainMethod.invoke(null, new Object[]{new String[0]});
    }
}
