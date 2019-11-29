package io.f04.code.ringcode.decode;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.exceptions.BadMetadataException;
import io.f04.code.ringcode.utils.CodecUtil;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Reader {

    private int ringWidth = Constants.RING_WIDTH;

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
    public byte[] read(BufferedImage rawImage, int centerX, int centerY) throws BadMetadataException, BadDataSizeException {
        double angle = (2 * Math.PI) / 18;
        int r = ringWidth * 4;

        List<Boolean> bits = new ArrayList<>();
        Metadata metadata = readMetadata(rawImage, r, angle, centerX, centerY);
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
        for (int i = 0; i < Constants.RING15 - 1; i++) {
            angle = (2 * Math.PI) / (360.0 / Constants.RING15_ANGLES[i]);
            r = ringWidth * (4 + Constants.RING15 - i - 1);

            for (int j = 0; j < 360 / Constants.RING15_ANGLES[i]; j++) {
                bits.add(testSample(rawImage, r, j * angle, (j + 1) * angle, centerX, centerY));
            }
        }
        byte[] code = new byte[(bits.size() + 7) / 8];
        for (int i = 0; i < bits.size(); i++) {
            code[i / 8] |= bits.get(i) ? (1 << (i % 8)) : 0;
        }

        Decoder decoder = new Decoder(metadata.getErrorCorrectionLevel());
        byte[] data = decoder.decode(code, CodecUtil.makeType2(Constants.RING15_MAX_BYTES), Constants.RING15_MAX_BYTES);
        return Arrays.copyOf(data, metadata.dataSize);
    }

//    private void readRing(BufferedImage rawImage, int r, double angle, int centerX, int centerY, int limit, List<Boolean> bits) {
//        for (int i = 0; i < limit; ++i) {
//            bits.add(testSample(rawImage, r, i * angle, (i + 1) * angle, centerX, centerY));
//        }
//    }

    private Metadata readMetadata(BufferedImage rawImage, int r, double angle, int centerX, int centerY) throws BadMetadataException {
        byte[] metadata = new byte[(18 + 7) / 8];
        for (int i = 0; i < 18; ++i) {
            boolean sample = testSample(rawImage, r, i * angle, (i + 1) * angle, centerX, centerY);
            metadata[i / 8] |= sample ? (1 << i % 8) : 0;
        }
        for (int i = 0; i < metadata.length - 1; i++) {
            metadata[i] ^= 0xaa;
        }
        metadata[metadata.length - 1] ^= 0x2;
        ByteBuffer buffer = ByteBuffer.wrap(metadata);
        int size = buffer.getShort();
        if (size < 0) {
            throw new BadMetadataException("bad data size");
        }

        int level = buffer.get();
        if (level < 0 || level >= ErrorCorrectionLevel.values().length) {
            throw new BadMetadataException("bad ErrorCorrectionLevel (" + level + ")");
        }

        return new Metadata(ErrorCorrectionLevel.values()[level], size);
    }

    private boolean testSample(BufferedImage rawImage, int r, double beginAngle, double endAngle, int centerX, int centerY) {
        Random rand = new Random();
        int countOne = 0, countZero = 0;
        for (int i = 0; i < sampleTimes; i++) {
            double t = r + 1 + rand.nextInt(ringWidth - 1);
            //double t = r + 3;
            double angle = nextDouble(rand, beginAngle, endAngle);
            assert angle >= beginAngle && angle <= endAngle;
            int x = (int)(centerX + t * Math.cos(angle));
            int y = (int)(centerY - t * Math.sin(angle));

            boolean one = rawImage.getRGB(x, y) != 0xffffffff;
            System.out.println(String.format("(%s, %s) %s", x, y, one));

            if (one) {
                countOne++;
            } else {
                countZero++;
            }
        }
        System.out.println("---------------");
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

    private static class Metadata {
        private ErrorCorrectionLevel errorCorrectionLevel;
        private int dataSize;

        Metadata(ErrorCorrectionLevel errorCorrectionLevel, int dataSize) {
            this.errorCorrectionLevel = errorCorrectionLevel;
            this.dataSize = dataSize;
        }

        public ErrorCorrectionLevel getErrorCorrectionLevel() {
            return errorCorrectionLevel;
        }

        public int getDataSize() {
            return dataSize;
        }
    }
}
