package entityClasses;

import java.sql.Timestamp;

/*******
 * <p> Title: Post Class </p>
 *
 * <p> Description: This class stores all the information associated with a
 * student discussion post in the Student Discussion System. It is used by the
 * GUI layer (PostDisplayPanel, PostNavBar, PostReplyEditPanel) to display,
 * create, edit, and delete posts, and by the database layer (Database.java)
 * to persist post data to the H2 database. </p>
 *
 * <p> This class is placed in the entityClasses package alongside User,
 * PasswordDTO, Reply, PostList, and ReplyList, following the established
 * Foundations codebase pattern for entity data objects. </p>
 *
 * <p> CRUD Implementation: </p>
 * <p> - CREATE: A new Post is created using the first constructor
 *   (Post(String, String, String, String)) and then persisted to the database
 *   via Database.addPost(). This satisfies the user story: "As a student,
 *   I can post statements and questions." (REQ-01) </p>
 * <p> - READ: Post data is retrieved from the database via Database.getPost()
 *   and Database.getAllPosts(), which reconstruct Post objects using the second
 *   constructor. Students can view all posts (REQ-03) and individual posts
 *   with their replies (REQ-04). </p>
 * <p> - UPDATE: The updateTitle(), updateBody(), and updateCategory() methods
 *   modify the Post object in memory. These changes are persisted to the
 *   database via Database.updatePostTitle(), Database.updatePostBody(), and
 *   Database.updatePostCategory(). This satisfies REQ-05. </p>
 * <p> - DELETE: Posts are removed from the database via Database.deletePost().
 *   The GUI (PostReplyEditPanel) presents an "Are you sure?" confirmation
 *   before calling this method, as required by the user story: "As a student,
 *   I can delete one of my posts. When I do this, I receive an 'Are you sure?'
 *   question before the delete takes place." (REQ-07) </p>
 *
 * <p> Attribute Rationale: </p>
 * <p> - postID: Unique database identifier. Set to -1 before insertion and
 *   assigned by the database on insert. Required for all CRUD operations that
 *   target a specific post. </p>
 * <p> - title: The subject line of the post, shown in the post list.
 *   Required by REQ-01 (create post) and REQ-03 (view post list). </p>
 * <p> - body: The full text content of the post. Required by REQ-01. </p>
 * <p> - author: The username of the student who created the post. Used to
 *   enforce post ownership — only the author can edit or delete their post
 *   (REQ-05, REQ-07, REQ-10). Also required by TP3 staff epics for reviewing
 *   student participation. </p>
 * <p> - category: The discussion thread the post belongs to (e.g. "General",
 *   "Question"). Required by the user story: "As a student, I can post to
 *   different threads. If I do not specify a thread, it defaults to the
 *   'General' thread." (REQ-12). Also required by TP3 staff epics for
 *   managing and filtering discussion threads. </p>
 * <p> - createdDate: Timestamp recorded when the post is first created.
 *   Shown in the post display panel. Required for post ordering and TP3
 *   staff review of student participation timelines. </p>
 * <p> - modifiedDate: Timestamp recorded when the post is last edited.
 *   Null if the post has never been edited. Supports audit trail for
 *   TP3 staff review epics. </p>
 *
 * <p> TP3 Forward-Looking Design Notes: </p>
 * <p> The following attributes are NOT yet implemented but are anticipated
 *   for TP3 based on the Staff epics in the User Stories document: </p>
 * <p> - isDeleted (boolean): Planned for soft-delete support. When a post
 *   is deleted, marking it as deleted rather than removing the row would
 *   allow replies to remain visible with a "original post has been deleted"
 *   message, as specified in the Students User Stories. Currently a known
 *   limitation — deleting a post also removes its replies. </p>
 * <p> - replyCount (int): Planned for TP3 to support the user story:
 *   "As a student, I can see a list of my posts, the number of replies."
 *   Currently reply count is computed at display time by querying the
 *   database rather than being stored on the Post object. </p>
 * <p> - isRead (boolean): Planned for TP3 to support read/unread tracking
 *   per the user story: "I can see which ones I have read and which I have
 *   not." Not yet implemented as it requires a per-user read-status table. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented Post class,
 * MVC GUI packages, and database integration
 * @author Kyle Kim (Team 3) — Added TP3 forward-looking documentation,
 * User Story mappings, and CRUD rationale
 *
 * @version 1.00  2026-06-XX  Initial implementation (James Suchovic)
 * @version 1.01  2026-06-28  Added full Javadoc, User Story mappings,
 *                             and TP3 forward-looking notes (Kyle Kim)
 */
