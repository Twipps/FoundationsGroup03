package CustomGuiComponents.postFunctionality;

import entityClasses.Post;
import entityClasses.PostList;
import entityClasses.Reply;
import entityClasses.ReplyList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/*******
 * <p> Title: PostDisplayPanel Class </p>
 *
 * <p> Description: A static utility class that builds the main content panel
 * for the Student Discussion System. When a student selects a post from the
 * PostNavBar, this class creates a ScrollPane showing the full post content
 * (title, author, category, creation date, body), an Add Reply input area,
 * and a list of all replies to that post. </p>
 *
 * <p> This class satisfies the following Students User Stories: </p>
 * <p> - REQ-01/REQ-04: "As a student, I can post statements and questions and
 *   receive replies" — the display panel shows the full post and all replies. </p>
 * <p> - REQ-02: "As a student, I can reply to an existing post" — the Add Reply
 *   input area and Submit Reply button are provided. </p>
 * <p> - REQ-05: "As a student, I can edit my post" — the Edit button navigates
 *   to PostReplyEditPanel for updating the post. </p>
 * <p> - REQ-07: "As a student, I can delete one of my posts. When I do this,
 *   I receive an 'Are you sure?' question before the delete takes place" —
 *   the Delete button shows a confirmation dialog before deleting. </p>
 * <p> - REQ-08: "As a student, I can delete my own reply" — each reply row
 *   has a Delete button. </p>
 * <p> - REQ-09: Input validation — the Add Reply area rejects empty body text
 *   and shows an error message. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * It does not contain business logic — all database operations are delegated
 * to PostList, ReplyList, and the Database class. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented Post display GUI,
 * reply input, and MVC integration
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added "Are you sure?" delete confirmation (REQ-07),
 *                             User Story mappings, and inline REQ comments
 */
public class PostDisplayPanel {

