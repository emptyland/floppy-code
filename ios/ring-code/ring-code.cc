#include "ring-code.h"
#include <opencv2/opencv.hpp>
#include <mutex>
extern "C" {
#include "rs.h"
} // extern "C"

static std::once_flag initlized_once;

void EnsureRingCodeLibraryInitialized() {
    std::call_once(initlized_once, []() {
        fec_init(); // init reed solomon library.
    });
}
