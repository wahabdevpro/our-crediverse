//
//  AddQuotaRequest.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/09.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class AddQuotaRequest: RequestHeader {
   
    var serviceID: String = ""
    var variantID: String = ""
    var subscriberNumber: Number = Number()
    var memberNumber: Number = Number()
    var quota: ServiceQuota = ServiceQuota()
    
}
