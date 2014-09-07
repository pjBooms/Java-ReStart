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
package javarestart.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Nikita Lipsky
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-05-24 18:59
 */
public class AppResourceFilter implements Filter {

    //TODO: move to properties
    private static final String APPS_BASE_PATH = "/apps";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final String pathTranslated = ((HttpServletRequest) request).getPathInfo();
        assert (pathTranslated.charAt(0) == '/');
        int resourceIdx = pathTranslated.indexOf('/', 1) + 1;
        if ((resourceIdx > 0) && (pathTranslated.length() != resourceIdx)) {
            final String rewritten = APPS_BASE_PATH + pathTranslated.substring(0, resourceIdx) +
                    "?resource=" + pathTranslated.substring(resourceIdx);
            ((HttpServletResponse) response).sendRedirect(rewritten);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
