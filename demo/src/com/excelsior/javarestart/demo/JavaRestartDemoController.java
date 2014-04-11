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
package com.excelsior.javarestart.demo;

import java.net.URL;
import java.util.ResourceBundle;

import com.excelsior.javarestart.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author Nikita Lipsky
 */
public class JavaRestartDemoController implements Initializable {
    
    @FXML
    private Label label;

    private static final String host = "http://localhost:8080";

    @FXML
    public void handleButtonAction(ActionEvent event) {
        String args[];
        switch (((Button)event.getSource()).getId()){
            case "SwingSet2": args = new String[]{host + "/SwingSet2/", "SwingSet2"}; break;
            case "Java2Demo": args = new String[]{host + "/Java2Demo/",  "java2d/Java2Demo"}; break;
            case "Ensemble" : args = new String[]{host + "/javafx/",  "ensemble/Ensemble2"}; break;
            case "BrickBreaker": args = new String[]{host + "/BrickBreaker/",  " brickbreaker/Main"}; break;
            case "Jenesis": args = new String[]{host + "/jenesis/",  "jenesis/Main"}; break;
            case "SWT": args = new String[]{host + "/swt/",  "org/eclipse/swt/examples/controlexample/ControlExample"}; break;
            default: throw new AssertionError();
        }
        Main.fork(args);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
