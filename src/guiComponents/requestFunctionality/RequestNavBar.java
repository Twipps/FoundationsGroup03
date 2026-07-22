package guiComponents.requestFunctionality;

import java.util.ArrayList;

import entityClasses.Request;
import entityClasses.RequestList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*******
 * <p> Title: RequestNavBar Class </p>
 *
 * <p> Description: A static utility class that builds the left-side navigation
 * panel for the Staff "Admin Action Request" System. Provides a Create Request button,
 * a keyword search bar, and a scrollable list of
 * all requests that match the current search and filter criteria. </p>
 *
 * <p> This class satisfies the following Staff User Stories: </p>
 * <p> - "As a staff member, I can request admins to perform admin-specific
 * actions that appear in a list that staff and admins can see." — the display 
 * panel shows the full request. </p>
 * <p> - "As a staff, I can delete my own request" — each request row
 * has a Delete button. </p>
 * <p> - Input validation — the Add Reply area rejects empty body text
 * and shows an error message. </p>
 *
 * <p> This class follows the View pattern from the Foundations MVC structure.
 * All database operations are delegated to RequestList and Database. </p>
 *
 * @author James Suchovic (Team 3) — Designed and implemented post navigation,
 * search, filtering, and post list display
 * @author Rob Taylor (Team 3) - modified to accommodate requests
 *
 * @version 1.00  2026-07-22  Initial implementation
 * 
 */
public class RequestNavBar {

	/** Cached request list — refreshed on each filterRequests() call to reflect
	 *  the latest database state. */
	private static RequestList allRequests = new RequestList();

	/**
	 * Prevents creation of RequestNavBar objects because this class only
	 * provides static GUI helper methods.
	 */
	private RequestNavBar() {}

	/*******
	 * <p> Method: createRequestNavBar() </p>
	 *
	 * <p> Description: Builds and returns the full request navigation bar VBox.
	 * Contains a Create Request button, search bar, and a
	 * scrollable list of requests filtered by the current search and category. </p>
	 *
	 * @param theStage    the primary application stage
	 * @param contentPane the main content pane used by the request interface
	 * @return a VBox containing the request navigation controls
	 */
	public static VBox createRequestNavBar(Stage theStage, BorderPane contentPane) {
		VBox rBox = new VBox(10);

		Button createRequest = new Button("Create Request");
		createRequest.setMaxWidth(Double.MAX_VALUE);

		createRequest.setOnAction(e -> {
			// requestID=-1 signals RequestEditPanel to create a new request
			contentPane.setCenter(RequestEditPanel.createRequestEditPanel(theStage, contentPane, -1));
		});

		HBox searchStuff = new HBox(10);

		// Search bar filters request list in real time by keyword
		TextField searchBar = new TextField();
		searchBar.setPromptText("Search requests...");

		// Category filter dropdown — "All" shows all categories
		ComboBox<String> statusFilter = new ComboBox<String>();
		statusFilter.getItems().add("All");
		statusFilter.getItems().add("Open");
		statusFilter.getItems().add("ReOpened");
		statusFilter.getItems().add("Closed");
		statusFilter.setValue("All"); // default to showing all categories

		searchStuff.getChildren().addAll(searchBar, statusFilter);

		VBox requestList = new VBox(8);
		ScrollPane scrollPane = new ScrollPane(requestList);
		scrollPane.setFitToWidth(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		// Initial population of the request list with no filter applied
		filterRequests(requestList, searchBar.getText(), statusFilter.getValue(), contentPane, theStage);

		// Update request list in real time as the user types in the search bar
		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			filterRequests(requestList, newValue, statusFilter.getValue(), contentPane, theStage);
		});

		// REQ-12: Update request list when the category filter selection changes
		statusFilter.setOnAction(e -> {
			filterRequests(requestList, searchBar.getText(), statusFilter.getValue(), contentPane, theStage);
		});

		rBox.getChildren().addAll(createRequest, searchStuff, scrollPane);
		rBox.setMaxWidth(300);

