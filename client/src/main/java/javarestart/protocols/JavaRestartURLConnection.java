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
package javarestart.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

/**
 * URLConnection implementation for {@code java://} and {@code wfx://} protocols.
 * The protocols are based on {@code http://} inside but it maybe changed in the future.
 *
 * @author Nikita Lipsky
 */
public class JavaRestartURLConnection extends URLConnection {

    private URLConnection redirectConnection;

    /**
     * Converts URLs of {@code java://} and {@code wfx://} to {@code http://}.
     */
    private static URL convertToHTTP(URL url) {
        try {
            return url.getProtocol().equals("http")? url : new URL("http", url.getHost(), url.getPort(), url.getFile());
        } catch (MalformedURLException e) {
            return url;
        }
    }

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    public JavaRestartURLConnection(URL url) {
        super(url);
        try {
            redirectConnection = convertToHTTP(url).openConnection();
        } catch (IOException e) {
        }
    }

    @Override
    public void connect() throws IOException {
        redirectConnection.connect();
        connected = true;
    }

    @Override
    public long getContentLengthLong() {
        return redirectConnection.getContentLengthLong();
    }


    @Override
    public long getLastModified() {
        return redirectConnection.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
        return redirectConnection.getHeaderField(name);
    }

    @Override
    public Permission getPermission() throws IOException {
        return redirectConnection.getPermission();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return redirectConnection.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return redirectConnection.getOutputStream();
    }
}
