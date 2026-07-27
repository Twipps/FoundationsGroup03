package tp3Tests;

import java.util.ArrayList;
import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.Post;
import entityClasses.Thread;

/*******
 * <p> Title: EvaluationParameterTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test suite covering the Evaluation
 * Parameter CRUD system added for TP3 — the Staff Epic: "As a staff member,
 * I can create, read, update, and delete a set of parameters used to
 * support reviewing the discussions of each student." </p>
 *
 * <p> Covers persisted CRUD on EvaluationParameters (create, read by staff
 * owner, update, delete, all correctly scoped to the owning staff member),
 * and the four metric calculation methods used to evaluate a student
 * against a parameter's threshold: post count, reply count, thread
 * participation count, and distinct-students-engaged count — each tested
 * both unscoped (any thread) and scoped to a single thread. </p>
 *
 * <p> This class creates its own database connection independent of the
 * running application, following the same pattern as
 * TP2BoundaryValueTests.java and TP3BackendTestingAutomation.java. </p>
 *
 * <p> How to run: Right-click this file → Run As → Java Application. Each
 * test prints PASS or FAIL to the console with an explanation. A fully
 * passing run shows 0 failures. </p>
 *
 * @author Kyle Kim (Team 3) — Test design, implementation, and documentation,
 * validating the EvaluationParameter backend implemented by James Suchovic (Team 3)
 *
 * @version 1.00  2026-07-26  Initial implementation for TP3
 */
public class EvaluationParameterTestingAutomation {

	/** Running count of tests that passed. */
	private static int numPassed = 0;

	/** Running count of tests that failed. */
	private static int numFailed = 0;

	/** Private constructor — static utility class, not meant to be instantiated. */
	private EvaluationParameterTestingAutomation() {}

	/**
	 * Direct database reference for testing, independent of the running app.
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
	 * used for thread-scoped test cases and for addPost() calls, which
	 * require a valid threadID under the TP3 schema.
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
	 * <p> Description: Entry point for the Evaluation Parameter test suite.
	 * Runs all test groups in sequence and prints a pass/fail summary. </p>
	 *
	 * @param args command line arguments (not used)
	 */
	public static void main(String[] args) {
		System.out.println("================================================");
		System.out.println("  Evaluation Parameter Testing Automation — Team 3");
		System.out.println("  Author: Kyle Kim");
		System.out.println("================================================\n");

		System.out.println("--- EvaluationParameter CRUD ---");
		testAddEvaluationParameter_persistsCorrectly();
		testGetEvaluationParametersForStaff_scopedToOwner();
		testUpdateEvaluationParameter_persistsChanges();
		testUpdateEvaluationParameter_cannotUpdateAnotherStaffMembersParameter();
		testDeleteEvaluationParameter_removesRow();
		testDeleteEvaluationParameter_cannotDeleteAnotherStaffMembersParameter();
		testAddEvaluationParameter_nullThreadIDMeansUnscoped();

		System.out.println("\n--- Metric Calculation: Post Count ---");
		testGetStudentPostCount_unscoped();
		testGetStudentPostCount_scopedToThreadExcludesOtherThreads();

		System.out.println("\n--- Metric Calculation: Reply Count ---");
		testGetStudentReplyCount_unscoped();
		testGetStudentReplyCount_scopedToThreadExcludesOtherThreads();

		System.out.println("\n--- Metric Calculation: Thread Participation ---");
		testGetStudentThreadParticipationCount_countsPostsAndReplies();
		testGetStudentThreadParticipationCount_distinctNotDuplicated();

		System.out.println("\n--- Metric Calculation: Distinct Students Engaged ---");
		testGetDistinctStudentsEngagedCount_unscopedMatchesExistingMethod();
		testGetDistinctStudentsEngagedCount_scopedToThreadExcludesOtherThreads();

		System.out.println("\n================================================");
		System.out.println("  Tests passed: " + numPassed);
		System.out.println("  Tests failed: " + numFailed);
		System.out.println("================================================");
	}

	// =========================================================================
	// EVALUATION PARAMETER CRUD TESTS
	// =========================================================================

