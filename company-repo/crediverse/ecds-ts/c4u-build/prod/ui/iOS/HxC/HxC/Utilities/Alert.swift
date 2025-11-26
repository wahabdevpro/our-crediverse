//
//  Alert.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/12.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class Alert {
    
    // Show an alert view controller
    class func show(viewController: UIViewController, title: String, message: String) {
        
        // Create the alert view controller
        let alert = UIAlertController(title: title, message: message, preferredStyle: .Alert);
        
        // Create the ok button
        let okButton = UIAlertAction(title: "Ok", style: UIAlertActionStyle.Cancel) {
            
            (action) in
            
        }
        
        // Add the ok button onto the controller
        alert.addAction(okButton)
        
        // Present the alert controller
        viewController.presentViewController(alert, animated: true, completion: nil)
        
    }
    
    // Display an error
    class func error(viewController: UIViewController, title: String, message: String) {
        
        show(viewController, title: title, message: message)
        
    }
    
    // Display a help dialog
    class func help(viewController: UIViewController, title: String, message: String, yes: UIAlertAction, no: UIAlertAction) {
        
        let alert = UIAlertController(title: title, message: message, preferredStyle: .ActionSheet)
        alert.addAction(no)
        alert.addAction(yes)
        viewController.presentViewController(alert, animated: true, completion: nil)
        
    }
    
}