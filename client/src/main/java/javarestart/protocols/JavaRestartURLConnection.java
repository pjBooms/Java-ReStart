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

import javarestart.WebClassLoader;
import javarestart.WebClassLoaderRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.Permission;

/**
 *
 * <pre>
 * URLConnection implementation for {@code java://} and {@code wfx://} protocols.
 *
 * It works as follows:
 *
 * - if the URL is associated with some remote application
 *   (some base URL is already associated with remote application or
 *     "?getDescription" subquery returns remote app description)
 *   then {@link WebClassLoader} linked to remote application may have prefetched content of the URL in the memory
 *   based on remote application usage profile. In this case this URL connection incapsulates access to the prefetched
 *   content.
 *
 * - otherwise http:// connection is created internally with the same path as original URL
 *   and the contents is fetched using the created connection handling broken connections though.
 *
 * </pre>
 *
 * @author Nikita Lipsky
 */
public class JavaRestartURLConnection extends URLConnection {

    private static final int CONNECT_ATTEMPTS = 5;
    private static final int CONNECTION_TIMEOUT = 1500;

    private URLConnection redirectConnection;

    private WebClassLoader classLoader;
    private boolean inMem;

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
        if (url.getQuery() == null) {
            classLoader = WebClassLoaderRegistry.resolveClassLoader(url);
            inMem = classLoader!=null && classLoader.isPreLoaded(url);
        }
        if (!inMem) {
            try {
                redirectConnection = convertToHTTP(url).openConnection();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void connect() throws IOException {
        if (!inMem) {
            redirectConnection.connect();
        }
        connected = true;
    }

    @Override
    public String getContentType() {
        if (inMem) {
            String contentType = null;
            try {
                contentType = URLConnection.guessContentTypeFromStream(classLoader.getPreLoadedResourceAsStream(getURL()));
                if (contentType == null) {
                    contentType = URLConnection.guessContentTypeFromName(url.getFile());
                }
            } catch (IOException e) {
            }
            return  (contentType == null) ?"content/unknown" : contentType;
        } else {
            return redirectConnection.getContentType();
        }
    }

    @Override
    public long getContentLengthLong() {
        if (inMem) {
            return classLoader.getPreLoadedResourceLength(url);
        }
        return redirectConnection.getContentLengthLong();
    }

    @Override
    public long getLastModified() {
        if (inMem) {
            try {
                return convertToHTTP(url).openConnection().getLastModified();
            } catch (IOException e) {
                return System.currentTimeMillis();
            }
        }
        return redirectConnection.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
        return inMem? null : redirectConnection.getHeaderField(name);
    }

    @Override
    public Permission getPermission() throws IOException {
        return inMem? super.getPermission() : redirectConnection.getPermission();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inMem) {
            return classLoader.getPreLoadedResourceAsStream(getURL());
        } else {
            redirectConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            for (int i = 0; i < CONNECT_ATTEMPTS; i++) {
                try {
                    return redirectConnection.getInputStream();
                } catch (SocketTimeoutException e) {
                }
                redirectConnection = convertToHTTP(url).openConnection();
                redirectConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                redirectConnection.connect();
            }
            throw new SocketTimeoutException("Failed to connect to " + url + " with " + CONNECT_ATTEMPTS + " attempts.");
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (inMem) {
            throw new UnknownServiceException();
        }
        return redirectConnection.getOutputStream();
    }
}
