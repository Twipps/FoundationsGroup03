package CustomGuiComponents;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

// aids firstAdmin and userAcct creation; the panels this gets 
// passed too will pass the role id for account creation
// invitation code indicates what roles you are.
// userLogin from invition will send a role depending on what invite
// code you havel firstAdmin page will send admin by default

// for firstAdmin and inviteAccountuNamePsswd setUp

// this is a shared panel for two gui's firstAdmin, and NewAccountNew
public class CreateAccountPanel {	
	public static VBox buildCreateAccountPanel(final int theRole) { 
		VBox rBox = new VBox(10);
		
		TextField text_Username = new TextField();
		PasswordField text_Password = new PasswordField();
		PasswordField text_PasswordAgain = new PasswordField();
		
		Label welc = new Label("Welcome");
		Label footer = new Label("Please setup an account");
		
		text_Username.setPromptText("Enter Username");
		text_Password.setPromptText("Enter Password");
		text_PasswordAgain.setPromptText("Enter Password Again"); 
		
		Button createAccount = new Button("Create Account");
		
		createAccount.setOnAction((_) -> {
			// this button will pass role (theRole) //TODO LINK THIS TO NEWACCOUTSETUP
		});											//TODO ADD CHECK LOGIC FOR PASSWD
													
		rBox.setStyle(
				"-fx-background-color: rgba(255,255,255,0.5);" +
				"-fx-padding: 30;" +
				"-fx-background-radius: 10;"
			);
		
		rBox.getChildren().addAll(
				welc, footer,
				text_Username, text_Password,
				text_PasswordAgain, createAccount
			);
		
		rBox.setPrefSize(250, 250);
		rBox.setMaxSize(250, 250);
		rBox.setAlignment(Pos.CENTER);
		
		return rBox;
	}
}