//
//  Date.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/14.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

extension NSDate {
    
    // Format the date into a string
    var format: String {
        let formatter = NSDateFormatter()
        formatter.dateFormat = "yyMMddHHmmss"
        return formatter.stringFromDate(self)
    }
    
    // Get the expired
    func isExpired(seconds: NSTimeInterval) -> Bool {
        
        return -self.timeIntervalSinceNow > seconds
        
    }
    
}
