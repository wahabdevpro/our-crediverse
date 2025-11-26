//
//  GetServiceRequest.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/05.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

class GetServiceRequest: RequestHeader {
    
    var subscriberNumber: Number = Number()
    var serviceID: String = ""
    var variantID: String = ""
    var activeOnly: Bool = false
    
}