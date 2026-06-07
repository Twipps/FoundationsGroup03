package CustomGuiComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminInvitationManagementPanel {
	public static VBox createInvitationManagementPanel() {
		VBox rPanel = new VBox(15);
		
		rPanel.setPadding(new Insets(15));
		
		Label emailTFTitle = new Label("Email Address");
		TextField userEmailInput = new TextField();
		
		HBox emailInputFunctionality = createButtonRow();
		
		rPanel.getChildren().addAll(emailTFTitle, userEmailInput, emailInputFunctionality);
		
		return rPanel;
	}
	
	private static HBox createButtonRow() {
		HBox rRow = new HBox();
		
		ComboBox<String> roleSelector = new ComboBox<>();
		Region spacer = new Region();
		Button sendEmail = new Button("Send Invitation");
		
		roleSelector.setPromptText("Choose Role");
		
	    roleSelector.getItems().addAll(
	            "Student",
	            "Instructor",
	            "Administrator"
	    );
	    
	    spacer.setPrefWidth(10);
		
		rRow.getChildren().addAll(roleSelector, spacer, sendEmail);
		
		sendEmail.setOnAction((_) -> {}); // to do
		
		return rRow;
	}
}