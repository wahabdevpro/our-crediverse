//
//  MigrateRequest.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/13.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

class MigrateRequest: RequestHeader {
    
    var serviceID: String = ""
    var variantID: String = ""
    var newServiceID: String = ""
    var newVariantID: String = ""
    var subscriberNumber: Number = Number()
    
}