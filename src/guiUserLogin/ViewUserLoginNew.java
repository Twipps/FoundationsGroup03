package guiUserLogin;

/**
 * <p>Title: ViewUserLoginNew Class</p>
 *
 * <p>Description: Class that builds and displays the user login page.
 * Provides fields for user authentication and invitation-based account
 * creation.</p>
 *
 * @author James Suchovic (Team 03)
 */

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewUserLoginNew {

	/** Username input field. */
	protected static TextField text_Username = new TextField();
	/** Password input field. */
	protected static PasswordField text_Password = new PasswordField();
	/** Invitation code input field for creating a new account. */
	protected static TextField text_Invitation = new TextField();
	/** Alert used to display login and account creation errors. */
	protected static Alert alertUsernamePasswordError = new Alert(AlertType.INFORMATION);
	
	/**
	 * Prevents creation of ViewUserLoginNew objects.
	 */
	private ViewUserLoginNew() {
	}

	/**
	 * Displays the user login page.
	 *
	 * @param theStage the primary application stage
	 */
	public static void DisplayUserLoginNew(Stage theStage) {
		BorderPane root = new BorderPane();
		VBox welcomeBox = new VBox(10);

		Label welc = new Label("Welcome Back");
		Label footer = new Label("Enter Credentials to continue");
		Label noAcct = new Label("No Account?");

		Button signIn = new Button("Sign in");
		Button createAccount = new Button("Create Account");

		root.setStyle("-fx-background-color: #9c3535;");

		text_Username.setText("");
		text_Password.setText("");
		text_Invitation.setText("");

		text_Username.setPromptText("Enter Username");
		text_Password.setPromptText("Enter Password");
		text_Invitation.setPromptText("Enter invite code");

		alertUsernamePasswordError.setTitle("Invalid username/password!");
		alertUsernamePasswordError.setHeaderText(null);

		signIn.setOnAction((_) -> {
			ControllerUserLoginNew.doLogin(theStage);
		});

		createAccount.setOnAction((_) -> {
			ControllerUserLoginNew.doSetupAccount(theStage, text_Invitation.getText());
		});

		text_Username.setMaxWidth(230);
		text_Password.setMaxWidth(230);
		text_Invitation.setMaxWidth(230);
		signIn.setMaxWidth(230);
		createAccount.setMaxWidth(230);

		welcomeBox.setStyle(
			"-fx-background-color: rgba(255,255,255,0.5);" +
			"-fx-padding: 30;" +
			"-fx-background-radius: 10;"
		);

		welcomeBox.getChildren().addAll(
			welc, footer,
			text_Username, text_Password,
			signIn, noAcct,
			text_Invitation, createAccount
		);

		welcomeBox.setPrefSize(300, 300);
		welcomeBox.setMaxSize(300, 300);
		welcomeBox.setAlignment(Pos.CENTER);

		root.setCenter(welcomeBox);

		Scene scene = new Scene(
			root,
			applicationMain.FoundationsMain.WINDOW_WIDTH,
			applicationMain.FoundationsMain.WINDOW_HEIGHT
		);

		theStage.setTitle("User Login");
		theStage.setScene(scene);
		theStage.show();
	}
}