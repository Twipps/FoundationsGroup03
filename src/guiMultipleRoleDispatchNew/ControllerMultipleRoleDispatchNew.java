package guiMultipleRoleDispatchNew;

import entityClasses.User;
import javafx.stage.Stage;

public class ControllerMultipleRoleDispatchNew {
	public static void doMRDP(Stage theStage, User user) {
		guiMultipleRoleDispatchNew.ViewMultipleRoleDispatchNew.displayMRDP(theStage, user);
	}
}