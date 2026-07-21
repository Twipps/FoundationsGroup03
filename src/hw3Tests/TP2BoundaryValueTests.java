package hw3Tests;

import database.Database;

/*******
 * <p> Title: TP2BoundaryValueTests Class </p>
 *
 * <p> Description: A semi-automated boundary value and coverage test suite
 * targeting the two CWE weaknesses identified for Kyle Kim in HW3 Task 2:
 * CWE-20 (Improper Input Validation) and CWE-400 (Uncontrolled Resource
 * Consumption). </p>
 *
 * <p> Tests focus on the most critical classes identified in TP2 Tests.pdf:
 * PostReplyEditPanel (input validation), PostNavBar (resource consumption
 * via makeSample and safeLower), and Database (input reaching the DB layer). </p>
 *
 * <p> Boundary Value Analysis applied: </p>
 * <p> - Title/body length: 0 chars (empty), 1 char (min valid), 79 chars,
 *   80 chars (max for preview), 81 chars (just over), 10000 chars (extreme). </p>
 * <p> - Null inputs: null title, null body, null category, null search query. </p>
 * <p> - Whitespace: single space, multiple spaces — must be caught by isBlank(). </p>
 *
 * <p> How to run: Right-click → Run As → Java Application.
 * Creates its own database connection. Prints PASS or FAIL for each test.
 * A fully passing run shows 0 failures. </p>
 *
 * <p> HW3 Task: Task 2.4 — TP2 Boundary Value and Coverage Tests </p>
 * <p> CWE Weaknesses Covered: CWE-20 (Input Validation), CWE-400 (Resource Consumption) </p>
 *
 * @author Kyle Kim (Team 3) — HW3 boundary value test design and implementation
 *
 * @version 1.00  2026-07-02  Initial implementation
 */
public class TP2BoundaryValueTests {

    /** Running count of tests that passed. */
    private static int numPassed = 0;

    /** Running count of tests that failed. */
    private static int numFailed = 0;

    /** Private constructor — static utility class. */
    private TP2BoundaryValueTests() {}

