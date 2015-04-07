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
*/package javarestart.webfx;

import webfx.browser.WebFX;

/**
 *
 * WebFX browser launcher that sets System properties for java://, wfx:// protocols initialization before start.
 *
 * @author Nikita Lipsky
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("java.protocol.handler.pkgs","javarestart.protocols");
        System.setProperty("webfx.url.handlers", "javarestart.webfx.JavaURLHandler,javarestart.webfx.WFXURLHandler");
        WebFX.main(args);
    }
}
