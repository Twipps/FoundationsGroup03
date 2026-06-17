package postComponents;

import java.util.ArrayList;

import database.Database;

/****
 * @author James Suchovic (Team 03) - Original implementation
 *
 * <p>Description: Builds a list of posts</p>
 */

public class PostList {
	
	private ArrayList<Post> postList;
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	public PostList () { postList = theDatabase.getAllPosts(); }
	
	// builds new list
	public void refreshList() { postList = theDatabase.getAllPosts(); }
	
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
	
	public void deletePost(int postID) { theDatabase.deletePost(postID); }
}