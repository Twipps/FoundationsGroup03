package guiNewAccountSetup;
// firstAdmin or NewAccountNew
// will hand over the role information.

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import entityClasses.User;
import javafx.stage.Stage;
import javafx.geometry.Pos;

// this page will use the VBox from UserSettingsPanel in customgui components
// to reuse the logic already made.

// this is a shared gui
public class ViewNewAccountSetup {
    public static void displayNewAccountSetup(Stage theStage, User user) {
        BorderPane root = new BorderPane();

        VBox userUpdateBox =
            CustomGuiComponents.UserSettingsPanel.createSettingsPanel(theStage, user);

        Button goHome = new Button("Continue");
        
        goHome.setPrefWidth(userUpdateBox.getPrefWidth());
        goHome.setStyle(
        	    "-fx-background-color: rgba(255,255,255,0.5);"
        	);
        
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(userUpdateBox, goHome);

        root.setStyle("-fx-background-color: #9c3535;");
        root.setCenter(centerBox);
        
        goHome.setOnAction((_) -> {

            if (user.getAdminRole()) {
                guiAdminHomeNew.ControllerAdminHomeNew.doAdminHomeNew(theStage, user);
            }
            else if (user.getNewInstructor()) {
            	guiInstructorNew.ViewInstructorNew.displayInstructorHomeNew(theStage, user);
            }
            else if (user.getNewStudent()) {
            	guiStudentNew.ViewStudentNew.displayInstructorHomeNew(theStage, user);
            }

        });
        
        Scene scene = new Scene(
            root,
            applicationMain.FoundationsMain.WINDOW_WIDTH,
            applicationMain.FoundationsMain.WINDOW_HEIGHT
        );

        theStage.setTitle("User Setup");
        theStage.setScene(scene);
        theStage.show();
    }
}