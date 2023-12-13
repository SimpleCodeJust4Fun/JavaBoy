package cn.jianjie.javaboy;

import cn.jianjie.javaboy.controller.Controller;
import cn.jianjie.javaboy.controller.Joypad;
import cn.jianjie.javaboy.cpu.Cpu;
import cn.jianjie.javaboy.cpu.InterruptManager;
import cn.jianjie.javaboy.cpu.Registers;
import cn.jianjie.javaboy.cpu.SpeedMode;
import cn.jianjie.javaboy.debug.Console;
import cn.jianjie.javaboy.gpu.Display;
import cn.jianjie.javaboy.gpu.Gpu;
import cn.jianjie.javaboy.memory.*;
import cn.jianjie.javaboy.memory.cart.Cartridge;
import cn.jianjie.javaboy.serial.SerialEndpoint;
import cn.jianjie.javaboy.serial.SerialPort;
import cn.jianjie.javaboy.sound.Sound;
import cn.jianjie.javaboy.sound.SoundOutput;
import cn.jianjie.javaboy.timer.Timer;
import cn.jianjie.javaboy.memory.*;

import java.util.ArrayList;
import java.util.List;

public class Gameboy implements Runnable {

    public static final int TICKS_PER_SEC = 4_194_304;

    private final Gpu gpu;

    private final Mmu mmu;

    private final Cpu cpu;

    private final Timer timer;

    private final Dma dma;

    private final Hdma hdma;

    private final Display display;

    private final Sound sound;

    private final SerialPort serialPort;

    private final boolean gbc;

    private final SpeedMode speedMode;

    private final Console console;

    private volatile boolean doStop;

    private final List<Runnable> tickListeners = new ArrayList<>();

    private boolean requestedScreenRefresh;

    private boolean lcdDisabled;

    public Gameboy(GameboyOptions options, Cartridge rom, Display display, Controller controller, SoundOutput soundOutput, SerialEndpoint serialEndpoint) {
        this(options, rom, display, controller, soundOutput, serialEndpoint, null);
    }

    public Gameboy(GameboyOptions options, Cartridge rom, Display display, Controller controller, SoundOutput soundOutput, SerialEndpoint serialEndpoint, Console console) {
        this.display = display;
        gbc = rom.isGbc();
        speedMode = new SpeedMode();
        InterruptManager interruptManager = new InterruptManager(gbc);
        timer = new Timer(interruptManager, speedMode);
        mmu = new Mmu();

        Ram oamRam = new Ram(0xfe00, 0x00a0);
        dma = new Dma(mmu, oamRam, speedMode);
        gpu = new Gpu(display, interruptManager, dma, oamRam, gbc);
        hdma = new Hdma(mmu);
        sound = new Sound(soundOutput, gbc);
        serialPort = new SerialPort(interruptManager, serialEndpoint, gbc);
        mmu.addAddressSpace(rom);
        mmu.addAddressSpace(gpu);
        mmu.addAddressSpace(new Joypad(interruptManager, controller));
        mmu.addAddressSpace(interruptManager);
        mmu.addAddressSpace(serialPort);
        mmu.addAddressSpace(timer);
        mmu.addAddressSpace(dma);
        mmu.addAddressSpace(sound);

        mmu.addAddressSpace(new Ram(0xc000, 0x1000));
        if (gbc) {
            mmu.addAddressSpace(speedMode);
            mmu.addAddressSpace(hdma);
            mmu.addAddressSpace(new GbcRam());
            mmu.addAddressSpace(new UndocumentedGbcRegisters());
        } else {
            mmu.addAddressSpace(new Ram(0xd000, 0x1000));
        }
        mmu.addAddressSpace(new Ram(0xff80, 0x7f));
        mmu.addAddressSpace(new ShadowAddressSpace(mmu, 0xe000, 0xc000, 0x1e00));
        mmu.indexSpaces();

        cpu = new Cpu(mmu, interruptManager, gpu, display, speedMode);

        interruptManager.disableInterrupts(false);
        if (!options.isUsingBootstrap()) {
            initRegs();
        }

        this.console = console;
    }

    private void initRegs() {
        Registers r = cpu.getRegisters();

        r.setAF(0x01b0);
        if (gbc) {
            r.setA(0x11);
        }
        r.setBC(0x0013);
        r.setDE(0x00d8);
        r.setHL(0x014d);
        r.setSP(0xfffe);
        r.setPC(0x0100);
    }

    public void run() {
        doStop = false;
        while (!doStop) {
            tick();
        }
    }

    public void stop() {
        doStop = true;
    }

    public void tick() {
        Gpu.Mode newMode = tickSubsystems();
        if (newMode != null) {
            hdma.onGpuUpdate(newMode);
        }

        if (!lcdDisabled && !gpu.isLcdEnabled()) {
            lcdDisabled = true;
            display.frameIsReady();
            hdma.onLcdSwitch(false);
        } else if (newMode == Gpu.Mode.VBlank) {
            requestedScreenRefresh = true;
            display.frameIsReady();
        }

        if (lcdDisabled && gpu.isLcdEnabled()) {
            lcdDisabled = false;
            hdma.onLcdSwitch(true);
        } else if (requestedScreenRefresh && newMode == Gpu.Mode.OamSearch) {
            requestedScreenRefresh = false;
        }
        if (console != null) {
            console.tick();
        }
        tickListeners.forEach(Runnable::run);
    }

    private Gpu.Mode tickSubsystems() {
        timer.tick();
        if (hdma.isTransferInProgress()) {
            hdma.tick();
        } else {
            cpu.tick();
        }
        dma.tick();
        sound.tick();
        serialPort.tick();
        return gpu.tick();
    }

    public AddressSpace getAddressSpace() {
        return mmu;
    }

    public Cpu getCpu() {
        return cpu;
    }

    public SpeedMode getSpeedMode() {
        return speedMode;
    }

    public Gpu getGpu() {
        return gpu;
    }

    public void registerTickListener(Runnable tickListener) {
        tickListeners.add(tickListener);
    }

    public void unregisterTickListener(Runnable tickListener) {
        tickListeners.remove(tickListener);
    }

    public Sound getSound() {
        return sound;
    }
}
