package guiStudentNew;

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

import CustomGuiComponents.HomeNavBar;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewStudentNew {
	protected static final int theRole = 2;
	
	 public static void displayInstructorHomeNew(Stage theStage, User user) {		 	
		    applicationMain.FoundationsMain.activeHomePage = theRole;
		 	
		    BorderPane root = new BorderPane();
		    BorderPane contentPane = new BorderPane();

		    Label titleBar = new Label("Student Home");
		    titleBar.setStyle(
		        "-fx-font-size: 28px;" +
		        "-fx-text-fill: #9c3535;" +
		        "-fx-font-weight: bold;"
		    );
		    
		    HBox header = new HBox(titleBar);
		    
		    header.setStyle("-fx-background-color: #FFCCCC;");
		    header.setPadding(new Insets(15)); // padding

		    contentPane.setTop(header);

		    VBox studentNavBar = 
		    		HomeNavBar.createNavigationBar(theStage, user, titleBar, contentPane, theRole);

		    studentNavBar.prefHeightProperty().bind(root.heightProperty());

		    root.setLeft(studentNavBar);
		    root.setCenter(contentPane);

		    Scene scene = new Scene(
		        root,
		        applicationMain.FoundationsMain.WINDOW_WIDTH,
		        applicationMain.FoundationsMain.WINDOW_HEIGHT
		    );

		    theStage.setTitle("Student Home");
		    theStage.setScene(scene);
		    theStage.show();
		}
}