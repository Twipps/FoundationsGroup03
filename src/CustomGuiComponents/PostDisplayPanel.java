package CustomGuiComponents;

import entityClasses.Post;
import entityClasses.PostList;
import entityClasses.Reply;
import entityClasses.ReplyList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class PostDisplayPanel {

	public static ScrollPane createPostDisplayPanel(Stage theStage, BorderPane contentPane, int postID) {
		ScrollPane postReplyStack = new ScrollPane();
		postReplyStack.setFitToWidth(true);

		VBox postStack = new VBox(15);
		postStack.setPadding(new Insets(20));

		PostList posts = new PostList();
		Post post = posts.getPost(postID);

		if (post == null) {
			postStack.getChildren().add(new Label("Post not found."));
			postReplyStack.setContent(postStack);
			return postReplyStack;
		}

		Label title = new Label(post.getTitle());
		title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button edit = new Button("Edit");
		Button delete = new Button("Delete");

		edit.setOnAction(e -> {
			contentPane.setCenter(PostReplyEditPanel.createPostEditPanel(theStage, contentPane, postID));
		});

		delete.setOnAction(e -> {
			posts.deletePost(postID);
			contentPane.setLeft(PostNavBar.createPostNavBar(theStage, contentPane));
			contentPane.setCenter(new Label("Select or create a post."));
		});

		HBox titleRow = new HBox(10);
		titleRow.getChildren().addAll(title, spacer, edit, delete);

		Label author = new Label("Posted by: " + post.getAuthor());
		Label category = new Label("Category: " + post.getCategory());
		Label createdDate = new Label("Created: " + post.getCreatedDate());

		Label body = new Label(post.getBody());
		body.setWrapText(true);

		postStack.getChildren().addAll(titleRow, author, category, createdDate, body, new Separator());

		postStack.getChildren().add(createReplyInput(contentPane, postID, theStage));

		Label repliesTitle = new Label("Replies");
		repliesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		postStack.getChildren().add(repliesTitle);

		postStack.getChildren().add(replyStack(theStage, contentPane, postID));

		postReplyStack.setContent(postStack);
		return postReplyStack;
	}

	private static VBox createReplyInput(BorderPane contentPane, int postID, Stage theStage) {
		VBox rBox = new VBox(8);

		Label replyLabel = new Label("Add Reply:");
		TextArea replyInput = new TextArea();
		replyInput.setWrapText(true);
		replyInput.setPrefRowCount(4);

		Label error = new Label();
		error.setStyle("-fx-text-fill: red;");

		Button submit = new Button("Submit Reply");

		submit.setOnAction(e -> {

			String body = replyInput.getText().trim();

			if (body.isEmpty()) {
				error.setText("Reply body cannot be empty.");
				return;
			}

			ReplyList replies = new ReplyList();

			replies.createReply(
				postID,
				body,
				applicationMain.FoundationsMain.database.getCurrentUsername()
			);

			contentPane.setCenter(
				PostDisplayPanel.createPostDisplayPanel(
					theStage,
					contentPane,
					postID
				)
			);
		});

		rBox.getChildren().addAll(replyLabel, replyInput, error, submit);
		return rBox;
	}

	// replystack is really ugly right now but it works PLACEHOLDER
	public static VBox replyStack(Stage theStage, BorderPane contentPane, int postID) {
		VBox rBox = new VBox(10);

		ReplyList replies = new ReplyList();

		for (Reply reply : replies.getRepliesForPost(postID)) {
			rBox.getChildren().add(createReplyRow(theStage, contentPane, reply));
		}

		return rBox;
	}

	public static VBox createReplyRow(Stage theStage, BorderPane contentPane, Reply reply) {
		VBox rBox = new VBox(6);
		rBox.setPadding(new Insets(10));
		rBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

		Label author = new Label("Reply by: " + reply.getAuthor());
		Label created = new Label("Created: " + reply.getCreatedDate());

		Label body = new Label(reply.getBody());
		body.setWrapText(true);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button delete = new Button("Delete");

		delete.setOnAction(e -> {
			ReplyList replies = new ReplyList();
			replies.deleteReply(reply.getReplyID());

			contentPane.setCenter(createPostDisplayPanel(theStage, contentPane, reply.getParentPostID()));
		});

		HBox topRow = new HBox(10);
		topRow.getChildren().addAll(author, spacer, delete);

		rBox.getChildren().addAll(topRow, created, body);

		return rBox;
	}
}