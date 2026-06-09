package guiMultipleRoleDispatchNew;

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

import entityClasses.User;
import javafx.stage.Stage;

public class ControllerMultipleRoleDispatchNew {
	public static void doMRDP(Stage theStage, User user) {
		guiMultipleRoleDispatchNew.ViewMultipleRoleDispatchNew.displayMRDP(theStage, user);
	}
}