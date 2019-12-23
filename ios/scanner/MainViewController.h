//
//  ViewController.h
//  ringcode
//
//  Created by Niko on 2019/12/10.
//  Copyright © 2019 Niko. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MainViewController : UIViewController
- (void)processorDidScanDone: (NSString *)content progressImages: (NSArray *)images;
@end

