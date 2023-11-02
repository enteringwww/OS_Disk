import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PromptWindow {
    public static void showWindow(String message) {
        Alert warning = new Alert(AlertType.WARNING);
        warning.getDialogPane().setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d1c7b7; -fx-font-size: 14;-fx-text-fill: #333;");
        warning.setTitle("提示");
        Stage alertStage = (Stage) warning.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("/image/disk.png"));
        warning.setHeaderText("");
        warning.setContentText(message);

        warning.showAndWait();
    }
}
