package io.f04.code.ringcode.picture;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class CapturerTest {

    @Test
    public void testSanity() throws Exception {
        Capturer capturer = new Capturer();
        capturer.setTest(true);
        capturer.capture(new File("src/test/resources/sample-1.jpeg"));
    }

    @Test
    public void testComplex() throws Exception {
        Capturer capturer = new Capturer();
        capturer.setTest(true);
        capturer.capture(new File("src/test/resources/sample-0.jpeg"));
    }

    @Test
    public void testComplex2() throws Exception {
        Capturer capturer = new Capturer();
        capturer.setTest(true);
        List<CapturedImage> images = capturer.capture(new File("src/test/resources/sample-2.jpeg"));
        assertEquals(1, images.size());

        CapturedImage image = images.get(0);
        assertNotNull(image);
        assertEquals(458, (int)image.getR()[0]);
    }
}
