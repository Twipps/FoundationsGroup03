package guiAdminHomeNew;

import entityClasses.User;
import guiUserUpdate.ViewUserUpdate;
import javafx.stage.Stage;

public class ControllerAdminHomeNew {
	
	// display Admin home page
	public static void doAdminHomeNew(Stage theStage, User user) {
		guiAdminHomeNew.ViewAdminHomeNew.displayAdminHomeNew(theStage, user);
	}
	
	// log out from admin 
	public static void performLogOut(Stage theStage) {
		guiUserLogin.ViewUserLogin.displayUserLogin(theStage);
	}
}