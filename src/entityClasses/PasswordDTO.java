package entityClasses;

/**
 * <p>Title: PasswordDTO Class</p>
 *
 * <p>Description: Stores the results of password validation checks. This class
 * contains the validation flags and error index used by the password verification
 * system.</p>
 *
 * @author Rob Taylor (Team 03)
 */

public class PasswordDTO {
    
	/**
	 * Creates an empty PasswordDTO object with all validation flags initialized
	 * to their default values.
	 */
	public PasswordDTO() {
	}
	
    //verifyPassword flags 
	private boolean foundUpperCase = false;
	private boolean foundLowerCase = false;
	private boolean foundNumericDigit = false;
	private boolean foundSpecialChar = false;
	private boolean foundLongEnough = false;
	private boolean containsData = false;
	private boolean containsInvalidCharacter = false;
	private int indexOfError = -1;
		
	/**
	 * Gets whether an uppercase letter was found.
	 *
	 * @return true if an uppercase letter was found; otherwise false
	 */
	public boolean getFoundUpperCase() { return foundUpperCase; }
	/**
	 * Gets whether a lowercase letter was found.
	 *
	 * @return true if a lowercase letter was found; otherwise false
	 */
	public boolean getFoundLowerCase() { return foundLowerCase; }
	/**
	 * Gets whether a numeric digit was found.
	 *
	 * @return true if a numeric digit was found; otherwise false
	 */
	public boolean getFoundNumericDigit() { return foundNumericDigit; }
	 /**
	  * Gets whether a special character was found.
	  *
	  * @return true if a special character was found; otherwise false
	  */
	public boolean getFoundSpecialChar() { return foundSpecialChar; }
	/**
	 * Gets whether the password met the minimum length requirement.
	 *
	 * @return true if the password is long enough; otherwise false
	 */
	public boolean getFoundLongEnough() { return foundLongEnough; }
	/**
	 * Gets whether the password contains data.
	 *
	 * @return true if the password contains data; otherwise false
	 */
	public boolean getContainsData() { return containsData; }
	/**
	 * Gets whether an invalid character was found.
	 *
	 * @return true if an invalid character was found; otherwise false
	 */
	public boolean getContainsInvalidCharacter() { return containsInvalidCharacter; }
	/**
	 * Gets the index of the validation error.
	 *
	 * @return the index of the error, or -1 if no error exists
	 */
	public int getIndexOfError() { return indexOfError; }
	
	/**
	 * Sets whether an uppercase letter was found.
	 *
	 * @param val the new uppercase flag
	 */
	public void setFoundUpperCase(boolean val) { foundUpperCase = val; }
	/**
	 * Sets whether a lowercase letter was found.
	 *
	 * @param val the new lowercase flag
	 */
	public void setFoundLowerCase(boolean val) { foundLowerCase = val; }
	/**
	 * Sets whether a numeric digit was found.
	 *
	 * @param val the new numeric digit flag
	 */
	public void setFoundNumericDigit(boolean val) { foundNumericDigit = val; }
	/**
	 * Sets whether a special character was found.
	 *
	 * @param val the new special character flag
	 */
	public void setFoundSpecialChar(boolean val) { foundSpecialChar = val; }
	/**
	 * Sets whether the password meets the minimum length requirement.
	 *
	 * @param val the new length validation flag
	 */
	public void setFoundLongEnough(boolean val) { foundLongEnough = val; }
	/**
	 * Sets whether the password contains data.
	 *
	 * @param val the new contains data flag
	 */
	public void setContainsData(boolean val) { containsData = val; }
	/**
	 * Sets whether the password contains invalid characters.
	 *
	 * @param val the new invalid character flag
	 */
	public void setContainsInvalidCharacter(boolean val) { containsInvalidCharacter = val; }
	/**
	 * Sets the index of the validation error.
	 *
	 * @param val the index of the validation error
	 */
	public void setIndexOfError(int val) { indexOfError = val; }
	
}
