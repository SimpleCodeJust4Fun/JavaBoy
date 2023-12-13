package cn.jianjie.javaboy.integration.support;

import cn.jianjie.javaboy.AddressSpace;
import cn.jianjie.javaboy.Gameboy;
import cn.jianjie.javaboy.GameboyOptions;
import cn.jianjie.javaboy.controller.Controller;
import cn.jianjie.javaboy.cpu.Cpu;
import cn.jianjie.javaboy.cpu.Registers;
import cn.jianjie.javaboy.gpu.Display;
import cn.jianjie.javaboy.memory.cart.Cartridge;
import cn.jianjie.javaboy.serial.ByteReceiver;
import cn.jianjie.javaboy.serial.ByteReceivingSerialEndpoint;
import cn.jianjie.javaboy.sound.SoundOutput;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static cn.jianjie.javaboy.cpu.BitUtils.getLSB;
import static cn.jianjie.javaboy.cpu.BitUtils.getMSB;

public class SerialTestRunner implements ByteReceiver {

    private final Gameboy gb;

    private final StringBuilder text;

    private final OutputStream os;

    public SerialTestRunner(File romFile, OutputStream os) throws IOException {
        GameboyOptions options = new GameboyOptions(romFile);
        Cartridge cart = new Cartridge(options);
        gb = new Gameboy(options, cart, Display.NULL_DISPLAY, Controller.NULL_CONTROLLER, SoundOutput.NULL_OUTPUT, new ByteReceivingSerialEndpoint(this));
        text = new StringBuilder();
        this.os = os;
    }

    public String runTest() {
        int divider = 0;
        while (true) {
            gb.tick();
            if (++divider == 4) {
                if (isInfiniteLoop(gb)) {
                    break;
                }
                divider = 0;
            }
        }
        return text.toString();
    }

    @Override
    public void onNewByte(int b) {
        text.append((char) b);
        try {
            os.write(b);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isInfiniteLoop(Gameboy gb) {
        Cpu cpu = gb.getCpu();
        if (cpu.getState() != Cpu.State.OPCODE) {
            return false;
        }
        Registers regs = cpu.getRegisters();
        AddressSpace mem = gb.getAddressSpace();

        int i = regs.getPC();
        boolean found = true;
        for (int v : new int[]{0x18, 0xfe}) { // jr fe
            if (mem.getByte(i++) != v) {
                found = false;
                break;
            }
        }
        if (found) {
            return true;
        }

        i = regs.getPC();
        for (int v : new int[]{0xc3, getLSB(i), getMSB(i)}) { // jp pc
            if (mem.getByte(i++) != v) {
                return false;
            }
        }
        return true;
    }
}
