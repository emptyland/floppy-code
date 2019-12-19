//
//  VideoCameraDelegate.h
//  ringcode
//
//  Created by Niko on 2019/12/14.
//  Copyright © 2019 Niko. All rights reserved.
//

#import "ViewController.h"
#import <opencv2/videoio/cap_ios.h>
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoCameraDelegate : NSObject<CvVideoCameraDelegate> {
    int ticks;
}
- (VideoCameraDelegate *)initWithViewController: (ViewController *)owner;
- (void)reset;
- (void)processImage:(cv::Mat&)image;
- (void)processNativeImage:(UIImage *)image;
@end

NS_ASSUME_NONNULL_END
