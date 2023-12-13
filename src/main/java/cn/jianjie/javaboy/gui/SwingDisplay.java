package cn.jianjie.javaboy.gui;

import cn.jianjie.javaboy.gpu.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SwingDisplay extends JPanel implements Display, Runnable {

    private final BufferedImage img;

    public static final int[] COLORS = new int[]{0xe6f8da, 0x99c886, 0x437969, 0x051f2a};

    public static final int[] COLORS_GRAYSCALE = new int[]{0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000};

    private final int[] rgb;

    private final int[] waitingFrame;

    private boolean enabled;

    private final int scale;

    private boolean doStop;

    private boolean frameIsWaiting;

    private final boolean grayscale;

    private int pos;

    public SwingDisplay(int scale, boolean grayscale) {
        super();
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();
        img = gfxConfig.createCompatibleImage(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        rgb = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
        waitingFrame = new int[rgb.length];
        this.scale = scale;
        this.grayscale = grayscale;
    }

    @Override
    public void putDmgPixel(int color) {
        rgb[pos++] = grayscale ? COLORS_GRAYSCALE[color] : COLORS[color];
        pos = pos % rgb.length;
    }

    @Override
    public void putColorPixel(int gbcRgb) {
        rgb[pos++] = Display.translateGbcRgb(gbcRgb);
    }

    @Override
    public synchronized void frameIsReady() {
        pos = 0;
        if (frameIsWaiting) {
            return;
        }
        frameIsWaiting = true;
        System.arraycopy(rgb, 0, waitingFrame, 0, rgb.length);
        notify();
    }

    @Override
    public void enableLcd() {
        enabled = true;
    }

    @Override
    public void disableLcd() {
        enabled = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        if (enabled) {
            g2d.drawImage(img, 0, 0, DISPLAY_WIDTH * scale, DISPLAY_HEIGHT * scale, null);
        } else {
            g2d.setColor(new Color(COLORS[0]));
            g2d.drawRect(0, 0, DISPLAY_WIDTH * scale, DISPLAY_HEIGHT * scale);
        }
        g2d.dispose();
    }

    @Override
    public void run() {
        doStop = false;
        frameIsWaiting = false;
        enabled = true;
        while (!doStop) {
            synchronized (this) {
                if (frameIsWaiting) {
                    img.setRGB(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, waitingFrame, 0, DISPLAY_WIDTH);
                    validate();
                    repaint();
                    frameIsWaiting = false;
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void stop() {
        doStop = true;
    }
}
