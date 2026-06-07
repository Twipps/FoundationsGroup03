package guiAdminHomeNew;

import CustomGuiComponents.AdminUserList;
import CustomGuiComponents.HomeNavBar;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewAdminHomeNew {
	
	protected static final int theRole = 1; // copying how the professor keeps a consistent role
	
	public static TextField text_InvitationEmailAddress;
	public static ComboBox<String> combobox_SelectRole;
	public static Label label_NumberOfInvitations = new Label();
	public static Alert alertEmailError = new Alert(Alert.AlertType.ERROR);
	public static Alert alertEmailSent = new Alert(Alert.AlertType.INFORMATION);
	
	// declare all leaf nodes elements we new to draw the page
	 private ViewAdminHomeNew() {
		 
	 }
	
	 public static void displayAdminHomeNew(Stage theStage, User user) {
		 	
		    applicationMain.FoundationsMain.activeHomePage = theRole;
		 	
		    BorderPane root = new BorderPane();
		    BorderPane contentPane = new BorderPane();
		    BorderPane userModifyPane = new BorderPane();

		    Label titleBar = new Label("Users");
		    titleBar.setStyle(
		        "-fx-font-size: 28px;" +
		        "-fx-text-fill: #9c3535;" +
		        "-fx-font-weight: bold;"
		    );
		    
		    HBox header = new HBox(titleBar);
		    
		    header.setStyle("-fx-background-color: #FFCCCC;");
		    header.setPadding(new Insets(15)); // padding

		    contentPane.setTop(header);
		    contentPane.setCenter(AdminUserList.createUserList(userModifyPane));
		    contentPane.setRight(userModifyPane);

		    VBox adminNavBar = HomeNavBar.createNavigationBar(theStage, user, titleBar, contentPane);

		    adminNavBar.prefHeightProperty().bind(root.heightProperty());

		    root.setLeft(adminNavBar);
		    root.setCenter(contentPane);

		    Scene scene = new Scene(
		        root,
		        applicationMain.FoundationsMain.WINDOW_WIDTH,
		        applicationMain.FoundationsMain.WINDOW_HEIGHT
		    );

		    theStage.setTitle("Admin Home");
		    theStage.setScene(scene);
		    theStage.show();
		}
}