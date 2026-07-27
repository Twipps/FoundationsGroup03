package guiComponents.staffHome;

import java.util.ArrayList;

import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.EvaluationParameterList;
import entityClasses.Thread;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: StaffReportParamCreateEditPanel Class </p>
 *
 * <p> Description: A static utility class that builds the create and edit
 * panel for staff evaluation parameters. When parameterID is -1, the panel
 * operates in Create mode. When parameterID identifies an existing parameter,
 * the panel operates in Edit mode and pre-populates the input fields. </p>
 *
 * <p> Staff users can define the parameter name, metric, comparison operator,
 * numeric threshold, optional description, optional thread scope, and active
 * status. Each staff user may create only one evaluation parameter for each
 * metric type. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00 2026-07-26 Initial implementation
 * @version 1.01 2026-07-26 Added one parameter per metric validation
 */
public class StaffReportParamCreateEditPanel {

	/** Shared reference to the application database. */
	private static Database theDatabase =
			applicationMain.FoundationsMain.database;

	/** Prevents creation of StaffReportParamCreateEditPanel objects. */
	private StaffReportParamCreateEditPanel() {}

	/*******
	 * <p> Method: createReportParamCreateEditPanel() </p>
	 *
	 * <p> Description: Builds and returns the evaluation parameter creation
	 * and editing panel. If parameterID is -1, a new parameter is created.
	 * Otherwise, the existing parameter is loaded and updated. </p>
	 *
	 * <p> Each staff user may create only one parameter for each metric.
	 * The duplicate check is applied during both creation and editing. </p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the main staff content pane
	 * @param parameterID the parameter ID to edit, or -1 when creating
	 * @return a VBox containing the evaluation parameter form
	 */
	public static VBox createReportParamCreateEditPanel(
			Stage theStage,
			BorderPane contentPane,
			int parameterID) {

		VBox rBox = new VBox(12);
		rBox.setPadding(new Insets(15));

		String currentUsername =
				theDatabase.getCurrentUsername();

		EvaluationParameterList parameterList =
				new EvaluationParameterList(currentUsername);

		EvaluationParameter currentParameter = null;

		/*
		 * Edit mode: retrieve the existing parameter using its ID.
		 */
		if (parameterID != -1) {
			currentParameter =
				parameterList.getEvaluationParameter(
					parameterID
				);
		}

		Label pageTitle = new Label(
			currentParameter == null
				? "Create Evaluation Parameter"
				: "Edit Evaluation Parameter"
		);

		pageTitle.setStyle(
			"-fx-font-size: 20px;" +
			"-fx-font-weight: bold;"
		);

		/*
		 * Name input.
		 */
		HBox nameStuff = new HBox(10);

		Label nameLabel = new Label("Name:");
		nameLabel.setPrefWidth(160);

		TextField nameInput = new TextField();
		nameInput.setPromptText(
			"Example: Minimum Posts"
		);
		nameInput.setPrefWidth(300);

		nameStuff.getChildren().addAll(
			nameLabel,
			nameInput
		);

		/*
		 * Metric selection.
		 */
		HBox metricStuff = new HBox(10);

		Label metricLabel = new Label("Metric:");
		metricLabel.setPrefWidth(160);

		ComboBox<String> metricSelection =
				new ComboBox<String>();

		metricSelection.getItems().addAll(
			"POST_COUNT",
			"REPLY_COUNT",
			"THREAD_COUNT",
			"DISTINCT_STUDENTS"
		);

		metricSelection.setPromptText(
			"Select a metric"
		);

		metricSelection.setPrefWidth(300);

		metricStuff.getChildren().addAll(
			metricLabel,
			metricSelection
		);

		/*
		 * Comparison operator selection.
		 */
		HBox operatorStuff = new HBox(10);

		Label operatorLabel =
				new Label("Comparison Operator:");

		operatorLabel.setPrefWidth(160);

		ComboBox<String> operatorSelection =
				new ComboBox<String>();

		operatorSelection.getItems().addAll(
			"GREATER_THAN",
			"GREATER_THAN_OR_EQUAL",
			"LESS_THAN",
			"LESS_THAN_OR_EQUAL",
			"EQUAL",
			"NOT_EQUAL"
		);

		operatorSelection.setValue(
			"GREATER_THAN_OR_EQUAL"
		);

		operatorSelection.setPrefWidth(300);

		operatorStuff.getChildren().addAll(
			operatorLabel,
			operatorSelection
		);

		/*
		 * Threshold input.
		 */
		HBox thresholdStuff = new HBox(10);

		Label thresholdLabel =
				new Label("Threshold:");

		thresholdLabel.setPrefWidth(160);

		TextField thresholdInput =
				new TextField();

		thresholdInput.setPromptText(
			"Enter a whole number"
		);

		thresholdInput.setPrefWidth(300);

		thresholdStuff.getChildren().addAll(
			thresholdLabel,
			thresholdInput
		);

		/*
		 * Thread scope selection.
		 *
		 * No selected thread means the parameter applies
		 * across every discussion thread.
		 */
		HBox threadStuff = new HBox(10);

		Label threadLabel =
				new Label("Thread Scope:");

		threadLabel.setPrefWidth(160);

		ComboBox<Thread> threadSelection =
				new ComboBox<Thread>();

		ArrayList<Thread> threads =
				theDatabase.getAllThreads();

		if (threads != null) {
			threadSelection.getItems().addAll(
				threads
			);
		}

		threadSelection.setPromptText(
			"All Threads"
		);

		/*
		 * Display each thread title instead of the
		 * Thread object's default toString value.
		 */
		threadSelection.setCellFactory(
			listView ->
				new ListCell<Thread>() {

					@Override
					protected void updateItem(
							Thread thread,
							boolean empty) {

						super.updateItem(
							thread,
							empty
						);

						if (empty || thread == null) {
							setText(null);
						}
						else {
							setText(
								thread.getTitle()
							);
						}
					}
				}
		);

		threadSelection.setButtonCell(
			new ListCell<Thread>() {

				@Override
				protected void updateItem(
						Thread thread,
						boolean empty) {

					super.updateItem(
						thread,
						empty
					);

					if (empty || thread == null) {
						setText("All Threads");
					}
					else {
						setText(
							thread.getTitle()
						);
					}
				}
			}
		);

		threadSelection.setPrefWidth(300);

		Button clearThreadButton =
				new Button("All Threads");

		clearThreadButton.setOnAction(e -> {
			threadSelection.setValue(null);
		});

		threadStuff.getChildren().addAll(
			threadLabel,
			threadSelection,
			clearThreadButton
		);

		/*
		 * Description input.
		 */
		HBox descriptionStuff =
				new HBox(10);

		Label descriptionLabel =
				new Label("Description:");

		descriptionLabel.setPrefWidth(160);

		TextArea descriptionInput =
				new TextArea();

		descriptionInput.setPromptText(
			"Optional description"
		);

		descriptionInput.setWrapText(true);
		descriptionInput.setPrefRowCount(5);
		descriptionInput.setPrefWidth(300);

		descriptionStuff.getChildren().addAll(
			descriptionLabel,
			descriptionInput
		);

		/*
		 * Active checkbox.
		 */
		HBox activeStuff = new HBox(10);

		Label activeLabel =
				new Label("Active:");

		activeLabel.setPrefWidth(160);

		CheckBox activeSelection =
				new CheckBox(
					"Include this parameter in evaluations"
				);

		activeSelection.setSelected(true);

		activeStuff.getChildren().addAll(
			activeLabel,
			activeSelection
		);

		/*
		 * Validation and database error message.
		 */
		Label errorLabel = new Label();

		errorLabel.setStyle(
			"-fx-text-fill: red;"
		);

		/*
		 * Save and cancel buttons.
		 */
		Button saveButton =
				new Button("Create Parameter");

		Button cancelButton =
				new Button("Cancel");

		HBox buttonStuff =
				new HBox(
					10,
					saveButton,
					cancelButton
				);

		/*
		 * Pre-populate the fields during Edit mode.
		 */
		if (currentParameter != null) {

			nameInput.setText(
				currentParameter.getName()
			);

			metricSelection.setValue(
				currentParameter.getMetric()
			);

			operatorSelection.setValue(
				currentParameter
					.getComparisonOperator()
			);

			thresholdInput.setText(
				String.valueOf(
					currentParameter.getThreshold()
				)
			);

			descriptionInput.setText(
				currentParameter.getDescription()
			);

			activeSelection.setSelected(
				currentParameter.isActive()
			);

			Integer currentThreadID =
					currentParameter.getThreadID();

			if (currentThreadID != null
					&& threads != null) {

				for (Thread thread : threads) {

					if (thread.getThreadID()
							== currentThreadID) {

						threadSelection.setValue(
							thread
						);

						break;
					}
				}
			}

			saveButton.setText("Save Changes");
		}

		final EvaluationParameter finalParameter =
				currentParameter;

		/*
		 * Create or update the evaluation parameter.
		 */
		saveButton.setOnAction(e -> {

			errorLabel.setText("");

			String inName =
					nameInput.getText();

			String inMetric =
					metricSelection.getValue();

			String inOperator =
					operatorSelection.getValue();

			String thresholdText =
					thresholdInput.getText();

			String inDescription =
					descriptionInput.getText();

			Thread selectedThread =
					threadSelection.getValue();

			boolean inActive =
					activeSelection.isSelected();

			/*
			 * Validate name.
			 */
			if (inName == null
					|| inName.isBlank()) {

				errorLabel.setText(
					"Name cannot be empty."
				);

				return;
			}

			/*
			 * Validate metric.
			 */
			if (inMetric == null
					|| inMetric.isBlank()) {

				errorLabel.setText(
					"A metric must be selected."
				);

				return;
			}

			/*
			 * Validate comparison operator.
			 */
			if (inOperator == null
					|| inOperator.isBlank()) {

				errorLabel.setText(
					"A comparison operator must be selected."
				);

				return;
			}

			/*
			 * Validate threshold.
			 */
			if (thresholdText == null
					|| thresholdText.isBlank()) {

				errorLabel.setText(
					"Threshold cannot be empty."
				);

				return;
			}

			int inThreshold;

			try {
				inThreshold =
					Integer.parseInt(
						thresholdText.trim()
					);
			}
			catch (NumberFormatException exception) {

				errorLabel.setText(
					"Threshold must be a whole number."
				);

				return;
			}

			if (inThreshold < 0) {

				errorLabel.setText(
					"Threshold cannot be negative."
				);

				return;
			}

			/*
			 * Null threadID means this parameter applies
			 * to all discussion threads.
			 */
			Integer inThreadID = null;

			if (selectedThread != null) {
				inThreadID =
					selectedThread.getThreadID();
			}

			/*
			 * Check whether the staff user already owns
			 * another parameter using the selected metric.
			 *
			 * During Edit mode, the current parameter is
			 * ignored so it can keep its original metric.
			 */
			EvaluationParameter duplicateParameter =
					findParameterByMetric(
						parameterList,
						inMetric
					);

			if (duplicateParameter != null
					&& (
						finalParameter == null
						|| duplicateParameter
							.getParameterID()
							!= finalParameter
								.getParameterID()
					)) {

				errorLabel.setText(
					"You already have an evaluation "
					+ "parameter for "
					+ formatMetric(inMetric)
					+ "."
				);

				return;
			}

			boolean operationSuccessful;

			/*
			 * Create mode.
			 */
			if (finalParameter == null) {

				operationSuccessful =
					theDatabase.addEvaluationParameter(
						currentUsername,
						inName.trim(),
						inMetric,
						inOperator,
						inThreshold,
						inDescription,
						inThreadID,
						inActive
					);
			}

			/*
			 * Edit mode.
			 */
			else {

				operationSuccessful =
					theDatabase.updateEvaluationParameter(
						finalParameter.getParameterID(),
						currentUsername,
						inName.trim(),
						inMetric,
						inOperator,
						inThreshold,
						inDescription,
						inThreadID,
						inActive
					);
			}

			if (!operationSuccessful) {

				errorLabel.setText(
					"Unable to save the evaluation parameter."
				);

				return;
			}

			/*
			 * Return to the evaluation parameter list after
			 * the database operation succeeds.
			 */
            contentPane.setCenter(
                    StaffReportResultDisplayPanel.createReportResultDisplayPanel(
                        theStage
                    )
			);
		});

		/*
		 * Return to the parameter list without saving.
		 */
		cancelButton.setOnAction(e -> {

            contentPane.setCenter(
                    StaffReportResultDisplayPanel.createReportResultDisplayPanel(
                        theStage
                    )
			);
		});

		rBox.getChildren().addAll(
			pageTitle,
			nameStuff,
			metricStuff,
			operatorStuff,
			thresholdStuff,
			threadStuff,
			descriptionStuff,
			activeStuff,
			errorLabel,
			buttonStuff
		);

		return rBox;
	}