		return rBox;
	}

	/*******
	 * <p> Method: filterRequests() </p>
	 *
	 * <p> Description: Refreshes the request list VBox with all requests that match
	 * both the keyword search and the selected category filter. Clears the
	 * existing list and repopulates it on each call. Shows "No requests found."
	 * if no requests match the current criteria. </p>
	 *
	 * @param requestList the VBox that contains the displayed request rows
	 * @param search      the search keyword — blank means show all requests
	 * @param category    the selected category — "All" means show all categories
	 * @param contentPane the main content pane used by the request interface
	 * @param theStage    the primary application stage
	 */
	public static void filterRequests(VBox requestList, String search, String category,
			BorderPane contentPane, Stage theStage) {
		requestList.getChildren().clear();

		// Refresh from database to catch any new requests added in this session
		allRequests.refreshList();

		ArrayList<Request> requests = allRequests.getRequestList();

		for (int i = 0; i < requests.size(); i++) {
			Request currentRequest = requests.get(i);

			// Only show request if it matches both search AND category filter
			if (matchesSearch(currentRequest, search) && matchesCategory(currentRequest, category)) {
				requestList.getChildren().add(createRequestRow(currentRequest, contentPane, theStage));
			}
		}

		// Show feedback message if no requests match the current filter
		if (requestList.getChildren().size() == 0) {
			requestList.getChildren().add(new Label("No requests found."));
		}
	}

	/*******
	 * <p> Method: matchesSearch() </p>
	 *
	 * <p> Description: Returns true if the given request's title, body, or category
	 * contains the search keyword (case-insensitive). Returns true for a blank
	 * or null search query, meaning all requests are shown when the search bar is
	 * empty. </p>
	 *
	 * @param request   the request being checked
	 * @param search 	the search keyword (may be null or blank)
	 * @return true if the request matches the search, false otherwise
	 */
	public static boolean matchesSearch(Request request, String search) {
		// blank search shows all requests
		if (search == null || search.isBlank()) {
			return true;
		}

		String searchLower = search.toLowerCase();

		// search matches title, body, or category (case-insensitive)
		return safeLower(request.getTitle()).contains(searchLower)
			|| safeLower(request.getBody()).contains(searchLower)
			|| safeLower(request.getStatus()).contains(searchLower);
	}

	/*******
	 * <p> Method: matchesCategory() </p>
	 *
	 * <p> Description: Returns true if the request's category matches the selected
	 * filter (case-insensitive). Returns true when the filter is null or "All",
	 * meaning all requests are shown when no specific category is selected. </p>
	 *
	 * @param request   the request being checked
	 * @param category 	the selected category filter (may be null or "All")
	 * @return true if the request matches the filter, false otherwise
	 */
	public static boolean matchesCategory(Request request, String status) {
		// "All" filter shows every request regardless of category
		if (status == null || status.compareTo("All") == 0) {
			return true;
		}

		// case-insensitive category match
		return safeLower(request.getStatus()).compareTo(status.toLowerCase()) == 0;
	}

	/*******
	 * <p> Method: createRequestRow() </p>
	 *
	 * <p> Description: Builds and returns a clickable row for a single request
	 * in the navigation list. Shows the request title, category tag, and a
	 * truncated body preview. Clicking the row loads the full request in
	 * RequestDisplayPanel. </p>
	 *
	 * @param request     the request represented by the row
	 * @param contentPane the main content pane used by the request interface
	 * @param theStage    the primary application stage
	 * @return a VBox containing the request title, category tag, and body preview
	 */
	public static VBox createRequestRow(Request request, BorderPane contentPane, Stage theStage) {
		VBox rBox = new VBox(5);
		rBox.setPadding(new Insets(8));
		rBox.setStyle("-fx-border-color: lightgray;");

		HBox titleRow = new HBox(10);
		Label title = new Label(request.getTitle());
		Label status = new Label("[" + request.getStatus() + "]");

		titleRow.getChildren().addAll(title, status);

		// Show a truncated preview of the request body (max 80 chars)
		Label sampleString = new Label(makeSample(request.getBody()));
		sampleString.setWrapText(true);

		// Clicking the row loads the full request in RequestDisplayPanel
		rBox.setOnMouseClicked(e -> {
			contentPane.setCenter(RequestDisplayPanel.createRequestDisplayPanel(
				theStage, contentPane, request.getRequestID()
			));
		});

		rBox.getChildren().addAll(titleRow, sampleString);
		return rBox;
	}

	/*******
	 * <p> Method: makeSample() </p>
	 *
	 * <p> Description: Returns a preview of the request body for display in the
	 * request list. Returns the full body if 80 characters or fewer, otherwise
	 * truncates to 80 characters and appends "...". Returns an empty string
	 * for a null body. </p>
	 *
	 * @param body the request body string (may be null)
	 * @return a preview string of at most 83 characters
	 */
	public static String makeSample(String body) {
		if (body == null) {
			return ""; // handle null body gracefully
		}

		if (body.length() <= 80) {
			return body;
		}

		return body.substring(0, 80) + "...";
	}

	/*******
	 * <p> Method: safeLower() </p>
	 *
	 * <p> Description: Returns the lowercase version of the given string, or
	 * an empty string if the input is null. Prevents NullPointerExceptions
	 * during search and category comparisons. </p>
	 *
	 * @param value the string to convert (may be null)
	 * @return lowercase string, or "" if null
	 */
	public static String safeLower(String value) {
		if (value == null) {
			return ""; // REQ-09: handle null values without crashing
		}

		return value.toLowerCase();
	}
}