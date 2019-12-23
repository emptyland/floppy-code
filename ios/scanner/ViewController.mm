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
#import "ProgressImagesViewController.h"

@interface ViewController ()<UIImagePickerControllerDelegate, UINavigationControllerDelegate>
@property (strong, nonatomic) CvVideoCamera *videoCamera;
@property (strong, nonatomic) VideoCameraDelegate *videoCameraDelegate;
@property (weak, nonatomic) IBOutlet UIView *cameraView;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *startStopCameraBarButtonItem;
@property (strong, nonatomic) NSMutableArray *contentModel;
@property (strong, nonatomic) NSArray *progressImagesModel;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.contentModel = [[NSMutableArray alloc] init];
    self.progressImagesModel = [[NSArray alloc] init];
    self.videoCameraDelegate = [[VideoCameraDelegate alloc] initWithViewController:self];
    self.videoCamera = [[CvVideoCamera alloc] initWithParentView:self.cameraView];
    self.videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
    self.videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPresetHigh;
    self.videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
    self.videoCamera.useAVCaptureVideoPreviewLayer = YES;
    self.videoCamera.defaultFPS = 40;
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

- (IBAction)onOpenResultView:(id)sender {
    if (self.videoCamera.running) {
        [self stopCamera];
    }

    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"ResultViewController"
                                                               creator: ^(NSCoder *coder) {
        return [[ResultViewController alloc] initWithData:self.contentModel coder:coder]; }];
    [self presentViewController:vc animated:YES completion:nil];
}

- (IBAction)onSelectPhotosAlbum:(id)sender {
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    picker.modalPresentationStyle = UIModalPresentationFullScreen;
    picker.delegate = self;
    picker.allowsEditing = NO;
    [self presentViewController:picker animated:YES completion:nil];
}

- (void)processorDidScanDone: (NSString *)content progressImages: (NSArray *)images {
    AudioServicesPlayAlertSound(1151);
    
    [self stopCamera];
    [self.contentModel addObject:content];
    if (images != nil) {
        self.progressImagesModel = images;
    }
    
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"ResultViewController"
                                                               creator: ^(NSCoder *coder) {
        return [[ResultViewController alloc] initWithData:self.contentModel coder:coder]; }];
//    UIViewController *vc = [sb instantiateViewControllerWithIdentifier:@"ProgressImagesViewController"
//                                                           creator: ^(NSCoder *coder) {
//    return [[ProgressImagesViewController alloc] initWithData:self.progressImagesModel coder:coder]; }];
    //[self.view addSubview:vc.view];
    //[self addChildViewController:vc];
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

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey, id> *)info {
    NSString *type = [info objectForKey:UIImagePickerControllerMediaType];
    UIImage *image = nil;
    if ([type isEqualToString:@"public.image"]) {
        NSString *key = nil;
        if (picker.allowsEditing) {
            key = UIImagePickerControllerEditedImage;
        } else {
            key = UIImagePickerControllerOriginalImage;
        }
        image = [info objectForKey:key];
    }
    [picker dismissViewControllerAnimated:YES completion:^{}];
    
    if (image != nil) {
        [self.videoCameraDelegate processNativeImage:image];
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:^{}];
}



@end
