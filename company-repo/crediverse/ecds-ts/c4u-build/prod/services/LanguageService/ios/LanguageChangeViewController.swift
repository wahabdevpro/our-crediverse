//
//  LanguageChangeViewController.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/15.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class LanguageChangeViewController: UIPluginViewController, UITextFieldDelegate, UIPickerViewDelegate, UIPickerViewDataSource {

    @IBOutlet weak var languageLabel: UILabel!
    @IBOutlet weak var serviceDescriptionLabel: UILabel!
    @IBOutlet weak var selectLanguageLabel: UILabel!
    
    @IBOutlet weak var languageTextField: UITextField!
    @IBOutlet weak var languagesPicker: UIPickerView!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    
    private var pluginInfo: IPlugin = LanguageChange()
    private var languages: [String] = []
    private var current: String = "unknown"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Apply the theme to the current page
        ThemeEngine.applyTheme(self)
        
        // Reload the information
        reload()
    }

    override func viewWillAppear(animated: Bool) {
        
        // Set the locale for the page
        locale()
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // Sets each component to a localised string
    func locale() -> Void {
        
        serviceDescriptionLabel.text = Locale.toString(pluginInfo.serviceID, id: "service_description")
        selectLanguageLabel.text = Locale.toString(pluginInfo.serviceID, id: "select_language")
        languageLabel.text = Locale.toString(pluginInfo.serviceID, id: "language")
        saveButton.title = Locale.toString(pluginInfo.serviceID, id: "save")
        
    }
    
    // Gets information on the language service
    func reload(force: Bool = false) {
        
        // Set the components
        languagesPicker.hidden = true
        saveButton.enabled = false
        
        // Get the service
        HxC.getService(serviceID: self.pluginInfo.serviceID, force: force, completion: {
            
            (response: GetServiceResponse) in
            
            if (response.returnCode == "success") {
                
                // Append the languages to the language array
                self.languages = []
                for service in response.serviceInfo {
                    
                    self.languages.append(service.variantName)
                    
                    // If language is active, then that is the current language
                    if (service.state == "active") {
                        self.current = service.variantName
                    }
                    
                }
                
                // Reload the components of the picker
                self.languagesPicker.reloadAllComponents()
                self.languageTextField.text = self.current
                
            }
            
            
        })
        
    }
    
    // Migrate the language
    @IBAction func save(sender: AnyObject) {
        
        // Set the label migrating to localised string
        let migrating = Locale.toString(pluginInfo.serviceID, id: "migrating")
        self.view.makeToastActivityWithMessage(message: "\(migrating)...")
        
        // Migrate languages
        HxC.migrate(serviceID: pluginInfo.serviceID, newServiceID: pluginInfo.serviceID, variantID: current, newVariantID: languageTextField.text!, completion: {
            
            (response: MigrateResponse) in
            
            // Hide the toast activity
            self.view.hideToastActivity()
            
            if (response.returnCode == "success") {
                
                // Set the message of the response
                self.view.makeToast(message: response.message)
                
                // Refresh the cache
                HxC.refresh()
                
                // Reload the languages
                self.reload(true)
                
                var index = 1
                
                // Get the language ID
                for var i = 0; i < self.languages.count; i++ {
                    
                    if self.languages[i] == self.languageTextField.text {
                        
                        index = i + 1
                        break
                        
                    }
                    
                }
                
                // Set the locale
                Locale.languageID = index
                Locale.update(self.locale)
                
                return
                
            }
            
            // Else set the response message
            self.view.makeToast(message: response.message)
            
        })
        
    }
    
    // MARK: - UITextfield Delegate
    
    func textFieldShouldBeginEditing(textField: UITextField) -> Bool {
        
        // Select the correct index for the picker
        for var i = 0; i < languages.count; i++ {
            
            if languages[i] == languageTextField.text {
                
                languagesPicker.selectRow(i, inComponent: 0, animated: false)
                break
                
            }
            
        }
        
        // Show the picker
        languagesPicker.hidden = false
        
        // Prevent the keyboard from showing
        return false
        
    }
    
    // MARK: - UIPickerView Datasource
    
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        
        return 1
        
    }
    
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        
        return languages.count
        
    }
    
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        
        return languages[row]
        
    }
    
    // MARK: - UIPickerView Delegate

    func pickerView(pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        
        // Set the language textfield
        languageTextField.text = languages[row]
        
        // Hide the picker
        pickerView.hidden = true
        
        // Enable the save button if the language is not equal to the current language
        if (languages[row] != current) {
            
            saveButton.enabled = true
            
        } else {
            
            saveButton.enabled = false
            
        }
        
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
