package cn.jianjie.javaboy.memory.cart.rtc;

public interface Clock {

    long currentTimeMillis();

    Clock SYSTEM_CLOCK = new Clock() {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };
}
