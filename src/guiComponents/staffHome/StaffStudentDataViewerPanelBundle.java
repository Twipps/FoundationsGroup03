package guiComponents.staffHome;

import java.util.ArrayList;
import java.util.List;

import applicationMain.FoundationsMain;
import entityClasses.Post;
import entityClasses.User;
import guiComponents.postFunctionality.PostDisplayPanel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p>Title: StaffStudentDataViewerPanelBundle Class</p>
 *
 * <p>Description: Creates a read-only student activity viewer for staff.
 * The left side allows staff to select a student and view that student's
 * posts. The right side displays students who have replied to at least
 * three distinct posts, along with their post and reply counts.</p>
 *
 * <p>This panel does not modify student data.</p>
 *
 * @author James Suchovic (Team 03)
 *
 * @version 1.00 2026-07-25 Initial implementation
 */
public class StaffStudentDataViewerPanelBundle {

	/**
	 * Prevents creation of StaffStudentDataViewerPanelBundle objects.
	 */
	private StaffStudentDataViewerPanelBundle() {}

	/*******
	 * <p>Method: createStudentDataViewerPanel()</p>
	 *
	 * <p>Description: Creates the complete student data viewer. The left
	 * column contains a student selection ComboBox and that student's posts.
	 * The right column contains read-only student activity statistics.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the main application content pane
	 * @return an HBox containing both student data columns
	 */
	public static HBox createStudentDataViewerPanel(
			Stage theStage,
			BorderPane contentPane) {

		HBox rBox = new HBox(20);
		rBox.setPadding(new Insets(20));

		VBox studentPostColumn = createStudentPostColumn(
			theStage,
			contentPane
		);

		VBox activityColumn = createActivityColumn();

		HBox.setHgrow(studentPostColumn, Priority.ALWAYS);
		HBox.setHgrow(activityColumn, Priority.ALWAYS);

		studentPostColumn.setMaxWidth(Double.MAX_VALUE);
		activityColumn.setMaxWidth(Double.MAX_VALUE);

		rBox.getChildren().addAll(
			studentPostColumn,
			activityColumn
		);

		return rBox;
	}

	/*******
	 * <p>Method: createStudentPostColumn()</p>
	 *
	 * <p>Description: Creates the left column containing a student selector,
	 * load button, and scrollable post list.</p>
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the main application content pane
	 * @return a VBox containing the student post viewer
	 */
	private static VBox createStudentPostColumn(
			Stage theStage,
			BorderPane contentPane) {

		VBox rBox = createColumnContainer();

		Label title = createTitle("Student Posts");

		Label instructions = new Label(
			"Select a student to view their posts."
		);

		ComboBox<User> studentSelection = new ComboBox<>();
		studentSelection.setPromptText("Select a student");
		studentSelection.setMaxWidth(Double.MAX_VALUE);

		List<User> students =
			FoundationsMain.database.getAllStudentUsers();

		studentSelection.getItems().addAll(students);

		studentSelection.setCellFactory(listView ->
			new ListCell<User>() {

				@Override
				protected void updateItem(
						User user,
						boolean empty) {

					super.updateItem(user, empty);

					if (empty || user == null) {
						setText(null);
					} else {
						setText(makeUserDisplay(user));
					}
				}
			}
		);

		studentSelection.setButtonCell(
			new ListCell<User>() {

				@Override
				protected void updateItem(
						User user,
						boolean empty) {

					super.updateItem(user, empty);

					if (empty || user == null) {
						setText(null);
					} else {
						setText(makeUserDisplay(user));
					}
				}
			}
		);

		Button loadButton = new Button("View Posts");
		loadButton.setMaxWidth(Double.MAX_VALUE);

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");

		VBox postList = new VBox(10);
		postList.setPadding(new Insets(5));

		postList.getChildren().add(
			new Label("No student selected.")
		);

		ScrollPane postScrollPane = new ScrollPane(postList);
		postScrollPane.setFitToWidth(true);

		postScrollPane.setHbarPolicy(
			ScrollPane.ScrollBarPolicy.NEVER
		);

		postScrollPane.setVbarPolicy(
			ScrollPane.ScrollBarPolicy.AS_NEEDED
		);

		VBox.setVgrow(postScrollPane, Priority.ALWAYS);

		loadButton.setOnAction(e -> {

			User selectedStudent =
				studentSelection.getValue();

			if (selectedStudent == null) {
				errorLabel.setText(
					"Please select a student."
				);

				return;
			}

			errorLabel.setText("");

			loadStudentPosts(
				postList,
				selectedStudent,
				theStage,
				contentPane
			);
		});

		rBox.getChildren().addAll(
			title,
			instructions,
			studentSelection,
			loadButton,
			errorLabel,
			new Separator(),
			postScrollPane
		);

		return rBox;
	}