	/*******
	 * <p> Method: findParameterByMetric() </p>
	 *
	 * <p> Description: Searches the staff user's current evaluation parameters
	 * for one using the supplied metric. This supports the requirement that
	 * each staff user may create only one parameter for each metric type. </p>
	 *
	 * @param parameterList the staff user's evaluation parameter collection
	 * @param metric the metric being searched for
	 * @return the matching parameter, or null if no match exists
	 */
	private static EvaluationParameter findParameterByMetric(
			EvaluationParameterList parameterList,
			String metric) {

		if (parameterList == null
				|| metric == null) {

			return null;
		}

		ArrayList<EvaluationParameter> parameters =
				parameterList
					.getEvaluationParameterList();

		if (parameters == null) {
			return null;
		}

		for (EvaluationParameter parameter : parameters) {

			if (parameter == null
					|| parameter.getMetric() == null) {

				continue;
			}

			if (parameter.getMetric()
					.equalsIgnoreCase(metric)) {

				return parameter;
			}
		}

		return null;
	}

	/*******
	 * <p> Method: formatMetric() </p>
	 *
	 * <p> Description: Converts a stored metric value such as POST_COUNT
	 * into a more readable value such as Post Count. </p>
	 *
	 * @param metric the stored metric value
	 * @return the formatted metric value
	 */
	private static String formatMetric(
			String metric) {

		if (metric == null || metric.isBlank()) {
			return "Unknown Metric";
		}

		String[] words =
				metric.toLowerCase().split("_");

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
}
