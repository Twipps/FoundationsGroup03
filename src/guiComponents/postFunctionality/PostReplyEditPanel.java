package guiComponents.postFunctionality;

import java.util.ArrayList;

import database.Database;
import entityClasses.Post;
import entityClasses.PostList;
import entityClasses.Thread;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
 * panel provides Title, Body, Category, and Thread fields for creating a
 * new post. </p>
 * <p> - REQ-05: "As a student, I can edit my post" — the panel pre-populates
 * with existing post data when editing, and calls updatePostTitle(),
 * updatePostBody(), updatePostCategory(), and updatePostThreadID() on save. </p>
 * <p> - REQ-09: Input validation — the panel validates that Title, Body,
 * Category, and Thread are all non-empty before allowing submission, showing
 * a red error message if any field is missing. </p>
 * <p> - REQ-12: "As a student, I can post to different threads. If I do not
 * specify a thread, it defaults to the General thread" — the Thread ComboBox
 * displays all available discussion threads stored in the database. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to the Database class directly. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented post creation
 * and editing panel with input validation and MVC integration
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added User Story mappings and inline REQ comments
 * @version 1.02  2026-07-25  Added database thread selection and threadID support
 */
public class PostReplyEditPanel {

	/** Reference to the application database for post CRUD operations. */
	private static Database theDatabase =
			applicationMain.FoundationsMain.database;

