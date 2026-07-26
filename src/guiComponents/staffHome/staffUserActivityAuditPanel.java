package guiComponents.staffHome;

/***
 * @author Jchacko (Team 3) - Designed and implemented the staff user
 * activity audit panel for displaying a selected student's post
 * activity and statistics.
 *
 * @version 1.0.0 - Jchacko (Team 3) - Initial implementation
 */

import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.Post;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class staffUserActivityAuditPanel {

    private static Database theDatabase =
            FoundationsMain.database;

    public static VBox createUserActivityAuditPanel(String username) {

        VBox auditPanel = new VBox(10);

        Label titleLabel = new Label("User Activity Audit");
        Label usernameLabel = new Label("Student: " + username);

        int postCount = theDatabase.getPostCountForUser(username);
        int replyCount = theDatabase.getReplyCountForUser(username);
        List<Post> userPosts = theDatabase.getPostsForUser(username);

        Label postCountLabel =
                new Label("Number of posts: " + postCount);

        Label replyCountLabel =
                new Label("Number of replies: " + replyCount);

        Label postsHeading =
                new Label("Posts created by this student:");

        auditPanel.getChildren().addAll(
                titleLabel,
                usernameLabel,
                postCountLabel,
                replyCountLabel,
                postsHeading
        );

        if (userPosts.isEmpty()) {
            auditPanel.getChildren().add(
                    new Label("No posts found for this student.")
            );
        } else {
            for (Post post : userPosts) {
                Label postLabel = new Label(
                        "Post ID: " + post.getPostID()
                        + " | Title: " + post.getTitle()
                        + " | Category: " + post.getCategory()
                );

                postLabel.setWrapText(true);
                auditPanel.getChildren().add(postLabel);
            }
        }

        return auditPanel;
    }
}