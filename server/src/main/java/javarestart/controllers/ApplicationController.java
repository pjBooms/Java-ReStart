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
package javarestart.controllers;

import javarestart.appresourceprovider.AppResourceProvider;
import javarestart.appresourceprovider.ResourceNotFoundException;
import javarestart.dto.AppDescriptorDto;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLConnection;
import java.util.HashMap;
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

	@RequestMapping(value = "/{applicationName}", params ={"resource"} ,method = RequestMethod.GET)
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

}