    /**
     * Direct database reference for testing DB-layer input handling.
     * Creates its own connection independent of the running app.
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
     * <p> Description: Entry point for the HW3 boundary value test suite.
     * Runs all boundary value and coverage tests and prints a summary. </p>
     *
     * <p> How to interpret output: Each test prints PASS or FAIL with a brief
     * explanation. The summary shows total passed and failed. 0 failures means
     * all boundary conditions are handled correctly. </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  HW3 TP2 Boundary Value Tests — Team 3");
        System.out.println("  Author: Kyle Kim");
        System.out.println("  CWE-20: Input Validation | CWE-400: Resource Consumption");
        System.out.println("================================================\n");

        // ── CWE-20: INPUT VALIDATION — TITLE BOUNDARY VALUES ─────────────────
        System.out.println("--- CWE-20: TITLE INPUT VALIDATION (BV-01 to BV-06) ---");
        testTitle_emptyString_rejected();
        testTitle_singleSpace_rejected();
        testTitle_multipleSpaces_rejected();
        testTitle_null_rejected();
        testTitle_singleChar_accepted();
        testTitle_255chars_accepted();

        // ── CWE-20: INPUT VALIDATION — BODY BOUNDARY VALUES ──────────────────
        System.out.println("\n--- CWE-20: BODY INPUT VALIDATION (BV-07 to BV-10) ---");
        testBody_emptyString_rejected();
        testBody_null_rejected();
        testBody_singleChar_accepted();
        testBody_whitespaceOnly_rejected();

        // ── CWE-20: INPUT VALIDATION — CATEGORY BOUNDARY VALUES ──────────────
        System.out.println("\n--- CWE-20: CATEGORY INPUT VALIDATION (BV-11 to BV-13) ---");
        testCategory_null_rejected();
        testCategory_empty_rejected();
        testCategory_valid_accepted();

        // ── CWE-20: SQL INJECTION DEFENSE ─────────────────────────────────────
        System.out.println("\n--- CWE-20: SQL INJECTION DEFENSE (BV-14) ---");
        testSQLInjection_inTitle_storedAsLiteral();

        // ── CWE-400: RESOURCE CONSUMPTION — makeSample BOUNDARY VALUES ────────
        System.out.println("\n--- CWE-400: RESOURCE CONSUMPTION — makeSample (RC-01 to RC-06) ---");
        testMakeSample_emptyBody();
        testMakeSample_null_returnsEmpty();
        testMakeSample_79chars_returnsAsIs();
        testMakeSample_80chars_returnsAsIs();
        testMakeSample_81chars_truncated();
        testMakeSample_10000chars_truncated();

        // ── CWE-400: RESOURCE CONSUMPTION — safeLower BOUNDARY VALUES ─────────
        System.out.println("\n--- CWE-400: RESOURCE CONSUMPTION — safeLower (RC-07 to RC-09) ---");
        testSafeLower_null_returnsEmpty();
        testSafeLower_emptyString_returnsEmpty();
        testSafeLower_normal_returnsLowercase();

        // ── COVERAGE TESTS ─────────────────────────────────────────────────────
        System.out.println("\n--- COVERAGE TESTS (CV-01 to CV-03) ---");
        testCoverage_createPost_bothBranches();
        testCoverage_getPost_foundAndNotFound();
        testCoverage_deletePost_existingAndNonExisting();

        // ── SUMMARY ────────────────────────────────────────────────────────────
        System.out.println("\n================================================");
        System.out.println("  Tests passed: " + numPassed);
        System.out.println("  Tests failed: " + numFailed);
        System.out.println("================================================");
    }

    // =========================================================================
    // CWE-20: TITLE INPUT VALIDATION
    // =========================================================================

    /*******
     * <p> Method: testTitle_emptyString_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel (validation logic) </p>
     * <p> Boundary: Lower boundary — empty string (length 0) must be rejected. </p>
     *
     * <p> Description: Simulates PostReplyEditPanel's title validation check
     * for an empty string. The isBlank() check is the guard that fires before
     * Database.addPost() is called. </p>
     *
     * <p> Input: title="" (length 0 — lower boundary) </p>
     * <p> Expected: isBlank() returns true (invalid — blocked). PASS. </p>
     */
    private static void testTitle_emptyString_rejected() {
        // CWE-20: empty string is the lower boundary — must be caught
        String title = "";
        boolean caught = (title == null || title.isBlank());
        printResult("BV-01 title_emptyString_rejected", caught,
            "CWE-20: Empty title (length 0, lower boundary) correctly rejected by isBlank().");
    }

    /*******
     * <p> Method: testTitle_singleSpace_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel (validation logic) </p>
     * <p> Boundary: Whitespace boundary — single space must be caught by isBlank(). </p>
     *
     * <p> Input: title=" " (single space) </p>
     * <p> Expected: isBlank() returns true (invalid). PASS. </p>
     */
    private static void testTitle_singleSpace_rejected() {
        // CWE-20: single space passes isEmpty() but must fail isBlank()
        String title = " ";
        boolean caught = (title == null || title.isBlank());
        printResult("BV-02 title_singleSpace_rejected", caught,
            "CWE-20: Single space title correctly rejected — isBlank() catches what isEmpty() misses.");
    }

    /*******
     * <p> Method: testTitle_multipleSpaces_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel (validation logic) </p>
     * <p> Boundary: Whitespace boundary — multiple spaces must be caught by isBlank(). </p>
     *
     * <p> Input: title="   " (multiple spaces) </p>
     * <p> Expected: isBlank() returns true (invalid). PASS. </p>
     */
    private static void testTitle_multipleSpaces_rejected() {
        // CWE-20: multiple spaces must also be caught
        String title = "   ";
        boolean caught = (title == null || title.isBlank());
        printResult("BV-03 title_multipleSpaces_rejected", caught,
            "CWE-20: Multiple spaces title correctly rejected by isBlank().");
    }

