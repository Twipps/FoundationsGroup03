package guiComponents.generalUse;

import applicationMain.FoundationsMain;
import entityClasses.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: RoleSessionSelectionPanel Class</p>
 *
 * <p>Description: Class that creates the role selection panel displayed when a
 * user has multiple available roles. Allows the user to choose which role to
 * use for the current session.</p>
 *
 * @author James Suchovic (Team 03)
 */


public class RoleSessionSelectionPanel {
	/**
	 * Prevents creation of RoleSessionSelectionPanel objects
	 */
	private RoleSessionSelectionPanel() {
	}
	
	/**
	 * Creates the role selection panel for users with multiple roles.
	 *
	 * @param theStage the primary application stage
	 * @param user the authenticated user whose available roles are displayed
	 * @return a VBox containing the available role selection buttons
	 */
	public static VBox createRoleSessionSelectionPanel(Stage theStage, User user) {
		VBox rBox = new VBox(10);
		
		Label pickRole = new Label("Choose Session Role");
		
		rBox.getChildren().add(pickRole);
		
		// not gonna be a loop just add if block for new roles
		if (user.getAdminRole()) {
			rBox.getChildren().add(createRoleButton(theStage, "Admin", user));
		}
		if (user.getNewInstructor()) {
			rBox.getChildren().add(createRoleButton(theStage, "Instructor", user));
		}
		if (user.getNewStudent()) {
			rBox.getChildren().add(createRoleButton(theStage, "Student", user));
		}	
		
		rBox.setStyle(
				"-fx-background-color: rgba(255,255,255,0.5);" +
				"-fx-padding: 30;" +
				"-fx-background-radius: 10;"
			);
		
		rBox.setPrefSize(200, 200);
		rBox.setMaxSize(200, 200);
		rBox.setAlignment(Pos.CENTER);

		return rBox;
	}
	
	private static Button createRoleButton(Stage theStage, String inRole, User user) {
		// generates from given user roles
		Button rButton = new Button(inRole);
		
		rButton.setOnAction((_) -> {
			if (inRole.equals("Admin")) {
				FoundationsMain.activeRole = 1;
				guiAdminHome.ControllerAdminHomeNew.doAdminHomeNew(theStage, user);
			} else if (inRole.equals("Instructor")) {
				FoundationsMain.activeRole = 3;
				guiInstructor.ViewInstructorNew.displayInstructorHomeNew(theStage, user);	
			} else if (inRole.equals("Student")) {
				FoundationsMain.activeRole = 2;
				guiStudent.ViewStudentNew.displayStudentHomeNew(theStage, user);
			}
		});
		
		return rButton;
	}
}