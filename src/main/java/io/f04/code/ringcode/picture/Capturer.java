package io.f04.code.ringcode.picture;

import io.f04.code.ringcode.utils.OpenCVInitializer;
import org.jetbrains.annotations.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Capturer {

    private static class PerspectiveResult {
        private Point[] dstPoints;

        private Mat dst;

        private Mat perspection;

        public PerspectiveResult(Point[] dstPoints, Mat dst, Mat perspection) {
            this.dstPoints = dstPoints;
            this.dst = dst;
            this.perspection = perspection;
        }

        public PerspectiveResult() {}

        @Nullable
        public Point[] getDstPoints() {
            return dstPoints;
        }

        @Nullable
        public Mat getDst() {
            return dst;
        }

        @Nullable
        public Mat getPerspection() {
            return perspection;
        }
    }

    private boolean test;

    private int step = 0;

    private File inputFile;

    private Mat src = null;

    //private Mat srcAll = null;

    static {
        OpenCVInitializer.ensure();
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public File getInputFile() {
        return inputFile;
    }

    @NotNull
    public List<CapturedImage> capture(@NotNull File inputFile) throws IOException {
        List<CapturedImage> images = new ArrayList<>();
        this.inputFile = inputFile;
        this.step = 0;
        Mat gray = prepare();
        List<MatOfPoint> aux = new ArrayList<>();
        List<MatOfPoint> marks = findContours(gray, aux);
        List<Point[]> pointGroups = processMarkedContours(marks, aux);
        for (Point[] points : pointGroups) {
            PerspectiveResult result = perspective(points, aux);
            if (result.getDstPoints() != null) {
                double[] distance = new double[4];
                distance[0] = computeDistance(result.getDstPoints()[0], result.getDstPoints()[1]);
                distance[1] = computeDistance(result.getDstPoints()[1], result.getDstPoints()[2]);
                distance[2] = computeDistance(result.getDstPoints()[2], result.getDstPoints()[3]);
                distance[3] = computeDistance(result.getDstPoints()[3], result.getDstPoints()[0]);

                Mat dst = cutTarget(result.getDst(), result.getDstPoints(), distance);
                images.add(new CapturedImage(distance, mat2BufferedImage(dst)));
            }
        }
        return images;
    }

    private Mat prepare() throws IOException {
        src = Imgcodecs.imread(inputFile.toString(), 1);
        if (src.empty()) {
            throw new IOException("open-cv can not open: " + inputFile);
        }
        //srcAll = src.clone();

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

        Imgproc.Canny(gray, gray, 100, 200);
        writeTestFileIfNeeded(gray);
        return gray;
    }

    @NotNull
    private PerspectiveResult perspective(Point[] threePoints, List<MatOfPoint> auxMarks) {
        if (test) {
            Mat srcWithLines = src.clone();
            Imgproc.line(srcWithLines, threePoints[0], threePoints[1], new Scalar(0, 0, 255), 2);
            Imgproc.line(srcWithLines, threePoints[1], threePoints[2], new Scalar(0, 0, 255), 2);
            Imgproc.line(srcWithLines, threePoints[0], threePoints[2], new Scalar(0, 0, 255), 2);
            writeTestFileIfNeeded(srcWithLines);
        }

        double[] ca = new double[2];
        double[] cb = new double[2];

        ca[0] = threePoints[1].x - threePoints[0].x;
        ca[1] = threePoints[1].y - threePoints[0].y;
        cb[0] = threePoints[2].x - threePoints[0].x;
        cb[1] = threePoints[2].y - threePoints[0].y;

        double angle1 = computeAngle(ca, cb);
        double ccw1;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw1 = 0;
        } else {
            ccw1 = 1;
        }
        ca[0] = threePoints[0].x - threePoints[1].x;
        ca[1] = threePoints[0].y - threePoints[1].y;
        cb[0] = threePoints[2].x - threePoints[1].x;
        cb[1] = threePoints[2].y - threePoints[1].y;
        double angle2 = computeAngle(ca, cb);
        double ccw2;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw2 = 0;
        } else {
            ccw2 = 1;
        }

        ca[0] = threePoints[1].x - threePoints[2].x;
        ca[1] = threePoints[1].y - threePoints[2].y;
        cb[0] = threePoints[0].x - threePoints[2].x;
        cb[1] = threePoints[0].y - threePoints[2].y;
        double angle3 = computeAngle(ca, cb);
        int ccw3;
        if (ca[0]*cb[1] - ca[1]*cb[0] > 0) {
            ccw3 = 0;
        } else {
            ccw3 = 1;
        }

        if (Double.isNaN(angle1) || Double.isNaN(angle2) || Double.isNaN(angle3)) {
            return new PerspectiveResult();
        }

        Point[] poly = new Point[4];
        if (angle3 > angle2 && angle3 > angle1) {
            if (ccw3 == 1) {
                poly[1] = threePoints[1];
                poly[3] = threePoints[0];
            } else {
                poly[1] = threePoints[0];
                poly[3] = threePoints[1];
            }
            poly[0] = threePoints[2];
            Point temp = new Point(threePoints[0].x + threePoints[1].x - threePoints[2].x, threePoints[0].y + threePoints[1].y - threePoints[2].y);
            poly[2] = temp;
        } else if (angle2 > angle1 && angle2 > angle3) {
            if (ccw2 == 1) {
                poly[1] = threePoints[0];
                poly[3] = threePoints[2];
            } else {
                poly[1] = threePoints[2];
                poly[3] = threePoints[0];
            }
            poly[0] = threePoints[1];
            Point temp = new Point(threePoints[0].x + threePoints[2].x - threePoints[1].x, threePoints[0].y + threePoints[2].y - threePoints[1].y);
            poly[2] = temp;
        } else if (angle1 > angle2 && angle1 > angle3) {
            if (ccw1 == 1) {
                poly[1] = threePoints[1];
                poly[3] = threePoints[2];
            } else {
                poly[1] = threePoints[2];
                poly[3] = threePoints[1];
            }
            poly[0] = threePoints[0];
            Point temp = new Point(threePoints[1].x + threePoints[2].x - threePoints[0].x, threePoints[1].y + threePoints[2].y - threePoints[0].y);
            poly[2] = temp;
        }
        if (!auxMarks.isEmpty()) {
            Optional<Point> found = auxMarks.stream().filter((mark)->{
                MatOfPoint2f mat2f = new MatOfPoint2f(mark.toArray());
                RotatedRect rect = Imgproc.minAreaRect(mat2f);
                return rect.boundingRect().contains(poly[2]);
            }).map(Capturer::centerCal)
                    .findFirst();
            poly[2] = found.orElse(poly[2]);
        }
        if (test) {
            Mat srcWithLines = src.clone();
            Imgproc.line(srcWithLines, poly[0], poly[1], new Scalar(0, 0, 255), 2);
            Imgproc.line(srcWithLines, poly[1], poly[2], new Scalar(0, 0, 255), 2);
            Imgproc.line(srcWithLines, poly[2], poly[3], new Scalar(0, 0, 255), 2);
            Imgproc.line(srcWithLines, poly[0], poly[3], new Scalar(0, 0, 255), 2);
            writeTestFileIfNeeded(srcWithLines);
        }

        Point[] trans = new Point[4];
        int temp = src.cols() / 4;
        trans[0] = new Point(temp, temp);
        trans[1] = new Point(temp, temp * 2 + temp);
        trans[2] = new Point(temp * 2 + temp, temp * 2 + temp);
        trans[3] = new Point(temp * 2 + temp, temp);

        double maxAngle = Math.max(angle3, Math.max(angle1, angle2));
        if (maxAngle < 75 || maxAngle > 115) {
            return new PerspectiveResult();
        }

        // do perspective the picture mat
        Mat perspectiveMmat = Imgproc.getPerspectiveTransform(
                Converters.vector_Point_to_Mat(Arrays.asList(poly), CvType.CV_32F),
                Converters.vector_Point_to_Mat(Arrays.asList(trans), CvType.CV_32F)); //warp_mat
        Mat dst = new Mat();
        Imgproc.warpPerspective(src, dst, perspectiveMmat, src.size(), Imgproc.INTER_LINEAR);
        writeTestFileIfNeeded(dst);

        Mat fourPointsMat = Converters.vector_Point_to_Mat(Arrays.asList(poly), CvType.CV_32F);
        Mat fourPointsDst = new Mat();
        Core.perspectiveTransform(fourPointsMat, fourPointsDst, perspectiveMmat);
        List<Point> newFourPoints = new ArrayList<>();
        Converters.Mat_to_vector_Point(fourPointsDst, newFourPoints);

        return new PerspectiveResult(newFourPoints.toArray(threePoints), dst, perspectiveMmat);
    }

    private Mat cutTarget(Mat dst, Point[] fourPoints, double[] distance) {
        double r = distance[0];
        System.out.println(Arrays.toString(distance));
        Rect roiArea = new Rect((int)fourPoints[0].x - 50, (int)fourPoints[0].y - 50, (int)r + 100, (int)r + 100);
        Mat dstRoi = new Mat(dst, roiArea);

        Imgproc.cvtColor(dstRoi, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(dst, dst, new Size(3, 3));
        Imgproc.equalizeHist(dst, dst);
        Mat output = new Mat();
        Imgproc.threshold(dst, output, 108, 255, Imgproc.THRESH_BINARY);
        writeTestFileIfNeeded(output);
        return output;
    }

    private List<Point[]> processMarkedContours(List<MatOfPoint> marks, List<MatOfPoint> auxMarks) {
        while (marks.size() > 3) {
            double minArea = Double.MAX_VALUE;
            int index = -1;
            for (int i = 0; i < marks.size(); i++) {
                double area = Imgproc.contourArea(marks.get(i));
                if (area < minArea) {
                    minArea = area;
                    index = i;
                }
            }

            if (index > 0) {
                marks.remove(index);
            }
        }

        if (marks.size() < 3) {
            return new ArrayList<>();
        }
        double maxArea = marks.stream()
                .map(Imgproc::contourArea)
                .max(Double::compareTo)
                .orElse(Double.MAX_VALUE);
        List<MatOfPoint> aux = auxMarks.stream()
                .filter((mark)->Imgproc.contourArea(mark) > maxArea)
                .collect(Collectors.toList());
        auxMarks.clear();
        auxMarks.addAll(aux);

        List<Point[]> results = new ArrayList<>();
        for (int i = 0; i < marks.size() - 2; i++) {
            Point[] threePoints = new Point[3];
            for (int j = i + 1; j < marks.size() - 1; j++) {
                for (int k = j + 1; k < marks.size(); k++) {
                    threePoints[0] = centerCal(marks.get(i));
                    threePoints[1] = centerCal(marks.get(j));
                    threePoints[2] = centerCal(marks.get(k));
                    orderThreePointers(threePoints);
                    results.add(threePoints);
                }
            }
        }
        return results;
    }

    private List<MatOfPoint> findContours(Mat gray, List<MatOfPoint> auxContours) {
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> markContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Point target = new Point(588, 834);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f newMtx = new MatOfPoint2f(contours.get(i).toArray());
            RotatedRect rotRect = Imgproc.minAreaRect(newMtx);
            double w = rotRect.size.width;
            double h = rotRect.size.height;
            double rate = Math.max(w, h) / Math.min(w, h);
            double area = Imgproc.contourArea(contours.get(i));

            if (rate < 1.3 && w < gray.cols() / 4.0 && h < gray.rows() / 4.0 && area > 60) {
                double[] ds = hierarchy.get(0, i);
                if (ds != null && ds.length > 3) {
                    int count = 0;
                    while ((int) ds[2] != -1) {
                        ++count;
                        ds = hierarchy.get(0, (int) ds[2]);
                    }
                    if (count >= 5) {
                        markContours.add(contours.get(i));
                    }
                    if (count == 2) {
                        auxContours.add(contours.get(i));
                    }
                }
            }
        }

        if (test) {
            Mat marks = src.clone();
            for (int i = 0; i < markContours.size(); i++) {
                Imgproc.drawContours(marks, markContours, i, new Scalar(0, 0, 255), -1);
            }
            for (int i = 0; i < auxContours.size(); i++) {
                Imgproc.drawContours(marks, auxContours, i, new Scalar(0, 255, 0), -1);
            }
            writeTestFileIfNeeded(marks);
        }
        return markContours;
    }

    private void writeTestFileIfNeeded(Mat dst) {
        if (!test) {
            return;
        }
        Imgcodecs.imwrite(inputFile.toString() + "-s" + step++ + ".png", dst);
    }

    private static void orderThreePointers(Point[] threePoints) {
        int first = 0;
        for (int i = 1; i < threePoints.length; i++) {
            Point pt = threePoints[i];
            if (pt.x < threePoints[first].x && pt.y < threePoints[first].y) {
                first = i;
            }
        }
        Point tmp = threePoints[first];
        threePoints[first] = threePoints[0];
        threePoints[0] = tmp;

        int second;
        if (threePoints[1].x > threePoints[0].x && threePoints[1].y < threePoints[2].y) {
            second = 1;
        } else {
            second = 2;
        }
        tmp = threePoints[second];
        threePoints[second] = threePoints[1];
        threePoints[1] = tmp;
    }

    private static double computeDistance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    private static double computeAngle(double[] ca, double[] cb) {
        return 180 / Math.PI * Math.acos((ca[0]*cb[0]+ca[1]*cb[1])
                / (Math.sqrt(ca[0]*ca[0]+ca[1]*ca[1])
                * Math.sqrt(cb[0]*cb[0]+cb[1]*cb[1])));
    }

    private static Point centerCal(MatOfPoint matOfPoint){
        MatOfPoint2f mat2f = new MatOfPoint2f( matOfPoint.toArray() );
        RotatedRect rect = Imgproc.minAreaRect( mat2f );
        Point[] vertices = new Point[4];
        rect.points(vertices);
        double centerX = ((vertices[0].x + vertices[1].x)/2 + (vertices[2].x + vertices[3].x)/2)/2;
        double centerY =  ((vertices[0].y + vertices[1].y)/2 + (vertices[2].y + vertices[3].y)/2)/2;
        return new Point(centerX,centerY);
    }

    private static BufferedImage mat2BufferedImage(Mat matrix) throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".png", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }
}
