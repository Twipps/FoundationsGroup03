package guiComponents.requestFunctionality;

import entityClasses.Request;
import entityClasses.RequestList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/*******
 * <p> Title: RequestDisplayPanel Class </p>
 *
 * <p> Description: A static utility class that builds the main content panel
 * for the Admin Action Request system. When an Instructor selects a request from the
 * RequestNavBar, this class creates a ScrollPane showing the full request content
 * (title, author, category, creation date, body)
 *
 * <p> This class satisfies the following Staff User Stories: </p>
 * <p> - "As a staff member, I can request admins to perform admin-specific
 *  actions that appear in a list that staff and admins can see." — the display 
 *  panel shows the full request. </p>
 * <p> - "As a staff, I can delete my own request" — each request row
 *   has a Delete button. </p>
 * <p> - Input validation — the Add Reply area rejects empty body text
 *   and shows an error message. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * It does not contain business logic — all database operations are delegated
 * to RequestList, and the Database class. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented Post display GUI,
 * reply input, and MVC integration
 * @author Rob Taylor (Team 3) - Modified to accommodate requests.
 *
 * @version 1.00  2026-07-22  Initial implementation
 */
public class RequestDisplayPanel {

	/**
	 * Prevents creation of RequestDisplayPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private RequestDisplayPanel() {}

	/*******
	 * <p> Method: createRequestDisplayPanel() </p>
	 *
	 * <p> Description: Builds and returns the full request display panel for the
	 * selected request. Shows the request title, author, category, creation date,
	 * and body. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the request interface
	 * @param requestID   the unique ID of the request to display
	 * @return a ScrollPane containing the selected request
	 */
	public static ScrollPane createRequestDisplayPanel(Stage theStage, BorderPane contentPane, int requestID) {
		ScrollPane requestScrollPane = new ScrollPane();
		requestScrollPane.setFitToWidth(true);

		VBox requestStack = new VBox(15);
		requestStack.setPadding(new Insets(20));

		// REQ-04: Load the request from the database by ID
		RequestList requests = new RequestList();
		Request request = requests.getRequest(requestID);

		// Handle case where request is not found (e.g. deleted by another session)
		if (request == null) {
			requestStack.getChildren().add(new Label("Request not found."));
			requestScrollPane.setContent(requestStack);
			return requestScrollPane;
		}

		// Display request title prominently
		Label title = new Label(request.getTitle());
		title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// Edit button navigates to RequestReplyEditPanel for updating the request
		Button edit = new Button("Edit");

		// Delete button shows "Are you sure?" confirmation before deleting
		Button delete = new Button("Delete");

		edit.setOnAction(e -> {
			// Navigate to edit panel for this request
			contentPane.setCenter(RequestEditPanel.createRequestEditPanel(theStage, contentPane, requestID));
		});

		delete.setOnAction(e -> {
			// Show confirmation dialog before deleting
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Request");
			confirmation.setHeaderText("Are you sure?");
			confirmation.setContentText("This will permanently delete the admin action request");

			confirmation.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					// Instructor confirmed deletion — remove request from database
					requests.deleteRequest(requestID);
					contentPane.setLeft(RequestNavBar.createRequestNavBar(theStage, contentPane));
					contentPane.setCenter(new Label("Select or create a request."));
				}
				// If student clicked Cancel, do nothing — request is preserved
			});
		});

		HBox titleRow = new HBox(10);
		titleRow.getChildren().addAll(title, spacer, edit, delete);

		// Display request metadata — author, category, creation date
		Label author = new Label("Requested by: " + request.getAuthor());
		Label category = new Label("Status: " + request.getStatus());
		Label createdDate = new Label("Created: " + request.getTimeCreated());

		// Display full request body
		Label body = new Label(request.getBody());
		body.setWrapText(true);

		requestStack.getChildren().addAll(titleRow, author, category, createdDate, body, new Separator());

		requestScrollPane.setContent(requestStack);
		return requestScrollPane;
	}
}