	/*******
	 * <p> Method: testAddEvaluationParameter_persistsCorrectly() </p>
	 *
	 * <p> Validates: Database.addEvaluationParameter(...) </p>
	 *
	 * <p> Description: Creates a new evaluation parameter and verifies it is
	 * retrievable afterward via getEvaluationParametersForStaff, with all
	 * fields matching what was inserted. </p>
	 *
	 * <p> Input: A parameter — "Minimum Posts", metric POST_COUNT, operator
	 * GREATER_THAN_OR_EQUAL, threshold 3. </p>
	 * <p> Expected output: The parameter is retrievable with matching fields. </p>
	 */
	private static void testAddEvaluationParameter_persistsCorrectly() {
		String staff = "evalTestStaff_" + System.currentTimeMillis();
		boolean added = db.addEvaluationParameter(
			staff, "Minimum Posts", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3,
			"Students should post at least 3 times.", null, true);

		ArrayList<EvaluationParameter> params = db.getEvaluationParametersForStaff(staff);
		boolean passed = added && params.size() == 1
			&& params.get(0).getName().equals("Minimum Posts")
			&& params.get(0).getMetric().equals("POST_COUNT")
			&& params.get(0).getThreshold() == 3;
		printResult("EP-01 addEvaluationParameter_persistsCorrectly", passed,
			"A newly added evaluation parameter is retrievable afterward with all fields intact.");
	}

	/*******
	 * <p> Method: testGetEvaluationParametersForStaff_scopedToOwner() </p>
	 *
	 * <p> Validates: Database.getEvaluationParametersForStaff(String) </p>
	 *
	 * <p> Description: Two different staff members each create a parameter.
	 * Verifies that querying one staff member's parameters does not return
	 * the other staff member's parameter. </p>
	 *
	 * <p> Input: Two staff users, one parameter each. </p>
	 * <p> Expected output: Each staff member's query returns only their own parameter. </p>
	 */
	private static void testGetEvaluationParametersForStaff_scopedToOwner() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String staffA = "evalStaffA_" + suffix;
		String staffB = "evalStaffB_" + suffix;

		db.addEvaluationParameter(staffA, "Staff A Param", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 2, "", null, true);
		db.addEvaluationParameter(staffB, "Staff B Param", "REPLY_COUNT", "GREATER_THAN_OR_EQUAL", 2, "", null, true);

