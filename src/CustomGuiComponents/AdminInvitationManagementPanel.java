package CustomGuiComponents;

import guiAdminHomeNew.ControllerAdminHomeNew;
import guiAdminHomeNew.ViewAdminHomeNew;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminInvitationManagementPanel {
	public static VBox createInvitationManagementPanel() {
		VBox rPanel = new VBox(15);
		
		rPanel.setPadding(new Insets(15));
		
		Label emailTFTitle = new Label("Email Address");
		ViewAdminHomeNew.text_InvitationEmailAddress = new javafx.scene.control.TextField();
		
		HBox emailInputFunctionality = createButtonRow();
		
		rPanel.getChildren().addAll(
				emailTFTitle,
				ViewAdminHomeNew.text_InvitationEmailAddress,
				emailInputFunctionality,
				ViewAdminHomeNew.label_NumberOfInvitations
		);
		
		return rPanel;
	}
	
	private static HBox createButtonRow() {
		HBox rRow = new HBox();
		
		ViewAdminHomeNew.combobox_SelectRole = new javafx.scene.control.ComboBox<>();
		Region spacer = new Region();
		Button sendEmail = new Button("Send Invitation");
		
		ViewAdminHomeNew.combobox_SelectRole.setPromptText("Choose Role");
		
	    ViewAdminHomeNew.combobox_SelectRole.getItems().addAll(
	            "Student",
	            "Instructor",
	            "Administrator"
	    );
	    
	    spacer.setPrefWidth(10);
		
		rRow.getChildren().addAll(ViewAdminHomeNew.combobox_SelectRole, spacer, sendEmail);
		
		sendEmail.setOnAction((e) -> {
			ControllerAdminHomeNew.performInvitation();
		});
		
		return rRow;
	}
}