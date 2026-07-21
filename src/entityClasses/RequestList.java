package entityClasses;

import java.util.ArrayList;
import database.Database;

/*******
 * <p> Title: RequestList Class </p>
 *
 * <p> Description: A model class that manages a collection of Request objects
 * loaded from the H2 database. Acts as an in-memory cache of the current
 * requests table, providing operations to refresh and retrieve requests
 * for use by the staff and admin GUI components. </p>
 *
 * <p> RequestList is used by staffRequestList to display all open and closed
 * admin action requests visible to both staff and admins. </p>
 *
 * <p> This class follows the Model pattern from the Foundations MVC structure,
 * sitting between the View (staffRequestList, staffRequestCreationPanel) and
 * the database layer (Database.java). </p>
 *
 * @author Rob Taylor (Team 3) — Implemented RequestList for TP3 admin request system
 * @author Kyle Kim (Team 3) — Added Javadoc
 *
 * @version 1.00  2026-07-21  Initial implementation for TP3
 */
public class RequestList {

	/** In-memory list of Request objects loaded from the database.
	 *  Populated on construction and refreshed via refreshList(). */
	private ArrayList<Request> requestList;

	/** Reference to the application database.
	 *  Shared via FoundationsMain.database. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/*******
	 * <p> Method: RequestList() — Constructor </p>
	 *
	 * <p> Description: Creates a RequestList by loading all current requests
	 * from the database. Called by staffRequestList when it needs to display
	 * the full list of admin action requests. </p>
	 */
	public RequestList() {
		// Load all requests from the database on construction
		requestList = theDatabase.getAllRequests();
	}

	/*******
	 * <p> Method: refreshList() </p>
	 *
	 * <p> Description: Reloads the request list from the database, replacing
	 * the current in-memory list with the latest data. Called when a request
	 * is created, closed, or reopened to keep the display current. </p>
	 */
	public void refreshList() {
		// Reload from DB to reflect latest request state after status changes
		requestList = theDatabase.getAllRequests();
	}

	/*******
	 * <p> Method: getRequest() </p>
	 *
	 * <p> Description: Searches the current in-memory request list for a request
	 * with the given requestID. Returns the matching Request object, or null if
	 * no request with that ID exists. </p>
	 *
	 * @param requestID the unique ID of the request to find
	 * @return the matching Request object, or null if not found
	 */
	public Request getRequest(int requestID) {
		Request rRequest = null;
		boolean found = false;
		int i = 0;

		while (!found && i != requestList.size()) {
			if (requestList.get(i).getRequestID() == requestID) {
				rRequest = requestList.get(i);
				found = true;
			}
			i++;
		}

		return rRequest; // null if not found
	}

	/*******
	 * <p> Method: getRequestList() </p>
	 *
	 * <p> Description: Returns the full in-memory list of Request objects.
	 * Used by staffRequestList to render all admin action requests. </p>
	 *
	 * @return the ArrayList of Request objects currently loaded from the database
	 */
	public ArrayList<Request> getRequestList() {
		return requestList;
	}

	/*******
	 * <p> Method: getOpenRequests() </p>
	 *
	 * <p> Description: Returns only the open and reopened requests from the
	 * in-memory list. Used by the admin interface to show which requests
	 * need attention. </p>
	 *
	 * @return an ArrayList of Request objects with status "OPEN" or "REOPENED"
	 */
	public ArrayList<Request> getOpenRequests() {
		ArrayList<Request> openList = new ArrayList<>();
		for (Request r : requestList) {
			if (r.isOpen()) openList.add(r);
		}
		return openList;
	}
}