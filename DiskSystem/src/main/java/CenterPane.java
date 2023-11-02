import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.HashMap;

public class CenterPane {
    public static FlowPane rootPane;

    //存储文件夹
    public static ArrayList<CatalogueItem> path;
    public static HashMap<CatalogueItem, Button> map;

    public CenterPane() {
    }

    public CenterPane(FlowPane rootPane) {
        CenterPane.rootPane = rootPane;
        path = new ArrayList<>();
        map = new HashMap<>();
    }
}
