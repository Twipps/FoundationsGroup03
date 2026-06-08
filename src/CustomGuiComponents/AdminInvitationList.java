package CustomGuiComponents;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminInvitationList {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;

    public static ScrollPane createInvitationList(BorderPane contentPane) {
        VBox container = createInvitationContainer();
        refreshInvitations(container);
        contentPane.setRight(null); // to to remove anything existing from user page

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private static VBox createInvitationContainer() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        return container;
    }

    public static void refreshInvitations(VBox container) {
        // Pull invitations from database
    	container.getChildren().clear();
    	ResultSet resultSet = theDatabase.getInvitationCodes();

    	try {
    		while (resultSet != null && resultSet.next()) {
    			String code = resultSet.getString("code");
    			String role = resultSet.getString("role");
    			String email = resultSet.getString("emailAddress");

    			container.getChildren().add(
    				createInvitationRow(code, role, email));
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }

    private static HBox createInvitationRow(String code, String role, 
    		String email) {
        HBox row = new HBox(15);

        row.setPadding(new Insets(10));

        Label codeLabel = new Label(code);
        Label roleLabel = new Label(role);
        Label emailLabel = new Label(email);

        codeLabel.setPrefWidth(200);
        roleLabel.setPrefWidth(150);
        emailLabel.setPrefWidth(250);

        row.getChildren().addAll(
            codeLabel,
            roleLabel,
            emailLabel
        );

        return row;
    }
}