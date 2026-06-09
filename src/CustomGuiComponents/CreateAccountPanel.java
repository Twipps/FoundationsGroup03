package CustomGuiComponents;

import java.sql.SQLException;

import database.Database;
import entityClasses.User;
import guiNewAccount.ModelNewAccount;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utilities.InputValidator;

/*******
 * <p> Title: CreateAccountPanel Class. </p>
 * 
 * <p> Description: A shared panel used by both the first admin setup and new account
 * creation flows. Validates username and password before creating the account and
 * navigating to the account setup page. Features real-time password validation
 * feedback as the user types, including matching check against the confirm field. </p>
 * 
 * @author James Suchovic (Team 3) - Designed and implemented account setup UI,
 *  navigation flow, layout structure, and functionality
 * @author Kyle Kim (Team 3) - Added on-screen error label and real-time password validation
 * 
 * @version 1.00    Initial implementation
 * @version 1.01    2026-06-08 Added error label for UI feedback (Kyle Kim)
 * @version 1.02    2026-06-08 Added real-time password validation listener (Kyle Kim)
 * @version 1.03    2026-06-08 Fixed real-time validation to watch both password fields (Kyle Kim)
 * @version 1.04    2026-06-08 Added guard flag to prevent listener cross-firing (Kyle Kim)
 */

