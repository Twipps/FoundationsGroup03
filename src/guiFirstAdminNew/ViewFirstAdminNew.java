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

// gets handed CreateAccountPanel

public class ViewFirstAdminNew {
	// this may not need to be passed like this, it can maybe just send a seperate value
	// normally the home pages set the role when they are logged in
	protected static final int theRole = 1; // automatically appoints admin
	
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
