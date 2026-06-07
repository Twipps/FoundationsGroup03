package CustomGuiComponents;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import entityClasses.User;
import guiAdminHomeNew.ControllerAdminHomeNew;
import guiUserUpdate.ViewUserUpdate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;

// This will be a reusable navigation bar with different 
// options depending on the role that it is given
public class HomeNavBar {
	
	static String brandColorNav = "-fx-background-color: #9c3535;";
	
	// will be different depending on user
    public static VBox createNavigationBar(Stage theStage, User user,
    		Label titleBar, BorderPane contentPane) {  
    	VBox rNavigation = null;
    	
    	if (user == null || user.getAdminRole() ) {
    		rNavigation = createAdminNavBar(theStage, user, titleBar, contentPane);
    	}
        
        return rNavigation;
    }
    
    private static VBox createAdminNavBar(Stage theStage, User user, 
    	Label titleBar, BorderPane contentPane) {
        VBox adminBar = new VBox(10);
        
        Button users = createNavBarButton(theStage,"Users");
        Button invitations = createNavBarButton(theStage, "Invitations");
        Button accountSettings = createUserSettingsButton(theStage, user);
        Button logout = createLogOutButton(theStage);
        
        Label userName = new Label(user.getFirstName() + " " + user.getLastName());
        Label roleLabel = new Label("Administrator"); 
        
        Circle placeHolderIcon = new Circle(25);
        
        placeHolderIcon.setStyle( "-fx-fill: #f1f5f9;" + 
        		"-fx-stroke: #852525;" + 
        		"-fx-stroke-width: 2;"
        );
                
        userName.setStyle (
        		"-fx-font-size: 15px;" +
//        		"-fx-font-weight: bold;" +
        		"-fx-text-fill: white;"
        );
        
        roleLabel.setStyle (
            	"-fx-font-size: 12px;" +
            	"-fx-text-fill: white;"
            );
        
        String navButtonStyle =
        	    "-fx-background-color: transparent;" +
        	    "-fx-border-color: transparent;" +
        	    "-fx-text-fill: white;" +
        	    "-fx-font-size: 14px;" +
        	    "-fx-font-weight: bold;" +
        	    "-fx-focus-color: transparent;" +
        	    "-fx-faint-focus-color: transparent;";

        users.setStyle(navButtonStyle);
        invitations.setStyle(navButtonStyle);
        accountSettings.setStyle(navButtonStyle);
        logout.setStyle(navButtonStyle);
        
        Region spacer = new Region(); // to space the logout button to the bottom
        VBox.setVgrow(spacer, Priority.ALWAYS); // tells the spacer to grow with prefHeight
        
        // refreshes per button push
        users.setOnAction(e -> {
            titleBar.setText("Users");
            contentPane.setCenter(AdminUserList.createUserList(contentPane));
        });

        invitations.setOnAction(e -> {
            titleBar.setText("Invitations");
            contentPane.setCenter(AdminInvitationList.createInvitationList(contentPane));
            contentPane.setRight(AdminInvitationManagementPanel.createInvitationManagementPanel()); //doesn't need info from the invitation list so it can be done here
        });
        
        adminBar.setSpacing(10);
        adminBar.setPadding(new Insets(15));
        adminBar.setAlignment(Pos.TOP_CENTER);
        
        adminBar.setStyle(brandColorNav);
        adminBar.getChildren().addAll(users, createSeparator(), invitations, createSeparator(),
        		spacer, placeHolderIcon,  userName, roleLabel, createSeparator(), accountSettings, createSeparator(), logout);
        
        return adminBar;
    }
    
    private static Button createUserSettingsButton(Stage theStage, User theUser) {
    	Button rAccountSettingsButton = createNavBarButton(theStage,"Account Settings");
    	rAccountSettingsButton.setOnAction((_) -> {ViewUserUpdate.displayUserUpdate(theStage, theUser);});
    	return rAccountSettingsButton;
    }
    
    // every user logout goes to the same page
    private static Button createLogOutButton(Stage theStage) {
    	Button rLogoutButton = createNavBarButton(theStage,"Logout");
    	rLogoutButton.setOnAction((_) -> {ControllerAdminHomeNew.performLogOut(theStage);});
    	
        return rLogoutButton;
    }
    
    private static Button createNavBarButton(Stage theStage, String title) {
    	Button rButton = new Button(title);
    	
    	rButton.setAlignment(Pos.CENTER);
    	
    	return rButton;
    }
    
    private static Separator createSeparator() {
        Separator sep = new Separator();
        sep.setOpacity(0.5);
        return sep;
    }
}