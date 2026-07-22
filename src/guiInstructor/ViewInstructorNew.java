package guiInstructor;

import entityClasses.User;
import guiComponents.generalUse.HomeNavBar;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: ViewInstructorNew Class</p>
 *
 * <p>Description: Class that builds and displays the instructor home page.
 * Creates the instructor interface and navigation bar for users in an
 * instructor session.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ViewInstructorNew {
	/** Role identifier used for instructor sessions. */
	protected static final int theRole = 3;
	
	/**
	 * Prevents creation of ViewInstructorNew objects.
	 */
	private ViewInstructorNew() {
	}
	
	 public static void displayInstructorHomeNew(Stage theStage, User user) {		 	
		    applicationMain.FoundationsMain.activeHomePage = theRole;
		 	
		    BorderPane root = new BorderPane();
		    BorderPane contentPane = new BorderPane();

		    Label titleBar = new Label("Instructor/Staff Home");
		    titleBar.setStyle(
		        "-fx-font-size: 28px;" +
		        "-fx-text-fill: #9c3535;" +
		        "-fx-font-weight: bold;"
		    );
		    
		    HBox header = new HBox(titleBar);
		    
		    header.setStyle("-fx-background-color: #FFCCCC;");
		    header.setPadding(new Insets(15)); // padding

		    contentPane.setTop(header);

		    VBox instructorNavBar = 
		    		HomeNavBar.createNavigationBar(theStage, user, titleBar, contentPane, theRole);

		    root.setLeft(instructorNavBar);
		    root.setCenter(contentPane);

		    Scene scene = new Scene(
		        root,
		        applicationMain.FoundationsMain.WINDOW_WIDTH,
		        applicationMain.FoundationsMain.WINDOW_HEIGHT
		    );

		    theStage.setTitle("Instructor Home");
		    theStage.setScene(scene);
		    theStage.show();
		}
}