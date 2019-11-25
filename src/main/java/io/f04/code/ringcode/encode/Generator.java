package io.f04.code.ringcode.encode;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.utils.MaskUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Generator {

    private byte[] data;

    private int ringSize = Constants.RING15;

    private List<Color> ringColors = new ArrayList<Color>() {
        {
            add(new Color(0x00, 0x00, 0x00));
        }
    };

    public void setData(byte[] data) {
        Constants.RingProfile profile = Constants.RINGS_PROFILE.get(ringSize);
        if (profile == null || data.length > profile.getMaxBytes()) {
            throw new IllegalArgumentException("bad data size.");
        }
        this.data = data;
    }

    public void setData(String data) throws UnsupportedEncodingException {
        setData(data.getBytes("UTF-8"));
    }

    public int getRingSize() {
        return ringSize;
    }

    public void setRingSize(int ringSize) {
        this.ringSize = ringSize;
    }

    public List<Color> getRingColors() {
        return ringColors;
    }

    public void setRingColors(List<Color> ringColors) {
        this.ringColors = ringColors;
    }

    public void toFile(File outFile, String format) throws IOException {
        BufferedImage bi = toBufferedImage();
        ImageIO.write(bi, format, outFile);
    }

    public BufferedImage toBufferedImage() {
        Encoder encoder = new Encoder();
        Constants.RingProfile profile = Constants.RINGS_PROFILE.get(ringSize);
        byte[] mask = MaskUtil.makeType2(profile.getMaxBytes());
        byte[] code = encoder.encode(data, mask, profile.getMaxBytes());
        BufferedImage image = new BufferedImage(profile.getImageSize(), profile.getImageSize(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        prepareDraw(gs, profile.getImageSize());
        drawAnchors(gs, profile.getImageSize());
        drawData(gs, profile.getImageSize(), profile.getAngles(), code);
        gs.dispose();
        return image;
    }

    private void prepareDraw(Graphics2D gs, int imageSize) {
        gs.setComposite(AlphaComposite.Src);
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new Rectangle2D.Float(0, 0, imageSize, imageSize));
    }

    private void drawAnchors(Graphics2D gs, int imageSize) {
        gs.setColor(Color.BLACK);

        int r1 = 4;
        int w2 = (int)(r1 / 0.8f);
        int w1 = (int)(r1 * 2 + w2 + w2 * 1.2f * 2);
        gs.setStroke(new BasicStroke(r1));

        int x = Constants.RING_WIDTH, y = Constants.RING_WIDTH;
        gs.draw(new Ellipse2D.Float(x, y, w1, w1));
        gs.fill(new Ellipse2D.Float(x + r1 + w2 * 1.2f, y + r1 + w2 * 1.2f, w2, w2));

        x = imageSize - w1 - Constants.RING_WIDTH;
        gs.draw(new Ellipse2D.Float(x, y, w1, w1));
        gs.fill(new Ellipse2D.Float(x + r1 + w2 * 1.2f, y + r1 + w2 * 1.2f, w2, w2));

        x = Constants.RING_WIDTH;
        y = imageSize - w1 - Constants.RING_WIDTH;
        gs.draw(new Ellipse2D.Float(x, y, w1, w1));
        gs.fill(new Ellipse2D.Float(x + r1 + w2 * 1.2f, y + r1 + w2 * 1.2f, w2, w2));

        gs.setComposite(AlphaComposite.SrcAtop);
    }

    private void drawData(Graphics2D gs, int imageSize, int []angles, byte[] data) {
        int bits = 0;
        int position = Constants.RING_WIDTH;
        int size = imageSize - Constants.RING_WIDTH * 2;
        for (int i = 0; i < ringSize - 1; ++i) {
            Color color = ringColors.get(i % ringColors.size());
            int minAngle = angles[i];
            bits = drawRing(gs, position, position, size, minAngle, color, data, bits);
            position += Constants.RING_WIDTH;
            size -= Constants.RING_WIDTH * 2;
        }

        // draw metadata:
        int minAngle = angles[angles.length - 1];
        byte[] metadata = new byte[(360 / minAngle + 7) / 8];
        Arrays.fill(metadata, (byte)0xaa);
        metadata[0] = (byte)((data.length << 8) ^ 0xaa);
        metadata[1] = (byte)((data.length & 0x00ff) ^ 0xaa);
        Color color = ringColors.get((ringSize - 1) % ringColors.size());
        drawRing(gs, position, position, size, minAngle, color, metadata, 0);
    }

    private int drawRing(Graphics2D gs, int x, int y, int size, int minAngle, Color color, byte[] code, int bits) {
        gs.setColor(color);
        int angle = 0;
        while (angle < 360 && bits < code.length * 8) {
            boolean one = (code[bits / 8] & (1 << (bits++ % 8))) != 0;

            if (one) {
                //println("value: $value $angle ${angle + minAngle}")
                gs.fillArc(x, y, size, size, angle, minAngle);
            } // not need draw zero.
            angle += minAngle;
        }
        gs.setColor(Color.WHITE);
        gs.fill(new Ellipse2D.Float(
                (x + Constants.RING_WIDTH),
                (x + Constants.RING_WIDTH),
                (size - Constants.RING_WIDTH * 2),
                (size - Constants.RING_WIDTH * 2)));
        return bits;
    }
}
