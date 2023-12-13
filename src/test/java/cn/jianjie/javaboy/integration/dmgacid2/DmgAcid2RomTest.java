package cn.jianjie.javaboy.integration.dmgacid2;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static cn.jianjie.javaboy.integration.support.RomTestUtils.*;

public class DmgAcid2RomTest {

    @Test
    public void testDmgAcid2() throws Exception {
        testRomWithImage(getPath("dmg-acid2.gb"));
    }

    private static Path getPath(String name) {
        return Paths.get("src/test/resources/roms/dmg-acid2", name);
    }
}
