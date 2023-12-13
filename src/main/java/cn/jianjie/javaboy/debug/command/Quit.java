package cn.jianjie.javaboy.debug.command;

import cn.jianjie.javaboy.debug.Command;
import cn.jianjie.javaboy.debug.CommandPattern;

public class Quit implements Command {

    private static final CommandPattern PATTERN = CommandPattern.Builder
            .create("quit", "q")
            .withDescription("quits the emulator")
            .build();

    @Override
    public CommandPattern getPattern() {
        return PATTERN;
    }

    @Override
    public void run(CommandPattern.ParsedCommandLine commandLine) {
        System.exit(0);
    }
}
