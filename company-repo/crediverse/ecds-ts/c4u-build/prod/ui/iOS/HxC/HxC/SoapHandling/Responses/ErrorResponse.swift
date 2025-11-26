//
//  ErrorResponse.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/10.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class ErrorResponse: ResponseHeader {
   
    var error: String
    
    init(error: String) {
        self.error = error
    }
    
}
