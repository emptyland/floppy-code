#include "ring-code.h"
#include <opencv2/opencv.hpp>
extern "C" {
#include "rs.h"
} // extern "C"
#include <math.h>
#include <stdarg.h>
#include <limits>
#include <random>

void Errorf(std::string *err, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    char buf[256];
    vsnprintf(buf, sizeof(buf), fmt, ap);
    va_end(ap);
    err->assign(buf);
}

namespace ring {

struct ErrorCorrectionLevel {
//    public int getTotalShardCount() { return dataShardCount + parityShardCount; }
//
//    public int computePayloadSize(int total) {
//        return total * dataShardCount / getTotalShardCount() - getTotalShardCount() * 2;
//    }
    int GetTotalShardCount() const { return data_shard_count + parity_shard_count; }
    int ComputePayloadSize(int total) const {
        return total * data_shard_count / GetTotalShardCount() - GetTotalShardCount() * 2;
    }
    
    const int data_shard_count;
    const int parity_shard_count;
    
    enum Level: int { L, M, Q, H, };
    
    static const ErrorCorrectionLevel kL;
    static const ErrorCorrectionLevel kM;
    static const ErrorCorrectionLevel kQ;
    static const ErrorCorrectionLevel kH;

    static const ErrorCorrectionLevel *kLevels[4];
}; // struct ErrorCorrectionLevel

const ErrorCorrectionLevel ErrorCorrectionLevel::kL{2,1};
const ErrorCorrectionLevel ErrorCorrectionLevel::kM{2,1};
const ErrorCorrectionLevel ErrorCorrectionLevel::kQ{2,2};
const ErrorCorrectionLevel ErrorCorrectionLevel::kH{2,2};

const ErrorCorrectionLevel *ErrorCorrectionLevel::kLevels[4] = {&kL, &kM, &kQ, &kH};

// Ring Code Constants Configuration
struct Constants {
    static constexpr int kRingWidth = 7;
    static constexpr int kAnchorWidth = 4;
    static constexpr int kRingMetadataBits = 18;
    
    static constexpr int kRing15 = 15;
    static constexpr int kRing15MaxBits = 666 - kRingMetadataBits; // 18 bits is metadata;
    static constexpr int kRing15ImageSize = 280;
    static constexpr int kRing15MaxBytes = kRing15MaxBits / 8;
    static const int kRing15Angles[];
}; // struct Constants

const int Constants::kRing15Angles[] = {5, 5, 5, 5, 5, 5, 10, 10, 10, 15, 15, 15, 20, 20, 20};

class Reader {
private:
    struct Metadata {
        ErrorCorrectionLevel::Level error_correction_level;
        size_t data_size;
    };
public:
    Reader(const cv::Mat &input, std::vector<cv::Mat> *debug_progress, std::string *error)
        : input_(input)
        , debug_progress_(debug_progress)
        , error_(error)
        , center_x_(input.cols / 2)
        , center_y_(input.rows / 2) {
        EnsureRingCodeLibraryInitialized();
        if (debug()) {
            dummy_ = input_.clone();
        }
    }
    
    ~Reader() {
        if (debug()) {
            debug_progress_->push_back(dummy_);
        }
    }
    
    Reader &WithCenterX(int x) {
        center_x_ = x;
        return *this;
    }
    
    Reader &WithCenterY(int y) {
        center_y_ = y;
        return *this;
    }
    
    Reader &WithRingWidth(double width) {
        ring_width_ = width;
        return *this;
    }
    
    Reader &WithSampleTimes(int times) {
        sample_times_ = times;
        return *this;
    }
    
    std::string Read() const;
    
private:
    bool debug() const { return debug_progress_ != nullptr; }
    
    bool CheckParams() const;
    bool ReadMetadata(int r, double angle, Metadata *m) const;
    bool Decode(const Metadata &metadata, uint8_t *bits, size_t size) const;
    bool TestSample(int r, double begin_angle, double end_angle) const;
    
    const cv::Mat input_;
    std::vector<cv::Mat> *const debug_progress_;
    std::string *const error_;
    int center_x_;
    int center_y_;
    double ring_width_ = .0;
    int sample_times_ = 17;
    
