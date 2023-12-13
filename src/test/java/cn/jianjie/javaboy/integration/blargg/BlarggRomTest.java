package cn.jianjie.javaboy.integration.blargg;

import cn.jianjie.javaboy.integration.support.RomTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlarggRomTest {

    @Test
    public void testCgbSound() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("cgb_sound.gb"));
    }

    @Test
    public void testCpuInstrs() throws IOException {
        RomTestUtils.testRomWithSerial(getPath("cpu_instrs.gb"));
    }

    @Test
    public void testDmgSound2() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("dmg_sound-2.gb"));
    }

    @Test
    public void testHaltBug() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("halt_bug.gb"));
    }

    @Test
    public void testInstrTiming() throws IOException {
        RomTestUtils.testRomWithSerial(getPath("instr_timing.gb"));
    }

    @Test
    @Ignore
    public void testInterruptTime() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("interrupt_time.gb"));
    }

    @Test
    public void testMemTiming2() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("mem_timing-2.gb"));
    }

    @Test
    public void testOamBug2() throws IOException {
        RomTestUtils.testRomWithMemory(getPath("oam_bug-2.gb"));
    }

    private static Path getPath(String name) {
        return Paths.get("src/test/resources/roms/blargg", name);
    }
}
