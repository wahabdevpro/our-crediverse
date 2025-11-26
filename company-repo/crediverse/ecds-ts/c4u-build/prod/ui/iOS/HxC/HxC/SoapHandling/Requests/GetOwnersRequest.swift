//
//  GetOwnersRequest.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/08.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

class GetOwnersRequest: RequestHeader {
    
    var serviceID: String = ""
    var variantID: String = ""
    var memberNumber: Number = Number()
    
}