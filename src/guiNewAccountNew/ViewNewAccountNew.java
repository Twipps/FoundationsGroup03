package guiNewAccountNew;

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// gets handed CreateAccountPanel
// role is determine by invite code handed to by invite login

public class ViewNewAccountNew {
	protected static final int theRole = 0; // set to zero to cause failure if invite fails
	
	public static void DisplayNewAccountNew(Stage theStage, String inviteCode) {	
		BorderPane root = new BorderPane();
		VBox welcomeBox = 
				CustomGuiComponents.CreateAccountPanel.buildCreateAccountPanel(theStage, inviteCode);
		
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