package CustomGuiComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import postComponents.Post;
import postComponents.PostList;

public class PostDisplayPanel {
	
	public static VBox createPostDisplayPanel(Stage theStage, BorderPane contentPane, int postID) {
		VBox rBox = new VBox(15);
		rBox.setPadding(new Insets(20));
		
		PostList posts = new PostList();
		Post post = posts.getPost(postID);
		
		if (post == null) {
			rBox.getChildren().add(new Label("Post not found."));
			return rBox;
		}
		
		Label title = new Label(post.getTitle());
		title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
		
		Region spacer = new Region(); // to grow and space out the edit button and title		
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		Button edit = new Button("Edit");
		Button delete = new Button("Delete");
		
		edit.setOnAction(e -> {
			contentPane.setCenter(PostReplyEditPanel.createPostEditPanel(theStage, contentPane, postID));
		});
		
		delete.setOnAction(e -> {
			posts.deletePost(postID);
			contentPane.setLeft(CustomGuiComponents.PostNavBar.createPostNavBar(theStage, contentPane));
			contentPane.setCenter(new Label("Select or create a post."));
		});
		
		HBox titleRow = new HBox(10);
		titleRow.getChildren().addAll(title, spacer, edit, delete);
		
		Label author = new Label("Posted by: " + post.getAuthor());
		Label category = new Label("Category: " + post.getCategory());
		Label createdDate = new Label("Created: " + post.getCreatedDate());
		
		Label body = new Label(post.getBody());
		body.setWrapText(true);
		
		rBox.getChildren().addAll(titleRow, author, category, createdDate, body);
		
		return rBox;
	}
}