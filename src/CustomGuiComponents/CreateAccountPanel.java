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
import javafx.stage.Stage;
import utilities.InputValidator;

// aids firstAdmin and userAcct creation; the panels this gets 
// passed too will pass the role id for account creation
// invitation code indicates what roles you are.
// userLogin from invition will send a role depending on what invite
// code you havel firstAdmin page will send admin by default

// for firstAdmin and inviteAccountuNamePsswd setUp

// this is a shared panel for two gui's firstAdmin, and NewAccountNew
public class CreateAccountPanel {	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	public static VBox buildCreateAccountPanel(Stage theStage, String inviteCode) { // inviteCode is null if it comes from firstAdmin
		VBox rBox = new VBox(10);
		
		TextField text_Username = new TextField();
		PasswordField text_Password = new PasswordField();
		PasswordField text_PasswordAgain = new PasswordField();
		
		Label welc = new Label("Welcome");
		Label footer = new Label("Please setup an account");
		
		text_Username.setPromptText("Enter Username");
		text_Password.setPromptText("Enter Password");
		text_PasswordAgain.setPromptText("Enter Password Again"); 
		
		Button createAccount = new Button("Create Account");
		
		 // check if inviteCode is null or invalid, if not
		 // set this user's role to that, and pass user onto user userSetup which
		// will then pull user settings panel with userValue
		// userSetup will set the users role when a user name is chosen
		
		// using the professors flow from the old controller
		createAccount.setOnAction(e -> {
		    String username = text_Username.getText().trim();
		    String password = text_Password.getText();

		    String returnString = InputValidator.verifyUsername(username);
		    if (returnString.compareTo("") != 0) {
		        text_Username.setText("");
		        System.out.println(returnString);
		        return;
		    }

		    if (!text_Password.getText().equals(text_PasswordAgain.getText())) {
		        text_Password.setText("");
		        text_PasswordAgain.setText("");
		        System.out.println("Passwords do not match.");
		        return;
		    }

		    returnString = ModelNewAccount.evaluatePassword(password);
		    if (returnString.compareTo("") != 0) {
		        text_Password.setText("");
		        text_PasswordAgain.setText("");
		        System.out.println(returnString);
		        return;
		    }

		    String role;

		    if (inviteCode == null) {
		        role = "Admin";
		    } else {
		        if (theDatabase.isInvitationExpired(inviteCode)) {
		            System.out.println("Invitation code expired.");
		            return;
		        }

		        role = theDatabase.getRoleGivenAnInvitationCode(inviteCode);

		        if (role == null || role.length() == 0) {
		            System.out.println("Invalid invitation code.");
		            return;
		        }
		    }

		    User newUser = null;

		    if (role.equals("Admin")) {
		        newUser = new User(username, password, "", "", "", "", "", true, false, false);
		    } else if (role.equals("Student")) {
		        newUser = new User(username, password, "", "", "", "", "", false, true, false);
		    } else if (role.equals("Instructor")) {
		        newUser = new User(username, password, "", "", "", "", "", false, false, true);
		    } else {
		        System.out.println("Role does not exist: " + role);
		        return;
		    }

		    if (inviteCode != null) {
		        newUser.setEmailAddress(theDatabase.getEmailAddressUsingCode(inviteCode));
		    }

		    try {
		        theDatabase.register(newUser);
		    } catch (SQLException ex) {
		        ex.printStackTrace();
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
				text_PasswordAgain, createAccount
			);
		
		rBox.setPrefSize(250, 250);
		rBox.setMaxSize(250, 250);
		rBox.setAlignment(Pos.CENTER);
		
		return rBox;
	}
}