package cn.jianjie.javaboy.gui;

import cn.jianjie.javaboy.controller.Controller;
import cn.jianjie.javaboy.debug.Console;
import cn.jianjie.javaboy.memory.cart.Cartridge;
import cn.jianjie.javaboy.serial.SerialEndpoint;
import cn.jianjie.javaboy.sound.SoundOutput;
import cn.jianjie.javaboy.Gameboy;
import cn.jianjie.javaboy.GameboyOptions;
import cn.jianjie.javaboy.gpu.Display;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Emulator {

    private static final int SCALE = 2;

    private final GameboyOptions options;

    private final Cartridge rom;

    private final AudioSystemSoundOutput sound;

    private final SwingDisplay display;

    private final SwingController controller;

    private final Gameboy gameboy;

    public Emulator(String[] args, Properties properties) throws IOException {
        options = parseArgs(args);
        rom = new Cartridge(options);
        SerialEndpoint serialEndpoint = SerialEndpoint.NULL_ENDPOINT;
        Console console = options.isDebug() ? new Console() : null;
        if (console != null) {
            new Thread(console).start();
        }
        if (options.isHeadless()) {
            sound = null;
            display = null;
            controller = null;
            gameboy = new Gameboy(options, rom, Display.NULL_DISPLAY, Controller.NULL_CONTROLLER, SoundOutput.NULL_OUTPUT, serialEndpoint, console);
        } else {
            sound = new AudioSystemSoundOutput();
            display = new SwingDisplay(SCALE, options.isGrayscale());
            controller = new SwingController(properties);
            gameboy = new Gameboy(options, rom, display, controller, sound, serialEndpoint, console);
        }
        if (console != null) {
            console.init(gameboy);
        }
    }

    private static GameboyOptions parseArgs(String[] args) {
        if (args.length == 0) {
            GameboyOptions.printUsage(System.out);
            System.exit(0);
            return null;
        }
        try {
            return createGameboyOptions(args);
        } catch(IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println();
            GameboyOptions.printUsage(System.err);
            System.exit(1);
            return null;
        }
    }

    private static GameboyOptions createGameboyOptions(String[] args) {
        Set<String> params = new HashSet<>();
        Set<String> shortParams = new HashSet<>();
        String romPath = null;
        for (String a : args) {
            if (a.startsWith("--")) {
                params.add(a.substring(2));
            } else if (a.startsWith("-")) {
                shortParams.add(a.substring(1));
            } else {
                romPath = a;
            }
        }
        if (romPath == null) {
            throw new IllegalArgumentException("ROM path hasn't been specified");
        }
        File romFile = new File(romPath);
        if (!romFile.exists()) {
            throw new IllegalArgumentException("The ROM path doesn't exist: " + romPath);
        }
        return new GameboyOptions(romFile, params, shortParams);
    }

    public void run() throws Exception {
        if (options.isHeadless()) {
            gameboy.run();
        } else {
            System.setProperty("sun.java2d.opengl", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(this::startGui);
        }
    }

    private void startGui() {
        display.setPreferredSize(new Dimension(160 * SCALE, 144 * SCALE));

        JFrame mainWindow = new JFrame("JavaBoy: " + rom.getTitle());
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                stopGui();
            }
        });
        mainWindow.setLocationRelativeTo(null);

        mainWindow.setContentPane(display);
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
        mainWindow.pack();

        mainWindow.addKeyListener(controller);

        new Thread(display).start();
        new Thread(sound).start();
        new Thread(gameboy).start();
    }

    private void stopGui() {
        display.stop();
        sound.stopThread();
        gameboy.stop();
        rom.flushBattery();
        System.exit(0);
    }
}
