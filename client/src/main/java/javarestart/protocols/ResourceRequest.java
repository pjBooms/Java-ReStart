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
package javarestart.protocols;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Semaphore;

/**
 * Utility class that is used for contest between resource preloading and separate URL request for the resource.
 * It encapsulates semaphore on which the thread that requesting the resource waits the contest result.
 *
 * @author Nikita Lipsky
 */
public class ResourceRequest {

    private URL request;

    private volatile byte[] content;

    private Semaphore semaphore;

    private volatile IOException exception;

    public ResourceRequest(URL request) {
        this.request = request;
        semaphore = new Semaphore(0);
    }

    public URL getRequest() {
        return request;
    }

    public void contentReady(byte[] content) {
        if (this.content == null) {
            this.content = content;
            semaphore.release();
        }
    }

    public void fail(IOException e) {
        if (this.content == null) {
            exception = e;
            semaphore.release();
        }
    }

    public boolean isFailed() {
        return exception != null;
    }

    public IOException getFail() {
        return exception;
    }

    public void waitForContent() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public byte[] getContent() {
        return content;
    }

}
