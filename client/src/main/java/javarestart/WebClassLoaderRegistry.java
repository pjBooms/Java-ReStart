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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * @author Nikita Lipsky
 */
public class WebClassLoaderRegistry {

    private static HashMap<URL, WebClassLoader> classloaders = new HashMap<>();
    private static HashMap<URL, WebClassLoader> associatedClassloaders = new HashMap<>();

    public static WebClassLoader getClassLoader(URL url) {
        return classloaders.get(url);
    }

    public static WebClassLoader resolveClassLoader(URL url) {
        if (url.getPath().endsWith("/..")) {
            return null;
        }
        WebClassLoader cl = null;
        URL baseURL = url;
        try {
            URL rootURL = new URL(baseURL, "/");
            while (((cl = associatedClassloaders.get(baseURL)) == null) && !baseURL.equals(rootURL)) {
                baseURL = new URL(baseURL, "..");
            }
        } catch (MalformedURLException e) {
        }

        if (cl == null) {
            try {
                JSONObject desc = Utils.getJSON(new URL(
                        url.toExternalForm() + "?getAppDescriptor"));
                if (desc != null) {
                    cl = new WebClassLoader(url, desc);
                }
            } catch (Exception e) {
            }
        }
        associatedClassloaders.put(url, cl);
        if (cl != null) {
            associatedClassloaders.put(cl.getBaseURL(), cl);
            classloaders.put(cl.getBaseURL(), cl);
        }
        return cl;
    }
}
