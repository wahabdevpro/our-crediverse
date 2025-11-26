//
//  UITextField.swift
//  HxC
//
//  Created by Justin Guedes on 2015/09/08.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

extension UITextField {
    
    // Enables the field
    func enable() {
        
        self.enabled = true;
        self.backgroundColor = UIColor.whiteColor()
        self.textColor = UIColor.blackColor()
        
    }
    
    // Disables the field
    func disable() {
        
        self.enabled = false;
        self.backgroundColor = UIColor.lightGrayColor()
        self.textColor = UIColor.blackColor()
        
    }
    
}