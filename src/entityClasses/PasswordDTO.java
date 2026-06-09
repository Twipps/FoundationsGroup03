package entityClasses;

// @ Author implemented and designed by (Rob Taylor group 03)

public class PasswordDTO {
    
    //verifyPassword flags 
	private boolean foundUpperCase = false;
	private boolean foundLowerCase = false;
	private boolean foundNumericDigit = false;
	private boolean foundSpecialChar = false;
	private boolean foundLongEnough = false;
	private boolean containsData = false;
	private boolean containsInvalidCharacter = false;
	private int indexOfError = -1;
		
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
