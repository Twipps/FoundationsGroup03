package guiAdminHome;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerAdminHomeNew Class. </p>
 * 
 * <p> Description: Controller for the new Admin Home Page. Handles invitation sending,
 * email validation, logout, and navigation. User management actions (delete, one-time
 * password, add/remove roles) are handled directly in AdminUserManagementPanel. </p>
 * 
 * @author James Suchovic (Team 3) - Designed and implemented account setup UI, 
 * navigation flow, layout structure, and functionality
 * @author Kyle Kim (Team 3) - Improved email validation
 * 
 * @version 1.00    Initial implementation
 * @version 1.01    2026-06-08 Improved invalidEmailAddress with format validation (Kyle Kim)
 */
public class ControllerAdminHomeNew {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of ControllerAdminHomeNew objects
	 */
	private ControllerAdminHomeNew() {
	}
	
	/**
	 * <p> Method: doAdminHomeNew() </p>
	 * <p> Description: Navigates to the new Admin Home page. </p>
	 *
	 * @param theStage the primary application stage
	 * @param user the admin user being displayed
	 */
	public static void doAdminHomeNew(Stage theStage, User user) {
		guiAdminHome.ViewAdminHomeNew.displayAdminHomeNew(theStage, user);
	}
	
	/**
	 * <p> Method: performLogOut() </p>
	 * <p> Description: Logs the admin out and returns to the login page. </p>
	 *
	 * @param theStage the primary application stage
	 */
	public static void performLogOut(Stage theStage) {
		guiUserLogin.ViewUserLoginNew.DisplayUserLoginNew(theStage);
	}
	
	/**********
	 * <p> Method: performInvitation() </p>
	 * <p> Description: Sends an invitation to the specified email address with the
	 * selected role. Validates the email address before proceeding. </p>
	 */
	public static void performInvitation() {
		String emailAddress = ViewAdminHomeNew.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return;
		}
		
		String theSelectedRole = (String) ViewAdminHomeNew.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress, theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHomeNew.alertEmailSent.setContentText(msg);
		ViewAdminHomeNew.alertEmailSent.showAndWait();
		
		ViewAdminHomeNew.text_InvitationEmailAddress.setText("");
		ViewAdminHomeNew.label_NumberOfInvitations.setText(
				"Number of outstanding invitations: " + theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> Method: invalidEmailAddress() </p>
	 * 
	 * <p> Description: Validates an email address before it is used. Checks that the
	 * address does not exceed the maximum length, is not empty, contains exactly one '@'
	 * character, and has a valid domain with at least one '.' that is not at the start
	 * or end of the domain. </p>
	 * 
	 * @param emailAddress the email address string to validate
	 * @return true if the email address is invalid, false if it is valid
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		// Check max length before anything else (prevents crash-based attacks)
		if (emailAddress.length() > 254) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"The email address is too long. Maximum length is 254 characters.");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that the field is not empty
		if (emailAddress.length() == 0) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that there is exactly one '@' and it is not at position 0
		int atIndex = emailAddress.indexOf('@');
		if (atIndex <= 0 || atIndex != emailAddress.lastIndexOf('@')) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"The email address must contain exactly one '@' character.");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that the domain part contains at least one '.'
		// and that '.' is not the first or last character of the domain
		String domain = emailAddress.substring(atIndex + 1);
		if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"The email address must have a valid domain (e.g. example.com).");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return true;
		}
		
		return false;
	}
}