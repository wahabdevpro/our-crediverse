//
//  HxCTests.swift
//  HxCTests
//
//  Created by Justin Guedes on 2015/04/09.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit
import XCTest

class HxCTests: XCTestCase {
    
    class TestPlugin {
        
        var storyboardName: String = "Test"
        var name: String = "Test Plugin"
        var serviceID: String = "tstplg"
        
    }
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testExample() {
        // This is an example of a functional test case.
        XCTAssert(true, "Pass")
    }
    
    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measureBlock() {
            // Put the code you want to measure the time of here.
        }
    }
    
    func testPluginLoading() {
        
        // let engine = PluginEngine.instance
        
        
    }
    
}
