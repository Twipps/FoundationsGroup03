package guiFirstAdmin;

import entityClasses.PasswordDTO;
import utilities.InputValidator;

/*******
 * <p> Title: ModelFirstAdmin Class. </p>
 * 
 * <p> Description: The First System Startup Page Model.  This class is not used as there is no
 * data manipulated by this MVC beyond accepting a username and password and then saving it in the
 * database.  When the code is enhanced for input validation, this model may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-15 Initial version
 *  
 */

public class ModelFirstAdmin {

	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordIndexofError = 0;			// Initialize the IndexofError

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state.  This
		// local variable is a working copy of the input.
		passwordInput = input;				// Save a copy of the input
		
		PasswordDTO result = InputValidator.verifyPassword(input);
		passwordIndexofError = result.getIndexOfError();
		
		if (passwordIndexofError != -1) {
			return "***Error*** " + input.charAt(passwordIndexofError) + " is an invalid character";
		}
		
		// Construct a String with a list of the requirement elements that were found.
		String errMessage = "";
		if (!result.getFoundUpperCase())
			errMessage += "Upper case; ";
		
		if (!result.getFoundLowerCase())
			errMessage += "Lower case; ";
		
		if (!result.getFoundNumericDigit())
			errMessage += "Numeric digits; ";
			
		if (!result.getFoundSpecialChar())
			errMessage += "Special character; ";
			
		if (!result.getFoundLongEnough())
			errMessage += "Long Enough; ";
		
		if (errMessage == "")
			return "";
		
		// If it gets here, there something was not found, so return an appropriate message
		passwordIndexofError = input.length();
		return errMessage + "conditions were not satisfied";
	}
}
