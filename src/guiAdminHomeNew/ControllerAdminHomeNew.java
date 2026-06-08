package guiAdminHomeNew;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

public class ControllerAdminHomeNew { // alot of this is from the old controller
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	// display Admin home page
	public static void doAdminHomeNew(Stage theStage, User user) {
		guiAdminHomeNew.ViewAdminHomeNew.displayAdminHomeNew(theStage, user);
	}
	
	// log out from admin 
	public static void performLogOut(Stage theStage) {
		guiUserLoginNew.ViewUserLoginNew.DisplayUserLoginNew(theStage);
	}
	
	public static void performInvitation () {
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
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHomeNew.alertEmailSent.setContentText(msg);
		ViewAdminHomeNew.alertEmailSent.showAndWait();
		
		ViewAdminHomeNew.text_InvitationEmailAddress.setText("");
		ViewAdminHomeNew.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHomeNew.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHomeNew.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
}