	/**
	 * Prevents creation of PostReplyEditPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private PostReplyEditPanel() {}

	/*******
	 * <p> Method: createPostEditPanel() </p>
	 *
	 * <p> Description: Builds and returns the post creation/editing panel.
	 * If postID is -1, operates in Create mode and inserts a new post. If
	 * postID refers to an existing post, operates in Edit mode and
	 * pre-populates the fields with the existing post's information. </p>
	 *
	 * <p> The panel contains fields for the post title, category, discussion
	 * thread, and body. The available discussion threads are retrieved from
	 * the database and displayed by their thread titles. </p>
	 *
	 * <p> Satisfies REQ-01 (create), REQ-05 (edit), REQ-09 (validation),
	 * and REQ-12 (thread selection). </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param postID      the ID of the post to edit, or -1 when creating a new post
	 * @param threadID    the initially selected thread ID
	 * @return a VBox containing the post creation and editing controls
	 */
	public static VBox createPostEditPanel(
			Stage theStage,
			BorderPane contentPane,
			int postID,
			int threadID) {

		VBox rBox = new VBox(10);

		// Load existing post if editing — null if creating a new post
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

		// Category selection dropdown
		HBox categoryStuff = new HBox(10);
		Label category = new Label("Category: ");
		ComboBox<String> categorySelection = new ComboBox<String>();

		// Available post categories
		categorySelection.getItems().add("General");
		categorySelection.getItems().add("Question");
		categorySelection.getItems().add("Bug");
		categorySelection.getItems().add("Help");

		// Default category when creating a new post
		categorySelection.setValue("General");

		// Thread selection dropdown
		HBox threadStuff = new HBox(10);
		Label threadLabel = new Label("Thread: ");
		ComboBox<Thread> threadSelection = new ComboBox<Thread>();

		/*
		 * REQ-12: Retrieve all available discussion threads from the database
		 * and add them to the thread selection ComboBox.
		 */
		ArrayList<Thread> threads = theDatabase.getAllThreads();
		threadSelection.getItems().addAll(threads);

		/*
		 * Display each thread's title in the dropdown instead of the default
		 * Thread object's memory address.
		 */
		threadSelection.setCellFactory(listView ->
			new ListCell<Thread>() {

				@Override
				protected void updateItem(Thread thread, boolean empty) {
					super.updateItem(thread, empty);

					if (empty || thread == null) {
						setText(null);
					} else {
						setText(thread.getTitle());
					}
				}
			}
		);

		/*
		 * Display the selected thread's title after the user chooses an item
		 * from the ComboBox.
		 */
		threadSelection.setButtonCell(
			new ListCell<Thread>() {

				@Override
				protected void updateItem(Thread thread, boolean empty) {
					super.updateItem(thread, empty);

					if (empty || thread == null) {
						setText(null);
					} else {
						setText(thread.getTitle());
					}
				}
			}
		);

		/*
		 * REQ-12: Select the thread supplied to this method. If the supplied
		 * thread does not exist, default to the General thread.
		 */
		Thread defaultThread = theDatabase.getThreadByID(threadID);

		if (defaultThread == null) {
			defaultThread = theDatabase.getThreadByTitle("General");
		}

		if (defaultThread != null) {
			for (Thread thread : threads) {
				if (thread.getThreadID() == defaultThread.getThreadID()) {
					threadSelection.setValue(thread);
					break;
				}
			}
		}

		// Body input area
		VBox bodyStuff = new VBox(5);
		Label body = new Label("Body: ");
		TextArea bodyInput = new TextArea();
		bodyInput.setWrapText(true);
		bodyInput.setPrefRowCount(10);

		// REQ-09: Error label shown when input validation fails
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		Button post = new Button("Post");

		if (currentPost != null) {
			// REQ-05: Pre-populate fields with existing post data
			titleInput.setText(currentPost.getTitle());
			bodyInput.setText(currentPost.getBody());
			categorySelection.setValue(currentPost.getCategory());

			// Change button label to indicate Edit mode
			post.setText("Save Changes");
		}

		titleStuff.getChildren().addAll(title, titleInput);
		categoryStuff.getChildren().addAll(category, categorySelection);
		threadStuff.getChildren().addAll(threadLabel, threadSelection);
		bodyStuff.getChildren().addAll(body, bodyInput);

		final Post finalPost = currentPost;

		post.setOnAction(e -> {
			String inTitle = titleInput.getText();
			String inBody = bodyInput.getText();
			String inCategory = categorySelection.getValue();
			String inAuthor = theDatabase.getCurrentUsername();
			Thread inThread = threadSelection.getValue();

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

			/*
			 * REQ-12: If no category is selected, assign the General category
			 * instead of preventing the post from being created.
			 */
			if (inCategory == null || inCategory.isBlank()) {
				inCategory = "General";
			}

			// REQ-09: Validate Thread — a discussion thread must be selected
			if (inThread == null) {
				errorLabel.setText("Thread must be selected.");
				return;
			}

			int selectedThreadID = inThread.getThreadID();

			if (finalPost == null) {
				/*
				 * REQ-01: Create mode — insert a new post and associate it
				 * with the selected discussion thread.
				 */
				theDatabase.addPost(
					inTitle,
					inBody,
					inAuthor,
					inCategory,
					selectedThreadID
				);

				/*
				 * The original postID is -1 during creation, so the display
				 * panel cannot load the newly created post using that value.
				 */
				contentPane.setCenter(
					new Label("Post created successfully.")
				);
			} else {
				/*
				 * REQ-05: Edit mode — update the existing post's title, body,
				 * category, and associated thread.
				 */
				theDatabase.updatePostTitle(
					finalPost.getPostID(),
					inTitle
				);

				theDatabase.updatePostBody(
					finalPost.getPostID(),
					inBody
				);

				theDatabase.updatePostCategory(
					finalPost.getPostID(),
					inCategory
				);

				theDatabase.updatePostThreadID(
					finalPost.getPostID(),
					selectedThreadID
				);

				/*
				 * Reload the existing post after its information has been
				 * updated.
				 */
				contentPane.setCenter(
					PostDisplayPanel.createPostDisplayPanel(
						theStage,
						contentPane,
						finalPost.getPostID()
					)
				);
			}

			// Refresh the navigation bar so changes appear immediately
			contentPane.setLeft(
				PostNavBar.createPostNavBar(
					theStage,
					contentPane
				)
			);
		});

		rBox.getChildren().addAll(
			titleStuff,
			categoryStuff,
			threadStuff,
			bodyStuff,
			errorLabel,
			post
		);

		return rBox;
	}
}