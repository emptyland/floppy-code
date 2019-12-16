package io.f04.code.ringcode.encode;

import io.f04.code.ringcode.constants.Constants;
import org.junit.Test;

import java.io.File;
import java.util.Random;

public class GeneratorTest {

    private Random rand = new Random();

    @Test
    public void testSanity() throws Exception {
        Generator g = new Generator();
        byte[] data = new byte[Constants.RING15_MAX_BYTES];
        rand.nextBytes(data);
        g.setData(data);
        g.toFile(new File("src/test/resources/2.png"), "png");
    }

    @Test
    public void testString() throws Exception {
        Generator g = new Generator();
        g.setRingColors(Constants.RING_COLORS);
        g.setData("Hello, World!");
        g.toFile(new File("src/test/resources/3.png"), "png");
    }

    @Test
    public void testRing31() throws Exception {
        Generator g = new Generator();
        g.setRingSize(Constants.RING31);
        g.setRingColors(Constants.RING_COLORS);
        byte[] data = new byte[Constants.RING31_MAX_BYTES];
        rand.nextBytes(data);
        g.setData(data);
        g.toFile(new File("src/test/resources/4.png"), "png");
    }

    @Test
    public void testMaxBits() {
        int bits = 0;
        for (int b : Constants.RING15_ANGLES) {
            bits += 360 / b;
        }
        System.out.println(bits);
    }
}
