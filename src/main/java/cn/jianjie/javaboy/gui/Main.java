package cn.jianjie.javaboy.gui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.application.name", "JavaBoy");
        new Emulator(args, loadProperties()).run();
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        File propFile = new File(new File(System.getProperty("user.home")), ".JavaBoy.properties");
        if (propFile.exists()) {
            try (FileReader reader = new FileReader(propFile)) {
                props.load(reader);
            }
        }
        return props;
    }

}
