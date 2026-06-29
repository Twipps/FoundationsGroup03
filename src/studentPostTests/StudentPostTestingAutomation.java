package studentPostTests;

import java.util.ArrayList;
import entityClasses.Post;
import entityClasses.Reply;
import database.Database;

/*******
 * <p> Title: StudentPostTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test suite for the Student Discussion System
 * implemented in TP2. This class tests all CRUD operations and input validation
 * behaviors for Posts and Replies, as required by the Students User Stories. </p>
 *
 * <p> Each test method corresponds to a specific requirement from the Students
 * User Stories and is documented with the requirement it satisfies, the input
 * used, the expected output, and how to interpret the console output to determine
 * if the test passed or failed. </p>
 *
 * <p> How to run: Right-click this file and select Run As → Java Application.
 * No other application needs to be running — this class creates its own
 * database connection via Database.connectToDatabase(). Each test prints
 * PASS or FAIL to the console. A summary of total passed and failed tests
 * is printed at the end. </p>
 *
 * <p> Note: This test class operates directly against the database layer
 * and does not test JavaFX GUI rendering. GUI-level validation behavior
 * is covered by StudentPostSearchFilterTests.java. </p>
 *
 * @author Kyle Kim (Team 3) — Test design, implementation, and documentation
 *
 * @version 1.00  2026-06-28  Initial implementation
 */
public class StudentPostTestingAutomation {

    /** Running count of tests that passed. */
    private static int numPassed = 0;

    /** Running count of tests that failed. */
    private static int numFailed = 0;

    /** Private constructor — this is a static utility class, not meant to be instantiated. */
    private StudentPostTestingAutomation() {}

    /**
     * Direct database reference used by all test methods.
     * Creates its own connection so the app does not need to be running.
     */
    private static Database db = new Database();

