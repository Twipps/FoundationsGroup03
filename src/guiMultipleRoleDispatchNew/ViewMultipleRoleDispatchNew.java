package guiMultipleRoleDispatchNew;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: ViewMultipleRoleDispatchNew Class</p>
 *
 * <p>Description: Class that builds and displays the multiple role selection
 * page. Allows users with multiple assigned roles to choose which role to use
 * for the current session.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ViewMultipleRoleDispatchNew {
	/** Reference to the application's database. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	/**
	 * Prevents creation of ViewMultipleRoleDispatchNew objects.
	 */
	private ViewMultipleRoleDispatchNew() {
	}
	
	/**
	 * Displays the multiple role selection page.
	 *
	 * @param theStage the primary application stage
	 * @param user the authenticated user selecting a session role
	 */
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