public class Post {

	// ── Attributes ────────────────────────────────────────────────────────────

	/** Unique database identifier for this post.
	 *  Set to -1 before the post is inserted into the database.
	 *  Assigned by the H2 database auto-increment on addPost(). */
	private int postID;

	/** The title/subject of the post, displayed in the post list nav bar.
	 *  Required by REQ-01 (create post) and REQ-03 (view post list).
	 *  Input validated by PostReplyEditPanel — must not be null or blank. */
	private String title;

	/** The full body text of the post, displayed in PostDisplayPanel.
	 *  Required by REQ-01.
	 *  Input validated by PostReplyEditPanel — must not be null or blank. */
	private String body;

	/** The username of the student who authored this post.
	 *  Used to enforce post ownership for edit/delete operations (REQ-10).
	 *  Also required by TP3 staff epics for reviewing student participation. */
	private String author;

	/** The discussion thread/category this post belongs to.
	 *  Defaults to "General" if the student does not specify a thread.
	 *  Required by REQ-12 (post to different threads) and REQ-13 (search by thread).
	 *  Also required by TP3 staff epics for creating, managing, and deleting threads. */
	private String category;

	/** Timestamp recording when this post was first created.
	 *  Set automatically at construction time using System.currentTimeMillis().
	 *  Displayed in PostDisplayPanel. Required for TP3 staff participation review. */
	private Timestamp createdDate;

	/** Timestamp recording when this post was last edited.
	 *  Null if the post has never been modified.
	 *  Updated automatically by updateTitle(), updateBody(), updateCategory().
	 *  Supports TP3 staff audit trail for reviewing edited posts. */
	private Timestamp modifiedDate;

	// ── Constructors ──────────────────────────────────────────────────────────

	/*******
	 * <p> Method: Post() — New Post Constructor </p>
	 *
	 * <p> Description: Creates a new Post object before it is inserted into
	 * the database. Sets postID to -1 (indicating no database ID yet) and
	 * records the current time as the creation timestamp. </p>
	 *
	 * <p> This constructor is called by PostReplyEditPanel when a student
	 * submits a new post. The post is then persisted via Database.addPost().
	 * Satisfies REQ-01: "As a student, I can post statements and questions." </p>
	 *
	 * @param inTitle    the title of the post — must not be null or blank
	 * @param inBody     the body text of the post — must not be null or blank
	 * @param inCategory the discussion thread category — defaults to "General"
	 * @param inAuthor   the username of the student creating the post
	 */
	public Post(String inTitle, String inBody, String inCategory, String inAuthor) {
		// REQ-01: Initialize a new post with all required fields
		postID = -1; // -1 indicates the post has not yet been assigned a database ID
		title = inTitle;
		body = inBody;
		author = inAuthor;
		category = inCategory; // REQ-12: category supports thread filtering
		createdDate = new Timestamp(System.currentTimeMillis()); // record creation time
		modifiedDate = null; // null indicates the post has never been modified
	}

	/*******
	 * <p> Method: Post() — Database Reconstruction Constructor </p>
	 *
	 * <p> Description: Creates a Post object from an existing database record.
	 * Used by Database.getAllPosts() and Database.getPost() to reconstruct
	 * Post objects from the H2 database ResultSet. </p>
	 *
	 * <p> Satisfies REQ-03 (view list of posts) and REQ-04 (view single post). </p>
	 *
	 * @param inPostID       the unique database identifier for the post
	 * @param inTitle        the title of the post
	 * @param inBody         the body text of the post
	 * @param inCategory     the discussion thread category
	 * @param inAuthor       the username of the student who created the post
	 * @param inCreatedDate  the timestamp when the post was created
	 * @param inModifiedDate the timestamp when the post was last modified,
	 *                       or null if the post has never been modified
	 */
	public Post(int inPostID, String inTitle, String inBody, String inCategory,
			String inAuthor, Timestamp inCreatedDate, Timestamp inModifiedDate) {
		// REQ-03, REQ-04: Reconstruct a post from the database for display
		postID = inPostID;
		title = inTitle;
		body = inBody;
		category = inCategory;
		author = inAuthor;
		createdDate = inCreatedDate;
		modifiedDate = inModifiedDate;
	}

	// ── Update Methods (CRUD — Update) ────────────────────────────────────────

