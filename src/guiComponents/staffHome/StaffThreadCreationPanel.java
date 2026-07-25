package guiComponents.staffHome;

import database.Database;
import entityClasses.Thread;
import entityClasses.ThreadList;
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
 * <p> Title: StaffThreadCreationPanel Class </p>
 *
 * <p> Description: A static utility class that builds the thread creation and
 * editing panel for the Staff Discussion System. Staff members use this panel
 * to create new discussion threads or edit an existing thread. </p>
 *
 * <p> When threadID is -1, the panel operates in Create mode and inserts a new
 * thread into the database. When threadID refers to an existing thread, the
 * panel operates in Edit mode and pre-populates the fields with the existing
 * thread data. </p>
 * 
 * <p> This class follows the View portion of the Foundations MVC structure.
 * Database operations are delegated to Database.java. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00  2026-07-25  Initial implementation for TP3
 */
public class StaffThreadCreationPanel {

	/** Reference to the application database for thread CRUD operations. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	// Prevents creation of StaffThreadCreationPanel objects.
	private StaffThreadCreationPanel() {
	}

	/*******
	 * <p> Method: createThreadCreationPanel() </p>
	 *
	 * <p> Description: Convenience method used when creating a new thread.
	 * Calls createThreadEditPanel() with a threadID of -1. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the staff interface
	 * @return a VBox containing the thread creation controls
	 */
	public static VBox createThreadCreationPanel(
			Stage theStage,
			BorderPane contentPane) {

		return createThreadEditPanel(theStage, contentPane, -1);
	}

	/*******
	 * <p> Method: createThreadEditPanel() </p>
	 *
	 * <p> Description: Builds and returns the thread creation/editing panel.
	 * If threadID is -1, the panel operates in Create mode. If threadID refers
	 * to an existing thread, the panel operates in Edit mode and pre-populates
	 * the fields with the existing thread information. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the staff interface
	 * @param threadID    the ID of the thread to edit, or -1 to create one
	 * @return a VBox containing the thread editing controls
	 */
	public static VBox createThreadEditPanel(
			Stage theStage,
			BorderPane contentPane,
			int threadID) {

		VBox rBox = new VBox(10);

		Thread currentThread = null;

		if (threadID != -1) {
			ThreadList threads = new ThreadList();
			currentThread = threads.getThread(threadID);
		}

		Label panelTitle = new Label(
			currentThread == null ? "Create Thread" : "Edit Thread"
		);

		panelTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		HBox titleStuff = new HBox(10);
		Label titleLabel = new Label("Title: ");
		TextField titleInput = new TextField();
		titleInput.setPromptText("Enter thread title");

		titleStuff.getChildren().addAll(titleLabel, titleInput);

		HBox categoryStuff = new HBox(10);
		Label categoryLabel = new Label("Category: ");

		ComboBox<String> categorySelection = new ComboBox<String>();
		categorySelection.getItems().add("General");
		categorySelection.getItems().add("Question");
		categorySelection.getItems().add("Bug");
		categorySelection.getItems().add("Help");
		categorySelection.setPromptText("Select category");

		categoryStuff.getChildren().addAll(
			categoryLabel,
			categorySelection
		);

		VBox bodyStuff = new VBox(5);
		Label bodyLabel = new Label("Description: ");

		TextArea bodyInput = new TextArea();
		bodyInput.setPromptText("Describe the purpose of this thread");
		bodyInput.setWrapText(true);
		bodyInput.setPrefRowCount(10);

		bodyStuff.getChildren().addAll(bodyLabel, bodyInput);

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		Label successLabel = new Label();
		successLabel.setStyle("-fx-text-fill: green;");

		Button submitButton = new Button("Create Thread");
		Button cancelButton = new Button("Cancel");

		if (currentThread != null) {
			titleInput.setText(currentThread.getTitle());
			bodyInput.setText(currentThread.getBody());
			categorySelection.setValue(currentThread.getCategory());

			submitButton.setText("Save Changes");

			if (currentThread.isGeneral()) {
				titleInput.setDisable(true);
				submitButton.setDisable(true);

				errorLabel.setText(
					"The General thread cannot be renamed."
				);
			}
		}

		final Thread finalThread = currentThread;

		submitButton.setOnAction(e -> {
			errorLabel.setText("");
			successLabel.setText("");

			String inTitle = titleInput.getText();
			String inBody = bodyInput.getText();
			String inCategory = categorySelection.getValue();

			String inAuthor =
				theDatabase.getCurrentUsername();

			if (inTitle == null || inTitle.isBlank()) {
				errorLabel.setText(
					"Thread title cannot be empty."
				);
				return;
			}

			inTitle = inTitle.trim();

			if (finalThread == null
					&& inTitle.equalsIgnoreCase("General")) {

				errorLabel.setText(
					"The title General is reserved."
				);
				return;
			}

			if (inBody != null) {
				inBody = inBody.trim();

				if (inBody.isEmpty()) {
					inBody = null;
				}
			}

			if (inCategory != null) {
				inCategory = inCategory.trim();

				if (inCategory.isEmpty()) {
					inCategory = null;
				}
			}

			if (finalThread == null) {
				boolean created = theDatabase.createThread(
					inTitle,
					inBody,
					inAuthor,
					inCategory
				);

				if (!created) {
					errorLabel.setText(
						"Unable to create thread. The title may already exist."
					);
					return;
				}
			} else {
				if (finalThread.isGeneral()) {
					errorLabel.setText(
						"The General thread cannot be renamed."
					);
					return;
				}

				boolean updated = theDatabase.updateThreadTitle(
					finalThread.getThreadID(),
					inTitle
				);

				if (!updated) {
					errorLabel.setText(
						"Unable to update thread title."
					);
					return;
				}
			}
			
			if (finalThread == null) {
				contentPane.setCenter(
					new Label("Thread created successfully.")
				);
			} else {
				contentPane.setCenter(
					StaffThreadDisplayPanel.createThreadDisplayPanel(
						theStage,
						contentPane,
						finalThread.getThreadID()
					)
				);
			}

			contentPane.setLeft(
				StaffThreadNavBar.createThreadNavBar(
					theStage,
					contentPane
				)
			);
		});

		cancelButton.setOnAction(e -> {
			if (finalThread == null) {
				contentPane.setCenter(
					new Label("Select a thread.")
				);
			} else {
				contentPane.setCenter(
					StaffThreadDisplayPanel.createThreadDisplayPanel(
						theStage,
						contentPane,
						finalThread.getThreadID()
					)
				);
			}
		});

		HBox buttonRow = new HBox(10);
		buttonRow.getChildren().addAll(
			submitButton,
			cancelButton
		);

		rBox.getChildren().addAll(
			panelTitle,
			titleStuff,
			categoryStuff,
			bodyStuff,
			errorLabel,
			successLabel,
			buttonRow
		);

		return rBox;
	}
}