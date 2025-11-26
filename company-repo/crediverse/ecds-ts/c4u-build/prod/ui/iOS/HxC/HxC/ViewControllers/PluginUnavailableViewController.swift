//
//  PluginUnavailableViewController.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/14.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class PluginUnavailableViewController: UIViewController {

    @IBOutlet weak var serviceUnavailableLabel: UILabel!
    @IBOutlet weak var contactCustomerCareLabel: UILabel!
    @IBOutlet weak var callCustomerCareButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Apply the theme to the current view
        ThemeEngine.applyTheme(self)
    }
    
    override func viewWillAppear(animated: Bool) {
        
        // Set the text for the view
        locale()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // Sets the various components to localised string
    func locale() {
        
        self.title = Locale.toString(id: "service_unavailable")
        serviceUnavailableLabel.text = Locale.toString(id: "service_unavailable_message")
        contactCustomerCareLabel.text = Locale.toString(id: "service_unavailable_contact")
        callCustomerCareButton.setTitle(Locale.toString(id: "service_unavailable_call"), forState: UIControlState.Normal)
        
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

    @IBAction func callCustomerCare(sender: AnyObject) {
        
        // Calls the number
        if let url = NSURL(string: "tel://0848654805") {
            UIApplication.sharedApplication().openURL(url)
        }
        
    }
    
}
