package guiComponents.staffHome;

import java.util.ArrayList;
import java.util.Optional;

import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.EvaluationParameterList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: StaffReportParamListPanel Class </p>
 *
 * <p> Description: A static utility class that builds the evaluation
 * parameter management panel for staff users. The panel displays a button
 * for creating a new evaluation parameter, a refresh button, and a
 * scrollable list of the current staff user's parameters. </p>
 *
 * <p> Each evaluation parameter row displays the parameter's name, metric,
 * comparison operator, threshold, optional description, thread scope, and
 * active status. Each row also provides Edit and Delete buttons for managing
 * the parameter. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00 2026-07-26 Initial implementation for TP3
 * @version 1.01 2026-07-26 Removed evaluation parameter search functionality
 */
public class StaffReportParamListPanel {

	/** Shared reference to the application database. */
	private static Database theDatabase =
		applicationMain.FoundationsMain.database;

	/** Prevents creation of StaffReportParamListPanel objects. */
	private StaffReportParamListPanel() {}

	/*******
	 * <p> Method: createReportParamListPanel() </p>
	 *
	 * <p> Description: Builds and returns the complete evaluation parameter
	 * management panel. The panel contains controls for creating, refreshing,
	 * editing, and deleting evaluation parameters. </p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the main content pane used by the staff interface
	 * @return a VBox containing the evaluation parameter controls
	 */
	public static VBox createReportParamListPanel(
			Stage theStage,
			BorderPane contentPane) {

		VBox rBox = new VBox(10);
		rBox.setPadding(new Insets(10));

		Label pageTitle =
			new Label("Evaluation Parameters");

		pageTitle.setStyle(
			"-fx-font-size: 20px;" +
			"-fx-font-weight: bold;"
		);

		Button createParameter =
			new Button("Create Evaluation Parameter");

		createParameter.setMaxWidth(
			Double.MAX_VALUE
		);

		createParameter.setOnAction(e -> {

			contentPane.setCenter(
				StaffReportParamCreateEditPanel
					.createReportParamCreateEditPanel(
						theStage,
						contentPane,
						-1
					)
			);
		});

		Button refresh = new Button("Refresh");

		refresh.setMaxWidth(
			Double.MAX_VALUE
		);

		VBox parameterDisplayList =
			new VBox(8);

		ScrollPane scrollPane =
			new ScrollPane(
				parameterDisplayList
			);

		scrollPane.setFitToWidth(true);

		VBox.setVgrow(
			scrollPane,
			Priority.ALWAYS
		);

		refreshParameterList(
			parameterDisplayList,
			contentPane,
			theStage
		);

		refresh.setOnAction(e -> {

			refreshParameterList(
				parameterDisplayList,
				contentPane,
				theStage
			);
		});

		rBox.getChildren().addAll(
			pageTitle,
			createParameter,
			refresh,
			scrollPane
		);

		return rBox;
	}

	/*******
	 * <p> Method: refreshParameterList() </p>
	 *
	 * <p> Description: Reloads the current staff user's evaluation parameters
	 * from the database and rebuilds the displayed parameter list. </p>
	 *
	 * @param parameterDisplayList the VBox displaying parameter rows
	 * @param contentPane the main staff content pane
	 * @param theStage the primary application stage
	 */
	public static void refreshParameterList(
			VBox parameterDisplayList,
			BorderPane contentPane,
			Stage theStage) {

		parameterDisplayList
			.getChildren()
			.clear();

		String currentStaffUsername =
			theDatabase.getCurrentUsername();

		EvaluationParameterList allParameters =
			new EvaluationParameterList(
				currentStaffUsername
			);

		ArrayList<EvaluationParameter> parameters =
			allParameters
				.getEvaluationParameterList();

		if (parameters == null) {

			parameterDisplayList
				.getChildren()
				.add(
					new Label(
						"Unable to load evaluation parameters."
					)
				);

			return;
		}

		for (EvaluationParameter parameter
				: parameters) {

			if (parameter == null) {
				continue;
			}

			parameterDisplayList
				.getChildren()
				.add(
					createParameterRow(
						parameter,
						allParameters,
						parameterDisplayList,
						contentPane,
						theStage
					)
				);
		}

		if (parameterDisplayList
				.getChildren()
				.isEmpty()) {

			parameterDisplayList
				.getChildren()
				.add(
					new Label(
						"No evaluation parameters found."
					)
				);
		}
	}

