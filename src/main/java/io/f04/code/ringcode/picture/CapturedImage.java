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

    public BufferedImage getImage() {
        return image;
    }
}
