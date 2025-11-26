//
//  RemoveQuotaRequest.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/12.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class RemoveQuotaRequest: RequestHeader {
    
    var serviceID: String = ""
    var variantID: String = ""
    var subscriberNumber: Number = Number()
    var memberNumber: Number = Number()
    var quota: ServiceQuota = ServiceQuota()
    
}