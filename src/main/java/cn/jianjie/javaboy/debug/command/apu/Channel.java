package cn.jianjie.javaboy.debug.command.apu;

import cn.jianjie.javaboy.sound.Sound;
import cn.jianjie.javaboy.debug.Command;
import cn.jianjie.javaboy.debug.CommandPattern;

import java.util.HashSet;
import java.util.Set;

public class Channel implements Command {

    private static final CommandPattern PATTERN = CommandPattern.Builder
            .create("apu chan")
            .withDescription("enable given channels (1-4)")
            .build();

    private final Sound sound;

    public Channel(Sound sound) {
        this.sound = sound;
    }

    @Override
    public CommandPattern getPattern() {
        return PATTERN;
    }

    @Override
    public void run(CommandPattern.ParsedCommandLine commandLine) {
        Set<String> channels = new HashSet<>(commandLine.getRemainingArguments());
        for (int i = 1; i <= 4; i++) {
            sound.enableChannel(i - 1, channels.contains(String.valueOf(i)));
        }
    }
}
