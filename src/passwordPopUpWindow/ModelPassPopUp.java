package passwordPopUpWindow;

import entityClasses.PasswordDTO;
import javafx.scene.paint.Color;
import utilities.InputValidator;

/*******
 * <p> Title: Model Class - establishes the required GUI data and the computations.
 * </p>
 *
 * <p> Description: This Model class is a major component of a Model View Controller (MVC)
 * application design that provides the user with a Graphical User Interface using JavaFX
 * widgets as opposed to a command line interface.
 * 
 * In this case the Model deals with an input from the user and checks to see if it conforms to
 * the requirements specified by a graphical representation of a finite state machine.
 * 
 * This is a purely static component of the MVC implementation.  There is no need to instantiate
 * the class.
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 2.00	2025-07-30 Rewrite of this application for the Fall 2025 offering of CSE 360
 * and other ASU courses.
 */

public class ModelPassPopUp {
		
	/*******
	 * <p> Title: updatePassword - Protected Method </p>
	 * 
	 * <p> Description: This method is called every time the user changes the password (e.g., with 
	 * every key pressed) using the GUI from the PasswordEvaluationGUITestbed.  It resets the 
	 * messages associated with each of the requirements and then evaluates the current password
	 * with respect to those requirements.  The results of that evaluation are display via the View
	 * to the user and via the console.</p>
	 */

	protected static void updatePassword() {
		ViewPassPopUp.resetAssessments();						// Reset the assessment flags to the
		String password = ViewPassPopUp.text_Password.getText();	// initial state and fetch the input
		
		// If the input is empty, clear the aspects of the user interface having to do with the
		// user input and tell the user that the input is empty.
		if (password.isEmpty()) {
			ViewPassPopUp.errPasswordPart1.setText("");
			ViewPassPopUp.errPasswordPart2.setText("");
			ViewPassPopUp.noInputFound.setText("No input text found!");
		}
		else
		{
			// There is user input, so evaluate it to see if it satisfies the requirements
			String errMessage = evaluatePassword(password);
			
			// Based on the evaluation, change the flag to green for each satisfied requirement
			updateFlags();
			
			// An empty string means there is no error message, which means the input is valid
			if (errMessage != "") {
				
				// Since the output is not empty, at least one requirement have not been satisfied.
				System.out.println(errMessage);			// Display the message to the console
				
				ViewPassPopUp.noInputFound.setText("");			// There was input, so no error message
				
				// Extract the input up to the point of the error and place it in Part 1
				ViewPassPopUp.errPasswordPart1.setText(password.substring(0, passwordIndexofError));
				
				// Place the red up arrow into Part 2
				ViewPassPopUp.errPasswordPart2.setText("\u21EB");
				
				// Tell the user about the meaning of the red up arrow
				ViewPassPopUp.errPasswordPart3.setText(
						"The red arrow points at the character causing the error!");
				
				// Tell the user that the password is not valid with a red message
				ViewPassPopUp.validPassword.setTextFill(Color.RED);
				ViewPassPopUp.validPassword.setText("Failure! The password is not valid.");
				
				// Ensure the button is disabled
				ViewPassPopUp.button_Finish.setDisable(true);
			}
			else {
				// All the requirements were satisfied - the password is valid
				System.out.println("Success! The password satisfies the requirements.");
				
				// Hide all of the error messages elements
				ViewPassPopUp.errPasswordPart1.setText("");
				ViewPassPopUp.errPasswordPart2.setText("");
				ViewPassPopUp.errPasswordPart3.setText("");
				
				// Tell the user that the password is valid with a green message
				ViewPassPopUp.validPassword.setTextFill(Color.GREEN);
				ViewPassPopUp.validPassword.setText("Success! The password satisfies the requirements.");
				
				// Enable the button so the user can accept this password or continue to add
				// more characters to the password and make it longer.
				ViewPassPopUp.button_Finish.setDisable(false);
			} 
		}
	}
	
	/*-********************************************************************************************
	 * 
	 * Attributes used by the Finite State Machine to inform the user about what was and was not
	 * valid and point to the character of the error.  This will enhance the user experience.
	 * 
	 */

	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;

	
	
	/*
	 * This private method checks each of the requirements and if one is satisfied, it changes the
	 * the text to tell the user of this fact and changes the text color from red to green.
	 * 
	 */
	
	private static void updateFlags() {
		if (foundUpperCase) {
			ViewPassPopUp.label_UpperCase.setText("At least one upper case letter - Satisfied");
			ViewPassPopUp.label_UpperCase.setTextFill(Color.GREEN);
		}

		if (foundLowerCase) {
			ViewPassPopUp.label_LowerCase.setText("At least one lower case letter - Satisfied");
			ViewPassPopUp.label_LowerCase.setTextFill(Color.GREEN);
		}

		if (foundNumericDigit) {
			ViewPassPopUp.label_NumericDigit.setText("At least one numeric digit - Satisfied");
			ViewPassPopUp.label_NumericDigit.setTextFill(Color.GREEN);
		}

		if (foundSpecialChar) {
			ViewPassPopUp.label_SpecialChar.setText("At least one special character - Satisfied");
			ViewPassPopUp.label_SpecialChar.setTextFill(Color.GREEN);
		}

		if (foundLongEnough) {
			ViewPassPopUp.label_LongEnough.setText("At least eight characters - Satisfied");
			ViewPassPopUp.label_LongEnough.setTextFill(Color.GREEN);
		}
	}
	

	/**********
	 * <p> Title: evaluatePassword - Public Method </p>
	 * 
	 * <p> Description: This method is a mechanical transformation of a Directed Graph diagram 
	 * into a Java method. This method is used by both the GUI version of the application as well
	 * as the testing automation version.
	 * 
	 * @param input		The input string evaluated by the directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a helpful description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordIndexofError = 0;			// Initialize the IndexofError

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state.  This
		// local variable is a working copy of the input.
		passwordInput = input;				// Save a copy of the input
		
		PasswordDTO result = InputValidator.verifyPassword(input);
		foundUpperCase = result.getFoundUpperCase();
		foundLowerCase = result.getFoundLowerCase();
		foundNumericDigit = result.getFoundNumericDigit();
		foundSpecialChar = result.getFoundSpecialChar();
		foundLongEnough = result.getFoundLongEnough();
		passwordIndexofError = result.getIndexOfError();
		
		if (passwordIndexofError != -1) {
			return "***Error*** " + input.charAt(passwordIndexofError) + " is an invalid character";
		}
		
		// Construct a String with a list of the requirement elements that were found.
		String errMessage = "";
		if (!foundUpperCase)
			errMessage += "Upper case; ";
		
		if (!foundLowerCase)
			errMessage += "Lower case; ";
		
		if (!foundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!foundSpecialChar)
			errMessage += "Special character; ";
			
		if (!foundLongEnough)
			errMessage += "Long Enough; ";
		
		if (errMessage == "")
			return "";
		
		// If it gets here, there something was not found, so return an appropriate message
		passwordIndexofError = input.length();
		return errMessage + "conditions were not satisfied";
	}
}
 
