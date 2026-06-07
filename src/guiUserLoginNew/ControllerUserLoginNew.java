package guiUserLoginNew;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;

public class ControllerUserLoginNew {

	private static Database theDatabase = applicationMain.FoundationsMain.database;

	public static void doUserLoginNew(Stage theStage) {
		ViewUserLoginNew.DisplayUserLoginNew(theStage);
	}

	protected static void doLogin(Stage theStage) {
		String username = ViewUserLoginNew.text_Username.getText();
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
					guiStudent.ViewStudentHome.displayRole1Home(theStage, user);
				}
			} else if (user.getNewInstructor()) {
				loginResult = theDatabase.loginRole2(user);
				if (loginResult) {
					guiInstructor.ViewInstructorHome.displayRole2Home(theStage, user);
				}
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch
				.displayMultipleRoleDispatch(theStage, user);
		}
	}

	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}
}