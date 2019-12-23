//
//  ResultViewController.h
//  ringcode
//
//  Created by Niko on 2019/12/16.
//  Copyright © 2019 Niko. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ResultViewController : UIViewController
- (ResultViewController *) initWithData: (NSArray<NSString *> *)data coder:(NSCoder *)coder;
- (ResultViewController *) initWithData: (NSArray<NSString *> *)data;
@end

NS_ASSUME_NONNULL_END
