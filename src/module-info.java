/**
 * FoundationsGroup03 module — CSE 360 Team 3 TP3 project.
 * Requires JavaFX controls and graphics, and the Java SQL module.
 */


module FoundationsGroup03 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;

	opens applicationMain to javafx.graphics, javafx.fxml;
}