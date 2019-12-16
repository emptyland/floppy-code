package io.f04.code.ringcode.picture;

import java.awt.image.BufferedImage;

public class CapturedImage {

    private double[] r;

    private BufferedImage image;

    public CapturedImage(double[] r, BufferedImage image) {
        this.r = r;
        this.image = image;
    }

    public double[] getR() {
        return r;
    }

    public double approximateWidth() {
        double w = r[0];
        int c = 1;
        for (int i = 1; i < r.length; i++) {
            if (r[i] == w) {
                c++;
            }
        }
        if (c == r.length) {
            return r[0];
        }
        w = 0;
        for (double e : r) {
            w += e;
        }
        return w / r.length;
    }

    public double approximateRingWidth() {
        return approximateWidth() / 32.33f;
    }

    public BufferedImage getImage() {
        return image;
    }
}
