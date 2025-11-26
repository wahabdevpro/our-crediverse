//
//  Number.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/07.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class Number: NSObject {
    
    var addressDigits: String = ""
    var numberType: String = "UNKNOWN"
    var numberPlan: String = "UNKNOWN"
    
    override init() {
        
    }
    
    init(addressDigits: String) {
        self.addressDigits = addressDigits
        self.numberType = "UNKNOWN"
        self.numberPlan = "UNKNOWN"
    }
    
    init(addressDigits: String, numberType: String, numberPlan: String) {
        self.addressDigits = addressDigits
        self.numberType = numberType
        self.numberPlan = numberPlan
    }
    
}
