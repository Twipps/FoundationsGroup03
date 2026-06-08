package guiMultipleRoleDispatchNew;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewMultipleRoleDispatchNew {
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	public static void displayMRDP(Stage theStage, User user) {
		BorderPane root = new BorderPane();
		VBox selectionBox = 
				CustomGuiComponents.RoleSessionSelectionPanel.
				createRoleSessionSelectionPanel(theStage, user);
		
		root.setStyle("-fx-background-color: #9c3535;");
		
		root.setCenter(selectionBox);
		
		Scene scene = new Scene(
				root,
				applicationMain.FoundationsMain.WINDOW_WIDTH,
				applicationMain.FoundationsMain.WINDOW_HEIGHT
			);
		
		theStage.setTitle("Select Session Role");
		theStage.setScene(scene);
		theStage.show();
	}
}