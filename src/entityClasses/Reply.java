package entityClasses;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/****
 * @author James Suchovic (Team 03) - Original implementation
 *
 * <p>Description: Represents a reply in memory for display to the GUI.</p>
 */

public class Reply {
	private int replyID;
	private int parentPostID;

	private String body;
	private String author;

	private Timestamp createdDate;
	private Timestamp modifiedDate;

	// Create before database insertion
	public Reply(int inParentPostID, String inBody, String inAuthor) {
		replyID = -1;
		parentPostID = inParentPostID;

		body = inBody;
		author = inAuthor;

		createdDate = new Timestamp(System.currentTimeMillis());
		modifiedDate = null;
	}

	// Constructor for database pull
	public Reply(int inReplyID, int inParentPostID, String inBody, String inAuthor,
			Timestamp inCreatedDate, Timestamp inModifiedDate) {

		replyID = inReplyID;
		parentPostID = inParentPostID;

		body = inBody;
		author = inAuthor;

		createdDate = inCreatedDate;
		modifiedDate = inModifiedDate;
	}

	// Modifiers
	public void updateBody(String inBody) {
		body = inBody;
		modifiedDate = new Timestamp(System.currentTimeMillis());
	}

	// Getters
	public int getReplyID() {return replyID;}
	public int getParentPostID() {return parentPostID;}

	public String getBody() {return body;}
	public String getAuthor() {return author;}

	public Timestamp getCreatedDate() {return createdDate;}
	public Timestamp getModifiedDate() {return modifiedDate;}
}