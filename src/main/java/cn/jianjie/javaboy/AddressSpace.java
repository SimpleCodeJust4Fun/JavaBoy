package cn.jianjie.javaboy;

public interface AddressSpace {

    boolean accepts(int address);

    void setByte(int address, int value);

    int getByte(int address);

}
