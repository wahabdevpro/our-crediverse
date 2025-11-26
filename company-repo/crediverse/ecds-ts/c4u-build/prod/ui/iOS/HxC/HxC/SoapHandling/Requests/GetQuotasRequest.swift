//
//  GetQuotasRequest.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/10.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class GetQuotasRequest: RequestHeader {
   
    var serviceID: String = ""
    var variantID: String = ""
    var subscriberNumber: Number = Number()
    var memberNumber: Number = Number()
    var quotaID: String?
    var service: String?
    var destination: String?
    var timeOfDay: String?
    var daysOfWeek: String?
    var activeOnly: Bool?
    
}
