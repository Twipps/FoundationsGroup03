package guiFirstAdminNew;

import CustomGuiComponents.CreateAccountPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: ViewFirstAdminNew Class</p>
 *
 * <p>Description: Class that builds and displays the first administrator account
 * creation page. This view is presented when no administrator account exists in
 * the system.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ViewFirstAdminNew {
	// this may not need to be passed like this, it can maybe just send a seperate value
	// normally the home pages set the role when they are logged in
	
	/** Role identifier used for the initial administrator account. */
	protected static final int theRole = 1; // automatically appoints admin
	
	/**
	 * Prevents creation of ViewFirstAdminNew objects.
	 */
	private ViewFirstAdminNew() {
	}
	
	/**
	 * Displays the first administrator account creation page.
	 *
	 * @param theStage the primary application stage
	 */
	public static void DisplayFirstAdminNew(Stage theStage) {	
		BorderPane root = new BorderPane();
		VBox welcomeBox =  
				CustomGuiComponents.CreateAccountPanel.buildCreateAccountPanel(theStage, null);
		
		root.setStyle("-fx-background-color: #9c3535;");
		
		root.setCenter(welcomeBox);
		
		Scene scene = new Scene(
				root,
				applicationMain.FoundationsMain.WINDOW_WIDTH,
				applicationMain.FoundationsMain.WINDOW_HEIGHT
			);
		
		theStage.setTitle("First User");
		theStage.setScene(scene);
		theStage.show();
	}
}

/* TODO its 3am I need to sleep
firstAdmin
inviteLogin: logical hook ups

newUserUpdate: visualHookup, could be handed the theRole

student/Instuctor placeholders
*/
