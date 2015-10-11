package javarestart.appresourceprovider;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author kit
 */
public class IdeaClassLoaders {

    private HashMap<String, URL> classLoaders = new HashMap<>();

    public IdeaClassLoaders(File desc) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(desc))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line);
                String clID = tok.nextToken();
                String resName = tok.nextToken();
                String urlS = tok.nextToken();
                URL url = urlS.equals("null") ? null : new URL(urlS);
                classLoaders.put(clID + '/' + resName, url);
            }
        }
    }

    public URL getURL(String resName) {
        return classLoaders.get(resName);
    }
}
