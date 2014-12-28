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
package javarestart.webfx;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@code java://} and {@code wfx://} protocols are based on {@code http://}.
 * This utility class converts URLs of {@code java://} and {@code wfx://} to {@code http://}.
 *
 * @author Nikita Lipsky
 */
public class URLConverter {

    public static URL convertToHTTP(URL url) {
        try {
            return url.getProtocol().equals("http")? url : new URL("http", url.getHost(), url.getPort(), url.getFile());
        } catch (MalformedURLException e) {
            return url;
        }

    }

}
