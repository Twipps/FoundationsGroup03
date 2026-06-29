package postComponents;

import java.util.ArrayList;

import database.Database;

/****
 * @author James Suchovic (Team 03) - Original implementation
 *
 * <p>Description: Builds a list of replies</p>
 */

public class ReplyList {

    private ArrayList<Reply> replyList;

    private static Database theDatabase =
            applicationMain.FoundationsMain.database;

    public ReplyList() {
        replyList = theDatabase.getAllReplies();
    }

    // builds new list
    public void refreshList() {
        replyList = theDatabase.getAllReplies();
    }

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
    
    public ArrayList<Reply> getRepliesForPost(int postID) {
    	ArrayList<Reply> matches = new ArrayList<>();

    	for (Reply reply : replyList) {
    		if (reply.getParentPostID() == postID) {
    			matches.add(reply);
    		}
    	}

    	return matches;
    }

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

    public void deleteReply(int replyID) {
        theDatabase.deleteReply(replyID);
    }
}