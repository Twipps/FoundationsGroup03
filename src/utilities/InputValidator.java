package utilities;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

public final class InputValidator {
	
	private InputValidator() {		//There is no need to make an instance of this class
	}
	
	public static final int MAX_INPUT_LENGTH = 32;  			// No input can exceed 32 chars
	
	//apply this filter to each input field using:
	//myTextField.setTextFormatter(new TextFormatter<Type>(InputValidator.maxLengthFilter)); //Restrict input to MAX_INPUT_LENGTH
	//or:
	//myTextInputDialog.getEditor().setTextFormatter(new TextFormatter<Type>(InputValidator.maxLengthFilter)); //Restrict input to MAX_INPUT_LENGTH
	//to restrict max length to the universal max input length
	public static UnaryOperator<TextFormatter.Change> maxLengthFilter = change -> {
        if (change.getControlNewText().length() > MAX_INPUT_LENGTH) {
            return null; // Reject the change
        }
        return change; // Accept the change
    };
    	
	//verifyUserName variables
	private static String userNameRecognizerErrorMessage = "";  // The error message text
	private static int userNameRecognizerIndexofError = -1;     // The index of error location
	private static int state = 0;
	private static int nextState = 0;
	private static char currentChar;
	private static int currentCharNdx = 0;
	private static boolean running;
	private static int userNameSize = 0;
	private static String inputLine = "";    // The input line
	private static boolean finalState = false; // Is this state a final state?
	
	public static boolean verifyInputLength(String input) {
		return input.length() <= MAX_INPUT_LENGTH;
	}
	
	public static String verifyPassword(String password) {
		return "";
	}
	
	public static String verifyUsername(String input) {

		// Reject empty input immediately; there is no meaningful character to point at.
		if (input.length() <= 0) {
			userNameRecognizerIndexofError = 0;
			return "\n*** ERROR *** The input is empty";
		}

		// Initialise all FSM state variables for a fresh run.
		state = 0;
		inputLine = input;
		currentCharNdx = 0;
		currentChar = input.charAt(0);

		running = true;
		nextState = -1;

		System.out.println("\nCurrent Final Input  Next\nState   State Char  State  Size");

		// Semantic action [0]: initialise the size counter before the first transition.
		userNameSize = 0;

		// ---------------------------------------------------------------------------------
		// Main FSM loop.  Each iteration processes one character in the current state and,
		// if a valid transition exists, advances to the next state and character.  The loop
		// stops when the end of the input is reached or when no valid transition is found.
		// ---------------------------------------------------------------------------------
		while (running) {
			switch (state) {

			// -----------------------------------------------------------------
			// State 0: start state.
			// Valid transition: AlphaChar (A–Z, a–z) → state 1.
			// Digits are intentionally excluded here.  They are valid UNChars
			// in subsequent positions (states 1 and 2) but not as the very
			// first character of a username, per the updated requirements.
			// -----------------------------------------------------------------
			case 0:
				if (helperMethods.isAlphaChar(currentChar)) {
					nextState = 1;
					// Semantic action [1]: count the character and update tooShort / tooLong.
					userNameSize++;
					// A single character cannot yet exceed 32, so the upper-bound check
					// is skipped here for efficiency; state 1 handles it on every transition.
				} else {
					// No valid transition from state 0 on this character; stop the FSM.
					running = false;
				}
				break;

			// -----------------------------------------------------------------
			// State 1: inside a valid alphanumeric run (the only final state).
			// Two valid transitions:
			//   UNChar (A–Z, a–z, 0–9) → stay in state 1
			//   SepChar (- _ . &)       → move to state 2
			// -----------------------------------------------------------------
			case 1:
				if (helperMethods.isUNChar(currentChar)) {
					nextState = 1;
					// Semantic action [1]: count the character and check bounds.
					userNameSize++;
				} else if (helperMethods.isSepChar(currentChar)) {
					nextState = 2;
					// Separator characters still count toward the total length.
					userNameSize++;
				} else {
					// Unrecognised character; halt the FSM.
					running = false;
				}
				// If the size has exceeded 32, stop immediately rather than reading further;
				// the error will be reported in the post-loop state 1 handler below.
				if (userNameSize > 32)
					running = false;
				break;

			// -----------------------------------------------------------------
			// State 2: just consumed a SepChar.
			// A SepChar is only valid between two UNChar characters, so the only
			// valid transition is UNChar → state 1.  Reaching end-of-input here,
			// or seeing another SepChar, is an error (caught in the post-loop
			// switch below).
			// -----------------------------------------------------------------
			case 2:
				if (helperMethods.isUNChar(currentChar)) {
					nextState = 1;
					// Semantic action [1]: count the character and check bounds.
					userNameSize++;
				} else {
					// Another SepChar or illegal character after a SepChar; halt.
					running = false;
				}
				// Same upper-bound guard as state 1.
				if (userNameSize > 32)
					running = false;
				break;

			} // end switch

			if (running) {
				helperMethods.displayDebuggingInfo();
				// Advance to the next character.  If the input is exhausted,
				// moveToNextCharacter clears the running flag for us.
				helperMethods.moveToNextCharacter();

				// Commit the transition.
				state = nextState;

				// State 1 is the only final state.
				finalState = (state == 1);

				// Reset nextState so a forgotten assignment is detectable.
				nextState = -1;
			}
		} // end while

		helperMethods.displayDebuggingInfo();
		System.out.println("The loop has ended.");

		// ---------------------------------------------------------------------------------
		// Post-loop error analysis.  The FSM has halted; determine whether that is because
		// the entire input was accepted (state 1, all input consumed, valid length) or
		// because of a specific error.  Each state can produce a targeted message.
		// ---------------------------------------------------------------------------------
		userNameRecognizerIndexofError = currentCharNdx; // Position of the problem character.
		userNameRecognizerErrorMessage = "\n*** ERROR *** ";

		switch (state) {

		case 0:
			// The FSM never left state 0, meaning the very first character was not alphabetic.
			// Digits, separators, and all other characters are rejected here.
			userNameRecognizerErrorMessage +=
				"A UserName must start with an alphabetic character (A–Z or a–z).\n";
			return userNameRecognizerErrorMessage;

		case 1:
			// State 1 is the accepting state.  Three sub-cases can still be errors:
			//   (a) the name is shorter than 4 characters,
			//   (b) the name is longer than 32 characters, or
			//   (c) the input was not fully consumed (an illegal character was found).

			if (userNameSize < 4) {
				userNameRecognizerErrorMessage +=
					"A UserName must have at least 4 characters.\n";
				return userNameRecognizerErrorMessage;
			} else if (userNameSize > 32) {
				userNameRecognizerErrorMessage +=
					"A UserName must have no more than 32 characters.\n";
				return userNameRecognizerErrorMessage;
			} else if (currentCharNdx < input.length()) {
				// There are unprocessed characters remaining; the one at currentCharNdx
				// was the character that caused the FSM to halt.
				userNameRecognizerErrorMessage +=
					"A UserName may only contain A–Z, a–z, 0–9, or the separators - _ . & "
					+ "between alphanumeric characters.\n";
				return userNameRecognizerErrorMessage;
			} else {
				// All characters consumed, length valid: the username is accepted.
				userNameRecognizerIndexofError = -1;
				userNameRecognizerErrorMessage = "";
				return userNameRecognizerErrorMessage;
			}

		case 2:
			// The FSM ended in state 2, meaning the username ends with a separator character
			// or two separators appeared consecutively.  Both are invalid because a SepChar
			// must always be followed by a UNChar.
			// Semantic action [2]: issue error if SizeCounter < 4 (caught by size check above,
			// but the message here covers the structural separator error specifically).
			userNameRecognizerErrorMessage +=
				"A separator character (-, _, ., or &) must be followed by an alphanumeric "
				+ "character (A–Z, a–z, 0–9). Consecutive separators and a trailing separator "
				+ "are not allowed.\n";
			return userNameRecognizerErrorMessage;

		default:
			// Guard against any future state added without a corresponding case.
			return "";
		}
	}
	
