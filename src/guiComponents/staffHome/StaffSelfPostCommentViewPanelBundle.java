package guiComponents.staffHome;

import entityClasses.Post;
import entityClasses.PostList;
import entityClasses.Reply;
import entityClasses.ReplyList;
import entityClasses.User;
import guiComponents.postFunctionality.PostDisplayPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p>Title: InstructorMyPostsPanel Class</p>
 *
 * <p>Description: Builds a panel containing all posts and replies created by
 * the selected instructor. Posts are displayed in the left column, while
 * replies are displayed in the right column.</p>
 *
 * <p>Selecting either a post or a reply opens the complete parent post using
 * the current PostDisplayPanel implementation.</p>
 *
 * <p>This class supports OA5: "As an instructor I want to be able to view all
 * posts and comments that I've made via a selection bar on the home page."</p>
 *
 * @author James Suchovic (Team 03)
 *
 * @version 1.00 Initial implementation
 * @version 1.01 Updated for the current post and thread system
 */
public class StaffSelfPostCommentViewPanelBundle {

	/** Maximum number of characters shown in a post or reply preview. */
	private static final int PREVIEW_LENGTH = 140;

	/**
	 * Prevents creation of instructorMyPostsPanel objects.
	 */
	private StaffSelfPostCommentViewPanelBundle() {}

	/*******
	 * <p>Method: createInstructorMyPostsPanel()</p>
	 *
	 * <p>Description: Creates the complete instructor activity panel. The
	 * left side contains posts authored by the instructor and the right side
	 * contains replies authored by the instructor.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the instructor home page content pane
	 * @param user the instructor whose activity should be displayed
	 * @return an HBox containing the instructor's posts and replies
	 */
	public static HBox createInstructorMyPostsPanels(
			Stage theStage,
			BorderPane contentPane,
			User user) {

		HBox rBox = new HBox(20);
		rBox.setPadding(new Insets(20));

		if (user == null) { // saftey
			rBox.getChildren().add(
				new Label("Unable to determine the current instructor.")
			);

			return rBox;
		}

		VBox postColumn = createPostColumn(
			theStage,
			contentPane,
			user
		);

		VBox replyColumn = createReplyColumn(
			theStage,
			contentPane,
			user
		);

		ScrollPane postScrollPane = new ScrollPane(postColumn);
		postScrollPane.setFitToWidth(true);
		postScrollPane.setHbarPolicy(
			ScrollPane.ScrollBarPolicy.NEVER
		);

		ScrollPane replyScrollPane = new ScrollPane(replyColumn);
		replyScrollPane.setFitToWidth(true);
		replyScrollPane.setHbarPolicy(
			ScrollPane.ScrollBarPolicy.NEVER
		);

		HBox.setHgrow(postScrollPane, Priority.ALWAYS);
		HBox.setHgrow(replyScrollPane, Priority.ALWAYS);

		postScrollPane.setMaxWidth(Double.MAX_VALUE);
		replyScrollPane.setMaxWidth(Double.MAX_VALUE);

		postScrollPane.setPrefWidth(500);
		replyScrollPane.setPrefWidth(500);

		rBox.getChildren().addAll(
			postScrollPane,
			replyScrollPane
		);

		return rBox;
	}

	/*******
	 * <p>Method: createPostColumn()</p>
	 *
	 * <p>Description: Creates the column containing all current posts whose
	 * author matches the selected instructor.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the instructor home page content pane
	 * @param user the instructor whose posts should be displayed
	 * @return a VBox containing the instructor's post rows
	 */
	private static VBox createPostColumn(
			Stage theStage,
			BorderPane contentPane,
			User user) {

		VBox rBox = createColumnContainer();

		Label title = createColumnTitle("My Posts");
		rBox.getChildren().add(title);

		PostList postList = new PostList();

		if (postList.getPostList() == null) {
			rBox.getChildren().add(
				new Label("Unable to load posts.")
			);

			return rBox;
		}

		boolean postFound = false;

		for (Post post : postList.getPostList()) {

			if (post == null) {
				continue;
			}

			if (sameAuthor(
					post.getAuthor(),
					user.getUserName())) {

				rBox.getChildren().add(
					createInstructorPostRow(
						theStage,
						contentPane,
						post
					)
				);

				postFound = true;
			}
		}

		if (!postFound) {
			rBox.getChildren().add(
				new Label(
					"No posts created by this instructor."
				)
			);
		}

		return rBox;
	}

	/*******
	 * <p>Method: createReplyColumn()</p>
	 *
	 * <p>Description: Creates the column containing all current replies whose
	 * author matches the selected instructor.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the instructor home page content pane
	 * @param user the instructor whose replies should be displayed
	 * @return a VBox containing the instructor's reply rows
	 */
	private static VBox createReplyColumn(
			Stage theStage,
			BorderPane contentPane,
			User user) {

		VBox rBox = createColumnContainer();

		Label title = createColumnTitle("My Comments");
		rBox.getChildren().add(title);

		ReplyList replyList = new ReplyList();

		if (replyList.getReplyList() == null) {
			rBox.getChildren().add(
				new Label("Unable to load comments.")
			);

			return rBox;
		}

		boolean replyFound = false;

		for (Reply reply : replyList.getReplyList()) {

			if (reply == null) {
				continue;
			}

			if (sameAuthor(
					reply.getAuthor(),
					user.getUserName())) {

				rBox.getChildren().add(
					createInstructorReplyRow(
						theStage,
						contentPane,
						reply
					)
				);

				replyFound = true;
			}
		}

		if (!replyFound) {
			rBox.getChildren().add(
				new Label(
					"No comments created by this instructor."
				)
			);
		}

		return rBox;
	}

