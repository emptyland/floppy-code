package io.f04.code.ringcode.picture;

import io.f04.code.ringcode.decode.Reader;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class CapturerTest {

//    @Test
//    public void testSanity() throws Exception {
//        Capturer capturer = new Capturer();
//        capturer.setTest(true);
//        capturer.capture(new File("src/test/resources/sample-3.jpeg"));
//    }

    @Test
    public void testComplex0() throws Exception {
        Capturer capturer = new Capturer();
        //capturer.setTest(true);
        List<CapturedImage> images = capturer.capture(new File("src/test/resources/sample-0.jpeg"));
        assertEquals(1, images.size());

        CapturedImage image = images.get(0);
        assertNotNull(image);
        assertEquals(446, (int)image.approximateWidth());

        Reader reader = new Reader();
        //reader.setTest(true);

        int w = image.getImage().getWidth() / 2;
        reader.setRingWidth(image.approximateRingWidth());
        reader.setSampleTimes(23);
        reader.setFileName(capturer.getInputFile().toString());
        byte[] data = reader.read(image.getImage(), w, w);
        assertEquals("Hello, World!", new String(data, StandardCharsets.UTF_8));
    }

    @Test
    public void testComplex2() throws Exception {
        Capturer capturer = new Capturer();
        List<CapturedImage> images = capturer.capture(new File("src/test/resources/sample-2.jpeg"));
        assertEquals(1, images.size());

        CapturedImage image = images.get(0);
        assertNotNull(image);
        assertEquals(458, (int)image.approximateWidth());

        Reader reader = new Reader();
        //reader.setTest(true);

        int w = image.getImage().getWidth() / 2;
        reader.setRingWidth(image.approximateRingWidth());
        reader.setSampleTimes(23);
        reader.setFileName(capturer.getInputFile().toString());
        int ok = 0, fail = 0;
        for (int i = 0; i < 100; ++i) {
            try {
                byte[] data = reader.read(image.getImage(), w, w);
                if ("Hello, World!".equals(new String(data, StandardCharsets.UTF_8))) {
                    ok++;
                } else {
                    fail++;
                }
            } catch (Exception e) {
                fail++;
            }
        }
        System.out.println("ok: " + ok + " fail: " + fail);
    }

    @Test
    public void testComplex3() throws Exception {
        Capturer capturer = new Capturer();
        //capturer.setTest(true);
        List<CapturedImage> images = capturer.capture(new File("src/test/resources/sample-3.jpeg"));
        assertEquals(1, images.size());

        CapturedImage image = images.get(0);
        assertNotNull(image);
        assertEquals(422, (int)image.approximateWidth());

        Reader reader = new Reader();
        //reader.setTest(true);

        int w = image.getImage().getWidth() / 2;
        reader.setRingWidth(image.approximateRingWidth());
        reader.setSampleTimes(23);
        reader.setFileName(capturer.getInputFile().toString());
        byte[] data = reader.read(image.getImage(), w, w);
        assertEquals("Hello, World!", new String(data, StandardCharsets.UTF_8));
    }
}
