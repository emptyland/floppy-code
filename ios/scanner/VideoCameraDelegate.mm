//
//  VideoCameraDelegate.m
//  ringcode
//
//  Created by Niko on 2019/12/14.
//  Copyright © 2019 Niko. All rights reserved.
//

#import <opencv2/imgcodecs/ios.h>
#import "VideoCameraDelegate.h"
#import "ring-code.h"

@interface VideoCameraDelegate ()
@property (weak, nonatomic) ViewController *owner;
@end

@implementation VideoCameraDelegate

- (VideoCameraDelegate *)initWithViewController: (ViewController *)owner {
    NSLog(@"init");
    self->ticks = 0;
    self.owner = owner;
    return self;
}

- (void)reset {
    self->ticks = 0;
}

- (void)processImage:(cv::Mat&)image {
    
    if (self->ticks++ >= 100) {
        dispatch_queue_t mainQueue = dispatch_get_main_queue();
        dispatch_async(mainQueue, ^{
            [self.owner processorDidScanDone:@"OK"];
        });
    }
    NSLog(@"capture! %d", self->ticks);
}

- (void)processNativeImage:(UIImage *)image {
    cv::Mat mat;
    UIImageToMat(image, mat);
    self->ticks++;
    
    std::vector<cv::Mat> debug_progress;
    std::string err;
    CaptureRingCodePhoto(mat, &debug_progress, &err);
    NSLog(@"process! %d", self->ticks);
}

@end

