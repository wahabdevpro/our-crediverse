//
//  BaseRequest.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/07.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class RequestHeader: NSObject {

    var callerID: String = ""
    var channel: String = ""
    var hostName: String = ""
    var transactionID: String = ""
    var sessionID: String = ""
    var version: String = ""
    var mode: String = ""
    var languageID: Int = -1
    
    override init() {
        super.init()
        
        HxC.populate(self)
    }
    
}
