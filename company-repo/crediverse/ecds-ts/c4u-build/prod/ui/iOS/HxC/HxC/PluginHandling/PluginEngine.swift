//
//  PluginEngine.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/10.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation
import UIKit

class PluginEngine {
    
    // Singleton approach - There can be only one Plugin Engine at any time
    class var instance: PluginEngine {
        
        struct Singleton {
            
            static let instance = PluginEngine()
            
        }
        
        return Singleton.instance;
        
    }
    
    // Stores the currently available plugins
    private var plugins: [IPlugin]
    
    private var defaultPlugin: UIViewController
    
    private var currentStoryboard: UIStoryboard!
    
    // Constructor
    private init() {
        
        plugins = [ LanguageChange(), CreditSharing() ]
        
        defaultPlugin = UIStoryboard(name: "Main", bundle: NSBundle.mainBundle()).instantiateViewControllerWithIdentifier("PluginUnavailable") 
        
    }
    
    func load(plugin: IPlugin) {
        
        plugins.append(plugin)
        
    }
    
    func present(viewController viewController: UIViewController, serviceID: String, params: ((pluginViewController: UIPluginViewController) -> Void)? = nil) -> Bool {
        
        var plugin: IPlugin?
        
        // Iterate through all the available plugins
        for p in plugins {
            
            // Check if the service ID's equal
            if p.serviceID == serviceID {
                
                plugin = p
                
            }
            
        }
        
        // If plugin is not nil then a plugin was found
        if plugin != nil {
            
            return present(viewController: viewController, plugin: plugin!, params: params)
            
        }
        
        present(viewController: viewController, nextViewController: defaultPlugin)
        
        return false
    }
    
    func present(viewController viewController: UIViewController, plugin: IPlugin, params: ((pluginViewController: UIPluginViewController) -> Void)? = nil) -> Bool {
        
        // First get the storyboard to display
        currentStoryboard = UIStoryboard(name: plugin.storyboardName, bundle: nil)
        
        // Instantiate the initial view controller of the storyboard
        // Initial view controller would be the central controller of the service where all other view controllers in the storyboard can be accessed through
        let initialViewController: UIPluginViewController = currentStoryboard.instantiateInitialViewController() as! UIPluginViewController
        
        if params != nil {
            params!(pluginViewController: initialViewController)
        }
        
        return present(viewController: viewController, nextViewController: initialViewController)
        
    }
    
    func present(viewController viewController: UIViewController, nextViewController: UIViewController) -> Bool {
        
        // Configure the view controller according to the theme
        ThemeEngine.applyTheme(nextViewController)
        
        if let navController = viewController.navigationController {
            
            navController.pushViewController(nextViewController, animated: true)
            
            return true
        }
        
        // Present the view controller from the current view controller
        viewController.presentViewController(nextViewController, animated: true, completion: {
            
            // The presenting is completed and additional code goes here
            
        })
        
        return true
        
    }
    
    func getPlugin(serviceID: String) -> IPlugin? {
        
        var plugin: IPlugin?
        
        // Iterate through all the available plugins
        for p in plugins {
            
            // Check if the service ID's equal
            if p.serviceID == serviceID {
                
                plugin = p
                
            }
            
        }
        
        return plugin
        
    }
}