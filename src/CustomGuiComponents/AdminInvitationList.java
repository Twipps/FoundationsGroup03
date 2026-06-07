package CustomGuiComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminInvitationList {

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

        container.getChildren().add(
            createInvitationRow(
                "INV-123456",
                "Instructor",
                "Unused"
            )
        );
    }

    private static HBox createInvitationRow(
        String code,
        String role,
        String status
    ) {

        HBox row = new HBox(15);

        row.setPadding(new Insets(10));

        Label codeLabel =
            new Label(code);

        Label roleLabel =
            new Label(role);

        Label statusLabel =
            new Label(status);

        codeLabel.setPrefWidth(200);
        roleLabel.setPrefWidth(150);
        statusLabel.setPrefWidth(150);

        row.getChildren().addAll(
            codeLabel,
            roleLabel,
            statusLabel
        );

        return row;
    }
}