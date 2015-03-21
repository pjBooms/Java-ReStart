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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Nikita Lipsky
 */
@Controller
public class ApplicationController {

    @Value("${apps.path}")
    private String appsPath;

    private String rootDir;

    private HashMap<String, AppResourceProvider> projectLoader = new HashMap<String, AppResourceProvider>();

    Logger logger = Logger.getLogger(ApplicationController.class.getName());

    private void initRootDir() {
        if (rootDir == null) {
            if (appsPath.startsWith("${user.home}")) {
                rootDir = System.getProperty("user.home") + appsPath.substring("${user.home}".length());
                logger.info(rootDir);
            } else {
                rootDir = appsPath;
            }

        }
    }

    private AppResourceProvider getOrRegisterApp(String applicationName) {
        AppResourceProvider resourceProvider = projectLoader.get(applicationName);
        if (resourceProvider == null) {
            try {
                initRootDir();
                resourceProvider = new AppResourceProvider(rootDir, applicationName);
                projectLoader.put(applicationName, resourceProvider);
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
            for (Entry<String, URL> entry: resourceProvider.getInitialBundle().entrySet()){
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


}
