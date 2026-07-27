package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.EvaluationParameter;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.Request;
import entityClasses.Thread;
import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MB)" on the l3ft side
 * of the page under the heading "Reference" for a PDF of 438 pages.)  This class leverages H2
 * and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Kyle Kim (Team 3) - Added deleteUser, setOneTimePassword, getOneTimePassword,
 *                             clearOneTimePassword methods and oneTimePassword column
 * @author James Suchovic (Team 3) - Added getAllUsers(), getInvitationCodes(), reply and post
 * 									 operations.
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by Pravalika 
   @version 2.01
 * 						Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.02		2025-12-17 Minor updates for Spring 2026
 * @version 2.03 	    2026-06-06 Added deleteUser and one-time password methods (Kyle Kim, Team 3)
 * @version 2.04        2026-08-06 Added getAllUsers() and getInvitationCodes()  (James Suchovic Team 3)
 * @version 2.04        2026-09-07 ExpiryDate to getInvitationsCodes()  (James Suchovic Team 3)
 * @version 2.05		2026-06-17 Added posts and replys table with relevant functions
 * @version 2.06		2026-07-21 Added Thread CRUD, Request CRUD, soft delete, reply count,
 *                               read/unread tracking, and staff statistics (Kyle Kim, Team 3)
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentStudentRole;
	private boolean currentInstructorRole;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newRole1 BOOL DEFAULT FALSE, "
				+ "newRole2 BOOL DEFAULT FALSE, "
				+ "oneTimePassword VARCHAR(255) DEFAULT NULL)";
		statement.execute(userTable);
		
		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
		        + "code VARCHAR(10) PRIMARY KEY, "
		        + "emailAddress VARCHAR(255), "
		        + "role VARCHAR(10), "
		        + "expiryDate TIMESTAMP DEFAULT NULL)";
		statement.execute(invitationCodesTable);
		
		String postTable = "CREATE TABLE IF NOT EXISTS posts ("
		        + "postID INT AUTO_INCREMENT PRIMARY KEY, "
		        + "threadID INT NOT NULL, "
		        + "title VARCHAR(255), "
		        + "body TEXT, "
		        + "author VARCHAR(255), "
		        + "category VARCHAR(255), "
		        + "staffFeedback TEXT, "
		        + "createdDate TIMESTAMP DEFAULT NULL, "
		        + "modifiedDate TIMESTAMP DEFAULT NULL, "
		        + "isDeleted BOOLEAN DEFAULT FALSE)";
		statement.execute(postTable);
		
		String evaluationParameterTable =
				"CREATE TABLE IF NOT EXISTS EvaluationParameters ("
					+ "parameterID INT AUTO_INCREMENT PRIMARY KEY, "
					+ "staffUsername VARCHAR(255), "
					+ "name VARCHAR(255), "
					+ "metric VARCHAR(255), "
					+ "comparisonOperator VARCHAR(255), "
					+ "threshold INT, "
					+ "description TEXT, "
					+ "threadID INT DEFAULT NULL, "
					+ "isActive BOOLEAN DEFAULT TRUE)";
			statement.execute(evaluationParameterTable);

		// Defensive migration: if this database was created before threadID/
		// staffFeedback existed on posts (DROP ALL OBJECTS is commented out
		// above so old local databases persist across schema changes),
		// CREATE TABLE IF NOT EXISTS is a no-op and the columns would be
		// missing, crashing addPost()/getAllPosts() with a "column not found"
		// error. These ALTER statements patch any pre-existing posts table
		// so every teammate's local database self-heals on next run instead
		// of requiring everyone to manually delete their local DB file.
		try {
			statement.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS threadID INT");
		} catch (SQLException e) {
			System.err.println("Migration note (threadID): " + e.getMessage());
		}
		try {
			statement.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS staffFeedback TEXT");
		} catch (SQLException e) {
			System.err.println("Migration note (staffFeedback): " + e.getMessage());
		}
		
		String replyTable = "CREATE TABLE IF NOT EXISTS replies ("
				+ "replyID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "parentPostID INT, "
				+ "body TEXT, "   
				+ "author VARCHAR(255), " 
				+ "createdDate TIMESTAMP DEFAULT NULL, " 
				+ "modifiedDate TIMESTAMP DEFAULT NULL)";
		statement.execute(replyTable);

		// ── Thread table (TP3 — Kyle Kim) ────────────────────────────────────
		String threadTable = "CREATE TABLE IF NOT EXISTS threads ("
				+ "threadID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "title VARCHAR(255) UNIQUE NOT NULL, "
				+ "body TEXT, "
				+ "author VARCHAR(255), "
				+ "category VARCHAR(255), "
				+ "createdDate TIMESTAMP DEFAULT NULL, "
				+ "modifiedDate TIMESTAMP DEFAULT NULL, "
				+ "isDeleted BOOLEAN DEFAULT FALSE)";
		statement.execute(threadTable);

		// Seed the General thread — must always exist, cannot be deleted or renamed
		String seedGeneral = "MERGE INTO threads (title, body, author, category, createdDate, isDeleted) "
				+ "KEY(title) VALUES ('General', 'Default thread for all posts', 'system', "
				+ "'General', CURRENT_TIMESTAMP, FALSE)";
		statement.execute(seedGeneral);

		// ── Admin request table (TP3 — Rob Taylor) ───────────────────────────
		String requestTable = "CREATE TABLE IF NOT EXISTS requests ("
				+ "requestID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "title VARCHAR(255) NOT NULL, "
				+ "author VARCHAR(255), "
				+ "requestType VARCHAR(100), "
				+ "status VARCHAR(50) DEFAULT 'OPEN', "
				+ "timeCreated TIMESTAMP DEFAULT NULL, "
				+ "lastUpdated TIMESTAMP DEFAULT NULL, "
				+ "body TEXT)";
		statement.execute(requestTable);

		// ── Read tracking table (TP3 — Kyle Kim) ─────────────────────────────
		String readTrackingTable = "CREATE TABLE IF NOT EXISTS postReadStatus ("
				+ "readStatusID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "username VARCHAR(255), "
				+ "postID INT, "
				+ "readAt TIMESTAMP DEFAULT NULL, "
				+ "UNIQUE(username, postID))";
		statement.execute(readTrackingTable);

		// ── Reply read tracking table (TP3 — Kyle Kim) ───────────────────────
		// Separate from postReadStatus because the requirement tracks read/unread
		// at the REPLY level, not just the post level — a student needs to see
		// which specific replies to their posts they have and have not read,
		// not just whether they've opened the post at all.
		String replyReadTrackingTable = "CREATE TABLE IF NOT EXISTS replyReadStatus ("
				+ "readStatusID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "username VARCHAR(255), "
				+ "replyID INT, "
				+ "readAt TIMESTAMP DEFAULT NULL, "
				+ "UNIQUE(username, replyID))";
		statement.execute(replyReadTrackingTable);
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newRole1, newRole2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentStudentRole = user.getNewStudent();
			pstmt.setBoolean(9, currentStudentRole);
			
			currentInstructorRole = user.getNewInstructor();
			pstmt.setBoolean(10, currentInstructorRole);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with "Select User" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginRole1(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as a Student else false.
 * 
 */
	public boolean loginRole1(User user) {
		// Validates a student user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole1 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
			       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginRole2(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Instructor role.
	 * 
	 * @return true if the specified user has been logged in as an Instructor else false.
	 * 
	 */
	public boolean loginRole2(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole2 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
			       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getNewStudent()) numberOfRoles++;
		if (user.getNewInstructor()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a role, this method establishes an invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specifies the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6);
	    
	    // Set expiry to 7 days from now
	    java.sql.Timestamp expiryDate = new java.sql.Timestamp(
	        System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));
	    
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role, expiryDate) "
	        + "VALUES (?, ?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.setTimestamp(4, expiryDate);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been used to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	return rs.getInt("count") > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode(String code) </p>
	 * 
	 * <p> Description: Get the email address associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	public String getEmailAddressUsingCode (String code) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		} catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	/*******
	 * <p> Method: void Invitations() </p>
	 * 
	 * <p> Description: Removes all invitation codes whose expiry date has passed.
	 * This method should be called each time the Admin Home page loads to ensure
	 * expired invitations are cleaned up automatically. </p>
	 * 
	 */
	public void removeExpiredInvitations() {
	    String query = "DELETE FROM InvitationCodes WHERE expiryDate < ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: boolean isInvitationExpired(String code) </p>
	 * 
	 * <p> Description: Checks whether a specific invitation code has expired.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 * 
	 * @return true if the invitation has expired or does not exist, false if still valid
	 * 
	 */
	public boolean isInvitationExpired(String code) {
	    String query = "SELECT expiryDate FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            java.sql.Timestamp expiry = rs.getTimestamp("expiryDate");
	            if (expiry == null) return false;
	            return expiry.before(new java.sql.Timestamp(System.currentTimeMillis()));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return true; // If not found, treat as expired
	}


	/*******
	 * <p> Method: java.sql.Timestamp getInvitationExpiry(String code) </p>
	 * 
	 * <p> Description: Returns the expiry date of an invitation code so it can
	 * be displayed to the admin when managing invitations.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 * 
	 * @return the expiry Timestamp, or null if not found
	 * 
	 */
	public java.sql.Timestamp getInvitationExpiry(String code) {
	    String query = "SELECT expiryDate FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getTimestamp("expiryDate");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/*******
	 * <p> Method: void deleteUser(String username) </p>
	 * 
	 * <p> Description: Removes a user account from the database permanently.
	 * This method is called by the admin after confirming the deletion via the
	 * "Are you sure?" dialog in the controller. An admin cannot delete their own
	 * account — that check is performed in the controller before calling this method.</p>
	 * 
	 * @param username is the username of the user to be deleted
	 * 
	 */
	public void deleteUser(String username) {
	    String query = "DELETE FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: void setOneTimePassword(String username, String tempPassword) </p>
	 * 
	 * <p> Description: Sets a one-time password for a user who has forgotten their password.
	 * The admin generates this password and communicates it to the user out of band.
	 * Once the user logs in with the one-time password, it must be cleared so it cannot
	 * be used again. </p>
	 * 
	 * @param username is the username of the user
	 * @param tempPassword is the generated one-time password to store
	 * 
	 */
	public void setOneTimePassword(String username, String tempPassword) {
	    String query = "UPDATE userDB SET oneTimePassword = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, tempPassword);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: String getOneTimePassword(String username) </p>
	 * 
	 * <p> Description: Retrieves the one-time password for a user. Used during login to
	 * check if the user is logging in with a one-time password so the system can prompt
	 * them to set a new password and then clear the one-time password. </p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the one-time password string, or null if none is set
	 * 
	 */
	public String getOneTimePassword(String username) {
	    String query = "SELECT oneTimePassword FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("oneTimePassword");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	/*******
	 * <p> Method: void clearOneTimePassword(String username) </p>
	 * 
	 * <p> Description: Clears the one-time password after the user has successfully logged in
	 * with it, so it cannot be used again. This is called after the user has logged in
	 * with the one-time password and established a new password. </p>
	 * 
	 * @param username is the username of the user
	 * 
	 */
	public void clearOneTimePassword(String username) {
	    String query = "UPDATE userDB SET oneTimePassword = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("firstName");
	        }
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * @param firstName is the new first name for the user
	 *  
	 */
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("middleName");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 * @param middleName is the new middle name for the user
	 *  
	 */
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("lastName");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the last name of a user given that user's username and the new
	 * 		last name.</p>
	 * 
	 * @param username is the username of the user
	 * @param lastName is the new last name for the user
	 *  
	 */
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("preferredFirstName");
	        }
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 * @param emailAddress is the new email address for the user
	 *  
	 */
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	/*******
	 * <p> Method: void updatePassword(String username, String password) </p>
	 * 
	 * <p> Description: Update the password of a user given that user's username and
	 * 		the new password.</p>
	 * 
	 * @param username is the username of the user
	 * @param password is the new password for the user
	 *  
	 */
	public void updatePassword(String username, String password) {
	    String query = "UPDATE userDB SET password = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPassword = password;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true if the get is successful, else false
	 *  
	 */
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentStudentRole = rs.getBoolean(10);
	    	currentInstructorRole = rs.getBoolean(11);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 * @param role is string that specifies the role to update
	 * @param value is the string that specifies TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	public boolean updateUserRole(String username, String role, String value) {
		if (role.compareTo("Admin") == 0) {
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}

		if (role.compareTo("Role1") == 0 || role.compareTo("Student") == 0) {
			String query = "UPDATE userDB SET newRole1 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentStudentRole = true;
				else
					currentStudentRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}

		if (role.compareTo("Role2") == 0 || role.compareTo("Instructor") == 0 
				|| role.compareTo("Staff") == 0) { // instuctor and staff are same for now
			String query = "UPDATE userDB SET newRole2 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentInstructorRole = true;
				else
					currentInstructorRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * <p> Description: Get the current user's username.</p>
	 * @return the username value is returned
	 */
	public String getCurrentUsername() { return currentUsername;};

	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * <p> Description: Get the current user's password.</p>
	 * @return the password value is returned
	 */
	public String getCurrentPassword() { return currentPassword;};

	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * <p> Description: Get the current user's first name.</p>
	 * @return the first name value is returned
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * <p> Description: Get the current user's middle name.</p>
	 * @return the middle name value is returned
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * <p> Description: Get the current user's last name.</p>
	 * @return the last name value is returned
	 */
	public String getCurrentLastName() { return currentLastName;};

	/*******
	 * <p> Method: String getCurrentPreferredFirstName() </p>
	 * <p> Description: Get the current user's preferred first name.</p>
	 * @return the preferred first name value is returned
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * <p> Description: Get the current user's email address.</p>
	 * @return the email address value is returned
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * @return true if this user plays an Admin role, else false
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	/*******
	 * <p> Method: boolean getCurrentNewRole1() </p>
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * @return true if this user plays a Student role, else false
	 */
	public boolean getCurrentNewRole1() { return currentStudentRole;};

	/*******
	 * <p> Method: boolean getCurrentNewRole2() </p>
	 * <p> Description: Get the current user's Instructor/Staff role attribute.</p>
	 * @return true if this user plays an Instructor or Staff role, else false
	 */
	public boolean getCurrentNewRole2() { return currentInstructorRole;};	
	
	/**
	 * Gets all users stored in the database.
	 *
	 * @return a list containing all user accounts
	 */
	public List<User> getAllUsers() { // admin home relies on this
	    List<User> users = new ArrayList<>();

	    String query = "SELECT * FROM userDB";

	    try (PreparedStatement statement =
	            connection.prepareStatement(query)) {

	        ResultSet result = statement.executeQuery();

	        while (result.next()) {
	            User user = new User(
	                result.getString("userName"),
	                result.getString("password"),
	                result.getString("firstName"),
	                result.getString("middleName"),
	                result.getString("lastName"),
	                result.getString("preferredFirstName"),
	                result.getString("emailAddress"),
	                result.getBoolean("adminRole"),
	                result.getBoolean("newRole1"),
	                result.getBoolean("newRole2") // REMEMBER TO ADD NEW ROLE AND STUFF
	            );

	            users.add(user);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return users;
	}
	
	/**
	 * Gets all active invitation codes from the database.
	 *
	 * @return a ResultSet containing invitation code, role, email address, and expiry date data, or null if the query fails
	 */
	public ResultSet getInvitationCodes() { // admin home relies on
		String query = "SELECT code, role, emailAddress, expiryDate FROM InvitationCodes";
		try {
			return statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets all posts stored in the database.
	 *
	 * @return an ArrayList containing all posts
	 */
	public ArrayList<Post> getAllPosts() {
	    ArrayList<Post> posts = new ArrayList<>();

	    String query = "SELECT * FROM Posts WHERE isDeleted = FALSE ORDER BY postID";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet result = pstmt.executeQuery();

	        while (result.next()) {
	            Post post = new Post(
	                result.getInt("postID"),
	                result.getString("title"),
	                result.getString("body"),
	                result.getString("category"),
	                result.getString("author"),
	                result.getTimestamp("createdDate"),
	                result.getTimestamp("modifiedDate"),
	                result.getString("staffFeedback")
	            );

	            posts.add(post);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return posts;
	}

	/**
	 * Gets a single post by its unique post ID.
	 *
	 * @param postID the unique ID of the post to retrieve
	 * @return the matching Post object, or null if no matching post exists
	 */
	public Post getPost(int postID) {
	    String query = "SELECT * FROM Posts WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);

	        ResultSet result = pstmt.executeQuery();

	        if (result.next()) {
	            return new Post(
	                result.getInt("postID"),
	                result.getString("title"),
	                result.getString("body"),
	                result.getString("category"),
	                result.getString("author"),
	                result.getTimestamp("createdDate"),
	                result.getTimestamp("modifiedDate"),
	                result.getString("staffFeedback")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	/**
	 * Adds a new post to the database.
	 *
	 * @param title the title of the post
	 * @param body the body text of the post
	 * @param author the author who created the post
	 * @param category the category assigned to the post
	 * @param threadID the thread the post belongs to
	 */
	public void addPost(String title, String body, String author,
	                    String category, int threadID) {

	    String query = "INSERT INTO Posts (threadID, title, body, author, category, createdDate, modifiedDate) " +
	                   "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, threadID);
	        pstmt.setString(2, title);
	        pstmt.setString(3, body);
	        pstmt.setString(4, author);
	        pstmt.setString(5, category);

	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Deletes a post from the database.
	 *
	 * @param postID the unique ID of the post to delete
	 */
	public void deletePost(int postID) {
	    String query = "DELETE FROM Posts WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the title of a post.
	 *
	 * @param postID the unique ID of the post to update
	 * @param newTitle the new title for the post
	 */
	public void updatePostTitle(int postID, String newTitle) {
	    String query = "UPDATE Posts SET title = ?, modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newTitle);
	        pstmt.setInt(2, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Updates the thread associated with a post.
	 *
	 * @param postID the ID of the post to update
	 * @param threadID the ID of the new thread
	 */
	public void updatePostThreadID(int postID, int threadID) {
	    String query = "UPDATE Posts SET threadID = ?, modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, threadID);
	        pstmt.setInt(2, postID);

	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the body text of a post.
	 *
	 * @param postID the unique ID of the post to update
	 * @param newBody the new body text for the post
	 */
	public void updatePostBody(int postID, String newBody) {
	    String query = "UPDATE Posts SET body = ?, modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newBody);
	        pstmt.setInt(2, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the category of a post.
	 *
	 * @param postID the unique ID of the post to update
	 * @param newCategory the new category for the post
	 */
	public void updatePostCategory(int postID, String newCategory) {
	    String query = "UPDATE Posts SET category = ?, modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newCategory);
	        pstmt.setInt(2, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Updates the private staff feedback of a post.
	 *
	 * @param postID the unique ID of the post to update
	 * @param newStaffFeedback the new staff feedback for the post
	 */
	public void updateStaffFeedback(int postID, String newStaffFeedback) {
	    String query = "UPDATE Posts SET staffFeedback = ? WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newStaffFeedback);
	        pstmt.setInt(2, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the modified date of a post to the current timestamp.
	 *
	 * @param postID the unique ID of the post to update
	 */
	public void updatePostModifiedDate(int postID) {
	    String query = "UPDATE Posts SET modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/**
	 * Gets all replies stored in the database.
	 *
	 * @return an ArrayList containing all replies
	 */
	public ArrayList<Reply> getAllReplies() {
	    ArrayList<Reply> replies = new ArrayList<>();

	    String query = "SELECT * FROM Replies ORDER BY replyID";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet result = pstmt.executeQuery();

	        while (result.next()) {
	            Reply reply = new Reply(
	                result.getInt("replyID"),
	                result.getInt("parentPostID"),
	                result.getString("body"),
	                result.getString("author"),
	                result.getTimestamp("createdDate"),
	                result.getTimestamp("modifiedDate")
	            );

	            replies.add(reply);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return replies;
	}

	/**
	 * Gets all replies associated with a specific post.
	 *
	 * @param postID the unique ID of the parent post
	 * @return an ArrayList containing replies for the specified post
	 */
	public ArrayList<Reply> getRepliesForPost(int postID) {
	    ArrayList<Reply> replies = new ArrayList<>();

	    String query = "SELECT * FROM Replies WHERE parentPostID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);

	        ResultSet result = pstmt.executeQuery();

	        while (result.next()) {
	            Reply reply = new Reply(
	                result.getInt("replyID"),
	                result.getInt("parentPostID"),
	                result.getString("body"),
	                result.getString("author"),
	                result.getTimestamp("createdDate"),
	                result.getTimestamp("modifiedDate")
	            );

	            replies.add(reply);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return replies;
	}

	/**
	 * Gets a single reply by its unique reply ID.
	 *
	 * @param replyID the unique ID of the reply to retrieve
	 * @return the matching Reply object, or null if no matching reply exists
	 */
	public Reply getReply(int replyID) {
	    String query = "SELECT * FROM Replies WHERE replyID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyID);

	        ResultSet result = pstmt.executeQuery();

	        if (result.next()) {
	            return new Reply(
	                result.getInt("replyID"),
	                result.getInt("parentPostID"),
	                result.getString("body"),
	                result.getString("author"),
	                result.getTimestamp("createdDate"),
	                result.getTimestamp("modifiedDate")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}
	
	/**
	 * Adds a new reply to the database.
	 *
	 * @param parentPostID the unique ID of the post being replied to
	 * @param body the body text of the reply
	 * @param author the author who created the reply
	 */
	public void addReply(int parentPostID, String body, String author) {
	    String query = "INSERT INTO Replies (parentPostID, body, author, createdDate, modifiedDate) " +
	                   "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, parentPostID);
	        pstmt.setString(2, body);
	        pstmt.setString(3, author);

	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Deletes a reply from the database.
	 *
	 * @param replyID the unique ID of the reply to delete
	 */
	public void deleteReply(int replyID) {
	    String query = "DELETE FROM Replies WHERE replyID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the body text of a reply.
	 *
	 * @param replyID the unique ID of the reply to update
	 * @param newBody the new body text for the reply
	 */
	public void updateReplyBody(int replyID, String newBody) {
		String query =
			    "UPDATE Replies " +
			    "SET body = ?, modifiedDate = CURRENT_TIMESTAMP " +
			    "WHERE replyID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newBody);
	        pstmt.setInt(2, replyID);

	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Updates the modified date of a reply to the current timestamp.
	 *
	 * @param replyID the unique ID of the reply to update
	 */
	public void updateReplyModifiedDate(int replyID) {
		String query =
			    "UPDATE Replies " +
			    "SET modifiedDate = CURRENT_TIMESTAMP " +
			    "WHERE replyID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	

	// =========================================================================
	// THREAD CRUD METHODS — TP3 (Kyle Kim)
	// =========================================================================

	/*******
	 * <p> Method: createThread() </p>
	 * <p> Description: Creates a new discussion thread. Validates title is not
	 * null or blank. Returns false if duplicate or validation fails.
	 * Satisfies STAFF-REQ-01. </p>
	 * @param title    the display name — must not be null or blank
	 * @param body     optional description (may be null)
	 * @param author   the staff member creating the thread
	 * @param category optional sub-category (may be null)
	 * @return true if created successfully, false otherwise
	 */
	public boolean createThread(String title, String body, String author, String category) {
		if (title == null || title.isBlank()) {
			System.err.println("createThread: thread title must not be null or blank.");
			return false;
		}
		String query = "INSERT INTO threads (title, body, author, category, createdDate, isDeleted) "
				+ "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, FALSE)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, title);
			pstmt.setString(2, body);
			pstmt.setString(3, author);
			pstmt.setString(4, category);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.err.println("createThread failed: " + e.getMessage());
			return false;
		}
	}

	/*******
	 * <p> Method: getAllThreads() </p>
	 * <p> Description: Returns all non-deleted Thread objects ordered by creation
	 * date. Always includes General. Used by staffThreadNavBar and PostNavBar.
	 * Satisfies STAFF-REQ-02. </p>
	 * @return ArrayList of active Thread objects
	 */
	public ArrayList<Thread> getAllThreads() {
		ArrayList<Thread> threads = new ArrayList<>();
		String query = "SELECT * FROM threads WHERE isDeleted = FALSE ORDER BY createdDate ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				threads.add(new Thread(
					rs.getInt("threadID"),
					rs.getString("title"),
					rs.getString("body"),
					rs.getString("author"),
					rs.getString("category"),
					rs.getTimestamp("createdDate"),
					rs.getTimestamp("modifiedDate"),
					rs.getBoolean("isDeleted")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return threads;
	}

	/*******
	 * <p> Method: getThreadByID() </p>
	 * <p> Description: Retrieves a single Thread by threadID. Returns null if
	 * not found or soft-deleted. Satisfies STAFF-REQ-02. </p>
	 * @param threadID the unique ID of the thread
	 * @return matching Thread object, or null
	 */
	public Thread getThreadByID(int threadID) {
		String query = "SELECT * FROM threads WHERE threadID = ? AND isDeleted = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, threadID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new Thread(
					rs.getInt("threadID"),
					rs.getString("title"),
					rs.getString("body"),
					rs.getString("author"),
					rs.getString("category"),
					rs.getTimestamp("createdDate"),
					rs.getTimestamp("modifiedDate"),
					rs.getBoolean("isDeleted")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*******
	 * <p> Method: getThreadByTitle() </p>
	 * <p> Description: Retrieves a Thread by title. Used for duplicate checking
	 * and General thread lookup. Returns null if not found. </p>
	 * @param title the title to search for
	 * @return matching Thread object, or null
	 */
	public Thread getThreadByTitle(String title) {
		if (title == null || title.isBlank()) return null;
		String query = "SELECT * FROM threads WHERE title = ? AND isDeleted = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, title);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new Thread(
					rs.getInt("threadID"),
					rs.getString("title"),
					rs.getString("body"),
					rs.getString("author"),
					rs.getString("category"),
					rs.getTimestamp("createdDate"),
					rs.getTimestamp("modifiedDate"),
					rs.getBoolean("isDeleted")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*******
	 * <p> Method: updateThreadTitle() </p>
	 * <p> Description: Renames a thread. General cannot be renamed. New title
	 * must not be null/blank or duplicate. Also updates all posts whose category
	 * matched the old title. Satisfies STAFF-REQ-03. </p>
	 * @param threadID the ID of the thread to rename
	 * @param newTitle the new display name
	 * @return true if rename succeeded, false otherwise
	 */
	public boolean updateThreadTitle(int threadID, String newTitle) {
		Thread existing = getThreadByID(threadID);
		if (existing == null) {
			System.err.println("updateThreadTitle: thread not found.");
			return false;
		}
		if ("General".equals(existing.getTitle())) {
			System.err.println("updateThreadTitle: General thread cannot be renamed.");
			return false;
		}
		if (newTitle == null || newTitle.isBlank()) {
			System.err.println("updateThreadTitle: new title must not be null or blank.");
			return false;
		}
		if (getThreadByTitle(newTitle) != null) {
			System.err.println("updateThreadTitle: a thread named '" + newTitle + "' already exists.");
			return false;
		}
		try {
			String oldTitle = existing.getTitle();
			String updatePosts = "UPDATE posts SET category = ? WHERE category = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(updatePosts)) {
				pstmt.setString(1, newTitle);
				pstmt.setString(2, oldTitle);
				pstmt.executeUpdate();
			}
			String updateThread = "UPDATE threads SET title = ?, modifiedDate = CURRENT_TIMESTAMP WHERE threadID = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(updateThread)) {
				pstmt.setString(1, newTitle);
				pstmt.setInt(2, threadID);
				int rows = pstmt.executeUpdate();
				return rows > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*******
	 * <p> Method: deleteThread() </p>
	 *
	 * <p> Description: Soft-deletes a discussion thread. Before the thread is
	 * deleted, every post associated with it is reassigned to the General
	 * thread. The General thread itself cannot be deleted. </p>
	 *
	 * @param threadID the ID of the thread to delete
	 * @return true if the thread was deleted, or false if deletion failed
	 */
	public boolean deleteThread(int threadID) {

		Thread existing = getThreadByID(threadID);

		if (existing == null) {
			return false;
		}

		if (existing.isGeneral()) {
			System.err.println(
				"deleteThread: General thread cannot be deleted."
			);
			return false;
		}

		Thread generalThread = getThreadByTitle("General");

		if (generalThread == null) {
			System.err.println(
				"deleteThread: General thread could not be found."
			);
			return false;
		}

		try {

			/*
			 * Reassign every post in the deleted thread to the
			 * General thread before deleting it.
			 */
			String migratePosts =
				"UPDATE Posts " +
				"SET threadID = ?, modifiedDate = CURRENT_TIMESTAMP " +
				"WHERE threadID = ?";

			try (PreparedStatement pstmt =
					connection.prepareStatement(migratePosts)) {

				pstmt.setInt(1, generalThread.getThreadID());
				pstmt.setInt(2, threadID);
				pstmt.executeUpdate();
			}

			/*
			 * Soft-delete the selected thread.
			 */
			String softDelete =
				"UPDATE Threads " +
				"SET isDeleted = TRUE, " +
				"modifiedDate = CURRENT_TIMESTAMP " +
				"WHERE threadID = ?";

			try (PreparedStatement pstmt =
					connection.prepareStatement(softDelete)) {

				pstmt.setInt(1, threadID);
				pstmt.executeUpdate();
			}

			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// =========================================================================
	// SOFT DELETE FOR POSTS — TP3 (Kyle Kim)
	// =========================================================================

	/*******
	 * <p> Method: softDeletePost() </p>
	 * <p> Description: Marks a post as soft-deleted (isDeleted=TRUE). Replies
	 * are preserved. PostDisplayPanel shows "This post has been deleted". </p>
	 * @param postID the unique ID of the post to soft-delete
	 */
	public void softDeletePost(int postID) {
	    System.out.println("softDeletePost called for postID: " + postID);
	    String query = "UPDATE posts SET isDeleted = TRUE, "
	            + "modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);
	        int rows = pstmt.executeUpdate();
	        System.out.println("Rows updated: " + rows);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// =========================================================================
	// REPLY COUNT — TP3 (Kyle Kim)
	// =========================================================================

	/*******
	 * <p> Method: getReplyCountForPost() </p>
	 * <p> Description: Returns number of replies for a post. Used by PostNavBar
	 * to display reply count on each post row. </p>
	 * @param postID the unique ID of the post
	 * @return the number of replies, or 0 if none
	 */
	public int getReplyCountForPost(int postID) {
		String query = "SELECT COUNT(*) AS replyCount FROM replies WHERE parentPostID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, postID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("replyCount");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// =========================================================================
	// READ/UNREAD TRACKING — TP3 (Kyle Kim)
	// =========================================================================

	/*******
	 * <p> Method: markPostAsRead() </p>
	 * <p> Description: Records that a user has read a post. Safe to call
	 * multiple times — MERGE prevents duplicates. </p>
	 * @param username the student marking the post as read
	 * @param postID   the post being marked as read
	 */
	public void markPostAsRead(String username, int postID) {
		String query = "MERGE INTO postReadStatus (username, postID, readAt) KEY(username, postID) VALUES (?, ?, CURRENT_TIMESTAMP)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setInt(2, postID);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*******
	 * <p> Method: hasUserReadPost() </p>
	 * <p> Description: Returns true if user has read the specified post. </p>
	 * @param username the username to check
	 * @param postID   the post ID to check
	 * @return true if the user has read this post
	 */
	public boolean hasUserReadPost(String username, int postID) {
		String query = "SELECT COUNT(*) AS cnt FROM postReadStatus WHERE username = ? AND postID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setInt(2, postID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: getUnreadPostCount() </p>
	 * <p> Description: Returns number of posts user has not yet read. </p>
	 * @param username the username to check
	 * @return the number of unread posts
	 */
	public int getUnreadPostCount(String username) {
		String query = "SELECT COUNT(*) AS cnt FROM posts p WHERE p.isDeleted = FALSE "
				+ "AND NOT EXISTS (SELECT 1 FROM postReadStatus r WHERE r.username = ? AND r.postID = p.postID)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: markReplyAsRead() </p>
	 * <p> Description: Records that a user has read a specific reply. Used so
	 * a post author can see which replies to their posts they have and have
	 * not yet read. Safe to call multiple times — MERGE prevents duplicates.
	 * Satisfies the Student User Story: "I can see...how many of [my replies]
	 * I have not yet read." </p>
	 * @param username the user marking the reply as read (typically the post author)
	 * @param replyID  the reply being marked as read
	 */
	public void markReplyAsRead(String username, int replyID) {
		String query = "MERGE INTO replyReadStatus (username, replyID, readAt) KEY(username, replyID) VALUES (?, ?, CURRENT_TIMESTAMP)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setInt(2, replyID);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*******
	 * <p> Method: hasUserReadReply() </p>
	 * <p> Description: Returns true if the given user has read the specified
	 * reply. </p>
	 * @param username the username to check
	 * @param replyID  the reply ID to check
	 * @return true if the user has read this reply
	 */
	public boolean hasUserReadReply(String username, int replyID) {
		String query = "SELECT COUNT(*) AS cnt FROM replyReadStatus WHERE username = ? AND replyID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setInt(2, replyID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: getUnreadReplyCountForPost() </p>
	 * <p> Description: Returns the number of replies on a specific post that
	 * the given user has not yet read. Used to show "how many [replies] I
	 * have not yet read" per post in the student's own-posts list. </p>
	 * @param username the post author checking their unread replies
	 * @param postID   the post to check replies for
	 * @return the number of unread replies on this post
	 */
	public int getUnreadReplyCountForPost(String username, int postID) {
		String query = "SELECT COUNT(*) AS cnt FROM replies r WHERE r.parentPostID = ? "
				+ "AND NOT EXISTS (SELECT 1 FROM replyReadStatus rs WHERE rs.username = ? AND rs.replyID = r.replyID)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, postID);
			pstmt.setString(2, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: getRepliesReceivedByUser() </p>
	 * <p> Description: Returns all replies made to any post authored by the
	 * given user — i.e. the replies that user has "received." Optionally
	 * filters to only unread replies. Satisfies the Student User Story:
	 * "I can list all the replies I have received or just those I have not
	 * read." </p>
	 * @param username   the post author whose received replies are being retrieved
	 * @param unreadOnly if true, only replies not yet read by username are returned
	 * @return ArrayList of Reply objects received by this user, newest first
	 */
	public ArrayList<Reply> getRepliesReceivedByUser(String username, boolean unreadOnly) {
		ArrayList<Reply> replies = new ArrayList<>();
		String query = "SELECT r.* FROM replies r "
				+ "JOIN posts p ON r.parentPostID = p.postID "
				+ "WHERE p.author = ? AND p.isDeleted = FALSE"
				+ (unreadOnly
					? " AND NOT EXISTS (SELECT 1 FROM replyReadStatus rs WHERE rs.username = ? AND rs.replyID = r.replyID)"
					: "")
				+ " ORDER BY r.createdDate DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			if (unreadOnly) {
				pstmt.setString(2, username);
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				replies.add(new Reply(
					rs.getInt("replyID"),
					rs.getInt("parentPostID"),
					rs.getString("body"),
					rs.getString("author"),
					rs.getTimestamp("createdDate"),
					rs.getTimestamp("modifiedDate")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return replies;
	}

	// =========================================================================
	// ADMIN REQUEST CRUD — TP3 (Rob Taylor)
	// =========================================================================

	/*******
	 * <p> Method: createRequest() </p>
	 * <p> Description: Creates a new admin action request. New requests always
	 * start with status "OPEN". Satisfies STAFF-REQ-07. </p>
	 * @param title       the request title — must not be null or blank
	 * @param author      the staff member submitting the request
	 * @param requestType the type of admin action requested
	 * @param body        full description of the request
	 * @return true if created successfully, false otherwise
	 */
	public boolean createRequest(String title, String author, String requestType, String body) {
		if (title == null || title.isBlank()) {
			System.err.println("createRequest: title must not be null or blank.");
			return false;
		}
		String query = "INSERT INTO requests (title, author, requestType, status, timeCreated, lastUpdated, body) "
				+ "VALUES (?, ?, ?, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, title);
			pstmt.setString(2, author);
			pstmt.setString(3, requestType);
			pstmt.setString(4, body);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.err.println("createRequest failed: " + e.getMessage());
			return false;
		}
	}

	/*******
	 * <p> Method: getAllRequests() </p>
	 * <p> Description: Returns all Request objects ordered by timeCreated
	 * descending. Satisfies STAFF-REQ-08. </p>
	 * @return ArrayList of all Request objects
	 */
	public ArrayList<Request> getAllRequests() {
		ArrayList<Request> requests = new ArrayList<>();
		String query = "SELECT * FROM requests ORDER BY timeCreated DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				requests.add(new Request(
					rs.getInt("requestID"),
					rs.getString("title"),
					rs.getString("author"),
					rs.getString("requestType"),
					rs.getString("status"),
					rs.getTimestamp("timeCreated"),
					rs.getTimestamp("lastUpdated"),
					rs.getString("body")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requests;
	}

	/*******
	 * <p> Method: getRequestByID() </p>
	 * <p> Description: Retrieves a single Request by requestID. Returns null if not found. </p>
	 * @param requestID the unique ID of the request
	 * @return matching Request object, or null
	 */
	public Request getRequestByID(int requestID) {
		String query = "SELECT * FROM requests WHERE requestID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, requestID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new Request(
					rs.getInt("requestID"),
					rs.getString("title"),
					rs.getString("author"),
					rs.getString("requestType"),
					rs.getString("status"),
					rs.getTimestamp("timeCreated"),
					rs.getTimestamp("lastUpdated"),
					rs.getString("body")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*******
	 * <p> Method: updateRequestStatus() </p>
	 * <p> Description: Updates request status. Valid: "OPEN", "CLOSED", "REOPENED".
	 * Satisfies STAFF-REQ-09. </p>
	 * @param requestID the ID of the request to update
	 * @param newStatus the new status value
	 * @return true if update succeeded
	 */
	public boolean updateRequestStatus(int requestID, String newStatus) {
		String query = "UPDATE requests SET status = ?, lastUpdated = CURRENT_TIMESTAMP WHERE requestID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newStatus);
			pstmt.setInt(2, requestID);
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*******
	 * <p> Method: updateRequestBody() </p>
	 * <p> Description: Updates the body description of a request. </p>
	 * @param requestID the ID of the request to update
	 * @param newBody   the updated description — must not be null or blank
	 * @return true if update succeeded
	 */
	public boolean updateRequestBody(int requestID, String newBody) {
		if (newBody == null || newBody.isBlank()) {
			System.err.println("updateRequestBody: body must not be null or blank.");
			return false;
		}
		String query = "UPDATE requests SET body = ?, lastUpdated = CURRENT_TIMESTAMP WHERE requestID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newBody);
			pstmt.setInt(2, requestID);
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*******
	 * <p> Method: updateRequestTitle() </p>
	 * <p> Description: Updates the title of a request. </p>
	 * @param requestID the ID of the request to update
	 * @param newTitle   the updated title — must not be null or blank
	 * @return true if update succeeded
	 */
	public boolean updateRequestTitle(int requestID, String newTitle) {
		if (newTitle == null || newTitle.isBlank()) {
			System.err.println("updateRequestTitle: title must not be null or blank.");
			return false;
		}
		String query = "UPDATE requests SET title = ?, lastUpdated = CURRENT_TIMESTAMP WHERE requestID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newTitle);
			pstmt.setInt(2, requestID);
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*******
	 * <p> Method: updateRequestType() </p>
	 * <p> Description: Updates the type of a request. </p>
	 * @param requestID the ID of the request to update
	 * @param newType   the updated type — must not be null or blank
	 * @return true if update succeeded
	 */
	public boolean updateRequestType(int requestID, String newType) {
		if (newType == null || newType.isBlank()) {
			System.err.println("updateRequestTitle: title must not be null or blank.");
			return false;
		}
		String query = "UPDATE requests SET requestType = ?, lastUpdated = CURRENT_TIMESTAMP WHERE requestID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newType);
			pstmt.setInt(2, requestID);
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Deletes a request from the database.
	 *
	 * @param requestID the unique ID of the request to delete
	 */
	public void deleteRequest(int requestID) {
	    String query = "DELETE FROM requests WHERE requestID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, requestID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// =========================================================================
	// STAFF STATISTICS — TP3 (Rob Taylor)
	// =========================================================================

	/*******
	 * <p> Method: getPostCountForUser() </p>
	 * <p> Description: Returns total non-deleted posts by a user.
	 * Used by staffUserActivityAuditPanel. Satisfies STAFF-REQ-05. </p>
	 * @param username the student username
	 * @return number of posts by this user
	 */
	public int getPostCountForUser(String username) {
		String query = "SELECT COUNT(*) AS cnt FROM posts WHERE author = ? AND isDeleted = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: getReplyCountForUser() </p>
	 * <p> Description: Returns total replies by a user.
	 * Used by staffUserActivityAuditPanel. Satisfies STAFF-REQ-05. </p>
	 * @param username the student username
	 * @return number of replies by this user
	 */
	public int getReplyCountForUser(String username) {
		String query = "SELECT COUNT(*) AS cnt FROM replies WHERE author = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: getPostsForUser() </p>
	 * <p> Description: Returns all non-deleted posts by a user ordered by
	 * creation date descending. Used by staffUserActivityAuditPanel.
	 * Satisfies STAFF-REQ-05. </p>
	 * @param username the student username
	 * @return ArrayList of Post objects by this user
	 */
	public ArrayList<Post> getPostsForUser(String username) {
		ArrayList<Post> posts = new ArrayList<>();
		String query = "SELECT * FROM posts WHERE author = ? AND isDeleted = FALSE ORDER BY createdDate DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				posts.add(new Post(
					rs.getInt("postID"),
					rs.getString("title"),
					rs.getString("body"),
					rs.getString("category"),
					rs.getString("author"),
					rs.getTimestamp("createdDate"),
					rs.getTimestamp("modifiedDate"),
					rs.getString("staffFeedback")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return posts;
	}

	/*******
	 * <p> Method: getDistinctStudentsRepliedTo() </p>
	 * <p> Description: Returns the number of DISTINCT other students whose posts
	 * a given user has replied to. Used to evaluate the Staff Statistics
	 * engagement requirement ("replied to posts from at least 3 different
	 * students"). Replies to the user's own posts are excluded — replying to
	 * yourself does not count toward engagement with classmates.
	 *
	 * Implementation note: a simple COUNT(*) on replies would overcount a
	 * student who replies many times to the same one or two classmates.
	 * COUNT(DISTINCT p.author) is required so repeated replies to the same
	 * person only count once — this is why a JOIN against posts is needed
	 * rather than just counting rows in the replies table. </p>
	 *
	 * @param username the student whose engagement is being measured
	 * @return the count of distinct other students this user has replied to
	 */
	public int getDistinctStudentsRepliedTo(String username) {
		String query = "SELECT COUNT(DISTINCT p.author) AS cnt "
				+ "FROM replies r "
				+ "JOIN posts p ON r.parentPostID = p.postID "
				+ "WHERE r.author = ? AND p.author <> ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, username); // exclude replies to the user's own posts
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getInt("cnt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: hasMetReplyEngagementRequirement() </p>
	 * <p> Description: Returns true if the user has replied to posts from at
	 * least 3 distinct other students. This is the boolean form of
	 * getDistinctStudentsRepliedTo(), intended for direct display in the
	 * Staff Statistics View (e.g. as a checkmark/badge per student) without
	 * requiring the GUI layer to know the threshold value itself. </p>
	 *
	 * @param username the student whose engagement is being measured
	 * @return true if the user has replied to 3 or more distinct other students
	 */
	public boolean hasMetReplyEngagementRequirement(String username) {
		return getDistinctStudentsRepliedTo(username) >= 3;
	}
	
	/*******
	 * <p> Method: getPostsForThread() </p>
	 *
	 * <p> Description: Returns all non-deleted posts associated with the
	 * supplied thread ID. Posts are matched directly using the threadID
	 * foreign key stored in the Posts table. </p>
	 *
	 * @param threadID the unique ID of the thread
	 * @return an ArrayList containing all active posts in the thread
	 */
	public ArrayList<Post> getPostsForThread(int threadID) {

		ArrayList<Post> posts = new ArrayList<>();

		String query =
			"SELECT * FROM Posts " +
			"WHERE threadID = ? AND isDeleted = FALSE " +
			"ORDER BY createdDate DESC";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			pstmt.setInt(1, threadID);

			ResultSet result = pstmt.executeQuery();

			while (result.next()) {

				Post post = new Post(
					result.getInt("postID"),
					result.getString("title"),
					result.getString("body"),
					result.getString("category"),
					result.getString("author"),
					result.getTimestamp("createdDate"),
					result.getTimestamp("modifiedDate"),
					result.getString("staffFeedback")
				);

				posts.add(post);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return posts;
	}
	
	/*******
	 * <p>Method: getAllStudentUsers()</p>
	 *
	 * <p>Description: Returns all users who currently have the Student role.
	 * A user may have additional roles and will still be included.</p>
	 *
	 * @return a list containing all student users
	 */
	public List<User> getAllStudentUsers() {

		List<User> students = new ArrayList<>();

		String query =
			"SELECT * FROM userDB " +
			"WHERE newRole1 = TRUE " +
			"ORDER BY userName";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			ResultSet result = pstmt.executeQuery();

			while (result.next()) {

				User user = new User(
					result.getString("userName"),
					result.getString("password"),
					result.getString("firstName"),
					result.getString("middleName"),
					result.getString("lastName"),
					result.getString("preferredFirstName"),
					result.getString("emailAddress"),
					result.getBoolean("adminRole"),
					result.getBoolean("newRole1"),
					result.getBoolean("newRole2")
				);

				students.add(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return students;
	}
	
	// Note: getStudentsWithRepliesOnAtLeastThreeDistinctPosts() was removed.
	// It duplicated getDistinctStudentsRepliedTo() / hasMetReplyEngagementRequirement()
	// above with a different (and buggier) definition — it counted distinct POSTS
	// replied to rather than distinct STUDENTS, and did not exclude self-replies,
	// so a student replying multiple times to their own post could incorrectly
	// qualify. The verified, tested version above is used instead.
	
	/*******
	 * <p> Method: getEvaluationParametersForStaff() </p>
	 *
	 * <p> Description: Retrieves all evaluation parameters owned by the specified
	 * staff user and reconstructs them as EvaluationParameter objects. </p>
	 *
	 * @param staffUsername the username of the staff member
	 * @return an ArrayList containing the staff member's evaluation parameters
	 */
	public ArrayList<EvaluationParameter> getEvaluationParametersForStaff(
			String staffUsername) {

		ArrayList<EvaluationParameter> parameterList = new ArrayList<>();

		String query =
			"SELECT parameterID, staffUsername, name, metric, "
			+ "comparisonOperator, threshold, description, threadID, isActive "
			+ "FROM EvaluationParameters "
			+ "WHERE staffUsername = ? "
			+ "ORDER BY parameterID";

		try (PreparedStatement preparedStatement =
				connection.prepareStatement(query)) {

			preparedStatement.setString(1, staffUsername);

			try (ResultSet resultSet =
					preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					/*
					 * getObject() is used because threadID may be NULL.
					 * Calling getInt() alone would return 0 for a NULL value.
					 */
					Integer threadID =
						resultSet.getObject("threadID", Integer.class);

					EvaluationParameter parameter = new EvaluationParameter(
							resultSet.getInt("parameterID"),
							resultSet.getString("staffUsername"),
							resultSet.getString("name"),
							resultSet.getString("metric"),
							resultSet.getString("comparisonOperator"),
							resultSet.getInt("threshold"),
							resultSet.getString("description"),
							threadID,
							resultSet.getBoolean("isActive")
						);

					parameterList.add(parameter);
				}
			}

		} catch (SQLException exception) {
			System.err.println(
				"Error retrieving evaluation parameters for staff user: "
				+ staffUsername
			);

			exception.printStackTrace();
		}

		return parameterList;
	}
	
	/*******
	 * <p> Method: deleteEvaluationParameter() </p>
	 *
	 * <p> Description: Deletes an evaluation parameter from the database.
	 * The staffUsername condition ensures that a staff member can only delete
	 * a parameter that belongs to them. </p>
	 *
	 * @param parameterID the unique ID of the parameter to delete
	 * @param staffUsername the username of the staff member deleting it
	 * @return true if a parameter was deleted, or false if no matching parameter
	 *         was found
	 */
	public boolean deleteEvaluationParameter(
			int parameterID,
			String staffUsername) {

		String query =
			"DELETE FROM EvaluationParameters "
			+ "WHERE parameterID = ? AND staffUsername = ?";

		try (PreparedStatement preparedStatement =
				connection.prepareStatement(query)) {

			preparedStatement.setInt(1, parameterID);
			preparedStatement.setString(2, staffUsername);

			int affectedRows = preparedStatement.executeUpdate();

			return affectedRows > 0;

		} catch (SQLException exception) {
			System.err.println(
				"Error deleting evaluation parameter: "
				+ parameterID
			);

			exception.printStackTrace();
			return false;
		}
	}
	
	/*******
	 * <p> Method: addEvaluationParameter() </p>
	 *
	 * <p> Description: Inserts a new evaluation parameter into the database. </p>
	 *
	 * @param staffUsername the owning staff username
	 * @param name the parameter name
	 * @param metric the metric being evaluated
	 * @param comparisonOperator the comparison operator
	 * @param threshold the required threshold
	 * @param description optional description
	 * @param threadID optional thread scope
	 * @param active whether the parameter is active
	 * @return true if the insert succeeded
	 */
	public boolean addEvaluationParameter(
			String staffUsername,
			String name,
			String metric,
			String comparisonOperator,
			int threshold,
			String description,
			Integer threadID,
			boolean active) {

		String sql =
			"INSERT INTO EvaluationParameters "
			+ "(staffUsername, name, metric, "
			+ "comparisonOperator, threshold, "
			+ "description, threadID, isActive) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement statement =
				connection.prepareStatement(sql)) {

			statement.setString(1, staffUsername);
			statement.setString(2, name);
			statement.setString(3, metric);
			statement.setString(4, comparisonOperator);
			statement.setInt(5, threshold);
			statement.setString(6, description);

			if (threadID == null) {
				statement.setNull(7, java.sql.Types.INTEGER);
			}
			else {
				statement.setInt(7, threadID);
			}

			statement.setBoolean(8, active);

			return statement.executeUpdate() > 0;

		}
		catch (SQLException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	/*******
	 * <p> Method: updateEvaluationParameter() </p>
	 *
	 * <p> Description: Updates an existing evaluation parameter. </p>
	 *
	 * @param parameterID the parameter being updated
	 * @param staffUsername the owning staff username
	 * @param name the parameter name
	 * @param metric the metric being evaluated
	 * @param comparisonOperator the comparison operator
	 * @param threshold the required threshold
	 * @param description optional description
	 * @param threadID optional thread scope
	 * @param active whether the parameter is active
	 * @return true if the update succeeded
	 */
	public boolean updateEvaluationParameter(
			int parameterID,
			String staffUsername,
			String name,
			String metric,
			String comparisonOperator,
			int threshold,
			String description,
			Integer threadID,
			boolean active) {

		String sql =
			"UPDATE EvaluationParameters "
			+ "SET name = ?, "
			+ "metric = ?, "
			+ "comparisonOperator = ?, "
			+ "threshold = ?, "
			+ "description = ?, "
			+ "threadID = ?, "
			+ "isActive = ? "
			+ "WHERE parameterID = ? "
			+ "AND staffUsername = ?";

		try (PreparedStatement statement =
				connection.prepareStatement(sql)) {

			statement.setString(1, name);
			statement.setString(2, metric);
			statement.setString(3, comparisonOperator);
			statement.setInt(4, threshold);
			statement.setString(5, description);

			if (threadID == null) {
				statement.setNull(6, java.sql.Types.INTEGER);
			}
			else {
				statement.setInt(6, threadID);
			}

			statement.setBoolean(7, active);
			statement.setInt(8, parameterID);
			statement.setString(9, staffUsername);

			return statement.executeUpdate() > 0;

		}
		catch (SQLException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns the number of non-deleted posts created by a student.
	 * When threadID is null, posts from every thread are counted.
	 *
	 * @param username the student's username
	 * @param threadID optional thread to restrict the count to
	 * @return number of posts created by the student
	 */
	public int getStudentPostCount(
			String username,
			Integer threadID) {

		if (threadID == null) {
			return getPostCountForUser(username);
		}

		String query =
			"SELECT COUNT(*) AS cnt "
			+ "FROM posts "
			+ "WHERE author = ? "
			+ "AND threadID = ? "
			+ "AND isDeleted = FALSE";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			pstmt.setString(1, username);
			pstmt.setInt(2, threadID);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("cnt");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	/**
	 * Returns the number of replies created by a student.
	 * When threadID is supplied, only replies on posts in that thread
	 * are counted.
	 *
	 * @param username the student's username
	 * @param threadID optional thread to restrict the count to
	 * @return number of replies created by the student
	 */
	public int getStudentReplyCount(
			String username,
			Integer threadID) {

		if (threadID == null) {
			return getReplyCountForUser(username);
		}

		String query =
			"SELECT COUNT(*) AS cnt "
			+ "FROM replies r "
			+ "JOIN posts p "
			+ "ON r.parentPostID = p.postID "
			+ "WHERE r.author = ? "
			+ "AND p.threadID = ? "
			+ "AND p.isDeleted = FALSE";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			pstmt.setString(1, username);
			pstmt.setInt(2, threadID);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("cnt");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	/**
	 * Returns the number of distinct threads in which a student has
	 * participated by either creating a post or writing a reply.
	 *
	 * @param username the student's username
	 * @return number of distinct threads participated in
	 */
	public int getStudentThreadParticipationCount(
			String username) {

		String query =
			"SELECT COUNT(DISTINCT activity.threadID) AS cnt "
			+ "FROM ("
			+ "    SELECT p.threadID "
			+ "    FROM posts p "
			+ "    WHERE p.author = ? "
			+ "    AND p.isDeleted = FALSE "
			+ "    UNION "
			+ "    SELECT p.threadID "
			+ "    FROM replies r "
			+ "    JOIN posts p "
			+ "    ON r.parentPostID = p.postID "
			+ "    WHERE r.author = ? "
			+ "    AND p.isDeleted = FALSE"
			+ ") activity";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			pstmt.setString(1, username);
			pstmt.setString(2, username);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("cnt");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	/**
	 * Returns the number of distinct other students whose posts the
	 * specified student has replied to.
	 *
	 * When threadID is supplied, only interactions in that thread are
	 * counted. Replies to the student's own posts are excluded.
	 *
	 * @param username the student whose engagement is being measured
	 * @param threadID optional thread to restrict the count to
	 * @return number of distinct students engaged with
	 */
	public int getDistinctStudentsEngagedCount(
			String username,
			Integer threadID) {

		if (threadID == null) {
			return getDistinctStudentsRepliedTo(username);
		}

		String query =
			"SELECT COUNT(DISTINCT p.author) AS cnt "
			+ "FROM replies r "
			+ "JOIN posts p "
			+ "ON r.parentPostID = p.postID "
			+ "JOIN userDB u "
			+ "ON p.author = u.userName "
			+ "WHERE r.author = ? "
			+ "AND p.author <> ? "
			+ "AND p.threadID = ? "
			+ "AND p.isDeleted = FALSE "
			+ "AND u.newRole1 = TRUE";

		try (PreparedStatement pstmt =
				connection.prepareStatement(query)) {

			pstmt.setString(1, username);
			pstmt.setString(2, username);
			pstmt.setInt(3, threadID);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("cnt");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database to the console.</p>
	 * 
	 * @throws SQLException if there is an issue accessing the database.
	 * 
	 */
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}