	/*******
	 * <p> Method: updateTitle() </p>
	 *
	 * <p> Description: Updates the title of this post in memory and records
	 * the modification timestamp. The change is persisted to the database
	 * by calling Database.updatePostTitle() after this method. </p>
	 *
	 * <p> Satisfies REQ-05: "As a student, I can edit my post title."
	 * Only the author of the post should be permitted to call this —
	 * ownership enforcement is handled by the GUI layer (REQ-10). </p>
	 *
	 * @param inTitle the new title for the post — must not be null or blank
	 */
	public void updateTitle(String inTitle) {
		// REQ-05: Update the post title and record the modification time
		title = inTitle;
		modifiedDate = new Timestamp(System.currentTimeMillis()); // track when edited
	}

	/*******
	 * <p> Method: updateBody() </p>
	 *
	 * <p> Description: Updates the body text of this post in memory and
	 * records the modification timestamp. The change is persisted to the
	 * database by calling Database.updatePostBody() after this method. </p>
	 *
	 * <p> Satisfies REQ-05: "As a student, I can edit my post body."
	 * Only the author of the post should be permitted to call this —
	 * ownership enforcement is handled by the GUI layer (REQ-10). </p>
	 *
	 * @param inBody the new body text for the post — must not be null or blank
	 */
	public void updateBody(String inBody) {
		// REQ-05: Update the post body and record the modification time
		body = inBody;
		modifiedDate = new Timestamp(System.currentTimeMillis()); // track when edited
	}

	/*******
	 * <p> Method: updateCategory() </p>
	 *
	 * <p> Description: Updates the category/thread of this post in memory and
	 * records the modification timestamp. The change is persisted to the
	 * database by calling Database.updatePostCategory() after this method. </p>
	 *
	 * <p> Satisfies REQ-05 and REQ-12: students can change the thread their
	 * post belongs to. Note: per the user stories, students do NOT have
	 * authority to create or delete threads — only staff can do that (TP3). </p>
	 *
	 * @param inCategory the new category for the post
	 */
	public void updateCategory(String inCategory) {
		// REQ-05, REQ-12: Update the post category and record the modification time
		// Note: thread creation/deletion is a staff-only function (TP3 epic)
		category = inCategory;
		modifiedDate = new Timestamp(System.currentTimeMillis()); // track when edited
	}

	// ── Getters (CRUD — Read) ─────────────────────────────────────────────────

	/*******
	 * <p> Method: getPostID() </p>
	 *
	 * <p> Description: Returns the unique database identifier for this post.
	 * Returns -1 if the post has not yet been inserted into the database. </p>
	 *
	 * @return the post ID, or -1 if not yet persisted
	 */
	public int getPostID() { return postID; } // REQ-04: used to retrieve specific post

	/*******
	 * <p> Method: getTitle() </p>
	 *
	 * <p> Description: Returns the title of this post.
	 * Displayed in the PostNavBar post list and PostDisplayPanel. </p>
	 *
	 * @return the post title
	 */
	public String getTitle() { return title; } // REQ-03, REQ-04: shown in post list and detail view

	/*******
	 * <p> Method: getBody() </p>
	 *
	 * <p> Description: Returns the full body text of this post.
	 * Displayed in PostDisplayPanel when the student selects a post. </p>
	 *
	 * @return the post body text
	 */
	public String getBody() { return body; } // REQ-04: shown in post detail view

	/*******
	 * <p> Method: getAuthor() </p>
	 *
	 * <p> Description: Returns the username of the student who created this post.
	 * Used to display authorship and to enforce post ownership rules. </p>
	 *
	 * @return the post author username
	 */
	public String getAuthor() { return author; } // REQ-10: used for ownership enforcement

	/*******
	 * <p> Method: getCategory() </p>
	 *
	 * <p> Description: Returns the discussion thread category of this post.
	 * Used by PostNavBar.matchesCategory() to filter posts by thread. </p>
	 *
	 * @return the post category
	 */
	public String getCategory() { return category; } // REQ-12, REQ-13: used for thread filtering

	/*******
	 * <p> Method: getCreatedDate() </p>
	 *
	 * <p> Description: Returns the timestamp when this post was created.
	 * Displayed in PostDisplayPanel. Used for TP3 staff participation review. </p>
	 *
	 * @return the post creation timestamp
	 */
	public Timestamp getCreatedDate() { return createdDate; }

	/*******
	 * <p> Method: getModifiedDate() </p>
	 *
	 * <p> Description: Returns the timestamp when this post was last modified,
	 * or null if the post has never been edited. Used for TP3 staff audit trail. </p>
	 *
	 * @return the post modification timestamp, or null if never modified
	 */
	public Timestamp getModifiedDate() { return modifiedDate; }
}