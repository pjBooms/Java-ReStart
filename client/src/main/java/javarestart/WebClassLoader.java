/*
 * Copyright (c) 2013-2015, Nikita Lipsky, Excelsior LLC.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Nikita Lipsky
 */
public class WebClassLoader extends URLClassLoader {

    private URL baseURL;

    private final JSONObject descriptor;

    private List<ClassLoaderListener> listeners;

    // Workaround for javafx.scene.media.AudioClip limitation.
    // It only supports http:// and file:// protocols,
    // so we convert java:// and wfx:// URLs to http:// to let .wav resources to be played
    // by AudioClip.
    // TODO: fix the limitation and contribute it to OpenJDK
    private boolean change4WavToHttp;

    private WebClassLoader(URL url, JSONObject desc, boolean baseURL) throws IOException {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.baseURL = baseURL? url : new URL(url, (String) desc.get("root"));
        final String protocol = this.baseURL.getProtocol();
        this.change4WavToHttp = Stream.of("java", "wfx").anyMatch(protocol::equals);
        this.descriptor = desc;
    }

    public WebClassLoader(final URL baseURL) throws IOException {
        this(baseURL, Utils.getJSON(baseURL), true);
    }

    public WebClassLoader(String baseURL) throws IOException {
        this(new URL(baseURL));
    }

    public WebClassLoader(URL url, JSONObject desc) throws IOException{
        this(url, desc, false);
    }

    public void addListener(ClassLoaderListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }
        listeners.add(listener);
    }

    private void fireClassLoaded(String classname) {
        if (listeners == null) return;
        for (ClassLoaderListener l: listeners) {
            l.classLoaded(classname);
        }
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
            Class c = tryToLoadClass(findResourceImpl(name.replace('.', '/') + ".class").openStream());
            fireClassLoaded(name);
            return c;
        } catch (final Exception e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    private URL checkResourceExists(URL url) {
        try {
            url.openStream().close();
            return url;
        } catch (Exception e) {
            return null;
        }
    }

    private URL findResourceImpl(final String name) {
        try {
            return new URL(
                    //see comment above for this hack explanation
                    change4WavToHttp && name.endsWith(".wav")? "http" : baseURL.getProtocol(),
                    baseURL.getHost(),
                    baseURL.getPort(),
                    baseURL.getPath() + '/' + name);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    @Override
    public URL findResource(final String name) {
        return checkResourceExists(findResourceImpl(name));
    }

    @Override
    protected String findLibrary(final String libname) {
        // TODO: nix/mac/solaris?
        File temp = Utils.fetchResourceToTempFile(libname, ".dll", findResource(libname + ".dll"));
        return temp ==  null? null: temp.getAbsolutePath();
    }

    private String getDescField(final String field) {
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

    public URL getBaseURL() {
        return baseURL;
    }
}