	/**
	 * Prevents creation of PostDisplayPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private PostDisplayPanel() {}

	/*******
	 * <p> Method: createPostDisplayPanel() </p>
	 *
	 * <p> Description: Builds and returns the full post display panel for the
	 * selected post. Shows the post title, author, category, creation date,
	 * body, an Add Reply input area, and all existing replies. </p>
	 *
	 * <p> Satisfies REQ-04: student can view a single post with its replies. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param postID      the unique ID of the post to display
	 * @return a ScrollPane containing the selected post, reply input, and replies
	 */
	public static ScrollPane createPostDisplayPanel(Stage theStage, BorderPane contentPane, int postID) {
		ScrollPane postReplyStack = new ScrollPane();
		postReplyStack.setFitToWidth(true);

		VBox postStack = new VBox(15);
		postStack.setPadding(new Insets(20));

		// REQ-04: Load the post from the database by ID
		PostList posts = new PostList();
		Post post = posts.getPost(postID);

		// Handle case where post is not found (e.g. deleted by another session)
		if (post == null) {
			postStack.getChildren().add(new Label("Post not found."));
			postReplyStack.setContent(postStack);
			return postReplyStack;
		}

		// REQ-04: Display post title prominently
		Label title = new Label(post.getTitle());
		title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// REQ-05: Edit button navigates to PostReplyEditPanel for updating the post
		Button edit = new Button("Edit");

		// REQ-07: Delete button shows "Are you sure?" confirmation before deleting
		Button delete = new Button("Delete");

		edit.setOnAction(e -> {
			// REQ-05: Navigate to edit panel for this post
			contentPane.setCenter(PostReplyEditPanel.createPostEditPanel(theStage, contentPane, postID));
		});

		delete.setOnAction(e -> {
			// REQ-07: Show confirmation dialog before deleting — user story explicitly requires this
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
			confirmation.setTitle("Delete Post");
			confirmation.setHeaderText("Are you sure?");
			confirmation.setContentText("This will permanently delete the post and its replies.");

			confirmation.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					// REQ-07: Student confirmed deletion — remove post from database
					posts.deletePost(postID);
					contentPane.setLeft(PostNavBar.createPostNavBar(theStage, contentPane));
					contentPane.setCenter(new Label("Select or create a post."));
				}
				// If student clicked Cancel, do nothing — post is preserved
			});
		});

		HBox titleRow = new HBox(10);
		titleRow.getChildren().addAll(title, spacer, edit, delete);

		// REQ-04: Display post metadata — author, category, creation date
		Label author = new Label("Posted by: " + post.getAuthor());
		Label category = new Label("Category: " + post.getCategory()); // REQ-12: shows thread
		Label createdDate = new Label("Created: " + post.getCreatedDate());

		// REQ-04: Display full post body
		Label body = new Label(post.getBody());
		body.setWrapText(true);

		postStack.getChildren().addAll(titleRow, author, category, createdDate, body, new Separator());

		// REQ-02: Add the reply input area below the post
		postStack.getChildren().add(createReplyInput(contentPane, postID, theStage));

		Label repliesTitle = new Label("Replies");
		repliesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		postStack.getChildren().add(repliesTitle);

		// REQ-02: Display all existing replies for this post
		postStack.getChildren().add(replyStack(theStage, contentPane, postID));

		postReplyStack.setContent(postStack);
		return postReplyStack;
	}

	/*
	 * Creates the Add Reply input area shown below the post body.
	 * Validates that the reply body is not empty before submitting (REQ-09).
	 * Private — only used internally by createPostDisplayPanel.
	 */
	private static VBox createReplyInput(BorderPane contentPane, int postID, Stage theStage) {
		VBox rBox = new VBox(8);

		Label replyLabel = new Label("Add Reply:");
		TextArea replyInput = new TextArea();
		replyInput.setWrapText(true);
		replyInput.setPrefRowCount(4);

		Label error = new Label();
		error.setStyle("-fx-text-fill: red;");

		Button submit = new Button("Submit Reply");

		submit.setOnAction(e -> {
			String body = replyInput.getText().trim();

			// REQ-09: Validate that reply body is not empty before submitting
			if (body.isEmpty()) {
				error.setText("Reply body cannot be empty.");
				return;
			}

			// REQ-02: Create the reply in the database linked to this post
			ReplyList replies = new ReplyList();
			replies.createReply(
				postID,
				body,
				applicationMain.FoundationsMain.database.getCurrentUsername()
			);

			// Refresh the display panel to show the new reply
			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(theStage, contentPane, postID)
			);
		});

		rBox.getChildren().addAll(replyLabel, replyInput, error, submit);
		return rBox;
	}

	/*******
	 * <p> Method: replyStack() </p>
	 *
	 * <p> Description: Builds and returns a VBox containing one reply row
	 * for each reply on the given post. </p>
	 *
	 * <p> Satisfies REQ-02: student can see replies to a post. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param postID      the unique ID of the post whose replies are displayed
	 * @return a VBox containing all reply rows for the selected post
	 */
	public static VBox replyStack(Stage theStage, BorderPane contentPane, int postID) {
		VBox rBox = new VBox(10);

		// REQ-02: Load all replies for this post from the database
		ReplyList replies = new ReplyList();

		for (Reply reply : replies.getRepliesForPost(postID)) {
			rBox.getChildren().add(createReplyRow(theStage, contentPane, reply));
		}

		return rBox;
	}

	/*******
	 * <p> Method: createReplyRow() </p>
	 *
	 * <p> Description: Builds and returns a single reply row showing the reply
	 * author, creation date, body text, and a Delete button. </p>
	 *
	 * <p> Satisfies REQ-02 (view reply) and REQ-08 (delete own reply). </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param reply       the Reply object to display
	 * @return a VBox containing the reply information and delete control
	 */
	public static VBox createReplyRow(Stage theStage, BorderPane contentPane, Reply reply) {
		VBox rBox = new VBox(6);
		rBox.setPadding(new Insets(10));
		rBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

		// REQ-02: Display reply author and timestamp
		Label author = new Label("Reply by: " + reply.getAuthor());
		Label created = new Label("Created: " + reply.getCreatedDate());

		// REQ-02: Display reply body text
		Label body = new Label(reply.getBody());
		body.setWrapText(true);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// REQ-08: Delete button removes this reply from the database
		Button delete = new Button("Delete");

		delete.setOnAction(e -> {
			// REQ-08: Delete the reply and refresh the post display
			ReplyList replies = new ReplyList();
			replies.deleteReply(reply.getReplyID());
			contentPane.setCenter(createPostDisplayPanel(theStage, contentPane, reply.getParentPostID()));
		});

		HBox topRow = new HBox(10);
		topRow.getChildren().addAll(author, spacer, delete);

		rBox.getChildren().addAll(topRow, created, body);
		return rBox;
	}
}