package guiComponents.staffHome;

import java.util.ArrayList;

import applicationMain.FoundationsMain;
import entityClasses.Post;
import entityClasses.Thread;
import entityClasses.ThreadList;
import guiComponents.postFunctionality.PostDisplayPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: StaffThreadDisplayPanel Class </p>
 *
 * <p> Description: Displays a selected discussion thread and all active posts
 * associated with that thread. The post collection is displayed inside a
 * vertically scrollable list with no fixed number of visible posts. </p>
 *
 * <p> Each post row displays the post title, author, body preview, creation
 * date, and reply count. Selecting a post opens the complete post through
 * PostDisplayPanel. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00  2026-07-25  Initial implementation for TP3
 */
public class StaffThreadDisplayPanel {

	/**
	 * Prevents creation of StaffThreadDisplayPanel.
	 */
	private StaffThreadDisplayPanel() {}

	/*******
	 * <p> Method: createThreadDisplayPanel() </p>
	 *
	 * <p> Description: Builds the display for one selected thread. The top
	 * section shows thread information and management controls. The remaining
	 * space contains a search field and an indefinitely scrollable list of
	 * posts associated with the selected threadID. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main application content pane
	 * @param threadID    the selected thread's database ID
	 * @return a BorderPane containing the thread information and posts
	 */
	public static BorderPane createThreadDisplayPanel(
			Stage theStage,
			BorderPane contentPane,
			int threadID) {

		BorderPane rPane = new BorderPane();
		rPane.setPadding(new Insets(20));

		ThreadList threadList = new ThreadList();
		Thread thread = threadList.getThread(threadID);

		if (thread == null) {
			rPane.setCenter(new Label("Thread not found."));
			return rPane;
		}

		VBox threadHeader = createThreadHeader(
			theStage,
			contentPane,
			thread
		);

		rPane.setTop(threadHeader);

		VBox postSection = new VBox(10);
		postSection.setPadding(new Insets(15, 0, 0, 0));

		Label postsTitle = new Label("Posts in " + thread.getTitle());
		postsTitle.setStyle(
			"-fx-font-size: 18px;" +
			"-fx-font-weight: bold;"
		);

		HBox searchRow = new HBox(10);

		TextField searchBar = new TextField();
		searchBar.setPromptText("Search posts in this thread...");
		HBox.setHgrow(searchBar, Priority.ALWAYS);

		Button refreshButton = new Button("Refresh");

		searchRow.getChildren().addAll(
			searchBar,
			refreshButton
		);

		VBox postList = new VBox(8);

		ScrollPane postScrollPane = new ScrollPane(postList);
		postScrollPane.setFitToWidth(true);
		postScrollPane.setPannable(true);

		postScrollPane.setVbarPolicy(
			ScrollPane.ScrollBarPolicy.AS_NEEDED
		);

		postScrollPane.setHbarPolicy(
			ScrollPane.ScrollBarPolicy.NEVER
		);

		VBox.setVgrow(postScrollPane, Priority.ALWAYS);

		loadPosts(
			postList,
			threadID,
			searchBar.getText(),
			theStage,
			contentPane
		);

		refreshButton.setOnAction(e -> {
			loadPosts(
				postList,
				threadID,
				searchBar.getText(),
				theStage,
				contentPane
			);
		});

		searchBar.textProperty().addListener(
			(observable, oldValue, newValue) -> {
				loadPosts(
					postList,
					threadID,
					newValue,
					theStage,
					contentPane
				);
			}
		);

		postSection.getChildren().addAll(
			postsTitle,
			searchRow,
			postScrollPane
		);

		rPane.setCenter(postSection);

		return rPane;
	}

	/*******
	 * <p> Method: createThreadHeader() </p>
	 *
	 * <p> Description: Creates the top section showing the selected thread's
	 * title, author, description, category, and management controls. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main application content pane
	 * @param thread      the thread being displayed
	 * @return a VBox containing the thread information
	 */
	private static VBox createThreadHeader(
			Stage theStage,
			BorderPane contentPane,
			Thread thread) {

		VBox rBox = new VBox(8);

		HBox titleRow = new HBox(10);

		Label titleLabel = new Label(thread.getTitle());
		titleLabel.setStyle(
			"-fx-font-size: 22px;" +
			"-fx-font-weight: bold;"
		);

		if (thread.isGeneral()) {
			titleLabel.setText(thread.getTitle() + " [Default]");
		}

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button editButton = new Button("Edit");
		Button deleteButton = new Button("Delete");

		editButton.setOnAction(e -> {
			contentPane.setCenter(
				StaffThreadCreationPanel.createThreadEditPanel(
					theStage,
					contentPane,
					thread.getThreadID()
				)
			);
		});

		deleteButton.setOnAction(e -> {
			FoundationsMain.database.deleteThread(
				thread.getThreadID()
			);

			contentPane.setLeft(
				StaffThreadNavBar.createThreadNavBar(
					theStage,
					contentPane
				)
			);

			contentPane.setCenter(
				new Label("Thread deleted.")
			);
		});

		if (thread.isGeneral()) {
			editButton.setDisable(true);
			deleteButton.setDisable(true);
		}

		titleRow.getChildren().addAll(
			titleLabel,
			spacer,
			editButton,
			deleteButton
		);

		Label authorLabel = new Label(
			"Created by: " + safeDisplay(thread.getAuthor())
		);

		Label categoryLabel = new Label(
			"Category: " + safeDisplay(thread.getCategory())
		);

		Label createdLabel = new Label(
			"Created: " + thread.getCreatedDate()
		);

		Label bodyLabel = new Label(
			thread.getBody() == null || thread.getBody().isBlank()
				? "No description."
				: thread.getBody()
		);

		bodyLabel.setWrapText(true);

		rBox.getChildren().addAll(
			titleRow,
			authorLabel,
			categoryLabel,
			createdLabel,
			bodyLabel,
			new Separator()
		);

		return rBox;
	}

