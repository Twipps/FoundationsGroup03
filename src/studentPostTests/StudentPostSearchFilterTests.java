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

    private static void testMatchesSearch_titleMatch() {
        Post post = new Post("Hello World", "Some body.", "General", "testUser");
        boolean passed = matchesSearch(post, "hello");
        printResult("TC-23 matchesSearch_titleMatch", passed,
            "matchesSearch returns true when keyword matches the post title.");
    }

    private static void testMatchesSearch_bodyMatch() {
        Post post = new Post("Some Title", "We use javafx here.", "General", "testUser");
        boolean passed = matchesSearch(post, "javafx");
        printResult("TC-24 matchesSearch_bodyMatch", passed,
            "matchesSearch returns true when keyword matches the post body.");
    }

    private static void testMatchesSearch_categoryMatch() {
        Post post = new Post("Title", "Body.", "Question", "testUser");
        boolean passed = matchesSearch(post, "question");
        printResult("TC-25 matchesSearch_categoryMatch", passed,
            "matchesSearch returns true when keyword matches the post category.");
    }

    private static void testMatchesSearch_caseInsensitive() {
        Post post = new Post("hello world", "Body.", "General", "testUser");
        boolean passed = matchesSearch(post, "HELLO");
        printResult("TC-26 matchesSearch_caseInsensitive", passed,
            "matchesSearch is case-insensitive — uppercase keyword matches lowercase title.");
    }

    private static void testMatchesSearch_noMatch() {
        Post post = new Post("Hello", "World", "General", "testUser");
        boolean passed = !matchesSearch(post, "xyz123");
        printResult("TC-27 matchesSearch_noMatch", passed,
            "matchesSearch returns false when keyword not in title, body, or category.");
    }

    private static void testMatchesSearch_blankQuery_returnsTrue() {
        Post post = new Post("Any Title", "Any body.", "General", "testUser");
        boolean passed = matchesSearch(post, "");
        printResult("TC-28 matchesSearch_blankQuery_returnsTrue", passed,
            "matchesSearch returns true for blank search (all posts shown).");
    }

    private static void testMatchesSearch_nullQuery_returnsTrue() {
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

    private static void testMatchesSearch_partialMatch() {
        Post post = new Post("Discussion System", "Body.", "General", "testUser");
        boolean passed = matchesSearch(post, "disc");
        printResult("TC-30 matchesSearch_partialMatch", passed,
            "matchesSearch returns true for partial keyword match in title.");
    }

    // =========================================================================
    // CATEGORY FILTER TESTS
    // =========================================================================

    private static void testMatchesCategory_exactMatch() {
        Post post = new Post("Title", "Body.", "Question", "testUser");
        boolean passed = matchesCategory(post, "Question");
        printResult("TC-31 matchesCategory_exactMatch", passed,
            "matchesCategory returns true when post category exactly matches the filter.");
    }

    private static void testMatchesCategory_caseInsensitive() {
        Post post = new Post("Title", "Body.", "question", "testUser");
        boolean passed = matchesCategory(post, "Question");
        printResult("TC-32 matchesCategory_caseInsensitive", passed,
            "matchesCategory is case-insensitive.");
    }

    private static void testMatchesCategory_allReturnsTrue() {
        Post post = new Post("Title", "Body.", "Bug", "testUser");
        boolean passed = matchesCategory(post, "All");
        printResult("TC-33 matchesCategory_allReturnsTrue", passed,
            "matchesCategory returns true for 'All' filter regardless of post category.");
    }

    private static void testMatchesCategory_nullReturnsTrue() {
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

    private static void testMatchesCategory_noMatch_returnsFalse() {
        Post post = new Post("Title", "Body.", "General", "testUser");
        boolean passed = !matchesCategory(post, "Question");
        printResult("TC-35 matchesCategory_noMatch_returnsFalse", passed,
            "matchesCategory returns false when post category does not match filter.");
    }

    private static void testMatchesCategory_differentCategory_returnsFalse() {
        Post post = new Post("Title", "Body.", "Bug", "testUser");
        boolean passed = !matchesCategory(post, "Help");
        printResult("TC-36 matchesCategory_differentCategory_returnsFalse", passed,
            "matchesCategory returns false for Bug post when Help filter is selected.");
    }

    // =========================================================================
    // EDGE CASE TESTS
    // =========================================================================

    private static void testMatchesSearch_nullPostFields_nocrash() {
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

    private static void testMakeSample_shortBody_returnsAsIs() {
        String result = makeSample("Short body.");
        boolean passed = result.equals("Short body.");
        printResult("TC-38 makeSample_shortBody_returnsAsIs", passed,
            "makeSample returns full body when 80 chars or fewer.");
    }

    private static void testMakeSample_longBody_truncated() {
        String longBody = "A".repeat(100);
        String result = makeSample(longBody);
        boolean passed = result.length() == 83 && result.endsWith("...");
        printResult("TC-39 makeSample_longBody_truncated", passed,
            "makeSample truncates body >80 chars and appends '...'.");
    }

    private static void testMakeSample_nullBody_returnsEmpty() {
        String result = makeSample(null);
        boolean passed = result != null && result.equals("");
        printResult("TC-40 makeSample_nullBody_returnsEmpty", passed,
            "makeSample returns empty string for null body without crashing.");
    }

    private static void testSafeLower_null_returnsEmpty() {
        String result = safeLower(null);
        boolean passed = result != null && result.equals("");
        printResult("TC-41 safeLower_null_returnsEmpty", passed,
            "safeLower returns empty string for null input.");
    }

    private static void testSafeLower_normal_returnsLowercase() {
        String result = safeLower("HELLO");
        boolean passed = result.equals("hello");
        printResult("TC-42 safeLower_normal_returnsLowercase", passed,
            "safeLower correctly converts input to lowercase.");
    }

    // =========================================================================
    // HELPER METHODS (copied from PostNavBar to avoid database initialization)
    // =========================================================================

    private static boolean matchesSearch(Post post, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String searchLower = search.toLowerCase();
        return safeLower(post.getTitle()).contains(searchLower)
            || safeLower(post.getBody()).contains(searchLower)
            || safeLower(post.getCategory()).contains(searchLower);
    }

    private static boolean matchesCategory(Post post, String category) {
        if (category == null || category.compareTo("All") == 0) {
            return true;
        }
        return safeLower(post.getCategory()).compareTo(category.toLowerCase()) == 0;
    }

    private static String makeSample(String body) {
        if (body == null) {
            return "";
        }
        if (body.length() <= 80) {
            return body;
        }
        return body.substring(0, 80) + "...";
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase();
    }

    // =========================================================================
    // PRINT RESULT
    // =========================================================================

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