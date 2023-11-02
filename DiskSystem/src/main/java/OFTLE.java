import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

//已打开文件表
public class OFTLE {

    //绝对路径名
    private String filePathName;

    //文件属性
    private byte fileAttribute;

    // 起始盘块号
    private int fileStartNumber;

    // 长度
    private int length;

    //操作类型，用“0”表示以读操作方式打开文件，用“1”表示以写操作方式打开文件
    private int operationType;

    // 读文件的位置，文件打开时 blockNumber 为文件起始盘块号，blockByteNumber 为“0”
    private Pointer read;

    // 写文件的位置，打开文件时 blockNumber 和 blockByteNumber 为文件的末尾位置
    private Pointer write;

    //bottom的表格
    private StringProperty filep = new SimpleStringProperty();
    private StringProperty typep = new SimpleStringProperty();

    public StringProperty filepProperty() {
        return filep;
    }

    public StringProperty typepProperty() {
        return typep;
    }

    public void setFilep() {
        filep.set(filePathName);
    }

    public void setTypep() {
        this.typep.set((operationType == 0) ? "读" : "写");
    }


    public OFTLE() {

    }

    public OFTLE(String filePathName, byte fileAttribute, int fileStartNumber, int length, int operationType) {
        this.filePathName = filePathName;
        this.fileAttribute = fileAttribute;
        this.fileStartNumber = fileStartNumber;
        this.length = length;
        this.operationType = operationType;
        setFilep();
        setTypep();
    }

    public void newPointer() {
        this.read = new Pointer();
        this.write = new Pointer();
    }

    public String getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(String filePathName) {
        this.filePathName = filePathName;
        this.filep = new SimpleStringProperty(String.valueOf(filePathName));
    }

    public byte getFileAttribute() {
        return fileAttribute;
    }

    public void setFileAttribute(byte fileAttribute) {
        this.fileAttribute = fileAttribute;
    }

    public int getFileStartNumber() {
        return fileStartNumber;
    }

    public void setFileStartNumber(int fileStartNumber) {
        this.fileStartNumber = fileStartNumber;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
        this.typep = new SimpleStringProperty(String.valueOf(operationType));
    }

    public Pointer getRead() {
        return read;
    }

    public void setRead(Pointer read) {
        this.read = read;
    }

    public Pointer getWrite() {
        return write;
    }

    public void setWrite(Pointer write) {
        this.write = write;
    }
}
