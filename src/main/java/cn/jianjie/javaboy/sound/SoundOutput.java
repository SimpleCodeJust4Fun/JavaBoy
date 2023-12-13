package cn.jianjie.javaboy.sound;

public interface SoundOutput {

    void start();

    void stop();

    void play(int left, int right);

    SoundOutput NULL_OUTPUT = new SoundOutput() {
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void play(int left, int right) {
        }
    };
}
