/**
 * <p>Title: Foundations26 Module</p>
 *
 * <p>Description: Defines the Java module configuration for the Foundations26
 * application. Specifies the required modules and package accessibility used
 * by the application.</p>
 */
module Foundations26 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
}