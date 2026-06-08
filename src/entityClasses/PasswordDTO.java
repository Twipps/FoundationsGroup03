package entityClasses;

public class PasswordDTO {
    
    //verifyPassword flags 
	private static boolean foundUpperCase = false;
	private static boolean foundLowerCase = false;
	private static boolean foundNumericDigit = false;
	private static boolean foundSpecialChar = false;
	private static boolean foundLongEnough = false;
	private static boolean containsData = false;
	private static boolean containsInvalidCharacter = false;
	private static int indexOfError = -1;
		
	public boolean getFoundUpperCase() { return foundUpperCase; }
	public boolean getFoundLowerCase() { return foundLowerCase; }
	public boolean getFoundNumericDigit() { return foundNumericDigit; }
	public boolean getFoundSpecialChar() { return foundSpecialChar; }
	public boolean getFoundLongEnough() { return foundLongEnough; }
	public boolean getContainsData() { return containsData; }
	public boolean getContainsInvalidCharacter() { return containsInvalidCharacter; }
	public int getIndexOfError() { return indexOfError; }
	
	public void setFoundUpperCase(boolean val) { foundUpperCase = val; }
	public void setFoundLowerCase(boolean val) { foundLowerCase = val; }
	public void setFoundNumericDigit(boolean val) { foundNumericDigit = val; }
	public void setFoundSpecialChar(boolean val) { foundSpecialChar = val; }
	public void setFoundLongEnough(boolean val) { foundLongEnough = val; }
	public void setContainsData(boolean val) { containsData = val; }
	public void setContainsInvalidCharacter(boolean val) { containsInvalidCharacter = val; }
	public void setIndexOfError(int val) { indexOfError = val; }
	
}
