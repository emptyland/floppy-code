#ifndef RING_CODE_H_
#define RING_CODE_H_

#include <string.h>
#include <string>
#include <memory>
#include <vector>

namespace cv {
class Mat;
} // namespace cv

class CapturedImage final {
public:
    CapturedImage(double r[4], const cv::Mat *image)
        : image_(image) {
        memcpy(r_, r, sizeof(r_));
    }
    ~CapturedImage();

    template<int N> inline double r() const {
        static_assert(N >= 0 && N < 4, "out of \'r\' rang.");
        return r_[N];
    }
    
    double ApproximateWidth() const;
    
    double ApproximateRingWidth() const { return ApproximateWidth() / 32.33f; }
    
    const cv::Mat &image() const { return *image_; }

    CapturedImage(const CapturedImage &) = delete;
    void operator = (const CapturedImage &) = delete;
private:
    std::unique_ptr<const cv::Mat> image_;
    double r_[4];
};

std::shared_ptr<CapturedImage>
CaptureRingCodePhoto(const cv::Mat &input,
                     std::vector<cv::Mat> *debug_progress,
                     std::string *err);



#endif // RING_CODE_H_
