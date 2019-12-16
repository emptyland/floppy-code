package io.f04.code.ringcode.decode;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.exceptions.BadMetadataException;
import io.f04.code.ringcode.utils.CodecUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Reader {

    /**
     * ring width
     */
    private double ringWidth = Constants.RING_WIDTH;

    /**
     * sample times
     */
    private int sampleTimes = 17;

    /**
     * test mode
     */
    private boolean test = false;

    /**
     * test mode output file name
     */
    private String fileName;

    public double getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(double ringWidth) {
        this.ringWidth = ringWidth;
    }

    public int getSampleTimes() {
        return sampleTimes;
    }

    public void setSampleTimes(int sampleTimes) {
        this.sampleTimes = sampleTimes;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] read(BufferedImage rawImage, int centerX, int centerY) throws BadMetadataException, BadDataSizeException, IOException {
        try {
            return breakableRead(rawImage, centerX, centerY);
        } catch (BadMetadataException | BadDataSizeException | IOException e) {
            throw e;
        } finally {
            if (test) {
                ImageIO.write(rawImage, "png", new File(fileName + "-r1.png"));
            }
        }
    }

    // 25
    private byte[] breakableRead(BufferedImage rawImage, int centerX, int centerY) throws BadMetadataException, BadDataSizeException, IOException {
        double angle = (2 * Math.PI) / 18;
        int r = (int) (ringWidth * 4);

        List<Boolean> bits = new ArrayList<>();
        Metadata metadata = readMetadata(rawImage, r, angle, centerX, centerY);
        for (int i = 0; i < Constants.RING15 - 1; i++) {
            angle = (2 * Math.PI) / (360.0 / Constants.RING15_ANGLES[i]);
            r = (int) (ringWidth * (4 + Constants.RING15 - i - 1));

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

    private Metadata readMetadata(BufferedImage rawImage, int r, double angle, int centerX, int centerY) throws BadMetadataException, IOException {
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
        Graphics2D gs = null;
        List<Point> samplePoints = null;
        if (test) {
            samplePoints = new ArrayList<>();
            gs = rawImage.createGraphics();
            gs.setComposite(AlphaComposite.Src);
            gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gs.setStroke(new BasicStroke(1));
            gs.setColor(Color.GREEN);
            gs.draw(new Ellipse2D.Float(centerX - r, centerY - r, r * 2, r * 2));
        }

        Random rand = new Random();
        int countOne = 0, countZero = 0;
        for (int i = 0; i < sampleTimes; i++) {
            double t = r + 1 + rand.nextInt((int) ringWidth - 1);
            //double t = r + 3;
            double angle = nextDouble(rand, beginAngle, endAngle);
            assert angle >= beginAngle && angle <= endAngle;
            int x = (int) (centerX + t * Math.cos(angle));
            int y = (int) (centerY - t * Math.sin(angle));

            if (gs != null) {
                samplePoints.add(new Point(x, y));
            }


            boolean one = rawImage.getRGB(x, y) != 0xffffffff;
            if (test) {
                System.out.println(String.format("(%s, %s) %s", x, y, one));
            }

            if (one) {
                countOne++;
            } else {
                countZero++;
            }
        }
        if (test) {
            System.out.println("---------------");
        }
        if (gs != null) {
            for (Point pt : samplePoints) {
                int x = pt.x, y = pt.y;
                gs.draw(new Line2D.Float(x - 1, y, x + 1, y));
                gs.draw(new Line2D.Float(x, y - 1, x, y + 1));
            }
            gs.dispose();
        }
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