public class CreateAccountPanel {	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**********
	 * <p> Method: buildCreateAccountPanel() </p>
	 * 
	 * <p> Description: Builds and returns a VBox panel for account creation. Used by
	 * both the first admin setup page and the new account creation page. If inviteCode
	 * is null, the account is created as Admin. Otherwise the role is determined by
	 * the invitation code. Features real-time password strength and match feedback
	 * as the user types in either password field. A guard flag prevents the two
	 * listeners from triggering each other in a feedback loop. </p>
	 * 
	 * @param theStage   the current JavaFX stage
	 * @param inviteCode the invitation code, or null if this is the first admin setup
	 * @return a VBox containing the account creation panel
	 */
	public static VBox buildCreateAccountPanel(Stage theStage, String inviteCode) {
	    System.out.println("=== buildCreateAccountPanel called ===");  
		VBox rBox = new VBox(10);
		
		TextField text_Username = new TextField();
		PasswordField text_Password = new PasswordField();
		PasswordField text_PasswordAgain = new PasswordField();
		
		Label welc = new Label("Welcome");
		Label footer = new Label("Please setup an account");
		
		// Error label — updates in real time as the user types
		Label errorLabel = new Label("");
		errorLabel.setTextFill(Color.RED);
		errorLabel.setWrapText(true);
		errorLabel.setMaxWidth(350);
		
		text_Username.setPromptText("Enter Username");
		text_Password.setPromptText("Enter Password");
		text_PasswordAgain.setPromptText("Enter Password Again");
		
		// Guard flag — prevents the two listeners from triggering each other
		final boolean[] updating = {false};
		
		// Real-time validation on the first password field
		// Checks strength and also whether it matches the confirm field
		text_Password.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (updating[0]) return;
		    updating[0] = true;
		    
		    if (newValue.isEmpty()) {
		        errorLabel.setText("");
		        updating[0] = false;
		        return;
		    }
		    String result = ModelNewAccount.evaluatePassword(newValue);
		    if (!result.isEmpty()) {
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText(result);
		    } else {
		        // Password is strong — now check if confirm field matches
		        if (text_PasswordAgain.getText().isEmpty()) {
		            errorLabel.setTextFill(Color.GREEN);
		            errorLabel.setText("✓ Password meets all requirements");
		        } else if (text_PasswordAgain.getText().equals(newValue)) {
		            errorLabel.setTextFill(Color.GREEN);
		            errorLabel.setText("✓ Password meets all requirements");
		        } else {
		            errorLabel.setTextFill(Color.RED);
		            errorLabel.setText("Passwords do not match.");
		        }
		    }
		    updating[0] = false;
		});
		
		// Real-time validation on the confirm password field
		// Checks whether it matches the first field
		text_PasswordAgain.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (updating[0]) return;
		    updating[0] = true;
		    
		    if (newValue.isEmpty()) {
		        // Confirm cleared — revert to showing strength status of first field
		        String result = ModelNewAccount.evaluatePassword(text_Password.getText());
		        if (text_Password.getText().isEmpty()) {
		            errorLabel.setText("");
		        } else if (!result.isEmpty()) {
		            errorLabel.setTextFill(Color.RED);
		            errorLabel.setText(result);
		        } else {
		            errorLabel.setTextFill(Color.GREEN);
		            errorLabel.setText("✓ Password meets all requirements");
		        }
		        updating[0] = false;
		        return;
		    }
		    // First check strength of the first field
		    String result = ModelNewAccount.evaluatePassword(text_Password.getText());
		    if (!result.isEmpty()) {
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText(result);
		        updating[0] = false;
		        return;
		    }
		    // Strength is good — check if they match
		    if (!newValue.equals(text_Password.getText())) {
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText("Passwords do not match.");
		    } else {
		        errorLabel.setTextFill(Color.GREEN);
		        errorLabel.setText("✓ Password meets all requirements");
		    }
		    updating[0] = false;
		});
		
		Button createAccount = new Button("Create Account");
		
		createAccount.setOnAction(e -> {
		    String username = text_Username.getText().trim();
		    String password = text_Password.getText();

		    // Validate username
		    String returnString = InputValidator.verifyUsername(username);
		    if (returnString.compareTo("") != 0) {
		        text_Username.setText("");
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText(returnString);
		        return;
		    }

		    // Check passwords match
		    if (!text_Password.getText().equals(text_PasswordAgain.getText())) {
		        text_PasswordAgain.setText("");
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText("Passwords do not match.");
		        return;
		    }

		    // Validate password strength
		    returnString = ModelNewAccount.evaluatePassword(password);
		    if (returnString.compareTo("") != 0) {
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText(returnString);
		        return;
		    }

		    // Clear any previous error
		    errorLabel.setText("");

		    String role;

		    if (inviteCode == null) {
		        role = "Admin";
		    } else {
		        if (theDatabase.isInvitationExpired(inviteCode)) {
		            errorLabel.setTextFill(Color.RED);
		            errorLabel.setText(
		            		"Invitation code has expired. Please contact an admin.");
		            return;
		        }

		        role = theDatabase.getRoleGivenAnInvitationCode(inviteCode);

		        if (role == null || role.length() == 0) {
		            errorLabel.setTextFill(Color.RED);
		            errorLabel.setText("Invalid invitation code.");
		            return;
		        }
		    }

		    User newUser = null;

		    if (role.equals("Admin")) {
		        newUser = new User(username, password, "", "", "", "", "",
		        		true, false, false);
		    } else if (role.equals("Student")) {
		        newUser = new User(username, password, "", "", "", "", "",
		        		false, true, false);
		    } else if (role.equals("Instructor")) {
		        newUser = new User(username, password, "", "", "", "", "",
		        		false, false, true);
		    } else {
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText("Role does not exist: " + role);
		        return;
		    }

		    if (inviteCode != null) {
		        newUser.setEmailAddress(theDatabase.getEmailAddressUsingCode(inviteCode));
		    }

		    try {
		        theDatabase.register(newUser);
		    } catch (SQLException ex) {
		        ex.printStackTrace();
		        errorLabel.setTextFill(Color.RED);
		        errorLabel.setText("Database error. Please try again.");
		        return;
		    }

		    if (inviteCode != null) {
		        theDatabase.removeInvitationAfterUse(inviteCode);
		    }

		    theDatabase.getUserAccountDetails(username);

		    guiNewAccountSetup.ControllerNewAccountSetup.doNewAccountSetup(theStage, newUser);
		});								
								
		rBox.setStyle(
				"-fx-background-color: rgba(255,255,255,0.5);" +
				"-fx-padding: 30;" +
				"-fx-background-radius: 10;"
			);
		
		rBox.getChildren().addAll(
				welc, footer,
				text_Username, text_Password,
				text_PasswordAgain, createAccount,
				errorLabel
			);
		
		rBox.setPrefSize(380, 300);
		rBox.setMaxSize(380, 300);
		rBox.setAlignment(Pos.CENTER);
		
		return rBox;
	}
}