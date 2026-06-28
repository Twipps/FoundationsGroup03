package CustomGuiComponents;

import java.util.ArrayList;

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
import postComponents.Post;
import postComponents.PostList;

public class PostNavBar {
	
	private static PostList allPosts = new PostList();
	
	// needs button to display edit panel
	// needs text field for seaching
	// needs vbox of selectable hbox's showing the posts you can navigate
	
	public static VBox createPostNavBar(Stage theStage, BorderPane contentPane) {
		VBox rBox = new VBox(10); // contains scroll list and search bar
		
		Button createPost = new Button("Create Post");
		createPost.setMaxWidth(Double.MAX_VALUE);
		
		createPost.setOnAction(e -> {
			contentPane.setCenter(PostReplyEditPanel.createPostEditPanel(theStage, contentPane, -1));
		});
		
		HBox searchStuff = new HBox(10);
		TextField searchBar = new TextField();
		ComboBox<String> categoryFilter = new ComboBox<String>();
		
		searchBar.setPromptText("Search posts...");
		
		categoryFilter.getItems().add("All"); // place holders
		categoryFilter.getItems().add("General");
		categoryFilter.getItems().add("Question");
		categoryFilter.getItems().add("Bug");
		categoryFilter.getItems().add("Help");
		categoryFilter.setValue("All");
		
		searchStuff.getChildren().addAll(searchBar, categoryFilter);
		
		VBox postList = new VBox(8); // to go inside scrollpane
		ScrollPane scrollPane = new ScrollPane(postList);
		scrollPane.setFitToWidth(true);
		
		scrollPane.setFitToWidth(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		
		filterPosts(postList, searchBar.getText(), categoryFilter.getValue(), contentPane, theStage);
		
		searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			filterPosts(postList, newValue, categoryFilter.getValue(), contentPane, theStage);
		});
		
		categoryFilter.setOnAction(e -> {
			filterPosts(postList, searchBar.getText(), categoryFilter.getValue(), contentPane, theStage);
		});
		
		rBox.getChildren().addAll(createPost, searchStuff, scrollPane);
		
		rBox.setMaxWidth(300);
	
		return rBox;
	}
	
	public static void filterPosts(VBox postList, String search, String category, BorderPane contentPane, Stage theStage) {
		postList.getChildren().clear();
		allPosts.refreshList();
		
		ArrayList<Post> posts = allPosts.getPostList();
		
		for (int i = 0; i < posts.size(); i++) {
			Post currentPost = posts.get(i);
			
			if (matchesSearch(currentPost, search) && matchesCategory(currentPost, category)) {
				postList.getChildren().add(createPostRow(currentPost, contentPane, theStage));
			}
		}
		
		if (postList.getChildren().size() == 0) {
			postList.getChildren().add(new Label("No posts found."));
		}
	}
	
	public static boolean matchesSearch(Post post, String search) {
		if (search == null || search.isBlank()) {
			return true;
		}
		
		String searchLower = search.toLowerCase();
		
		return safeLower(post.getTitle()).contains(searchLower) || safeLower(post.getBody()).contains(searchLower)
				|| safeLower(post.getCategory()).contains(searchLower);
	}
	
	public static boolean matchesCategory(Post post, String category) {
		if (category == null || category.compareTo("All") == 0) {
			return true;
		}
		
		return safeLower(post.getCategory()).compareTo(category.toLowerCase()) == 0;
	}
	
	public static VBox createPostRow(Post post, BorderPane contentPane, Stage theStage) {
		VBox rBox = new VBox(5);
		rBox.setPadding(new Insets(8));
		rBox.setStyle("-fx-border-color: lightgray;");
		
		HBox titleRow = new HBox(10);
		Label title = new Label(post.getTitle());
		Label category = new Label("[" + post.getCategory() + "]");
		
		titleRow.getChildren().addAll(title, category);
		
		// string of about 10 characters branched from a found sequence
		Label sampleString = new Label(makeSample(post.getBody()));  
		sampleString.setWrapText(true);
		
		rBox.setOnMouseClicked(e -> {
			contentPane.setCenter(PostDisplayPanel.createPostDisplayPanel(
				theStage, contentPane, post.getPostID()
			));
		});
		
		rBox.getChildren().addAll(titleRow, sampleString);
		
		return rBox;
	}
	
	public static String makeSample(String body) {
		if (body == null) {
			return "";
		}
		
		if (body.length() <= 80) {
			return body;
		}
		
		return body.substring(0, 80) + "...";
	}
	
	public static String safeLower(String value) {
		if (value == null) {
			return "";
		}
		
		return value.toLowerCase();
	}
}