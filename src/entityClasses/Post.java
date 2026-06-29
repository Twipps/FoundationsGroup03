package postComponents;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import database.Database;

/****
 * @author James Suchovic (Team 03) - Original implementation
 *
 * <p>Description: Represents a post in memory for display to the GUI<p>
 */

public class Post {
	private int postID;
	private String title;
	private String body;
	private String author;
	private String category;
	
	private Timestamp createdDate;
	private Timestamp modifiedDate;
	
	// create before insertion
	public Post (String inTitle, String inBody, String inCategory, String inAuthor){
		postID = -1;
		title = inTitle;
		body = inBody;
		author = inAuthor;
		category = inCategory;
		createdDate = new Timestamp(System.currentTimeMillis());
		modifiedDate = null;
	}
	
	// constructor for database pull
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
	
	//modifiers Update
	public void updateTitle(String inTitle){
		title = inTitle;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};
	
	public void updateBody(String inBody) {
		body = inBody;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};

	public void updateCategory(String inCategory) {
		category = inCategory;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	};
		
	// getters Read new 
	public int getPostID() {return postID;}
	public String getTitle() {return title;}
	public String getBody() {return body;}
	public String getAuthor() {return author;}
	public String getCategory() {return category;}
	public Timestamp getCreatedDate() {return createdDate;}
	public Timestamp modifiedDate() {return modifiedDate;}
}