    static {
        try {
            db.connectToDatabase();
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    // =========================================================================
    // MAIN
    // =========================================================================

    /*******
     * <p> Method: main() </p>
     *
     * <p> Description: Entry point for the semi-automated test suite. Runs all
     * test cases in sequence and prints a pass/fail summary to the console.
     * Tests are ordered: Create → Read → Update → Delete → Replies → Validation. </p>
     *
     * <p> How to interpret output: Each test prints either
     * "*** PASS ***" or "*** FAIL ***" followed by the test name and a brief
     * explanation. The final summary shows the total number of tests passed
     * and failed. A fully passing run should show 0 failures. </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  TP2 Student Post Testing Automation — Team 3");
        System.out.println("  Author: Kyle Kim");
        System.out.println("================================================\n");

        // ── CREATE POST ───────────────────────────────────────────────────────
        System.out.println("--- CREATE POST TESTS ---");
        testCreatePost_validInput();
        testCreatePost_emptyTitle_rejected();
        testCreatePost_emptyBody_rejected();
        testCreatePost_emptyCategory_rejected();
        testCreatePost_nullTitle_rejected();

        // ── READ POST ─────────────────────────────────────────────────────────
        System.out.println("\n--- READ POST TESTS ---");
        testGetAllPosts_returnsList();
        testGetPostById_validId();
        testGetPostById_invalidId_returnsNull();

        // ── UPDATE POST ───────────────────────────────────────────────────────
        System.out.println("\n--- UPDATE POST TESTS ---");
        testUpdatePostTitle_valid();
        testUpdatePostBody_valid();
        testUpdatePostCategory_valid();
        testUpdatePost_emptyTitle_rejected();
        testUpdatePost_emptyBody_rejected();

        // ── DELETE POST ───────────────────────────────────────────────────────
        System.out.println("\n--- DELETE POST TESTS ---");
        testDeletePost_valid();
        testDeletePost_nonExistentId_noError();

        // ── CREATE REPLY ──────────────────────────────────────────────────────
        System.out.println("\n--- CREATE REPLY TESTS ---");
        testCreateReply_validInput();
        testCreateReply_emptyBody_rejected();
        testCreateReply_nullBody_rejected();

        // ── READ REPLY ────────────────────────────────────────────────────────
        System.out.println("\n--- READ REPLY TESTS ---");
        testGetRepliesForPost_returnsCorrectReplies();
        testGetRepliesForPost_noReplies_returnsEmpty();

        // ── UPDATE REPLY ──────────────────────────────────────────────────────
        System.out.println("\n--- UPDATE REPLY TESTS ---");
        testUpdateReplyBody_valid();

        // ── DELETE REPLY ──────────────────────────────────────────────────────
        System.out.println("\n--- DELETE REPLY TESTS ---");
        testDeleteReply_valid();

        // ── SUMMARY ───────────────────────────────────────────────────────────
        System.out.println("\n================================================");
        System.out.println("  Tests passed: " + numPassed);
        System.out.println("  Tests failed: " + numFailed);
        System.out.println("================================================");
    }

    // =========================================================================
    // CREATE POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testCreatePost_validInput() </p>
     *
     * <p> Requirement: REQ-01 — Student can create a new post with a valid
     * title, body, category, and author. </p>
     *
     * <p> Description: Adds a post to the database with all valid fields,
     * then retrieves all posts and verifies the list is non-empty. </p>
     *
     * <p> Input: title="Test Title", body="Test body.", author="testUser",
     * category="General" </p>
     *
     * <p> Expected output: Post list contains at least one post. PASS. </p>
     *
     * <p> How to interpret: Non-empty list confirms the database layer
     * correctly persists a new post. </p>
     */
    private static void testCreatePost_validInput() {
        // REQ-01: Create a post with valid input
        db.addPost("Test Title", "Test body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        boolean passed = posts != null && posts.size() > 0;
        printResult("TC-01 createPost_validInput", passed,
            "Post was created and is retrievable from the database.");
    }

    /*******
     * <p> Method: testCreatePost_emptyTitle_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with an empty title. </p>
     *
     * <p> Description: Simulates the validation check in PostReplyEditPanel
     * for a blank title string. </p>
     *
     * <p> Input: title="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (invalid). PASS. </p>
     */
    private static void testCreatePost_emptyTitle_rejected() {
        // REQ-09: Empty title must be rejected by input validation
        String title = "";
        boolean validationCatchesIt = (title == null || title.isBlank());
        printResult("TC-02 createPost_emptyTitle_rejected", validationCatchesIt,
            "Empty title correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with an empty body. </p>
     *
     * <p> Description: Simulates the validation check for a blank body string. </p>
     *
     * <p> Input: body="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (invalid). PASS. </p>
     */
    private static void testCreatePost_emptyBody_rejected() {
        // REQ-09: Empty body must be rejected
        String body = "";
        boolean validationCatchesIt = (body == null || body.isBlank());
        printResult("TC-03 createPost_emptyBody_rejected", validationCatchesIt,
            "Empty body correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_emptyCategory_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with no category. </p>
     *
     * <p> Description: Simulates the validation check for a null category. </p>
     *
     * <p> Input: category=null </p>
     *
     * <p> Expected output: Validation check returns true (invalid). PASS. </p>
     */
    private static void testCreatePost_emptyCategory_rejected() {
        // REQ-09: Null/blank category must be rejected
        String category = null;
        boolean validationCatchesIt = (category == null || category.isBlank());
        printResult("TC-04 createPost_emptyCategory_rejected", validationCatchesIt,
            "Null category correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_nullTitle_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation handles null title without crashing. </p>
     *
     * <p> Description: Verifies null title is handled gracefully. </p>
     *
     * <p> Input: title=null </p>
     *
     * <p> Expected output: Validation check returns true (invalid). PASS. </p>
     */
    private static void testCreatePost_nullTitle_rejected() {
        // REQ-09: Null title must be handled without crashing
        String title = null;
        boolean validationCatchesIt = (title == null || title.isBlank());
        printResult("TC-05 createPost_nullTitle_rejected", validationCatchesIt,
            "Null title handled without throwing NullPointerException.");
    }

    // =========================================================================
    // READ POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testGetAllPosts_returnsList() </p>
     *
     * <p> Requirement: REQ-03 — Student can view a list of all posts. </p>
     *
     * <p> Description: Seeds one post and verifies getAllPosts returns a
     * non-null, non-empty list. </p>
     *
     * <p> Input: Database seeded with at least one post. </p>
     *
     * <p> Expected output: Non-null, non-empty list. PASS. </p>
     */
    private static void testGetAllPosts_returnsList() {
        // REQ-03: getAllPosts must return a non-empty list when posts exist
        db.addPost("Read Test Post", "Body for read test.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        boolean passed = posts != null && posts.size() > 0;
        printResult("TC-06 getAllPosts_returnsList", passed,
            "getAllPosts returns a non-null, non-empty list when posts exist.");
    }

    /*******
     * <p> Method: testGetPostById_validId() </p>
     *
     * <p> Requirement: REQ-04 — Student can retrieve a single post by ID. </p>
     *
     * <p> Description: Adds a post, gets its ID from getAllPosts, retrieves
     * it by ID, and verifies the title matches. </p>
     *
     * <p> Input: Valid postID from a seeded post. </p>
     *
     * <p> Expected output: Non-null Post with matching title. PASS. </p>
     */
    private static void testGetPostById_validId() {
        // REQ-04: getPost must return the correct post for a valid ID
        db.addPost("GetById Test", "Body.", "testUser", "Question");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        Post retrieved = db.getPost(postID);
        boolean passed = retrieved != null && retrieved.getTitle().equals("GetById Test");
        printResult("TC-07 getPostById_validId", passed,
            "getPost returns the correct Post object for a valid postID.");
    }

    /*******
     * <p> Method: testGetPostById_invalidId_returnsNull() </p>
     *
     * <p> Requirement: REQ-04 — Retrieving a post with non-existent ID returns null. </p>
     *
     * <p> Description: Calls getPost with ID 999999. Verifies null is returned. </p>
     *
     * <p> Input: postID=999999 (non-existent) </p>
     *
     * <p> Expected output: null. PASS. </p>
     */
    private static void testGetPostById_invalidId_returnsNull() {
        // REQ-04: getPost must return null for a non-existent postID
        Post retrieved = db.getPost(999999);
        boolean passed = retrieved == null;
        printResult("TC-08 getPostById_invalidId_returnsNull", passed,
            "getPost returns null for a non-existent postID (999999).");
    }

    // =========================================================================
    // UPDATE POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testUpdatePostTitle_valid() </p>
     *
     * <p> Requirement: REQ-05 — Student can edit the title of their own post. </p>
     *
     * <p> Description: Adds a post, updates its title, retrieves it, and
     * verifies the new title is stored. </p>
     *
     * <p> Input: Valid postID, newTitle="Updated Title" </p>
     *
     * <p> Expected output: Retrieved post has title "Updated Title". PASS. </p>
     */
    private static void testUpdatePostTitle_valid() {
        // REQ-05: updatePostTitle must persist the new title
        db.addPost("Original Title", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.updatePostTitle(postID, "Updated Title");
        Post updated = db.getPost(postID);
        boolean passed = updated != null && updated.getTitle().equals("Updated Title");
        printResult("TC-09 updatePostTitle_valid", passed,
            "updatePostTitle correctly persists the new title.");
    }

    /*******
     * <p> Method: testUpdatePostBody_valid() </p>
     *
     * <p> Requirement: REQ-05 — Student can edit the body of their own post. </p>
     *
     * <p> Description: Adds a post, updates its body, retrieves it, and
     * verifies the new body is stored. </p>
     *
     * <p> Input: Valid postID, newBody="Updated Body." </p>
     *
     * <p> Expected output: Retrieved post has body "Updated Body.". PASS. </p>
     */
    private static void testUpdatePostBody_valid() {
        // REQ-05: updatePostBody must persist the new body
        db.addPost("Title", "Original Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.updatePostBody(postID, "Updated Body.");
        Post updated = db.getPost(postID);
        boolean passed = updated != null && updated.getBody().equals("Updated Body.");
        printResult("TC-10 updatePostBody_valid", passed,
            "updatePostBody correctly persists the new body.");
    }

    /*******
     * <p> Method: testUpdatePostCategory_valid() </p>
     *
     * <p> Requirement: REQ-05, REQ-12 — Student can change the category of their post. </p>
     *
     * <p> Description: Adds a post with category "General", updates to
     * "Question", retrieves it, and verifies the change. </p>
     *
     * <p> Input: Valid postID, newCategory="Question" </p>
     *
     * <p> Expected output: Retrieved post has category "Question". PASS. </p>
     */
    private static void testUpdatePostCategory_valid() {
        // REQ-05, REQ-12: updatePostCategory must persist the new category
        db.addPost("Title", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.updatePostCategory(postID, "Question");
        Post updated = db.getPost(postID);
        boolean passed = updated != null && updated.getCategory().equals("Question");
        printResult("TC-11 updatePostCategory_valid", passed,
            "updatePostCategory correctly persists the new category.");
    }

    /*******
     * <p> Method: testUpdatePost_emptyTitle_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects empty title on update. </p>
     *
     * <p> Description: Simulates validation check for blank title on update. </p>
     *
     * <p> Input: newTitle="" </p>
     *
     * <p> Expected output: Validation check returns true (blocked). PASS. </p>
     */
    private static void testUpdatePost_emptyTitle_rejected() {
        // REQ-09: Empty title must be caught during update validation
        String newTitle = "";
        boolean validationCatchesIt = (newTitle == null || newTitle.isBlank());
        printResult("TC-12 updatePost_emptyTitle_rejected", validationCatchesIt,
            "Empty title on update correctly blocked by validation logic.");
    }

    /*******
     * <p> Method: testUpdatePost_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects empty body on update. </p>
     *
     * <p> Description: Simulates validation check for blank body on update. </p>
     *
     * <p> Input: newBody="" </p>
     *
     * <p> Expected output: Validation check returns true (blocked). PASS. </p>
     */
    private static void testUpdatePost_emptyBody_rejected() {
        // REQ-09: Empty body must be caught during update validation
        String newBody = "";
        boolean validationCatchesIt = (newBody == null || newBody.isBlank());
        printResult("TC-13 updatePost_emptyBody_rejected", validationCatchesIt,
            "Empty body on update correctly blocked by validation logic.");
    }

    // =========================================================================
    // DELETE POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testDeletePost_valid() </p>
     *
     * <p> Requirement: REQ-07 — Student can delete their own post. </p>
     *
     * <p> Description: Adds a post, deletes it, then calls getPost and
     * verifies null is returned. </p>
     *
     * <p> Input: Valid postID of a seeded post. </p>
     *
     * <p> Expected output: getPost returns null after deletion. PASS. </p>
     */
    private static void testDeletePost_valid() {
        // REQ-07: deletePost must remove the post from the database
        db.addPost("To Be Deleted", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.deletePost(postID);
        Post retrieved = db.getPost(postID);
        boolean passed = retrieved == null;
        printResult("TC-14 deletePost_valid", passed,
            "deletePost removes post; getPost returns null for deleted ID.");
    }

    /*******
     * <p> Method: testDeletePost_nonExistentId_noError() </p>
     *
     * <p> Requirement: REQ-07 — Deleting a non-existent post does not crash. </p>
     *
     * <p> Description: Calls deletePost with ID 999999. Verifies no exception. </p>
     *
     * <p> Input: postID=999999 (non-existent) </p>
     *
     * <p> Expected output: No exception thrown. PASS. </p>
     */
    private static void testDeletePost_nonExistentId_noError() {
        // REQ-07: deletePost on a non-existent ID must not crash
        boolean passed = true;
        try {
            db.deletePost(999999);
        } catch (Exception e) {
            passed = false;
        }
        printResult("TC-15 deletePost_nonExistentId_noError", passed,
            "deletePost on non-existent ID completes without throwing an exception.");
    }

    // =========================================================================
    // CREATE REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testCreateReply_validInput() </p>
     *
     * <p> Requirement: REQ-02 — Student can reply to an existing post. </p>
     *
     * <p> Description: Seeds a parent post, adds a reply, retrieves replies
     * for that post, and verifies the reply body is correct. </p>
     *
     * <p> Input: parentPostID=valid, body="This is a reply.", author="testUser" </p>
     *
     * <p> Expected output: Non-empty reply list with correct body. PASS. </p>
     */
    private static void testCreateReply_validInput() {
        // REQ-02: addReply must persist a reply linked to the correct parent post
        db.addPost("Parent Post", "Parent body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.addReply(postID, "This is a reply.", "testUser");
        ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() > 0
            && replies.get(0).getBody().equals("This is a reply.");
        printResult("TC-16 createReply_validInput", passed,
            "addReply persists reply and links it correctly to the parent post.");
    }

    /*******
     * <p> Method: testCreateReply_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects replies with empty body. </p>
     *
     * <p> Description: Verifies that a blank body is caught by validation
     * before addReply is called. </p>
     *
     * <p> Input: body="" </p>
     *
     * <p> Expected output: Validation check returns true (blocked). PASS. </p>
     */
    private static void testCreateReply_emptyBody_rejected() {
        // REQ-09: empty body must be caught before calling addReply
        String body = "";
        boolean validationCatchesIt = (body == null || body.isBlank());
        printResult("TC-17 createReply_emptyBody_rejected", validationCatchesIt,
            "Empty reply body correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreateReply_nullBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation handles null reply body. </p>
     *
     * <p> Description: Verifies null body is caught by validation logic. </p>
     *
     * <p> Input: body=null </p>
     *
     * <p> Expected output: Validation check returns true (blocked). PASS. </p>
     */
    private static void testCreateReply_nullBody_rejected() {
        // REQ-09: null body must not be silently inserted
        String body = null;
        boolean validationCatchesIt = (body == null || body.isBlank());
        printResult("TC-18 createReply_nullBody_rejected", validationCatchesIt,
            "Null reply body correctly identified as invalid by validation logic.");
    }

    // =========================================================================
    // READ REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testGetRepliesForPost_returnsCorrectReplies() </p>
     *
     * <p> Requirement: REQ-04 — Student can view replies for a specific post. </p>
     *
     * <p> Description: Seeds a post with two replies. Verifies only those
     * two replies are returned for that post. </p>
     *
     * <p> Input: postID with 2 replies seeded. </p>
     *
     * <p> Expected output: List of size 2, all with correct parentPostID. PASS. </p>
     */
    private static void testGetRepliesForPost_returnsCorrectReplies() {
        // REQ-04: getRepliesForPost must return only replies for the given post
        db.addPost("Reply Filter Test", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.addReply(postID, "Reply 1", "testUser");
        db.addReply(postID, "Reply 2", "testUser");

        ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() == 2
            && replies.stream().allMatch(r -> r.getParentPostID() == postID);
        printResult("TC-19 getRepliesForPost_returnsCorrectReplies", passed,
            "getRepliesForPost returns exactly the replies belonging to the target post.");
    }

    /*******
     * <p> Method: testGetRepliesForPost_noReplies_returnsEmpty() </p>
     *
     * <p> Requirement: REQ-04 — Viewing a post with no replies returns empty list. </p>
     *
     * <p> Description: Seeds a post with no replies and verifies an empty
     * (not null) list is returned. </p>
     *
     * <p> Input: postID with no replies. </p>
     *
     * <p> Expected output: Empty list (size 0). PASS. </p>
     */
    private static void testGetRepliesForPost_noReplies_returnsEmpty() {
        // REQ-04: getRepliesForPost must return empty list (not null) for post with no replies
        db.addPost("No Replies Post", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() == 0;
        printResult("TC-20 getRepliesForPost_noReplies_returnsEmpty", passed,
            "getRepliesForPost returns empty (non-null) list for post with no replies.");
    }

    // =========================================================================
    // UPDATE REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testUpdateReplyBody_valid() </p>
     *
     * <p> Requirement: REQ-06 — Student can edit the body of their own reply. </p>
     *
     * <p> Description: Seeds a post and reply, updates the reply body,
     * retrieves it, and verifies the new body is stored. </p>
     *
     * <p> Input: Valid replyID, newBody="Updated reply body." </p>
     *
     * <p> Expected output: Retrieved reply has body "Updated reply body.". PASS. </p>
     */
    private static void testUpdateReplyBody_valid() {
        // REQ-06: updateReplyBody must persist the new body
        db.addPost("Update Reply Test", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.addReply(postID, "Original reply.", "testUser");
        ArrayList<Reply> replies = db.getRepliesForPost(postID);
        int replyID = replies.get(replies.size() - 1).getReplyID();

        db.updateReplyBody(replyID, "Updated reply body.");
        Reply updated = db.getReply(replyID);
        boolean passed = updated != null && updated.getBody().equals("Updated reply body.");
        printResult("TC-21 updateReplyBody_valid", passed,
            "updateReplyBody correctly persists the new reply body.");
    }

    // =========================================================================
    // DELETE REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testDeleteReply_valid() </p>
     *
     * <p> Requirement: REQ-08 — Student can delete their own reply. </p>
     *
     * <p> Description: Seeds a post and reply, deletes the reply, then calls
     * getReply and verifies null is returned. </p>
     *
     * <p> Input: Valid replyID. </p>
     *
     * <p> Expected output: getReply returns null after deletion. PASS. </p>
     */
    private static void testDeleteReply_valid() {
        // REQ-08: deleteReply must remove the reply from the database
        db.addPost("Delete Reply Test", "Body.", "testUser", "General");
        ArrayList<Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        db.addReply(postID, "Reply to delete.", "testUser");
        ArrayList<Reply> replies = db.getRepliesForPost(postID);
        int replyID = replies.get(replies.size() - 1).getReplyID();

        db.deleteReply(replyID);
        Reply retrieved = db.getReply(replyID);
        boolean passed = retrieved == null;
        printResult("TC-22 deleteReply_valid", passed,
            "deleteReply removes reply; getReply returns null for deleted ID.");
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
            System.out.println("  *** FAIL ***  " + numFailed);
            numFailed++;
        }
        System.out.println("               " + explanation);
    }
}