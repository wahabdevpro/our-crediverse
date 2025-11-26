//
//  ThemeEngine.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/09.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation
import UIKit

class ThemeEngine {
    
    // Applies the theme to a view controller
    class func applyTheme(controller: UIViewController) {
        
        // Get the navigational controller
        if let navController = controller.navigationController {
            
            // Apply the theme to the navigation controller
            applyTheme(navController)
            
        }
        
        // Apply the theme to the view
        applyTheme(controller.view)
        
    }
    
    // Applies theme to the navigational controller
    class func applyTheme(navigationViewController: UINavigationController) {
        
        
    }
    
    // Applies theme to the view
    class func applyTheme(view: UIView) {
        
        // Get the dictionary from the ThemeConfiguration.plist
        let path = NSBundle.mainBundle().pathForResource("ThemeConfiguration", ofType: "plist")
        let dict = NSDictionary(contentsOfFile: path!)
        
        // Get the config
        let themeConfig: AnyObject? = dict?.objectForKey("theme")
        
        // Handle the background
        iterateViews(view, themeConfig: themeConfig!)
    }
    
    private class func iterateViews(view: UIView, themeConfig: AnyObject) {
        
        // Skip if it is a UITextField
        if view is UITextField {
            
            return
            
        }
        
        // Check if it is a button
        if view is UIButton {
            
            // If it has a background colour
            if view.backgroundColor != nil {
                
                // Apply theme colour
                view.backgroundColor = colorWithHexString(themeConfig.objectForKey("button_background") as! String)
                
            }
            
            return
            
        }
        
        // Apply the background colour to the view
        view.backgroundColor = colorWithHexString(themeConfig.objectForKey("background") as! String)
        
        // Iterate through the sub views
        for v in view.subviews {
            
            // Apply the theme to the view
            iterateViews(v , themeConfig: themeConfig)
            
        }
    }
    
    // Takes a hex string and convert to UIColor
    private class func colorWithHexString (hex:String) -> UIColor {
        var cString:String = hex.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet() as NSCharacterSet).uppercaseString
        
        if (cString.hasPrefix("#")) {
            cString = cString.substringFromIndex(cString.startIndex.advancedBy(1))
        }
        
        if (cString.characters.count != 6) {
            return UIColor.grayColor()
        }
        
        var rgbValue:UInt32 = 0
        NSScanner(string: cString).scanHexInt(&rgbValue)
        
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
    
}