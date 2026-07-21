package guiNewAccount;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: ViewNewAccountNew Class</p>
 *
 * <p>Description: Class that builds and displays the new account creation page.
 * Uses an invitation code to determine the role assigned to the new account.</p>
 *
 * @author James Suchovic (Team 03)
 */
public class ViewNewAccountNew {
	/** Role identifier used before the invitation role is resolved. */
	protected static final int theRole = 0; // set to zero to cause failure if invite fails
	
	/**
	 * Prevents creation of ViewNewAccountNew objects.
	 */
	private ViewNewAccountNew() {
	}
	
	/**
	 * Displays the new account creation page.
	 *
	 * @param theStage the primary application stage
	 * @param inviteCode the invitation code used to create the account
	 */
	public static void DisplayNewAccountNew(Stage theStage, String inviteCode) {	
		BorderPane root = new BorderPane();
		VBox welcomeBox = 
				guiComponents.generalUse.CreateAccountPanel.buildCreateAccountPanel(theStage, inviteCode);
		
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