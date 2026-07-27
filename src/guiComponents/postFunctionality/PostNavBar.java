package guiComponents.postFunctionality;

import java.util.ArrayList;

import entityClasses.Post;
import entityClasses.PostList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: PostNavBar Class </p>
 *
 * <p> Description: A static utility class that builds the left-side navigation
 * panel for the Student Discussion System. Provides a Create Post button,
 * a keyword search bar, a category filter dropdown, and a scrollable list of
 * all posts that match the current search and filter criteria. </p>
 *
 * <p> This class satisfies the following Students User Stories: </p>
 * <p> - REQ-01: "As a student, I can post statements and questions" — the
 *   Create Post button navigates to PostReplyEditPanel for new post creation. </p>
 * <p> - REQ-03: "As a student, I can see a list of posts others have made" —
 *   the post list shows all posts with title, category, and body preview. </p>
 * <p> - REQ-12: "As a student, I can post to different threads" — the category
 *   filter ComboBox lets students filter posts by thread (General, Question,
 *   Bug, Help). </p>
 * <p> - REQ-13: "As a student, I can search for posts with keywords" — the
 *   search bar filters the post list in real time by title, body, or category.
 *   If no thread is specified, all threads are searched. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to PostList and Database. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented post navigation,
 * search, filtering, and post list display
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added User Story mappings and inline REQ comments
 * 
 * TODO: Edit categories to be threadTitles instead; edit posts database to have a threadID
 */
public class PostNavBar {

	/** Cached post list — refreshed on each filterPosts() call to reflect
	 *  the latest database state. */
	private static PostList allPosts = new PostList();

	/**
	 * Prevents creation of PostNavBar objects because this class only
	 * provides static GUI helper methods.
	 */
	private PostNavBar() {}

	/*******
	 * <p> Method: createPostNavBar() </p>
	 *
	 * <p> Description: Builds and returns the full post navigation bar VBox.
	 * Contains a Create Post button, search bar, category filter, and a
	 * scrollable list of posts filtered by the current search and category. </p>
	 *
	 * <p> Satisfies REQ-01, REQ-03, REQ-12, REQ-13. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @return a VBox containing the post navigation controls
	 */
	public static VBox createPostNavBar(Stage theStage, BorderPane contentPane) {
		VBox rBox = new VBox(10);

		// REQ-01: Create Post button navigates to the post creation panel
		Button createPost = new Button("Create Post");
		createPost.setMaxWidth(Double.MAX_VALUE);

		createPost.setOnAction(e -> {
			// REQ-01: postID=-1 signals PostReplyEditPanel to create a new post.
			// TP3: createPostEditPanel now also requires an initial threadID
			// (schema migration added threadID as a real FK on posts) — default
			// to General since "Create Post" has no thread context of its own.
			entityClasses.Thread generalThread =
				applicationMain.FoundationsMain.database.getThreadByTitle("General");
			int defaultThreadID = (generalThread != null) ? generalThread.getThreadID() : -1;
			contentPane.setCenter(
				PostReplyEditPanel.createPostEditPanel(theStage, contentPane, -1, defaultThreadID)
			);
		});

		// TP3: Manual refresh button — reloads the post list (and reply counts)
		// from the database without requiring navigation away and back
		Button refresh = new Button("Refresh");
		refresh.setMaxWidth(Double.MAX_VALUE);

		HBox searchStuff = new HBox(10);

		// REQ-13: Search bar filters post list in real time by keyword
		TextField searchBar = new TextField();
		searchBar.setPromptText("Search posts...");

		// REQ-12: Category filter dropdown — "All" shows all threads
		ComboBox<String> categoryFilter = new ComboBox<String>();
		categoryFilter.getItems().add("All");
		categoryFilter.getItems().add("General");
		categoryFilter.getItems().add("Question");
		categoryFilter.getItems().add("Bug");
		categoryFilter.getItems().add("Help");
		categoryFilter.setValue("All"); // default to showing all threads

		searchStuff.getChildren().addAll(searchBar, categoryFilter);

		VBox postList = new VBox(8);
		ScrollPane scrollPane = new ScrollPane(postList);
		scrollPane.setFitToWidth(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		// Initial population of the post list with no filter applied
		filterPosts(postList, searchBar.getText(), categoryFilter.getValue(), contentPane, theStage);

		// TP3: Refresh button re-runs the current search/filter to reload from the database
		refresh.setOnAction(e -> {
			filterPosts(postList, searchBar.getText(), categoryFilter.getValue(), contentPane, theStage);
		});

		// REQ-13: Update post list in real time as the user types in the search bar
		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			filterPosts(postList, newValue, categoryFilter.getValue(), contentPane, theStage);
		});