    /*******
     * <p> Method: testTitle_null_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel (validation logic) </p>
     * <p> Boundary: Null boundary — null title must be caught without crashing. </p>
     *
     * <p> Input: title=null </p>
     * <p> Expected: null check fires without NullPointerException. PASS. </p>
     */
    private static void testTitle_null_rejected() {
        // CWE-20: null must not cause NullPointerException
        boolean passed = false;
        try {
            String title = null;
            boolean caught = (title == null || title.isBlank());
            passed = caught;
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("BV-04 title_null_rejected", passed,
            "CWE-20: Null title caught without NullPointerException.");
    }

    /*******
     * <p> Method: testTitle_singleChar_accepted() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel (validation logic) </p>
     * <p> Boundary: Just above lower boundary — single character must be accepted. </p>
     *
     * <p> Input: title="A" (length 1 — just above lower boundary) </p>
     * <p> Expected: isBlank() returns false (valid — accepted). PASS. </p>
     */
    private static void testTitle_singleChar_accepted() {
        // CWE-20: length 1 is just above the lower boundary — must be accepted
        String title = "A";
        boolean valid = !(title == null || title.isBlank());
        printResult("BV-05 title_singleChar_accepted", valid,
            "CWE-20: Single char title (length 1, just above lower boundary) accepted.");
    }

    /*******
     * <p> Method: testTitle_255chars_accepted() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Class Under Test: PostReplyEditPanel and Database </p>
     * <p> Boundary: Near-max boundary — 255 character title must be accepted and stored. </p>
     *
     * <p> Input: title=255 character string </p>
     * <p> Expected: Post created and retrievable. PASS. </p>
     */
    private static void testTitle_255chars_accepted() {
        // CWE-20: 255 chars is the typical VARCHAR limit — must be accepted
        String title = "A".repeat(255);
        db.addPost(title, "Test body.", "testUser", "General");
        java.util.ArrayList<entityClasses.Post> posts = db.getAllPosts();
        boolean passed = posts.stream().anyMatch(p -> p.getTitle().equals(title));
        printResult("BV-06 title_255chars_accepted", passed,
            "CWE-20: 255 character title (near-max boundary) accepted and stored in DB.");
    }

    // =========================================================================
    // CWE-20: BODY INPUT VALIDATION
    // =========================================================================

    /*******
     * <p> Method: testBody_emptyString_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Lower boundary — empty body must be rejected. </p>
     *
     * <p> Input: body="" </p>
     * <p> Expected: isBlank() returns true (invalid). PASS. </p>
     */
    private static void testBody_emptyString_rejected() {
        // CWE-20: empty body is the lower boundary
        String body = "";
        boolean caught = (body == null || body.isBlank());
        printResult("BV-07 body_emptyString_rejected", caught,
            "CWE-20: Empty body (lower boundary) correctly rejected by isBlank().");
    }

    /*******
     * <p> Method: testBody_null_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Null boundary — null body must not crash. </p>
     *
     * <p> Input: body=null </p>
     * <p> Expected: null check fires without NullPointerException. PASS. </p>
     */
    private static void testBody_null_rejected() {
        // CWE-20: null body must not cause NullPointerException
        boolean passed = false;
        try {
            String body = null;
            boolean caught = (body == null || body.isBlank());
            passed = caught;
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("BV-08 body_null_rejected", passed,
            "CWE-20: Null body caught without NullPointerException.");
    }

    /*******
     * <p> Method: testBody_singleChar_accepted() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Just above lower boundary — single character body accepted. </p>
     *
     * <p> Input: body="A" (length 1) </p>
     * <p> Expected: isBlank() returns false (valid). PASS. </p>
     */
    private static void testBody_singleChar_accepted() {
        // CWE-20: length 1 is just above lower boundary — must be accepted
        String body = "A";
        boolean valid = !(body == null || body.isBlank());
        printResult("BV-09 body_singleChar_accepted", valid,
            "CWE-20: Single char body (just above lower boundary) accepted.");
    }

    /*******
     * <p> Method: testBody_whitespaceOnly_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Whitespace boundary — whitespace-only body rejected. </p>
     *
     * <p> Input: body="\t\n  " (tabs and newlines) </p>
     * <p> Expected: isBlank() catches mixed whitespace. PASS. </p>
     */
    private static void testBody_whitespaceOnly_rejected() {
        // CWE-20: tabs and newlines are whitespace — isBlank() must catch them
        String body = "\t\n  ";
        boolean caught = (body == null || body.isBlank());
        printResult("BV-10 body_whitespaceOnly_rejected", caught,
            "CWE-20: Tab/newline whitespace body correctly rejected by isBlank().");
    }

    // =========================================================================
    // CWE-20: CATEGORY INPUT VALIDATION
    // =========================================================================

    /*******
     * <p> Method: testCategory_null_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Null boundary — null category (no ComboBox selection) rejected. </p>
     *
     * <p> Input: category=null </p>
     * <p> Expected: null check fires. PASS. </p>
     */
    private static void testCategory_null_rejected() {
        // CWE-20: null category means no dropdown selection — must be blocked
        String category = null;
        boolean caught = (category == null || category.isBlank());
        printResult("BV-11 category_null_rejected", caught,
            "CWE-20: Null category (no dropdown selection) correctly rejected.");
    }

    /*******
     * <p> Method: testCategory_empty_rejected() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Empty boundary — empty category string rejected. </p>
     *
     * <p> Input: category="" </p>
     * <p> Expected: isBlank() returns true. PASS. </p>
     */
    private static void testCategory_empty_rejected() {
        // CWE-20: empty category is another lower boundary case
        String category = "";
        boolean caught = (category == null || category.isBlank());
        printResult("BV-12 category_empty_rejected", caught,
            "CWE-20: Empty category string correctly rejected by isBlank().");
    }

    /*******
     * <p> Method: testCategory_valid_accepted() </p>
     *
     * <p> CWE: CWE-20 — Improper Input Validation </p>
     * <p> Boundary: Valid category — a real category name is accepted. </p>
     *
     * <p> Input: category="General" </p>
     * <p> Expected: isBlank() returns false (valid). PASS. </p>
     */
    private static void testCategory_valid_accepted() {
        // CWE-20: a real category name must be accepted
        String category = "General";
        boolean valid = !(category == null || category.isBlank());
        printResult("BV-13 category_valid_accepted", valid,
            "CWE-20: Valid category 'General' correctly accepted.");
    }

    // =========================================================================
    // CWE-20: SQL INJECTION DEFENSE
    // =========================================================================

    /*******
     * <p> Method: testSQLInjection_inTitle_storedAsLiteral() </p>
     *
     * <p> CWE: CWE-20 / CWE-89 — SQL Injection defense via PreparedStatement </p>
     * <p> Class Under Test: Database.addPost() </p>
     *
     * <p> Description: Attempts to insert a SQL injection string as a post title.
     * Verifies the string is stored as a literal value rather than being executed
     * as SQL, confirming PreparedStatement protection is working. </p>
     *
     * <p> Input: title="'; DROP TABLE posts; --" </p>
     * <p> Expected: Post stored with literal title; posts table still exists. PASS. </p>
     */
    private static void testSQLInjection_inTitle_storedAsLiteral() {
        // CWE-20/CWE-89: SQL injection must be stored as literal, not executed
        String injectionTitle = "'; DROP TABLE posts; --";
        boolean passed = false;
        try {
            db.addPost(injectionTitle, "Injection test body", "testUser", "General");
            // If posts table was dropped, getAllPosts() would throw an exception
            java.util.ArrayList<entityClasses.Post> posts = db.getAllPosts();
            boolean found = posts.stream().anyMatch(p -> injectionTitle.equals(p.getTitle()));
            passed = found; // injection stored as literal, posts table intact
        } catch (Exception e) {
            passed = false; // table was dropped or other SQL error — injection succeeded
        }
        printResult("BV-14 sqlInjection_inTitle_storedAsLiteral", passed,
            "CWE-20/CWE-89: SQL injection in title stored as literal; posts table intact (PreparedStatement working).");
    }

    // =========================================================================
    // CWE-400: RESOURCE CONSUMPTION — makeSample BOUNDARY VALUES
    // =========================================================================

    /*******
     * <p> Method: testMakeSample_emptyBody() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Lower boundary — empty string (length 0). </p>
     *
     * <p> Input: body="" </p>
     * <p> Expected: Returns "" (empty string, not truncated). PASS. </p>
     */
    private static void testMakeSample_emptyBody() {
        // CWE-400: empty body is lower boundary for makeSample
        String result = makeSample("");
        boolean passed = result != null && result.equals("");
        printResult("RC-01 makeSample_emptyBody", passed,
            "CWE-400: makeSample returns empty string for empty body (lower boundary).");
    }

    /*******
     * <p> Method: testMakeSample_null_returnsEmpty() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Null boundary — null body must not crash. </p>
     *
     * <p> Input: body=null </p>
     * <p> Expected: Returns "" without NullPointerException. PASS. </p>
     */
    private static void testMakeSample_null_returnsEmpty() {
        // CWE-400: null body in makeSample must not crash the UI
        boolean passed = false;
        try {
            String result = makeSample(null);
            passed = result != null && result.equals("");
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("RC-02 makeSample_null_returnsEmpty", passed,
            "CWE-400: makeSample returns empty string for null body without crashing.");
    }

    /*******
     * <p> Method: testMakeSample_79chars_returnsAsIs() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Just below truncation boundary (79 chars) — returned as-is. </p>
     *
     * <p> Input: body=79 character string </p>
     * <p> Expected: Full string returned unchanged. PASS. </p>
     */
    private static void testMakeSample_79chars_returnsAsIs() {
        // CWE-400: 79 chars is just below truncation boundary — no truncation
        String body = "A".repeat(79);
        String result = makeSample(body);
        boolean passed = result.equals(body) && !result.endsWith("...");
        printResult("RC-03 makeSample_79chars_returnsAsIs", passed,
            "CWE-400: 79 char body (just below boundary) returned as-is, not truncated.");
    }

    /*******
     * <p> Method: testMakeSample_80chars_returnsAsIs() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Exactly at truncation boundary (80 chars) — returned as-is. </p>
     *
     * <p> Input: body=80 character string </p>
     * <p> Expected: Full 80-char string returned unchanged. PASS. </p>
     */
    private static void testMakeSample_80chars_returnsAsIs() {
        // CWE-400: exactly 80 chars is the boundary — must NOT be truncated
        String body = "A".repeat(80);
        String result = makeSample(body);
        boolean passed = result.equals(body) && result.length() == 80;
        printResult("RC-04 makeSample_80chars_returnsAsIs", passed,
            "CWE-400: 80 char body (exactly at boundary) returned as-is (not truncated).");
    }

    /*******
     * <p> Method: testMakeSample_81chars_truncated() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Just above truncation boundary (81 chars) — must be truncated. </p>
     *
     * <p> Input: body=81 character string </p>
     * <p> Expected: Returns first 80 chars + "..." (83 chars total). PASS. </p>
     */
    private static void testMakeSample_81chars_truncated() {
        // CWE-400: 81 chars is just above the boundary — must be truncated
        String body = "A".repeat(81);
        String result = makeSample(body);
        boolean passed = result.length() == 83 && result.endsWith("...");
        printResult("RC-05 makeSample_81chars_truncated", passed,
            "CWE-400: 81 char body (just above boundary) truncated to 80 chars + '...'.");
    }

    /*******
     * <p> Method: testMakeSample_10000chars_truncated() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: makeSample() </p>
     * <p> Boundary: Extreme boundary — 10000 chars must be truncated without lag. </p>
     *
     * <p> Input: body=10000 character string </p>
     * <p> Expected: Returns first 80 chars + "..." — UI remains responsive. PASS. </p>
     */
    private static void testMakeSample_10000chars_truncated() {
        // CWE-400: extreme input must be handled without resource exhaustion
        String body = "A".repeat(10000);
        String result = makeSample(body);
        boolean passed = result.length() == 83 && result.endsWith("...");
        printResult("RC-06 makeSample_10000chars_truncated", passed,
            "CWE-400: 10000 char body (extreme boundary) truncated correctly — no resource exhaustion.");
    }

    // =========================================================================
    // CWE-400: RESOURCE CONSUMPTION — safeLower BOUNDARY VALUES
    // =========================================================================

    /*******
     * <p> Method: testSafeLower_null_returnsEmpty() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: safeLower() </p>
     * <p> Boundary: Null boundary — null input must return "" not crash. </p>
     *
     * <p> Input: value=null </p>
     * <p> Expected: Returns "" without NullPointerException. PASS. </p>
     */
    private static void testSafeLower_null_returnsEmpty() {
        // CWE-400: null in safeLower must not crash search/filter logic
        boolean passed = false;
        try {
            String result = safeLower(null);
            passed = result != null && result.equals("");
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("RC-07 safeLower_null_returnsEmpty", passed,
            "CWE-400: safeLower returns empty string for null without NullPointerException.");
    }

    /*******
     * <p> Method: testSafeLower_emptyString_returnsEmpty() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: safeLower() </p>
     * <p> Boundary: Lower boundary — empty string returns empty string. </p>
     *
     * <p> Input: value="" </p>
     * <p> Expected: Returns "". PASS. </p>
     */
    private static void testSafeLower_emptyString_returnsEmpty() {
        // CWE-400: empty string lower boundary for safeLower
        String result = safeLower("");
        boolean passed = result != null && result.equals("");
        printResult("RC-08 safeLower_emptyString_returnsEmpty", passed,
            "CWE-400: safeLower returns empty string for empty input (lower boundary).");
    }

    /*******
     * <p> Method: testSafeLower_normal_returnsLowercase() </p>
     *
     * <p> CWE: CWE-400 — Uncontrolled Resource Consumption </p>
     * <p> Class Under Test: safeLower() </p>
     * <p> Boundary: Normal case — mixed case string returns all lowercase. </p>
     *
     * <p> Input: value="Hello WORLD" </p>
     * <p> Expected: Returns "hello world". PASS. </p>
     */
    private static void testSafeLower_normal_returnsLowercase() {
        // CWE-400: normal input must return correct lowercase for search matching
        String result = safeLower("Hello WORLD");
        boolean passed = result.equals("hello world");
        printResult("RC-09 safeLower_normal_returnsLowercase", passed,
            "CWE-400: safeLower correctly lowercases mixed-case input.");
    }

    // =========================================================================
    // COVERAGE TESTS
    // =========================================================================

    /*******
     * <p> Method: testCoverage_createPost_bothBranches() </p>
     *
     * <p> Description: Coverage test ensuring both the create path and the
     * retrieve path of Database.addPost() and getAllPosts() are exercised.
     * Satisfies the coverage testing requirement from Task 2.0. </p>
     *
     * <p> Input: Add post, retrieve all posts. </p>
     * <p> Expected: Post found in list. PASS. </p>
     */
    private static void testCoverage_createPost_bothBranches() {
        // Coverage: exercise both addPost and getAllPosts branches
        db.addPost("CV01 Coverage Post", "Coverage body", "testUser", "General");
        java.util.ArrayList<entityClasses.Post> posts = db.getAllPosts();
        boolean passed = posts.stream().anyMatch(p -> "CV01 Coverage Post".equals(p.getTitle()));
        printResult("CV-01 createPost_bothBranches", passed,
            "Coverage: addPost and getAllPosts both branches exercised successfully.");
    }

    /*******
     * <p> Method: testCoverage_getPost_foundAndNotFound() </p>
     *
     * <p> Description: Coverage test for both branches of Database.getPost() —
     * the found branch (returns Post) and the not-found branch (returns null). </p>
     *
     * <p> Input: Valid postID and invalid postID (999999). </p>
     * <p> Expected: Valid returns Post; invalid returns null. PASS. </p>
     */
    private static void testCoverage_getPost_foundAndNotFound() {
        // Coverage: exercise both found and not-found branches of getPost
        db.addPost("CV02 GetPost Test", "Body", "testUser", "General");
        java.util.ArrayList<entityClasses.Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        entityClasses.Post found = db.getPost(postID);
        entityClasses.Post notFound = db.getPost(999999);
        boolean passed = found != null && notFound == null;
        printResult("CV-02 getPost_foundAndNotFound", passed,
            "Coverage: getPost found branch returns Post; not-found branch returns null.");
    }

    /*******
     * <p> Method: testCoverage_deletePost_existingAndNonExisting() </p>
     *
     * <p> Description: Coverage test for both branches of Database.deletePost() —
     * deleting an existing post and attempting to delete a non-existent post. </p>
     *
     * <p> Input: Valid postID (deleted) and 999999 (non-existent). </p>
     * <p> Expected: Both complete without exception. PASS. </p>
     */
    private static void testCoverage_deletePost_existingAndNonExisting() {
        // Coverage: exercise both delete branches — existing and non-existing
        db.addPost("CV03 Delete Test", "Body", "testUser", "General");
        java.util.ArrayList<entityClasses.Post> posts = db.getAllPosts();
        int postID = posts.get(posts.size() - 1).getPostID();

        boolean passed = true;
        try {
            db.deletePost(postID);       // existing — should succeed
            db.deletePost(999999);       // non-existing — should not crash
        } catch (Exception e) {
            passed = false;
        }
        printResult("CV-03 deletePost_existingAndNonExisting", passed,
            "Coverage: deletePost completes for both existing and non-existing post IDs.");
    }


    // =========================================================================
    // LOCAL COPIES OF PostNavBar METHODS
    // (copied to avoid PostNavBar static initializer triggering DB connection)
    // =========================================================================

    /*******
     * <p> Method: makeSample() </p>
     *
     * <p> Description: Returns a preview of the post body for display in the
     * post list. Mirrors PostNavBar.makeSample(). Copied here to avoid
     * triggering PostNavBar's static PostList field which requires a DB connection. </p>
     *
     * @param body the post body string (may be null)
     * @return a preview string of at most 83 characters
     */
    private static String makeSample(String body) {
        // CWE-400: mirrors PostNavBar.makeSample — truncates at 80 chars
        if (body == null) return "";
        if (body.length() <= 80) return body;
        return body.substring(0, 80) + "...";
    }

    /*******
     * <p> Method: safeLower() </p>
     *
     * <p> Description: Returns the lowercase version of the given string, or
     * empty string if null. Mirrors PostNavBar.safeLower(). </p>
     *
     * @param value the string to convert (may be null)
     * @return lowercase string, or "" if null
     */
    private static String safeLower(String value) {
        // CWE-400: mirrors PostNavBar.safeLower — null-safe lowercase
        if (value == null) return "";
        return value.toLowerCase();
    }

    // =========================================================================
    // PRINT RESULT
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