	private class helperMethods {
		/*****
		 * Private helper: advance currentCharNdx by one.  If the new index is still within the
		 * input string, currentChar is updated to the character at that position.  Otherwise,
		 * currentChar is set to a space (a character that matches no valid transition) and the
		 * running flag is cleared so the FSM loop terminates on its next iteration check.
		 */
		private static void moveToNextCharacter() {
			currentCharNdx++;
			if (currentCharNdx < inputLine.length())
				currentChar = inputLine.charAt(currentCharNdx);
			else {
				currentChar = ' ';
				running = false;
			}
		}
		
		/*****
		 * Private helper: display one row of the FSM execution trace to System.out.
		 * Each row shows the current state, whether it is a final state, the current input character,
		 * the next state the FSM will move to, and the running size counter.
		 */
		private static void displayDebuggingInfo() {
			if (currentCharNdx >= inputLine.length())
				System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state +
						((finalState) ? "       F   " : "           ") + "None");
			else
				System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state +
					((finalState) ? "       F   " : "           ") + "  " + currentChar + " " +
					((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") +
					nextState + "     " + userNameSize);
		}
		
		/*****
		 * Private helper: returns true when the given character is an alphanumeric character,
		 * i.e. one of A–Z, a–z, or 0–9.  These are called UNChar (UserName characters) in the
		 * FSM diagram.
		 *
		 * @param ch  the character to test
		 * @return    true if ch is A–Z, a–z, or 0–9; false otherwise
		 */
		private static boolean isUNChar(char ch) {
			return (ch >= 'A' && ch <= 'Z') ||
			       (ch >= 'a' && ch <= 'z') ||
			       (ch >= '0' && ch <= '9');
		}

		/*****
		 * Private helper: returns true when the given character is an alphabetic character,
		 * i.e. one of A–Z or a–z.  Only alphabetic characters are allowed as the very first
		 * character of a username (AlphaChar in the FSM diagram).  This restriction prevents
		 * usernames that begin with a digit or a separator, which could be mistaken for numeric
		 * identifiers or produce confusing display artefacts.
		 *
		 * @param ch  the character to test
		 * @return    true if ch is A–Z or a–z; false otherwise
		 */
		private static boolean isAlphaChar(char ch) {
			return (ch >= 'A' && ch <= 'Z') ||
			       (ch >= 'a' && ch <= 'z');
		}

		/*****
		 * Private helper: returns true when the given character is one of the four recognised
		 * separator characters: dash (-), underscore (_), period (.), or ampersand (&amp;).
		 * These are called SepChar in the FSM diagram.  A SepChar is only valid between two
		 * UNChar characters; consecutive separators and a trailing separator are both errors.
		 *
		 * @param ch  the character to test
		 * @return    true if ch is -, _, ., or &amp;; false otherwise
		 */
		private static boolean isSepChar(char ch) {
			return ch == '-' || ch == '_' || ch == '.' || ch == '&';
		}
	}
	

}
