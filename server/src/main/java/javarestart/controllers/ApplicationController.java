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
package javarestart.controllers;

import javarestart.appresourceprovider.AppResourceProvider;
import javarestart.appresourceprovider.ResourceNotFoundException;
import javarestart.dto.AppDescriptorDto;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Nikita Lipsky
 */
@Controller
public class ApplicationController {

    public static final String APP_PROFILE = "app.profile";

    @Value("${apps.path}")
    private String appsPath;

    private String rootDir;

    @Autowired
    private ServletContext servletContext;

    private HashMap<String, AppResourceProvider> apps = new HashMap<String, AppResourceProvider>();

    Logger logger = Logger.getLogger(ApplicationController.class.getName());

    private void initRootDirAndContext() {
        if (rootDir == null) {
            if (appsPath.startsWith("${user.home}")) {
                rootDir = System.getProperty("user.home") + appsPath.substring("${user.home}".length());
                logger.info(rootDir);
            } else {
                rootDir = appsPath;
            }
            AppResourceProvider.initContextPath(servletContext.getContextPath());
        }
    }

    private AppResourceProvider getOrRegisterApp(String applicationName) {
        AppResourceProvider resourceProvider = apps.get(applicationName);
        if (resourceProvider == null) {
            try {
                initRootDirAndContext();
                resourceProvider = new AppResourceProvider(rootDir, applicationName);
                apps.put(applicationName, resourceProvider);
            } catch (Exception e) {
                logger.warning(e.toString());
            }
        }
        return resourceProvider;
    }

    @ResponseBody
    @RequestMapping(value = "/{applicationName}", method = RequestMethod.GET)
    public AppDescriptorDto getAppDescriptor(@PathVariable("applicationName") String applicationName, HttpServletResponse response) throws Exception {
        AppResourceProvider resourceProvider = getOrRegisterApp(applicationName);
        if (resourceProvider == null) {
            response.sendError(404);
            return null;
        }
        return resourceProvider.getAppDescriptor();
    }

    @RequestMapping(value = "/{applicationName}", params = {"resource"}, method = RequestMethod.GET)
    public void loadResource(@RequestParam(value = "resource") String resourceName, @PathVariable("applicationName") String applicationName, HttpServletResponse response) throws Exception {
        AppResourceProvider resourceProvider = getOrRegisterApp(applicationName);
        URLConnection resource = null;
        try {
            resource = resourceProvider.load(resourceName);
            resource.connect();
            response.setContentLength(resource.getContentLength());
            IOUtils.copy(resource.getInputStream(), response.getOutputStream());
            response.flushBuffer();
            logger.info("Class or resource loaded: " + resourceName);
        }catch (ResourceNotFoundException e) {
            logger.warning(e.toString());
            response.sendError(404);
        }
    }

    private static final String CRLF = "\r\n";

    @RequestMapping(value = "/{applicationName}", params = {"bundle"}, method = RequestMethod.GET)
    public void loadBundle(@RequestParam(value = "bundle") String bundleName, @PathVariable("applicationName") String applicationName, HttpServletResponse response) throws Exception {
        AppResourceProvider resourceProvider = getOrRegisterApp(applicationName);
        if (resourceProvider == null) {
            response.sendError(404);
            return;
        }
        response.setHeader("Transfer-Encoding", "chunked");
        try (
            OutputStream output = response.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output), true)
        ) {
            Map<String, URL> bundle = resourceProvider.getInitialBundle();
            for (Entry<String, URL> entry: bundle.entrySet().toArray(new Entry[bundle.size()])){
                    String resName = entry.getKey();
                    writer.print(Integer.toHexString(resName.length()));
                    writer.print(CRLF);
                    writer.print(resName);
                    writer.print(CRLF);
                    URL url = entry.getValue();
                    if (url != null) {
                        URLConnection conn = url.openConnection();
                        conn.connect();
                        writer.print(Integer.toHexString(conn.getContentLength()));
                        writer.print(CRLF);
                        writer.flush();
                        IOUtils.copy(conn.getInputStream(), output);
                    } else {
                        writer.print(Integer.toHexString(-1));
                    }
                    output.flush();
                    writer.print(CRLF);
            }
           writer.print("0");
           writer.print(CRLF);
        } catch (IOException e) {
            logger.warning(e.toString());
        }
        response.flushBuffer();
    }

    @PostConstruct
    public void postConstruct() {
        initRootDirAndContext();
        logger.info("Reading apps profiles");
        File[] files = new File(rootDir).listFiles();
        if (files == null) {
            logger.severe("No apps found. Check apps.path property");
            return;
        }
        for (File f: files) {
            File appProfile = new File(f, APP_PROFILE);
            if (appProfile.exists()) {
                AppResourceProvider provider = getOrRegisterApp(f.getName());
                assert provider != null;
                logger.info("Reading profile: " + appProfile.getAbsolutePath());
                try (LineNumberReader reader = new LineNumberReader(new BufferedReader(
                        new InputStreamReader(new FileInputStream(appProfile)))))
                {
                    String resource;
                    while ((resource = reader.readLine()) != null) {
                        try {
                            provider.load(resource);
                        } catch (ResourceNotFoundException e) {
                            logger.warning("Resource does not exist already: " + e.toString());
                        }
                    }
                } catch (IOException e) {
                    logger.severe(e.toString());
                }
            }
        }
        logger.info("Apps profiles were successfully read");
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Writing apps profiles");
        for (Entry<String, AppResourceProvider> app: apps.entrySet()) {
            File appProfile = new File(new File(rootDir, app.getKey()), APP_PROFILE);
            logger.info("Writing profile: " + appProfile.getAbsolutePath());
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(appProfile)))))
            {
                AppResourceProvider provider = app.getValue();
                for (String resource: provider.getInitialBundle().keySet()) {
                    writer.println(resource);
                }
            } catch (IOException e) {
                logger.severe(e.toString());
            }
        }
        logger.info("Apps profiles were successfully wriiten");
    }



}
