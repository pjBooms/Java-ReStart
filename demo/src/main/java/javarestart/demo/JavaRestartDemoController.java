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
package javarestart.demo;

import java.net.URL;
import java.util.ResourceBundle;

import javarestart.JavaRestartLauncher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 *
 * @author Nikita Lipsky
 */
public class JavaRestartDemoController implements Initializable {
    
    @FXML
    public void handleButtonAction(ActionEvent event) {
        String args[] = new String[] {JavaRestartDemo.host + ((Button)event.getSource()).getId()};
        JavaRestartLauncher.fork(args);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
}
