
import javafx.scene.control.Button;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class Operation {
    // 缓冲数组1
    private static byte[] buffer1 = new byte[64];
    private static OpenFile openFile = new OpenFile();

    public Operation() {

    }

    public static OpenFile getOpenFile() {
        return openFile;
    }

    public static void setOpenFile(OpenFile openFile) {
        Operation.openFile = openFile;
    }


    // 寻找空闲磁盘块:从FAT表里查内容为0的项，找到返回盘块号，找不到则返回-1，代表磁盘空间已满
    public static int searchFreeBlock() {
        byte[] buffer = new byte[64];
        buffer = Disk.readBlock(0);
        for (int i = 3; i < 64; i++) {
            if (buffer[i] == 0) {
                return i;
            }
        }

        buffer = Disk.readBlock(1);
        for (int i = 0; i < 64; i++) {
            if (buffer[i] == 0) {
                return i + 64;
            }
        }
        return -1;
    }

    // 在某目录下有无同名文件/目录:blockNum为目录盘块号下标，查找在第几个索引里有名字name，没有则返回-1
    public static int searchName(String name, int blockNum) {
        buffer1 = Disk.readBlock(blockNum);
        String str;
        for (int i = 0; i < 8; i++) {
            str = CatalogueItem.getAllName2(Arrays.copyOfRange(buffer1, i * 8, i * 8 + 8));// Arrays.copyOfRange函数不取到to的下标
            if (str.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // 获取目录长度
    public static int getListLength(int blockNum) {
        byte[] buffer = Disk.readBlock(blockNum);
        for (int i = 0; i < 8; i++) {
            if (buffer[i * 8] == '$') {
                return i;
            }
        }
        return 8;
    }

    // 用起始块号寻找目录项的位置:buffer是个放索引的盘块，要在这个盘块中找到blockNum对应的索引,找到返回索引下标，没找到返回-1
    public static int getListNum(int blockNum, byte[] buffer) {
        for (int i = 0; i < 8 && buffer[i * 8] != '$'; i++) {
            if (buffer[i * 8 + 6] == blockNum) {
                return i;
            }
        }
        return -1;
    }

    // 删除目录项 , 删除某块中某个目录项
    public static void deletElement(int blockNum, int listNum) {
        buffer1 = Disk.readBlock(blockNum);
        if (listNum < 7 && listNum >= 0) {
            // 把后面的目录项前移，当不是最后一个目录项时
            for (int i = listNum; i < 7 && buffer1[i * 8] != '$'; i++) {
                for (int j = 0; j < 8; j++) {
                    buffer1[i * 8 + j] = buffer1[i * 8 + j + 8];
                }
            }
            buffer1[56] = '$';
        } else if (listNum == 7) {
            buffer1[56] = '$';
        }

        // 模拟缓冲区内容写回磁盘
        Disk.writeBlock(blockNum, buffer1);
    }

    // 增加目录项   增加目录项到下标为blockNum的盘块的，下标为listLength的目录项上
    public static void addElement(int blockNum, int listLength, String name, String typeName, byte attribute,
                                  byte startBlock, byte length) {
        if (name.length() == 0) {
            name = "aut";// 代表自动生成的
        } else if (name.length() == 1) {
            name = name + "\0\0";
        } else if (name.length() == 2) {
            name = name + "\0";
        }
        // 是文件
        if (typeName != "  ")// typeName==“ ”是目录
        {
            if (typeName.length() == 0) {
                typeName = "au";
            } else if (typeName.length() == 1) {
                typeName = typeName + "\0";
            }
        }

        // 在调用该功能前已经检查过列表空间是否已满，所以就不用再检查，直接修改就行了
        buffer1 = Disk.readBlock(blockNum);
        buffer1[listLength * 8 + 0] = (byte) (name.getBytes()[0]);
        buffer1[listLength * 8 + 1] = (byte) (name.getBytes()[1]);// 这里如果没有第二个字符的话，(name.getBytes()[1]会出现数组下标越界的异常。所以要手动给未填满的字符加‘\0’
        buffer1[listLength * 8 + 2] = (byte) (name.getBytes()[2]);
        buffer1[listLength * 8 + 3] = (byte) (typeName.getBytes()[0]);
        buffer1[listLength * 8 + 4] = (byte) (typeName.getBytes()[1]);
        buffer1[listLength * 8 + 5] = attribute;
        buffer1[listLength * 8 + 6] = startBlock;
        buffer1[listLength * 8 + 7] = length;

        Disk.writeBlock(blockNum, buffer1);
    }


    // 获得绝对路径名
    public static String getPathName(ArrayList<CatalogueItem> path) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < path.size() - 1; i++) {
            str.append(path.get(i).getAllName());
            str.append("/");
        }
        str.append(path.get(path.size() - 1).getAllName());
        return new String(str);
    }

    // 根据绝对路径path，找到绝对路径所指向的文件/目录的起始盘块号。
    public static int getBlockIndex(ArrayList<CatalogueItem> path) {
        // 这个路径是可以有文件名的，不是只能有路径名
        return path.get(path.size() - 1).getStartBlock();
    }

    // ①建立文件create_file（文件名，文件属性）{}
    public static CatalogueItem create_file(String name, String type, byte attribute, ArrayList<CatalogueItem> path) {
        int pathBlockIndex = getBlockIndex(path);// 得到路径指向的目录的盘块的下标，-1表示未找到
        int blockNum = searchFreeBlock();
        int listLength;

        // 未找到路径所指向的文件夹
        if (pathBlockIndex == -1) {
            PromptWindow.showWindow("未找到路径所指向的文件夹，创建文件失败。");
            return null;
        }
        // 属性为只读的文件不能建立
        if (attribute % 2 == 1) {
            PromptWindow.showWindow("属性为只读的文件不能建立，创建文件失败。");
            return null;
        }
        // 命名重复
        if (searchName(name + "." + type, pathBlockIndex) != -1) {
            PromptWindow.showWindow("命名重复，创建文件失败。");
            return null;
        }

        // 磁盘空间已满
        if (blockNum == -1) {
            PromptWindow.showWindow("磁盘空间已满，创建文件失败。");
            return null;
        }

        // 上级目录已满
        if ((listLength = getListLength(pathBlockIndex)) >= 8) {
            PromptWindow.showWindow("一个文件夹最多创建8个文件或目录，创建文件失败。");
            return null;
        }

        // 修改上级目录 //感觉返回的FileMsg可以写在下面这个函数里。
        addElement(pathBlockIndex, listLength, name, type, attribute, (byte) blockNum, (byte) 1);

        // 更新FAT
        Disk.writeOnlyByte(0, 0, blockNum, (byte) 255);

        // 添加文件结束符'#'
        Disk.writeOnlyByte(blockNum, 0, 0, (byte) ('#'));

        return new CatalogueItem(Disk.readBlock(pathBlockIndex), listLength);
    }

    //②打开文件open_file（文件名，操作类型）{}
    public static int open_file(ArrayList<CatalogueItem> path, int operateType) throws CloneNotSupportedException {
        int pathBlockIndex = getBlockIndex(path);
        int index;
        int indexOfList;

        // 未找到路径所指向的文件夹
        if (pathBlockIndex == -1) {
            PromptWindow.showWindow("未找到路径所指向的文件，打开文件失败");
            return -1;
        }

        // 该文件已在打开文件列表中
        if ((index = openFile.searchOpenedFile(pathBlockIndex)) != -1) {
            // 打开方式一致
            if (operateType == openFile.getOftle(index).getOperationType()) {
                return index;
            } else if (operateType != openFile.getOftle(index).getOperationType()) {
                // 打开方式不一致。
                return -1;
            }
        }

        // 当前打开文件列表已满
        if (openFile.getLength() >= 5) {
            PromptWindow.showWindow("当前打开文件列表已满。打开文件失败");
            return -1;
        }

        // 把ArrayList<CatalogueItem> path深度复制，然后删掉最后一个节点，得到parentPath
        ArrayList<CatalogueItem> parentPath = Operation.getParentPath(path);

        buffer1 = Disk.readBlock(getBlockIndex(parentPath));// 读取该文件所在的文件夹
        indexOfList = searchName(path.get(path.size() - 1).getAllName(), getBlockIndex(parentPath));// 这个要打开的文件在上级目录的索引中的下标

        // 找不到该文件在其目录中的索引
        if (indexOfList == -1) {
            PromptWindow.showWindow("找不到该文件在其目录中的索引，打开文件失败");
            return -1;
        }
        // 不能以写方式打开只读文件
        if (Disk.readOnlyByte(getBlockIndex(parentPath), indexOfList, 5) % 2 == 1 && operateType == 1) {
            PromptWindow.showWindow("不能以写方式打开只读文件，打开文件失败");
            return -1;
        }

        // 添加打开文件目录项
        int pointer = indexOfList * 8;

        // int x=getFileLength(buffer1[pointer + 6]);//测试。得到文件的长度，用于填写文件打开表

        openFile.addFileOrCatalogue(getPathName(path), buffer1[pointer + 5], buffer1[pointer + 6],
                getFileLength(buffer1[pointer + 6]), operateType);

        if (operateType == 0)// 读文件直接把指针指向文件开头就好
        {
            openFile.changePointer(openFile.getLength() - 1, openFile.getOftle(openFile.getLength() - 1).getFileStartNumber(), 0);
        } else// 写文件把指针指向文件的末尾，就是指向‘#’
        {
            int nextDisk = openFile.getOftle(openFile.getLength() - 1).getFileStartNumber();// 起始盘块号
            int endBnum = 63;
            int endDnum;
            int flag = 1;

            do {
                endDnum = nextDisk;// endDnum放该文件的第一个盘块的盘块号
                buffer1 = Disk.readBlock(endDnum / 64);
                nextDisk = buffer1[endDnum % 64];// nextDisk放该文件的第二个盘块的盘块号
                buffer1 = Disk.readBlock(endDnum);// buffer1是读出的该文件的第一个盘块

                for (int i = 0; i < 64; i++) {
                    if (buffer1[i] == (byte) ('#')) {
                        endBnum = i;
                        flag = 0;
                        break;
                    }
                }
            } while (nextDisk != -1 && flag == 1);
            openFile.changePointer(openFile.getLength() - 1, endDnum, endBnum);// 设置打开的文件的写指针{endDnum,endBnum}
        }
        return openFile.getLength() - 1;// 返回已经打开的文件在文件打开表中的索引
    }

    private static int getFileLength(int startBlock) {
        int count = 0;
        int nextBlock;
        byte[] buffer = new byte[64];

        // 读取文件内容到str中
        while (startBlock != -1) {
            nextBlock = Disk.readOnlyByte(0, 0, startBlock);// 在FAT表中读到这个文件的下一个盘块的索引

            if (nextBlock == -1)// 即当前是文件的最后一个盘块
            {
                buffer = Disk.readBlock(startBlock);// 把文件的当前盘块读到缓冲区buffer
                count = count + getJingHaoIndex(buffer);
                return count;
            } else {
                count = count + 64;
            }
            startBlock = nextBlock;// 读下一盘块前，修改读指针指向下一盘块
        }
        return count;
    }


    // ③读文件read_file（文件名，读取长度）{}
    public static String read_file(ArrayList<CatalogueItem> path, int length)
            throws CloneNotSupportedException, UnsupportedEncodingException {
        int ofIndex;

        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        int listNum = getListNum(path.get(path.size() - 1).getStartBlock(),
                Disk.readBlock(parentPath.get(parentPath.size() - 1).getStartBlock()));

        byte[] fileBuffer = new byte[Disk.readOnlyByte(parentPath.get(parentPath.size() - 1).getStartBlock(), listNum,
                7) * 64];
        int fileBufferIndex = 0;// 指向fileContextByte读到哪里的指针

        // 打开错误 // ofIndex得到打开文件在打开表里的索引
        if ((ofIndex = open_file(path, 0)) < 0) {
            PromptWindow.showWindow("打开文件失败，请检查打开方式！");
            return null;
        }

        // 不需要读指针初始化,因为打开文件时，根据你输入的要读还是写文件，已经把读/写指针初始化了。
        int nextBlock;
        int lastBlockIndex = 0;

        // 读取文件内容到str中
        while (openFile.getOftle(ofIndex).getRead().getBlockNumber() != -1) {
            nextBlock = Disk.readOnlyByte(0, 0, openFile.getOftle(ofIndex).getRead().getBlockNumber());// 在FAT表中读到这个文件的下一个盘块的索引
            buffer1 = Disk.readBlock(openFile.getOftle(ofIndex).getRead().getBlockNumber());// 把文件的当前盘块读到缓冲区buffer1

            System.arraycopy(buffer1, 0, fileBuffer, fileBufferIndex, 64);
            fileBufferIndex = fileBufferIndex + 64;

            if (nextBlock == -1)// 即当前是文件的最后一个盘块
            {
                lastBlockIndex = getJingHaoIndex(buffer1);
            }
            openFile.changePointer(ofIndex, nextBlock, 0);// 读下一盘块前，修改读指针指向下一盘块
        }
        openFile.changePointer(ofIndex, path.get(path.size() - 1).getStartBlock(), 0);// 防止下次读打开时因为读指针没有指向开头出现问题，所以这里设置下指针
        int min = (length < (fileBufferIndex - 64 + lastBlockIndex) ? length : (fileBufferIndex - 64 + lastBlockIndex));
        byte[] fileContext = Arrays.copyOf(fileBuffer, min);

        return new String(fileContext, "utf8");
    }

    // ④写文件write_file（文件名，缓冲，写长度）{}
    public static void write_file(ArrayList<CatalogueItem> path, byte[] content, int length) throws CloneNotSupportedException {
        // open_file(path, 1)函数把打开文件会发生的错误都检查过了，不用再检查。

        // 打开文件
        int ofIndex;
        if ((ofIndex = open_file(path, 1)) < 0) {
            PromptWindow.showWindow("打开文件失败。写入失败。");
            return;
        }

        // 看看磁盘容量够不够
        int blockNum = openFile.getOftle(ofIndex).getWrite().getBlockNumber();
        buffer1 = Disk.readBlock(blockNum);

        int needBlocks = getNeedBlock(ofIndex, length);// 计算需要的盘块数
        int freeBlocks = getFreeBlocks();// 计算没被占用的盘块数
        if (needBlocks > freeBlocks) {
            PromptWindow.showWindow("磁盘容量不够。写入失败。");
            return;
        }
        // 这个函数改FAT表(分配空间)
        modifyFAT(needBlocks, blockNum);

        // 这里是往磁盘中写content和“#”，是先写入content，再写入“#”。但也可以把“#”加入到content这个字节数组里，一起写入。
        for (int i = 0; i < length; i++) {
            if (openFile.getOftle(ofIndex).getWrite().getBlockByteNumber() == 64) // 因为是0-63，64越界了
            {
                Disk.writeBlock(openFile.getOftle(ofIndex).getWrite().getBlockNumber(), buffer1);// 把写好的一个缓冲区存到磁盘

                openFile.changePointer(ofIndex, Disk.readOnlyByte(openFile.getOftle(ofIndex).getWrite().getBlockNumber() / 64, 0,
                        openFile.getOftle(ofIndex).getWrite().getBlockNumber() % 64), 0);// 修改文件写指针指向下一个盘块的开头

                i--;// 代表这里没有把content写入缓冲区，所以指向content的指针不后移
                clearArray(buffer1);// 写入一个盘块后，把缓冲区清空再写写字符到缓冲区。缓冲区最后要写到磁盘文件的。不清空也可以，因为有“#”来标志结束。但不清空比较难观察。所以这里清空下缓冲区再写下一盘块。
            } else {
                buffer1[openFile.getOftle(ofIndex).getWrite().getBlockByteNumber()] = content[i];// 这里把content的一个字节写入缓冲区
                openFile.getOftle(ofIndex).setLength(openFile.getOftle(ofIndex).getLength() + 1);// 文件的字节长度加一
                openFile.getOftle(ofIndex).getWrite().setBlockByteNumber(openFile.getOftle(ofIndex).getWrite().getBlockByteNumber() + 1);// 要写的文件的指针向后挪一字节
            }
        }

        // 文件打开表的文件占用字节数int length、写指针位置Pointer write都设置了。
        // 文件字节长度不包括结束符‘#’，文件写指针最终指向‘#’。
        if (openFile.getOftle(ofIndex).getWrite().getBlockByteNumber() == 64) // 因为是0-63，64越界了
        {
            Disk.writeBlock(openFile.getOftle(ofIndex).getWrite().getBlockNumber(), buffer1);// 把写好的一个缓冲区存到磁盘

            openFile.changePointer(ofIndex, Disk.readOnlyByte(openFile.getOftle(ofIndex).getWrite().getBlockNumber() / 64, 0,
                    openFile.getOftle(ofIndex).getWrite().getBlockNumber() % 64), 0);

            Disk.writeOnlyByte(openFile.getOftle(ofIndex).getWrite().getBlockNumber(), 0, 0, (byte) '#');// 要写的文件的指针向后挪一字节
            // 安全问题：'#'以字节形式存入，，可能有某个汉字的最后三分之一个字节和这个‘#’的字节是一样的。那么读文件就会只读一部分。
            // 也有可能用户输入了一个“#”字符，而utf8编码把“#”转为其ascii码，即一个字节，那么读出时，用户输入的这个“#”字符会被认为是文件结束符，那么读文件就会只读一部分。
            // 解决方法：不是用'#'来确定读到哪里停止，而是用文件的字节数来确定，读到哪里停止。但需要保存每个文件的文件字节数。没有实现。
        } else// 因为要让文件写指针最终指向‘#’，前面已经做到了，所以这里不再次设置Bunm
        {
            buffer1[openFile.getOftle(ofIndex).getWrite().getBlockByteNumber()] = (byte) ('#');
            Disk.writeBlock(openFile.getOftle(ofIndex).getWrite().getBlockNumber(), buffer1);// 把写好的一个缓冲区存到磁盘
        }

        // 测试
        // int dnum = ofList.getOftle(ofIndex).getWrite().getDnum();
        // int bnum = ofList.getOftle(ofIndex).getWrite().getBnum();

        // 设置添加后文件占用的盘块数
        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        int listNum = getListNum(path.get(path.size() - 1).getStartBlock(),
                Disk.readBlock(parentPath.get(parentPath.size() - 1).getStartBlock()));
        Disk.writeOnlyByte(parentPath.get(parentPath.size() - 1).getStartBlock(), listNum, 7,
                (byte) (Disk.readOnlyByte(parentPath.get(parentPath.size() - 1).getStartBlock(), listNum, 7)
                        + needBlocks));

    }

    // ⑤关闭文件 close_file（文件名）{}
    public static void close_file(ArrayList<CatalogueItem> path) {
        int pathBlockIndex = getBlockIndex(path);

        // 用文件的起始地址进行比对
        for (int i = 0; i < openFile.getLength(); i++) {
            if (openFile.getOftle(i).getFileStartNumber() == pathBlockIndex) {
                openFile.deleteFromOpenedFileTable(i);
                break;
            }
        }
    }

    // ⑥删除文件（delete_file）{}
    public static int delete_file(ArrayList<CatalogueItem> path) throws CloneNotSupportedException {
        int pathBlockIndex = getBlockIndex(path);
        // 未找到路径所指向的文件夹
        if (pathBlockIndex == -1)// 删除文件这里其实不会找不到路径所指向的文件。因为文件如果已经删掉了或没建立过，那么界面上就不会有这个文件的图标，自然无法用右键菜单来删除这个文件。
        {
            PromptWindow.showWindow("未找到路径所指向的文件，删除文件失败。");
            return -1;
        }

        // 该文件已在打开文件列表中
        if ((openFile.searchOpenedFile(pathBlockIndex)) != -1) {
            PromptWindow.showWindow("该文件已经打开，无法删除，删除文件失败。");
            return -1;
        }
        // 释放文件占用的数据块（在FAT表中把这一块清0）
        int blockNum;
        int nextNum = pathBlockIndex;
        while (nextNum > 0) {
            blockNum = nextNum;
            buffer1 = Disk.readBlock(blockNum / 64);
            nextNum = buffer1[blockNum % 64];
            Disk.freeBlock(blockNum);
        }

        // 修改上级目录
        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        buffer1 = Disk.readBlock(getBlockIndex(parentPath));// 得到放这个要删除的文件的索引的目录的盘块
        int listNum;
        // 目录中没有该目录项
        if ((listNum = getListNum(getBlockIndex(path), buffer1)) == -1) {
            PromptWindow.showWindow("找不到该文件在其目录中的索引，删除文件失败。");
            return -1;
        }
        deletElement(getBlockIndex(parentPath), listNum);
        return 1;
    }

    // ⑦显示文件内容（typefile）{}
    public static String typeFile(ArrayList<CatalogueItem> path) throws CloneNotSupportedException {
        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        buffer1 = Disk.readBlock(getBlockIndex(parentPath));
        int listNum = getListNum(getBlockIndex(path), buffer1);

        CatalogueItem msg = new CatalogueItem(buffer1, listNum);
        String attribute;
        if (msg.getAttribute() == 8) {
            attribute = "目录";
        } else if ((msg.getAttribute() % 2) == 1) {
            attribute = "只读文件";
        } else {
            attribute = "普通文件";
        }

        String s = "文件名: " + msg.getAllName() + "\n文件类型: " + msg.getAllName().substring(msg.getAllName().indexOf('.') + 1) + "\n文件属性: " + attribute + "\n文件起始盘块号: " + msg.getStartBlock()
                + "\n文件长度: " + msg.getLength() + " 块";
        return s;
    }

    // ⑧改变文件属性（change）{}
    public static int change(ArrayList<CatalogueItem> path, byte newAttribute) throws CloneNotSupportedException {
        if ((openFile.searchOpenedFile(getBlockIndex(path))) != -1) {
            PromptWindow.showWindow("该文件已经打开，无法改变文件属性！");
            return -1;
        }

        // 修改上级目录
        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        buffer1 = Disk.readBlock(getBlockIndex(parentPath));
        int listNum = getListNum(getBlockIndex(path), buffer1);
        Disk.writeOnlyByte(getBlockIndex(parentPath), listNum, 5, newAttribute);
        path.get(path.size() - 1).setAttribute(newAttribute);
        return 1;
    }

    // 返回输入的盘块内除‘#’外还有几个字符
    public static int getJingHaoIndex(byte[] buffer) {
        int i = 0;
        for (i = 0; i < 64; i++)// i最终是最后一个盘块指向'#'的索引
        {
            if (buffer[i] == (byte) ('#')) {
                break;
            }
        }
        return i;
    }

    public static int getFreeBlocks() {
        int count = 0;
        byte[] buffer = Disk.readBlock(0);
        for (int i = 0; i < 64; i++) {
            if (buffer[i] == 0) {
                count++;
            }
        }
        buffer = Disk.readBlock(1);
        for (int i = 0; i < 64; i++) {
            if (buffer[i] == 0) {
                count++;
            }
        }
        return count;
    }

    public static int getNeedBlock(int ofIndex, int length) {
        int needBlocks;
        int blockNum = openFile.getOftle(ofIndex).getWrite().getBlockNumber();
        byte[] buffer = Disk.readBlock(blockNum);
        int lastBlockIndex = getJingHaoIndex(buffer);
        int remain = 64 - lastBlockIndex - 1;// 当前盘块还能放多少字节 // -1是因为最后要放入“#”
        if (remain > length) {
            return 0;
        } else {
            int i = length - remain;// 当前盘块放不下的字节

            if (i % 64 == 0) {
                needBlocks = i / 64;
            } else {
                needBlocks = i / 64 + 1;
            }
        }

        return needBlocks;
    }

    public static void modifyFAT(int needBlocks, int blockNum) {
        int nextBlock;
        for (int i = 0; i <= needBlocks; i++) {
            nextBlock = searchFreeBlock();
            if (i != needBlocks) {
                Disk.writeOnlyByte(blockNum / 64, 0, blockNum % 64, (byte) nextBlock);
            } else {
                Disk.writeOnlyByte(blockNum / 64, 0, blockNum % 64, (byte) -1);
                // 其实可以直接：Disk.writeOnlyByte(0, 0, blockNum, (byte)
                // -1);，结果一样的。比如blockNum=65时。这两种写法都可以。
            }
            blockNum = nextBlock;
        }
    }

    // 把ArrayList<CatalogueItem> path深度复制，然后删掉最后一个节点，得到parentPath
    public static ArrayList<CatalogueItem> getParentPath(ArrayList<CatalogueItem> path) throws CloneNotSupportedException {
        ArrayList<CatalogueItem> parentPath = new ArrayList<CatalogueItem>();
        for (CatalogueItem msg : path) {
            parentPath.add((CatalogueItem) msg.clone());
        }
        parentPath.remove(path.size() - 1);
        return parentPath;
    }

    public static ArrayList<CatalogueItem> clonePath(ArrayList<CatalogueItem> path) throws CloneNotSupportedException {
        ArrayList<CatalogueItem> clonePath = new ArrayList<CatalogueItem>();
        for (CatalogueItem msg : path) {
            clonePath.add((CatalogueItem) msg.clone());
        }
        return clonePath;
    }


    private static void clearArray(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }
    }

    // 目录操作
    // ①建立目录（md）{}
    public static CatalogueItem md(String name, ArrayList<CatalogueItem> path) {
        // 属性直接为目录属性8

        // 找到父目录
        int pathBlockIndex = getBlockIndex(path);

        // 查找父目录是否存在
        if (pathBlockIndex == -1) {
            PromptWindow.showWindow("未找到路径所指向的文件夹，创建目录失败。");
            return null;
        }

        int blockNum;
        int listLength;

        // 命名重复
        if (searchName(name, pathBlockIndex) != -1) {
            PromptWindow.showWindow("命名重复，创建目录失败。");
            return null;
        }

        // 磁盘空间已满
        if ((blockNum = searchFreeBlock()) == -1) {
            PromptWindow.showWindow("磁盘空间已满，创建目录失败。");
            return null;
        }

        // 上级目录已满
        if ((listLength = getListLength(pathBlockIndex)) >= 8) {
            PromptWindow.showWindow("一个文件夹最多创建8个文件或目录，创建目录失败。");
            return null;
        }

        // 修改上级目录
        addElement(pathBlockIndex, listLength, name, "  ", (byte) 8, (byte) blockNum, (byte) 0);

        // 更新FAT
        Disk.writeOnlyByte(0, 0, blockNum, (byte) 255);

        // 把每个目录项设置为空，就是在每个目录项第一个字节加'$'
        for (int i = 0; i < 8; i++) {
            Disk.writeOnlyByte(blockNum, i, 0, (byte) '$');
        }

        return new CatalogueItem(Disk.readBlock(pathBlockIndex), listLength);
    }

    // 获取下标为blockNum的盘块中有哪些索引项，得到它们的起始盘块号
    public static ArrayList<Integer> getList(int blockNum) {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        byte[] buffer = Disk.readBlock(blockNum);
        for (int i = 0; i < 8; i++) {
            if (buffer[i * 8] == '$') {
                return arr;
            }
            arr.add((int) (buffer[i * 8 + 6]));
        }
        return arr;
    }

    // ②显示目录内容（dir）{}
    public static HashMap<CatalogueItem, Button> dir(ArrayList<CatalogueItem> path, HashMap<CatalogueItem, Button> map) {
        int pathBlockIndex = getBlockIndex(path);
        HashMap<CatalogueItem, Button> child = new HashMap<CatalogueItem, Button>();
        ArrayList<Integer> arr = getList(pathBlockIndex);
        Set<Map.Entry<CatalogueItem, Button>> entry = map.entrySet();
        if (arr != null) {
            for (Integer i : arr) {
                for (Map.Entry<CatalogueItem, Button> ent : entry) {
                    if (ent.getKey().getStartBlock() == i) {
                        child.put(ent.getKey(), ent.getValue());
                    }
                }
            }
        }

        return child;

    }

    //③删除空目录（rd）
    public static int rd(ArrayList<CatalogueItem> path) throws CloneNotSupportedException {
        // 释放目录占用的数据块（在FAT表中把这一块清0）
        int pathBlockIndex = getBlockIndex(path);
        if ((getListLength(pathBlockIndex)) > 0) {
            PromptWindow.showWindow("非空目录无法删除，删除失败。");
            return -1;
        }
        Disk.freeBlock(pathBlockIndex);

        // 修改上级目录
        ArrayList<CatalogueItem> parentPath = getParentPath(path);
        buffer1 = Disk.readBlock(getBlockIndex(parentPath));// 得到放这个要删除的文件的索引的目录的盘块
        int listNum;
        // 目录中没有该目录项
        if ((listNum = getListNum(getBlockIndex(path), buffer1)) == -1) {
            PromptWindow.showWindow("找不到该文件在其目录中的索引，删除文件失败。");
            return -1;
        }
        deletElement(getBlockIndex(parentPath), listNum);

        return 1;
    }
}
