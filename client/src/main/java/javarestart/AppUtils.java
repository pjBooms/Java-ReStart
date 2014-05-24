package javarestart;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-05-24 12:41
 */
public final class AppUtils {
    private AppUtils() {
    }

    public static JSONObject getJSON(final String url) throws IOException {
        return getJSON(new URL(url));
    }

    public static JSONObject getJSON(final URL url) throws IOException {
        return (JSONObject) JSONValue.parse(getText(url));
    }

    public static String getText(final URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (LineNumberReader in = new LineNumberReader(
                new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")))) {

            final StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }

}
