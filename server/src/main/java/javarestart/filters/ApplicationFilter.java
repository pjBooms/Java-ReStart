package javarestart.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-05-24 18:59
 */
public class ApplicationFilter implements Filter {
    private static final Pattern SLASH = Pattern.compile("/");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final String pathTranslated = ((HttpServletRequest) request).getPathInfo();
        if (!pathTranslated.endsWith("/")) {
            final String[] split = SLASH.split(pathTranslated);
            if (split.length > 2) {
                final StringBuilder stringBuilder = new StringBuilder();
                for (int i = 2 ; i< split.length; i++) {
                    stringBuilder.append(split[i]).append('/');
                }

                if (stringBuilder.length() > 0) {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                }

                final String rewritten = "/apps/" + split[1] + "/?resource=" + stringBuilder;
                ((HttpServletResponse) response).sendRedirect(rewritten);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
