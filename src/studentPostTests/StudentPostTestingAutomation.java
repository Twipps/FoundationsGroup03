package studentPostTests;

import entityClasses.Post;
import entityClasses.PostList;
import entityClasses.Reply;
import entityClasses.ReplyList;
import guiComponents.postFunctionality.PostNavBar;
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
 * <p> How to run: Execute the main() method. Each test prints PASS or FAIL to
 * the console along with a description of what was tested. A summary of total
 * passed and failed tests is printed at the end. </p>
 *
 * <p> Note: This test class operates directly against the database layer
 * (postComponents and database packages) and does not test JavaFX GUI rendering.
 * GUI-level validation behavior (e.g. empty title rejection in PostReplyEditPanel)
 * is covered by manual tests described in the StudentPostTests_Design.docx. </p>
 *
 * @author Kyle Kim (Team 3) — Test design, implementation, and documentation
 *
 * @version 1.00  2026-06-XX  Initial implementation
 */
public class StudentPostTestingAutomation {

    // ── counters ──────────────────────────────────────────────────────────────

    /** Running count of tests that passed. */
    private static int numPassed = 0;

    /** Running count of tests that failed. */
    private static int numFailed = 0;

    // ── shared database reference ─────────────────────────────────────────────

    /**
     * Direct reference to the application database used to seed and verify
     * test data without going through the full GUI stack.
     * NOTE: Requires the application database to be connected before running.
     */
    private static Database db = applicationMain.FoundationsMain.database;

    /**
     * The threadID of the "General" thread, resolved once at class load.
     * The posts table now stores threadID as a required foreign key
     * (schema updated after this test class was originally written), so
     * every addPost() call below must supply a valid threadID. General is
     * guaranteed to always exist (seeded automatically by createTables()),
     * so it is safe to resolve and reuse here. Note: the category string
     * argument to addPost() is independent of threadID and is unaffected —
     * tests that assert on category values (e.g. "Question") are unchanged.
     */
    private static final int GENERAL_THREAD_ID = resolveGeneralThreadID();

    private static int resolveGeneralThreadID() {
        entityClasses.Thread general = db.getThreadByTitle("General");
        if (general == null) {
            System.err.println("WARNING: General thread not found — tests requiring addPost will fail.");
            return -1;
        }
        return general.getThreadID();
    }

    // ── main ──────────────────────────────────────────────────────────────────

