package CustomGuiComponents;

// @author James Suchovic (Team 3) - Designed and implemented account setup UI,
// navigation flow, layout structure, and functionality

import entityClasses.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RoleSessionSelectionPanel {
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
				guiAdminHomeNew.ControllerAdminHomeNew.doAdminHomeNew(theStage, user);
			} else if (inRole.equals("Instructor")) {
				guiInstructorNew.ViewInstructorNew.displayInstructorHomeNew(theStage, user);	
			} else if (inRole.equals("Student")) {
				guiStudentNew.ViewStudentNew.displayInstructorHomeNew(theStage, user);
			}
		});
		
		return rButton;
	}
}