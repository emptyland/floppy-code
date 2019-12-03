package io.f04.code.ringcode.decode;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReaderTest {

    @Test
    public void testSanity() throws Exception {
        Reader reader = new Reader();
        BufferedImage image = ImageIO.read(new File("src/test/resources/3-b1.png"));
        assertEquals(280, image.getHeight());
        assertEquals(280, image.getWidth());
        byte[] data = reader.read(image, 140, 140);
        assertEquals("Hello, World!", new String(data, StandardCharsets.UTF_8));
    }

    @Test
    public void testProcessed() throws Exception {
        Reader reader = new Reader();
        BufferedImage image = ImageIO.read(new File("src/test/resources/sample-0-0-1-2.png"));
        assertEquals(640, image.getHeight());
        assertEquals(640, image.getWidth());
        //reader.setTest(true);
        reader.setRingWidth(16.7f);
        reader.setSampleTimes(5);
        byte[] data = reader.read(image, 320, 318);
        assertEquals("Hello, World!", new String(data, StandardCharsets.UTF_8));
    }
}
