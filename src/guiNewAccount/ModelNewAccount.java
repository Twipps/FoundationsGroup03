package guiNewAccount;

import entityClasses.PasswordDTO;
import utilities.InputValidator;

/*******
 * <p> Title: ModelNewAccount Class. </p>
 * 
 * <p> Description: The NewAccount Page Model. This class evaluates password strength
 * using the InputValidator FSM and returns a human-readable error message listing
 * any unmet requirements. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Rob Taylor
 * @author Kyle Kim (Team 3) - Improved error messages and fixed String comparison bug
 * 
 * @version 1.00    2025-08-15 Initial version
 * @version 1.01    2026-06-08 Added password validation logic (Rob Taylor)
 * @version 1.02    2026-06-08 Improved error messages; fixed == vs .equals() bug (Kyle Kim)
 */
public class ModelNewAccount {

	public static String passwordInput = "";
	public static int passwordIndexofError = -1;
	
	/**********
	 * <p> Method: evaluatePassword(String input) </p>
	 * 
	 * <p> Description: Evaluates a password string against the required strength
	 * criteria using the InputValidator FSM. Returns an empty string if the password
	 * meets all requirements, or a descriptive error message listing what is missing.
	 * 
	 * Requirements: at least one uppercase letter, one lowercase letter, one digit,
	 * one special character, and a minimum length of 8 characters. </p>
	 * 
	 * @param input the password string to evaluate
	 * @return empty string if valid, or error message describing missing requirements
	 */
	public static String evaluatePassword(String input) {
		passwordIndexofError = 0;
		passwordInput = input;
		
		PasswordDTO result = InputValidator.verifyPassword(input);
		
		if (result == null) {
			return "Password must contain:\n  • At least one uppercase letter (A-Z)"
					+ "\n  • At least one lowercase letter (a-z)"
					+ "\n  • At least one number (0-9)"
					+ "\n  • At least one special character (!@#$%...)"
					+ "\n  • At least 8 characters long";
		}
		
		passwordIndexofError = result.getIndexOfError();
		
		if (passwordIndexofError != -1) {
			return "Invalid character found: '" + input.charAt(passwordIndexofError) + "'";
		}
		
		// Build a descriptive message listing any unmet requirements
		String errMessage = "";
		
		if (!result.getFoundUpperCase())
			errMessage += "\n  • At least one uppercase letter (A-Z)";
		
		if (!result.getFoundLowerCase())
			errMessage += "\n  • At least one lowercase letter (a-z)";
		
		if (!result.getFoundNumericDigit())
			errMessage += "\n  • At least one number (0-9)";
			
		if (!result.getFoundSpecialChar())
			errMessage += "\n  • At least one special character (!@#$%...)";
			
		if (!result.getFoundLongEnough())
			errMessage += "\n  • At least 8 characters long";
		
		// Fixed: use .equals() not == for String comparison
		if (errMessage.equals(""))
			return "";
		
		passwordIndexofError = input.length();
		return "Password must contain:" + errMessage;
	}
}