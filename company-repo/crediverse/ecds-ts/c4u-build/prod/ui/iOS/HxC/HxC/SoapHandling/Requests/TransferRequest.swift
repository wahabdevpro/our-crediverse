//
//  TransferRequest.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/10.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class TransferRequest: RequestHeader {
   
    var serviceID: String = ""
    var variantID: String = ""
    var subscriberNumber: Number = Number()
    var recipientNumber: Number = Number()
    var amount = 0
    var pin: String = ""
    
}
