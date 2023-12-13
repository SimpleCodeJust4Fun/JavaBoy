package cn.jianjie.javaboy.debug;

import cn.jianjie.javaboy.debug.command.Quit;
import cn.jianjie.javaboy.debug.command.ShowHelp;
import cn.jianjie.javaboy.debug.command.apu.Channel;
import cn.jianjie.javaboy.debug.command.cpu.ShowOpcode;
import cn.jianjie.javaboy.debug.command.cpu.ShowOpcodes;
import cn.jianjie.javaboy.debug.command.ppu.ShowBackground;
import cn.jianjie.javaboy.Gameboy;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Console implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    private final Deque<CommandExecution> commandBuffer = new ArrayDeque<>();

    private final Semaphore semaphore = new Semaphore(0);

    private volatile boolean isStarted;

    private List<Command> commands;

    public Console() {
    }

    public void init(Gameboy gameboy) {
        commands = new ArrayList<>();
        commands.add(new ShowHelp(commands));
        commands.add(new ShowOpcode());
        commands.add(new ShowOpcodes());
        commands.add(new Quit());

        commands.add(new ShowBackground(gameboy, ShowBackground.Type.WINDOW));
        commands.add(new ShowBackground(gameboy, ShowBackground.Type.BACKGROUND));
        commands.add(new Channel(gameboy.getSound()));

        commands.sort(Comparator.comparing(c -> c.getPattern().getCommandNames().get(0)));
    }

    @Override
    public void run() {
        isStarted = true;

        LineReader lineReader = LineReaderBuilder
                .builder()
                .build();

        while (true) {
            try {
                String line = lineReader.readLine("JavaBoy> ");
                for (Command cmd : commands) {
                    if (cmd.getPattern().matches(line)) {
                            CommandPattern.ParsedCommandLine parsed = cmd.getPattern().parse(line);
                            commandBuffer.add(new CommandExecution(cmd, parsed));
                            semaphore.acquire();
                    }
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } catch (UserInterruptException e) {
                System.exit(0);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
                break;
            }
        }
    }

    public void tick() {
        if (!isStarted) {
            return;
        }

        while (!commandBuffer.isEmpty()) {
            commandBuffer.poll().run();
            semaphore.release();
        }
    }

    private static class CommandExecution {

        private final Command command;

        private final CommandPattern.ParsedCommandLine arguments;

        public CommandExecution(Command command, CommandPattern.ParsedCommandLine arguments) {
            this.command = command;
            this.arguments = arguments;
        }

        public void run() {
            command.run(arguments);
        }
    }
}
