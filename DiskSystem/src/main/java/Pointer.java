class Pointer {

    // 盘块号
    private int blockNumber;

    // 第几个字节
    private int blockByteNumber;

    public Pointer() {
        this.blockByteNumber = 0;
        this.blockNumber = 0;
    }

    public Pointer(int d, int b) {
        this.blockNumber = d;
        this.blockByteNumber = b;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getBlockByteNumber() {
        return blockByteNumber;
    }

    public void setBlockByteNumber(int blockByteNumber) {
        this.blockByteNumber = blockByteNumber;
    }
}