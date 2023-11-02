import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

// 主要页面，读取fxml文件初始化
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image("/image/disk.png"));
        stage.setTitle("模拟磁盘文件系统");
        stage.setOnCloseRequest(event -> System.exit(0));
        Parent rootPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/MainPage.fxml")));
        Scene scene = new Scene(rootPane, 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

}
