#include "ring-code.h"
#include <opencv2/opencv.hpp>
#include <math.h>
#include <limits>

namespace ring {

class PerspectiveResult final {
public:
    PerspectiveResult(cv::Point dst_points[4], const cv::Mat &dst, const cv::Mat &perspection)
        : dst_(dst)
        , perspection_(perspection) {
        memcpy(dst_points_, dst_points, sizeof(dst_points_));
    }
    
    const cv::Point *dst_points() const { return dst_points_; }
    const cv::Mat &dst() const { return dst_; }
    const cv::Mat &perspection() const { return perspection_; }
private:
    cv::Point dst_points_[4];
    cv::Mat dst_;
    cv::Mat perspection_;
}; // class PerspectiveResult

class Capturer {
public:
    Capturer(const cv::Mat &src, std::vector<cv::Mat> *debug_progress)
        : src_(src)
        , debug_progress_(debug_progress) {
    }
    
    std::shared_ptr<CapturedImage> Capture();

    Capturer(const Capturer &) = delete;
    void operator = (const Capturer &) = delete;
private:
    bool debug() const { return debug_progress_ != nullptr; }

    cv::Mat Prepare() {
        cv::Mat gray;
        cv::cvtColor(src_, gray, cv::COLOR_RGB2GRAY);
        cv::GaussianBlur(gray, gray, cv::Size{5, 5}, .0);
        cv::Canny(gray, gray, 100.0, 200.0);
        AppendDebugProgressIfNeeded(gray);
        return gray;
    }
    
    std::vector<cv::Mat_<cv::Point>>
    FindContours(const cv::Mat &gray, std::vector<cv::Mat_<cv::Point>> *aux_contours);
    
    int ProcessMarkedContours(std::vector<cv::Mat_<cv::Point>> *marks,
                              std::vector<cv::Mat_<cv::Point>> *aux_marks,
                              cv::Point three_points[3]);
    
    PerspectiveResult *Perspective(const cv::Point three_points[3],
                                   const std::vector<cv::Mat_<cv::Point>> &aux_marks);
    
    cv::Mat CutTarge(const cv::Mat &input, const cv::Point points[4], const double dist[4]) {
        double r = dist[0];
        cv::Rect roi_area(points[0].x - 50, points[0].y - 50, r + 100, r + 100);
        cv::Mat dst_roi(input, roi_area);
        
        cv::Mat dst;
        cv::cvtColor(dst_roi, dst, cv::COLOR_RGB2GRAY);
        cv::blur(dst, dst, cv::Size(3, 3));
        cv::equalizeHist(dst, dst);
        
        cv::Mat output;
        cv::threshold(dst, output, 108, 255, cv::THRESH_BINARY);
        AppendDebugProgressIfNeeded(output);
        return output;
//        Rect roiArea = new Rect((int)fourPoints[0].x - 50, (int)fourPoints[0].y - 50, (int)r + 100, (int)r + 100);
//        Mat dstRoi = new Mat(dst, roiArea);
//
//        Imgproc.cvtColor(dstRoi, dst, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.blur(dst, dst, new Size(3, 3));
//        Imgproc.equalizeHist(dst, dst);
//        Mat output = new Mat();
//        Imgproc.threshold(dst, output, 108, 255, Imgproc.THRESH_BINARY);
//        writeTestFileIfNeeded(output);
//        return output;
    }
    
    static cv::Point CenterCal(const cv::Mat_<cv::Point> &mat_of_point);
    static void OrderThreePointers(cv::Point three_points[3]);
    
