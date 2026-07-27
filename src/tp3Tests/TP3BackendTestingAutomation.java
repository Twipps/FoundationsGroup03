package tp3Tests;

import java.util.ArrayList;
import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.Thread;

/*******
 * <p> Title: TP3BackendTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test suite covering every new backend
 * method added to the Student Discussion System for TP3. This suite
 * validates: Staff Thread CRUD support (getPostsForThread), Soft Delete for
 * Posts, Staff Statistics engagement tracking (distinct-student reply
 * counting), and Read/Unread Tracking at both the post and reply level. </p>
 *
 * <p> Each test method is documented with the specific TP3 method it
 * validates, the input used, the expected output, and how to interpret the
 * console output to determine pass/fail — following the same format as
 * StudentPostTestingAutomation.java and TP2BoundaryValueTests.java from
 * TP2/HW3, per the requirement that the Javadoc be consistent with the
 * style used across the Foundations codebase. </p>
 *
 * <p> This class creates its own database connection independent of the
 * running application, following the same pattern as
 * TP2BoundaryValueTests.java, so it can be run standalone without the GUI. </p>
 *
 * <p> How to run: Right-click this file → Run As → Java Application. Each
 * test prints PASS or FAIL to the console with an explanation. A fully
 * passing run shows 0 failures. </p>
 *
 * @author Kyle Kim (Team 3) — Test design, implementation, and documentation
 * for all TP3 backend additions
 *
 * @version 1.00  2026-07-26  Initial implementation for TP3
 */
public class TP3BackendTestingAutomation {

	/** Running count of tests that passed. */
	private static int numPassed = 0;

	/** Running count of tests that failed. */
	private static int numFailed = 0;

	/** Private constructor — this is a static utility class, not meant to be instantiated. */
	private TP3BackendTestingAutomation() {}

	/**
	 * Direct database reference for testing. Creates its own connection
	 * independent of the running app, same pattern as TP2BoundaryValueTests.
	 */
	private static Database db = new Database();

	static {
		try {
			db.connectToDatabase();
		} catch (Exception e) {
			System.err.println("Failed to connect to database: " + e.getMessage());
		}
	}

	/**
	 * The threadID of the "General" thread, resolved once at class load,
	 * since it is guaranteed to always exist (seeded by createTables()).
	 * Required because addPost() takes threadID as a parameter following
	 * the TP3 threadID foreign key migration.
	 */
	private static final int GENERAL_THREAD_ID = resolveGeneralThreadID();

	private static int resolveGeneralThreadID() {
		Thread general = db.getThreadByTitle("General");
		return (general != null) ? general.getThreadID() : -1;
	}

	// =========================================================================
	// MAIN
	// =========================================================================

	/*******
	 * <p> Method: main() </p>
	 *
	 * <p> Description: Entry point for the TP3 backend test suite. Runs all
	 * test groups in sequence and prints a pass/fail summary. </p>
	 *
	 * @param args command line arguments (not used)
	 */
	public static void main(String[] args) {
		System.out.println("================================================");
		System.out.println("  TP3 Backend Testing Automation — Team 3");
		System.out.println("  Author: Kyle Kim");
		System.out.println("================================================\n");

		System.out.println("--- getPostsForThread() — Staff Thread CRUD support ---");
		testGetPostsForThread_returnsCorrectPosts();
		testGetPostsForThread_excludesOtherThreads();
		testGetPostsForThread_nonexistentThread_returnsEmpty();

		System.out.println("\n--- Soft Delete for Posts ---");
		testSoftDeletePost_excludedFromFilteredList();
		testSoftDeletePost_stillRetrievableDirectly();
		testSoftDeletePost_repliesPreserved();

		System.out.println("\n--- Post-level Read/Unread Tracking ---");
		testMarkPostAsRead_reflectedInHasUserReadPost();
		testHasUserReadPost_falseForUnreadPost();
		testMarkPostAsRead_isIdempotent();

		System.out.println("\n--- Reply-level Read/Unread Tracking ---");
		testUnreadReplyCount_beforeReading();
		testUnreadReplyCount_decrementsAsRepliesRead();
		testGetRepliesReceivedByUser_allVsUnreadOnly();
		testMarkReplyAsRead_isIdempotent();

		System.out.println("\n--- Staff Statistics: Distinct-Student Engagement ---");
		testDistinctStudentsRepliedTo_countsUniqueAuthorsOnly();
		testDistinctStudentsRepliedTo_excludesSelfReplies();
		testHasMetReplyEngagementRequirement_thresholdBehavior();

		System.out.println("\n================================================");
		System.out.println("  Tests passed: " + numPassed);
		System.out.println("  Tests failed: " + numFailed);
		System.out.println("================================================");
	}

