//
//  IPlugin.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/10.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

protocol IPlugin {
    
    // Required to load the GUI
    var storyboardName: String { get }
    
    // Required for soap transactions
    var serviceID: String { get }
    
    // Name that this service should display
    var name: String { get }
    
    // Description of the service itself
    var description: String { get }
    
}