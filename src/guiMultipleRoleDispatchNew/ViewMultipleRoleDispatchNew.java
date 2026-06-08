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
		// VBox thinBox = createRoleSelectionBox
		
		
		
		root.setStyle("-fx-background-color: #9c3535;");
		
		Label pickRole = new Label("Choose Session Role");
		
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