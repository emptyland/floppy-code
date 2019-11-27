package io.f04.code.ringcode.decode;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.assertEquals;

public class ReaderTest {

    @Test
    public void testSanity() throws Exception {
        Reader reader = new Reader();
        BufferedImage image = ImageIO.read(new File("src/test/resources/3-b1.png"));
        assertEquals(280, image.getHeight());
        assertEquals(280, image.getWidth());
        reader.read(image, 140, 140);
    }
}
