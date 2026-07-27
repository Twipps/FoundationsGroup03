package guiComponents.staffHome;

import java.util.ArrayList;
import java.util.List;

import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.EvaluationParameterList;
import entityClasses.User;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: ReportResultDisplayPanel Class </p>
 *
 * <p> Description: A static utility class that builds the staff engagement
 * report panel. The panel contains a Run Report button and a scrollable list
 * of all student users. </p>
 *
 * <p> When the report is run, each student is evaluated using only the active
 * evaluation parameters owned by the currently logged in staff user. A
 * student passes when they satisfy every active parameter. A student fails
 * when they fail one or more active parameters. </p>
 *
 * @author James Suchovic (Team 3)
 *
 * @version 1.00 2026-07-26 Initial implementation
 */
public class StaffReportResultDisplayPanel {

	/** Shared reference to the application database. */
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/** Prevents creation of ReportResultDisplayPanel objects. */
	private StaffReportResultDisplayPanel() {}

	/*******
	 * <p> Method: createReportResultDisplayPanel() </p>
	 *
	 * <p> Description: Builds and returns the engagement report display panel.
	 * The report is not calculated until the staff user presses the Run Report
	 * button. </p>
	 *
	 * @param theStage the primary application stage
	 * @return a VBox containing the report controls and results
	 */
	public static VBox createReportResultDisplayPanel(
			Stage theStage) {

		VBox rBox = new VBox(12);
		rBox.setPadding(new Insets(15));

		Label pageTitle =
				new Label("Student Engagement Report");

		pageTitle.setStyle(
			"-fx-font-size: 20px;" +
			"-fx-font-weight: bold;"
		);

		Label instructions = new Label(
			"Run all students against your active "
			+ "evaluation parameters."
		);

		instructions.setWrapText(true);

		Button runReportButton =
				new Button("Run Report");

		runReportButton.setMaxWidth(
			Double.MAX_VALUE
		);

		Label statusLabel = new Label();

		VBox reportResults = new VBox(8);

		ScrollPane resultScrollPane =
				new ScrollPane(reportResults);

		resultScrollPane.setFitToWidth(true);

		VBox.setVgrow(
			resultScrollPane,
			Priority.ALWAYS
		);

		runReportButton.setOnAction(e -> {

			runReport(
				reportResults,
				statusLabel
			);
		});

		rBox.getChildren().addAll(
			pageTitle,
			instructions,
			runReportButton,
			statusLabel,
			resultScrollPane
		);

		return rBox;
	}

	/*******
	 * <p> Method: runReport() </p>
	 *
	 * <p> Description: Loads the currently logged in staff user's active
	 * evaluation parameters and evaluates every student against them. The
	 * displayed report is rebuilt each time this method runs. </p>
	 *
	 * @param reportResults the VBox containing student result rows
	 * @param statusLabel the label used to display report status messages
	 */
	private static void runReport(
			VBox reportResults,
			Label statusLabel) {

		reportResults.getChildren().clear();
		statusLabel.setText("");

		String currentStaffUsername =
				theDatabase.getCurrentUsername();

		EvaluationParameterList parameterList =
				new EvaluationParameterList(
					currentStaffUsername
				);

		ArrayList<EvaluationParameter> parameters =
				getActiveParameters(
					parameterList
				);

		if (parameters.isEmpty()) {

			statusLabel.setText(
				"You do not have any active "
				+ "evaluation parameters."
			);

			return;
		}

		List<User> students = theDatabase.getAllStudentUsers();

		if (students == null
				|| students.isEmpty()) {

			statusLabel.setText(
				"No student users were found."
			);

			return;
		}

		int passedStudents = 0;
		int failedStudents = 0;

		for (User student : students) {

			if (student == null) {
				continue;
			}

			boolean passed =
					evaluateStudent(
						student,
						parameters
					);

			if (passed) {
				passedStudents++;
			}
			else {
				failedStudents++;
			}

			reportResults.getChildren().add(
				createStudentResultRow(
					student,
					passed
				)
			);
		}

		statusLabel.setText(
			"Parameters used: "
			+ parameters.size()
			+ " | Passed: "
			+ passedStudents
			+ " | Failed: "
			+ failedStudents
		);
	}

	/*******
	 * <p> Method: getActiveParameters() </p>
	 *
	 * <p> Description: Returns only the active parameters owned by the
	 * currently logged in staff user. The supplied EvaluationParameterList
	 * already contains only parameters belonging to that staff user. </p>
	 *
	 * @param parameterList the current staff user's parameter collection
	 * @return a list containing only active parameters
	 */
	private static ArrayList<EvaluationParameter>
			getActiveParameters(
				EvaluationParameterList parameterList) {

		ArrayList<EvaluationParameter> activeParameters =
				new ArrayList<EvaluationParameter>();

		if (parameterList == null) {
			return activeParameters;
		}

		ArrayList<EvaluationParameter> allParameters =
				parameterList
					.getEvaluationParameterList();

		if (allParameters == null) {
			return activeParameters;
		}

		for (EvaluationParameter parameter
				: allParameters) {

			if (parameter != null
					&& parameter.isActive()) {

				activeParameters.add(parameter);
			}
		}

		return activeParameters;
	}

