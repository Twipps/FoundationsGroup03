package guiAdminHome;

import CustomGuiComponents.adminHome.AdminUserList;
import CustomGuiComponents.generalUse.HomeNavBar;
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

/**
 * <p>Title: ViewAdminHomeNew Class</p>
 *
 * <p>Description: Class that builds and displays the administrator home page.
 * It sets up the admin navigation bar, user list, invitation controls, and
 * shared alert components.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ViewAdminHomeNew {
	/** The role value used for the administrator session. */
	protected static final int theRole = 1; // copying how the professor keeps a consistent role
	
	/** Text field used to enter an invitation email address. */
	public static TextField text_InvitationEmailAddress;
	/** Combo box used to select the invited user's role. */
	public static ComboBox<String> combobox_SelectRole;
	/** Label showing the number of outstanding invitations. */
	public static Label label_NumberOfInvitations = new Label();
	/** Alert displayed when an invitation email error occurs. */
	public static Alert alertEmailError = new Alert(Alert.AlertType.ERROR);
	/** Alert displayed when an invitation is sent successfully. */
	public static Alert alertEmailSent = new Alert(Alert.AlertType.INFORMATION);
	
	/**
	 * Prevents creation of ViewAdminHomeNew objects.
	 */
	private ViewAdminHomeNew() {
	}
	
	/**
	 * Displays the administrator home page.
	 *
	 * @param theStage the primary application stage
	 * @param user the administrator user being displayed
	 */
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

		    VBox adminNavBar = HomeNavBar.createNavigationBar(theStage, user, titleBar, contentPane, theRole);

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