import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PopupWindow {
    public static void showWindow(String message, Stage stage) {
        Label label = new Label(message);
        label.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10px;");

        StackPane popupLayout = new StackPane();
        popupLayout.getChildren().add(label);

        Popup popup = new Popup();
        popup.getContent().add(popupLayout);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), event -> popup.hide()));
        timeline.play();
        popup.setOnHidden(event -> timeline.stop());
        popup.show(stage);
    }
}
