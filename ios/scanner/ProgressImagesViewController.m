//
//  ProgressImagesViewController.m
//  ringcode
//
//  Created by Niko on 2019/12/21.
//  Copyright © 2019 Niko. All rights reserved.
//

#import "ProgressImagesViewController.h"

@interface ProgressImagesViewController ()
@property (weak, nonatomic) NSArray<UIImage *> *progressImages;
@property (weak, nonatomic) IBOutlet UIImageView *progressImageView;
@property (weak, nonatomic) IBOutlet UISegmentedControl *imagesSegment;
@end

@implementation ProgressImagesViewController

- (ProgressImagesViewController *)initWithData:(NSArray<UIImage *> *)data coder:(NSCoder *)coder; {
    self = [super initWithCoder:coder];
    self.progressImages = data;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.imagesSegment removeAllSegments];
    for (int i = 0; i < self.progressImages.count; i++) {
        NSString *title = [NSString stringWithFormat:@"%d", i+1];
        [self.imagesSegment insertSegmentWithTitle:title atIndex:i animated:NO];
    }
    [self.imagesSegment setSelectedSegmentIndex: 0];
    
    if (self.progressImages.count > 0) {
        self.progressImageView.image = [self.progressImages objectAtIndex:0];
    }
}

- (IBAction)didSegmentChanged:(UISegmentedControl *)sender {
    self.progressImageView.image = [self.progressImages objectAtIndex:sender.selectedSegmentIndex];
}

#pragma mark - Table view data source

/*
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:<#@"reuseIdentifier"#> forIndexPath:indexPath];
    
    // Configure the cell...
    
    return cell;
}
*/

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
