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
package com.excelsior.javarestart.controllers;

import com.excelsior.javarestart.appresourceprovider.ResourceNotFoundException;
import com.excelsior.javarestart.appresourceprovider.SimpleResourceProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Nikita Lipsky
 */
@Controller
public class ApplicationController {

    @Value("${apps.path}")
    private String rootDir;

    private HashMap<String, SimpleResourceProvider> projectLoader = new HashMap<String, SimpleResourceProvider>();

    Logger logger = Logger.getLogger(ApplicationController.class.getName());

	@RequestMapping(value = "/{applicationName}", params ={"resource"} ,method = RequestMethod.GET)
	public void loadResource(@RequestParam(value = "resource") String resourceName, @PathVariable("applicationName") String applicationName, HttpServletResponse response) throws Exception {
        SimpleResourceProvider cl = projectLoader.get(applicationName);
        if (cl == null) {
            try {
                cl = new SimpleResourceProvider(rootDir, applicationName);
                projectLoader.put(applicationName, cl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        InputStream resource = null;
        try {
            resource = cl.load(resourceName);
            IOUtils.copy(resource, response.getOutputStream());
            response.flushBuffer();
            logger.info("Class or resource loaded: " + resourceName);
        }catch (ResourceNotFoundException e) {
            logger.warning(e.toString());
            response.sendError(404);
        } finally {
            try {
                if (resource != null)
                    resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}