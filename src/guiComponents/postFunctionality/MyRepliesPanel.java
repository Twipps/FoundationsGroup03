package guiComponents.postFunctionality;

import java.util.ArrayList;

import entityClasses.Reply;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*******
 * <p> Title: MyRepliesPanel Class </p>
 *
 * <p> Description: A static utility class that builds the "My Replies" view
 * for students — a list of every reply received across all of the student's
 * own posts, with the ability to filter to unread replies only and search
 * by author username or keyword in the reply body. </p>
 *
 * <p> This class satisfies the Student User Story: "As a student, I can see
 * a list of my posts, the number of replies, how many of them I have not
 * yet read. I can list all the replies I have received or just those I
 * have not read, so I don't have to scan through my messages. I can search
 * my list of replies to show messages from a specific user or messages
 * that contain specified keywords." </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to Database. Backed by
 * Database.getRepliesReceivedByUser(), markReplyAsRead(), and
 * hasUserReadReply(), which are covered by ReplyReadTrackingScratchTest
 * (8/8 assertions passing). </p>
 *
 * @author Kyle Kim (Team 3) — Designed and implemented the read/unread
 * reply view for TP3
 *
 * @version 1.00  2026-07-26  Initial implementation for TP3
 */
public class MyRepliesPanel {

	/**
	 * Prevents creation of MyRepliesPanel objects because this class only
	 * provides static GUI helper methods.
	 */
	private MyRepliesPanel() {}

