package CustomGuiComponents;

import java.util.List;

import database.Database;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * <p>Title: AdminUserList Class</p>
 *
 * <p>Description: Class that builds and refreshes the list of user accounts
 * displayed in the administrator interface.</p>
 *
 * @author James Suchovic (Team 03)
 */


public class AdminUserList {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of AdminUserList objects.
	 */
	private AdminUserList() {
	}

	/**
	 * Creates the scrollable user list displayed in the administrator interface.
	 *
	 * @param userModifyPane the main content pane used by the administrator screen
	 * @return a ScrollPane containing the current list of users
	 */
    public static ScrollPane createUserList(BorderPane userModifyPane) {
        VBox container = createUserContainer();
        refreshUsers(container, userModifyPane);
        userModifyPane.setRight(null); // refresh

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private static VBox createUserContainer() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        return container;
    }

    /**
     * Reloads the displayed list of users from the database.
     *
     * @param container the VBox that will contain the user rows
     * @param userModifyPane the pane used to display user management controls
     */
    public static void refreshUsers(VBox container, BorderPane userModifyPane) {
        List<User> allUsers = theDatabase.getAllUsers();

        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);

            container.getChildren().add(
                createUserRow(
                    user.getUserName(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getEmailAddress(), getRoles(user), container,
                    userModifyPane
                )
            );
        }
    }
    
    private static String getRoles(User user) {
        String roles = "";

        if (user.getAdminRole()) {
            roles += "Admin";
        }

        if (user.getNewStudent()) {
            if (!roles.isEmpty()) {
                roles += ", ";
            }
            roles += "Student";
        }

        if (user.getNewInstructor()) {
            if (!roles.isEmpty()) {
                roles += ", ";
            }
            roles += "Instructor";
        }

        return roles;
    }

    private static HBox createUserRow(String username, String fullName, 
    		String email,String roles, VBox container, BorderPane userModifyPane ) {

        HBox row = new HBox(15);

        row.setPadding(new Insets(10));

        Label usernameLabel = new Label(username);
        Label fullNameLabel = new Label(fullName);
        Label emailLabel = new Label(email);
        Label roleLabel = new Label(roles);

        usernameLabel.setPrefWidth(120);
        fullNameLabel.setPrefWidth(200);
        emailLabel.setPrefWidth(250);
        roleLabel.setPrefWidth(150);

        row.getChildren().addAll(
            usernameLabel,
            fullNameLabel,
            emailLabel,
            roleLabel
        );
        
        row.setOnMouseClicked(e -> {
            userModifyPane.setRight(
                AdminUserManagementPanel.createUserManagementPanel(
                    username,
                    fullName,
                    email,
                    roles,
                    () -> {
                        container.getChildren().clear();
                        refreshUsers(container, userModifyPane);
                    }
                )
            );
        });

        return row;
    }
}