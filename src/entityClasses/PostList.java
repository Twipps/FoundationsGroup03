package entityClasses;

import java.util.ArrayList;

import database.Database;

/**
 * <p>Title: PostList Class</p>
 *
 * <p>Description: Class that manages a collection of Post objects loaded from the
 * database. Provides operations for refreshing, retrieving, and deleting posts
 * used by the student discussion GUI.</p>
 *
 * @author James Suchovic (Team 03)
 */
public class PostList {
	
	private ArrayList<Post> postList;
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Creates a PostList by loading all current posts from the database.
	 */
	public PostList () { postList = theDatabase.getAllPosts(); }
	
	/**
	 * Reloads the post list from the database.
	 */
	public void refreshList() { postList = theDatabase.getAllPosts(); }
	
	/**
	 * Searches the current post list for a post with the given post ID.
	 *
	 * @param postID the unique ID of the post to find
	 * @return the matching post, or null if no post with that ID exists
	 */
	public Post getPost(int postID) {
		Post rPost = null;
		boolean found = false;
		int i = 0;
		
		while (!found && i != postList.size()) {
			if (postList.get(i).getPostID() == postID) {
				rPost = postList.get(i);
				found = true;
			}
			
			i++;
		}
		
		return rPost;
	}
	
	/**
	 * Gets the current list of posts.
	 *
	 * @return the list of posts currently loaded from the database
	 */
	public ArrayList<Post> getPostList() { return postList; }
	
	/**
	 * Deletes the post with the given post ID from the database.
	 *
	 * @param postID the unique ID of the post to delete
	 */
	public void deletePost(int postID) { theDatabase.deletePost(postID); }
}