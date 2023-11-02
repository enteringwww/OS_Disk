
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NewPage implements Initializable {

    @FXML
    private TextField textName;

    @FXML
    private TextField textTypeName;

    @FXML
    private RadioButton dir;

    @FXML
    private RadioButton file;

    @FXML
    private RadioButton readOnlyFile;

    @FXML
    private Button submitButton;
    public Button fileButton;
    public Stage readStage;
    public Stage writeStage;
    public CatalogueItem catalogueItem;

    // 指向文件的路径
    public ArrayList<CatalogueItem> filePath;

    public NewPage() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void newFileOrFolder(String textNameStr, String textTypeNameStr, int attribute) {
        if (attribute == 8) {
            CatalogueItem md = Operation.md(textNameStr, CenterPane.path);
            if ((catalogueItem = md) != null) {
                try {
                    filePath = Operation.clonePath(CenterPane.path);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                filePath.add(catalogueItem);


                fileButton = new Button();
                fileButton = getCButton(md);


                setRightMenuFolderForCatalogue();
                CenterPane.rootPane.getChildren().add(fileButton);

                //                System.out.println("parentT size: " +MainPage.parentT.size());
                if (MainPage.parentT.isEmpty()) {
                    TreeItem<String> tree1 = new TreeItem<>(textNameStr);
                    tree1.setGraphic(new ImageView(new Image("/image/foldert.png")));
                    MainPage.rootTreeItem.getChildren().add(tree1);
                } else {
                    TreeItem<String> foundItem = findItemByName(MainPage.rootTreeItem, MainPage.parentT.get(MainPage.parentT.size() - 1));
                    foundItem.setExpanded(true);
                    TreeItem<String> stringTreeItem = new TreeItem<>(textNameStr);
                    stringTreeItem.setGraphic(new ImageView(new Image("/image/foldert.png")));
//                    System.out.println("stringTreeItem: "+stringTreeItem);
                    foundItem.getChildren().add(stringTreeItem);
                }

                fileButton.setOnMouseClicked(e -> {
                    MainPage.rootContextMenu.hide();
                    if (e.getClickCount() == 2) {
                        CenterPane.path.add(catalogueItem);

                        MainPage.parentT.add(catalogueItem.getAllName());
//                        MainPage.nowCatalogueArray.add(new TreeItem<>(textNameStr));

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
                    }
                });

                CenterPane.map.put(catalogueItem, fileButton);
            }

        } else {
            CatalogueItem file1 = Operation.create_file(textNameStr, textTypeNameStr, (byte) attribute, CenterPane.path);
            if ((catalogueItem = file1) != null) {
                // 得到指向当前文件的路径
                try {
                    filePath = Operation.clonePath(CenterPane.path);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                // filePath是个指向当前文件的路径
                filePath.add(catalogueItem);

                fileButton = new Button();
                fileButton = getFButton(file1);

                if (MainPage.parentT.isEmpty()) {
                    TreeItem<String> tree1 = new TreeItem<>(catalogueItem.getAllName());
                    tree1.setGraphic(new ImageView(new Image("/image/filet.png")));
                    MainPage.rootTreeItem.getChildren().add(tree1);
                } else {
                    TreeItem<String> foundItem = findItemByName(MainPage.rootTreeItem, MainPage.parentT.get(MainPage.parentT.size() - 1));
                    foundItem.setExpanded(true);
                    TreeItem<String> stringTreeItem = new TreeItem<>(catalogueItem.getAllName());
                    stringTreeItem.setGraphic(new ImageView(new Image("/image/filet.png")));
//                    System.out.println("stringTreeItem: "+stringTreeItem);
                    foundItem.getChildren().add(stringTreeItem);
                }

                fileButton.setOnMouseClicked(e -> {
                    MainPage.rootContextMenu.hide();
                    if (e.getClickCount() == 2) {
                        String text = null;
                        try {
                            int listNum = Operation.getListNum(filePath.get(filePath.size() - 1).getStartBlock(),
                                    Disk.readBlock(filePath.get(filePath.size() - 2).getStartBlock()));

                            text = Operation.read_file(filePath, Disk.readOnlyByte(filePath.get(filePath.size() - 2).getStartBlock(), listNum, 7) * 64);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        if (text != null) {
                            getReadStage(text, catalogueItem);
                        } else {
                            getReadStage(null, catalogueItem);
                        }
                    }
                });

                setRightMenuFileForFile(fileButton);
                CenterPane.rootPane.getChildren().add(fileButton);

                CenterPane.map.put(catalogueItem, fileButton);
            }
        }
    }

    private void setRightMenuFolderForCatalogue() {
        ContextMenu rightMenu = new ContextMenu();

        MenuItem displayFolderItem = new MenuItem("打开目录", new ImageView(new Image("/image/open.png")));
        displayFolderItem.setOnAction((a) -> {
            CenterPane.path.add(catalogueItem);

            MainPage.parentT.add(catalogueItem.getAllName());
//            MainPage.nowCatalogueArray.add(new TreeItem<>(msg.getAllName()));
//            for (TreeItem<String> stringTreeItem : MainPage.nowCatalogueArray) {
//                System.out.println("dangqian:: "+stringTreeItem);
//            }

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
        });
        MenuItem removeFolderItem = new MenuItem("删除空目录", new ImageView(new Image("/image/close.png")));
        removeFolderItem.setOnAction((a) -> {
            try {
                if (Operation.rd(filePath) != -1) {
                    CenterPane.rootPane.getChildren().remove(this.fileButton);
                    CenterPane.map.remove(catalogueItem);


                    TreeItem<String> foundItem = findItemByName(MainPage.rootTreeItem, catalogueItem.getAllName());
                    foundItem.getParent().getChildren().remove(foundItem);
                    if (!MainPage.parentT.isEmpty()) {
                        MainPage.parentT.remove(MainPage.parentT.size() - 1);
                    }
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

        });

        // 设置右键菜单样式
        rightMenu.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d1c7b7; -fx-border-width: 2; ");

        // 设置菜单项样式
        displayFolderItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        removeFolderItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");


        rightMenu.getItems().addAll(displayFolderItem, removeFolderItem);
        fileButton.setContextMenu(rightMenu);
    }

    private void setRightMenuFileForFile(Button btn) {
        ContextMenu rightMenu = new ContextMenu();

        MenuItem readFileItem = new MenuItem("读文件", new ImageView(new Image("/image/read.png")));
        readFileItem.setOnAction((a) -> {

            String text = null;
            try {
                int listNum = Operation.getListNum(filePath.get(filePath.size() - 1).getStartBlock(),
                        Disk.readBlock(filePath.get(filePath.size() - 2).getStartBlock()));
                //下面文件所占的磁盘块数这里得从磁盘读，因为对象里的内容没有改。
                text = Operation.read_file(filePath, Disk.readOnlyByte(filePath.get(filePath.size() - 2).getStartBlock(), listNum, 7)/*msg.getLength()*/ * 64);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (text != null) {
                getReadStage(text, catalogueItem);
            } else {
                getReadStage(null, catalogueItem);
            }
        });

        MenuItem writeFileItem = new MenuItem("写文件", new ImageView(new Image("/image/write.png")));
        writeFileItem.setOnAction((a) -> {
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setPrefColumnCount(20);
            textArea.setPrefRowCount(10);
            textArea.setFont(Font.font(20));
            Button writeBtn = new Button("保存");
            writeBtn.setPrefSize(100, 30);
            writeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            writeBtn.setOnMouseEntered(e -> writeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;-fx-border-width: 2; -fx-border-color: blue; -fx-border-radius: 3"));
            writeBtn.setOnMouseExited(e -> writeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
            writeBtn.setOnAction(e -> {
                String text = textArea.getText();
                try {
                    Operation.write_file(filePath, text.getBytes(StandardCharsets.UTF_8), text.getBytes().length);
                } catch (CloneNotSupportedException e1) {
                    e1.printStackTrace();
                }
            });
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(20));
            vBox.getChildren().addAll(textArea, writeBtn);
            writeStage = new Stage();
            Scene s = new Scene(vBox);
            writeStage.setScene(s);
            writeStage.setTitle("正在写: " + catalogueItem.getAllName());
            writeStage.getIcons().add(new Image("/image/disk.png"));
            writeStage.show();
        });

        MenuItem closeFileItem = new MenuItem("关闭文件", new ImageView(new Image("/image/closeFile.png")));
        closeFileItem.setOnAction((a) -> {
            Operation.close_file(filePath);
            if (readStage != null) {
                readStage.close();
            }
            if (writeStage != null) {
                writeStage.close();
            }
        });

        MenuItem deleteFileItem = new MenuItem("删除文件", new ImageView(new Image("/image/delete.png")));
        deleteFileItem.setOnAction((a) -> {
            try {
                if ((Operation.delete_file(filePath)) != -1) {
                    CenterPane.rootPane.getChildren().remove(this.fileButton);

                    TreeItem<String> foundItem = findItemByName(MainPage.rootTreeItem, catalogueItem.getAllName());
//            MainPage.nowCatalogueArray.get(MainPage.nowCatalogueArray.size() - 1).getChildren().remove(msg.getAllName());
                    foundItem.getParent().getChildren().remove(foundItem);

                    CenterPane.map.remove(catalogueItem);//用输出返回值是不是null来测试是否删除
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

        MenuItem typeFileItem = new MenuItem("显示文件内容", new ImageView(new Image("/image/detile.png")));
        typeFileItem.setOnAction((a) -> {
            String text = null;
            try {
                text = Operation.typeFile(filePath);

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            String result = text.replaceAll("(\n|$)", "\n\n");
            getContentStage(result, catalogueItem);
        });

        MenuItem changeFileItem = new MenuItem("改变文件属性", new ImageView(new Image("/image/change.png")));
        changeFileItem.setOnAction((a) -> {
            byte attribute = catalogueItem.getAttribute();
            if (attribute == 4) {
                attribute = 3;
            } else if (attribute == 3) {
                attribute = 4;
            }
            try {
                if (Operation.change(filePath, attribute) != -1) {
                    PromptWindow.showWindow("已改变文件属性为：" + ((attribute == 4) ? "普通文件" : "系统只读文件"));
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

        });

        // 设置右键菜单样式
        rightMenu.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d1c7b7; -fx-border-width: 2; ");

        // 设置菜单项样式
        readFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        changeFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        closeFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        deleteFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        writeFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        typeFileItem.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");


        rightMenu.getItems().addAll(readFileItem, writeFileItem, closeFileItem, deleteFileItem, typeFileItem,
                changeFileItem);
        btn.setContextMenu(rightMenu);
    }

    private void getReadStage(String string, CatalogueItem message) {
        readStage = new Stage();
        readStage.setTitle("正在读: " + message.getAllName());
        Text text = new Text(string);
        text.setFont(Font.font("Arial", 30));
        text.setLineSpacing(2);
        text.setWrappingWidth(300);
        Pane root = new TextFlow();
        root.getChildren().add(text);

        readStage.setScene(new Scene(root, 350, 200));
        readStage.getIcons().add(new Image("/image/disk.png"));
        readStage.show();
    }

    private void getContentStage(String string, CatalogueItem message) {
        readStage = new Stage();
        readStage.setTitle("显示文件内容: " + message.getAllName());

        Text text = new Text(string);
        text.setFont(Font.font("Arial", 25));
        text.setWrappingWidth(400);
        Pane root = new StackPane();
        root.getChildren().add(text);

        readStage.setScene(new Scene(root, 520, 450));
        readStage.getIcons().add(new Image("/image/disk.png"));
        readStage.show();
    }

    @FXML
    public void catalogueAction() {
        String textNameStr = textName.getText().trim();
        String textTypeNameStr = textTypeName.getText().trim();
        if (textNameStr.contains("$") || textNameStr.contains(".") || textNameStr.contains("/")) {
            PromptWindow.showWindow("文件名不能包含“$”、“.”、“/”字符。");
            return;
        }
        if (textNameStr.isEmpty()) {
            PromptWindow.showWindow("输入错误！请重试");
            return;
        }
        if (textNameStr.length() > 3) {
            PromptWindow.showWindow("文件名不能超过3个字符");
            return;
        }
        int attribute = 0;
        if (dir.isSelected()) {
            attribute = 8;
        } else if (file.isSelected()) {
            attribute = 4;
        } else if (readOnlyFile.isSelected()) {
            attribute = 3;
        }

        MainPage.newstage.close();

        newFileOrFolder(textNameStr, textTypeNameStr, attribute);
    }

    @FXML
    public void fileAction() {
        String textNameStr = textName.getText().trim();
        String textTypeNameStr = textTypeName.getText().trim();
        if (textNameStr.contains("$") || textNameStr.contains(".") || textNameStr.contains("/")) {
            PromptWindow.showWindow("文件名不能包含“$”、“.”、“/”字符。");
            return;
        }
        if (textNameStr.isEmpty() || textTypeNameStr.isEmpty()) {
            PromptWindow.showWindow("输入错误！请重试");
            return;
        }
        if (textNameStr.length() > 3) {
            PromptWindow.showWindow("文件名不能超过3个字符");
            return;
        }
        if (textTypeNameStr.length() > 2) {
            PromptWindow.showWindow("类型不能超过2个字符");
            return;
        }
        int attribute = 0;
        if (dir.isSelected()) {
            attribute = 8;
        } else if (file.isSelected()) {
            attribute = 4;
        } else if (readOnlyFile.isSelected()) {
            attribute = 3;
        }

        MainPage.newstage.close();

        newFileOrFolder(textNameStr, textTypeNameStr, attribute);
    }

    private TreeItem<String> findItemByName(TreeItem<String> root, String name) {
        if (root.getValue().equals(name)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> found = findItemByName(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @FXML
    private void mouseEnter() {
        submitButton.setStyle("-fx-font-size: 16; -fx-background-color: #4caf50; -fx-text-fill: #fff; -fx-border-width: 4; -fx-border-color: blue; -fx-border-radius: 3");
    }

    @FXML
    private void mouseExit() {
        submitButton.setStyle("-fx-font-size: 16; -fx-background-color: #4caf50; -fx-text-fill: #fff;");
    }

    private Button getCButton(CatalogueItem catalogueItem) {
        // 文件名字
        Text customText = new Text(catalogueItem.getAllName());
        customText.setFont(Font.font("Arial", 17));
        ImageView imageView = new ImageView(new Image("/image/folder.png"));

        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(imageView, customText);
        vBox.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(vBox);

        // 设置按钮样式
        button.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 10; -fx-border-color: #d1c7b7; -fx-border-width: 2;");

        // 添加阴影效果
        button.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.2)));

        // 鼠标经过时的样式
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        // 鼠标离开时的样式
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d1c7b7; -fx-border-width: 2;"));

        // 鼠标点击时的样式
        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        // 鼠标释放时恢复到鼠标经过时的样式
        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        return button;
    }

    private Button getFButton(CatalogueItem catalogueItem) {
        // 文件名字
        Text customText = new Text(catalogueItem.getAllName());
        customText.setFont(Font.font("Arial", 17));

        ImageView imageView = new ImageView(new Image("/image/file.png"));

        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(imageView, customText);
        vBox.setAlignment(Pos.CENTER);

        Button button = new Button();
//        button.setStyle("-fx-background-color: #afdfe4; -fx-background-radius: 10;");
        button.setGraphic(vBox);

        button.setStyle("-fx-background-color: #fffef9; -fx-background-radius: 10; -fx-border-color: #d1c7b7; -fx-border-width: 2;");
        // 鼠标经过时的样式
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        // 鼠标离开时的样式
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #fffef9; -fx-border-color: #d1c7b7; -fx-border-width: 2;"));

        // 鼠标点击时的样式
        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        // 鼠标释放时恢复到鼠标经过时的样式
        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #33a3dc; -fx-border-width: 2;"));

        return button;
    }
}
