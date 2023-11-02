import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainPage implements Initializable {


    public static TreeView<String> treeView;
    public static TreeItem<String> rootTreeItem;
    //    public static ArrayList<TreeItem<String>> nowCatalogueArray; // 当前进入目录的树节点
    public static ArrayList<String> parentT; // 进入的目录的名字

    public BorderPane rootBorder;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label backLabel;
    @FXML
    private Menu goBack;
    @FXML
    private Menu doNew;
    @FXML
    private MenuItem createCatalogue;
    @FXML
    private MenuItem createFile;

    @FXML
    public TableView<OFTLE> tableView;
    @FXML
    private TableColumn col1;
    @FXML
    private TableColumn col2;
    @FXML
    private FlowPane rootPane;
    static ContextMenu rootContextMenu;
    public static Stage newstage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootContextMenu = creatContextMenu();

        rootPane.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                rootContextMenu.hide();
            }
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                rootContextMenu.show(rootPane, e.getScreenX(), e.getScreenY());
            }
        });


        rootPane.setHgap(35);
        rootPane.setVgap(35);
        rootPane.setPadding(new Insets(30, 30, 30, 30));
        new CenterPane(rootPane);

        // 初始化
        Disk.start();
        CatalogueItem roo = new CatalogueItem("$  ".getBytes(), "  ".getBytes(), (byte) 8, (byte) 2, (byte) 0);
        CenterPane.path.add(roo);


        doNew.setGraphic(new ImageView(new Image("/image/doNew.png")));

        createCatalogue.setGraphic(new ImageView(new Image("/image/newFolder.png")));
        createCatalogue.setOnAction(e -> {
            newstage = new Stage();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/NewCatalogue.fxml")));
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            newstage.setTitle("新建目录");
            newstage.getIcons().add(new Image("/image/disk.png"));
            newstage.setScene(new Scene(root));
            newstage.initModality(Modality.APPLICATION_MODAL);
            newstage.show();
        });

        createFile.setGraphic(new ImageView(new Image("/image/newFile.png")));
        createFile.setOnAction(e -> {
            newstage = new Stage();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/NewFile.fxml")));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            newstage.setTitle("新建文件");
            newstage.getIcons().add(new Image("/image/disk.png"));
            newstage.initModality(Modality.APPLICATION_MODAL);
            newstage.setScene(new Scene(root));
            newstage.show();
        });

        backLabel.setGraphic(new ImageView(new Image("/image/goBack.png")));
        backLabel.setOnMouseClicked(goBackButton);


        tableView.setItems(Operation.getOpenFile().getOpenedFilesTable());

        tableView.setPlaceholder(new Label("无已打开的文件"));
        col1.setCellValueFactory(new PropertyValueFactory("filep"));
        col2.setCellValueFactory(new PropertyValueFactory("typep"));

        parentT = new ArrayList<>();
        treeView = new TreeView<>();
//        nowCatalogueArray = new ArrayList<>();

        rootTreeItem = new TreeItem<>("$");
//        nowCatalogueArray.add(rootTreeItem);


        rootTreeItem.setExpanded(true);
        rootTreeItem.setGraphic(new ImageView(new Image("/image/rootTree.png")));

        treeView.setRoot(rootTreeItem);

        treeView.setStyle("-fx-max-width: 200; -fx-background-color: #f0f0f0;-fx-border-color: #d0d0d0;-fx-text-fill: #333;-fx-font-family: 'Arial';-fx-font-size: 14px;");
        rootBorder.setLeft(treeView);


    }

    private ContextMenu creatContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("新建目录", new ImageView(new Image("/image/newFolder.png")));
        menuItem1.setOnAction(e -> createCatalogue.fire());
        MenuItem menuItem2 = new MenuItem("新建文件", new ImageView(new Image("/image/newFile.png")));
        menuItem2.setOnAction(e -> createFile.fire());
        MenuItem menuItem3 = new MenuItem("返回上一级", new ImageView(new Image("/image/goBack.png")));
        menuItem3.setOnAction(goBackButton);

        contextMenu.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d1c7b7; -fx-border-width: 2; ");
        menuItem1.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        menuItem2.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        menuItem3.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");

        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);

        return contextMenu;
    }

    EventHandler goBackButton = e -> {
        if (CenterPane.path.size() == 1) {
            PromptWindow.showWindow("当前已经是根目录，无法后退。");
            return;
        }
        CenterPane.path.remove(CenterPane.path.size() - 1);

//        if (nowCatalogueArray.size() != 1) {
//            nowCatalogueArray.remove(nowCatalogueArray.size() - 1);
//        }
        if (!parentT.isEmpty()) {
            parentT.remove(parentT.size() - 1);
        }


        HashMap<CatalogueItem, Button> map = Operation.dir(CenterPane.path, CenterPane.map);

        Set<Map.Entry<CatalogueItem, Button>> entryAll = CenterPane.map.entrySet();
        for (Map.Entry<CatalogueItem, Button> ent : entryAll) {
            ent.getValue().setVisible(false);
            ent.getValue().setManaged(false);
        }

        Set<Map.Entry<CatalogueItem, Button>> entry = map.entrySet();
        for (Map.Entry<CatalogueItem, Button> ent : entry) {
            ent.getValue().setVisible(true);
            ent.getValue().setManaged(true);
        }

    };
}
