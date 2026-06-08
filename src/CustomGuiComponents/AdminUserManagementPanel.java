package CustomGuiComponents;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminUserManagementPanel {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	// Given the user that's selected from the scroll panel
	public static VBox createUserManagementPanel(String username, String fullName, 
    	String email,String roles, Runnable refreshUsers) { // runnable brings back memories

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

	    setupRoleBoxes(username, roleSelectorAdd, roleSelectorRemove);

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
	    
	    add.setOnAction((_) -> {
	    	String role = roleSelectorAdd.getValue();
	    	if (role != null && role.compareTo("Choose Role") != 0) {
	    		if (theDatabase.updateUserRole(username, role, "true")) {
	    			setupRoleBoxes(username, roleSelectorAdd, roleSelectorRemove);
	    		}
	    		refreshUsers.run();
	    	}
	    });
	    
	    remove.setOnAction((_) -> {
	    	String role = roleSelectorRemove.getValue();
	    	if (role != null && role.compareTo("Choose Role") != 0) {
	    		if (theDatabase.updateUserRole(username, role, "false")) {
	    			setupRoleBoxes(username, roleSelectorAdd, roleSelectorRemove);
	    		}
	    		refreshUsers.run();
	    	}
	    });
	    
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
	
	private static void setupRoleBoxes(String username, ComboBox<String> roleSelectorAdd,
			ComboBox<String> roleSelectorRemove) {
		
		theDatabase.getUserAccountDetails(username);
		
		roleSelectorAdd.getItems().clear();
		roleSelectorRemove.getItems().clear();
		
		roleSelectorAdd.getItems().add("Choose Role");
		roleSelectorRemove.getItems().add("Choose Role");
		
		if (!theDatabase.getCurrentAdminRole())
			roleSelectorAdd.getItems().add("Admin");
		if (!theDatabase.getCurrentNewRole1())
			roleSelectorAdd.getItems().add("Student");
		if (!theDatabase.getCurrentNewRole2())
			roleSelectorAdd.getItems().add("Instructor");

		if (theDatabase.getCurrentAdminRole())
			roleSelectorRemove.getItems().add("Admin");
		if (theDatabase.getCurrentNewRole1())
			roleSelectorRemove.getItems().add("Student");
		if (theDatabase.getCurrentNewRole2())
			roleSelectorRemove.getItems().add("Instructor");
		
		roleSelectorAdd.getSelectionModel().select(0);
		roleSelectorRemove.getSelectionModel().select(0);
	}
	
	private static Region createSpacer(int size) {
		Region spacer = new Region();
		spacer.setPrefWidth(size);
		return spacer;
	}
}