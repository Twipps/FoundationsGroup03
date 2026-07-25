	package guiComponents.staffHome;

	import java.util.ArrayList;

	import entityClasses.Thread;
	import entityClasses.ThreadList;
	import javafx.geometry.Insets;
	import javafx.scene.control.Button;
	import javafx.scene.control.Label;
	import javafx.scene.control.ScrollPane;
	import javafx.scene.control.TextField;
	import javafx.scene.layout.BorderPane;
	import javafx.scene.layout.Priority;
	import javafx.scene.layout.VBox;
	import javafx.stage.Stage;

	/*******
	 * <p> Title: StaffThreadNavBar Class </p>
	 *
	 * <p> Description: A static utility class that builds the navigation
	 * panel for staff thread management. Displays a Create Thread button, a keyword
	 * search bar, a manual refresh button, and a scrollable list of all active
	 * discussion threads. </p>
	 *
	 * @author James Suchovic (Team 3)
	 *
	 * @version 1.00  2026-07-25  Initial implementation for TP3
	 */
	public class StaffThreadNavBar {

		private static ThreadList allThreads = new ThreadList();

		// Prevents creation of StaffThreadNavBar objects.
		private StaffThreadNavBar() {}

		/*******
		 * <p> Method: createThreadNavBar() </p>
		 *
		 * <p> Description: Builds and returns the complete thread navigation panel.
		 * The panel contains a Create Thread button, Refresh button, search bar,
		 * and scrollable list of active threads. </p>
		 *
		 * @param theStage    the primary application stage
		 * @param contentPane the main content pane used by the staff interface
		 * @return a VBox containing the thread navigation controls
		 */
		public static VBox createThreadNavBar(
				Stage theStage,
				BorderPane contentPane) {

			VBox rBox = new VBox(10);
			rBox.setPadding(new Insets(10));
			rBox.setPrefWidth(300);
			rBox.setMaxWidth(300);

			Button createThread = new Button("Create Thread");
			createThread.setMaxWidth(Double.MAX_VALUE);

			createThread.setOnAction(e -> {
				contentPane.setCenter(
					StaffThreadCreationPanel.createThreadCreationPanel(
						theStage,
						contentPane
					)
				);
			});

			Button refresh = new Button("Refresh");
			refresh.setMaxWidth(Double.MAX_VALUE);

			TextField searchBar = new TextField();
			searchBar.setPromptText("Search threads...");

			VBox threadDisplayList = new VBox(8);

			ScrollPane scrollPane = new ScrollPane(threadDisplayList);
			scrollPane.setFitToWidth(true);
			VBox.setVgrow(scrollPane, Priority.ALWAYS);

			filterThreads(
				threadDisplayList,
				searchBar.getText(),
				contentPane,
				theStage
			);

			refresh.setOnAction(e -> {
				filterThreads(
					threadDisplayList,
					searchBar.getText(),
					contentPane,
					theStage
				);
			});

			searchBar.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					filterThreads(
						threadDisplayList,
						newValue,
						contentPane,
						theStage
					);
				}
			);

			rBox.getChildren().addAll(
				createThread,
				refresh,
				searchBar,
				scrollPane
			);

			return rBox;
		}

		/*******
		 * <p> Method: filterThreads() </p>
		 *
		 * <p> Description: Clears and rebuilds the displayed thread list using the
		 * provided search text. A thread is shown when its title, body, author, or
		 * category contains the search text. </p>
		 *
		 * <p> Deleted threads are not expected to appear because
		 * Database.getAllThreads() should only return active threads. An additional
		 * isDeleted check is included as a defensive measure. </p>
		 *
		 * @param threadDisplayList the VBox containing the displayed thread rows
		 * @param search            the current search query
		 * @param contentPane       the main staff content pane
		 * @param theStage          the primary application stage
		 */
		public static void filterThreads(
				VBox threadDisplayList,
				String search,
				BorderPane contentPane,
				Stage theStage) {

			threadDisplayList.getChildren().clear();

			 // Reload from the database so newly created, renamed, or deleted
			 // threads are reflected immediately.

			allThreads.refreshList();

			ArrayList<Thread> threads = allThreads.getThreadList();

			if (threads == null) {
				threadDisplayList.getChildren().add(
					new Label("Unable to load threads.")
				);
				return;
			}

			for (int i = 0; i < threads.size(); i++) {
				Thread currentThread = threads.get(i);

				if (currentThread == null || currentThread.isDeleted()) {
					continue;
				}

				if (matchesSearch(currentThread, search)) {
					threadDisplayList.getChildren().add(
						createThreadRow(
							currentThread,
							contentPane,
							theStage
						)
					);
				}
			}

			if (threadDisplayList.getChildren().isEmpty()) {
				threadDisplayList.getChildren().add(
					new Label("No threads found.")
				);
			}
		}

		/*******
		 * <p> Method: matchesSearch() </p>
		 *
		 * <p> Description: Determines whether a thread matches the supplied search
		 * query. The title, body, author, and category fields are searched without
		 * case sensitivity. </p>
		 *
		 * @param thread the thread being examined
		 * @param search the search query
		 * @return true if the thread matches or the search is blank
		 */
		public static boolean matchesSearch(Thread thread, String search) {

			if (thread == null) {
				return false;
			}

			if (search == null || search.isBlank()) {
				return true;
			}

			String searchLower = search.trim().toLowerCase();

			return safeLower(thread.getTitle()).contains(searchLower)
				|| safeLower(thread.getBody()).contains(searchLower)
				|| safeLower(thread.getAuthor()).contains(searchLower)
				|| safeLower(thread.getCategory()).contains(searchLower);
		}

		/*******
		 * <p> Method: createThreadRow() </p>
		 *
		 * <p> Description: Builds a selectable navigation row representing one
		 * discussion thread. The row displays the thread title, optional category,
		 * description preview, and author. Clicking the row opens the thread in
		 * StaffThreadDisplayPanel. </p>
		 *
		 * @param thread     the thread represented by the row
		 * @param contentPane the main staff content pane
		 * @param theStage    the primary application stage
		 * @return a VBox representing the thread
		 */
		public static VBox createThreadRow(
				Thread thread,
				BorderPane contentPane,
				Stage theStage) {

			VBox rBox = new VBox(5);
			rBox.setPadding(new Insets(8));
			rBox.setStyle(
				"-fx-border-color: lightgray;" +
				"-fx-border-radius: 3;" +
				"-fx-background-radius: 3;"
			);

			Label titleLabel = new Label(thread.getTitle());
			titleLabel.setStyle("-fx-font-weight: bold;");

			if (thread.isGeneral()) {
				titleLabel.setText(thread.getTitle() + " [Default]");
			}

			Label categoryLabel = new Label();

			if (thread.getCategory() == null
					|| thread.getCategory().isBlank()) {

				categoryLabel.setText("No category");
			}
			else {
				categoryLabel.setText("[" + thread.getCategory() + "]");
			}

			categoryLabel.setStyle(
				"-fx-text-fill: gray;" +
				"-fx-font-size: 11px;"
			);

			Label bodyPreview = new Label(makeSample(thread.getBody()));
			bodyPreview.setWrapText(true);

			Label authorLabel = new Label(
				"Created by: " + safeDisplay(thread.getAuthor())
			);

			authorLabel.setStyle(
				"-fx-text-fill: gray;" +
				"-fx-font-size: 11px;"
			);

			rBox.setOnMouseClicked(e -> {
				contentPane.setCenter(
					StaffThreadDisplayPanel.createThreadDisplayPanel(
						theStage,
						contentPane,
						thread.getThreadID()
					)
				);
			});

			rBox.setOnMouseEntered(e -> {
				rBox.setStyle(
					"-fx-border-color: gray;" +
					"-fx-border-radius: 3;" +
					"-fx-background-radius: 3;" +
					"-fx-background-color: #f2f2f2;"
				);
			});

			rBox.setOnMouseExited(e -> {
				rBox.setStyle(
					"-fx-border-color: lightgray;" +
					"-fx-border-radius: 3;" +
					"-fx-background-radius: 3;"
				);
			});

			rBox.getChildren().addAll(
				titleLabel,
				categoryLabel,
				bodyPreview,
				authorLabel
			);

			return rBox;
		}

		/*******
		 * <p> Method: makeSample() </p>
		 *
		 * <p> Description: Creates a shortened preview of the thread description.
		 * Descriptions longer than 80 characters are truncated. </p>
		 *
		 * @param body the thread description
		 * @return a shortened description suitable for the navigation list
		 */
		public static String makeSample(String body) {

			if (body == null || body.isBlank()) {
				return "No description.";
			}

			if (body.length() <= 80) {
				return body;
			}

			return body.substring(0, 80) + "...";
		}

		/*******
		 * <p> Method: safeLower() </p>
		 *
		 * <p> Description: Safely converts a string to lowercase. Null values
		 * produce an empty string. </p>
		 *
		 * @param value the string to convert
		 * @return the lowercase string, or an empty string for null
		 */
		public static String safeLower(String value) {

			if (value == null) {
				return "";
			}

			return value.toLowerCase();
		}

		/*******
		 * <p> Method: safeDisplay() </p>
		 *
		 * <p> Description: Returns a display safe version of a string. Null or blank
		 * values are displayed as "Unknown". </p>
		 *
		 * @param value the value being displayed
		 * @return the original value or "Unknown"
		 */
		private static String safeDisplay(String value) {

			if (value == null || value.isBlank()) {
				return "Unknown";
			}

			return value;
		}
}