	/*******
	 * <p>Method: loadStudentPosts()</p>
	 *
	 * <p>Description: Loads every active post authored by the selected
	 * student into the provided VBox.</p>
	 *
	 * @param postList the VBox receiving post rows
	 * @param student the selected student
	 * @param theStage the primary application stage
	 * @param contentPane the main application content pane
	 */
	private static void loadStudentPosts(
			VBox postList,
			User student,
			Stage theStage,
			BorderPane contentPane) {

		postList.getChildren().clear();

		ArrayList<Post> posts =
			FoundationsMain.database.getPostsForUser(
				student.getUserName()
			);

		Label studentTitle = new Label(
			"Posts by " + student.getUserName()
		);

		studentTitle.setStyle(
			"-fx-font-size: 16px;" +
			"-fx-font-weight: bold;"
		);

		postList.getChildren().add(studentTitle);

		if (posts == null || posts.isEmpty()) {
			postList.getChildren().add(
				new Label(
					"This student has not created any posts."
				)
			);

			return;
		}

		for (Post post : posts) {

			if (post == null) {
				continue;
			}

			postList.getChildren().add(
				createPostRow(
					post,
					theStage,
					contentPane
				)
			);
		}
	}

	/*******
	 * <p>Method: createPostRow()</p>
	 *
	 * <p>Description: Creates one selectable read-only post preview.
	 * Selecting the row opens the complete post.</p>
	 *
	 * @param post the post represented by this row
	 * @param theStage the primary application stage
	 * @param contentPane the main application content pane
	 * @return a VBox containing the post preview
	 */
	private static VBox createPostRow(
			Post post,
			Stage theStage,
			BorderPane contentPane) {

		VBox rBox = new VBox(6);
		rBox.setPadding(new Insets(10));

		String normalStyle =
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;";

		String hoverStyle =
			"-fx-border-color: gray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;" +
			"-fx-background-color: #f2f2f2;";

		rBox.setStyle(normalStyle);

		Label title = new Label(
			safeDisplay(post.getTitle(), "Untitled Post")
		);

		title.setStyle("-fx-font-weight: bold;");

		HBox informationRow = new HBox(10);

		Label category = new Label(
			"Category: " +
			safeDisplay(post.getCategory(), "General")
		);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label created = new Label(
			"Created: " + post.getCreatedDate()
		);

		informationRow.getChildren().addAll(
			category,
			spacer,
			created
		);

		Label body = new Label(
			makeSample(post.getBody())
		);

		body.setWrapText(true);

		rBox.setOnMouseClicked(e -> {
			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(
					theStage,
					contentPane,
					post.getPostID()
				)
			);
		});

		rBox.setOnMouseEntered(e ->
			rBox.setStyle(hoverStyle)
		);

		rBox.setOnMouseExited(e ->
			rBox.setStyle(normalStyle)
		);

		rBox.getChildren().addAll(
			title,
			informationRow,
			body
		);

