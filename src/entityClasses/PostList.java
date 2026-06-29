package entityClasses;

import java.util.ArrayList;

import database.Database;

/*******
 * <p> Title: PostList Class </p>
 *
 * <p> Description: A model class that manages a collection of Post objects
 * loaded from the H2 database. Acts as an in-memory cache of the current
 * post table, providing operations to refresh, retrieve, and delete posts
 * for use by the student discussion GUI components. </p>
 *
 * <p> PostList is used by PostDisplayPanel, PostNavBar, and PostReplyEditPanel
 * to access post data without making repeated direct database calls. The list
 * is refreshed via refreshList() whenever the GUI needs up-to-date data. </p>
 *
 * <p> This class satisfies the following Students User Stories: </p>
 * <p> - REQ-03: "As a student, I can see a list of posts others have made" —
 *   getPostList() returns the full list of posts for display in PostNavBar. </p>
 * <p> - REQ-04: "As a student, I can view a single post with its replies" —
 *   getPost(postID) retrieves a specific post for display in PostDisplayPanel. </p>
 * <p> - REQ-07: "As a student, I can delete one of my posts" —
 *   deletePost(postID) removes a post from the database. Called by
 *   PostDisplayPanel after the student confirms the delete confirmation dialog. </p>
 *
 * <p> This class follows the Model pattern from the Foundations MVC structure,
 * sitting between the View (PostDisplayPanel, PostNavBar) and the database
 * layer (Database.java). </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented PostList as
 * part of the student discussion MVC structure
 *
 * @version 1.00  2026-06-XX  Initial implementation
 * @version 1.01  2026-06-28  Added User Story mappings and inline REQ comments
 */
public class PostList {

	/** In-memory list of Post objects loaded from the database.
	 *  Populated on construction and refreshed via refreshList(). */
	private ArrayList<Post> postList;

	/** Reference to the application database for post retrieval and deletion.
	 *  Shared with the rest of the application via FoundationsMain.database. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/*******
	 * <p> Method: PostList() — Constructor </p>
	 *
	 * <p> Description: Creates a PostList by loading all current posts from
	 * the database. Called by PostNavBar, PostDisplayPanel, and
	 * PostReplyEditPanel when they need access to the post collection. </p>
	 *
	 * <p> Satisfies REQ-03: loads all posts so the post list can be displayed. </p>
	 */
	public PostList() {
		// REQ-03: Load all posts from the database into memory on construction
		postList = theDatabase.getAllPosts();
	}

	/*******
	 * <p> Method: refreshList() </p>
	 *
	 * <p> Description: Reloads the post list from the database, replacing the
	 * current in-memory list with the latest data. Called by PostNavBar's
	 * filterPosts() method each time the search or category filter changes,
	 * ensuring the displayed list is always current. </p>
	 *
	 * <p> Satisfies REQ-03 and REQ-13: ensures the post list stays up to date
	 * as new posts are created or existing ones are deleted. </p>
	 */
	public void refreshList() {
		// REQ-03, REQ-13: Reload from database to reflect latest post state
		postList = theDatabase.getAllPosts();
	}

	/*******
	 * <p> Method: getPost() </p>
	 *
	 * <p> Description: Searches the current in-memory post list for a post
	 * with the given postID. Returns the matching Post object, or null if no
	 * post with that ID exists in the current list. </p>
	 *
	 * <p> Satisfies REQ-04: used by PostDisplayPanel to retrieve the specific
	 * post the student clicked on in the navigation list. Also used by
	 * PostReplyEditPanel to pre-populate the edit form with existing post data
	 * (REQ-05). </p>
	 *
	 * @param postID the unique ID of the post to find
	 * @return the matching Post object, or null if not found
	 */
	public Post getPost(int postID) {
		Post rPost = null;
		boolean found = false;
		int i = 0;

		// REQ-04: Search the in-memory list for the post with the matching ID
		while (!found && i != postList.size()) {
			if (postList.get(i).getPostID() == postID) {
				rPost = postList.get(i);
				found = true; // stop searching once found
			}
			i++;
		}

		return rPost; // returns null if no post with this ID exists
	}

	/*******
	 * <p> Method: getPostList() </p>
	 *
	 * <p> Description: Returns the full in-memory list of Post objects.
	 * Used by PostNavBar.filterPosts() to iterate over all posts and apply
	 * search and category filters. </p>
	 *
	 * <p> Satisfies REQ-03: provides the complete post collection for display
	 * in the post navigation list. </p>
	 *
	 * @return the ArrayList of Post objects currently loaded from the database
	 */
	public ArrayList<Post> getPostList() {
		return postList; // REQ-03: used by PostNavBar to display the post list
	}

	/*******
	 * <p> Method: deletePost() </p>
	 *
	 * <p> Description: Deletes the post with the given postID from the database
	 * by delegating to Database.deletePost(). Called by PostDisplayPanel after
	 * the student confirms the "Are you sure?" delete confirmation dialog. </p>
	 *
	 * <p> Satisfies REQ-07: "As a student, I can delete one of my posts.
	 * When I do this, I receive an 'Are you sure?' question before the delete
	 * takes place." — the confirmation dialog is shown by PostDisplayPanel
	 * before this method is called. </p>
	 *
	 * @param postID the unique ID of the post to delete
	 */
	public void deletePost(int postID) {
		// REQ-07: Delete the post from the database — confirmation already shown by PostDisplayPanel
		theDatabase.deletePost(postID);
	}
}