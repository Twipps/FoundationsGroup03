package CustomGuiComponents;

import entityClasses.User;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class RoleSessionSelectionPanel {
	// this may be too much effort for a visual thing
	public static VBox createRoleSessionSelectionPanel(User user) {
		VBox rBox = new VBox();
		
		// some loop from user roles {
		rBox.getChildren().addAll(
				
				);
		// }
		
		return rBox;
	}
	
	private static Button createRoleButton(int currentRole) {
		// generates from given user roles
		Button rButton = new Button();
		
		rButton.setOnAction((_) -> {
			// set to launch to one of the home pages depending on the given roles
			}
		);
		
		return rButton;
	}
}