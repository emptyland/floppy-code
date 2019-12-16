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
- (ResultViewController *) initWithData: (NSArray *)data coder:(NSCoder *)coder;
@end

NS_ASSUME_NONNULL_END
