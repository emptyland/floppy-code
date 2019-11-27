package io.f04.code.ringcode.demo;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class ImageTest {

    //private static File headerFile = new File("/Users/niko/header.jpeg");
    static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nu.pattern.OpenCV.loadShared(); //add this
    }

    private int bits = 0;

    private Random rand = new Random();

    @Test
    public void testRead() throws Exception {
        // 加载时灰度
        Mat src = Imgcodecs.imread("src/test/resources/3.png", Imgcodecs.IMREAD_GRAYSCALE);
        // 保存灰度
        //Imgcodecs.imwrite("/Users/hecj/Desktop/b160_1.png", src);
        Mat target = new Mat();
        // 二值化处理
        Imgproc.threshold(src, target, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);//灰度图像二值化
        // 保存二值化后图片
        Imgcodecs.imwrite("src/test/resources/3-b1.png", target);


        //Imgproc.findContours();
    }

    @Test
    public void testImage() throws Exception {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        gs.setComposite(AlphaComposite.Src);
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new Rectangle2D.Float(0.0f, 0.0f, 200.0f, 200.0f));

        gs.setColor(Color.BLACK);
        gs.setStroke(new BasicStroke(3.0f));
        gs.draw(new Ellipse2D.Float(5.0f, 5.0f, 20.0f, 20.0f));
        gs.fill(new Ellipse2D.Float(10.0f, 10.0f, 11.0f, 11.0f));
        gs.draw(new Ellipse2D.Float(175.0f, 5.0f, 20.0f, 20.0f));
        gs.draw(new Ellipse2D.Float(5.0f, 175.0f, 20.0f, 20.0f));
        gs.setComposite(AlphaComposite.SrcAtop);

        //gs.color = Color.BLACK
        drawRing(gs, 5, 5, 190, 5, new Color(0x03, 0xa9, 0xf4));
        drawRing(gs, 10, 10, 180, 5, new Color(0x00, 0xdc, 0xb4));
        drawRing(gs, 15, 15, 170, 5, new Color(0x00, 0x96, 0x88));
        drawRing(gs, 20, 20, 160, 5, new Color(0x74, 0x3a, 0xb7));
        drawRing(gs, 25, 25, 150, 5, new Color(0x3f, 0x51, 0xb5));
        drawRing(gs, 30, 30, 140, 5, new Color(0x56, 0x77, 0xfc));
        drawRing(gs, 35, 35, 130, 10, new Color(0x25, 0x9b, 0x24));
        drawRing(gs, 40, 40, 120, 10, new Color(0x8b, 0xc3, 0x4a));
        drawRing(gs, 45, 45, 110, 10, new Color(0x25, 0x9b, 0x24));
        drawRing(gs, 50, 50, 100, 15, new Color(0x03, 0xa9, 0xf4));
        drawRing(gs, 55, 55, 90, 15, new Color(0x00, 0xdc, 0xb4));
        drawRing(gs, 60, 60, 80, 15, new Color(0x00, 0x96, 0x88));
        drawRing(gs, 65, 65, 70, 20, new Color(0x74, 0x3a, 0xb7));
        drawRing(gs, 70, 70, 60, 20, new Color(0x3f, 0x51, 0xb5));
        drawRing(gs, 75, 75, 50, 20, new Color(0x56, 0x77, 0xfc));

        ImageIO.write(image, "png", new File("src/test/resources/1.png"));

        System.out.println("bits: " + bits);
    }

    private void drawRing(Graphics2D gs, int x, int y, int size, int minAngle, Color color) {
        gs.setColor(color);
        int angle = 0;
        while (angle < 360) {
            int value = rand.nextInt(100);

            if (value > 55) {
                //println("value: $value $angle ${angle + minAngle}")
                gs.fillArc(x, y, size, size, angle, minAngle);
            }
            angle += minAngle;
            bits += 1;
        }
        gs.setColor(Color.WHITE);
        gs.fill(new Ellipse2D.Float((x + 5), (x + 5), (size - 10), (size - 10)));
    }

//    private fun setRadius(srcImage:BufferedImage, radius: Int, border: Int = 0, padding: Int = 0): BufferedImage {
//        val width = srcImage.width
//        val height = srcImage.height
//        val canvasWidth = width + padding * 2
//        val canvasHeight = height + padding * 2
//
//        val image = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
//        val gs = image.createGraphics()
//        gs.composite = AlphaComposite.Src
//        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//        gs.color = Color.WHITE
//        gs.fill(RoundRectangle2D.Float(0.0f, 0.0f, canvasWidth.toFloat(), canvasHeight.toFloat(), radius.toFloat(), radius.toFloat()))
//        gs.composite = AlphaComposite.SrcAtop
//        gs.drawImage(setClip(srcImage, radius), padding, padding, null)
//        if (border != 0) {
//            gs.color = Color.GRAY
//            gs.stroke = BasicStroke(border.toFloat())
//            gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius)
//        }
//        gs.dispose()
//        return image
//    }
//
//    private fun setClip(srcImage: BufferedImage, radius: Int): BufferedImage {
//        val width = srcImage.width
//        val height = srcImage.height
//        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
//        val gs = image.createGraphics()
//
//        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//        gs.clip = RoundRectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), radius.toDouble(), radius.toDouble())
//        gs.drawImage(srcImage, 0, 0, null)
//        gs.dispose()
//        return image
//    }

    @Test
    public void testBinary() throws Exception {
        byte[] d = {-86, -5, -86};
        printBinary(d);

        byte[] b = {-85, -6, 2};
        printBinary(b);
    }

    private void printBinary(byte[] d) {
        StringBuilder sb = new StringBuilder();
        for (byte b: d) {
            for (int i = 0; i < 8; i++) {
                if ((b & (1 << i)) != 0) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }
}