		return rBox;
	}

	/*******
	 * <p>Method: createActivityColumn()</p>
	 *
	 * <p>Description: Creates the right-side read-only activity list. Only
	 * students who have submitted replies on at least three distinct posts
	 * are displayed.</p>
	 *
	 * @return a VBox containing qualifying student statistics
	 */
	private static VBox createActivityColumn() {

		VBox rBox = createColumnContainer();

		Label title = createTitle(
			"Students Active Across Multiple Posts"
		);

		HBox headerRow = createStatisticsHeader();

		VBox studentRows = new VBox(5);

		ScrollPane scrollPane = new ScrollPane(studentRows);
		scrollPane.setFitToWidth(true);

		scrollPane.setHbarPolicy(
			ScrollPane.ScrollBarPolicy.NEVER
		);

		scrollPane.setVbarPolicy(
			ScrollPane.ScrollBarPolicy.AS_NEEDED
		);

		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		List<User> qualifyingStudents =
			FoundationsMain.database
				.getStudentsWithRepliesOnAtLeastThreeDistinctPosts();

		if (qualifyingStudents == null
				|| qualifyingStudents.isEmpty()) {

			studentRows.getChildren().add(
				new Label(
					"No students currently meet this requirement."
				)
			);
		} else {

			for (User student : qualifyingStudents) {

				if (student == null) {
					continue;
				}

				studentRows.getChildren().add(
					createStudentStatisticsRow(student)
				);
			}
		}

		rBox.getChildren().addAll(
			title,
			new Separator(),
			headerRow,
			scrollPane
		);

		return rBox;
	}

	/**
	 * Creates the column labels for the student statistics list.
	 *
	 * @return an HBox containing column labels
	 */
	private static HBox createStatisticsHeader() {

		HBox rBox = new HBox(10);
		rBox.setPadding(new Insets(5));

		Label studentLabel = new Label("Student");
		Label postCountLabel = new Label("Posts");
		Label replyCountLabel = new Label("Replies");

		studentLabel.setStyle("-fx-font-weight: bold;");
		postCountLabel.setStyle("-fx-font-weight: bold;");
		replyCountLabel.setStyle("-fx-font-weight: bold;");

		studentLabel.setPrefWidth(220);
		postCountLabel.setPrefWidth(70);
		replyCountLabel.setPrefWidth(70);

		rBox.getChildren().addAll(
			studentLabel,
			postCountLabel,
			replyCountLabel
		);

		return rBox;
	}

	/**
	 * Creates a read-only row showing one student's username, post count,
	 * and reply count.
	 *
	 * @param student the student represented by the row
	 * @return an HBox containing the student's statistics
	 */
	private static HBox createStudentStatisticsRow(
			User student) {

		HBox rBox = new HBox(10);
		rBox.setPadding(new Insets(8));

		rBox.setStyle(
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 4;"
		);

		String username = student.getUserName();

		int postCount =
			FoundationsMain.database
				.getPostCountForUser(username);

		int replyCount =
			FoundationsMain.database
				.getReplyCountForUser(username);

		Label studentLabel = new Label(
			makeUserDisplay(student)
		);

		Label postCountLabel = new Label(
			String.valueOf(postCount)
		);

		Label replyCountLabel = new Label(
			String.valueOf(replyCount)
		);

		studentLabel.setPrefWidth(220);
		postCountLabel.setPrefWidth(70);
		replyCountLabel.setPrefWidth(70);

		rBox.getChildren().addAll(
			studentLabel,
			postCountLabel,
			replyCountLabel
		);

		return rBox;
	}

	/**
	 * Creates the shared container used by both columns.
	 *
	 * @return a formatted VBox
	 */
	private static VBox createColumnContainer() {

		VBox rBox = new VBox(10);
		rBox.setPadding(new Insets(15));

		rBox.setStyle(
			"-fx-border-color: lightgray;" +
			"-fx-border-radius: 5;" +
			"-fx-background-radius: 5;"
		);

		rBox.setPrefWidth(500);
		rBox.setFillWidth(true);

		return rBox;
	}

	/**
	 * Creates a formatted section title.
	 *
	 * @param text the title text
	 * @return the formatted title label
	 */
	private static Label createTitle(String text) {

		Label title = new Label(text);

		title.setStyle(
			"-fx-font-size: 20px;" +
			"-fx-font-weight: bold;"
		);

		return title;
	}

	/**
	 * Creates the text shown for a user in the ComboBox and statistics list.
	 *
	 * @param user the user being displayed
	 * @return a readable user description
	 */
	private static String makeUserDisplay(User user) {

		if (user == null) {
			return "";
		}

		String fullName =
			safeDisplay(user.getFirstName(), "") +
			" " +
			safeDisplay(user.getLastName(), "");

		fullName = fullName.trim();

		if (fullName.isBlank()) {
			return user.getUserName();
		}

		return user.getUserName() + " - " + fullName;
	}

	/**
	 * Creates an abbreviated post body preview.
	 *
	 * @param body the full post body
	 * @return the abbreviated preview
	 */
	private static String makeSample(String body) {

		if (body == null || body.isBlank()) {
			return "No content.";
		}

		String trimmedBody = body.trim();

		if (trimmedBody.length() <= 120) {
			return trimmedBody;
		}

		return trimmedBody.substring(0, 120) + "...";
	}

	/**
	 * Safely returns a display value.
	 *
	 * @param value the original value
	 * @param fallback the fallback value
	 * @return the original value or fallback
	 */
	private static String safeDisplay(
			String value,
			String fallback) {

		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value;
	}
}