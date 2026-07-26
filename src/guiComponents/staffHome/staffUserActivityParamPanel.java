package guiComponents.staffHome;

/***
 * @author Jchacko (Team 3) - Designed and implemented the staff user
 * activity parameter panel for selecting a student and viewing the
 * selected student's post activity.
 *
 * @version 1.0.0 - Jchacko (Team 3) - Initial implementation
 */

import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
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

        List<String> usernames = theDatabase.getUserList();

        ComboBox<String> userComboBox =
                new ComboBox<>(FXCollections.observableArrayList(usernames));

       // userComboBox.setPromptText("<Select a User>");

        Button viewActivityButton = new Button("View User Activity");

        viewActivityButton.setOnAction(event -> {

            String selectedUsername = userComboBox.getValue();

            if (selectedUsername == null || selectedUsername.equals("<Select a User>")) {
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