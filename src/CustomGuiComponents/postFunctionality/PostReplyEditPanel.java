package CustomGuiComponents.postFunctionality;

import database.Database;
import entityClasses.Post;
import entityClasses.PostList;
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
 * <p> Title: PostReplyEditPanel Class </p>
 *
 * <p> Description: A static utility class that builds the post creation and
 * editing panel for the Student Discussion System. Used both when a student
 * creates a new post and when they edit an existing one. </p>
 *
 * <p> When postID is -1, the panel is in Create mode — a new post is inserted
 * into the database on submit. When postID refers to an existing post, the
 * panel is in Edit mode — the existing post's fields are pre-populated and
 * updated on submit. </p>
 *
 * <p> This class satisfies the following Students User Stories: </p>
 * <p> - REQ-01: "As a student, I can post statements and questions" — the
 *   panel provides Title, Body, and Category fields for creating a new post. </p>
 * <p> - REQ-05: "As a student, I can edit my post" — the panel pre-populates
 *   with existing post data when editing, and calls updatePostTitle(),
 *   updatePostBody(), and updatePostCategory() on save. </p>
 * <p> - REQ-09: Input validation — the panel validates that Title, Body, and
 *   Category are all non-empty before allowing submission, showing a red
 *   error message if any field is missing. </p>
 * <p> - REQ-12: "As a student, I can post to different threads. If I do not
 *   specify a thread, it defaults to the General thread" — the Category
 *   ComboBox provides thread selection with General, Question, Bug, and Help. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to the Database class directly. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented post creation
 * and editing panel with input validation and MVC integration
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added User Story mappings and inline REQ comments
 * 
 * TODO: instead of a warning for an empty category there needs to be a "general" category assigned for each post
 */
public class PostReplyEditPanel {

	/** Reference to the application database for post CRUD operations. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**
	 * Prevents creation of PostReplyEditPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private PostReplyEditPanel() {}

	/*******
	 * <p> Method: createPostEditPanel() </p>
	 *
	 * <p> Description: Builds and returns the post creation/editing panel.
	 * If postID is -1, operates in Create mode (new post). If postID refers
	 * to an existing post, operates in Edit mode (pre-populated fields). </p>
	 *
	 * <p> Satisfies REQ-01 (create), REQ-05 (edit), REQ-09 (validation),
	 * and REQ-12 (thread selection). </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param postID      the ID of the post to edit, or -1 when creating a new post
	 * @return a VBox containing the post editing controls
	 */
	public static VBox createPostEditPanel(Stage theStage, BorderPane contentPane, int postID) {
		VBox rBox = new VBox(10);

		// Load existing post if editing — null if creating new post
		Post currentPost = null;

		if (postID != -1) {
			// REQ-05: Load the existing post to pre-populate the edit fields
			PostList posts = new PostList();
			currentPost = posts.getPost(postID);
		}

		// Title input field
		HBox titleStuff = new HBox(10);
		Label title = new Label("Title: ");
		TextField titleInput = new TextField();

		// Category selection dropdown — REQ-12: supports multiple threads
		HBox categoryStuff = new HBox(10);
		Label category = new Label("Category: ");
		ComboBox<String> selection = new ComboBox<String>();

		// REQ-12: Available thread categories
		selection.getItems().add("General"); // default thread per user stories
		selection.getItems().add("Question");
		selection.getItems().add("Bug");
		selection.getItems().add("Help");
		selection.setPromptText("Select category");

		// Body input area
		VBox bodyStuff = new VBox(5);
		Label body = new Label("Body: ");
		TextArea bodyInput = new TextArea();
		bodyInput.setWrapText(true);
		bodyInput.setPrefRowCount(10);

		// REQ-09: Error label shown when validation fails
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		Button post = new Button("Post");

		if (currentPost != null) {
			// REQ-05: Pre-populate fields with existing post data for editing
			titleInput.setText(currentPost.getTitle());
			bodyInput.setText(currentPost.getBody());
			selection.setValue(currentPost.getCategory());
			post.setText("Save Changes"); // change button label to indicate edit mode
		}

		titleStuff.getChildren().addAll(title, titleInput);
		categoryStuff.getChildren().addAll(category, selection);
		bodyStuff.getChildren().addAll(body, bodyInput);

		final Post finalPost = currentPost;

		post.setOnAction(e -> {
			String inTitle = titleInput.getText();
			String inBody = bodyInput.getText();
			String inCategory = selection.getValue();
			String inAuthor = applicationMain.FoundationsMain.database.getCurrentUsername();

			// REQ-09: Validate Title — must not be null or blank
			if (inTitle == null || inTitle.isBlank()) {
				errorLabel.setText("Title cannot be empty.");
				return;
			}

			// REQ-09: Validate Body — must not be null or blank
			if (inBody == null || inBody.isBlank()) {
				errorLabel.setText("Body cannot be empty.");
				return;
			}

			// REQ-09: Validate Category — must be selected from the dropdown
			if (inCategory == null || inCategory.isBlank()) {
				errorLabel.setText("Category must be selected.");
				return;
			}

			if (finalPost == null) {
				// REQ-01: Create mode — insert new post into the database
				theDatabase.addPost(inTitle, inBody, inAuthor, inCategory);
			} else {
				// REQ-05: Edit mode — update the existing post's fields
				theDatabase.updatePostTitle(finalPost.getPostID(), inTitle);
				theDatabase.updatePostBody(finalPost.getPostID(), inBody);
				theDatabase.updatePostCategory(finalPost.getPostID(), inCategory);
			}

			// Refresh the post display and nav bar after save
			contentPane.setCenter(PostDisplayPanel.createPostDisplayPanel(
				theStage, contentPane, postID));
			contentPane.setLeft(CustomGuiComponents.postFunctionality.PostNavBar.createPostNavBar(theStage, contentPane));
		});

		rBox.getChildren().addAll(titleStuff, categoryStuff, bodyStuff, errorLabel, post);
		return rBox;
	}
}