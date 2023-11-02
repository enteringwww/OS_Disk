import java.io.RandomAccessFile;

public class Disk {

    // 磁盘初始化
    public static void start() {
        // 每个物理块大小为64字节，用字节数组模拟
        byte[] buffer = new byte[64];
        for (int i = 0; i < 64; i++) {
            buffer[i] = 0;
        }
        //模拟磁盘有128个物理块
        for (int i = 0; i < 128; i++) {
            // 初始化盘块为全0
            writeBlock(i, buffer);
        }
        // 文件分配表（FAT）的前3个字节填入255;      i = 0、1代表 文件分配表 的结束，2代表 根目录 的结束
        writeOnlyByte(0, 0, 0, (byte) 255);
        writeOnlyByte(0, 0, 1, (byte) 255);
        writeOnlyByte(0, 0, 2, (byte) 255);

        // 盘块2根目录的8个目录项填入“$”      盘块2作为根目录。
        for (int i = 0; i < 8; i++) {
            Disk.writeOnlyByte(2, i, 0, (byte) '$');
        }
    }

    // 从index盘块中读取64字节数据到buffer数组里
    public static byte[] readBlock(int indexOfSector) {
        byte[] buffer = new byte[64];
        try (RandomAccessFile randomAccessFileaf = new RandomAccessFile("data.txt", "rw")) {
            long pointer = indexOfSector * 64L;
            for (int i = 0; i < 64; i++) {
                randomAccessFileaf.seek(pointer + i);
                buffer[i] = randomAccessFileaf.readByte();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    // 把buffer里的64个字节写入到下标为index的盘块里
    public static void writeBlock(int indexOfSector, byte[] buffer) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("data.txt", "rw"))//这句new的时候如果没有该文件会自动创建的
        {
            long pointer = indexOfSector * 64L;
            for (int i = 0; i < 64; i++) {
                // 找到位置
                randomAccessFile.seek(pointer + i);

                // 写入
                randomAccessFile.writeByte(buffer[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 往块里写入单个字节
    public static void writeOnlyByte(int blockNumber, int listNumber, int byteNumber, byte word) {
        try (RandomAccessFile raf = new RandomAccessFile("data.txt", "rw")) {
            long pointer = blockNumber * 64 + listNumber * 8 + byteNumber;
            raf.seek(pointer);
            raf.writeByte(word);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从块里读出单个字节
    public static byte readOnlyByte(int blockNumber, int listNumber, int byteNumber) {
        byte b = -2;
        try (RandomAccessFile raf = new RandomAccessFile("data.txt", "rw")) {
            long pointer = blockNumber * 64 + listNumber * 8 + byteNumber;// 按字节来找的。比如（0，0，3）得到FAT表中的字节3（字节0开始）
            raf.seek(pointer);
            b = raf.readByte();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }


    // 回收盘块，即在磁盘0，1块的FAT里，改为0
    public static void freeBlock(int index) {
        byte[] buffer = readBlock(index / 64);
        buffer[index % 64] = 0;
        writeBlock(index / 64, buffer);
    }
}
