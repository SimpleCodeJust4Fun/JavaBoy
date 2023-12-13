package cn.jianjie.javaboy.integration.blargg.individual;

import cn.jianjie.javaboy.integration.support.ParametersProvider;
import cn.jianjie.javaboy.integration.support.RomTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OamBug2Test {

    private final Path rom;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws IOException {
        return ParametersProvider.getParameters("blargg/oam_bug-2");
    }

    public OamBug2Test(String name, Path rom) {
        this.rom = rom;
    }

    @Test
    public void test() throws IOException {
        RomTestUtils.testRomWithMemory(rom);
    }
}
