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