	/*******
	 * <p> Method: loadPosts() </p>
	 *
	 * <p> Description: Reloads the scrollable post list with every active post
	 * associated with the selected threadID. Posts can additionally be filtered
	 * by title, body, or author using the supplied search text. </p>
	 *
	 * @param postList    the VBox receiving the post rows
	 * @param threadID    the selected thread ID
	 * @param search      optional post search text
	 * @param theStage    the primary application stage
	 * @param contentPane the main application content pane
	 */
	private static void loadPosts(
			VBox postList,
			int threadID,
			String search,
			Stage theStage,
			BorderPane contentPane) {

		postList.getChildren().clear();

		ArrayList<Post> posts =
			FoundationsMain.database.getPostsForThread(threadID);

		if (posts == null) {
			postList.getChildren().add(
				new Label("Unable to load posts.")
			);
			return;
		}

		for (Post post : posts) {
			if (post == null) {
				continue;
			}

			if (matchesSearch(post, search)) {
				postList.getChildren().add(
					createPostRow(
						post,
						theStage,
						contentPane
					)
				);
			}
		}

		if (postList.getChildren().isEmpty()) {
			postList.getChildren().add(
				new Label("No posts found in this thread.")
			);
		}
	}

	/*******
	 * <p> Method: createPostRow() </p>
	 *
	 * <p> Description: Creates a clickable post preview row resembling the
	 * rows used by PostNavBar. Selecting the row marks the post as read and
	 * opens the full post using PostDisplayPanel. </p>
	 *
	 * @param post        the post being displayed
	 * @param theStage    the primary application stage
	 * @param contentPane the main application content pane
	 * @return a VBox containing the post preview
	 */
	private static VBox createPostRow(
			Post post,
			Stage theStage,
			BorderPane contentPane) {

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

		HBox titleRow = new HBox(10);

		Label titleLabel = new Label(post.getTitle());
		titleLabel.setStyle("-fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		int replyCount =
			FoundationsMain.database.getReplyCountForPost(
				post.getPostID()
			);

		Label replyLabel = new Label(
			replyCount +
			(replyCount == 1 ? " reply" : " replies")
		);

		replyLabel.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		titleRow.getChildren().addAll(
			titleLabel,
			spacer,
			replyLabel
		);

		Label authorLabel = new Label(
			"Posted by: " + safeDisplay(post.getAuthor())
		);

		authorLabel.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		Label bodyPreview = new Label(
			makeSample(post.getBody())
		);

		bodyPreview.setWrapText(true);

		Label createdLabel = new Label(
			"Created: " + post.getCreatedDate()
		);

		createdLabel.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		rBox.setOnMouseClicked(e -> {
			FoundationsMain.database.markPostAsRead(
				FoundationsMain.database.getCurrentUsername(),
				post.getPostID()
			);

			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(
					theStage,
					contentPane,
					post.getPostID()
				)
			);
		});

		rBox.setOnMouseEntered(e -> {
			rBox.setStyle(hoverStyle);
		});

		rBox.setOnMouseExited(e -> {
			rBox.setStyle(normalStyle);
		});

		rBox.getChildren().addAll(
			titleRow,
			authorLabel,
			bodyPreview,
			createdLabel
		);

		return rBox;
	}

	/*******
	 * <p> Method: matchesSearch() </p>
	 *
	 * <p> Description: Returns true when the post title, body, or author
	 * contains the supplied search text. </p>
	 *
	 * @param post   the post being checked
	 * @param search the search text
	 * @return true when the post should be displayed
	 */
	private static boolean matchesSearch(
			Post post,
			String search) {

		if (search == null || search.isBlank()) {
			return true;
		}

		String searchLower = search.trim().toLowerCase();

		return safeLower(post.getTitle()).contains(searchLower)
			|| safeLower(post.getBody()).contains(searchLower)
			|| safeLower(post.getAuthor()).contains(searchLower);
	}

	/*******
	 * <p> Method: makeSample() </p>
	 *
	 * <p> Description: Creates an abbreviated post body preview. </p>
	 *
	 * @param body the complete post body
	 * @return the abbreviated post body
	 */
	private static String makeSample(String body) {

		if (body == null) {
			return "";
		}

		if (body.length() <= 120) {
			return body;
		}

		return body.substring(0, 120) + "...";
	}

	/**
	 * Safely converts a nullable string to lowercase.
	 *
	 * @param value the original string
	 * @return the lowercase value or an empty string
	 */
	private static String safeLower(String value) {

		if (value == null) {
			return "";
		}

		return value.toLowerCase();
	}

	/**
	 * Safely prepares a nullable value for display.
	 *
	 * @param value the original value
	 * @return the original value or "Unknown"
	 */
	private static String safeDisplay(String value) {

		if (value == null || value.isBlank()) {
			return "Unknown";
		}

		return value;
	}
}