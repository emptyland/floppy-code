package io.f04.code.ringcode.picture;

import io.f04.code.ringcode.utils.OpenCVInitializer;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.junit.Assert.*;

public class InvisibleWatermarkTest {
    static {
        OpenCVInitializer.ensure();
    }

    @Test
    public void sanity() {
        Mat src = Imgcodecs.imread("src/test/resources/sample-10.jpeg", 1);
        assertFalse(src.empty());

        Mat dst = InvisibleWatermark.addWatermarkWithText(src, "TEST: Watermark");
        Imgcodecs.imwrite("src/test/resources/sample-10-mark.png", dst);

        dst = InvisibleWatermark.getWatermark(dst);
        Imgcodecs.imwrite("src/test/resources/sample-10-unmark.png", dst);

    }

    @Test
    public void sanity1() {
        Mat src = Imgcodecs.imread("src/test/resources/sample-10-mark-1.png", 1);

        Mat dst = InvisibleWatermark.getWatermark(src);
        Imgcodecs.imwrite("src/test/resources/sample-10-unmark-1.png", dst);

    }
}
