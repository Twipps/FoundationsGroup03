package database;

import java.util.ArrayList;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.Thread;

public class ReplyReadTrackingScratchTest {
	public static void main(String[] args) throws Exception {
		Database db = new Database();
		db.connectToDatabase();

		Thread general = db.getThreadByTitle("General");
		int threadID = general.getThreadID();

		// ── Setup: postAuthor creates a post; two other students reply ─────
		db.addPost("My Post", "body", "postAuthor", "General", threadID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = 0;
		for (Post p : posts) {
			if (p.getAuthor().equals("postAuthor")) postID = p.getPostID();
		}

		db.addReply(postID, "Reply 1", "studentA");
		db.addReply(postID, "Reply 2", "studentB");
		db.addReply(postID, "Reply 3", "studentC");

		ArrayList<Reply> allReplies = db.getRepliesForPost(postID);
		int reply1ID = allReplies.get(0).getReplyID();
		int reply2ID = allReplies.get(1).getReplyID();
		int reply3ID = allReplies.get(2).getReplyID();

		// ── TEST 1: initially, all 3 replies are unread ─────────────────────
		int unreadBefore = db.getUnreadReplyCountForPost("postAuthor", postID);
		System.out.println("TEST 1 - Unread count before reading any: " + unreadBefore
			+ " (expected 3) - " + (unreadBefore == 3 ? "PASS" : "FAIL"));

		// ── TEST 2: mark one reply as read, count should drop to 2 ─────────
		db.markReplyAsRead("postAuthor", reply1ID);
		int unreadAfterOne = db.getUnreadReplyCountForPost("postAuthor", postID);
		System.out.println("TEST 2 - Unread count after reading 1: " + unreadAfterOne
			+ " (expected 2) - " + (unreadAfterOne == 2 ? "PASS" : "FAIL"));

		// ── TEST 3: hasUserReadReply reflects correctly for read vs unread ──
		boolean reply1Read = db.hasUserReadReply("postAuthor", reply1ID);
		boolean reply2Read = db.hasUserReadReply("postAuthor", reply2ID);
		System.out.println("TEST 3 - reply1 read: " + reply1Read + " (expected true) - "
			+ (reply1Read ? "PASS" : "FAIL"));
		System.out.println("         reply2 read: " + reply2Read + " (expected false) - "
			+ (!reply2Read ? "PASS" : "FAIL"));

		// ── TEST 4: getRepliesReceivedByUser(unreadOnly=false) returns all 3 ─
		ArrayList<Reply> allReceived = db.getRepliesReceivedByUser("postAuthor", false);
		System.out.println("TEST 4 - All received replies: " + allReceived.size()
			+ " (expected 3) - " + (allReceived.size() == 3 ? "PASS" : "FAIL"));

		// ── TEST 5: getRepliesReceivedByUser(unreadOnly=true) returns only 2 ─
		ArrayList<Reply> unreadReceived = db.getRepliesReceivedByUser("postAuthor", true);
		System.out.println("TEST 5 - Unread received replies: " + unreadReceived.size()
			+ " (expected 2) - " + (unreadReceived.size() == 2 ? "PASS" : "FAIL"));
		boolean reply1Excluded = unreadReceived.stream().noneMatch(r -> r.getReplyID() == reply1ID);
		System.out.println("         reply1 correctly excluded from unread list: "
			+ (reply1Excluded ? "PASS" : "FAIL"));

		// ── TEST 6: mark remaining 2 as read, unread count should hit 0 ─────
		db.markReplyAsRead("postAuthor", reply2ID);
		db.markReplyAsRead("postAuthor", reply3ID);
		int unreadFinal = db.getUnreadReplyCountForPost("postAuthor", postID);
		System.out.println("TEST 6 - Unread count after reading all 3: " + unreadFinal
			+ " (expected 0) - " + (unreadFinal == 0 ? "PASS" : "FAIL"));

		// ── TEST 7: marking as read is idempotent (safe to call twice) ──────
		boolean idempotentPassed = true;
		try {
			db.markReplyAsRead("postAuthor", reply1ID); // already read — should not error
		} catch (Exception e) {
			idempotentPassed = false;
		}
		System.out.println("TEST 7 - markReplyAsRead is idempotent (no error on repeat): "
			+ (idempotentPassed ? "PASS" : "FAIL"));

		// ── TEST 8: a user with no replies received gets an empty list, not null
		ArrayList<Reply> noneReceived = db.getRepliesReceivedByUser("userWithNoPosts", false);
		System.out.println("TEST 8 - User with no posts gets empty (not null) list: "
			+ (noneReceived != null && noneReceived.size() == 0 ? "PASS" : "FAIL"));

		System.out.println("\n================================================");
		System.out.println("REPLY READ/UNREAD TRACKING VERIFICATION COMPLETE");
		System.out.println("================================================");

		db.closeConnection();
	}
}