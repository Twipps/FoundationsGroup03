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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminUserManagementPanel {
	
	// Given the user that's selected from the scroll panel
	public static VBox createUserManagementPanel(String username, String fullName, 
    	String email,String roles) { 

	    VBox rBox = new VBox(15);
	    rBox.setPadding(new Insets(20));
	    
	    //rBox.setStyle("-fx-background-color: #f7e092;");
	    Label panelTitle = new Label("User Actions for: ");
	    Label selectedUser = new Label(username);
	    Label noOmnimanPls = new Label("Are you sure?");

	    ComboBox<String> roleSelectorAdd = new ComboBox<>();
	    ComboBox<String> roleSelectorRemove = new ComboBox<>();
	    
	    roleSelectorAdd.setPromptText("Choose Role");
	    roleSelectorRemove.setPromptText("Choose Role");

	    roleSelectorAdd.getItems().addAll("Admin", "Student", "Instructor"); // STILL NEED TO ADD PREVIOUS FUNCTIONALITY
	    roleSelectorRemove.getItems().addAll("Admin", "Student", "Instructor");

	    Button setOneTime = new Button("Set One-Time Password");
	    Button add = new Button("Add Role");
	    Button remove = new Button("Remove Role");
	    Button delete = new Button("Delete User");
	    Button yes = new Button("Yes");
	    Button no = new Button("No");

	    HBox addRow = addRemove("Add Role:", roleSelectorAdd, add);
	    HBox removeRow = addRemove("Remove Role:", roleSelectorRemove, remove);
	    HBox deleteRow = new HBox();
	    HBox yesNo = new HBox();
	    
	    yesNo.getChildren().addAll(yes, createSpacer(5), no);
	   
	    VBox yesNoWithPrompt = new VBox();
	    yesNoWithPrompt.setVisible(false);
	    
	    yesNoWithPrompt.getChildren().addAll(noOmnimanPls, yesNo);
	    
	    deleteRow.getChildren().addAll(delete, createSpacer(15), yesNoWithPrompt);
	    
	    HBox userSelection = new HBox();
	    userSelection.getChildren().addAll(panelTitle, selectedUser);
	    
	    delete.setOnAction((_) -> {yesNoWithPrompt.setVisible(true);});
	    yes.setOnAction((_) -> {yesNoWithPrompt.setVisible(false);});
	    no.setOnAction((_) -> {yesNoWithPrompt.setVisible(false);});

	    rBox.getChildren().addAll( userSelection, setOneTime, 
	    		addRow, removeRow, deleteRow);

	    return rBox;
	}

	private static HBox addRemove(String labelText, ComboBox<String> comboBox, Button button) {
	    HBox row = new HBox(10);
	    row.setAlignment(Pos.CENTER_LEFT);

	    row.getChildren().addAll(
	        comboBox,
	        button
	    );

	    return row;
	}
	
	private static Region createSpacer(int size) {
		Region spacer = new Region();
		spacer.setPrefWidth(size);
		return spacer;
	}
}