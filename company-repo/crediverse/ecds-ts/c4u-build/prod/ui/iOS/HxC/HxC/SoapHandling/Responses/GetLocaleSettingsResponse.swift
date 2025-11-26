//
//  GetLocaleSettingsResponse.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/12.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class GetLocaleSettingsResponse: ResponseHeader {
    
    var languageCode: String = ""
    var name: String = ""
    var alphabet: String = ""
    var dateFormat: String = ""
    var encodingScheme: String = ""
    var currencyDecimalDigits = 0
    
}