		// REQ-12: Update post list when the category filter selection changes
		categoryFilter.setOnAction(e -> {
			filterPosts(postList, searchBar.getText(), categoryFilter.getValue(), contentPane, theStage);
		});

		rBox.getChildren().addAll(createPost, refresh, searchStuff, scrollPane);
		rBox.setMaxWidth(300);

		return rBox;
	}

	/*******
	 * <p> Method: filterPosts() </p>
	 *
	 * <p> Description: Refreshes the post list VBox with all posts that match
	 * both the keyword search and the selected category filter. Clears the
	 * existing list and repopulates it on each call. Shows "No posts found."
	 * if no posts match the current criteria. </p>
	 *
	 * <p> Satisfies REQ-12 (thread filter) and REQ-13 (keyword search). </p>
	 *
	 * @param postList    the VBox that contains the displayed post rows
	 * @param search      the search keyword — blank means show all posts (REQ-13)
	 * @param category    the selected category — "All" means show all threads (REQ-12)
	 * @param contentPane the main content pane used by the post interface
	 * @param theStage    the primary application stage
	 */
	public static void filterPosts(VBox postList, String search, String category,
			BorderPane contentPane, Stage theStage) {
		postList.getChildren().clear();

		// Refresh from database to catch any new posts added in this session
		allPosts.refreshList();

		ArrayList<Post> posts = allPosts.getPostList();

		for (int i = 0; i < posts.size(); i++) {
			Post currentPost = posts.get(i);

			// REQ-12, REQ-13: Only show post if it matches both search AND category filter
			if (matchesSearch(currentPost, search) && matchesCategory(currentPost, category)) {
				postList.getChildren().add(createPostRow(currentPost, contentPane, theStage));
			}
		}

		// Show feedback message if no posts match the current filter
		if (postList.getChildren().size() == 0) {
			postList.getChildren().add(new Label("No posts found."));
		}
	}

	/*******
	 * <p> Method: matchesSearch() </p>
	 *
	 * <p> Description: Returns true if the given post's title, body, or category
	 * contains the search keyword (case-insensitive). Returns true for a blank
	 * or null search query, meaning all posts are shown when the search bar is
	 * empty. </p>
	 *
	 * <p> Satisfies REQ-13: "As a student, I can search for posts with keywords
	 * that match a search keyword that I specify. If I do not specify a thread,
	 * all threads are searched." </p>
	 *
	 * @param post   the post being checked
	 * @param search the search keyword (may be null or blank)
	 * @return true if the post matches the search, false otherwise
	 */
	public static boolean matchesSearch(Post post, String search) {
		// REQ-13: blank search shows all posts
		if (search == null || search.isBlank()) {
			return true;
		}

		String searchLower = search.toLowerCase();

		// REQ-13: search matches title, body, or category (case-insensitive)
		return safeLower(post.getTitle()).contains(searchLower)
			|| safeLower(post.getBody()).contains(searchLower)
			|| safeLower(post.getCategory()).contains(searchLower);
	}

	/*******
	 * <p> Method: matchesCategory() </p>
	 *
	 * <p> Description: Returns true if the post's category matches the selected
	 * filter (case-insensitive). Returns true when the filter is null or "All",
	 * meaning all posts are shown when no specific thread is selected. </p>
	 *
	 * <p> Satisfies REQ-12: "As a student, I can post to different threads.
	 * If I do not specify a thread, it defaults to the General thread." </p>
	 *
	 * @param post     the post being checked
	 * @param category the selected category filter (may be null or "All")
	 * @return true if the post matches the filter, false otherwise
	 */
	public static boolean matchesCategory(Post post, String category) {
		// REQ-12: "All" filter shows every post regardless of thread
		if (category == null || category.compareTo("All") == 0) {
			return true;
		}

		// REQ-12: case-insensitive category match
		return safeLower(post.getCategory()).compareTo(category.toLowerCase()) == 0;
	}

	/*******
	 * <p> Method: createPostRow() </p>
	 *
	 * <p> Description: Builds and returns a clickable row for a single post
	 * in the navigation list. Shows the post title, category tag, and a
	 * truncated body preview. Clicking the row loads the full post in
	 * PostDisplayPanel. </p>
	 *
	 * <p> Satisfies REQ-03: "As a student, I can see a list of posts others
	 * have made that might be related to things that are important to me." </p>
	 *
	 * @param post        the post represented by the row
	 * @param contentPane the main content pane used by the post interface
	 * @param theStage    the primary application stage
	 * @return a VBox containing the post title, category tag, and body preview
	 */
	public static VBox createPostRow(Post post, BorderPane contentPane, Stage theStage) {
		VBox rBox = new VBox(5);
		rBox.setPadding(new Insets(8));
		rBox.setStyle("-fx-border-color: lightgray;");

		String currentUser = applicationMain.FoundationsMain.database.getCurrentUsername();
		// TP3: Read/unread tracking — REQ: "I can see which [posts] I have
		// read and which I have not."
		boolean isRead = applicationMain.FoundationsMain.database.hasUserReadPost(currentUser, post.getPostID());

		HBox titleRow = new HBox(10);

		// TP3: Unread posts show a bold title with a small blue dot indicator;
		// read posts show a normal-weight title. This mirrors the pattern most
		// students already expect from email/forum unread indicators.
		Label title = new Label((isRead ? "" : "\u25CF ") + post.getTitle()); // REQ-03: show post title
		title.setStyle(isRead ? "" : "-fx-font-weight: bold;");
		if (!isRead) {
			title.setTextFill(javafx.scene.paint.Color.web("#2266cc"));
		}

		Label category = new Label("[" + post.getCategory() + "]"); // REQ-12: show thread

		// TP3: Show reply count on each post row
		int replyCount = applicationMain.FoundationsMain.database.getReplyCountForPost(post.getPostID());
		Label replyLabel = new Label(replyCount + (replyCount == 1 ? " reply" : " replies"));
		replyLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

		titleRow.getChildren().addAll(title, category, replyLabel);

		// TP3: Show unread reply count on any post with replies. Satisfies
		// both halves of the Student User Stories: "I can see a list of my
		// posts... how many of [replies] I have not yet read" AND "I can see
		// a list of posts from others... the number [of replies] that I have
		// not read." Not restricted to the current user's own posts —
		// getUnreadReplyCountForPost() is scoped by viewing username, not
		// post authorship, so this works correctly for any post.
		if (replyCount > 0) {
			int unreadReplies = applicationMain.FoundationsMain.database
				.getUnreadReplyCountForPost(currentUser, post.getPostID());
			if (unreadReplies > 0) {
				Label unreadRepliesLabel = new Label("(" + unreadReplies + " unread)");
				unreadRepliesLabel.setStyle("-fx-text-fill: #cc3333; -fx-font-size: 11px; -fx-font-weight: bold;");
				titleRow.getChildren().add(unreadRepliesLabel);
			}
		}

		// REQ-03: Show a truncated preview of the post body (max 80 chars)
		Label sampleString = new Label(makeSample(post.getBody()));
		sampleString.setWrapText(true);

		// REQ-04: Clicking the row loads the full post in PostDisplayPanel
		// TP3: Mark post as read when clicked, and immediately refresh the
		// left nav bar so the unread indicator clears right away instead of
		// requiring navigation away and back to see the updated read state
		rBox.setOnMouseClicked(e -> {
			applicationMain.FoundationsMain.database.markPostAsRead(currentUser, post.getPostID());
			contentPane.setCenter(PostDisplayPanel.createPostDisplayPanel(
				theStage, contentPane, post.getPostID()
			));
			contentPane.setLeft(createPostNavBar(theStage, contentPane));
		});

		rBox.getChildren().addAll(titleRow, sampleString);
		return rBox;
	}

	/*******
	 * <p> Method: makeSample() </p>
	 *
	 * <p> Description: Returns a preview of the post body for display in the
	 * post list. Returns the full body if 80 characters or fewer, otherwise
	 * truncates to 80 characters and appends "...". Returns an empty string
	 * for a null body. </p>
	 *
	 * @param body the post body string (may be null)
	 * @return a preview string of at most 83 characters
	 */
	public static String makeSample(String body) {
		if (body == null) {
			return ""; // REQ-09: handle null body gracefully
		}

		if (body.length() <= 80) {
			return body;
		}

		return body.substring(0, 80) + "...";
	}

	/*******
	 * <p> Method: safeLower() </p>
	 *
	 * <p> Description: Returns the lowercase version of the given string, or
	 * an empty string if the input is null. Prevents NullPointerExceptions
	 * during search and category comparisons. </p>
	 *
	 * @param value the string to convert (may be null)
	 * @return lowercase string, or "" if null
	 */
	public static String safeLower(String value) {
		if (value == null) {
			return ""; // REQ-09: handle null values without crashing
		}

		return value.toLowerCase();
	}
}