package guiAdminHome;

import database.Database;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  When those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Kyle Kim (Team 3) - Implemented listUsers, improved invalidEmailAddress
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation
 * @version 1.02		2026-06-06 Implemented listUsers and email validation (Kyle Kim, Team 3)
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home page status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void setOnetimePassword () {
		System.out.println("\n*** WARNING ***: One-Time Password Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("One-Time Password Issue");
		ViewAdminHome.alertNotImplemented.setContentText("One-Time Password Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. Requires UI changes from James before
	 * full implementation can proceed. </p>
	 */
	protected static void deleteUser() {
		System.out.println("\n*** WARNING ***: Delete User Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Delete User Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Delete User Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that lists all users currently in the system.
	 * For each user, the list displays the username, full name, email address, and the
	 * roles that user plays. </p>
	 */
	protected static void listUsers() {
		// Build a formatted string of all users in the system
		StringBuilder userList = new StringBuilder();
		
		for (String username : theDatabase.getUserList()) {
			// Skip the placeholder entry at the top of the list
			if (username.equals("<Select a User>")) continue;
			
			// Fetch full details for this user
			theDatabase.getUserAccountDetails(username);
			
			// Build the name string
			String firstName = theDatabase.getCurrentFirstName();
			String lastName = theDatabase.getCurrentLastName();
			String fullName = (firstName + " " + lastName).trim();
			if (fullName.isEmpty()) fullName = "(no name set)";
			
			// Get email
			String email = theDatabase.getCurrentEmailAddress();
			if (email == null || email.isEmpty()) email = "(no email set)";
			
			// Build roles string
			String roles = "";
			if (theDatabase.getCurrentAdminRole()) roles += "Admin ";
			if (theDatabase.getCurrentNewRole1()) roles += "Student ";
			if (theDatabase.getCurrentNewRole2()) roles += "Instructor ";
			if (roles.isEmpty()) roles = "(no roles)";
			
			// Append this user's info as one line
			userList.append("User: ").append(username)
					.append("\n  Name:  ").append(fullName)
					.append("\n  Email: ").append(email)
					.append("\n  Roles: ").append(roles.trim())
					.append("\n\n");
		}
		
		// Display the list in an alert dialog
		ViewAdminHome.alertNotImplemented.setTitle("User List");
		ViewAdminHome.alertNotImplemented.setHeaderText("All Users in the System " +
				"(" + theDatabase.getNumberOfUsers() + " total)");
		ViewAdminHome.alertNotImplemented.setContentText(
				userList.length() == 0 ? "No users found." : userList.toString());
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system. </p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that validates an email address before it is used.
	 * Checks that the address does not exceed the maximum length, is not empty, contains
	 * exactly one '@' character, and has a valid domain with at least one '.' that is not
	 * at the start or end of the domain. </p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 * @return true if the email address is invalid, false if it is valid
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		// Check max length before anything else (prevents crash-based attacks)
		if (emailAddress.length() > 254) {
			ViewAdminHome.alertEmailError.setContentText(
					"The email address is too long. Maximum length is 254 characters.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that the field is not empty
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that there is exactly one '@' character and it is not at position 0
		int atIndex = emailAddress.indexOf('@');
		if (atIndex <= 0 || atIndex != emailAddress.lastIndexOf('@')) {
			ViewAdminHome.alertEmailError.setContentText(
					"The email address must contain exactly one '@' character.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		// Check that the domain part (after '@') contains at least one '.'
		// and that '.' is not the first or last character of the domain
		String domain = emailAddress.substring(atIndex + 1);
		if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
			ViewAdminHome.alertEmailError.setContentText(
					"The email address must have a valid domain (e.g. example.com).");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		
		// Email address passed all checks
		return false;
	}
	
	/**********
	 * <p> Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}