package entityClasses;

import java.sql.Timestamp;

/*******
 * <p> Title: Thread Class </p>
 *
 * <p> Description: This class stores all the information associated with a
 * discussion thread in the Student Discussion System. Threads are the
 * top-level organizational structure for posts — each post belongs to exactly
 * one thread. Staff members can create, read, update, and delete threads.
 * The "General" thread is the system default and cannot be deleted or renamed. </p>
 *
 * <p> This class is placed in the entityClasses package alongside Post, Reply,
 * Request, User, and PasswordDTO, following the established Foundations
 * codebase pattern for entity data objects. </p>
 *
 * <p> CRUD Implementation: </p>
 * <p> - CREATE: A new Thread is created via Database.createThread(). The General
 *   thread is seeded automatically during Database.createTables(). Staff members
 *   create additional threads via staffThreadCreationPanel. Satisfies STAFF-REQ-01. </p>
 * <p> - READ: Threads are retrieved via Database.getAllThreads() and
 *   Database.getThreadByID(). Used by staffThreadNavBar to display the thread
 *   list and by PostNavBar to populate the category dropdown. Satisfies STAFF-REQ-02. </p>
 * <p> - UPDATE: Thread title is updated via Database.updateThreadTitle().
 *   The General thread cannot be renamed. Satisfies STAFF-REQ-03. </p>
 * <p> - DELETE: Threads are soft-deleted via Database.deleteThread(), which sets
 *   isDeleted=true. All posts in a deleted thread are migrated to General.
 *   The General thread cannot be deleted. Satisfies STAFF-REQ-04. </p>
 *
 * <p> Test Coverage: ThreadTestingAutomation.java (TC-T01 through TC-T26) </p>
 *
 * @author Kyle Kim (Team 3) — Implemented Thread entity class for TP3
 * @author James Suchovic (Team 3) — Defined Thread schema and GUI integration
 *
 * @version 1.00  2026-07-21  Initial implementation for TP3
 */
public class Thread {

	/** Unique database identifier. Set to -1 before insertion. */
	private int threadID;

	/** Display name shown in staffThreadNavBar and PostNavBar.
	 *  Must be unique. "General" is reserved and cannot be changed. */
	private String title;

	/** Optional description of the thread's purpose. May be null. */
	private String body;

	/** Username of the staff member who created this thread. */
	private String author;

	/** Optional sub-category label. May be null. */
	private String category;

	/** Timestamp when this thread was created. */
	private Timestamp createdDate;

	/** Timestamp when this thread was last edited, or null if never modified. */
	private Timestamp modifiedDate;

	/** Soft-delete flag. When true, thread is hidden but posts are preserved. */
	private boolean isDeleted;

	/*******
	 * <p> Method: Thread() — New Thread Constructor </p>
	 *
	 * <p> Description: Creates a new Thread object before it is inserted into
	 * the database. Sets threadID to -1, isDeleted to false, and records
	 * the current time as the creation timestamp. Called by staffThreadCreationPanel.
	 * Satisfies STAFF-REQ-01. </p>
	 *
	 * @param title    the display name — must not be null or blank
	 * @param body     optional description (may be null)
	 * @param author   the staff member creating the thread
	 * @param category optional sub-category label (may be null)
	 */
	public Thread(String title, String body, String author, String category) {
		this.threadID = -1; // not yet assigned a DB ID
		this.title = title;
		this.body = body;
		this.author = author;
		this.category = category;
		this.createdDate = new Timestamp(System.currentTimeMillis());
		this.modifiedDate = null;
		this.isDeleted = false; // new threads are never soft-deleted
	}

	/*******
	 * <p> Method: Thread() — Database Reconstruction Constructor </p>
	 *
	 * <p> Description: Creates a Thread object from an existing database record.
	 * Used by Database.getAllThreads() and Database.getThreadByID() to reconstruct
	 * Thread objects from the H2 database ResultSet. Satisfies STAFF-REQ-02. </p>
	 *
	 * @param threadID     the unique database identifier
	 * @param title        the display name
	 * @param body         optional description (may be null)
	 * @param author       the staff member who created the thread
	 * @param category     optional sub-category (may be null)
	 * @param createdDate  when the thread was created
	 * @param modifiedDate when last modified, or null
	 * @param isDeleted    true if soft-deleted
	 */
	public Thread(int threadID, String title, String body, String author,
			String category, Timestamp createdDate, Timestamp modifiedDate,
			boolean isDeleted) {
		this.threadID = threadID;
		this.title = title;
		this.body = body;
		this.author = author;
		this.category = category;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.isDeleted = isDeleted;
	}

	/*******
	 * <p> Method: updateTitle() </p>
	 *
	 * <p> Description: Updates the title in memory and records modification time.
	 * Persisted via Database.updateThreadTitle(). The General thread cannot be
	 * renamed — enforced at the database layer. Satisfies STAFF-REQ-03. </p>
	 *
	 * @param newTitle the new display name — must not be null or blank
	 */
	public void updateTitle(String newTitle) {
		// STAFF-REQ-03: General thread protection enforced at DB layer
		this.title = newTitle;
		this.modifiedDate = new Timestamp(System.currentTimeMillis());
	}

	/*******
	 * <p> Method: softDelete() </p>
	 *
	 * <p> Description: Marks this thread as soft-deleted in memory.
	 * Persisted via Database.deleteThread(). General cannot be soft-deleted.
	 * Satisfies STAFF-REQ-04. </p>
	 */
	public void softDelete() {
		// STAFF-REQ-04: soft delete preserves posts, just hides the thread
		this.isDeleted = true;
		this.modifiedDate = new Timestamp(System.currentTimeMillis());
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	/** @return the unique DB ID, or -1 if not yet persisted */
	public int getThreadID() { return threadID; }

	/** @return the display name of this thread */
	public String getTitle() { return title; }

	/** @return the optional description, or null */
	public String getBody() { return body; }

	/** @return the staff member who created this thread */
	public String getAuthor() { return author; }

	/** @return the optional sub-category label, or null */
	public String getCategory() { return category; }

	/** @return the creation timestamp */
	public Timestamp getCreatedDate() { return createdDate; }

	/** @return the last modification timestamp, or null */
	public Timestamp getModifiedDate() { return modifiedDate; }

	/** @return true if this thread has been soft-deleted */
	public boolean isDeleted() { return isDeleted; }

	/*******
	 * <p> Method: isGeneral() </p>
	 *
	 * <p> Description: Returns true if this is the General thread.
	 * Used by GUI to disable Edit and Delete controls for the General thread.
	 * The General thread is permanently protected from modification or deletion. </p>
	 *
	 * @return true if this thread's title is "General"
	 */
	public boolean isGeneral() {
		// General is the system default and must always exist
		return "General".equals(title);
	}

	/*******
	 * <p> Method: toString() </p>
	 *
	 * <p> Description: Returns the thread title as the string representation.
	 * Used by JavaFX ComboBox to display thread names directly. </p>
	 *
	 * @return the thread title
	 */
	@Override
	public String toString() { return title; }
}