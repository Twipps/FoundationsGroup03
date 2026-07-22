package guiNewAccountSetup;

/**
 * <p>Title: ControllerNewAccountSetup Class</p>
 *
 * <p>Description: Controller for the new account setup page. Directs newly
 * created users to complete their account information after registration.</p>
 *
 * @author James Suchovic (Team 03)
 */

import entityClasses.User;
import javafx.stage.Stage;

// this page will use the VBox from UserSettingsPanel in customguicomponents
// to reuse the logic already made.

// this is a shared gui
public class ControllerNewAccountSetup {
	/**
	 * Prevents creation of ControllerNewAccountSetup objects.
	 */
	private ControllerNewAccountSetup() {
	}
	
	/**
	 * Displays the new account setup page.
	 *
	 * @param theStage the primary application stage
	 * @param user the newly created user completing account setup
	 */
	public static void doNewAccountSetup(Stage theStage, User user) {
		guiNewAccountSetup.ViewNewAccountSetup.displayNewAccountSetup(theStage, user);
		
	}
}