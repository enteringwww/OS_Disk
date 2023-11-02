
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;


// 已打开文件表
public class OpenFile {
    private ArrayList<OFTLE> fileList;

    // 监听变化，在界面实时显示
    private ObservableList<OFTLE> openedFilesTable;

    public OpenFile() {
        fileList = new ArrayList<OFTLE>(5);
        openedFilesTable = FXCollections.observableArrayList(new ArrayList<OFTLE>());
    }

    // 给定一个文件的起始块号，查看该文件是否在打开文件的列表中。在就返回它在列表中的下标，不在返回-1。
    public int searchOpenedFile(int index) {
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).getFileStartNumber() == index) {
                return i;
            }
        }
        return -1;
    }

    // 添加新的文件打开列表信息(打开文件）
    public void addFileOrCatalogue(String name, byte attribute, int number, int length, int flag) {
        // 若文件列表已满
        if (fileList.size() >= 5) {
            PromptWindow.showWindow("文件列表已满，打开文件失败");
            return;
        } else {
            fileList.add(new OFTLE(name, attribute, number, length, flag));
            openedFilesTable.add(new OFTLE(name, attribute, number, length, flag));
        }
    }

    // 从 已打开文件打开列表 中删除一条记录(关闭文件)
    public void deleteFromOpenedFileTable(int n) {
        fileList.remove(n);
        openedFilesTable.remove(n);
    }

    // 修改文件指针
    public void changePointer(int n, int dnum, int bnum) {
        if (fileList.get(n).getOperationType() == 0) {
            fileList.get(n).setRead(new Pointer(dnum, bnum));
        } else {
            fileList.get(n).setWrite(new Pointer(dnum, bnum));
        }
    }

    // 获取文件信息
    public OFTLE getOftle(int n) {
        return fileList.get(n);
    }

    public int getLength() {
        return fileList.size();
    }

    public ArrayList<OFTLE> getFileList() {
        return fileList;
    }

    public void setFileList(ArrayList<OFTLE> fileList) {
        this.fileList = fileList;
    }

    public ObservableList<OFTLE> getOpenedFilesTable() {
        return openedFilesTable;
    }

    public void setOpenedFilesTable(ObservableList<OFTLE> openedFilesTable) {
        this.openedFilesTable = openedFilesTable;
    }

}