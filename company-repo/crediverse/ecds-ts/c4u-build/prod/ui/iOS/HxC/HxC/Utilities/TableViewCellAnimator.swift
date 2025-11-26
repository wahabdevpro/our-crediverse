//
//  TableViewCellAnimator.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/10.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class TableViewCellAnimator {
    
    // Creates a fading animation for the table view cells
    class func fadeAnimation(cell: UITableViewCell) {
        
        let view = cell.contentView
        view.layer.opacity = 0.1
        UIView.animateWithDuration(1.4, animations: {
            view.layer.opacity = 1
        })
        
    }
    
    // Plays a rotating animation with the table view cells
    class func rotationAnimation(cell: UITableViewCell) {
        
        let view = cell.contentView
        let rotationDegrees: CGFloat = -15.0
        let rotationRadians: CGFloat = rotationDegrees * (CGFloat(M_PI)/180.0)
        let offset = CGPointMake(-20, -20)
        var startTransform = CATransform3DIdentity
        startTransform = CATransform3DRotate(CATransform3DIdentity,
            rotationRadians, 0.0, 0.0, 1.0)
        startTransform = CATransform3DTranslate(startTransform, offset.x, offset.y, 0.0)
        
        view.layer.transform = startTransform
        view.layer.opacity = 0.8
        
        UIView.animateWithDuration(0.4) {
            view.layer.transform = CATransform3DIdentity
            view.layer.opacity = 1
        }
        
    }
    
}