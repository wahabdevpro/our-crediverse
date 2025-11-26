//
//  MainViewController.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/14.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class MainViewController: UIViewController {

    @IBOutlet weak var appNameLabel: UILabel!
    @IBOutlet weak var myServicesButton: UIButton!
    @IBOutlet weak var availableServicesButton: UIButton!
    
    // DEMO
    static var startup: Bool = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Initialise the locale dictionary
        Locale.Init()
        
        // Apply theme to page
        ThemeEngine.applyTheme(self)
        
    }
    
    override func viewWillAppear(animated: Bool) {
        locale()
    }
    
    override func viewDidAppear(animated: Bool) {
        
        // For demo purposes, check if started up
        if !MainViewController.startup {
            
            // Set the startup variable once
            MainViewController.startup = true
            
            // Display the login page
            let loginViewController = self.storyboard?.instantiateViewControllerWithIdentifier("login") as! LoginViewController
            let navigationController = UINavigationController(rootViewController: loginViewController)
            self.navigationController?.presentViewController(navigationController, animated: true, completion: nil)
            
        }
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func changeNumber(sender: AnyObject) {
        
        // Display the login page
        let loginViewController = self.storyboard?.instantiateViewControllerWithIdentifier("login") as! LoginViewController
        let navigationController = UINavigationController(rootViewController: loginViewController)
        self.navigationController?.presentViewController(navigationController, animated: true, completion: nil)
        
    }
    
    // Change the locale for all text on the screen
    func locale() {
        
        appNameLabel.text = Locale.toString(id: "app_name")
        myServicesButton.setTitle(Locale.toString(id: "my_services"), forState: UIControlState.Normal)
        availableServicesButton.setTitle(Locale.toString(id: "available_services"), forState: UIControlState.Normal)
        
    }
    
    // MARK: - Navigation
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        // Check if destination controller is my services or available services
        let destinationViewController: SevicesTableViewController = segue.destinationViewController as! SevicesTableViewController
        destinationViewController.active = segue.identifier == "MY_SERVICES"
        
        // Change the title of the destination controller to the button text
        let btn = sender as! UIButton
        destinationViewController.title = btn.titleLabel?.text;
        
    }

}
