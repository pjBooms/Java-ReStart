package javarestart;

/**
 * Listener that listens class loading events from the {@code WebClassLoader}.
 */
public interface ClassLoaderListener {

    public void classLoaded(String classname);

}
