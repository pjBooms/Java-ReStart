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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.excelsior.javarestart;

import java.io.IOException;
import java.lang.reflect.Method;

public class Main {

    public static void fork(String args[])  {

        String javaHome = System.getProperty("java.home");
        System.out.println(javaHome);
        String codeSource = Main.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm().substring(6);
        String fxrt = javaHome + "\\lib\\jfxrt.jar";
        String classpath = "\"" + codeSource + ";" + fxrt + "\"";
        System.out.println(codeSource);
        String javaLauncher = "\"" + javaHome + "\\bin\\javaw.exe\"" + " -Dbinary.css=false -cp " + classpath + " " + Main.class.getName();
        for (String arg: args) {
            javaLauncher = javaLauncher + " " + arg;
        }

        System.out.println(javaLauncher);

        final String finalJavaLauncher = javaLauncher;
        (new Thread(){
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec(finalJavaLauncher).waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: <URL> <MainClass>");
            return;
        }

        if (args[0].equals("fork")) {
            String[] args2 = new String[args.length - 1];
            for (int i = 0; i < args.length -1; i++) {
                args2[i] = args[i + 1];
            }
            fork(args2);
            return;
        }

        AppClassloader loader = new AppClassloader(args[0]);
        Thread.currentThread().setContextClassLoader(loader);
        Class mainClass = loader.loadClass(args[1]);
        Method main = mainClass.getMethod("main", String[].class);
        main.setAccessible(true);
        main.invoke(null, new Object[]{new String[0]});
    }
}
