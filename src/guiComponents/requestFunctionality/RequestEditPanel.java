package guiComponents.requestFunctionality;

import database.Database;
import entityClasses.Request;
import entityClasses.RequestList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: RequestEditPanel Class </p>
 *
 * <p> Description: A static utility class that builds the request creation and
 * editing panel for the Admin Action Request system. Used both when an Instructor
 * creates a new request and when they edit an existing one. </p>
 *
 * <p> When requestID is -1, the panel is in Create mode — a new request is inserted
 * into the database on submit. When requestID refers to an existing request, the
 * panel is in Edit mode — the existing request's fields are pre-populated and
 * updated on submit. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to the Database class directly. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented post creation
 * and editing panel with input validation and MVC integration
 * @author Rob Taylor (Team 3) - modified to accommodate requests
 *
 * @version 1.00  2026-07-22  Initial implementation
 * 
 */
public class RequestEditPanel {

	/** Reference to the application database for request CRUD operations. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**
	 * Prevents creation of RequestEditPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private RequestEditPanel() {}

	/*******
	 * <p> Method: createRequestEditPanel() </p>
	 *
	 * <p> Description: Builds and returns the request creation/editing panel.
	 * If requestID is -1, operates in Create mode (new request). If requestID refers
	 * to an existing request, operates in Edit mode (pre-populated fields). </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the request interface
	 * @param requestID   the ID of the request to edit, or -1 when creating a new request
	 * @return a VBox containing the request editing controls
	 */
	public static VBox createRequestEditPanel(Stage theStage, BorderPane contentPane, int requestID) {
		VBox rBox = new VBox(10);

		// Load existing request if editing — null if creating new request
		Request currentRequest = null;

		if (requestID != -1) {
			// Load the existing request to pre-populate the edit fields
			RequestList requests = new RequestList();
			currentRequest = requests.getRequest(requestID);
		}

		// Title input field
		HBox titleStuff = new HBox(10);
		Label title = new Label("Title: ");
		TextField titleInput = new TextField();
		
		// Request Type selection dropdown
		HBox categoryStuff = new HBox(10);
		Label category = new Label("Request Type: ");
		ComboBox<String> selection = new ComboBox<String>();

		// Available thread categories
		selection.getItems().add("Add Role");
		selection.getItems().add("Remove Role");
		selection.getItems().add("Delete User");
		selection.getItems().add("Send One Time Password");
		selection.getItems().add("Send Invitation");
		selection.setPromptText("Select category");

		// Body input area
		VBox bodyStuff = new VBox(5);
		Label body = new Label("Body: ");
		TextArea bodyInput = new TextArea();
		bodyInput.setWrapText(true);
		bodyInput.setPrefRowCount(10);

		// Error label shown when validation fails
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		Button request = new Button("Make Request");

		if (currentRequest != null) {
			// Pre-populate fields with existing request data for editing
			titleInput.setText(currentRequest.getTitle());
			bodyInput.setText(currentRequest.getBody());
			request.setText("Save Changes"); // change button label to indicate edit mode
		}

		titleStuff.getChildren().addAll(title, titleInput);
		categoryStuff.getChildren().addAll(category, selection);
		bodyStuff.getChildren().addAll(body, bodyInput);

		final Request finalRequest = currentRequest;

		request.setOnAction(e -> {
			String inTitle = titleInput.getText();
			String inBody = bodyInput.getText();
			String inType = selection.getValue();
			String inAuthor = applicationMain.FoundationsMain.database.getCurrentUsername();

			// Validate Title — must not be null or blank
			if (inTitle == null || inTitle.isBlank()) {
				errorLabel.setText("Title cannot be empty.");
				return;
			}

			// Validate Body — must not be null or blank
			if (inBody == null || inBody.isBlank()) {
				errorLabel.setText("Body cannot be empty.");
				return;
			}

			if (finalRequest == null) {
				// Create mode — insert new request into the database
				theDatabase.createRequest(inTitle, inAuthor, inType, inBody);
			} else {
				// Edit mode — update the existing request's fields
				theDatabase.updateRequestTitle(finalRequest.getRequestID(), inTitle);
				theDatabase.updateRequestBody(finalRequest.getRequestID(), inBody);
				theDatabase.updateRequestStatus(finalRequest.getRequestID(), inType);
			}

			// Refresh the request display and nav bar after save
			contentPane.setCenter(RequestDisplayPanel.createRequestDisplayPanel(
				theStage, contentPane, requestID));
			contentPane.setLeft(guiComponents.requestFunctionality.RequestNavBar.createRequestNavBar(theStage, contentPane));
		});

		rBox.getChildren().addAll(titleStuff, categoryStuff, bodyStuff, errorLabel, request);
		return rBox;
	}
}