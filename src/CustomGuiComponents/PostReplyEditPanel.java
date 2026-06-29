package CustomGuiComponents;

import database.Database;
import entityClasses.Post;
import entityClasses.PostList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>Title: PostReplyEditPanel Class</p>
 *
 * <p>Description: Class that creates the post editing panel used by the student
 * discussion GUI. Provides controls for creating a new post or editing an
 * existing post.</p>
 *
 * @author James Suchovic (Team 03)
 */

public class PostReplyEditPanel {
	
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**
	 * Prevents creation of PostReplyEditPanel objects.
	 */
	private PostReplyEditPanel() {
	}
	
	/**
	 * Creates the post editing panel.
	 *
	 * @param theStage the primary application stage
	 * @param contentPane the main content pane used by the post interface
	 * @param postID the ID of the post to edit, or -1 when creating a new post
	 * @return a VBox containing the post editing controls
	 */
	public static VBox createPostEditPanel(Stage theStage, BorderPane contentPane, int postID) {
		VBox rBox = new VBox(10);
		
		Post currentPost = null;
		
		if (postID != -1) {
			PostList posts = new PostList();
			currentPost = posts.getPost(postID);
		}
		
		HBox titleStuff = new HBox(10);
		Label title = new Label("Title: ");
		TextField titleInput = new TextField(); 
		
		HBox categoryStuff = new HBox(10);
		Label category = new Label("Category: ");
		ComboBox<String> selection = new ComboBox<String>();
		
		selection.getItems().add("General");
		selection.getItems().add("Question");
		selection.getItems().add("Bug");
		selection.getItems().add("Help");
		selection.setPromptText("Select category");
		
		VBox bodyStuff = new VBox(5);
		Label body = new Label("Body: ");
		TextArea bodyInput = new TextArea();
		bodyInput.setWrapText(true);
		bodyInput.setPrefRowCount(10);
		
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red;");
		
		Button post = new Button("Post");
		
		if (currentPost != null) {
			titleInput.setText(currentPost.getTitle());
			bodyInput.setText(currentPost.getBody());
			selection.setValue(currentPost.getCategory());
			post.setText("Save Changes");
		}
		
		titleStuff.getChildren().addAll(title, titleInput);
		categoryStuff.getChildren().addAll(category, selection);
		bodyStuff.getChildren().addAll(body, bodyInput);
		
		final Post finalPost = currentPost;
		
		post.setOnAction(e -> {
			String inTitle = titleInput.getText();
			String inBody = bodyInput.getText();
			String inCategory = selection.getValue();
			String inAuthor = applicationMain.FoundationsMain.database.getCurrentUsername();
			
			if (inTitle == null || inTitle.isBlank()) {
				errorLabel.setText("Title cannot be empty.");
				return;
			}
			
			if (inBody == null || inBody.isBlank()) {
				errorLabel.setText("Body cannot be empty.");
				return;
			}
			
			if (inCategory == null || inCategory.isBlank()) {
				errorLabel.setText("Category must be selected.");
				return;
			}
			
			if (finalPost == null) {
				theDatabase.addPost(inTitle, inBody, inAuthor, inCategory);
			} else {
				theDatabase.updatePostTitle(finalPost.getPostID(), inTitle);
				theDatabase.updatePostBody(finalPost.getPostID(), inBody);
				theDatabase.updatePostCategory(finalPost.getPostID(), inCategory);
			}
			
			contentPane.setCenter(PostDisplayPanel.createPostDisplayPanel(
					theStage, contentPane, postID));
			contentPane.setLeft(CustomGuiComponents.PostNavBar.createPostNavBar(theStage, contentPane));
		});
		
		rBox.getChildren().addAll(titleStuff, categoryStuff, bodyStuff, errorLabel, post);
		
		return rBox;
	}
}