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
	
	// little archaic, but i'm creating this to create dynamic arrays that change what's on and what's in a post nav button.
	public ArrayList<Post> getPostList() { return postList; }
	
	public void deletePost(int postID) { theDatabase.deletePost(postID); }
}