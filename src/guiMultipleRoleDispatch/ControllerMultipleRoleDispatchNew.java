package guiMultipleRoleDispatch;



import entityClasses.User;
import javafx.stage.Stage;

/**
 * <p>Title: ControllerMultipleRoleDispatchNew Class</p>
 *
 * <p>Description: Controller for the multiple role dispatch page. Directs users
 * with multiple assigned roles to the role selection view.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ControllerMultipleRoleDispatchNew {
	/**
	 * Prevents creation of ControllerMultipleRoleDispatchNew objects.
	 */
	private ControllerMultipleRoleDispatchNew() {
	}
	
	/**
	 * Displays the multiple role selection page.
	 *
	 * @param theStage the primary application stage
	 * @param user the authenticated user selecting a session role
	 */
	public static void doMRDP(Stage theStage, User user) {
		guiMultipleRoleDispatch.ViewMultipleRoleDispatchNew.displayMRDP(theStage, user);
	}
}