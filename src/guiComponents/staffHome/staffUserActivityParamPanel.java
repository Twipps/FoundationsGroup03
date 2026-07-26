package guiComponents.staffHome;

/***
 * @author Jchacko (Team 3) - Designed and implemented the staff user
 * activity parameter panel for selecting a student and viewing the
 * selected student's post activity.
 *
 * @version 1.0.0 - Jchacko (Team 3) - Initial implementation
 */

import java.util.ArrayList;
import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.User;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class staffUserActivityParamPanel {

    private static final Database theDatabase = FoundationsMain.database;

    public static VBox createUserActivityParamPanel(Stage theStage) {

        VBox panel = new VBox(15);

        Label titleLabel = new Label("Select a student");
        Label messageLabel = new Label("");

        // TP3: Filter to students only (newRole1) — previously listed every
        // user including staff/admin, which didn't make sense for a
        // student-activity audit tool
        List<User> allUsers = theDatabase.getAllUsers();
        List<String> studentUsernames = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getNewStudent()) {
                studentUsernames.add(user.getUserName());
            }
        }

        ComboBox<String> userComboBox =
                new ComboBox<>(FXCollections.observableArrayList(studentUsernames));

        // Note: getAllUsers() does not prepend a placeholder item (unlike
        // getUserList()), so setPromptText() alone is sufficient here —
        // no duplicate "<Select a User>" entry will appear in the dropdown
        userComboBox.setPromptText("<Select a Student>");

        Button viewActivityButton = new Button("View User Activity");

        viewActivityButton.setOnAction(event -> {

            String selectedUsername = userComboBox.getValue();

            if (selectedUsername == null) {
                messageLabel.setText("Please select a user.");
                return;
            }

            Stage auditStage = new Stage();

            VBox auditPanel =
                    staffUserActivityAuditPanel.createUserActivityAuditPanel(selectedUsername);

            Scene scene = new Scene(auditPanel, 700, 500);

            auditStage.setTitle("User Activity Audit");
            auditStage.setScene(scene);
            auditStage.show();
        });

        panel.getChildren().addAll(
                titleLabel,
                userComboBox,
                viewActivityButton,
                messageLabel
        );

        return panel;
    }
}