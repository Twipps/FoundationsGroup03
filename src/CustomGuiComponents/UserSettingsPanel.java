// new panel to replace guiUserUpdate
package CustomGuiComponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserSettingsPanel {
	// this will get applied to the center of any user home page
	public static VBox createSettingsPanel(Stage theStage) {
		VBox settingsBox = new VBox(15);

		VBox userNameRow = new VBox(5);
		VBox passwordRow = new VBox(5);
		VBox firstNameRow = new VBox(5);
		VBox middleNameRow = new VBox(5);
		VBox lastNameRow = new VBox(5);
		VBox preferredNameRow = new VBox(5);
		VBox emailRow = new VBox(5);

		Label currentUsername = new Label("Username:");
		Label currentPassword = new Label("Password:");
		Label currentFirstName = new Label("First Name:");
		Label currentMiddleName = new Label("Middle Name:");
		Label currentLastName = new Label("Last Name:");
		Label currentPreferredName = new Label("Preferred Name:");
		Label currentEmailAddr = new Label("Email:");

		TextField newUsername = new TextField();
		TextField newPassword = new TextField();
		TextField newFirstName = new TextField();
		TextField newMiddleName = new TextField();
		TextField newLastName = new TextField();
		TextField newPreferredName = new TextField();
		TextField newEmailAddr = new TextField();

		Button updateUsername = new Button("Update");
		Button updatePassword = new Button("Update");
		Button updateFirstName = new Button("Update");
		Button updateMiddleName = new Button("Update");
		Button updateLastName = new Button("Update");
		Button updatePreferredName = new Button("Update");
		Button updateEmail = new Button("Update");

		userNameRow.getChildren().addAll(currentUsername, new HBox(10, newUsername, updateUsername));
		passwordRow.getChildren().addAll(currentPassword, new HBox(10, newPassword, updatePassword));
		firstNameRow.getChildren().addAll(currentFirstName, new HBox(10, newFirstName, updateFirstName));
		middleNameRow.getChildren().addAll(currentMiddleName, new HBox(10, newMiddleName, updateMiddleName));
		lastNameRow.getChildren().addAll(currentLastName, new HBox(10, newLastName, updateLastName));
		preferredNameRow.getChildren().addAll(currentPreferredName, new HBox(10, newPreferredName, updatePreferredName));
		emailRow.getChildren().addAll(currentEmailAddr, new HBox(10, newEmailAddr, updateEmail));
		
		settingsBox.setAlignment(Pos.BASELINE_CENTER);
		settingsBox.setPadding(new Insets(5));

		settingsBox.getChildren().addAll(
			userNameRow,
			passwordRow,
			firstNameRow,
			middleNameRow,
			lastNameRow,
			preferredNameRow,
			emailRow
		);

		return settingsBox;
	}
}