		ArrayList<EvaluationParameter> staffAParams = db.getEvaluationParametersForStaff(staffA);
		boolean passed = staffAParams.size() == 1
			&& staffAParams.get(0).getName().equals("Staff A Param");
		printResult("EP-02 getEvaluationParametersForStaff_scopedToOwner", passed,
			"getEvaluationParametersForStaff correctly returns only the querying staff member's own parameters.");
	}

	/*******
	 * <p> Method: testUpdateEvaluationParameter_persistsChanges() </p>
	 *
	 * <p> Validates: Database.updateEvaluationParameter(...) </p>
	 *
	 * <p> Description: Creates a parameter, updates its threshold, and
	 * verifies the change is persisted on the next read. </p>
	 *
	 * <p> Input: A parameter with threshold 3, updated to threshold 5. </p>
	 * <p> Expected output: The re-read parameter shows threshold 5. </p>
	 */
	private static void testUpdateEvaluationParameter_persistsChanges() {
		String staff = "evalUpdateStaff_" + System.currentTimeMillis();
		db.addEvaluationParameter(staff, "Update Test", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3, "", null, true);

		EvaluationParameter created = db.getEvaluationParametersForStaff(staff).get(0);

		boolean updated = db.updateEvaluationParameter(
			created.getParameterID(), staff, "Update Test", "POST_COUNT",
			"GREATER_THAN_OR_EQUAL", 5, "", null, true);

		EvaluationParameter reRead = db.getEvaluationParametersForStaff(staff).get(0);
		boolean passed = updated && reRead.getThreshold() == 5;
		printResult("EP-03 updateEvaluationParameter_persistsChanges", passed,
			"updateEvaluationParameter correctly persists a changed threshold (expected 5, got "
			+ reRead.getThreshold() + ").");
	}

	/*******
	 * <p> Method: testUpdateEvaluationParameter_cannotUpdateAnotherStaffMembersParameter() </p>
	 *
	 * <p> Validates: Database.updateEvaluationParameter(...) — ownership scoping </p>
	 *
	 * <p> Description: Staff member A creates a parameter. Staff member B
	 * attempts to update it by ID. Verifies the update is rejected because
	 * the WHERE clause requires staffUsername to match the owner. </p>
	 *
	 * <p> Input: Parameter owned by staffA; update attempted with staffB. </p>
	 * <p> Expected output: updateEvaluationParameter returns false; original values unchanged. </p>
	 */
	private static void testUpdateEvaluationParameter_cannotUpdateAnotherStaffMembersParameter() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String staffA = "evalOwnerA_" + suffix;
		String staffB = "evalIntruderB_" + suffix;

		db.addEvaluationParameter(staffA, "Owned by A", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3, "", null, true);
		EvaluationParameter created = db.getEvaluationParametersForStaff(staffA).get(0);

		boolean updateResult = db.updateEvaluationParameter(
			created.getParameterID(), staffB, "Hijacked", "POST_COUNT",
			"GREATER_THAN_OR_EQUAL", 99, "", null, true);

		EvaluationParameter unchanged = db.getEvaluationParametersForStaff(staffA).get(0);
		boolean passed = !updateResult && unchanged.getThreshold() == 3
			&& unchanged.getName().equals("Owned by A");
		printResult("EP-04 updateEvaluationParameter_cannotUpdateAnotherStaffMembersParameter", passed,
			"A staff member cannot update another staff member's evaluation parameter — ownership scoping enforced.");
	}

	/*******
	 * <p> Method: testDeleteEvaluationParameter_removesRow() </p>
	 *
	 * <p> Validates: Database.deleteEvaluationParameter(int, String) </p>
	 *
	 * <p> Description: Creates a parameter, deletes it, and verifies it no
	 * longer appears in getEvaluationParametersForStaff. </p>
	 *
	 * <p> Input: A parameter, then deleted by its owner. </p>
	 * <p> Expected output: getEvaluationParametersForStaff no longer returns it. </p>
	 */
	private static void testDeleteEvaluationParameter_removesRow() {
		String staff = "evalDeleteStaff_" + System.currentTimeMillis();
		db.addEvaluationParameter(staff, "To Delete", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3, "", null, true);
		EvaluationParameter created = db.getEvaluationParametersForStaff(staff).get(0);

		boolean deleted = db.deleteEvaluationParameter(created.getParameterID(), staff);

		ArrayList<EvaluationParameter> remaining = db.getEvaluationParametersForStaff(staff);
		boolean passed = deleted && remaining.isEmpty();
		printResult("EP-05 deleteEvaluationParameter_removesRow", passed,
			"deleteEvaluationParameter removes the row; it no longer appears for that staff member.");
	}

	/*******
	 * <p> Method: testDeleteEvaluationParameter_cannotDeleteAnotherStaffMembersParameter() </p>
	 *
	 * <p> Validates: Database.deleteEvaluationParameter(int, String) — ownership scoping </p>
	 *
	 * <p> Description: Staff member A creates a parameter. Staff member B
	 * attempts to delete it by ID. Verifies deletion is rejected and the
	 * parameter still exists for staff member A. </p>
	 *
	 * <p> Input: Parameter owned by staffA; delete attempted with staffB. </p>
	 * <p> Expected output: deleteEvaluationParameter returns false; parameter still exists. </p>
	 */
	private static void testDeleteEvaluationParameter_cannotDeleteAnotherStaffMembersParameter() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String staffA = "evalDeleteOwnerA_" + suffix;
		String staffB = "evalDeleteIntruderB_" + suffix;

		db.addEvaluationParameter(staffA, "Protected Param", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3, "", null, true);
		EvaluationParameter created = db.getEvaluationParametersForStaff(staffA).get(0);

		boolean deleteResult = db.deleteEvaluationParameter(created.getParameterID(), staffB);

		ArrayList<EvaluationParameter> stillThere = db.getEvaluationParametersForStaff(staffA);
		boolean passed = !deleteResult && stillThere.size() == 1;
		printResult("EP-06 deleteEvaluationParameter_cannotDeleteAnotherStaffMembersParameter", passed,
			"A staff member cannot delete another staff member's evaluation parameter — ownership scoping enforced.");
	}

	/*******
	 * <p> Method: testAddEvaluationParameter_nullThreadIDMeansUnscoped() </p>
	 *
	 * <p> Validates: Database.addEvaluationParameter(...), Database.getEvaluationParametersForStaff(String) </p>
	 *
	 * <p> Description: Creates a parameter with a null threadID (no thread
	 * scope) and verifies it round-trips correctly as null rather than
	 * being coerced to 0 — a common JDBC pitfall with nullable int columns. </p>
	 *
	 * <p> Input: A parameter created with threadID=null. </p>
	 * <p> Expected output: getThreadID() on the retrieved parameter is null, not 0. </p>
	 */
	private static void testAddEvaluationParameter_nullThreadIDMeansUnscoped() {
		String staff = "evalNullThreadStaff_" + System.currentTimeMillis();
		db.addEvaluationParameter(staff, "Unscoped Param", "POST_COUNT", "GREATER_THAN_OR_EQUAL", 3, "", null, true);

		EvaluationParameter retrieved = db.getEvaluationParametersForStaff(staff).get(0);
		boolean passed = retrieved.getThreadID() == null;
		printResult("EP-07 addEvaluationParameter_nullThreadIDMeansUnscoped", passed,
			"A parameter created with threadID=null correctly round-trips as null, not 0 "
			+ "(uses getObject(\"threadID\", Integer.class) to avoid the JDBC int-vs-null pitfall).");
	}

	// =========================================================================
	// METRIC CALCULATION: POST COUNT
	// =========================================================================

	/*******
	 * <p> Method: testGetStudentPostCount_unscoped() </p>
	 *
	 * <p> Validates: Database.getStudentPostCount(String, Integer) </p>
	 *
	 * <p> Description: Verifies that calling getStudentPostCount with a null
	 * threadID (unscoped) returns the same result as the existing tested
	 * getPostCountForUser method — confirming the delegation is correct. </p>
	 *
	 * <p> Input: A student with 2 posts, queried with threadID=null. </p>
	 * <p> Expected output: getStudentPostCount(user, null) equals getPostCountForUser(user). </p>
	 */
	private static void testGetStudentPostCount_unscoped() {
		String student = "postCountStudent_" + System.currentTimeMillis();
		db.addPost("Post 1", "body", student, "General", GENERAL_THREAD_ID);
		db.addPost("Post 2", "body", student, "General", GENERAL_THREAD_ID);

		int scoped = db.getStudentPostCount(student, null);
		int existing = db.getPostCountForUser(student);
		boolean passed = scoped == 2 && scoped == existing;
		printResult("EP-08 getStudentPostCount_unscoped", passed,
			"getStudentPostCount(user, null) matches getPostCountForUser(user) — correct delegation (got "
			+ scoped + ", expected 2).");
	}

	/*******
	 * <p> Method: testGetStudentPostCount_scopedToThreadExcludesOtherThreads() </p>
	 *
	 * <p> Validates: Database.getStudentPostCount(String, Integer) </p>
	 *
	 * <p> Description: A student posts once in General and once in a newly
	 * created second thread. Verifies that scoping the count to only the
	 * new thread returns 1, not 2. </p>
	 *
	 * <p> Input: A student with 1 post in General, 1 post in a new thread. </p>
	 * <p> Expected output: getStudentPostCount scoped to the new thread returns 1. </p>
	 */
	private static void testGetStudentPostCount_scopedToThreadExcludesOtherThreads() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String student = "scopedPostStudent_" + suffix;

		db.createThread("EPTest_Thread_" + suffix, "body", "testUser", null);
		Thread newThread = db.getThreadByTitle("EPTest_Thread_" + suffix);

		db.addPost("Post in General", "body", student, "General", GENERAL_THREAD_ID);
		db.addPost("Post in new thread", "body", student, "General", newThread.getThreadID());

		int scopedCount = db.getStudentPostCount(student, newThread.getThreadID());
		printResult("EP-09 getStudentPostCount_scopedToThreadExcludesOtherThreads", scopedCount == 1,
			"getStudentPostCount scoped to a specific thread correctly excludes posts in other threads (got "
			+ scopedCount + ", expected 1).");
	}

	// =========================================================================
	// METRIC CALCULATION: REPLY COUNT
	// =========================================================================

	/*******
	 * <p> Method: testGetStudentReplyCount_unscoped() </p>
	 *
	 * <p> Validates: Database.getStudentReplyCount(String, Integer) </p>
	 *
	 * <p> Description: Verifies getStudentReplyCount with threadID=null
	 * matches the existing tested getReplyCountForUser method. </p>
	 *
	 * <p> Input: A student with 2 replies, queried with threadID=null. </p>
	 * <p> Expected output: getStudentReplyCount(user, null) equals getReplyCountForUser(user). </p>
	 */
	private static void testGetStudentReplyCount_unscoped() {
		String student = "replyCountStudent_" + System.currentTimeMillis();
		db.addPost("Reply target", "body", "someoneElse", "General", GENERAL_THREAD_ID);
		ArrayList<Post> posts = db.getAllPosts();
		int postID = posts.get(posts.size() - 1).getPostID();

		db.addReply(postID, "reply 1", student);
		db.addReply(postID, "reply 2", student);

		int scoped = db.getStudentReplyCount(student, null);
		int existing = db.getReplyCountForUser(student);
		boolean passed = scoped == 2 && scoped == existing;
		printResult("EP-10 getStudentReplyCount_unscoped", passed,
			"getStudentReplyCount(user, null) matches getReplyCountForUser(user) — correct delegation (got "
			+ scoped + ", expected 2).");
	}

	/*******
	 * <p> Method: testGetStudentReplyCount_scopedToThreadExcludesOtherThreads() </p>
	 *
	 * <p> Validates: Database.getStudentReplyCount(String, Integer) </p>
	 *
	 * <p> Description: A student replies to a post in General and to a post
	 * in a new thread. Verifies scoping to the new thread returns 1, not 2. </p>
	 *
	 * <p> Input: A student with 1 reply in General, 1 reply in a new thread. </p>
	 * <p> Expected output: getStudentReplyCount scoped to the new thread returns 1. </p>
	 */
	private static void testGetStudentReplyCount_scopedToThreadExcludesOtherThreads() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String student = "scopedReplyStudent_" + suffix;

		db.createThread("EPTest_ReplyThread_" + suffix, "body", "testUser", null);
		Thread newThread = db.getThreadByTitle("EPTest_ReplyThread_" + suffix);

		db.addPost("Post in General", "body", "someoneElse", "General", GENERAL_THREAD_ID);
		db.addPost("Post in new thread", "body", "someoneElse", "General", newThread.getThreadID());

		ArrayList<Post> posts = db.getAllPosts();
		int postInGeneral = 0, postInNewThread = 0;
		for (Post p : posts) {
			if (p.getTitle().equals("Post in General")) postInGeneral = p.getPostID();
			if (p.getTitle().equals("Post in new thread")) postInNewThread = p.getPostID();
		}

		db.addReply(postInGeneral, "reply in general", student);
		db.addReply(postInNewThread, "reply in new thread", student);

		int scopedCount = db.getStudentReplyCount(student, newThread.getThreadID());
		printResult("EP-11 getStudentReplyCount_scopedToThreadExcludesOtherThreads", scopedCount == 1,
			"getStudentReplyCount scoped to a specific thread correctly excludes replies in other threads (got "
			+ scopedCount + ", expected 1).");
	}

	// =========================================================================
	// METRIC CALCULATION: THREAD PARTICIPATION
	// =========================================================================

	/*******
	 * <p> Method: testGetStudentThreadParticipationCount_countsPostsAndReplies() </p>
	 *
	 * <p> Validates: Database.getStudentThreadParticipationCount(String) </p>
	 *
	 * <p> Description: A student posts in one new thread and replies to a
	 * post in a second, different new thread. Verifies the participation
	 * count is 2, since participation is counted via posting OR replying. </p>
	 *
	 * <p> Input: A student posting in threadA and replying in threadB. </p>
	 * <p> Expected output: getStudentThreadParticipationCount returns 2. </p>
	 */
	private static void testGetStudentThreadParticipationCount_countsPostsAndReplies() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String student = "participationStudent_" + suffix;

		db.createThread("EPTest_ParticipationA_" + suffix, "body", "testUser", null);
		db.createThread("EPTest_ParticipationB_" + suffix, "body", "testUser", null);
		Thread threadA = db.getThreadByTitle("EPTest_ParticipationA_" + suffix);
		Thread threadB = db.getThreadByTitle("EPTest_ParticipationB_" + suffix);

		// Student posts directly in thread A
		db.addPost("My post in A", "body", student, "General", threadA.getThreadID());

		// Someone else posts in thread B; student replies to it
		db.addPost("Post in B", "body", "otherUser", "General", threadB.getThreadID());
		ArrayList<Post> posts = db.getAllPosts();
		int postInB = 0;
		for (Post p : posts) {
			if (p.getTitle().equals("Post in B")) postInB = p.getPostID();
		}
		db.addReply(postInB, "my reply in B", student);

		int participationCount = db.getStudentThreadParticipationCount(student);
		printResult("EP-12 getStudentThreadParticipationCount_countsPostsAndReplies", participationCount == 2,
			"getStudentThreadParticipationCount correctly counts 2 threads — one via posting, one via "
			+ "replying (got " + participationCount + ", expected 2).");
	}

	/*******
	 * <p> Method: testGetStudentThreadParticipationCount_distinctNotDuplicated() </p>
	 *
	 * <p> Validates: Database.getStudentThreadParticipationCount(String) </p>
	 *
	 * <p> Description: A student both posts in a thread AND replies to
	 * another post in that same thread. Verifies the thread is only counted
	 * once, not twice — the UNION in the underlying query must deduplicate. </p>
	 *
	 * <p> Input: A student posting once and replying once, both within the
	 * same single thread. </p>
	 * <p> Expected output: getStudentThreadParticipationCount returns 1, not 2. </p>
	 */
	private static void testGetStudentThreadParticipationCount_distinctNotDuplicated() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String student = "dedupeStudent_" + suffix;

		db.createThread("EPTest_Dedupe_" + suffix, "body", "testUser", null);
		Thread thread = db.getThreadByTitle("EPTest_Dedupe_" + suffix);

		db.addPost("My own post", "body", student, "General", thread.getThreadID());
		db.addPost("Someone else's post", "body", "otherUser", "General", thread.getThreadID());

		ArrayList<Post> posts = db.getAllPosts();
		int othersPostID = 0;
		for (Post p : posts) {
			if (p.getTitle().equals("Someone else's post")) othersPostID = p.getPostID();
		}
		db.addReply(othersPostID, "my reply, same thread", student);

		int participationCount = db.getStudentThreadParticipationCount(student);
		printResult("EP-13 getStudentThreadParticipationCount_distinctNotDuplicated", participationCount == 1,
			"getStudentThreadParticipationCount correctly counts the thread once, not twice, when the "
			+ "student both posted and replied within the same thread (got " + participationCount
			+ ", expected 1).");
	}

	// =========================================================================
	// METRIC CALCULATION: DISTINCT STUDENTS ENGAGED
	// =========================================================================

	/*******
	 * <p> Method: testGetDistinctStudentsEngagedCount_unscopedMatchesExistingMethod() </p>
	 *
	 * <p> Validates: Database.getDistinctStudentsEngagedCount(String, Integer) </p>
	 *
	 * <p> Description: Verifies that calling getDistinctStudentsEngagedCount
	 * with threadID=null returns the same result as the existing, already-
	 * tested getDistinctStudentsRepliedTo method — confirming the unscoped
	 * path correctly delegates rather than reimplementing the logic. </p>
	 *
	 * <p> Input: A student replying to 2 distinct classmates, queried unscoped. </p>
	 * <p> Expected output: getDistinctStudentsEngagedCount(user, null) equals
	 * getDistinctStudentsRepliedTo(user). </p>
	 */
	private static void testGetDistinctStudentsEngagedCount_unscopedMatchesExistingMethod() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String user = "engagedDelegateUser_" + suffix;
		String s1 = "engagedDelegateA_" + suffix;
		String s2 = "engagedDelegateB_" + suffix;

		db.addPost("Post by A", "body", s1, "General", GENERAL_THREAD_ID);
		db.addPost("Post by B", "body", s2, "General", GENERAL_THREAD_ID);

		ArrayList<Post> posts = db.getAllPosts();
		int postA = 0, postB = 0;
		for (Post p : posts) {
			if (p.getAuthor().equals(s1)) postA = p.getPostID();
			if (p.getAuthor().equals(s2)) postB = p.getPostID();
		}

		db.addReply(postA, "reply", user);
		db.addReply(postB, "reply", user);

		int scoped = db.getDistinctStudentsEngagedCount(user, null);
		int existing = db.getDistinctStudentsRepliedTo(user);
		boolean passed = scoped == 2 && scoped == existing;
		printResult("EP-14 getDistinctStudentsEngagedCount_unscopedMatchesExistingMethod", passed,
			"getDistinctStudentsEngagedCount(user, null) matches the existing tested "
			+ "getDistinctStudentsRepliedTo(user) — correct delegation, no reimplementation (got "
			+ scoped + ", expected 2).");
	}

	/*******
	 * <p> Method: testGetDistinctStudentsEngagedCount_scopedToThreadExcludesOtherThreads() </p>
	 *
	 * <p> Validates: Database.getDistinctStudentsEngagedCount(String, Integer) </p>
	 *
	 * <p> Description: A student replies to a classmate's post in a new
	 * thread, and to a different classmate's post in General. Verifies
	 * scoping the engagement count to only the new thread returns 1, not 2. </p>
	 *
	 * <p> Note: unlike the other metric tests in this suite, the two "post
	 * author" usernames here must be registered as real Student users
	 * (via Database.register()) rather than arbitrary strings. This is
	 * because the thread-scoped path of getDistinctStudentsEngagedCount()
	 * joins against userDB and requires newRole1=TRUE on the post author —
	 * a constraint the unscoped path (which delegates to
	 * getDistinctStudentsRepliedTo()) does not apply. An earlier version of
	 * this test used unregistered usernames and failed for exactly this
	 * reason; registering real Student accounts here reflects realistic
	 * app usage, where every post author is already a registered user. </p>
	 *
	 * <p> Input: Replies to 2 distinct registered students across 2 different threads. </p>
	 * <p> Expected output: Scoped to the new thread, the count is 1. </p>
	 */
	private static void testGetDistinctStudentsEngagedCount_scopedToThreadExcludesOtherThreads() {
		String suffix = String.valueOf(System.currentTimeMillis());
		String user = "engagedScopedUser_" + suffix;
		String studentInGeneral = "engagedScopedGeneral_" + suffix;
		String studentInNewThread = "engagedScopedNewThread_" + suffix;

		// Register both post authors as actual Student users — required
		// because the scoped query path joins userDB and checks newRole1
		try {
			db.register(new entityClasses.User(studentInGeneral, "pw", "First", "M", "Last",
				"First", "test@test.com", false, true, false));
			db.register(new entityClasses.User(studentInNewThread, "pw", "First", "M", "Last",
				"First", "test@test.com", false, true, false));
		} catch (Exception e) {
			System.err.println("Setup error registering test students: " + e.getMessage());
		}

		db.createThread("EPTest_EngagementThread_" + suffix, "body", "testUser", null);
		Thread newThread = db.getThreadByTitle("EPTest_EngagementThread_" + suffix);

		db.addPost("Post in General", "body", studentInGeneral, "General", GENERAL_THREAD_ID);
		db.addPost("Post in new thread", "body", studentInNewThread, "General", newThread.getThreadID());

		ArrayList<Post> posts = db.getAllPosts();
		int postInGeneral = 0, postInNewThread = 0;
		for (Post p : posts) {
			if (p.getAuthor().equals(studentInGeneral)) postInGeneral = p.getPostID();
			if (p.getAuthor().equals(studentInNewThread)) postInNewThread = p.getPostID();
		}

		db.addReply(postInGeneral, "reply in general", user);
		db.addReply(postInNewThread, "reply in new thread", user);

		int scopedCount = db.getDistinctStudentsEngagedCount(user, newThread.getThreadID());
		printResult("EP-15 getDistinctStudentsEngagedCount_scopedToThreadExcludesOtherThreads", scopedCount == 1,
			"getDistinctStudentsEngagedCount scoped to a specific thread correctly counts only "
			+ "engagement within that thread, when replied-to authors are registered students (got "
			+ scopedCount + ", expected 1).");
	}

	// =========================================================================
	// HELPER
	// =========================================================================

	/*******
	 * <p> Method: printResult() </p>
	 *
	 * <p> Description: Prints a formatted PASS or FAIL result to the console
	 * and increments the appropriate counter. </p>
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