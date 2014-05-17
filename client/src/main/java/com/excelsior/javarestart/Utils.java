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
 *  along with Java ReStart.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.excelsior.javarestart;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

    static File fetchResourceToTempFile(String resName, String resExt, URL from) {
        File temp;
        try {
            temp = File.createTempFile(resName, resExt);
        } catch (IOException e) {
            return null;
        }
        temp.deleteOnExit();
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(temp)))
        {
            copy(from.openStream(), os);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return temp;
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            out.write(data, 0, nRead);
        }
    }
}
