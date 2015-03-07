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
package javarestart.protocols.wfx;

import javarestart.protocols.JavaRestartURLConnection;
import javarestart.WebClassLoader;
import javarestart.WebClassLoaderRegistry;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 *  Implementation of {@code URLStreamHandler} of {@code wfx://} protocol.
 *  It defines classloader for fxml pages to {@link WebClassLoader} of Java ReStart.
 *
 * @author Nikita Lipsky
 */
public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        WebClassLoader classLoader = WebClassLoaderRegistry.getClassLoader(u);
        if (classLoader != null) {
            return classLoader.getFxml().openConnection();
        }
        return new JavaRestartURLConnection(u);
    }
}
