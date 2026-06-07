package CustomGuiComponents;

import java.util.ArrayList;
import java.util.List;

import entityClasses.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminUserManagementPanel {
	
	// Given the user that's selected from the scroll panel
	public static VBox createUserManagementPanel(String username, String fullName, 
    		String email,String roles) { 

	    VBox rBox = new VBox(15);
	    rBox.setPadding(new Insets(20));

	    Label panelTitle = new Label("User Actions");
	    Label selectedUser = new Label(username);

	    ComboBox<String> roleSelectorAdd = new ComboBox<>();
	    ComboBox<String> roleSelectorRemove = new ComboBox<>();

	    roleSelectorAdd.getItems().addAll("Admin", "Student", "Instructor");
	    roleSelectorRemove.getItems().addAll("Admin", "Student", "Instructor");

	    Button setOneTime = new Button("Set One-Time Password");
	    Button add = new Button("Add Role");
	    Button remove = new Button("Remove Role");
	    Button delete = new Button("Delete User");

	    HBox addRow = addRemove("Add Role:", roleSelectorAdd, add);
	    HBox removeRow = addRemove("Remove Role:", roleSelectorRemove, remove);

	    rBox.getChildren().addAll(
	        panelTitle,
	        selectedUser,
	        setOneTime,
	        addRow,
	        removeRow,
	        delete
	    );

	    return rBox;
	}

	private static HBox addRemove(String labelText, ComboBox<String> comboBox, Button button) {
	    HBox row = new HBox(10);
	    row.setAlignment(Pos.CENTER_LEFT);

	    Label label = new Label(labelText);

	    row.getChildren().addAll(
	        label,
	        comboBox,
	        button
	    );

	    return row;
	}
}