//
//  ContactInfo.swift
//  HxC
//
//  Created by Justin Guedes on 2015/09/22.
//  Copyright © 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class ContactInfo: NSObject {

    var name: String = ""
    
    override init() {
        
    }
    
    init(name: String) {
        
        self.name = name
        
    }
    
}
