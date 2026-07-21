package CustomGuiComponents.adminHome;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * <p>Title: AdminInvitationList Class</p>
 *
 * <p>Description: Class that builds and refreshes the invitation code list shown
 * in the admin user interface.</p>
 *
 * @author James Suchovic (Team 03)
 * @version 1.01 added expiration date to invite list
 */

public class AdminInvitationList {
		
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of AdminInvitationList objects.
	 */
	private AdminInvitationList() {
	}

	/**
	 * Creates the scrollable invitation list panel for the admin interface.
	 *
	 * @param contentPane the main content pane used by the admin screen
	 * @return a ScrollPane containing the current invitation list
	 */
    public static ScrollPane createInvitationList(BorderPane contentPane) {
        VBox container = createInvitationContainer();
        refreshInvitations(container);
        contentPane.setRight(null);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private static VBox createInvitationContainer() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        return container;
    }

    /**
     * Reloads the displayed invitation codes from the database.
     *
     * @param container the VBox that will contain the invitation rows
     */
    public static void refreshInvitations(VBox container) {
    	container.getChildren().clear();
    	ResultSet resultSet = theDatabase.getInvitationCodes();

    	try {
    		while (resultSet != null && resultSet.next()) {
    			String code = resultSet.getString("code");
    			String role = resultSet.getString("role");
    			String email = resultSet.getString("emailAddress");

    			String expiryDate = resultSet.getString("expiryDate");
    			if (expiryDate == null) {
    				expiryDate = "Never";
    			}

    			container.getChildren().add(
    				createInvitationRow(code, role, email, expiryDate));
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }

    private static HBox createInvitationRow(String code, String role,
    		String email, String expiryDate) {
        HBox row = new HBox(15);

        row.setPadding(new Insets(10));

        Label codeLabel = new Label(code);
        Label roleLabel = new Label(role);
        Label emailLabel = new Label(email);
        Label expiryLabel = new Label(expiryDate);

        codeLabel.setPrefWidth(200);
        roleLabel.setPrefWidth(150);
        emailLabel.setPrefWidth(250);
        expiryLabel.setPrefWidth(250);

        row.getChildren().addAll(
            codeLabel,
            roleLabel,
            emailLabel,
            expiryLabel
        );

        return row;
    }
}