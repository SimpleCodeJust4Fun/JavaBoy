package cn.jianjie.javaboy.memory;

import cn.jianjie.javaboy.AddressSpace;

public class Ram implements AddressSpace {

    private final int[] space;

    private final int length;

    private final int offset;

    public Ram(int offset, int length) {
        this.space = new int[length];
        this.length = length;
        this.offset = offset;
    }

    @Override
    public boolean accepts(int address) {
        return address >= offset && address < offset + length;
    }

    @Override
    public void setByte(int address, int value) {
        space[address - offset] = value;
    }

    @Override
    public int getByte(int address) {
        int index = address - offset;
        if (index < 0 || index >= space.length) {
            throw new IndexOutOfBoundsException("Address: " + address);
        }
        return space[index];
    }
}
