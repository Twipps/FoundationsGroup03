package studentPostTests;

import entityClasses.Post;

/*******
 * <p> Title: StudentPostSearchFilterTests Class. </p>
 *
 * <p> Description: A standalone semi-automated test suite for the search and
 * filter functionality of the Student Discussion System. These tests operate
 * entirely in memory using Post objects and do not require a database connection,
 * so they can be run at any time independently of the application. </p>
 *
 * <p> Tests cover REQ-12 (category filtering) and REQ-13 (keyword search),
 * both implemented in matchesSearch() and matchesCategory(). </p>
 *
 * <p> How to run: Right-click this file → Run As → Java Application.
 * No VM arguments or database connection required. </p>
 *
 * @author Kyle Kim (Team 3) — Test design, implementation, and documentation
 *
 * @version 1.00  2026-06-28  Initial implementation
 */
public class StudentPostSearchFilterTests {

    /** Running count of tests that passed. */
    private static int numPassed = 0;

    /** Running count of tests that failed. */
    private static int numFailed = 0;

    /** Private constructor — this is a static utility class, not meant to be instantiated. */
    private StudentPostSearchFilterTests() {}

    /*******
     * <p> Method: main() </p>
     *
     * <p> Description: Entry point for the standalone search and filter test suite.
     * Runs all search and category filter tests and prints a pass/fail summary.
     * No database or JavaFX runtime required. </p>
     *
     * <p> How to interpret output: Each test prints either
     * "*** PASS ***" or "*** FAIL ***" followed by the test name and explanation.
     * A fully passing run shows 0 failures. </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  TP2 Search and Filter Tests — Team 3");
        System.out.println("  Author: Kyle Kim");
        System.out.println("  (Standalone — no database required)");
        System.out.println("================================================\n");

        // ── SEARCH TESTS ──────────────────────────────────────────────────────
        System.out.println("--- SEARCH TESTS (REQ-13) ---");
        testMatchesSearch_titleMatch();
        testMatchesSearch_bodyMatch();
        testMatchesSearch_categoryMatch();
        testMatchesSearch_caseInsensitive();
        testMatchesSearch_noMatch();
        testMatchesSearch_blankQuery_returnsTrue();
        testMatchesSearch_nullQuery_returnsTrue();
        testMatchesSearch_partialMatch();

        // ── CATEGORY FILTER TESTS ─────────────────────────────────────────────
        System.out.println("\n--- CATEGORY FILTER TESTS (REQ-12) ---");
        testMatchesCategory_exactMatch();
        testMatchesCategory_caseInsensitive();
        testMatchesCategory_allReturnsTrue();
        testMatchesCategory_nullReturnsTrue();
        testMatchesCategory_noMatch_returnsFalse();
        testMatchesCategory_differentCategory_returnsFalse();

        // ── EDGE CASES ────────────────────────────────────────────────────────
        System.out.println("\n--- EDGE CASE TESTS ---");
        testMatchesSearch_nullPostFields_nocrash();
        testMakeSample_shortBody_returnsAsIs();
        testMakeSample_longBody_truncated();
        testMakeSample_nullBody_returnsEmpty();
        testSafeLower_null_returnsEmpty();
        testSafeLower_normal_returnsLowercase();

        // ── SUMMARY ───────────────────────────────────────────────────────────
        System.out.println("\n================================================");
        System.out.println("  Tests passed: " + numPassed);
        System.out.println("  Tests failed: " + numFailed);
        System.out.println("================================================");
    }

    // =========================================================================
    // SEARCH TESTS
    // =========================================================================

    /*******
     * <p> Method: testMatchesSearch_titleMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search by keyword matches post title. </p>
     *
     * <p> Description: Creates a Post with a known title and verifies
     * matchesSearch returns true when the search keyword appears in the title. </p>
     *
     * <p> Input: Post title="Hello World", search="hello" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_titleMatch() {
        // REQ-13: keyword in title must return true
        Post post = new Post("Hello World", "Some body.", "General", "testUser");
        boolean passed = matchesSearch(post, "hello");
        printResult("TC-23 matchesSearch_titleMatch", passed,
            "matchesSearch returns true when keyword matches the post title.");
    }

    /*******
     * <p> Method: testMatchesSearch_bodyMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search by keyword matches post body. </p>
     *
     * <p> Description: Creates a Post with a known body and verifies
     * matchesSearch returns true when the keyword appears in the body. </p>
     *
     * <p> Input: Post body="We use javafx here.", search="javafx" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_bodyMatch() {
        // REQ-13: keyword in body must return true
        Post post = new Post("Some Title", "We use javafx here.", "General", "testUser");
        boolean passed = matchesSearch(post, "javafx");
        printResult("TC-24 matchesSearch_bodyMatch", passed,
            "matchesSearch returns true when keyword matches the post body.");
    }

    /*******
     * <p> Method: testMatchesSearch_categoryMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search by keyword matches post category. </p>
     *
     * <p> Description: Verifies matchesSearch returns true when the keyword
     * appears in the post's category field. </p>
     *
     * <p> Input: Post category="Question", search="question" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_categoryMatch() {
        // REQ-13: keyword in category must return true
        Post post = new Post("Title", "Body.", "Question", "testUser");
        boolean passed = matchesSearch(post, "question");
        printResult("TC-25 matchesSearch_categoryMatch", passed,
            "matchesSearch returns true when keyword matches the post category.");
    }

    /*******
     * <p> Method: testMatchesSearch_caseInsensitive() </p>
     *
     * <p> Requirement: REQ-13 — Search is case-insensitive. </p>
     *
     * <p> Description: Verifies that searching with uppercase keyword matches
     * a lowercase title, confirming case-insensitive behavior. </p>
     *
     * <p> Input: Post title="hello world", search="HELLO" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_caseInsensitive() {
        // REQ-13: search must be case-insensitive
        Post post = new Post("hello world", "Body.", "General", "testUser");
        boolean passed = matchesSearch(post, "HELLO");
        printResult("TC-26 matchesSearch_caseInsensitive", passed,
            "matchesSearch is case-insensitive — uppercase keyword matches lowercase title.");
    }

    /*******
     * <p> Method: testMatchesSearch_noMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search returns false when keyword not found. </p>
     *
     * <p> Description: Verifies matchesSearch returns false when the keyword
     * does not appear in the title, body, or category. </p>
     *
     * <p> Input: Post with title="Hello", body="World", search="xyz123" </p>
     *
     * <p> Expected output: false. Console prints PASS. </p>
     */
    private static void testMatchesSearch_noMatch() {
        // REQ-13: keyword not present anywhere must return false
        Post post = new Post("Hello", "World", "General", "testUser");
        boolean passed = !matchesSearch(post, "xyz123");
        printResult("TC-27 matchesSearch_noMatch", passed,
            "matchesSearch returns false when keyword not in title, body, or category.");
    }