	/*******
	 * <p> Method: createMyRepliesPanel() </p>
	 *
	 * <p> Description: Builds and returns the full "My Replies" panel,
	 * containing an unread-only toggle, a search bar, and a scrollable list
	 * of matching replies received by the currently logged-in user. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @return a VBox containing the My Replies view
	 */
	public static VBox createMyRepliesPanel(Stage theStage, BorderPane contentPane) {
		VBox rBox = new VBox(10);
		rBox.setPadding(new Insets(15));

		Label heading = new Label("My Replies");
		heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

		String currentUser = applicationMain.FoundationsMain.database.getCurrentUsername();

		// Unread-only filter toggle
		CheckBox unreadOnlyCheckBox = new CheckBox("Show unread only");

		// Search bar — filters by author username or keyword in reply body
		TextField searchBar = new TextField();
		searchBar.setPromptText("Search by author or keyword...");
		HBox.setHgrow(searchBar, Priority.ALWAYS);

		HBox controlsRow = new HBox(10, unreadOnlyCheckBox, searchBar);

		VBox replyListBox = new VBox(8);
		ScrollPane scrollPane = new ScrollPane(replyListBox);
		scrollPane.setFitToWidth(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		// Initial population
		refreshReplyList(replyListBox, currentUser, unreadOnlyCheckBox.isSelected(),
			searchBar.getText(), contentPane, theStage);

		// Re-filter live as the unread toggle or search text changes
		unreadOnlyCheckBox.setOnAction(e -> refreshReplyList(
			replyListBox, currentUser, unreadOnlyCheckBox.isSelected(),
			searchBar.getText(), contentPane, theStage));

		searchBar.textProperty().addListener((obs, oldVal, newVal) -> refreshReplyList(
			replyListBox, currentUser, unreadOnlyCheckBox.isSelected(),
			newVal, contentPane, theStage));

		rBox.getChildren().addAll(heading, controlsRow, scrollPane);
		return rBox;
	}

	/*******
	 * <p> Method: refreshReplyList() </p>
	 *
	 * <p> Description: Reloads the reply list, applying the current
	 * unread-only and search filters. Clears and repopulates the given VBox
	 * on each call, mirroring the filterPosts() pattern already used in
	 * PostNavBar for consistency across the codebase. </p>
	 *
	 * @param replyListBox   the VBox that displays the reply rows
	 * @param username       the currently logged-in student
	 * @param unreadOnly     if true, only unread replies are shown
	 * @param search         search keyword — matches author username or reply body (blank = show all)
	 * @param contentPane    the main content pane used by the post interface
	 * @param theStage       the primary application stage
	 */
	private static void refreshReplyList(VBox replyListBox, String username, boolean unreadOnly,
			String search, BorderPane contentPane, Stage theStage) {
		replyListBox.getChildren().clear();

		ArrayList<Reply> replies = applicationMain.FoundationsMain.database
			.getRepliesReceivedByUser(username, unreadOnly);

		String searchLower = (search == null) ? "" : search.trim().toLowerCase();

		for (Reply reply : replies) {
			boolean matches = searchLower.isEmpty()
				|| safeLower(reply.getAuthor()).contains(searchLower)
				|| safeLower(reply.getBody()).contains(searchLower);

			if (matches) {
				replyListBox.getChildren().add(createReplyRow(reply, username, contentPane, theStage));
			}
		}

		if (replyListBox.getChildren().isEmpty()) {
			Label noneLabel = new Label(unreadOnly
				? "No unread replies."
				: "No replies found.");
			noneLabel.setStyle("-fx-text-fill: gray;");
			replyListBox.getChildren().add(noneLabel);
		}
	}

	/*******
	 * <p> Method: createReplyRow() </p>
	 *
	 * <p> Description: Builds a single row for a reply in the My Replies
	 * list, showing the replying student's username, a body preview, the
	 * created date, and a read/unread indicator. Clicking the row marks the
	 * reply as read and navigates to the parent post so the student can see
	 * the reply in context. </p>
	 *
	 * @param reply       the Reply to display
	 * @param username    the currently logged-in student (used to check/mark read status)
	 * @param contentPane the main content pane used by the post interface
	 * @param theStage    the primary application stage
	 * @return a VBox containing the reply row
	 */
	private static VBox createReplyRow(Reply reply, String username, BorderPane contentPane, Stage theStage) {
		VBox rowBox = new VBox(4);
		rowBox.setPadding(new Insets(8));
		rowBox.setStyle("-fx-border-color: lightgray;");

		boolean isRead = applicationMain.FoundationsMain.database.hasUserReadReply(username, reply.getReplyID());

		HBox topRow = new HBox(10);
		Label authorLabel = new Label((isRead ? "" : "\u25CF ") + "From: " + reply.getAuthor());
		authorLabel.setStyle(isRead ? "" : "-fx-font-weight: bold;");
		if (!isRead) {
			authorLabel.setTextFill(Color.web("#2266cc"));
		}

		Label dateLabel = new Label(String.valueOf(reply.getCreatedDate()));
		dateLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

		topRow.getChildren().addAll(authorLabel, dateLabel);

		Label bodyPreview = new Label(makePreview(reply.getBody()));
		bodyPreview.setWrapText(true);

		rowBox.getChildren().addAll(topRow, bodyPreview);

		// Clicking a reply marks it read and takes the student to the parent post
		rowBox.setOnMouseClicked(e -> {
			applicationMain.FoundationsMain.database.markReplyAsRead(username, reply.getReplyID());
			contentPane.setCenter(PostDisplayPanel.createPostDisplayPanel(
				theStage, contentPane, reply.getParentPostID()
			));
		});

		return rowBox;
	}

	/*******
	 * <p> Method: makePreview() </p>
	 *
	 * <p> Description: Returns a preview of the reply body, truncated to 100
	 * characters with "..." appended if longer. Mirrors PostNavBar's
	 * makeSample() pattern for consistency. </p>
	 *
	 * @param body the reply body (may be null)
	 * @return a preview string of at most 103 characters
	 */
	private static String makePreview(String body) {
		if (body == null) return "";
		if (body.length() <= 100) return body;
		return body.substring(0, 100) + "...";
	}

	/*******
	 * <p> Method: safeLower() </p>
	 *
	 * <p> Description: Returns the lowercase version of the given string, or
	 * an empty string if the input is null. Prevents NullPointerExceptions
	 * during search matching. </p>
	 *
	 * @param value the string to convert (may be null)
	 * @return lowercase string, or "" if null
	 */
	private static String safeLower(String value) {
		if (value == null) return "";
		return value.toLowerCase();
	}
}