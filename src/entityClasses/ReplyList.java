package entityClasses;

import java.util.ArrayList;

import database.Database;

/**
 * <p>Title: ReplyList Class</p>
 *
 * <p>Description: Class that manages a collection of Reply objects loaded from the
 * database. Provides operations for refreshing, retrieving, creating, and deleting
 * replies used by the student discussion GUI.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class ReplyList {

    private ArrayList<Reply> replyList;

    private static Database theDatabase =
            applicationMain.FoundationsMain.database;

    /**
     * Creates a ReplyList by loading all current replies from the database.
     */
    public ReplyList() {
        replyList = theDatabase.getAllReplies();
    }

    /**
     * Reloads the reply list from the database.
     */
    public void refreshList() {
        replyList = theDatabase.getAllReplies();
    }

    /**
     * Searches the current reply list for a reply with the given reply ID.
     *
     * @param replyID the unique ID of the reply to find
     * @return the matching Reply object, or null if no reply with that ID exists
     */
    public Reply getReply(int replyID) {
        Reply rReply = null;
        boolean found = false;
        int i = 0;

        while (!found && i != replyList.size()) {

            if (replyList.get(i).getReplyID() == replyID) {
                rReply = replyList.get(i);
                found = true;
            }

            i++;
        }

        return rReply;
    }
    
    /**
     * Gets all replies associated with the specified parent post.
     *
     * @param postID the unique identifier of the parent post
     * @return a list containing all replies that belong to the specified post
     */
    public ArrayList<Reply> getRepliesForPost(int postID) {
    	ArrayList<Reply> matches = new ArrayList<>();

    	for (Reply reply : replyList) {
    		if (reply.getParentPostID() == postID) {
    			matches.add(reply);
    		}
    	}

    	return matches;
    }

    /**
     * Creates a new reply and stores it in the database.
     *
     * @param parentPostID the unique identifier of the parent post
     * @param body the body text of the reply
     * @param author the author who created the reply
     *
     * @throws IllegalArgumentException if the reply body is null or empty
     */
    public void createReply(int parentPostID, String body, String author) {

    	if (body == null || body.trim().isEmpty()) {
    		throw new IllegalArgumentException(
    			"Reply body cannot be empty.");
    	}

    	theDatabase.addReply(
    		parentPostID,
    		body.trim(),
    		author
    	);

    	refreshList();
    }

    /**
     * Deletes the reply with the given reply ID from the database.
     *
     * @param replyID the unique identifier of the reply to delete
     */
    public void deleteReply(int replyID) {
        theDatabase.deleteReply(replyID);
    }
}