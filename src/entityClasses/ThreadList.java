package entityClasses;

import java.util.ArrayList;
import database.Database;

/*******
 * <p> Title: ThreadList Class </p>
 *
 * <p> Description: A model class that manages a collection of Thread objects
 * loaded from the H2 database. Acts as an in-memory cache of the current
 * threads table, providing operations to refresh, retrieve, and delete threads
 * for use by the staff discussion GUI components. </p>
 *
 * <p> ThreadList is used by staffThreadNavBar and staffThreadCreationPanel to
 * access thread data without making repeated direct database calls. </p>
 *
 * <p> This class follows the Model pattern from the Foundations MVC structure,
 * sitting between the View (staffThreadNavBar, staffThreadDisplayPanel) and
 * the database layer (Database.java). </p>
 *
 * <p> Test Coverage: ThreadTestingAutomation.java (TC-T06, TC-T07, TC-T18) </p>
 *
 * @author Kyle Kim (Team 3) — Implemented ThreadList for TP3 Staff Thread CRUD
 *
 * @version 1.00  2026-07-21  Initial implementation for TP3
 */
public class ThreadList {

	/** In-memory list of Thread objects loaded from the database.
	 *  Populated on construction and refreshed via refreshList(). */
	private ArrayList<Thread> threadList;

	/** Reference to the application database.
	 *  Shared via FoundationsMain.database. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/*******
	 * <p> Method: ThreadList() — Constructor </p>
	 *
	 * <p> Description: Creates a ThreadList by loading all current non-deleted
	 * threads from the database. Called by staffThreadNavBar and PostNavBar
	 * when they need the current thread collection. </p>
	 */
	public ThreadList() {
		// Load all active threads from the database on construction
		threadList = theDatabase.getAllThreads();
	}

	/*******
	 * <p> Method: refreshList() </p>
	 *
	 * <p> Description: Reloads the thread list from the database, replacing
	 * the current in-memory list with the latest data. Called when a thread
	 * is created, renamed, or deleted to keep the display current. </p>
	 */
	public void refreshList() {
		// Reload from DB to reflect latest thread state after CRUD operations
		threadList = theDatabase.getAllThreads();
	}

	/*******
	 * <p> Method: getThread() </p>
	 *
	 * <p> Description: Searches the current in-memory thread list for a thread
	 * with the given threadID. Returns the matching Thread object, or null if
	 * no thread with that ID exists. </p>
	 *
	 * @param threadID the unique ID of the thread to find
	 * @return the matching Thread object, or null if not found
	 */
	public Thread getThread(int threadID) {
		Thread rThread = null;
		boolean found = false;
		int i = 0;
		while (!found && i != threadList.size()) {
			if (threadList.get(i).getThreadID() == threadID) {
				rThread = threadList.get(i);
				found = true;
			}
			i++;
		}
		return rThread; // null if not found
	}

	/*******
	 * <p> Method: getThreadList() </p>
	 *
	 * <p> Description: Returns the full in-memory list of Thread objects.
	 * Used by staffThreadNavBar to render the thread navigation list. </p>
	 *
	 * @return the ArrayList of Thread objects currently loaded from the database
	 */
	public ArrayList<Thread> getThreadList() {
		return threadList;
	}

	/*******
	 * <p> Method: deleteThread() </p>
	 *
	 * <p> Description: Soft-deletes the thread with the given threadID by
	 * delegating to Database.deleteThread(). The General thread cannot be
	 * deleted — enforced at the database layer. </p>
	 *
	 * @param threadID the unique ID of the thread to soft-delete
	 */
	public void deleteThread(int threadID) {
		// General thread protection enforced at Database layer
		theDatabase.deleteThread(threadID);
	}
}