	/*******
	 * <p> Method: createParameterRow() </p>
	 *
	 * <p> Description: Builds one row representing an evaluation parameter.
	 * The row displays the parameter information and includes Edit and Delete
	 * buttons. </p>
	 *
	 * @param parameter the parameter represented by this row
	 * @param parameterList the model used for deletion
	 * @param parameterDisplayList the displayed list being refreshed
	 * @param contentPane the main staff content pane
	 * @param theStage the primary application stage
	 * @return an HBox representing the evaluation parameter
	 */
	public static HBox createParameterRow(
			EvaluationParameter parameter,
			EvaluationParameterList parameterList,
			VBox parameterDisplayList,
			BorderPane contentPane,
			Stage theStage) {

		HBox rBox = new HBox(12);

		rBox.setPadding(
			new Insets(10)
		);

		rBox.setStyle(
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 3;" +
			"-fx-background-radius: 3;"
		);

		VBox parameterInformation =
			new VBox(5);

		Label nameLabel =
			new Label(
				safeDisplay(
					parameter.getName()
				)
			);

		nameLabel.setStyle(
			"-fx-font-weight: bold;" +
			"-fx-font-size: 14px;"
		);

		Label ruleLabel =
			new Label(
				formatMetric(
					parameter.getMetric()
				)
				+ " "
				+ formatOperator(
					parameter
						.getComparisonOperator()
				)
				+ " "
				+ parameter.getThreshold()
			);

		Label descriptionLabel =
			new Label(
				makeSample(
					parameter.getDescription()
				)
			);

		descriptionLabel.setWrapText(true);

		descriptionLabel.setStyle(
			"-fx-text-fill: gray;"
		);

		Label scopeLabel =
			new Label();

		if (parameter.getThreadID() == null) {

			scopeLabel.setText(
				"Scope: All threads"
			);
		}
		else {

			scopeLabel.setText(
				"Scope: Thread "
				+ parameter.getThreadID()
			);
		}

		scopeLabel.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		Label statusLabel =
			new Label(
				parameter.isActive()
					? "Status: Active"
					: "Status: Inactive"
			);

		statusLabel.setStyle(
			"-fx-text-fill: gray;" +
			"-fx-font-size: 11px;"
		);

		parameterInformation
			.getChildren()
			.addAll(
				nameLabel,
				ruleLabel,
				descriptionLabel,
				scopeLabel,
				statusLabel
			);

		HBox.setHgrow(
			parameterInformation,
			Priority.ALWAYS
		);

		Region spacer =
			new Region();

		HBox.setHgrow(
			spacer,
			Priority.ALWAYS
		);

		Button editButton =
			new Button("Edit");

		editButton.setOnAction(e -> {

			contentPane.setCenter(
				StaffReportParamCreateEditPanel
					.createReportParamCreateEditPanel(
						theStage,
						contentPane,
						parameter
							.getParameterID()
					)
			);
		});

		Button deleteButton =
			new Button("Delete");

		deleteButton.setOnAction(e -> {

			Alert confirmation =
				new Alert(
					Alert.AlertType.CONFIRMATION
				);

			confirmation.setTitle(
				"Delete Evaluation Parameter"
			);

			confirmation.setHeaderText(
				"Delete \""
				+ safeDisplay(
					parameter.getName()
				)
				+ "\"?"
			);

			confirmation.setContentText(
				"This evaluation parameter will be "
				+ "permanently deleted."
			);

			Optional<ButtonType> result =
				confirmation.showAndWait();

			if (result.isPresent()
					&& result.get()
						== ButtonType.OK) {

				parameterList
					.deleteEvaluationParameter(
						parameter
							.getParameterID()
					);

				refreshParameterList(
					parameterDisplayList,
					contentPane,
					theStage
				);
			}
		});

		HBox buttonBox =
			new HBox(
				8,
				editButton,
				deleteButton
			);

		rBox.getChildren().addAll(
			parameterInformation,
			spacer,
			buttonBox
		);

		return rBox;
	}

	/*******
	 * <p> Method: formatMetric() </p>
	 *
	 * <p> Description: Converts a stored metric value such as POST_COUNT
	 * into a readable GUI label such as Post Count. </p>
	 *
	 * @param metric the stored metric value
	 * @return the formatted metric name
	 */
	private static String formatMetric(
			String metric) {

		if (metric == null
				|| metric.isBlank()) {

			return "Unknown Metric";
		}

		String[] words =
			metric
				.toLowerCase()
				.split("_");

		StringBuilder formatted =
			new StringBuilder();

		for (String word : words) {

			if (word.isBlank()) {
				continue;
			}

			if (formatted.length() > 0) {
				formatted.append(" ");
			}

			formatted.append(
				Character.toUpperCase(
					word.charAt(0)
				)
			);

			if (word.length() > 1) {

				formatted.append(
					word.substring(1)
				);
			}
		}

		return formatted.toString();
	}

	/*******
	 * <p> Method: formatOperator() </p>
	 *
	 * <p> Description: Converts a stored comparison operator into a readable
	 * mathematical symbol for display. </p>
	 *
	 * @param operator the stored comparison operator
	 * @return a readable comparison symbol
	 */
	private static String formatOperator(
			String operator) {

		if (operator == null) {
			return "?";
		}

		switch (operator) {

			case "GREATER_THAN":
				return ">";

			case "GREATER_THAN_OR_EQUAL":
				return ">=";

			case "LESS_THAN":
				return "<";

			case "LESS_THAN_OR_EQUAL":
				return "<=";

			case "EQUAL":
				return "=";

			case "NOT_EQUAL":
				return "!=";

			default:
				return operator;
		}
	}

	/*******
	 * <p> Method: makeSample() </p>
	 *
	 * <p> Description: Produces a shortened preview of the parameter
	 * description. Descriptions longer than 100 characters are truncated. </p>
	 *
	 * @param description the parameter description
	 * @return a shortened description
	 */
	private static String makeSample(
			String description) {

		if (description == null
				|| description.isBlank()) {

			return "No description.";
		}

		if (description.length() <= 100) {
			return description;
		}

		return description.substring(
			0,
			100
		) + "...";
	}

	/*******
	 * <p> Method: safeDisplay() </p>
	 *
	 * <p> Description: Returns a display safe version of a string. </p>
	 *
	 * @param value the value being displayed
	 * @return the original value or "Unnamed Parameter"
	 */
	private static String safeDisplay(
			String value) {

		if (value == null
				|| value.isBlank()) {

			return "Unnamed Parameter";
		}

		return value;
	}
}