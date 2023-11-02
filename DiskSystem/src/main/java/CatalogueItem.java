import java.util.Arrays;

// 文件目录结构由若干 `目录项` 组成
public class CatalogueItem implements Cloneable {
    private byte[] fileName; // 文件名，目录名：3个字节
    private byte[] backName; // 文件类型名：2个字节;    目录时，此项填入空格
    private byte attribute; // 文件属性：1个字节； 8目录  3只读  4普通文件
    private byte startBlock; // 起始盘块号：1个字节
    private byte length; // 文件长度单位为盘块    目录时，保留 1字节未使用（在实验中填写“0”）

    public CatalogueItem(byte[] fileName, byte[] backName, byte attribute, byte startBlock, byte length) {
        this.fileName = fileName;
        this.backName = backName;
        this.attribute = attribute;
        this.startBlock = startBlock;
        this.length = length;
    }

    // 生成目录项
    public CatalogueItem(byte[] buffer, int index) {
        // 文件名：3个字节
        this.fileName = new byte[]{buffer[index * 8 + 0], buffer[index * 8 + 1], buffer[index * 8 + 2]};
        // 文件类型名：2个字节
        this.backName = new byte[]{buffer[index * 8 + 3], buffer[index * 8 + 4]};
        // 文件属性：1个字节
        this.attribute = buffer[index * 8 + 5];
        // 起始盘块号：1个字节
        this.startBlock = buffer[index * 8 + 6];
        // 文件长度：1 个字节
        this.length = buffer[index * 8 + 7];
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CatalogueItem catalogueItem = (CatalogueItem) super.clone();
        byte[] fileName = catalogueItem.fileName;
        byte[] backName = catalogueItem.backName;
        byte[] newFileName = fileName.clone();
        byte[] newBackName = backName.clone();
        catalogueItem.fileName = newFileName;
        catalogueItem.backName = newBackName;
        return catalogueItem;
    }

    //得到名字；当目录时，得到名字；当文件时，得到 《名.类型》
    public String getAllName() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (fileName[i] == (byte) '\0') {
                break;
            }
            stringBuilder.append((char) fileName[i]);
        }
        // attribute<8表示这是一个文件,不是目录
        if (attribute < 8) {
            stringBuilder.append('.');
            for (int i = 0; i < 2; i++) {
                if (backName[i] == (byte) '\0') {
                    break;
                }
                stringBuilder.append((char) backName[i]);
            }
        }
        return new String(stringBuilder).trim();
    }

    // 传入目录项，得到名字
    public static String getAllName2(byte[] fileMessage) {
        byte[] fileName = new byte[]
                {fileMessage[0], fileMessage[1], fileMessage[2]};
        byte[] backName = new byte[]
                {fileMessage[3], fileMessage[4]};
        byte attribute = fileMessage[5];

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (fileName[i] == (byte) '\0') {
                break;
            }
            stringBuilder.append((char) fileName[i]);
        }
        if (attribute < 8) // attribute<8表示这是一个文件,不是目录
        {
            stringBuilder.append('.');
            for (int i = 0; i < 2; i++) {
                if (backName[i] == (byte) '\0') {
                    break;
                }
                stringBuilder.append((char) backName[i]);
            }
        }
        return new String(stringBuilder);
    }

    public byte[] getFileName() {
        return fileName;
    }

    public void setFileName(byte[] fileName) {
        this.fileName = fileName;
    }

    public byte[] getBackName() {
        return backName;
    }

    public void setBackName(byte[] backName) {
        this.backName = backName;
    }

    public byte getAttribute() {
        return attribute;
    }

    public void setAttribute(byte attribute) {
        this.attribute = attribute;
    }

    public byte getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(byte startBlock) {
        this.startBlock = startBlock;
    }

    public byte getLength() {
        return length;
    }

    public void setLength(byte length) {
        this.length = length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + attribute;
        result = prime * result + Arrays.hashCode(backName);
        result = prime * result + Arrays.hashCode(fileName);
        result = prime * result + length;
        result = prime * result + startBlock;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CatalogueItem other = (CatalogueItem) obj;
        if (attribute != other.attribute)
            return false;
        if (!Arrays.equals(backName, other.backName))
            return false;
        if (!Arrays.equals(fileName, other.fileName))
            return false;
        if (length != other.length)
            return false;
        if (startBlock != other.startBlock)
            return false;
        return true;
    }

}
