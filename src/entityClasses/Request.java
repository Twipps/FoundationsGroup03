package entityClasses;

import java.sql.Timestamp;

/*******
 * <p> Title: Request Class </p>
 *
 * <p> Description: This class stores all the information associated with a
 * staff request for admin action in the Student Discussion System. Staff members
 * can submit requests to admins for actions that require admin-level permissions.
 * Admins can document actions taken and close requests. Staff can reopen closed
 * requests and update descriptions. </p>
 *
 * <p> This class is placed in the entityClasses package alongside Post, Thread,
 * Reply, User, and PasswordDTO, following the established Foundations codebase
 * pattern for entity data objects. </p>
 *
 * <p> CRUD Implementation: </p>
 * <p> - CREATE: A new Request is created via Database.createRequest(). Staff
 *   members submit requests via staffRequestCreationPanel. Satisfies STAFF-REQ-07. </p>
 * <p> - READ: Requests are retrieved via Database.getAllRequests() and
 *   Database.getRequestByID(). Displayed in staffRequestList for both staff
 *   and admins. Satisfies STAFF-REQ-08. </p>
 * <p> - UPDATE: Request status and body are updated via Database.updateRequestStatus()
 *   and Database.updateRequestBody(). Admins close requests; staff can reopen them.
 *   Satisfies STAFF-REQ-09. </p>
 * <p> - DELETE: Requests are not deleted — they are closed (status="CLOSED") to
 *   preserve the audit trail as required by the user story. </p>
 *
 * <p> Request Status Values: </p>
 * <p> - "OPEN": Request submitted, awaiting admin action. </p>
 * <p> - "CLOSED": Admin has documented action taken and closed the request. </p>
 * <p> - "REOPENED": Staff has reopened a previously closed request. </p>
 *
 * <p> Test Coverage: RequestTestingAutomation.java </p>
 *
 * @author Rob Taylor (Team 3) — Designed and implemented Request entity class
 * and admin request system backend
 * @author Kyle Kim (Team 3) — Added Javadoc and User Story mappings
 *
 * @version 1.00  2026-07-21  Initial implementation for TP3
 */
public class Request {

	/** Unique database identifier. Set to -1 before insertion. */
	private int requestID;

	/** Short title summarizing what admin action is being requested.
	 *  Must not be null or blank. */
	private String title;

	/** The username of the staff member who submitted this request. */
	private String author;

	/** The type of admin action being requested (e.g. "ROLE_CHANGE", "USER_DELETE").
	 *  Used for filtering in the admin request list. */
	private String requestType;

	/** Current status of this request: "OPEN", "CLOSED", or "REOPENED".
	 *  New requests always start as "OPEN". */
	private String status;

	/** Timestamp when this request was first submitted. */
	private Timestamp timeCreated;

	/** Timestamp when this request was last updated (status change or body edit). */
	private Timestamp lastUpdated;

	/** Full description of the request and any admin notes added when closing.
	 *  Staff can update this when reopening a request. */
	private String body;

	/*******
	 * <p> Method: Request() — New Request Constructor </p>
	 *
	 * <p> Description: Creates a new Request object before it is inserted into
	 * the database. Sets requestID to -1, status to "OPEN", and records the
	 * current time as the creation timestamp. Called by staffRequestCreationPanel.
	 * Satisfies STAFF-REQ-07. </p>
	 *
	 * @param title       short title of the request — must not be null or blank
	 * @param author      the staff member submitting the request
	 * @param requestType the type of admin action being requested
	 * @param body        full description of what is being requested
	 */
	public Request(String title, String author, String requestType, String body) {
		this.requestID = -1; // not yet assigned a DB ID
		this.title = title;
		this.author = author;
		this.requestType = requestType;
		this.status = "OPEN"; // all new requests start as OPEN
		this.timeCreated = new Timestamp(System.currentTimeMillis());
		this.lastUpdated = new Timestamp(System.currentTimeMillis());
		this.body = body;
	}

