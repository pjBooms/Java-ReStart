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
package javarestart.appresourceprovider;

import javarestart.dto.AppDescriptorDto;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author Nikita Lipsky
 */
public class AppResourceProvider {
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final String APP_PROPERTIES = "app.properties";

    private final URLClassLoader loader;
    private final AppDescriptorDto appDescriptor;

    public AppResourceProvider(String appPath, String projectName) throws Exception {
        Properties appProps = new Properties();
        File baseDir = new File(appPath, projectName);
        appProps.load(new BufferedInputStream(new FileInputStream(new File(baseDir, APP_PROPERTIES))));
        appDescriptor = new AppDescriptorDto();
        appDescriptor.setMain(appProps.getProperty("main"));
        appDescriptor.setSplash(appProps.getProperty("splash"));
        appDescriptor.setFxml(appProps.getProperty("fxml"));
        String classPath[] = SEMICOLON.split(appProps.getProperty("classpath"));
        URL[] urls = new URL[classPath.length];
        for (int i = 0; i < classPath.length; i++) {
            urls[i] = new File(baseDir, classPath[i]).toURI().toURL();
        }
        loader = new URLClassLoader(urls);
    }

    public URLConnection load(final String resourceName) throws ResourceNotFoundException {
        try {
            URL result = loader.findResource(resourceName);
            if (result == null) {
                throw new ResourceNotFoundException("Requested resource not found: "+ resourceName);
            }
            return result.openConnection();
        } catch (IOException e) {
            throw new ResourceNotFoundException("Requested resource not found: "+ resourceName, e);
        }
    }

    public AppDescriptorDto getAppDescriptor() {
        return appDescriptor;
    }
}
