package guiNewAccountSetup;
// firstAdmin or NewAccountNew
// will hand over the role information.

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import entityClasses.User;
import javafx.stage.Stage;
import javafx.geometry.Pos;

/**
 * <p>Title: ViewNewAccountSetup Class</p>
 *
 * <p>Description: Class that builds and displays the new account setup page.
 * Allows users to complete their account information before continuing to their
 * assigned home page.</p>
 *
 * @author James Suchovic (Team 03)
 */
public class ViewNewAccountSetup {
	/**
	 * Prevents creation of ViewNewAccountSetup objects.
	 */
	private ViewNewAccountSetup() {
	}
	
	/**
	 * Displays the new account setup page.
	 *
	 * @param theStage the primary application stage
	 * @param user the user completing account setup
	 */
    public static void displayNewAccountSetup(Stage theStage, User user) {
        BorderPane root = new BorderPane();

        VBox userUpdateBox =
            CustomGuiComponents.UserSettingsPanel.createSettingsPanel(theStage, user);

        Button goHome = new Button("Continue");
        
        goHome.setPrefWidth(userUpdateBox.getPrefWidth());
        goHome.setStyle(
        	    "-fx-background-color: rgba(255,255,255,0.5);"
        	);
        
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(userUpdateBox, goHome);

        root.setStyle("-fx-background-color: #9c3535;");
        root.setCenter(centerBox);
        
        goHome.setOnAction((_) -> {

            if (user.getAdminRole()) {
                guiAdminHomeNew.ControllerAdminHomeNew.doAdminHomeNew(theStage, user);
            }
            else if (user.getNewInstructor()) {
            	guiInstructorNew.ViewInstructorNew.displayInstructorHomeNew(theStage, user);
            }
            else if (user.getNewStudent()) {
            	guiStudentNew.ViewStudentNew.displayInstructorHomeNew(theStage, user);
            }

        });
        
        Scene scene = new Scene(
            root,
            applicationMain.FoundationsMain.WINDOW_WIDTH,
            applicationMain.FoundationsMain.WINDOW_HEIGHT
        );

        theStage.setTitle("User Setup");
        theStage.setScene(scene);
        theStage.show();
    }
}