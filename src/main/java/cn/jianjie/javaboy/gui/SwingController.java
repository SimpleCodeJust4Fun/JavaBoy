package cn.jianjie.javaboy.gui;

import cn.jianjie.javaboy.controller.ButtonListener;
import cn.jianjie.javaboy.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SwingController implements Controller, KeyListener {

    private static final Logger LOG = LoggerFactory.getLogger(SwingController.class);

    private ButtonListener listener;

    private final Map<Integer, ButtonListener.Button> mapping;

    public SwingController(Properties properties) {
        EnumMap<ButtonListener.Button, Integer> buttonToKey = new EnumMap<>(ButtonListener.Button.class);

        buttonToKey.put(ButtonListener.Button.LEFT, KeyEvent.VK_LEFT);
        buttonToKey.put(ButtonListener.Button.RIGHT, KeyEvent.VK_RIGHT);
        buttonToKey.put(ButtonListener.Button.UP, KeyEvent.VK_UP);
        buttonToKey.put(ButtonListener.Button.DOWN, KeyEvent.VK_DOWN);
        buttonToKey.put(ButtonListener.Button.A, KeyEvent.VK_Z);
        buttonToKey.put(ButtonListener.Button.B, KeyEvent.VK_X);
        buttonToKey.put(ButtonListener.Button.START, KeyEvent.VK_ENTER);
        buttonToKey.put(ButtonListener.Button.SELECT, KeyEvent.VK_BACK_SPACE);

        for (String k : properties.stringPropertyNames()) {
            String v = properties.getProperty(k);
            if (k.startsWith("btn_") && v.startsWith("VK_")) {
                try {
                    ButtonListener.Button button = ButtonListener.Button.valueOf(k.substring(4).toUpperCase());
                    Field field = KeyEvent.class.getField(properties.getProperty(k));
                    if (field.getType() != int.class) {
                        continue;
                    }
                    int value = field.getInt(null);
                    buttonToKey.put(button, value);
                } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
                    LOG.error("Can't parse button configuration", e);
                }
            }
        }

        mapping = buttonToKey.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public void setButtonListener(ButtonListener listener) {
        this.listener = listener;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (listener == null) {
            return;
        }
        ButtonListener.Button b = getButton(e);
        if (b != null) {
            listener.onButtonPress(b);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (listener == null) {
            return;
        }
        ButtonListener.Button b = getButton(e);
        if (b != null) {
            listener.onButtonRelease(b);
        }
    }

    private ButtonListener.Button getButton(KeyEvent e) {
        return mapping.get(e.getKeyCode());
    }
}
