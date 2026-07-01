package guiUserLoginNew;

/**
 * <p>Title: ControllerUserLoginNew Class</p>
 *
 * <p>Description: Controller for the user login page. Handles login validation,
 * one-time password login, invitation-code account creation, and role-based
 * navigation after successful authentication.</p>
 *
 * @author James Suchovic (Team 03)
 * @author Kyle Kim (Team 03)
 */

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

public class ControllerUserLoginNew {

	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of ControllerUserLoginNew objects.
	 */
	private ControllerUserLoginNew() {
	}

	/**
	 * Displays the user login page.
	 *
	 * @param theStage the primary application stage
	 */
	public static void doUserLoginNew(Stage theStage) {
		ViewUserLoginNew.DisplayUserLoginNew(theStage);
	}

	protected static void doLogin(Stage theStage) {
		String username = ViewUserLoginNew.text_Username.getText().trim();
		String password = ViewUserLoginNew.text_Password.getText();
		boolean loginResult = false;

		if (theDatabase.getUserAccountDetails(username) == false) {
			ViewUserLoginNew.alertUsernamePasswordError.setContentText(
				"Incorrect username/password. Try again!"
			);
			ViewUserLoginNew.alertUsernamePasswordError.showAndWait();
			return;
		}

		String actualPassword = theDatabase.getCurrentPassword();

		// Check if the entered password matches the one-time password
		String oneTimePassword = theDatabase.getOneTimePassword(username);
		if (oneTimePassword != null && password.compareTo(oneTimePassword) == 0) {
			// User is logging in with a one-time password — clear it and force a reset
			theDatabase.clearOneTimePassword(username);

			User user = new User(
				username, password,
				theDatabase.getCurrentFirstName(),
				theDatabase.getCurrentMiddleName(),
				theDatabase.getCurrentLastName(),
				theDatabase.getCurrentPreferredFirstName(),
				theDatabase.getCurrentEmailAddress(),
				theDatabase.getCurrentAdminRole(),
				theDatabase.getCurrentNewRole1(),
				theDatabase.getCurrentNewRole2()
			);

			ViewUserLoginNew.alertUsernamePasswordError.setTitle("Password Reset Required");
			ViewUserLoginNew.alertUsernamePasswordError.setHeaderText("One-Time Password Accepted");
			ViewUserLoginNew.alertUsernamePasswordError.setContentText(
				"Your one-time password has been accepted. "
				+ "You must now set a new password on the next screen."
			);
			ViewUserLoginNew.alertUsernamePasswordError.showAndWait();

			guiNewAccountSetup.ControllerNewAccountSetup.doNewAccountSetup(theStage, user);
			return;
		}

		if (password.compareTo(actualPassword) != 0) {
			ViewUserLoginNew.alertUsernamePasswordError.setContentText(
				"Incorrect username/password. Try again!"
			);
			ViewUserLoginNew.alertUsernamePasswordError.showAndWait();
			return;
		}

		User user = new User(
			username,
			password,
			theDatabase.getCurrentFirstName(),
			theDatabase.getCurrentMiddleName(),
			theDatabase.getCurrentLastName(),
			theDatabase.getCurrentPreferredFirstName(),
			theDatabase.getCurrentEmailAddress(),
			theDatabase.getCurrentAdminRole(),
			theDatabase.getCurrentNewRole1(),
			theDatabase.getCurrentNewRole2()
		);

		int numberOfRoles = theDatabase.getNumberOfRoles(user);

		if (numberOfRoles == 1) {
			if (user.getAdminRole()) {
				loginResult = theDatabase.loginAdmin(user);
				if (loginResult) {
					guiAdminHomeNew.ViewAdminHomeNew.displayAdminHomeNew(theStage, user);
				}
			} else if (user.getNewStudent()) {
				loginResult = theDatabase.loginRole1(user);
				if (loginResult) {
					guiStudentNew.ViewStudentNew.displayStudentHomeNew(theStage, user);
				}
			} else if (user.getNewInstructor()) {
				loginResult = theDatabase.loginRole2(user);
				if (loginResult) {
					guiInstructorNew.ViewInstructorNew.displayInstructorHomeNew(theStage, user);
				}
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			guiMultipleRoleDispatchNew.ViewMultipleRoleDispatchNew.displayMRDP(theStage, user);
		}
	}

	protected static void doSetupAccount(Stage theStage, String invitationCode) {

		// Trim whitespace to prevent lookup failures from accidental spaces
		if (invitationCode != null) invitationCode = invitationCode.trim();

	    if (invitationCode == null || invitationCode.length() == 0) {
	        ViewUserLoginNew.alertUsernamePasswordError.setTitle("Missing Invitation Code");
	        ViewUserLoginNew.alertUsernamePasswordError.setHeaderText("No Invitation Code Entered");
	        ViewUserLoginNew.alertUsernamePasswordError.setContentText(
	            "Please enter an invitation code before creating an account."
	        );
	        ViewUserLoginNew.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    if (theDatabase.isInvitationExpired(invitationCode)) {
	        ViewUserLoginNew.alertUsernamePasswordError.setTitle("Invitation Code Expired");
	        ViewUserLoginNew.alertUsernamePasswordError.setHeaderText("Invitation Code Expired");
	        ViewUserLoginNew.alertUsernamePasswordError.setContentText(
	            "This invitation code has expired. Please contact an admin for a new invitation."
	        );
	        ViewUserLoginNew.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    String role = theDatabase.getRoleGivenAnInvitationCode(invitationCode);

	    if (role == null || role.length() == 0) {
	        ViewUserLoginNew.alertUsernamePasswordError.setTitle("Invalid Invitation Code");
	        ViewUserLoginNew.alertUsernamePasswordError.setHeaderText("The invitation code is not valid.");
	        ViewUserLoginNew.alertUsernamePasswordError.setContentText("Correct the code and try again.");
	        ViewUserLoginNew.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    guiNewAccountNew.ViewNewAccountNew.DisplayNewAccountNew(theStage, invitationCode);
	}
}