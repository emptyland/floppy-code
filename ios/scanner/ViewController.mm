//
//  ViewController.m
//  ringcode
//
//  Created by Niko on 2019/12/10.
//  Copyright © 2019 Niko. All rights reserved.
//

#import <opencv2/videoio/cap_ios.h>
#import "VideoCameraDelegate.h"
#import "ViewController.h"
#import "ResultViewController.h"

@interface ViewController ()
@property (strong, nonatomic) CvVideoCamera *videoCamera;
@property (strong, nonatomic) VideoCameraDelegate *videoCameraDelegate;
@property (weak, nonatomic) IBOutlet UIView *cameraView;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *startStopCameraBarButtonItem;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.videoCameraDelegate = [[VideoCameraDelegate alloc] initWithViewController:self];
    self.videoCamera = [[CvVideoCamera alloc] initWithParentView:self.cameraView];
    self.videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
    self.videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPresetHigh;
    self.videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
    self.videoCamera.useAVCaptureVideoPreviewLayer = YES;
    self.videoCamera.defaultFPS = 30;
    self.videoCamera.grayscaleMode = NO;
    self.videoCamera.delegate = self.videoCameraDelegate;
    
    //[self startCamera];
}

- (IBAction)onStartStopCamera:(UIBarButtonItem *)sender {
    if (self.videoCamera.running) {
        [self stopCamera];
    } else {
        [self startCamera];
    }
}

- (void)processorDidScanDone: (NSString *)content {
    [self stopCamera];
    
    NSArray *data = [[NSArray alloc] initWithObjects:content, @"Last", nil];
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"ResultViewController"
                                                               creator: ^(NSCoder *coder) {
        return [[ResultViewController alloc] initWithData:data coder:coder]; }];
    //[(ResultViewController *)vc pushResult:content];
    [self presentViewController:vc animated:YES completion:nil];
}

- (void)startCamera {
    if (!self.videoCamera.running) {
        [self.videoCameraDelegate reset];
        [self.videoCamera start];
        
        CATransition *animation = [CATransition animation];
        animation.type = kCATransitionMoveIn;
        animation.subtype = kCATransitionFromTop;
        animation.duration = 0.4;
        animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseIn];
        animation.fillMode = kCAFillModeForwards;

        [self.cameraView.layer addAnimation:animation forKey:@"startAnimation"];
        
        self.startStopCameraBarButtonItem.image = [UIImage systemImageNamed:@"camera"];
    }
}

- (void)stopCamera {
    if (self.videoCamera.running) {
        [self.videoCamera stop];
        
        CATransition *animation = [CATransition animation];
        animation.type = kCATransitionPush;
        animation.subtype = kCATransitionFromBottom;
        animation.duration = 0.4;
        animation.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseOut];
        animation.fillMode = kCAFillModeForwards;

        [self.cameraView.layer addAnimation:animation forKey:@"stopAnimation"];
        self.startStopCameraBarButtonItem.image = [UIImage systemImageNamed:@"camera.fill"];
    }
}

@end
