package guiComponents.adminHome;

import database.Database;
import guiAdminHome.ControllerAdminHomeNew;
import guiAdminHome.ViewAdminHomeNew;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * <p>Title: AdminInvitationManagementPanel Class</p>
 *
 * <p>Description: Class that creates the invitation management panel for the
 * administrator interface. Provides controls for selecting a role, entering an
 * email address, and sending invitation codes.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class AdminInvitationManagementPanel {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of AdminInvitationManagementPanel.
	 */
	private AdminInvitationManagementPanel() {
	}
	
	/**
	 * Creates the invitation management panel displayed in the administrator
	 * interface.
	 *
	 * @param contentPane the main content pane used by the administrator screen
	 * @return a VBox containing the invitation management controls
	 */
	public static VBox createInvitationManagementPanel(BorderPane contentPane) {
		VBox rPanel = new VBox(15);	
		rPanel.setPadding(new Insets(15));
		
		Label emailTFTitle = new Label("Email Address");
		ViewAdminHomeNew.text_InvitationEmailAddress = new javafx.scene.control.TextField();
		
		HBox emailInputFunctionality = createButtonRow(contentPane);
		
		ViewAdminHomeNew.text_InvitationEmailAddress.setText(""); // for initial page load
		ViewAdminHomeNew.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
		
		rPanel.getChildren().addAll(
				emailTFTitle,
				ViewAdminHomeNew.text_InvitationEmailAddress,
				emailInputFunctionality,
				ViewAdminHomeNew.label_NumberOfInvitations
		);
		
		return rPanel;
	}
	
	private static HBox createButtonRow(BorderPane contentPane) {
		HBox rRow = new HBox();
		
		ViewAdminHomeNew.combobox_SelectRole = new javafx.scene.control.ComboBox<>();
		Region spacer = new Region();
		Button sendEmail = new Button("Send Invitation");
		
		ViewAdminHomeNew.combobox_SelectRole.setPromptText("Choose Role");	
	    ViewAdminHomeNew.combobox_SelectRole.getItems().addAll("Student",
	    		"Instructor", "Admin");
	    
	    spacer.setPrefWidth(10);
		
		rRow.getChildren().addAll(ViewAdminHomeNew.combobox_SelectRole, spacer, sendEmail);
		
		sendEmail.setOnAction((e) -> {
			if (ViewAdminHomeNew.combobox_SelectRole.getValue() == null) { return; } // dont send it selector is empty
			ControllerAdminHomeNew.performInvitation();
			contentPane.setCenter(AdminInvitationList.createInvitationList(contentPane));
			contentPane.setRight(AdminInvitationManagementPanel.createInvitationManagementPanel(contentPane));
		});
		
		return rRow;
	}
}