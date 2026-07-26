/**
 * FoundationsGroup03 module — CSE 360 Team 3 TP2 project.
 * Requires JavaFX controls and graphics, and the Java SQL module.
 */
module Foundations26{
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;

	opens applicationMain to javafx.graphics, javafx.fxml;
}