	/*******
	 * <p> Method: Request() — Database Reconstruction Constructor </p>
	 *
	 * <p> Description: Creates a Request object from an existing database record.
	 * Used by Database.getAllRequests() and Database.getRequestByID() to
	 * reconstruct Request objects from the H2 database ResultSet.
	 * Satisfies STAFF-REQ-08. </p>
	 *
	 * @param requestID   the unique database identifier
	 * @param title       the request title
	 * @param author      the staff member who submitted the request
	 * @param requestType the type of action requested
	 * @param status      current status: "OPEN", "CLOSED", or "REOPENED"
	 * @param timeCreated when the request was submitted
	 * @param lastUpdated when the request was last updated
	 * @param body        full description and admin notes
	 */
	public Request(int requestID, String title, String author, String requestType,
			String status, Timestamp timeCreated, Timestamp lastUpdated, String body) {
		this.requestID = requestID;
		this.title = title;
		this.author = author;
		this.requestType = requestType;
		this.status = status;
		this.timeCreated = timeCreated;
		this.lastUpdated = lastUpdated;
		this.body = body;
	}

	/*******
	 * <p> Method: closeRequest() </p>
	 *
	 * <p> Description: Sets the status to "CLOSED" and updates the lastUpdated
	 * timestamp. Called when an admin documents action taken on a request.
	 * Persisted via Database.updateRequestStatus(). Satisfies STAFF-REQ-09. </p>
	 */
	public void closeRequest() {
		// STAFF-REQ-09: Admin closes request after taking action
		this.status = "CLOSED";
		this.lastUpdated = new Timestamp(System.currentTimeMillis());
	}

	/*******
	 * <p> Method: reopenRequest() </p>
	 *
	 * <p> Description: Sets the status to "REOPENED" and updates the lastUpdated
	 * timestamp. Called when a staff member reopens a previously closed request.
	 * Persisted via Database.updateRequestStatus(). Satisfies STAFF-REQ-09. </p>
	 */
	public void reopenRequest() {
		// STAFF-REQ-09: Staff can reopen a closed request
		this.status = "REOPENED";
		this.lastUpdated = new Timestamp(System.currentTimeMillis());
	}

	/*******
	 * <p> Method: updateBody() </p>
	 *
	 * <p> Description: Updates the body description of this request in memory
	 * and records the update timestamp. Persisted via Database.updateRequestBody().
	 * Used when staff update the description when reopening a request. </p>
	 *
	 * @param newBody the updated description — must not be null or blank
	 */
	public void updateBody(String newBody) {
		this.body = newBody;
		this.lastUpdated = new Timestamp(System.currentTimeMillis());
	}

	// ── Getters ───────────────────────────────────────────────────────────────

	/** @return the unique DB ID, or -1 if not yet persisted */
	public int getRequestID() { return requestID; }

	/** @return the request title */
	public String getTitle() { return title; }

	/** @return the staff member who submitted this request */
	public String getAuthor() { return author; }

	/** @return the type of admin action requested */
	public String getRequestType() { return requestType; }

	/** @return the current status: "OPEN", "CLOSED", or "REOPENED" */
	public String getStatus() { return status; }

	/** @return the timestamp when this request was submitted */
	public Timestamp getTimeCreated() { return timeCreated; }

	/** @return the timestamp when this request was last updated */
	public Timestamp getLastUpdated() { return lastUpdated; }

	/** @return the full description and admin notes */
	public String getBody() { return body; }

	/*******
	 * <p> Method: isOpen() </p>
	 *
	 * <p> Description: Returns true if this request is currently open or reopened.
	 * Used by the GUI to determine which requests need admin attention. </p>
	 *
	 * @return true if status is "OPEN" or "REOPENED"
	 */
	public boolean isOpen() {
		// Both OPEN and REOPENED requests need admin attention
		return "OPEN".equals(status) || "REOPENED".equals(status);
	}
}