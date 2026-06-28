package CustomGuiComponents;

/*** 
*  @author James Suchovic (Team 3) - Designed and implemented account setup UI,
*  navigation flow, layout structure, and functionality
*  @author Kyle Kim (Team 3) - real-time password validation
*  
*  @version 1.0.1 - James Suchovic (Team 3) -  tweaked and incorporated real-time password validation
*/

import database.Database;
import entityClasses.User;
import guiNewAccountNew.ModelNewAccount;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class UserSettingsPanel {
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	// this will get applied to the center of any user home page
	public static VBox createSettingsPanel(Stage theStage, User theUser) {
		VBox settingsBox = new VBox(15);

		VBox userNameRow = new VBox(5);
		VBox passwordRow = new VBox(5);
		VBox firstNameRow = new VBox(5);
		VBox middleNameRow = new VBox(5);
		VBox lastNameRow = new VBox(5);
		VBox preferredNameRow = new VBox(5);
		VBox emailRow = new VBox(5);

		Label currentUsername = new Label("Username: " + displayValue(theUser.getUserName()));
		Label currentPassword = new Label("Password: " + displayValue(theUser.getPassword()));
		Label currentFirstName = new Label("First Name: " + displayValue(theUser.getFirstName()));
		Label currentMiddleName = new Label("Middle Name: " + displayValue(theUser.getMiddleName()));
		Label currentLastName = new Label("Last Name: " + displayValue(theUser.getLastName()));
		Label currentPreferredName = new Label("Preferred Name: " + displayValue(theUser.getPreferredFirstName()));
		Label currentEmailAddr = new Label("Email: " + displayValue(theUser.getEmailAddress()));
		Label errorLabel = new Label("");
		
		final boolean[] passwdValid = {false};

		TextField newPassword = new TextField();
		TextField newFirstName = new TextField();
		TextField newMiddleName = new TextField();
		TextField newLastName = new TextField();
		TextField newPreferredName = new TextField();
		TextField newEmailAddr = new TextField();

		Button updatePassword = new Button("Update");
		Button updateFirstName = new Button("Update");
		Button updateMiddleName = new Button("Update");
		Button updateLastName = new Button("Update");
		Button updatePreferredName = new Button("Update");
		Button updateEmail = new Button("Update");
		
		HBox passwordInput = new HBox(10, newPassword, updatePassword);
		HBox firstNameInput = new HBox(10, newFirstName, updateFirstName);
		HBox middleNameInput = new HBox(10, newMiddleName, updateMiddleName);
		HBox lastNameInput = new HBox(10, newLastName, updateLastName);
		HBox preferredNameInput = new HBox(10, newPreferredName, updatePreferredName);
		HBox emailInput = new HBox(10, newEmailAddr, updateEmail);

		passwordInput.setAlignment(Pos.CENTER);
		firstNameInput.setAlignment(Pos.CENTER);
		middleNameInput.setAlignment(Pos.CENTER);
		lastNameInput.setAlignment(Pos.CENTER);
		preferredNameInput.setAlignment(Pos.CENTER);
		emailInput.setAlignment(Pos.CENTER);
		
		updatePassword.setDisable(true);
		
		newPassword.textProperty().addListener((observable, oldValue, newValue) -> {		    
		    if (newValue.isEmpty()) {
		        errorLabel.setText("");
		        passwdValid[0] = false;
		        updatePassword.setDisable(true);
		        return;
		    }
		    String result = ModelNewAccount.evaluatePassword(newValue);
		    if (!result.isEmpty()) {
		        errorLabel.setTextFill(Color.RED);
		        updatePassword.setDisable(true);
		        errorLabel.setText(result);
		        passwdValid[0] = false;
		    } else {
		        errorLabel.setTextFill(Color.GREEN);
		        errorLabel.setText("✓ Password meets all requirements");
		        updatePassword.setDisable(false);
		        passwdValid[0] = true;
		    }
		});
		
		updatePassword.setOnAction((_) -> {
			if (newPassword.getText().trim().isEmpty()) {return;}
			theDatabase.updatePassword(theUser.getUserName(), newPassword.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setPassword(theDatabase.getCurrentPassword());
			currentPassword.setText("Password: " + displayValue(theUser.getPassword()));
			newPassword.setText("");
		});

		updateFirstName.setOnAction((_) -> {
			if (newFirstName.getText().trim().isEmpty()) {return;}
			theDatabase.updateFirstName(theUser.getUserName(), newFirstName.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setFirstName(theDatabase.getCurrentFirstName());
			currentFirstName.setText("First Name: " + displayValue(theUser.getFirstName()));
			newFirstName.setText("");
		});

		updateMiddleName.setOnAction((_) -> {
			if (newMiddleName.getText().trim().isEmpty()) {return;}
			theDatabase.updateMiddleName(theUser.getUserName(), newMiddleName.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setMiddleName(theDatabase.getCurrentMiddleName());
			currentMiddleName.setText("Middle Name: " + displayValue(theUser.getMiddleName()));
			newMiddleName.setText("");
		});

		updateLastName.setOnAction((_) -> {
			if (newLastName.getText().trim().isEmpty()) {return;}
			theDatabase.updateLastName(theUser.getUserName(), newLastName.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setLastName(theDatabase.getCurrentLastName());
			currentLastName.setText("Last Name: " + displayValue(theUser.getLastName()));
			newLastName.setText("");
		});

		updatePreferredName.setOnAction((_) -> {
			if (newPreferredName.getText().trim().isEmpty()) {return;}
			theDatabase.updatePreferredFirstName(theUser.getUserName(), newPreferredName.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setPreferredFirstName(theDatabase.getCurrentPreferredFirstName());
			currentPreferredName.setText("Preferred Name: " + displayValue(theUser.getPreferredFirstName()));
			newPreferredName.setText("");
		});

		updateEmail.setOnAction((_) -> {
			if (newEmailAddr.getText().trim().isEmpty()) {return;}
			theDatabase.updateEmailAddress(theUser.getUserName(), newEmailAddr.getText());
			theDatabase.getUserAccountDetails(theUser.getUserName());
			theUser.setEmailAddress(theDatabase.getCurrentEmailAddress());
			currentEmailAddr.setText("Email: " + displayValue(theUser.getEmailAddress()));
			newEmailAddr.setText("");
		});

		userNameRow.getChildren().addAll(currentUsername);
		passwordRow.getChildren().addAll(currentPassword, passwordInput);
		firstNameRow.getChildren().addAll(currentFirstName, firstNameInput);
		middleNameRow.getChildren().addAll(currentMiddleName, middleNameInput);
		lastNameRow.getChildren().addAll(currentLastName, lastNameInput);
		preferredNameRow.getChildren().addAll(currentPreferredName, preferredNameInput);
		emailRow.getChildren().addAll(currentEmailAddr, emailInput);
		
		settingsBox.setPrefWidth(350);
		settingsBox.setMaxWidth(350);
		
		settingsBox.setPrefHeight(450);
		settingsBox.setMaxHeight(506);
		
		settingsBox.setAlignment(Pos.CENTER);

		settingsBox.setStyle(
		    "-fx-background-color: rgba(255,255,255,0.5);" +
		    "-fx-padding: 30;" +
		    "-fx-background-radius: 10;"
		);;

		settingsBox.getChildren().addAll(
			userNameRow,
			errorLabel,
			passwordRow,
			firstNameRow,
			middleNameRow,
			lastNameRow,
			preferredNameRow,
			emailRow
		);

		return settingsBox;
	}
		
	private static String displayValue(String value) {
		if (value == null || value.length() < 1) return "<none>";
		return value;
	}
}