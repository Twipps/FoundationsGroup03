package entityClasses;

import java.sql.Timestamp;

/**
 * <p>Title: Post Class</p>
 *
 * <p>Description: Class that stores the information associated with a post so it can be displayed and manipulated by the GUI..</p>
 *
 * @author James Suchovic (Team 03)
 */

public class Post {
	private int postID;
	private String title;
	private String body;
	private String author;
	private String category;
	
	private Timestamp createdDate;
	private Timestamp modifiedDate;
	
	/**
	 * Creates a new post before it is inserted into the database.
	 *
	 * @param inTitle the title of the post
	 * @param inBody the body text of the post
	 * @param inCategory the category assigned to the post
	 * @param inAuthor the author who created the post
	 */
	public Post (String inTitle, String inBody, String inCategory, String inAuthor){
		postID = -1;
		title = inTitle;
		body = inBody;
		author = inAuthor;
		category = inCategory;
		createdDate = new Timestamp(System.currentTimeMillis());
		modifiedDate = null;
	}
	
	/**
	 * Creates a post object from the database.
	 *
	 * @param inPostID the unique database identifier for the post
	 * @param inTitle the title of the post
	 * @param inBody the body text of the post
	 * @param inCategory the category assigned to the post
	 * @param inAuthor the author who created the post
	 * @param inCreatedDate the timestamp showing when the post was created
	 * @param inModifiedDate the timestamp showing when the post was last modified
	 */
	public Post(int inPostID, String inTitle, String inBody, String inCategory, String inAuthor,
			Timestamp inCreatedDate, Timestamp inModifiedDate) {	
		postID = inPostID;
		title = inTitle;
		body = inBody;
		category = inCategory;
		author = inAuthor;
		createdDate = inCreatedDate;
		modifiedDate = inModifiedDate;
	}
	
	/**
	 * Updates the title of this post and records the modification time.
	 *
	 * @param inTitle the new title for the post
	 */
	public void updateTitle(String inTitle){
		title = inTitle;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};
	
	/**
	 * Updates the body text of this post and records the modification time.
	 *
	 * @param inBody the new body text for the post
	 */
	public void updateBody(String inBody) {
		body = inBody;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};

	/**
	 * Updates the category of this post and records the modification time.
	 *
	 * @param inCategory the new category for the post
	 */
	public void updateCategory(String inCategory) {
		category = inCategory;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};
		
	/**
	 * Gets the unique database identifier for this post.
	 *
	 * @return the post ID, or -1 if the post has not been inserted into the database
	 */ 
	public int getPostID() {return postID;}
	/**
	 * Gets the title of this post.
	 *
	 * @return the post title
	 */
	public String getTitle() {return title;}
	/**
	 * Gets the body text of this post.
	 *
	 * @return the post body text
	 */
	public String getBody() {return body;}
	/**
	 * Gets the author of this post.
	 *
	 * @return the post author
	 */
	public String getAuthor() {return author;}
	/**
	 * Gets the category assigned to this post.
	 *
	 * @return the post category
	 */
	public String getCategory() {return category;}
	/**
	 * Gets the timestamp when this post was created.
	 *
	 * @return the post creation timestamp
	 */
	public Timestamp getCreatedDate() {return createdDate;}
	/**
	 * Gets the timestamp when this post was last modified.
	 *
	 * @return the post modification timestamp, or null if the post has not been modified
	 */
	public Timestamp getModifiedDate() {return modifiedDate;}
}