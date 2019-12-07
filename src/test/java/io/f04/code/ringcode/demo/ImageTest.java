package io.f04.code.ringcode.demo;

import org.junit.Test;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public void test0() throws Exception {
        final String fileName = "src/test/resources/sample-15.jpeg";
        Mat src = Imgcodecs.imread(fileName, 1);
        Mat gray = new Mat();
        List<MatOfPoint> contours = prepareCarNoPhoto(src, gray, fileName);

        List<MatOfPoint> carPlates = choseLicencePlate(contours, 2000);
        System.out.println(carPlates.size());

        Mat srcAll = src.clone();
        for (MatOfPoint carPlate : carPlates) {
            MatOfPoint2f tmp2f = new MatOfPoint2f(carPlate.toArray());
            RotatedRect rectTuple = Imgproc.minAreaRect(tmp2f);
            Point[] points = new Point[4];
            rectTuple.points(points);
            Imgproc.rectangle(srcAll, points[0], points[2], new Scalar(0, 255, 0));
        }

//        for (int i = 0; i < carPlates.size(); i++) {
//            Imgproc.drawContours(srcAll, carPlates, i, new Scalar(0, 0, 255), -1);
//        }
        Imgcodecs.imwrite(fileName + "-a2.png", srcAll);
    }

    private List<MatOfPoint> prepareCarNoPhoto(Mat src, Mat gray, String fileName) {
        //Mat src = Imgcodecs.imread("src/test/resources/sample-10.jpeg", 1);

        //彩色图转灰度图
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
        //对图像进行平滑处理
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0, 0, Core.BORDER_DEFAULT);

        Mat kernel = Mat.ones(23, 23, CvType.CV_8U);
        Mat opening = new Mat();
        Imgproc.morphologyEx(gray, opening, Imgproc.MORPH_OPEN, kernel);
        Core.addWeighted(gray, 1, opening, -1, 0, opening);
        //Imgproc.dilate

        Mat threshold = new Mat();
        Imgproc.threshold(opening, threshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Mat edge = new Mat();
        Imgproc.Canny(threshold, edge, 112, 200);

        Imgcodecs.imwrite(fileName + "-a0.png", edge);


        kernel = Mat.ones(10, 10, CvType.CV_8U);
        Mat edge1 = new Mat();
        Imgproc.morphologyEx(edge, edge1, Imgproc.MORPH_CLOSE, kernel);
        Mat edge2 = new Mat();
        Imgproc.morphologyEx(edge1, edge2, Imgproc.MORPH_OPEN, kernel);

        Imgcodecs.imwrite(fileName + "-a1.png", edge2);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edge2, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private List<MatOfPoint> choseLicencePlate(List<MatOfPoint> contours, int minArea) {
        List<MatOfPoint> tmpContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) > minArea) {
                tmpContours.add(contour);
            }
        }
        List<MatOfPoint> carPlate = new ArrayList<>();
        for (MatOfPoint tmp : tmpContours) {
            MatOfPoint2f tmp2f = new MatOfPoint2f(tmp.toArray());
            RotatedRect rectTuple = Imgproc.minAreaRect(tmp2f);
            double width = rectTuple.size.width;
            double height = rectTuple.size.height;
            double aspectRatio =  (width > height) ? width / height : height / width;
            if (aspectRatio > 2 && aspectRatio < 5) {
                System.out.println(aspectRatio);
                carPlate.add(tmp);
                Mat rectVertices = new Mat();
                Imgproc.boxPoints(rectTuple, rectVertices);
            }
        }

        return carPlate;
    }

    @Test
    public void test1() throws Exception {
        // 加载时灰度
        Mat src = Imgcodecs.imread("src/test/resources/sample-1.jpeg", 1);
        Mat gray = new Mat();
        Mat srcAll = src.clone();

        //彩色图转灰度图
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
        //对图像进行平滑处理
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

        Imgproc.Canny(gray, gray, 112, 255);
        Imgcodecs.imwrite("src/test/resources/sample-0-a1.png", gray);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> markContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f newMtx = new MatOfPoint2f(contours.get(i).toArray());
            RotatedRect rotRect = Imgproc.minAreaRect(newMtx);
            double w = rotRect.size.width;
            double h = rotRect.size.height;
            double rate = Math.max(w, h) / Math.min(w, h);
            // 长短轴比小于1.3，总面积大于60
            double area = Imgproc.contourArea(contours.get(i));

            if (rate < 1.3 && w < gray.cols() / 4.0 && h < gray.rows() / 4.0 && area > 60) {
                // 计算层数，二维码角框有五层轮廓（有说六层），这里不计自己这一层，有4个以上子轮廓则标记这一点
                double[] ds = hierarchy.get(0, i);
                if (ds != null && ds.length > 3) {
                    int count = 0;
                    if (ds[3] == -1) {// 最外层轮廓排除
                        continue;
                    }

                    // 计算所有子轮廓数量
                    while ((int) ds[2] != -1) {
                        ++count;
                        ds = hierarchy.get(0, (int) ds[2]);
                    }
                    if (count >= 4) {
                        markContours.add(contours.get(i));
                    }
                }
            }
        }

        // 这部分代码画框，调试用
        System.out.println(markContours.size());
        for (int i = 0; i < markContours.size(); i++) {
            Imgproc.drawContours(srcAll, markContours, i, new Scalar(0, 0, 255), -1);
        }
        Imgcodecs.imwrite("src/test/resources/sample-0-a2.png", srcAll);

