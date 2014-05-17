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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Nikita Lipsky
 */
public class AppClassloader extends URLClassLoader {

    private URL baseURL;

    private boolean local;

    public AppClassloader(String baseURL) throws MalformedURLException {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.baseURL = new URL(baseURL);
        this.local = this.baseURL.getProtocol().equals("file");
    }

    private Class tryToLoadClass(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        Utils.copy(in, buffer);
        buffer.flush();

        byte buf[] = buffer.toByteArray();
        return defineClass (buf, 0, buf.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return tryToLoadClass(findResource(name.replace('.', '/') + ".class").openStream());
        } catch (Exception e) {
            throw new ClassNotFoundException ();
        }
    }

    @Override
    public URL findResource(String name) {
        String resName = local ? name : "?resource=" + name;
        try {
            return new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(), baseURL.getPath() + "/" + resName);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    protected String findLibrary(String libname) {
        File temp = Utils.fetchResourceToTempFile(libname, ".dll", findResource(libname + ".dll"));
        return temp ==  null? null: temp.getAbsolutePath();
    }
}
