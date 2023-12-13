package cn.jianjie.javaboy.cpu;

public class Flags {

    private static final int Z_POS = 7;

    private static final int N_POS = 6;

    private static final int H_POS = 5;

    private static final int C_POS = 4;

    private int flags;

    public int getFlagsByte() {
        return flags;
    }

    public boolean isZ() {
        return BitUtils.getBit(flags, Z_POS);
    }

    public boolean isN() {
        return BitUtils.getBit(flags, N_POS);
    }

    public boolean isH() {
        return BitUtils.getBit(flags, H_POS);
    }

    public boolean isC() {
        return BitUtils.getBit(flags, C_POS);
    }

    public void setZ(boolean z) {
        flags = BitUtils.setBit(flags, Z_POS, z);
    }

    public void setN(boolean n) {
        flags = BitUtils.setBit(flags, N_POS, n);
    }

    public void setH(boolean h) {
        flags = BitUtils.setBit(flags, H_POS, h);
    }

    public void setC(boolean c) {
        flags = BitUtils.setBit(flags, C_POS, c);
    }

    public void setFlagsByte(int flags) {
        BitUtils.checkByteArgument("flags", flags);
        this.flags = flags & 0xf0;
    }

    @Override
    public String toString() {
        return String.valueOf(isZ() ? 'Z' : '-') +
                (isN() ? 'N' : '-') +
                (isH() ? 'H' : '-') +
                (isC() ? 'C' : '-') +
                "----";
    }
}
