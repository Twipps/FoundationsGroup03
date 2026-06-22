package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.User;
import postComponents.Post;
import postComponents.Reply;

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
				+ "title VARCHAR(255), "
				+ "body TEXT, "
				+ "author VARCHAR(255), "
				+ "category VARCHAR(255), "
				+ "createdDate TIMESTAMP DEFAULT NULL, "
				+ "modifiedDate TIMESTAMP DEFAULT NULL)";
		statement.execute(postTable);
		
		String replyTable = "CREATE TABLE IF NOT EXISTS replies ("
				+ "replyID INT AUTO_INCREMENT PRIMARY KEY, "
				+ "parentPostID INT, "
				+ "body TEXT, "   
				+ "author VARCHAR(255), " 
				+ "createdDate TIMESTAMP DEFAULT NULL, " 
				+ "modifiedDate TIMESTAMP DEFAULT NULL)";
		statement.execute(replyTable);
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
 *  starting with "<Select User>" at the start of the list. </p>
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
	
	// Adding for new ui functionalities
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
	
	public ResultSet getInvitationCodes() { // admin home relies on
		String query = "SELECT code, role, emailAddress, expiryDate FROM InvitationCodes";
		try {
			return statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// POST operations
	public ArrayList<Post> getAllPosts() {
	    ArrayList<Post> posts = new ArrayList<>();

	    String query = "SELECT * FROM Posts ORDER BY postID";

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
	                result.getTimestamp("modifiedDate")
	            );

	            posts.add(post);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return posts;
	}

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
	                result.getTimestamp("modifiedDate")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	public void addPost(String title, String body, String author, String category) {
	    String query = "INSERT INTO Posts (title, body, author, category, createdDate, modifiedDate) " +
	                   "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, title);
	        pstmt.setString(2, body);
	        pstmt.setString(3, author);
	        pstmt.setString(4, category);

	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void deletePost(int postID) {
	    String query = "DELETE FROM Posts WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

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

	public void updatePostModifiedDate(int postID) {
	    String query = "UPDATE Posts SET modifiedDate = CURRENT_TIMESTAMP WHERE postID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	// REPLY operations
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

	public void deleteReply(int replyID) {
	    String query = "DELETE FROM Replies WHERE replyID = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

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