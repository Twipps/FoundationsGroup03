package guiStudentNew;

import CustomGuiComponents.HomeNavBar;
import CustomGuiComponents.PostNavBar;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewStudentNew Class </p>
 *
 * <p> Description: The View class for the Student Home page in the Student
 * Discussion System. Builds and displays the student home page layout,
 * including the role navigation bar on the left, the page header, and
 * the post navigation panel (PostNavBar) as the main content area. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * It is called by ControllerUserLoginNew, ViewNewAccountSetup, and
 * RoleSessionSelectionPanel when a user with the Student role logs in or
 * selects the Student role from the role selection page. </p>
 *
 * <p> This class satisfies the following Students User Stories: </p>
 * <p> - REQ-03: "As a student, I can see a list of posts others have made" —
 *   the student home page loads PostNavBar as the default content, showing
 *   the full post list immediately on login. </p>
 * <p> - REQ-01/REQ-12/REQ-13: All post creation, thread filtering, and
 *   keyword search functionality is accessible from this page via PostNavBar. </p>
 *
 * <p> Note: The display method is named displayInstructorHomeNew for
 * compatibility with existing call sites in ControllerUserLoginNew,
 * ViewNewAccountSetup, and RoleSessionSelectionPanel. A future refactor
 * should rename this to displayStudentHomeNew for clarity. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented student home
 * page layout and navigation integration
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added PostNavBar as default content (REQ-03)
 *                             and User Story mappings
 */
public class ViewStudentNew {

	/** Role identifier used for student sessions.
	 *  Value 2 corresponds to the Student role in the Foundations role system. */
	protected static final int theRole = 2;

	/**
	 * Prevents creation of ViewStudentNew objects because this class only
	 * provides static GUI display methods.
	 */
	private ViewStudentNew() {}

	/*******
	 * <p> Method: displayInstructorHomeNew() </p>
	 *
	 * <p> Description: Builds and displays the Student Home page. Sets up the
	 * root BorderPane layout with the role navigation bar on the left, a
	 * styled header at the top, and the PostNavBar as the default center
	 * content so students immediately see the post list on login. </p>
	 *
	 * <p> Note: This method is named displayInstructorHomeNew for compatibility
	 * with existing call sites. It displays the Student Home page, not the
	 * Instructor Home page. </p>
	 *
	 * <p> Satisfies REQ-03: student sees the post list immediately upon
	 * navigating to the Student Home page. </p>
	 *
	 * @param theStage the primary application stage
	 * @param user     the student user whose session is being displayed
	 */
	public static void displayInstructorHomeNew(Stage theStage, User user) {
		// REQ-03: Set active home page to student role so nav bar renders correctly
		applicationMain.FoundationsMain.activeHomePage = theRole;

		BorderPane root = new BorderPane();
		BorderPane contentPane = new BorderPane();

		// Page header label
		Label titleBar = new Label("Student Home");
		titleBar.setStyle(
			"-fx-font-size: 28px;" +
			"-fx-text-fill: #9c3535;" +
			"-fx-font-weight: bold;"
		);

		HBox header = new HBox(titleBar);
		header.setStyle("-fx-background-color: #FFCCCC;");
		header.setPadding(new Insets(15));
		contentPane.setTop(header);

		// REQ-03: Load PostNavBar as the default center content so the student
		// immediately sees the full post list on login
		contentPane.setLeft(PostNavBar.createPostNavBar(theStage, contentPane));

		// Left-side role navigation bar (Home, Posts, Account Settings, Logout)
		VBox studentNavBar =
			HomeNavBar.createNavigationBar(theStage, user, titleBar, contentPane, theRole);
		studentNavBar.prefHeightProperty().bind(root.heightProperty());

		root.setLeft(studentNavBar);
		root.setCenter(contentPane);

		Scene scene = new Scene(
			root,
			applicationMain.FoundationsMain.WINDOW_WIDTH,
			applicationMain.FoundationsMain.WINDOW_HEIGHT
		);

		theStage.setTitle("Student Home");
		theStage.setScene(scene);
		theStage.show();
	}
}