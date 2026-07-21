package CustomGuiComponents.adminHome;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/*******
 * <p> Title: AdminUserManagementPanel Class. </p>
 * 
 * <p> Description: A custom JavaFX component that provides admin controls for managing
 * a selected user. Allows the admin to set a one-time password, add/remove roles,
 * and delete the user account with confirmation. </p>
 * 
 * @author James Suchovic (Team 3) - Designed and implemented account setup UI,
 *                                   navigation flow, layout structure, and functionality
 * @author Kyle Kim (Team 3) - Wired deleteUser and setOneTimePassword logic                                  
 *                                   
 * @version 1.01    2026-06-08 Added deleteUser and setOneTimePassword functionality (Kyle Kim)
 */
public class AdminUserManagementPanel {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of AdminUserManagementPanel objects.
	 */
	private AdminUserManagementPanel() {
	}
	
	/**********
	 * <p> Method: createUserManagementPanel() </p>
	 * 
	 * <p> Description: Creates a VBox panel with controls for managing a selected user.
	 * Includes buttons for setting a one-time password, adding/removing roles, and
	 * deleting the user account. The delete button shows an "Are you sure?" confirmation
	 * before proceeding. </p>
	 * 
	 * @param username the username of the selected user
	 * @param fullName the full name of the selected user
	 * @param email the email address of the selected user
	 * @param roles the current roles of the selected user
	 * @param refreshUsers a Runnable that refreshes the user list after changes
	 * @return a VBox containing the management panel
	 */
	public static VBox createUserManagementPanel(String username, String fullName, 
    	String email, String roles, Runnable refreshUsers) {

	    VBox rBox = new VBox(15);
	    rBox.setPadding(new Insets(20));
	    
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
	    
	    // Set One-Time Password — generates a random temp password, stores it in the
	    // database, and displays it to the admin so they can communicate it to the user
	    setOneTime.setOnAction((_) -> {
	    	String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
	    	theDatabase.setOneTimePassword(username, tempPassword);
	    	Alert alert = new Alert(AlertType.INFORMATION);
	    	alert.setTitle("One-Time Password Set");
	    	alert.setHeaderText("Temporary password for: " + username);
	    	alert.setContentText("One-time password: " + tempPassword
	    			+ "\nCommunicate this to the user."
	    			+ "\nIt will be cleared after their first login.");
	    	alert.showAndWait();
	    });
	    
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
	    
	    // Show confirmation dialog before deleting
	    delete.setOnAction((_) -> {yesNoWithPrompt.setVisible(true);});
	    
	    // Confirmed — delete the user and refresh the list
	    yes.setOnAction((_) -> {
	    	yesNoWithPrompt.setVisible(false);
	    	theDatabase.deleteUser(username);
	    	refreshUsers.run();
	    });
	    
	    // Cancelled — just hide the confirmation
	    no.setOnAction((_) -> {yesNoWithPrompt.setVisible(false);});

	    rBox.getChildren().addAll(userSelection, setOneTime, 
	    		addRow, removeRow, deleteRow);

	    return rBox;
	}

	private static HBox addRemove(String labelText, ComboBox<String> comboBox, Button button) {
	    HBox row = new HBox(10);
	    row.setAlignment(Pos.CENTER_LEFT);
	    row.getChildren().addAll(comboBox, button);
	    return row;
	}
	
	/**********
	 * <p> Method: setupRoleBoxes() </p>
	 * 
	 * <p> Description: Populates the add/remove role combo boxes based on the user's
	 * current roles. Roles the user already has appear in the remove box; roles they
	 * don't have appear in the add box. </p>
	 */
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