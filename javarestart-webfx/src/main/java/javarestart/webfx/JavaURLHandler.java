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

import javarestart.JavaRestartLauncher;
import webfx.contentdescriptors.ContentDescriptor;
import webfx.urlhandlers.URLHandler;

import java.net.URL;

/**
 * Implementation of {@code webfx.urlhandlers.URLHandler} of {@code java://} protocol.
 * It launches application by {@code java://} URL via Java ReStart.
 *
 * @author Nikita Lipsky
 */
public class JavaURLHandler implements URLHandler {
    @Override
    public String[] getProtocols() {
        return new String[]{"java"};
    }

    @Override
    public Result handle(URL url) {
        JavaRestartLauncher.fork(url.toExternalForm());
        return new Result(ContentDescriptor.NoContent.instance(), null);
    }
}