	/*******
	 * <p> Method: evaluateStudent() </p>
	 *
	 * <p> Description: Evaluates one student against every active parameter.
	 * The student passes only when every parameter requirement is satisfied.
	 * Evaluation stops immediately when one parameter fails. </p>
	 *
	 * @param student the student being evaluated
	 * @param parameters the active staff owned parameters
	 * @return true if the student satisfies every parameter
	 */
	private static boolean evaluateStudent(
			User student,
			ArrayList<EvaluationParameter> parameters) {

		String username =
				student.getUserName();

		for (EvaluationParameter parameter
				: parameters) {

			int studentValue =
					getStudentMetricValue(
						username,
						parameter
					);

			boolean parameterPassed =
					compareValue(
						studentValue,
						parameter
							.getComparisonOperator(),
						parameter.getThreshold()
					);

			if (!parameterPassed) {
				return false;
			}
		}

		return true;
	}

	/*******
	 * <p> Method: getStudentMetricValue() </p>
	 *
	 * <p> Description: Retrieves the student's value for the metric specified
	 * by an evaluation parameter. If the parameter contains a thread ID, only
	 * activity from that thread is counted. A null thread ID means activity
	 * across all threads is counted. </p>
	 *
	 * @param username the student username
	 * @param parameter the parameter being evaluated
	 * @return the student's calculated value for the metric
	 */
	private static int getStudentMetricValue(
			String username,
			EvaluationParameter parameter) {

		String metric =
				parameter.getMetric();

		Integer threadID =
				parameter.getThreadID();

		if ("POST_COUNT".equals(metric)) {

			return theDatabase.getStudentPostCount(
					username,
					threadID
				);
		}

		if ("REPLY_COUNT".equals(metric)) {

			return theDatabase.getStudentReplyCount(
					username,
					threadID
				);
		}

		if ("THREAD_COUNT".equals(metric)) {

			return theDatabase.getStudentThreadParticipationCount(
					username
				);
		}

		if ("DISTINCT_STUDENTS".equals(metric)) {

			return theDatabase.getDistinctStudentsEngagedCount(
					username,
					threadID
				);
		}

		return 0;
	}

	/*******
	 * <p> Method: compareValue() </p>
	 *
	 * <p> Description: Applies the stored comparison operator to the student's
	 * calculated metric value and the required threshold. </p>
	 *
	 * @param studentValue the student's calculated metric value
	 * @param comparisonOperator the stored comparison operation
	 * @param threshold the staff defined requirement
	 * @return true if the student satisfies the comparison
	 */
	private static boolean compareValue(
			int studentValue,
			String comparisonOperator,
			int threshold) {

		if (comparisonOperator == null) {
			return false;
		}

		switch (comparisonOperator) {

			case "GREATER_THAN":
				return studentValue > threshold;

			case "GREATER_THAN_OR_EQUAL":
				return studentValue >= threshold;

			case "LESS_THAN":
				return studentValue < threshold;

			case "LESS_THAN_OR_EQUAL":
				return studentValue <= threshold;

			case "EQUAL":
				return studentValue == threshold;

			case "NOT_EQUAL":
				return studentValue != threshold;

			default:
				return false;
		}
	}

	/*******
	 * <p> Method: createStudentResultRow() </p>
	 *
	 * <p> Description: Creates one report row containing the student's
	 * username and their final PASS or FAIL result. </p>
	 *
	 * @param student the student represented by the row
	 * @param passed whether the student satisfied every parameter
	 * @return an HBox containing the student report result
	 */
	private static HBox createStudentResultRow(
			User student,
			boolean passed) {

		HBox rBox = new HBox(10);
		rBox.setPadding(new Insets(10));

		rBox.setStyle(
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 3;" +
			"-fx-background-radius: 3;"
		);

		Label studentLabel =
				new Label(
					student.getUserName()
				);

		studentLabel.setStyle(
			"-fx-font-weight: bold;"
		);

		Region spacer = new Region();

		HBox.setHgrow(
			spacer,
			Priority.ALWAYS
		);

		Label resultLabel =
				new Label(
					passed ? "PASS" : "FAIL"
				);

		if (passed) {

			resultLabel.setStyle(
				"-fx-text-fill: green;" +
				"-fx-font-weight: bold;"
			);
		}
		else {

			resultLabel.setStyle(
				"-fx-text-fill: red;" +
				"-fx-font-weight: bold;"
			);
		}

		rBox.getChildren().addAll(
			studentLabel,
			spacer,
			resultLabel
		);

		return rBox;
	}
}