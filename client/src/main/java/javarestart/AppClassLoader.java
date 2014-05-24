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
package javarestart;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Nikita Lipsky
 */
public class AppClassLoader extends URLClassLoader {

    private URL baseURL;

    private boolean local;
    private final JSONObject descriptor;

    public AppClassLoader(final URL baseURL) throws IOException {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.baseURL = baseURL;
        this.local = this.baseURL.getProtocol().equals("file");
        this.descriptor = AppUtils.getJSON(baseURL);
    }

    public AppClassLoader(String baseURL) throws IOException {
        this(new URL(baseURL));
    }

    private Class<?> tryToLoadClass(InputStream in) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Utils.copy(in, buffer);
        buffer.flush();

        byte buf[] = buffer.toByteArray();
        return defineClass(null, buf, 0, buf.length);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return tryToLoadClass(findResource(name.replace('.', '/') + ".class").openStream());
        } catch (final Exception e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    @Override
    public URL findResource(final String name) {
        final String resName = local ? name : "?resource=" + name;
        try {
            return new URL(baseURL.getProtocol(),
                    baseURL.getHost(),
                    baseURL.getPort(),
                    baseURL.getPath() + '/' + resName);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    @Override
    protected String findLibrary(String libname) {
        // TODO: nix/mac/solaris?
        File temp = Utils.fetchResourceToTempFile(libname, ".dll", findResource(libname + ".dll"));
        return temp ==  null? null: temp.getAbsolutePath();
    }

    private String getDescField(String field) {
        return (String) descriptor.get(field);
    }

    public URL getFxml() {
        return getResource(getDescField("fxml"));
    }

    public URL getSplash() {
        return getResource(getDescField("splash"));
    }

    public Class<?> getMain() throws ClassNotFoundException {
        return loadClass(getDescField("main"));
    }
}
