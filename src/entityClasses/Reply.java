package entityClasses;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * <p>Title: Reply Class</p>
 *
 * <p>Description: Class that stores the information associated with a reply so it can be displayed and manipulated by the GUI.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class Reply {
	private int replyID;
	private int parentPostID;

	private String body;
	private String author;

	private Timestamp createdDate;
	private Timestamp modifiedDate;

	/**
	 * Creates a new reply before it is inserted into the database.
	 *
	 * @param inParentPostID the ID of the parent post
	 * @param inBody the body text of the reply
	 * @param inAuthor the author who created the reply
	 */
	public Reply(int inParentPostID, String inBody, String inAuthor) {
		replyID = -1;
		parentPostID = inParentPostID;

		body = inBody;
		author = inAuthor;

		createdDate = new Timestamp(System.currentTimeMillis());
		modifiedDate = null;
	}

	/**
	 * Creates a reply object from an existing database record.
	 *
	 * @param inReplyID the unique database identifier for the reply
	 * @param inParentPostID the unique identifier of the parent post
	 * @param inBody the body text of the reply
	 * @param inAuthor the author who created the reply
	 * @param inCreatedDate the timestamp showing when the reply was created
	 * @param inModifiedDate the timestamp showing when the reply was last modified
	 */
	public Reply(int inReplyID, int inParentPostID, String inBody, String inAuthor,
			Timestamp inCreatedDate, Timestamp inModifiedDate) {

		replyID = inReplyID;
		parentPostID = inParentPostID;

		body = inBody;
		author = inAuthor;

		createdDate = inCreatedDate;
		modifiedDate = inModifiedDate;
	}

	/**
	 * Updates the body text of this reply and records the modification time.
	 *
	 * @param inBody the new body text for the reply
	 */
	public void updateBody(String inBody) {
		body = inBody;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Gets the unique database identifier for this reply.
	 *
	 * @return the reply ID, or -1 if the reply has not been inserted into the database
	 */
	public int getReplyID() {return replyID;}
	/**
	 * Gets the unique identifier of the parent post.
	 *
	 * @return the parent post ID
	 */
	public int getParentPostID() {return parentPostID;}
	
	/**
	 * Gets the body text of this reply.
	 *
	 * @return the reply body text
	 */
	public String getBody() {return body;}
	/**
	 * Gets the author of this reply.
	 *
	 * @return the reply author
	 */
	public String getAuthor() {return author;}

	/**
	 * Gets the timestamp when this reply was created.
	 *
	 * @return the reply creation timestamp
	 */
	public Timestamp getCreatedDate() {return createdDate;}
	/**
	 * Gets the timestamp when this reply was last modified.
	 *
	 * @return the reply modification timestamp, or null if the reply has not been modified
	 */
	public Timestamp getModifiedDate() {return modifiedDate;}
}