//        markContours.sort((a, b)-> {
//            double aa = Imgproc.contourArea(a);
//            double ba = Imgproc.contourArea(b);
//            if (aa > ba) {
//                return -1;
//            } else if (aa < ba) {
//                return 1;
//            }
//            return 0;
//        });
        while (markContours.size() > 3) {
            double minArea = Double.MAX_VALUE;
            int index = -1;
            for (int i = 0; i < markContours.size(); i++) {
                double area = Imgproc.contourArea(markContours.get(i));
                if (area < minArea) {
                    minArea = area;
                    index = i;
                }
            }

            if (index > 0) {
                markContours.remove(index);
            }
        }

        // 二维码有三个角轮廓，少于三个的无法定位放弃，多余三个的循环裁剪出来
        if (markContours.size() < 3) {
            // ignore
        } else {
            for (int i = 0; i < markContours.size() - 2; i++) {
                List<MatOfPoint> threePointList = new ArrayList<>();
                for (int j = i + 1; j < markContours.size() - 1; j++) {
                    for (int k = j + 1; k < markContours.size(); k++) {
                        threePointList.add(markContours.get(i));
                        threePointList.add(markContours.get(j));
                        threePointList.add(markContours.get(k));
                        perspective(threePointList, src, i + "-" + j + "-" + k);
                        threePointList.clear();
                    }
                }
            }
        }
    }

    @Test
    public void test2() throws Exception {
        // 加载时灰度
        Mat src = Imgcodecs.imread("src/test/resources/sample-0.jpeg", 1);
        Mat target = new Mat();

        Imgproc.cvtColor(src, target, Imgproc.COLOR_RGB2GRAY);
        //对图像进行平滑处理
        Imgproc.blur(target, target, new Size(3, 3));
        Imgproc.equalizeHist(target, target);
        //指定112阀值进行二值化
        Mat output = new Mat();
        Imgproc.threshold(target, output, 205, 255, Imgproc.THRESH_BINARY);
        // 二值化处理
        //Imgproc.threshold(src, target, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);//灰度图像二值化
        // 保存二值化后图片
        Imgcodecs.imwrite("src/test/resources/sample-0-b1.png", output);
    }

    @Test
    public void test3() throws Exception {
        Mat src = Imgcodecs.imread("src/test/resources/sample-0.jpeg", 1);
        int m = Core.getOptimalDFTSize(src.rows());
        int n = Core.getOptimalDFTSize(src.cols());
        Mat padding = new Mat();

        Core.copyMakeBorder(src, padding, 0, m - src.rows(), 0,n - src.cols(), Core.BORDER_CONSTANT, new Scalar(0, 0, 0));
        List<Mat> planes = new ArrayList<>();
        Mat padding32 = new Mat();
        padding.convertTo(padding32, CvType.CV_32F);
        planes.add(padding32);
        planes.add(Mat.zeros(padding.size(), CvType.CV_32F));
        Mat complex = new Mat();
        //merge(planes, 2, complexImg);
        Core.merge(planes, complex);
        //System.out.println(String.format("%s, %s", CvType.CV_32F, complex.type()));

//        Mat dst = new Mat();
//        complex.convertTo(dst, CvType.CV_32FC1);
        Core.dft(complex, complex);

        Imgcodecs.imwrite("src/test/resources/sample-0-ffi0.jpg", complex);
    }

    @Test
    public void testBinary() throws Exception {
        byte[] d = {-86, -5, -86};
        printBinary(d);

        byte[] b = {-85, -6, 2};
        printBinary(b);
    }

    private void printBinary(byte[] d) {
        StringBuilder sb = new StringBuilder();
        for (byte b : d) {
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

    private Mat perspective(List<MatOfPoint> contours, Mat src, String idx) {
        Point[] pointThree = new Point[3];
        for (int i = 0; i < contours.size(); i++) {
            pointThree[i] = centerCal(contours.get(i));
        }

        // 画线
        Mat srcWithLines = src.clone();
        Imgproc.line(srcWithLines, pointThree[0], pointThree[1], new Scalar(0, 0, 255), 2);
        Imgproc.line(srcWithLines, pointThree[1], pointThree[2], new Scalar(0, 0, 255), 2);
        Imgproc.line(srcWithLines, pointThree[0], pointThree[2], new Scalar(0, 0, 255), 2);
        Imgcodecs.imwrite("src/test/resources/sample-0-" + idx + "-a3.png", srcWithLines);

        double[] ca = new double[2];
        double[] cb = new double[2];

        ca[0] = pointThree[1].x - pointThree[0].x;
        ca[1] = pointThree[1].y - pointThree[0].y;
        cb[0] = pointThree[2].x - pointThree[0].x;
        cb[1] = pointThree[2].y - pointThree[0].y;

        double angle1 = computeAngle(ca, cb);
        double ccw1;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw1 = 0;
        } else {
            ccw1 = 1;
        }
        ca[0] = pointThree[0].x - pointThree[1].x;
        ca[1] = pointThree[0].y - pointThree[1].y;
        cb[0] = pointThree[2].x - pointThree[1].x;
        cb[1] = pointThree[2].y - pointThree[1].y;
        double angle2 = computeAngle(ca, cb);
        double ccw2;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw2 = 0;
        } else {
            ccw2 = 1;
        }

        ca[0] = pointThree[1].x - pointThree[2].x;
        ca[1] = pointThree[1].y - pointThree[2].y;
        cb[0] = pointThree[0].x - pointThree[2].x;
        cb[1] = pointThree[0].y - pointThree[2].y;
        double angle3 = computeAngle(ca, cb);
        int ccw3;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw3 = 0;
        } else {
            ccw3 = 1;
        }

        System.out.println("angle1:" + angle1 + ",angle2:" + angle2 + ",angle3:" + angle3);
        if (Double.isNaN(angle1) || Double.isNaN(angle2) || Double.isNaN(angle3)) {
            return null;
        }

        Point[] poly = new Point[4];
        if (angle3 > angle2 && angle3 > angle1) {
            if (ccw3 == 1) {
                poly[1] = pointThree[1];
                poly[3] = pointThree[0];
            } else {
                poly[1] = pointThree[0];
                poly[3] = pointThree[1];
            }
            poly[0] = pointThree[2];
            Point temp = new Point(pointThree[0].x + pointThree[1].x - pointThree[2].x, pointThree[0].y + pointThree[1].y - pointThree[2].y);
            poly[2] = temp;
        } else if (angle2 > angle1 && angle2 > angle3) {
            if (ccw2 == 1) {
                poly[1] = pointThree[0];
                poly[3] = pointThree[2];
            } else {
                poly[1] = pointThree[2];
                poly[3] = pointThree[0];
            }
            poly[0] = pointThree[1];
            Point temp = new Point(pointThree[0].x + pointThree[2].x - pointThree[1].x, pointThree[0].y + pointThree[2].y - pointThree[1].y);
            poly[2] = temp;
        } else if (angle1 > angle2 && angle1 > angle3) {
            if (ccw1 == 1) {
                poly[1] = pointThree[1];
                poly[3] = pointThree[2];
            } else {
                poly[1] = pointThree[2];
                poly[3] = pointThree[1];
            }
            poly[0] = pointThree[0];
            Point temp = new Point(pointThree[1].x + pointThree[2].x - pointThree[0].x, pointThree[1].y + pointThree[2].y - pointThree[0].y);
            poly[2] = temp;
        }

        Point[] trans = new Point[4];

        int temp = src.cols() / 4;
        trans[0] = new Point(temp, temp);
        trans[1] = new Point(temp, temp * 2 + temp);
        trans[2] = new Point(temp * 2 + temp, temp * 2 + temp);
        trans[3] = new Point(temp * 2 + temp, temp);

        double maxAngle = Math.max(angle3, Math.max(angle1, angle2));
        System.out.println(maxAngle);
        if (maxAngle < 75 || maxAngle > 115) { // 二维码为直角，最大角过大或者过小都判断为不是二维码
            return null;
        }

        Mat perspectiveMmat = Imgproc.getPerspectiveTransform(
                Converters.vector_Point_to_Mat(Arrays.asList(poly), CvType.CV_32F),
                Converters.vector_Point_to_Mat(Arrays.asList(trans), CvType.CV_32F)); //warp_mat
        Mat dst = new Mat();
        //计算变换结果
        Imgproc.warpPerspective(src, dst, perspectiveMmat, src.size(), Imgproc.INTER_LINEAR);
        //Imgproc.perspectiveTransform
