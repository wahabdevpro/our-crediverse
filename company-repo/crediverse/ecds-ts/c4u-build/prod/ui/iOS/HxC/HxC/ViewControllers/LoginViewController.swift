//
//  LoginViewController.swift
//  HxC
//
//  Created by Justin Guedes on 2015/07/29.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController {

    @IBOutlet weak var numberTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var doneButton: UIBarButtonItem!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Disable the button
        doneButton.enabled = false
        
        // Check if there is a default number
        if let defaultNumber = NSUserDefaults.standardUserDefaults().objectForKey("msisdn") as? String {
            
            // Assign the number to the textfield
            numberTextField.text = defaultNumber
            
            // Make user enter in the password
            passwordTextField.becomeFirstResponder()
            return
            
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func textFieldEditted(sender: AnyObject) {
        
        // Enable the done button if the number textfield is not empty
        doneButton.enabled = !numberTextField.text!.isEmpty && !passwordTextField.text!.isEmpty
        
    }
    
    @IBAction func dismiss(sender: AnyObject) {
        
        // Resign the keyboard
        numberTextField.resignFirstResponder()
        passwordTextField.resignFirstResponder()
        
        if passwordTextField.text!.characters.count < 4 {
            
            passwordTextField.text = ""
            Alert.show(self, title: "Invalid Number/Password", message: "The details entered are incorrect.")
            
            // Disable the button
            doneButton.enabled = false
            
            return
        }
        
        let last4Digits = numberTextField.text!.substringWithRange(Range<String.Index>(start: numberTextField.text!.endIndex.advancedBy(-4), end: numberTextField.text!.endIndex))
        
        if passwordTextField.text != last4Digits {
            
            passwordTextField.text = ""
            Alert.show(self, title: "Invalid Number/Password", message: "The details entered are incorrect.")
            
            // Disable the button
            doneButton.enabled = false
            
            return
        }
        
        // Set the user number to the number textfield
        User.number = numberTextField.text!
        
        // Store the msisdn
        NSUserDefaults.standardUserDefaults().setObject(numberTextField.text, forKey: "msisdn")
        NSUserDefaults.standardUserDefaults().synchronize()
        
        // Reset the cache
        HxC.refresh()
        
        // Dismiss this view controller
        self.navigationController?.dismissViewControllerAnimated(true, completion: nil)
        
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