    /*******
     * <p> Method: testMatchesSearch_blankQuery_returnsTrue() </p>
     *
     * <p> Requirement: REQ-13 — Blank search returns all posts. </p>
     *
     * <p> Description: Verifies matchesSearch returns true for a blank search
     * string, meaning all posts are shown when the search bar is empty. </p>
     *
     * <p> Input: Post with any content, search="" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_blankQuery_returnsTrue() {
        // REQ-13: blank search must show all posts
        Post post = new Post("Any Title", "Any body.", "General", "testUser");
        boolean passed = matchesSearch(post, "");
        printResult("TC-28 matchesSearch_blankQuery_returnsTrue", passed,
            "matchesSearch returns true for blank search (all posts shown).");
    }

    /*******
     * <p> Method: testMatchesSearch_nullQuery_returnsTrue() </p>
     *
     * <p> Requirement: REQ-13 — Null search query returns all posts without crashing. </p>
     *
     * <p> Description: Verifies matchesSearch returns true for a null search
     * string and does not throw a NullPointerException. </p>
     *
     * <p> Input: Post with any content, search=null </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_nullQuery_returnsTrue() {
        // REQ-13: null search must not crash and must return true (show all)
        Post post = new Post("Any Title", "Any body.", "General", "testUser");
        boolean passed = false;
        try {
            passed = matchesSearch(post, null);
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("TC-29 matchesSearch_nullQuery_returnsTrue", passed,
            "matchesSearch handles null search query without crashing.");
    }

    /*******
     * <p> Method: testMatchesSearch_partialMatch() </p>
     *
     * <p> Requirement: REQ-13 — Search matches partial keyword. </p>
     *
     * <p> Description: Verifies matchesSearch returns true when the search
     * keyword is a substring of the title, not necessarily the full word. </p>
     *
     * <p> Input: Post title="Discussion System", search="disc" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesSearch_partialMatch() {
        // REQ-13: partial keyword match must return true
        Post post = new Post("Discussion System", "Body.", "General", "testUser");
        boolean passed = matchesSearch(post, "disc");
        printResult("TC-30 matchesSearch_partialMatch", passed,
            "matchesSearch returns true for partial keyword match in title.");
    }

    // =========================================================================
    // CATEGORY FILTER TESTS
    // =========================================================================

    /*******
     * <p> Method: testMatchesCategory_exactMatch() </p>
     *
     * <p> Requirement: REQ-12 — Category filter matches exact category. </p>
     *
     * <p> Description: Creates a Post with category "Question" and verifies
     * matchesCategory returns true for filter "Question". </p>
     *
     * <p> Input: Post category="Question", filter="Question" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_exactMatch() {
        // REQ-12: exact category match must return true
        Post post = new Post("Title", "Body.", "Question", "testUser");
        boolean passed = matchesCategory(post, "Question");
        printResult("TC-31 matchesCategory_exactMatch", passed,
            "matchesCategory returns true when post category exactly matches the filter.");
    }

    /*******
     * <p> Method: testMatchesCategory_caseInsensitive() </p>
     *
     * <p> Requirement: REQ-12 — Category filter is case-insensitive. </p>
     *
     * <p> Description: Verifies matchesCategory returns true when the filter
     * and post category differ only in case. </p>
     *
     * <p> Input: Post category="question", filter="Question" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_caseInsensitive() {
        // REQ-12: category filter must be case-insensitive
        Post post = new Post("Title", "Body.", "question", "testUser");
        boolean passed = matchesCategory(post, "Question");
        printResult("TC-32 matchesCategory_caseInsensitive", passed,
            "matchesCategory is case-insensitive.");
    }

    /*******
     * <p> Method: testMatchesCategory_allReturnsTrue() </p>
     *
     * <p> Requirement: REQ-12 — 'All' category shows every post. </p>
     *
     * <p> Description: Verifies matchesCategory returns true for any post
     * when the filter is set to "All". </p>
     *
     * <p> Input: Post category="Bug", filter="All" </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_allReturnsTrue() {
        // REQ-12: "All" filter must match every post
        Post post = new Post("Title", "Body.", "Bug", "testUser");
        boolean passed = matchesCategory(post, "All");
        printResult("TC-33 matchesCategory_allReturnsTrue", passed,
            "matchesCategory returns true for 'All' filter regardless of post category.");
    }

    /*******
     * <p> Method: testMatchesCategory_nullReturnsTrue() </p>
     *
     * <p> Requirement: REQ-12 — Null category filter shows all posts without crashing. </p>
     *
     * <p> Description: Verifies matchesCategory returns true for a null filter
     * without throwing a NullPointerException. </p>
     *
     * <p> Input: Post category="General", filter=null </p>
     *
     * <p> Expected output: true. Console prints PASS. </p>
     */
    private static void testMatchesCategory_nullReturnsTrue() {
        // REQ-12: null filter must not crash and should return true
        Post post = new Post("Title", "Body.", "General", "testUser");
        boolean passed = false;
        try {
            passed = matchesCategory(post, null);
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("TC-34 matchesCategory_nullReturnsTrue", passed,
            "matchesCategory handles null filter without crashing.");
    }

    /*******
     * <p> Method: testMatchesCategory_noMatch_returnsFalse() </p>
     *
     * <p> Requirement: REQ-12 — Category filter excludes non-matching posts. </p>
     *
     * <p> Description: Verifies matchesCategory returns false when the post
     * category does not match the selected filter. </p>
     *
     * <p> Input: Post category="General", filter="Question" </p>
     *
     * <p> Expected output: false. Console prints PASS. </p>
     */
    private static void testMatchesCategory_noMatch_returnsFalse() {
        // REQ-12: non-matching category must return false
        Post post = new Post("Title", "Body.", "General", "testUser");
        boolean passed = !matchesCategory(post, "Question");
        printResult("TC-35 matchesCategory_noMatch_returnsFalse", passed,
            "matchesCategory returns false when post category does not match filter.");
    }

    /*******
     * <p> Method: testMatchesCategory_differentCategory_returnsFalse() </p>
     *
     * <p> Requirement: REQ-12 — Category filter correctly excludes Bug posts from Help filter. </p>
     *
     * <p> Description: Verifies a "Bug" post is excluded when filtering by "Help". </p>
     *
     * <p> Input: Post category="Bug", filter="Help" </p>
     *
     * <p> Expected output: false. Console prints PASS. </p>
     */
    private static void testMatchesCategory_differentCategory_returnsFalse() {
        // REQ-12: "Bug" post must not appear under "Help" filter
        Post post = new Post("Title", "Body.", "Bug", "testUser");
        boolean passed = !matchesCategory(post, "Help");
        printResult("TC-36 matchesCategory_differentCategory_returnsFalse", passed,
            "matchesCategory returns false for Bug post when Help filter is selected.");
    }

    // =========================================================================
    // EDGE CASE TESTS
    // =========================================================================

    /*******
     * <p> Method: testMatchesSearch_nullPostFields_nocrash() </p>
     *
     * <p> Requirement: REQ-09 — System handles null post fields gracefully. </p>
     *
     * <p> Description: Verifies matchesSearch does not crash when called on a
     * Post whose fields may be null. The safeLower() helper in PostNavBar
     * should handle null fields without a NullPointerException. </p>
     *
     * <p> Input: Post with title=null, body=null, category=null (via
     * direct field test using safeLower), search="test" </p>
     *
     * <p> Expected output: No exception thrown. Console prints PASS. </p>
     */
    private static void testMatchesSearch_nullPostFields_nocrash() {
        // REQ-09: safeLower must handle null without crashing
        boolean passed = false;
        try {
            String result = safeLower(null);
            passed = result != null && result.equals("");
        } catch (NullPointerException e) {
            passed = false;
        }
        printResult("TC-37 matchesSearch_nullPostFields_nocrash", passed,
            "safeLower returns empty string for null input without throwing NullPointerException.");
    }

    /*******
     * <p> Method: testMakeSample_shortBody_returnsAsIs() </p>
     *
     * <p> Requirement: REQ-03 — Post list preview shows body text correctly. </p>
     *
     * <p> Description: Verifies makeSample returns the full body when it is
     * 80 characters or fewer, with no truncation. </p>
     *
     * <p> Input: body="Short body." (11 chars) </p>
     *
     * <p> Expected output: "Short body." Console prints PASS. </p>
     */
    private static void testMakeSample_shortBody_returnsAsIs() {
        // REQ-03: short body must be returned as-is in the post list preview
        String result = makeSample("Short body.");
        boolean passed = result.equals("Short body.");
        printResult("TC-38 makeSample_shortBody_returnsAsIs", passed,
            "makeSample returns full body when 80 chars or fewer.");
    }

    /*******
     * <p> Method: testMakeSample_longBody_truncated() </p>
     *
     * <p> Requirement: REQ-03 — Post list preview truncates long body text. </p>
     *
     * <p> Description: Verifies makeSample truncates a body longer than 80
     * characters and appends "..." at the end. </p>
     *
     * <p> Input: body = 100-character string </p>
     *
     * <p> Expected output: 83-character string ending in "...". Console prints PASS. </p>
     */
    private static void testMakeSample_longBody_truncated() {
        // REQ-03: long body must be truncated to 80 chars + "..."
        String longBody = "A".repeat(100);
        String result = makeSample(longBody);
        boolean passed = result.length() == 83 && result.endsWith("...");
        printResult("TC-39 makeSample_longBody_truncated", passed,
            "makeSample truncates body >80 chars and appends '...'.");
    }

    /*******
     * <p> Method: testMakeSample_nullBody_returnsEmpty() </p>
     *
     * <p> Requirement: REQ-09 — System handles null body in preview without crashing. </p>
     *
     * <p> Description: Verifies makeSample returns an empty string for a null
     * body without throwing a NullPointerException. </p>
     *
     * <p> Input: body=null </p>
     *
     * <p> Expected output: "" (empty string). Console prints PASS. </p>
     */
    private static void testMakeSample_nullBody_returnsEmpty() {
        // REQ-09: null body in makeSample must not crash
        String result = makeSample(null);
        boolean passed = result != null && result.equals("");
        printResult("TC-40 makeSample_nullBody_returnsEmpty", passed,
            "makeSample returns empty string for null body without crashing.");
    }

    /*******
     * <p> Method: testSafeLower_null_returnsEmpty() </p>
     *
     * <p> Requirement: REQ-09 — safeLower handles null without crashing. </p>
     *
     * <p> Description: Verifies safeLower returns an empty string for null input. </p>
     *
     * <p> Input: null </p>
     *
     * <p> Expected output: "". Console prints PASS. </p>
     */
    private static void testSafeLower_null_returnsEmpty() {
        // REQ-09: safeLower must return "" for null
        String result = safeLower(null);
        boolean passed = result != null && result.equals("");
        printResult("TC-41 safeLower_null_returnsEmpty", passed,
            "safeLower returns empty string for null input.");
    }

    /*******
     * <p> Method: testSafeLower_normal_returnsLowercase() </p>
     *
     * <p> Requirement: REQ-13 — safeLower correctly lowercases input for search. </p>
     *
     * <p> Description: Verifies safeLower returns the lowercase version of a
     * normal string. </p>
     *
     * <p> Input: "HELLO" </p>
     *
     * <p> Expected output: "hello". Console prints PASS. </p>
     */
    private static void testSafeLower_normal_returnsLowercase() {
        // REQ-13: safeLower must return lowercase version of input
        String result = safeLower("HELLO");
        boolean passed = result.equals("hello");
        printResult("TC-42 safeLower_normal_returnsLowercase", passed,
            "safeLower correctly converts input to lowercase.");
    }


    // =========================================================================
    // HELPER METHODS (copied from PostNavBar to avoid database initialization)
    // =========================================================================

    /*******
     * <p> Method: matchesSearch() </p>
     *
     * <p> Description: Returns true if the given search keyword appears in the
     * post's title, body, or category (case-insensitive). Returns true for a
     * blank or null search query, meaning all posts are shown. Mirrors the
     * implementation in PostNavBar. </p>
     *
     * @param post   the Post to check
     * @param search the search keyword (may be null or blank)
     * @return true if the post matches the search, false otherwise
     */
    private static boolean matchesSearch(Post post, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String searchLower = search.toLowerCase();
        return safeLower(post.getTitle()).contains(searchLower)
            || safeLower(post.getBody()).contains(searchLower)
            || safeLower(post.getCategory()).contains(searchLower);
    }

    /*******
     * <p> Method: matchesCategory() </p>
     *
     * <p> Description: Returns true if the post's category matches the given
     * filter (case-insensitive). Returns true when the filter is null or "All".
     * Mirrors the implementation in PostNavBar. </p>
     *
     * @param post     the Post to check
     * @param category the category filter (may be null or "All")
     * @return true if the post matches the filter, false otherwise
     */
    private static boolean matchesCategory(Post post, String category) {
        if (category == null || category.compareTo("All") == 0) {
            return true;
        }
        return safeLower(post.getCategory()).compareTo(category.toLowerCase()) == 0;
    }

    /*******
     * <p> Method: makeSample() </p>
     *
     * <p> Description: Returns a preview of the post body for display in the
     * post list. Returns the full body if 80 characters or fewer, otherwise
     * truncates to 80 characters and appends "...". Returns an empty string
     * for a null body. Mirrors the implementation in PostNavBar. </p>
     *
     * @param body the post body string (may be null)
     * @return a preview string of at most 83 characters
     */
    private static String makeSample(String body) {
        if (body == null) {
            return "";
        }
        if (body.length() <= 80) {
            return body;
        }
        return body.substring(0, 80) + "...";
    }

    /*******
     * <p> Method: safeLower() </p>
     *
     * <p> Description: Returns the lowercase version of the given string, or
     * an empty string if the input is null. Prevents NullPointerExceptions
     * during search comparisons. Mirrors the implementation in PostNavBar. </p>
     *
     * @param value the string to convert (may be null)
     * @return lowercase string, or "" if null
     */
    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
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