	// =========================================================================
	// getPostsForThread() TESTS
	// =========================================================================

	/*******
	 * <p> Method: testGetPostsForThread_returnsCorrectPosts() </p>
	 *
	 * <p> Validates: Database.getPostsForThread(int threadID) </p>
	 *
	 * <p> Description: Creates a new thread with 2 posts in it and verifies
	 * getPostsForThread returns exactly those 2 posts. </p>
	 *
	 * <p> Input: A new thread with 2 posts added to it. </p>
	 * <p> Expected output: getPostsForThread returns a list of size 2. </p>
	 */
	private static void testGetPostsForThread_returnsCorrectPosts() {
		db.createThread("TP3Test_ThreadA_" + System.currentTimeMillis(), "body", "testUser", null);
		Thread thread = db.getThreadByTitle(
			db.getAllThreads().get(db.getAllThreads().size() - 1).getTitle());
		int threadID = thread.getThreadID();

		db.addPost("Post 1", "body", "testUser", "General", threadID);
		db.addPost("Post 2", "body", "testUser", "General", threadID);

		ArrayList<Post> posts = db.getPostsForThread(threadID);
		boolean passed = posts.size() == 2;
		printResult("TP3-01 getPostsForThread_returnsCorrectPosts", passed,
			"getPostsForThread returns exactly the posts belonging to the specified thread (found "
			+ posts.size() + ", expected 2).");
	}

	/*******
	 * <p> Method: testGetPostsForThread_excludesOtherThreads() </p>
	 *
	 * <p> Validates: Database.getPostsForThread(int threadID) </p>
	 *
	 * <p> Description: Creates two separate threads, each with one post, and
	 * verifies that querying one thread does not return the other thread's post. </p>
	 *
	 * <p> Input: Two threads, one post each. </p>
	 * <p> Expected output: getPostsForThread(threadA) does not include the post from threadB. </p>
	 */
	private static void testGetPostsForThread_excludesOtherThreads() {
		String suffix = String.valueOf(System.currentTimeMillis());
		db.createThread("TP3Test_ThreadB_" + suffix, "body", "testUser", null);
		db.createThread("TP3Test_ThreadC_" + suffix, "body", "testUser", null);

		Thread threadB = db.getThreadByTitle("TP3Test_ThreadB_" + suffix);
		Thread threadC = db.getThreadByTitle("TP3Test_ThreadC_" + suffix);

		db.addPost("Post in B", "body", "testUser", "General", threadB.getThreadID());
		db.addPost("Post in C", "body", "testUser", "General", threadC.getThreadID());

		ArrayList<Post> postsInB = db.getPostsForThread(threadB.getThreadID());
		boolean passed = postsInB.stream().noneMatch(p -> p.getTitle().equals("Post in C"));
		printResult("TP3-02 getPostsForThread_excludesOtherThreads", passed,
			"getPostsForThread correctly excludes posts belonging to a different thread.");
	}

	/*******
	 * <p> Method: testGetPostsForThread_nonexistentThread_returnsEmpty() </p>
	 *
	 * <p> Validates: Database.getPostsForThread(int threadID) </p>
	 *
	 * <p> Description: Calls getPostsForThread with a threadID that does not
	 * exist. Verifies an empty list is returned rather than null or an exception. </p>
	 *
	 * <p> Input: threadID=999999 (non-existent) </p>
	 * <p> Expected output: An empty (non-null) list. </p>
	 */
	private static void testGetPostsForThread_nonexistentThread_returnsEmpty() {
		ArrayList<Post> posts = db.getPostsForThread(999999);
		boolean passed = posts != null && posts.isEmpty();
		printResult("TP3-03 getPostsForThread_nonexistentThread_returnsEmpty", passed,
			"getPostsForThread returns an empty list (not null, no exception) for a nonexistent threadID.");
	}

	// =========================================================================
	// SOFT DELETE FOR POSTS TESTS
	// =========================================================================

