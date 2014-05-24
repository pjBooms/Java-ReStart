package javarestart;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-05-24 11:28
 */
public enum OS {
    WINDOWS
    , MAC
    , NIX
    , SOLARIS
    , UNKNOWN
    ;

    private static final OS DETECTED_OS = getFromSystemProperties();

    public static OS get() {
        return DETECTED_OS;
    }

    private  static OS getFromSystemProperties() {
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("nix")
            || osName.contains("nux")
            || osName.contains("aix")) {
            return NIX;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("mac")) {
            return MAC;
        } else if (osName.contains("sunos")) {
            return SOLARIS;
        }

        return UNKNOWN;
    }
}