	/*******
	 * <p>Method: createInstructorPostRow()</p>
	 *
	 * <p>Description: Creates a selectable preview row for one post.
	 * Selecting the row opens the complete post.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the instructor home page content pane
	 * @param post the post represented by the row
	 * @return a VBox containing the post preview
	 */
	private static VBox createInstructorPostRow(
			Stage theStage,
			BorderPane contentPane,
			Post post) {

		VBox rBox = createSelectableRow();

		HBox titleRow = new HBox(10);

		Label title = new Label(
			safeDisplay(post.getTitle(), "Untitled Post")
		);

		title.setStyle("-fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label category = new Label(
			safeDisplay(post.getCategory(), "General")
		);

		category.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		titleRow.getChildren().addAll(
			title,
			spacer,
			category
		);

		Label created = new Label(
			"Created: " + post.getCreatedDate()
		);

		created.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		Label body = new Label(
			makeSample(post.getBody())
		);

		body.setWrapText(true);

		rBox.setOnMouseClicked(e -> {
			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(
					theStage,
					contentPane,
					post.getPostID()
				)
			);
		});

		rBox.getChildren().addAll(
			titleRow,
			created,
			body
		);

		return rBox;
	}

	/*******
	 * <p>Method: createInstructorReplyRow()</p>
	 *
	 * <p>Description: Creates a selectable preview row for one reply.
	 * Selecting the row opens the post that contains the reply.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the instructor home page content pane
	 * @param reply the reply represented by the row
	 * @return a VBox containing the reply preview
	 */
	private static VBox createInstructorReplyRow(
			Stage theStage,
			BorderPane contentPane,
			Reply reply) {

		VBox rBox = createSelectableRow();

		HBox headingRow = new HBox(10);

		Label parentPost = new Label(
			"Comment on Post #" +
			reply.getParentPostID()
		);

		parentPost.setStyle("-fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label created = new Label(
			String.valueOf(reply.getCreatedDate())
		);

		created.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		headingRow.getChildren().addAll(
			parentPost,
			spacer,
			created
		);

		Label body = new Label(
			makeSample(reply.getBody())
		);

		body.setWrapText(true);

		/*
		 * Open the parent post so the instructor can see the reply
		 * in its original discussion context.
		 */
		rBox.setOnMouseClicked(e -> {
			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(
					theStage,
					contentPane,
					reply.getParentPostID()
				)
			);
		});

		rBox.getChildren().addAll(
			headingRow,
			body
		);

		return rBox;
	}

	/**
	 * Creates the shared layout used by both activity columns.
	 *
	 * @return a styled VBox for one activity column
	 */
	private static VBox createColumnContainer() {

		VBox rBox = new VBox(10);

		rBox.setPadding(new Insets(15));

		rBox.setStyle(
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;"
		);

		rBox.setFillWidth(true);

		return rBox;
	}

	/**
	 * Creates a heading for an activity column.
	 *
	 * @param text the heading text
	 * @return the formatted heading label
	 */
	private static Label createColumnTitle(String text) {

		Label title = new Label(text);

		title.setStyle(
			"-fx-font-size: 20px;" +
			"-fx-font-weight: bold;"
		);

		return title;
	}

	/**
	 * Creates a row with shared normal and hover formatting.
	 *
	 * @return the formatted selectable row
	 */
	private static VBox createSelectableRow() {

		VBox rBox = new VBox(6);
		rBox.setPadding(new Insets(10));

		String normalStyle =
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;";

		String hoverStyle =
			"-fx-border-color: gray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;" +
			"-fx-background-color: #f2f2f2;";

		rBox.setStyle(normalStyle);

		rBox.setOnMouseEntered(e ->
			rBox.setStyle(hoverStyle)
		);

		rBox.setOnMouseExited(e ->
			rBox.setStyle(normalStyle)
		);

		return rBox;
	}

	/**
	 * Checks whether two author usernames refer to the same user.
	 *
	 * @param author the author stored on the post or reply
	 * @param userName the instructor username
	 * @return true when the usernames match
	 */
	private static boolean sameAuthor(
			String author,
			String userName) {

		if (author == null || userName == null) {
			return false;
		}

		return author.trim().equalsIgnoreCase(
			userName.trim()
		);
	}

	/**
	 * Produces an abbreviated preview of a post or reply body.
	 *
	 * @param text the complete body
	 * @return the shortened body preview
	 */
	private static String makeSample(String text) {

		if (text == null || text.isBlank()) {
			return "No content.";
		}

		String trimmedText = text.trim();

		if (trimmedText.length() <= PREVIEW_LENGTH) {
			return trimmedText;
		}

		return trimmedText.substring(
			0,
			PREVIEW_LENGTH
		) + "...";
	}

	/**
	 * Safely prepares a nullable string for display.
	 *
	 * @param value the original value
	 * @param fallback the value used when the original is empty
	 * @return a safe display string
	 */
	private static String safeDisplay(
			String value,
			String fallback) {

		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value;
	}
}