    static double ComputeDistance(cv::Point p1, cv::Point p2) {
        return sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    static double ComputeAngle(const double ca[2], const double cb[2]) {
        return 180 / M_PI * acos((ca[0] * cb[0] + ca[1] * cb[1])
                / (sqrt(ca[0] * ca[0] + ca[1] * ca[1])
                * sqrt(cb[0] * cb[0] + cb[1] * cb[1])));
    }
    
    void AppendDebugProgressIfNeeded(const cv::Mat &dst) {
        if (debug_progress_) { debug_progress_->push_back(dst); }
    }
    
    std::vector<cv::Mat> *debug_progress_;
    cv::Mat src_;
}; // class Capturer

std::shared_ptr<CapturedImage> Capturer::Capture() {
    // TODO:
    cv::Mat gray(Prepare());
    std::vector<cv::Mat_<cv::Point>> aux;
    std::vector<cv::Mat_<cv::Point>> marks = FindContours(gray, &aux);
    
    cv::Point points[3];
    int n = ProcessMarkedContours(&marks, &aux, points);
    if (n > 0) {
        std::unique_ptr<PerspectiveResult> result(Perspective(points, aux));
        if (result) {
            double dist[4] = {
                ComputeDistance(result->dst_points()[0], result->dst_points()[1]),
                ComputeDistance(result->dst_points()[1], result->dst_points()[2]),
                ComputeDistance(result->dst_points()[2], result->dst_points()[3]),
                ComputeDistance(result->dst_points()[3], result->dst_points()[0]),
            };
            (void)dist;
            
            cv::Mat dst = CutTarge(result->dst(), result->dst_points(), dist);
            return std::make_shared<CapturedImage>(dist, new cv::Mat(dst));
        }
    }
    return std::shared_ptr<CapturedImage>(nullptr);
}

std::vector<cv::Mat_<cv::Point>>
Capturer::FindContours(const cv::Mat &gray, std::vector<cv::Mat_<cv::Point>> *aux_contours) {
    std::vector<std::vector<cv::Point>> contours;
    std::vector<cv::Vec4i> hierarchy;
    cv::findContours(gray, contours, hierarchy, cv::RETR_TREE, cv::CHAIN_APPROX_SIMPLE);
    
    std::vector<cv::Mat_<cv::Point>> mark_contours;
    for (int i = 0; i < contours.size(); i++) {
        cv::Mat_<cv::Point> new_mtx(contours[i]);
        cv::RotatedRect rot_rect(cv::minAreaRect(new_mtx));
        
        double w = rot_rect.size.width,
               h = rot_rect.size.height,
               rate = std::max(w, h) / std::min(w, h),
               area = cv::contourArea(contours[i]);
        if (rate < 1.3 && w < gray.cols / 4.0 && h < gray.rows / 4.0 && area > 60) {
            
            auto ds = hierarchy[i];
            if (ds[0] > 3) {
                int count = 0;
                while (ds[2] != -1) {
                    ++count;
                    //ds = hierarchy.get(0, (int) ds[2]);
                    ds = hierarchy[ds[2]];
                }
                if (count >= 5) {
                    mark_contours.push_back(cv::Mat_<cv::Point>(contours[i], true));
                }
                if (count == 2) {
                    aux_contours->push_back(cv::Mat_<cv::Point>(contours[i], true));
                }
            }
        }
    }
    
    if (debug()) {
        cv::Mat marks(src_.clone());
        for (int i = 0; i < mark_contours.size(); i++) {
            cv::drawContours(marks, mark_contours, i, cv::Scalar{0, 0, 255}, -1);
        }
        for (int i = 0; i < aux_contours->size(); i++) {
            cv::drawContours(marks, *aux_contours, i, cv::Scalar{0, 255, 0}, -1);
        }
        AppendDebugProgressIfNeeded(marks);
    }
    
    return mark_contours;
}

int Capturer::ProcessMarkedContours(std::vector<cv::Mat_<cv::Point>> *marks,
                                    std::vector<cv::Mat_<cv::Point>> *aux_marks,
                                    cv::Point three_points[3]) {
    while (marks->size() > 3) {
        double min_area = std::numeric_limits<double>::max();
        int index = -1;
        for (int i = 0; i < marks->size(); i++) {
            double area = cv::contourArea(marks->at(i));
            if (area < min_area) {
                min_area = area;
                index = i;
            }
        }
        if (index > 0) {
            marks->erase(marks->begin() + index);
        }
    }
    if (marks->size() < 3) {
        return 0;
    }
    
    double max_area = .0;
    for (const auto &mark : *marks) {
        if (cv::contourArea(mark) > max_area) {
            max_area = cv::contourArea(mark);
        }
    }
    
    std::vector<cv::Mat_<cv::Point>> aux;
    for (const auto &mark : *aux_marks) {
        if (cv::contourArea(mark) > max_area) {
            aux.push_back(mark);
        }
    }
    aux_marks->clear();
    *aux_marks = std::move(aux);

    three_points[0] = CenterCal(marks->at(0));
    three_points[1] = CenterCal(marks->at(1));
    three_points[2] = CenterCal(marks->at(2));
    OrderThreePointers(three_points);
    return 1;
}

PerspectiveResult *Capturer::Perspective(const cv::Point three_points[3],
                                         const std::vector<cv::Mat_<cv::Point>> &aux_marks) {
    // TODO:
    return nullptr;
}

cv::Point Capturer::CenterCal(const cv::Mat_<cv::Point> &mat_of_point) {
    cv::Mat_<cv::Point2f> mat2f(mat_of_point);
    cv::RotatedRect rect = cv::minAreaRect(mat2f);
    cv::Point2f vertices[4];
    rect.points(vertices);
    double x = ((vertices[0].x + vertices[1].x)/2 + (vertices[2].x + vertices[3].x)/2)/2;
    double y = ((vertices[0].y + vertices[1].y)/2 + (vertices[2].y + vertices[3].y)/2)/2;
    return cv::Point(x, y);
}

void Capturer::OrderThreePointers(cv::Point three_points[3]) {
    int first = 0;
    for (int i = 1; i < 3; i++) {
        cv::Point pt = three_points[i];
        if (pt.x < three_points[first].x && pt.y < three_points[first].y) {
            first = i;
        }
    }
    std::swap(three_points[0], three_points[first]);
    
    int second = 0;
    if (three_points[1].x > three_points[0].x && three_points[1].y < three_points[2].y) {
        second = 1;
    } else {
        second = 2;
    }
    std::swap(three_points[1], three_points[second]);
}

} // namespace ring


CapturedImage::~CapturedImage() {
}


std::shared_ptr<CapturedImage>
CaptureRingCodePhoto(const cv::Mat &input,
                     std::vector<cv::Mat> *debug_progress,
                     std::string *err) {
    ring::Capturer capturer(input, debug_progress);
    return capturer.Capture();
}
