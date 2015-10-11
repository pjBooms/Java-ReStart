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
package javarestart.appresourceprovider;

import javarestart.dto.AppDescriptorDto;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Nikita Lipsky
 */
public class AppResourceProvider {
    private static final Pattern PATH_SEP = Pattern.compile(";");
    private static final String APP_PROPERTIES = "app.properties";
    private final URLClassLoader loader;
    private final AppDescriptorDto appDescriptor;
    private final IdeaClassLoaders ideaClassLoaders;

    private final LinkedHashMap<String, URL> loaded = new LinkedHashMap<>();

    //TODO: move to properties
    private static final String APPS_BASE_PATH = "/apps";

    private static String contextPath;

    public static void initContextPath(String contextPath) {
        AppResourceProvider.contextPath = contextPath;
    }

    public AppResourceProvider(String appPath, String projectName) throws Exception {
        final Properties appProps = new Properties();
        final File baseDir = new File(appPath, projectName);
        appProps.load(new BufferedInputStream(new FileInputStream(new File(baseDir, APP_PROPERTIES))));
        appDescriptor = new AppDescriptorDto();
        appDescriptor.setRoot(contextPath + APPS_BASE_PATH + '/' + projectName);
        appDescriptor.setMain(appProps.getProperty("main"));
        appDescriptor.setSplash(appProps.getProperty("splash"));
        appDescriptor.setSplashCloseOn(appProps.getProperty("splashCloseOn"));
        appDescriptor.setFxml(appProps.getProperty("fxml"));
        appDescriptor.setIncludePackages(appProps.getProperty("includePackages"));
        final String[] classPath = PATH_SEP.split(appProps.getProperty("classpath"));
        final List<URL> urls = new ArrayList<>(classPath.length);
        for (final String path : classPath) {
            final String classPathElement = path.trim();
            if (!classPathElement.isEmpty()) {
                urls.add(new File(baseDir, classPathElement).toURI().toURL());
            }
        }
        String ideaDesc = appProps.getProperty("ideaDesc");
        ideaClassLoaders = ideaDesc == null ? null : new IdeaClassLoaders(new File(baseDir, ideaDesc));
        loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    public URLConnection load(final String resourceName) throws ResourceNotFoundException {
        URL result = null;
        try {
            if (ideaClassLoaders != null) {
                result = ideaClassLoaders.getURL(resourceName);
            }
            if (result == null) {
                result = loader.findResource(resourceName);
            }
            if (result == null) {
                throw new ResourceNotFoundException("Requested resource not found: " + resourceName);
            }
            return result.openConnection();
        } catch (final IOException e) {
            result = null;
            throw new ResourceNotFoundException("Requested resource not found: " + resourceName, e);
        } finally {
            if (!loaded.containsKey(resourceName)) {
                loaded.put(resourceName, result);
            }
        }
    }

    public Map<String, URL> getInitialBundle() {
        return loaded;
    }

    public AppDescriptorDto getAppDescriptor() {
        return appDescriptor;
    }
}
