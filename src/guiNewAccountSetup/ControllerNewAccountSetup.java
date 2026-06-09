package guiNewAccountSetup;

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

//firstAdmin or NewAccountNew
//will hand over the role information.

import entityClasses.User;
import javafx.stage.Stage;

// this page will use the VBox from UserSettingsPanel in customguicomponents
// to reuse the logic already made.

// this is a shared gui
public class ControllerNewAccountSetup {
	public static void doNewAccountSetup(Stage theStage, User user) {
		guiNewAccountSetup.ViewNewAccountSetup.displayNewAccountSetup(theStage, user);
		
	}
}