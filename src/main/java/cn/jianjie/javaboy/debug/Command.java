package cn.jianjie.javaboy.debug;

public interface Command {

    CommandPattern getPattern();

    void run(CommandPattern.ParsedCommandLine commandLine);
}
