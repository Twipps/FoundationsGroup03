package CustomGuiComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminUserList {

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

    public static void refreshUsers(VBox container, BorderPane userModifyPane) {
        // Pull users from database
    	container.getChildren().add(
    		    createUserRow(
    		        "jsuchovic",
    		        "James Suchovic",
    		        "jsuchovic@gmail.com",
    		        "Administrator",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "asmith",
    		        "Alice Smith",
    		        "alice.smith@email.com",
    		        "Instructor",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "bjohnson",
    		        "Brian Johnson",
    		        "brian.johnson@email.com",
    		        "Student",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "cwilson",
    		        "Chloe Wilson",
    		        "chloe.wilson@email.com",
    		        "Staff",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "dlee",
    		        "David Lee",
    		        "david.lee@email.com",
    		        "Student",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "egarcia",
    		        "Emma Garcia",
    		        "emma.garcia@email.com",
    		        "Instructor",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "fmartin",
    		        "Frank Martin",
    		        "frank.martin@email.com",
    		        "Staff",
    		        userModifyPane
    		    )
    		);

    		container.getChildren().add(
    		    createUserRow(
    		        "hnguyen",
    		        "Hannah Nguyen",
    		        "hannah.nguyen@email.com",
    		        "Student",
    		        userModifyPane
    		    )
    		);
        
    }

    private static HBox createUserRow(String username, String fullName, 
    		String email,String roles, BorderPane userModifyPane ) {

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
                    roles
                )
            );
        });

        return row;
    }
}