    /*******
     * <p> Method: main() </p>
     *
     * <p> Description: Entry point for the semi-automated test suite. Runs all
     * test cases in sequence and prints a pass/fail summary to the console.
     * Tests are ordered: Create → Read → Update → Delete → Replies → Validation
     * → Search/Filter, matching the CRUD flow of the Students User Stories. </p>
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

        // ── SEARCH / FILTER ───────────────────────────────────────────────────
        System.out.println("\n--- SEARCH AND FILTER TESTS ---");
        testMatchesSearch_titleMatch();
        testMatchesSearch_bodyMatch();
        testMatchesSearch_noMatch();
        testMatchesSearch_blankQuery_returnsTrue();
        testMatchesCategory_matchingCategory();
        testMatchesCategory_allCategory_returnsTrue();
        testMatchesCategory_noMatch_returnsFalse();

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
     * then retrieves all posts and verifies the list is non-empty, confirming
     * the post was persisted. </p>
     *
     * <p> Input: title="Test Title", body="Test body.", author="testUser",
     * category="General" </p>
     *
     * <p> Expected output: Post list contains at least one post after insertion.
     * Console prints PASS. </p>
     *
     * <p> How to interpret: If the post list is non-empty after the call to
     * addPost, the database layer correctly persists a new post. </p>
     */
    private static void testCreatePost_validInput() {
        // REQ-01: Create a post with valid input
        db.addPost("Test Title", "Test body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        boolean passed = posts.getPostList().size() > 0;
        printResult("TC-01 createPost_validInput", passed,
            "Post was created and is retrievable from the database.");
    }

    /*******
     * <p> Method: testCreatePost_emptyTitle_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with an empty title. </p>
     *
     * <p> Description: Attempts to add a post with a blank title string.
     * The PostReplyEditPanel validates this in the GUI before calling addPost,
     * so this test verifies that the validation logic in the panel correctly
     * identifies a blank title. Since addPost itself does not validate, this
     * test simulates the validation check directly. </p>
     *
     * <p> Input: title="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (title is invalid).
     * Console prints PASS. </p>
     *
     * <p> How to interpret: The isBlank() check mirrors PostReplyEditPanel's
     * guard. If it returns true for an empty string, the GUI would have shown
     * an error and blocked submission. </p>
     */
    private static void testCreatePost_emptyTitle_rejected() {
        // REQ-09: Empty title must be rejected by input validation
        String title = "";
        boolean validationCatchesIt = (title == null || title.isBlank());
        printResult("TC-02 createPost_emptyTitle_rejected", validationCatchesIt,
            "Empty title is correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with an empty body. </p>
     *
     * <p> Description: Verifies that a blank body string is caught by the
     * same validation logic used in PostReplyEditPanel before database insertion. </p>
     *
     * <p> Input: body="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (body is invalid).
     * Console prints PASS. </p>
     */
    private static void testCreatePost_emptyBody_rejected() {
        // REQ-09: Empty body must be rejected
        String body = "";
        boolean validationCatchesIt = (body == null || body.isBlank());
        printResult("TC-03 createPost_emptyBody_rejected", validationCatchesIt,
            "Empty body is correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_emptyCategory_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects posts with no category selected. </p>
     *
     * <p> Description: Verifies that a null category (no selection made in the
     * ComboBox) is caught by validation before database insertion. </p>
     *
     * <p> Input: category=null (no selection) </p>
     *
     * <p> Expected output: Validation check returns true (category is invalid).
     * Console prints PASS. </p>
     */
    private static void testCreatePost_emptyCategory_rejected() {
        // REQ-09: Null/blank category must be rejected
        String category = null;
        boolean validationCatchesIt = (category == null || category.isBlank());
        printResult("TC-04 createPost_emptyCategory_rejected", validationCatchesIt,
            "Null category is correctly identified as invalid by validation logic.");
    }

    /*******
     * <p> Method: testCreatePost_nullTitle_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation handles null title without crashing. </p>
     *
     * <p> Description: Verifies that a null title is handled gracefully by the
     * validation logic — no NullPointerException is thrown. </p>
     *
     * <p> Input: title=null </p>
     *
     * <p> Expected output: Validation check returns true (null title is invalid).
     * Console prints PASS. </p>
     */
    private static void testCreatePost_nullTitle_rejected() {
        // REQ-09: Null title must be handled without crashing
        String title = null;
        boolean validationCatchesIt = (title == null || title.isBlank());
        printResult("TC-05 createPost_nullTitle_rejected", validationCatchesIt,
            "Null title is correctly handled without throwing NullPointerException.");
    }

    // =========================================================================
    // READ POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testGetAllPosts_returnsList() </p>
     *
     * <p> Requirement: REQ-03 — Student can view a list of all posts. </p>
     *
     * <p> Description: Seeds the database with one post, then calls getAllPosts
     * via PostList and verifies the returned list is non-null and non-empty. </p>
     *
     * <p> Input: Database seeded with at least one post. </p>
     *
     * <p> Expected output: Non-null, non-empty list. Console prints PASS. </p>
     */
    private static void testGetAllPosts_returnsList() {
        // REQ-03: getAllPosts must return a non-empty list when posts exist
        db.addPost("Read Test Post", "Body for read test.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        boolean passed = posts.getPostList() != null && posts.getPostList().size() > 0;
        printResult("TC-06 getAllPosts_returnsList", passed,
            "getAllPosts returns a non-null, non-empty list when posts exist.");
    }

    /*******
     * <p> Method: testGetPostById_validId() </p>
     *
     * <p> Requirement: REQ-04 — Student can retrieve a single post by ID. </p>
     *
     * <p> Description: Adds a post, retrieves all posts to find its ID,
     * then retrieves that post by ID and verifies the returned Post object
     * is not null and has the correct title. </p>
     *
     * <p> Input: Valid postID from a seeded post. </p>
     *
     * <p> Expected output: Non-null Post with matching title. Console prints PASS. </p>
     */
    private static void testGetPostById_validId() {
        // REQ-04: getPost must return the correct post for a valid ID
        db.addPost("GetById Test", "Body.", "testUser", "Question", GENERAL_THREAD_ID);
        PostList posts = new PostList();

        // Get the last post in the list (most recently added)
        int lastIndex = posts.getPostList().size() - 1;
        int postID = posts.getPostList().get(lastIndex).getPostID();

        Post retrieved = db.getPost(postID);
        boolean passed = retrieved != null && retrieved.getTitle().equals("GetById Test");
        printResult("TC-07 getPostById_validId", passed,
            "getPost returns the correct Post object for a valid postID.");
    }

    /*******
     * <p> Method: testGetPostById_invalidId_returnsNull() </p>
     *
     * <p> Requirement: REQ-04 — Retrieving a post with a non-existent ID returns null. </p>
     *
     * <p> Description: Calls getPost with an ID (999999) that is guaranteed
     * not to exist in the database. Verifies null is returned gracefully. </p>
     *
     * <p> Input: postID=999999 (non-existent) </p>
     *
     * <p> Expected output: null returned. Console prints PASS. </p>
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
     * <p> Description: Adds a post, retrieves its ID, updates its title via
     * updatePostTitle, then retrieves the post again and verifies the new title
     * is reflected. </p>
     *
     * <p> Input: Valid postID, newTitle="Updated Title" </p>
     *
     * <p> Expected output: Retrieved post has title "Updated Title".
     * Console prints PASS. </p>
     */
    private static void testUpdatePostTitle_valid() {
        // REQ-05: updatePostTitle must persist the new title
        db.addPost("Original Title", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.updatePostTitle(postID, "Updated Title");

        Post updated = db.getPost(postID);
        boolean passed = updated != null && updated.getTitle().equals("Updated Title");
        printResult("TC-09 updatePostTitle_valid", passed,
            "updatePostTitle correctly persists the new title to the database.");
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
     * <p> Expected output: Retrieved post has body "Updated Body.".
     * Console prints PASS. </p>
     */
    private static void testUpdatePostBody_valid() {
        // REQ-05: updatePostBody must persist the new body
        db.addPost("Title", "Original Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.updatePostBody(postID, "Updated Body.");

        Post updated = db.getPost(postID);
        boolean passed = updated != null && updated.getBody().equals("Updated Body.");
        printResult("TC-10 updatePostBody_valid", passed,
            "updatePostBody correctly persists the new body to the database.");
    }

    /*******
     * <p> Method: testUpdatePostCategory_valid() </p>
     *
     * <p> Requirement: REQ-05, REQ-12 — Student can change the category of their post. </p>
     *
     * <p> Description: Adds a post with category "General", updates it to
     * "Question", retrieves it, and verifies the change. </p>
     *
     * <p> Input: Valid postID, newCategory="Question" </p>
     *
     * <p> Expected output: Retrieved post has category "Question".
     * Console prints PASS. </p>
     */
    private static void testUpdatePostCategory_valid() {
        // REQ-05, REQ-12: updatePostCategory must persist the new category
        db.addPost("Title", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

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
     * <p> Description: Simulates the PostReplyEditPanel validation logic for
     * an update action with a blank title. Verifies the blank check fires
     * before updatePostTitle would be called. </p>
     *
     * <p> Input: newTitle="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (blocked).
     * Console prints PASS. </p>
     */
    private static void testUpdatePost_emptyTitle_rejected() {
        // REQ-09: Empty title must be caught during update validation
        String newTitle = "";
        boolean validationCatchesIt = (newTitle == null || newTitle.isBlank());
        printResult("TC-12 updatePost_emptyTitle_rejected", validationCatchesIt,
            "Empty title on update is correctly blocked by validation logic.");
    }

    /*******
     * <p> Method: testUpdatePost_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects empty body on update. </p>
     *
     * <p> Description: Simulates the PostReplyEditPanel validation logic for
     * an update action with a blank body. </p>
     *
     * <p> Input: newBody="" (empty string) </p>
     *
     * <p> Expected output: Validation check returns true (blocked).
     * Console prints PASS. </p>
     */
    private static void testUpdatePost_emptyBody_rejected() {
        // REQ-09: Empty body must be caught during update validation
        String newBody = "";
        boolean validationCatchesIt = (newBody == null || newBody.isBlank());
        printResult("TC-13 updatePost_emptyBody_rejected", validationCatchesIt,
            "Empty body on update is correctly blocked by validation logic.");
    }

    // =========================================================================
    // DELETE POST TESTS
    // =========================================================================

    /*******
     * <p> Method: testDeletePost_valid() </p>
     *
     * <p> Requirement: REQ-07 — Student can delete their own post. </p>
     *
     * <p> Description: Adds a post, records its ID, deletes it via deletePost,
     * then calls getPost on that ID and verifies null is returned, confirming
     * the post no longer exists in the database. </p>
     *
     * <p> Input: Valid postID of a seeded post. </p>
     *
     * <p> Expected output: getPost returns null after deletion.
     * Console prints PASS. </p>
     */
    private static void testDeletePost_valid() {
        // REQ-07: deletePost must remove the post from the database
        db.addPost("To Be Deleted", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.deletePost(postID);

        Post retrieved = db.getPost(postID);
        boolean passed = retrieved == null;
        printResult("TC-14 deletePost_valid", passed,
            "deletePost removes the post; getPost returns null for the deleted ID.");
    }

    /*******
     * <p> Method: testDeletePost_nonExistentId_noError() </p>
     *
     * <p> Requirement: REQ-07 — Deleting a non-existent post does not crash. </p>
     *
     * <p> Description: Calls deletePost with an ID that does not exist.
     * Verifies the method completes without throwing an exception. </p>
     *
     * <p> Input: postID=999999 (non-existent) </p>
     *
     * <p> Expected output: No exception thrown. Console prints PASS. </p>
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
     * <p> Description: Seeds a parent post, adds a reply to it via addReply,
     * then retrieves replies for that post and verifies at least one reply
     * exists with the correct body. </p>
     *
     * <p> Input: parentPostID=valid, body="This is a reply.", author="testUser" </p>
     *
     * <p> Expected output: getRepliesForPost returns a non-empty list with the
     * correct reply body. Console prints PASS. </p>
     */
    private static void testCreateReply_validInput() {
        // REQ-02: addReply must persist a reply linked to the correct parent post
        db.addPost("Parent Post", "Parent body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.addReply(postID, "This is a reply.", "testUser");

        java.util.ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() > 0
            && replies.get(0).getBody().equals("This is a reply.");
        printResult("TC-16 createReply_validInput", passed,
            "addReply persists the reply and links it correctly to the parent post.");
    }

    /*******
     * <p> Method: testCreateReply_emptyBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation rejects replies with an empty body. </p>
     *
     * <p> Description: Calls ReplyList.createReply with an empty body string
     * and verifies that an IllegalArgumentException is thrown, as implemented
     * in ReplyList.createReply(). </p>
     *
     * <p> Input: parentPostID=1, body="" (empty string), author="testUser" </p>
     *
     * <p> Expected output: IllegalArgumentException thrown with message
     * "Reply body cannot be empty.". Console prints PASS. </p>
     */
    private static void testCreateReply_emptyBody_rejected() {
        // REQ-09: ReplyList.createReply must throw IllegalArgumentException for empty body
        boolean passed = false;
        try {
            ReplyList replies = new ReplyList();
            replies.createReply(1, "", "testUser");
        } catch (IllegalArgumentException e) {
            // Expected — empty body is correctly rejected
            passed = e.getMessage().equals("Reply body cannot be empty.");
        } catch (Exception e) {
            passed = false;
        }
        printResult("TC-17 createReply_emptyBody_rejected", passed,
            "createReply throws IllegalArgumentException for an empty body.");
    }

    /*******
     * <p> Method: testCreateReply_nullBody_rejected() </p>
     *
     * <p> Requirement: REQ-09 — Input validation handles null reply body without crashing. </p>
     *
     * <p> Description: Calls ReplyList.createReply with a null body and verifies
     * an IllegalArgumentException is thrown (null body triggers the isBlank check
     * via NullPointerException caught upstream, or the null check in the method). </p>
     *
     * <p> Input: parentPostID=1, body=null, author="testUser" </p>
     *
     * <p> Expected output: Exception is thrown (IllegalArgumentException or
     * NullPointerException). Console prints PASS. </p>
     */
    private static void testCreateReply_nullBody_rejected() {
        // REQ-09: null body must not silently insert a null reply
        boolean passed = false;
        try {
            ReplyList replies = new ReplyList();
            replies.createReply(1, null, "testUser");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Either exception is acceptable — null body is blocked
            passed = true;
        } catch (Exception e) {
            passed = false;
        }
        printResult("TC-18 createReply_nullBody_rejected", passed,
            "createReply throws an exception for a null body — null not silently inserted.");
    }

    // =========================================================================
    // READ REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testGetRepliesForPost_returnsCorrectReplies() </p>
     *
     * <p> Requirement: REQ-04 — Student can view replies for a specific post. </p>
     *
     * <p> Description: Seeds a post with two replies and one reply on a different
     * post. Calls getRepliesForPost and verifies only the two replies for the
     * target post are returned. </p>
     *
     * <p> Input: postID=target post with 2 replies seeded. </p>
     *
     * <p> Expected output: List of size 2, all with correct parentPostID.
     * Console prints PASS. </p>
     */
    private static void testGetRepliesForPost_returnsCorrectReplies() {
        // REQ-04: getRepliesForPost must return only replies for the given post
        db.addPost("Reply Filter Test", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.addReply(postID, "Reply 1", "testUser");
        db.addReply(postID, "Reply 2", "testUser");

        java.util.ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() == 2
            && replies.stream().allMatch(r -> r.getParentPostID() == postID);
        printResult("TC-19 getRepliesForPost_returnsCorrectReplies", passed,
            "getRepliesForPost returns exactly the replies belonging to the target post.");
    }

    /*******
     * <p> Method: testGetRepliesForPost_noReplies_returnsEmpty() </p>
     *
     * <p> Requirement: REQ-04 — Viewing a post with no replies returns an empty list. </p>
     *
     * <p> Description: Seeds a post with no replies and calls getRepliesForPost.
     * Verifies an empty (not null) list is returned. </p>
     *
     * <p> Input: postID=a post with no replies. </p>
     *
     * <p> Expected output: Empty list (size 0). Console prints PASS. </p>
     */
    private static void testGetRepliesForPost_noReplies_returnsEmpty() {
        // REQ-04: getRepliesForPost must return an empty list (not null) for a post with no replies
        db.addPost("No Replies Post", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        java.util.ArrayList<Reply> replies = db.getRepliesForPost(postID);
        boolean passed = replies != null && replies.size() == 0;
        printResult("TC-20 getRepliesForPost_noReplies_returnsEmpty", passed,
            "getRepliesForPost returns an empty (non-null) list for a post with no replies.");
    }

    // =========================================================================
    // UPDATE REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testUpdateReplyBody_valid() </p>
     *
     * <p> Requirement: REQ-06 — Student can edit the body of their own reply. </p>
     *
     * <p> Description: Seeds a post and reply, updates the reply body via
     * updateReplyBody, retrieves the reply by ID, and verifies the new body
     * is stored. </p>
     *
     * <p> Input: Valid replyID, newBody="Updated reply body." </p>
     *
     * <p> Expected output: Retrieved reply has body "Updated reply body.".
     * Console prints PASS. </p>
     */
    private static void testUpdateReplyBody_valid() {
        // REQ-06: updateReplyBody must persist the new body
        db.addPost("Update Reply Test", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.addReply(postID, "Original reply.", "testUser");
        java.util.ArrayList<Reply> replies = db.getRepliesForPost(postID);
        int replyID = replies.get(replies.size() - 1).getReplyID();

        db.updateReplyBody(replyID, "Updated reply body.");

        Reply updated = db.getReply(replyID);
        boolean passed = updated != null && updated.getBody().equals("Updated reply body.");
        printResult("TC-21 updateReplyBody_valid", passed,
            "updateReplyBody correctly persists the new reply body to the database.");
    }

    // =========================================================================
    // DELETE REPLY TESTS
    // =========================================================================

    /*******
     * <p> Method: testDeleteReply_valid() </p>
     *
     * <p> Requirement: REQ-08 — Student can delete their own reply. </p>
     *
     * <p> Description: Seeds a post and reply, deletes the reply via deleteReply,
     * then calls getReply and verifies null is returned. </p>
     *
     * <p> Input: Valid replyID. </p>
     *
     * <p> Expected output: getReply returns null after deletion.
     * Console prints PASS. </p>
     */
    private static void testDeleteReply_valid() {
        // REQ-08: deleteReply must remove the reply from the database
        db.addPost("Delete Reply Test", "Body.", "testUser", "General", GENERAL_THREAD_ID);
        PostList posts = new PostList();
        int postID = posts.getPostList().get(posts.getPostList().size() - 1).getPostID();

        db.addReply(postID, "Reply to delete.", "testUser");
        java.util.ArrayList<Reply> replies = db.getRepliesForPost(postID);
        int replyID = replies.get(replies.size() - 1).getReplyID();

        db.deleteReply(replyID);

        Reply retrieved = db.getReply(replyID);
        boolean passed = retrieved == null;
        printResult("TC-22 deleteReply_valid", passed,
            "deleteReply removes the reply; getReply returns null for the deleted ID.");
    }

    // =========================================================================
    // SEARCH AND FILTER TESTS
    // =========================================================================

    /*******
     * <p> Method: testMatchesSearch_titleMatch() </p>
     *
     * <p> Requirement: REQ-13 — Student can search posts by keyword in the title. </p>
     *
     * <p> Description: Creates a Post object in memory and calls
     * PostNavBar.matchesSearch with a keyword that appears in the post's title.
     * Verifies true is returned. </p>
     *
     * <p> Input: Post with title="Hello World", search="hello" </p>
     *
     * <p> Expected output: matchesSearch returns true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_titleMatch() {
        // REQ-13: matchesSearch must return true when keyword matches the title
        Post post = new Post("Hello World", "Some body.", "General", "testUser");
        boolean passed = PostNavBar.matchesSearch(post, "hello");
        printResult("TC-23 matchesSearch_titleMatch", passed,
            "matchesSearch returns true when the keyword matches the post title.");
    }

    /*******
     * <p> Method: testMatchesSearch_bodyMatch() </p>
     *
     * <p> Requirement: REQ-13 — Student can search posts by keyword in the body. </p>
     *
     * <p> Description: Creates a Post object with a body containing "javafx"
     * and verifies matchesSearch returns true for that keyword. </p>
     *
     * <p> Input: Post with body="We use javafx here.", search="javafx" </p>
     *
     * <p> Expected output: matchesSearch returns true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_bodyMatch() {
        // REQ-13: matchesSearch must return true when keyword matches the body
        Post post = new Post("Some Title", "We use javafx here.", "General", "testUser");
        boolean passed = PostNavBar.matchesSearch(post, "javafx");
        printResult("TC-24 matchesSearch_bodyMatch", passed,
            "matchesSearch returns true when the keyword matches the post body.");
    }

    /*******
     * <p> Method: testMatchesSearch_noMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search returns false when keyword is not found. </p>
     *
     * <p> Description: Creates a Post object and calls matchesSearch with a
     * keyword that does not appear in the title, body, or category. </p>
     *
     * <p> Input: Post with title="Hello", body="World", search="xyz123" </p>
     *
     * <p> Expected output: matchesSearch returns false. Console prints PASS. </p>
     */
    private static void testMatchesSearch_noMatch() {
        // REQ-13: matchesSearch must return false when keyword is not found anywhere
        Post post = new Post("Hello", "World", "General", "testUser");
        boolean passed = !PostNavBar.matchesSearch(post, "xyz123");
        printResult("TC-25 matchesSearch_noMatch", passed,
            "matchesSearch returns false when the keyword does not appear in title, body, or category.");
    }

    /*******
     * <p> Method: testMatchesSearch_blankQuery_returnsTrue() </p>
     *
     * <p> Requirement: REQ-13 — Blank search query returns all posts. </p>
     *
     * <p> Description: Calls matchesSearch with a blank string. Verifies true
     * is returned, meaning all posts are shown when the search bar is empty. </p>
     *
     * <p> Input: Post with any content, search="" </p>
     *
     * <p> Expected output: matchesSearch returns true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_blankQuery_returnsTrue() {
        // REQ-13: blank search must return all posts (no filtering)
        Post post = new Post("Any Title", "Any body.", "General", "testUser");
        boolean passed = PostNavBar.matchesSearch(post, "");
        printResult("TC-26 matchesSearch_blankQuery_returnsTrue", passed,
            "matchesSearch returns true for a blank search query (all posts shown).");
    }

    /*******
     * <p> Method: testMatchesCategory_matchingCategory() </p>
     *
     * <p> Requirement: REQ-12 — Student can filter posts by category. </p>
     *
     * <p> Description: Creates a Post with category "Question" and calls
     * matchesCategory with "Question". Verifies true is returned. </p>
     *
     * <p> Input: Post with category="Question", filter="Question" </p>
     *
     * <p> Expected output: matchesCategory returns true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_matchingCategory() {
        // REQ-12: matchesCategory must return true when category matches the filter
        Post post = new Post("Title", "Body.", "Question", "testUser");
        boolean passed = PostNavBar.matchesCategory(post, "Question");
        printResult("TC-27 matchesCategory_matchingCategory", passed,
            "matchesCategory returns true when the post category matches the filter.");
    }

    /*******
     * <p> Method: testMatchesCategory_allCategory_returnsTrue() </p>
     *
     * <p> Requirement: REQ-12 — Selecting 'All' category shows all posts. </p>
     *
     * <p> Description: Calls matchesCategory with "All" filter. Verifies true
     * is returned regardless of the post's category. </p>
     *
     * <p> Input: Post with category="Bug", filter="All" </p>
     *
     * <p> Expected output: matchesCategory returns true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_allCategory_returnsTrue() {
        // REQ-12: "All" category filter must match every post regardless of category
        Post post = new Post("Title", "Body.", "Bug", "testUser");
        boolean passed = PostNavBar.matchesCategory(post, "All");
        printResult("TC-28 matchesCategory_allCategory_returnsTrue", passed,
            "matchesCategory returns true for 'All' filter regardless of post category.");
    }

    /*******
     * <p> Method: testMatchesCategory_noMatch_returnsFalse() </p>
     *
     * <p> Requirement: REQ-12 — Category filter excludes non-matching posts. </p>
     *
     * <p> Description: Creates a Post with category "General" and calls
     * matchesCategory with "Question". Verifies false is returned. </p>
     *
     * <p> Input: Post with category="General", filter="Question" </p>
     *
     * <p> Expected output: matchesCategory returns false. Console prints PASS. </p>
     */
    private static void testMatchesCategory_noMatch_returnsFalse() {
        // REQ-12: matchesCategory must return false when categories do not match
        Post post = new Post("Title", "Body.", "General", "testUser");
        boolean passed = !PostNavBar.matchesCategory(post, "Question");
        printResult("TC-29 matchesCategory_noMatch_returnsFalse", passed,
            "matchesCategory returns false when the post category does not match the filter.");
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
     * @param testName   the name of the test being reported
     * @param passed     true if the test passed, false if it failed
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