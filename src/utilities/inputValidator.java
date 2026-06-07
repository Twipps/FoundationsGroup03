package utilities;

public final class inputValidator {
	
	private inputValidator() {		//There is no need to make an instance of this class
		
	}
	
	public static String verifyPassword(String password) {
		return passwordPopUpWindow.Model.evaluatePassword(password);
	}
	
	public static String verifyUserName(String username) {
		return userNameRecognizerTestbed.UserNameRecognizer.checkForValidUserName(username);
	}

}