//
        Mat pointThreeMat = Converters.vector_Point_to_Mat(Arrays.asList(pointThree), CvType.CV_32F);
        Mat pointThreeDst = new Mat();
        Core.perspectiveTransform(pointThreeMat, pointThreeDst, perspectiveMmat);
        List<Point> newPointThree = new ArrayList<>();
        Converters.Mat_to_vector_Point(pointThreeDst, newPointThree);

        double[] r = new double[3];
        r[0] = computeDistance(newPointThree.get(0), newPointThree.get(1)); // 0->1: 540
        r[1] = computeDistance(newPointThree.get(0), newPointThree.get(2)); // 0->2: 763
        r[2] = computeDistance(newPointThree.get(1), newPointThree.get(2)); // 1->2: 540
        System.out.println(Arrays.toString(r));

        Rect roiArea = new Rect((int)newPointThree.get(1).x - 50, (int)newPointThree.get(1).y - 50, (int)r[0] + 100, (int)r[0] + 100);
        Mat dstRoi = new Mat(dst, roiArea);

        Imgproc.cvtColor(dstRoi, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(dst, dst, new Size(3, 3));
        Imgproc.equalizeHist(dst, dst);
        Mat output = new Mat();
        Imgproc.threshold(dst, output, 108, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("src/test/resources/sample-0-" + idx + ".png", output);
        return dst;
    }

    private double computeDistance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    private double computeAngle(double[] ca, double[] cb) {
        return 180 / Math.PI * Math.acos((ca[0]*cb[0]+ca[1]*cb[1])/(Math.sqrt(ca[0]*ca[0]+ca[1]*ca[1])*Math.sqrt(cb[0]*cb[0]+cb[1]*cb[1])));
    }

    private Point centerCal(MatOfPoint matOfPoint){
        //int size = matOfPoint.cols();
        MatOfPoint2f mat2f = new MatOfPoint2f( matOfPoint.toArray() );
        RotatedRect rect = Imgproc.minAreaRect( mat2f );
        Point[] vertices = new Point[4];
        rect.points(vertices);
        double centerX = ((vertices[0].x + vertices[1].x)/2 + (vertices[2].x + vertices[3].x)/2)/2;
        double centerY =  ((vertices[0].y + vertices[1].y)/2 + (vertices[2].y + vertices[3].y)/2)/2;
        return new Point(centerX,centerY);
    }
}