    cv::Mat dummy_;
    std::random_device rd_;
}; // class Reader

std::string Reader::Read() const {
    std::string data;
    if (!CheckParams()) {
        return data;
    }

    double angle = (2 * M_PI) / Constants::kRingMetadataBits;
    int r = static_cast<int>(ring_width_ * 4);
    Metadata metadata;
    if (!ReadMetadata(r, angle, &metadata)) {
        return data;
    }
    
    uint8_t bits[(Constants::kRing15MaxBits + 7) / 8];
    memset(bits, 0, sizeof(bits));
    int b = 0;
    for (int i = 0; i < Constants::kRing15 - 1; i++) {
        angle = (2 * M_PI) / (360.0 / Constants::kRing15Angles[i]);
        r = static_cast<int>(ring_width_ * (4 + Constants::kRing15 - i - 1));

        for (int j = 0; j < 360 / Constants::kRing15Angles[i]; j++) {
            bool sample = TestSample(r, j * angle, (j + 1) * angle);
            bits[b / 8] |= sample ? (1 << (b % 8)) : 0;
            b++;
        }
    }

    if (!Decode(metadata, bits, sizeof(bits))) {
        return data;
    }
    data.assign(reinterpret_cast<const char *>(bits), metadata.data_size);
    return data;
}

bool Reader::CheckParams() const {
    if (input_.dims != 2) {
        Errorf(error_, "incorrect input mat dims: %d", input_.dims);
        return false;
    }
    if (center_x_ < 0 || center_x_ > input_.cols) {
        Errorf(error_, "center_x out of range: %d", center_x_);
        return false;
    }
    if (center_y_ < 0 || center_y_ > input_.cols) {
        Errorf(error_, "center_y out of range: %d", center_y_);
        return false;
    }
    if (ring_width_ <= 1.) {
        Errorf(error_, "ring_width too samlle: %f", ring_width_);
        return false;
    }
    if (sample_times_ < 1) {
        Errorf(error_, "sample_times too samlle: %d", sample_times_);
        return false;
    }
    return true;
}

bool Reader::ReadMetadata(int r, double angle, Metadata *receive) const {
    int8_t buf[(Constants::kRingMetadataBits + 7) / 8];
    memset(buf, 0, sizeof(buf));
    
    for (int i = 0; i < 18; ++i) {
        bool sample = TestSample(r, i * angle, (i + 1) * angle);
        buf[i / 8] |= sample ? (1 << (i % 8)) : 0;
    }
    for (int i = 0; i < sizeof(buf) - 1; i++) {
        buf[i] ^= 0xaa;
    }
    buf[sizeof(buf) - 1] ^= 0x2;
    
    receive->data_size = (static_cast<int16_t>(buf[0]) << 8) | buf[1];
    if (receive->data_size > 0x7fff) {
        Errorf(error_, "incorrect metadata: bad size data.");
        return false;
    }
    if (buf[2] >= 4) {
        Errorf(error_, "incorrect metadata: bad error correction level.");
        return false;
    }
    receive->error_correction_level = static_cast<ErrorCorrectionLevel::Level>(buf[2]);
    return true;
}

bool Reader::Decode(const Metadata &metadata, uint8_t *bits, size_t size) const {

    for (int i = 0; i < size; i++) {
        if (i % 2 == 0) {
            bits[i] ^= 0x55;
        } else {
            bits[i] ^= 0xaa;
        }
    }
    const ErrorCorrectionLevel *level = ErrorCorrectionLevel::kLevels[metadata.error_correction_level];
    reed_solomon *rs = reed_solomon_new(level->data_shard_count, level->parity_shard_count);
    if (!rs) {
        Errorf(error_, "not enough memory: reed_solomon_new");
        return false;
    }

    const int payload_size = level->ComputePayloadSize(static_cast<int>(size));
    const int shard_size = (payload_size + level->data_shard_count - 1) / level->data_shard_count;
    std::unique_ptr<uint8_t*[]> shards(new uint8_t*[level->GetTotalShardCount()]);
    std::unique_ptr<uint8_t[]> marks(new uint8_t[level->GetTotalShardCount()]);
    for (int i = 0; i < level->GetTotalShardCount(); i++) {
        shards[i] = new uint8_t[shard_size];

        uint8_t *p = bits + i * (shard_size + 2); // 2 == crc16 check sum
        uint16_t check_sum = (static_cast<uint16_t>(p[1]) >> 8) | p[0];
        (void)check_sum;
        memcpy(shards[i], p + 2, shard_size);

        // TODO: use crc16 checksum.
        marks[i] = 0;
    }

    int err = reed_solomon_reconstruct(rs, shards.get(), marks.get(), level->GetTotalShardCount(),
                                      shard_size);
    if (err < 0) {
        Errorf(error_, "reed solomon decode missing fail!");
        for (int i = 0; i < level->GetTotalShardCount(); i++) {
            delete [] shards[i];
        }
        return false;
    }

    for (int i = 0; i < level->GetTotalShardCount(); i++) {
        memcpy(bits + i * shard_size, shards[i], shard_size);
        delete [] shards[i];
    }
    reed_solomon_release(rs);
    return true;
}

bool Reader::TestSample(int r, double begin_angle, double end_angle) const {
    std::uniform_real_distribution<> random1(.0, ring_width_ - 1);
    std::uniform_real_distribution<> random2(begin_angle, end_angle);
    std::minstd_rand minstd;
    
    int count_one = 0, count_zero = 0;
    for (int i = 0; i < sample_times_; i++) {
        double t = r + 1 + random1(minstd);
        double angle = random2(minstd);
        
        int x = static_cast<int>(center_x_ + t * cos(angle));
        int y = static_cast<int>(center_y_ - t * sin(angle));
        
        // channels == 1;
        // TODO:
        uchar pixel = input_.at<uchar>(cv::Point{x, y});
        if (pixel != 0xff) {
            count_one++;
        } else {
            count_zero++;
        }
  
        if (debug()) {
            cv::line(dummy_, {x - 1, y}, {x + 1, y}, {255, 0, 0});
            cv::line(dummy_, {x, y - 1}, {x, y + 1}, {255, 0, 0});
        }
    }
    return count_one > count_zero;
}

} // namespace ring


std::string ReadRawImage(const cv::Mat &input,
                         int center_x,
                         int center_y,
                         double ring_width,
                         std::vector<cv::Mat> *debug_progress,
                         std::string *err) {
    if (center_x < 0 || center_x > input.cols) {
        Errorf(err, "center_x out of range: %d", center_x);
        return "";
    }
    if (center_y < 0 || center_y > input.rows) {
        Errorf(err, "center_y out of range: %d", center_y);
        return "";
    }
    
    return ring::Reader(input, debug_progress, err)
        .WithCenterX(center_x)
        .WithCenterY(center_y)
        .WithRingWidth(ring_width)
        .WithSampleTimes(23)
        .Read();
}
