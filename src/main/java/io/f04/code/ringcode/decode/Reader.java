package io.f04.code.ringcode.decode;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Reader {

    private int ringWidth = 5;

    private int sampleTimes = 5;

    public int getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(int ringWidth) {
        this.ringWidth = ringWidth;
    }

    public int getSampleTimes() {
        return sampleTimes;
    }

    public void setSampleTimes(int sampleTimes) {
        this.sampleTimes = sampleTimes;
    }

    // 25
    public byte[] read(BufferedImage rawImage, int centerX, int centerY) {
        double angle = (2 * Math.PI) / 18;
        int r = ringWidth * 6;

        //List<Boolean> sample = new ArrayList<>();
        readMetadata(rawImage, r, angle, centerX, centerY);
        //Math.sin()
        return null;
    }

    private int readMetadata(BufferedImage rawImage, int r, double angle, int centerX, int centerY) {
        //List<Boolean> sample = new ArrayList<>();
        byte[] metadata = new byte[(18 + 7) / 8];
        //Arrays.fill(metadata, (byte)0xaa);
        for (int i = 0; i < 18; ++i) {
            boolean sample = testSample(rawImage, r, i * angle, (i + 1) * angle, centerX, centerY);
            metadata[i / 8] |= sample ? (1 << i % 8) : 0;
        }
        for (int i = 0; i < metadata.length; i++) {
            metadata[i] ^= 0xaa;
        }
        int code = ByteBuffer.wrap(metadata).getShort();
        System.out.println(String.format("%s | %d", Arrays.toString(metadata), code));
        return code;
    }

    private boolean testSample(BufferedImage rawImage, int r, double beginAngle, double endAngle, int centerX, int centerY) {
        Random rand = new Random();
        int countOne = 0, countZero = 0;
        for (int i = 0; i < sampleTimes; i++) {
            double t = r + rand.nextInt(4);
            //double t = r + 3;
            double angle = nextDouble(rand, beginAngle, endAngle);
            assert angle >= beginAngle && angle <= endAngle;
            int x = (int)(centerX + t * Math.cos(angle));
            int y = (int)(centerY - t * Math.sin(angle));

            boolean one = rawImage.getRGB(x, y) != 0xffffffff;
            //System.out.println(String.format("(%s, %s) %s", x, y, one));

            if (one) {
                countOne++;
            } else {
                countZero++;
            }
        }
        //System.out.println("---------------");
        return countOne > countZero;
    }

    private static double nextDouble(Random rand, double from, double until) {
        double size = until - from;
        double r = 0;
        if (Double.isInfinite(size) && Double.isFinite(from) && Double.isFinite(until)) {
            double r1 = rand.nextDouble() * (until / 2 - from / 2);
            r = from + r1 + r1;
        } else {
            r = from + rand.nextDouble() * size;
        }

        return r >= until ? until - Double.MIN_VALUE : r;
    }

//    public open fun nextDouble(from: Double, until: Double): Double {
//        checkRangeBounds(from, until)
//        val size = until - from
//        val r = if (size.isInfinite() && from.isFinite() && until.isFinite()) {
//            val r1 = nextDouble() * (until / 2 - from / 2)
//            from + r1 + r1
//        } else {
//            from + nextDouble() * size
//        }
//        return if (r >= until) until.nextDown() else r
//    }
}