	/*******
	 * <p> Method: testSoftDeletePost_excludedFromFilteredList() </p>
	 *
	 * <p> Validates: Database.softDeletePost(int postID) </p>
	 *
	 * <p> Description: Creates a post, soft-deletes it, then verifies it no
	 * longer appears in getAllPosts() (which filters isDeleted=FALSE). </p>
	 *
	 * <p> Input: A post, then softDeletePost() called on it. </p>
	 * <p> Expected output: The post is absent from getAllPosts(). </p>
	 */
	private static void testSoftDeletePost_excludedFromFilteredList() {
		db.addPost("SoftDeleteTest_A", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> before = db.getAllPosts();
		int postID = before.get(before.size() - 1).getPostID();

		db.softDeletePost(postID);

		ArrayList<Post> after = db.getAllPosts();
		boolean passed = after.stream().noneMatch(p -> p.getPostID() == postID);
		printResult("TP3-04 softDeletePost_excludedFromFilteredList", passed,
			"Soft-deleted post is correctly excluded from the filtered getAllPosts() browse view.");
	}

	/*******
	 * <p> Method: testSoftDeletePost_stillRetrievableDirectly() </p>
	 *
	 * <p> Validates: Database.softDeletePost(int postID), Database.getPost(int postID) </p>
	 *
	 * <p> Description: Confirms a soft-deleted post is still retrievable via
	 * the direct getPost() lookup, proving the row was not hard-deleted. </p>
	 *
	 * <p> Input: A post, soft-deleted. </p>
	 * <p> Expected output: getPost() still returns the post (non-null). </p>
	 */
	private static void testSoftDeletePost_stillRetrievableDirectly() {
		db.addPost("SoftDeleteTest_B", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.softDeletePost(postID);

		Post retrieved = db.getPost(postID);
		boolean passed = retrieved != null && retrieved.getTitle().equals("SoftDeleteTest_B");
		printResult("TP3-05 softDeletePost_stillRetrievableDirectly", passed,
			"Soft-deleted post is still retrievable via direct getPost() — confirms soft delete, not hard delete.");
	}

	/*******
	 * <p> Method: testSoftDeletePost_repliesPreserved() </p>
	 *
	 * <p> Validates: Database.softDeletePost(int postID), Database.getRepliesForPost(int postID) </p>
	 *
	 * <p> Description: Creates a post with 2 replies, soft-deletes the post,
	 * and verifies both replies remain fully intact — satisfying the
	 * requirement that deleting a post does not delete its replies. </p>
	 *
	 * <p> Input: A post with 2 replies, then softDeletePost() called. </p>
	 * <p> Expected output: getRepliesForPost() still returns both replies. </p>
	 */
	private static void testSoftDeletePost_repliesPreserved() {
		db.addPost("SoftDeleteTest_C", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "Reply 1", "testUser");
		db.addReply(postID, "Reply 2", "testUser");

		db.softDeletePost(postID);

		ArrayList<Reply> replies = db.getRepliesForPost(postID);
		boolean passed = replies.size() == 2;
		printResult("TP3-06 softDeletePost_repliesPreserved", passed,
			"Replies remain fully intact after their parent post is soft-deleted (found "
			+ replies.size() + ", expected 2).");
	}

	// =========================================================================
	// POST-LEVEL READ/UNREAD TRACKING TESTS
	// =========================================================================

	/*******
	 * <p> Method: testMarkPostAsRead_reflectedInHasUserReadPost() </p>
	 *
	 * <p> Validates: Database.markPostAsRead(String, int), Database.hasUserReadPost(String, int) </p>
	 *
	 * <p> Description: Marks a post as read for a test user and verifies
	 * hasUserReadPost immediately reflects the change. </p>
	 *
	 * <p> Input: A post, marked read by "readTestUser". </p>
	 * <p> Expected output: hasUserReadPost returns true for that user/post pair. </p>
	 */
	private static void testMarkPostAsRead_reflectedInHasUserReadPost() {
		db.addPost("ReadTrackingTest_A", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.markPostAsRead("readTestUser", postID);

		boolean passed = db.hasUserReadPost("readTestUser", postID);
		printResult("TP3-07 markPostAsRead_reflectedInHasUserReadPost", passed,
			"markPostAsRead is correctly reflected by an immediate hasUserReadPost check.");
	}

	/*******
	 * <p> Method: testHasUserReadPost_falseForUnreadPost() </p>
	 *
	 * <p> Validates: Database.hasUserReadPost(String, int) </p>
	 *
	 * <p> Description: Creates a post that has never been marked as read by
	 * a given user, and verifies hasUserReadPost correctly returns false. </p>
	 *
	 * <p> Input: A newly created, unread post. </p>
	 * <p> Expected output: hasUserReadPost returns false. </p>
	 */
	private static void testHasUserReadPost_falseForUnreadPost() {
		db.addPost("ReadTrackingTest_B", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		boolean hasRead = db.hasUserReadPost("neverReadThisUser", postID);
		printResult("TP3-08 hasUserReadPost_falseForUnreadPost", !hasRead,
			"hasUserReadPost correctly returns false for a post that has never been marked read.");
	}

	/*******
	 * <p> Method: testMarkPostAsRead_isIdempotent() </p>
	 *
	 * <p> Validates: Database.markPostAsRead(String, int) </p>
	 *
	 * <p> Description: Calls markPostAsRead twice on the same user/post pair
	 * and verifies no exception is thrown (the underlying MERGE statement
	 * must handle the duplicate key gracefully). </p>
	 *
	 * <p> Input: The same post, marked as read twice by the same user. </p>
	 * <p> Expected output: No exception thrown on the second call. </p>
	 */
	private static void testMarkPostAsRead_isIdempotent() {
		db.addPost("ReadTrackingTest_C", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		boolean passed = true;
		try {
			db.markPostAsRead("idempotentTestUser", postID);
			db.markPostAsRead("idempotentTestUser", postID); // repeat — must not throw
		} catch (Exception e) {
			passed = false;
		}
		printResult("TP3-09 markPostAsRead_isIdempotent", passed,
			"markPostAsRead can be safely called multiple times without throwing an exception.");
	}

	// =========================================================================
	// REPLY-LEVEL READ/UNREAD TRACKING TESTS
	// =========================================================================

	/*******
	 * <p> Method: testUnreadReplyCount_beforeReading() </p>
	 *
	 * <p> Validates: Database.getUnreadReplyCountForPost(String, int) </p>
	 *
	 * <p> Description: Creates a post with 3 replies from other students and
	 * verifies the unread count for the post's author is 3 before any
	 * replies have been marked read. </p>
	 *
	 * <p> Input: A post with 3 replies, none yet marked read. </p>
	 * <p> Expected output: getUnreadReplyCountForPost returns 3. </p>
	 */
	private static void testUnreadReplyCount_beforeReading() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String author = "replyCountAuthor_" + suffix;
		db.addPost("UnreadCountTest_A", "body", author, "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "r1", "studentX");
		db.addReply(postID, "r2", "studentY");
		db.addReply(postID, "r3", "studentZ");

		int unread = db.getUnreadReplyCountForPost(author, postID);
		printResult("TP3-10 unreadReplyCount_beforeReading", unread == 3,
			"getUnreadReplyCountForPost correctly returns 3 before any replies are marked read (got "
			+ unread + ").");
	}

	/*******
	 * <p> Method: testUnreadReplyCount_decrementsAsRepliesRead() </p>
	 *
	 * <p> Validates: Database.getUnreadReplyCountForPost(String, int), Database.markReplyAsRead(String, int) </p>
	 *
	 * <p> Description: Marks one of three replies as read and verifies the
	 * unread count decrements from 3 to 2. </p>
	 *
	 * <p> Input: A post with 3 replies; 1 marked read. </p>
	 * <p> Expected output: getUnreadReplyCountForPost returns 2. </p>
	 */
	private static void testUnreadReplyCount_decrementsAsRepliesRead() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String author = "replyCountAuthor2_" + suffix;
		db.addPost("UnreadCountTest_B", "body", author, "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "r1", "studentX");
		db.addReply(postID, "r2", "studentY");
		db.addReply(postID, "r3", "studentZ");

		ArrayList<Reply> replies = db.getRepliesForPost(postID);
		db.markReplyAsRead(author, replies.get(0).getReplyID());

		int unread = db.getUnreadReplyCountForPost(author, postID);
		printResult("TP3-11 unreadReplyCount_decrementsAsRepliesRead", unread == 2,
			"getUnreadReplyCountForPost correctly decrements to 2 after marking 1 of 3 replies as read (got "
			+ unread + ").");
	}

	/*******
	 * <p> Method: testGetRepliesReceivedByUser_allVsUnreadOnly() </p>
	 *
	 * <p> Validates: Database.getRepliesReceivedByUser(String, boolean) </p>
	 *
	 * <p> Description: Verifies that calling getRepliesReceivedByUser with
	 * unreadOnly=false returns all replies received, while unreadOnly=true
	 * correctly excludes replies already marked as read. </p>
	 *
	 * <p> Input: A post with 2 replies; 1 marked read. </p>
	 * <p> Expected output: unreadOnly=false returns 2; unreadOnly=true returns 1. </p>
	 */
	private static void testGetRepliesReceivedByUser_allVsUnreadOnly() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String author = "receivedRepliesAuthor_" + suffix;
		db.addPost("ReceivedRepliesTest", "body", author, "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "r1", "studentX");
		db.addReply(postID, "r2", "studentY");

		ArrayList<Reply> replies = db.getRepliesForPost(postID);
		db.markReplyAsRead(author, replies.get(0).getReplyID());

		ArrayList<Reply> all = db.getRepliesReceivedByUser(author, false);
		ArrayList<Reply> unreadOnly = db.getRepliesReceivedByUser(author, true);

		boolean passed = all.size() == 2 && unreadOnly.size() == 1;
		printResult("TP3-12 getRepliesReceivedByUser_allVsUnreadOnly", passed,
			"getRepliesReceivedByUser returns 2 for unreadOnly=false and 1 for unreadOnly=true (got "
			+ all.size() + " and " + unreadOnly.size() + ").");
	}

	/*******
	 * <p> Method: testMarkReplyAsRead_isIdempotent() </p>
	 *
	 * <p> Validates: Database.markReplyAsRead(String, int) </p>
	 *
	 * <p> Description: Calls markReplyAsRead twice on the same user/reply
	 * pair and verifies no exception is thrown. </p>
	 *
	 * <p> Input: The same reply, marked as read twice by the same user. </p>
	 * <p> Expected output: No exception thrown on the second call. </p>
	 */
	private static void testMarkReplyAsRead_isIdempotent() {
		db.addPost("IdempotentReplyTest", "body", "testUser", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();
		db.addReply(postID, "reply", "studentX");
		ArrayList<Reply> replies = db.getRepliesForPost(postID);
		int replyID = replies.get(0).getReplyID();

		boolean passed = true;
		try {
			db.markReplyAsRead("idempotentReplyUser", replyID);
			db.markReplyAsRead("idempotentReplyUser", replyID); // repeat — must not throw
		} catch (Exception e) {
			passed = false;
		}
		printResult("TP3-13 markReplyAsRead_isIdempotent", passed,
			"markReplyAsRead can be safely called multiple times without throwing an exception.");
	}

	// =========================================================================
	// STAFF STATISTICS: DISTINCT-STUDENT ENGAGEMENT TESTS
	// =========================================================================

	/*******
	 * <p> Method: testDistinctStudentsRepliedTo_countsUniqueAuthorsOnly() </p>
	 *
	 * <p> Validates: Database.getDistinctStudentsRepliedTo(String) </p>
	 *
	 * <p> Description: A student replies twice to the same classmate's post
	 * and once to a second classmate. Verifies the distinct count is 2, not
	 * 3 — proving repeated replies to the same person are not double-counted. </p>
	 *
	 * <p> Input: 2 replies to studentA's post, 1 reply to studentB's post. </p>
	 * <p> Expected output: getDistinctStudentsRepliedTo returns 2. </p>
	 */
	private static void testDistinctStudentsRepliedTo_countsUniqueAuthorsOnly() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String repliedByUser = "engagementUser_" + suffix;
		String studentA = "studentA_" + suffix;
		String studentB = "studentB_" + suffix;

		db.addPost("Post by A", "body", studentA, "General", GENERAL_THREAD_ID);
		db.addPost("Post by B", "body", studentB, "General", GENERAL_THREAD_ID);

		ArrayList<Post> posts = db.getAllPosts();
		int postA = 0, postB = 0;
		for (Post p : posts) {
			if (p.getAuthor().equals(studentA)) postA = p.getPostID();
			if (p.getAuthor().equals(studentB)) postB = p.getPostID();
		}

		db.addReply(postA, "reply 1", repliedByUser);
		db.addReply(postA, "reply 2 (same student)", repliedByUser);
		db.addReply(postB, "reply 3", repliedByUser);

		int distinctCount = db.getDistinctStudentsRepliedTo(repliedByUser);
		printResult("TP3-14 distinctStudentsRepliedTo_countsUniqueAuthorsOnly", distinctCount == 2,
			"getDistinctStudentsRepliedTo correctly counts 2 distinct students, not 3, despite 2 "
			+ "replies going to the same student (got " + distinctCount + ").");
	}

	/*******
	 * <p> Method: testDistinctStudentsRepliedTo_excludesSelfReplies() </p>
	 *
	 * <p> Validates: Database.getDistinctStudentsRepliedTo(String) </p>
	 *
	 * <p> Description: A student replies to their own post. Verifies this
	 * does not count toward their distinct-student engagement total. </p>
	 *
	 * <p> Input: A user replying to their own post. </p>
	 * <p> Expected output: getDistinctStudentsRepliedTo returns 0. </p>
	 */
	private static void testDistinctStudentsRepliedTo_excludesSelfReplies() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String user = "selfReplyUser_" + suffix;

		db.addPost("My own post", "body", user, "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "replying to myself", user);

		int distinctCount = db.getDistinctStudentsRepliedTo(user);
		printResult("TP3-15 distinctStudentsRepliedTo_excludesSelfReplies", distinctCount == 0,
			"getDistinctStudentsRepliedTo correctly excludes replies to the user's own posts (got "
			+ distinctCount + ", expected 0).");
	}

	/*******
	 * <p> Method: testHasMetReplyEngagementRequirement_thresholdBehavior() </p>
	 *
	 * <p> Validates: Database.hasMetReplyEngagementRequirement(String) </p>
	 *
	 * <p> Description: A student replies to exactly 2 distinct classmates
	 * (below the 3-student threshold), then replies to a 3rd. Verifies the
	 * boolean flips from false to true at the correct boundary. </p>
	 *
	 * <p> Input: Replies to 2 distinct students, then a 3rd. </p>
	 * <p> Expected output: false after 2 distinct students; true after 3. </p>
	 */
	private static void testHasMetReplyEngagementRequirement_thresholdBehavior() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String user = "thresholdUser_" + suffix;
		String s1 = "thresholdStudent1_" + suffix;
		String s2 = "thresholdStudent2_" + suffix;
		String s3 = "thresholdStudent3_" + suffix;

		db.addPost("Post 1", "body", s1, "General", GENERAL_THREAD_ID);
		db.addPost("Post 2", "body", s2, "General", GENERAL_THREAD_ID);
		db.addPost("Post 3", "body", s3, "General", GENERAL_THREAD_ID);

		ArrayList<Post> posts = db.getAllPosts();
		int post1 = 0, post2 = 0, post3 = 0;
		for (Post p : posts) {
			if (p.getAuthor().equals(s1)) post1 = p.getPostID();
			if (p.getAuthor().equals(s2)) post2 = p.getPostID();
			if (p.getAuthor().equals(s3)) post3 = p.getPostID();
		}

		db.addReply(post1, "reply", user);
		db.addReply(post2, "reply", user);
		boolean metBefore = db.hasMetReplyEngagementRequirement(user);

		db.addReply(post3, "reply", user);
		boolean metAfter = db.hasMetReplyEngagementRequirement(user);

		boolean passed = !metBefore && metAfter;
		printResult("TP3-16 hasMetReplyEngagementRequirement_thresholdBehavior", passed,
			"hasMetReplyEngagementRequirement correctly returns false at 2 distinct students and "
			+ "true at 3 (got before=" + metBefore + ", after=" + metAfter + ").");
	}

	// =========================================================================
	// HELPER
	// =========================================================================

	/*******
	 * <p> Method: printResult() </p>
	 *
	 * <p> Description: Prints a formatted PASS or FAIL result to the console
	 * and increments the appropriate counter. Used by every test method to
	 * produce consistent output. </p>
	 *
	 * @param testName    the name of the test being reported
	 * @param passed      true if the test passed, false if it failed
	 * @param explanation a brief description of what was verified
	 */
	private static void printResult(String testName, boolean passed, String explanation) {
		if (passed) {
			System.out.println("  *** PASS ***  " + testName);
			numPassed++;
		} else {
			System.out.println("  *** FAIL ***  " + testName);
			numFailed++;
		}
		System.out.println("               " + explanation);
	}
}