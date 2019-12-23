//
//  ProgressImagesViewController.h
//  ringcode
//
//  Created by Niko on 2019/12/21.
//  Copyright © 2019 Niko. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ProgressImagesViewController : UIViewController
- (ProgressImagesViewController *) initWithData: (NSArray<UIImage *> *)data coder:(NSCoder *)coder;
@end

NS_ASSUME_NONNULL_END
