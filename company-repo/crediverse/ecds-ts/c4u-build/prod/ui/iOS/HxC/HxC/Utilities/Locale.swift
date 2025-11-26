//
//  Locale.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/12.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import Foundation

class Locale {
    
    static var languageID = 1
    static var languageCode = "eng"
    static var languageName = "English"
    static var alphabet = "latn"
    static var dateFormat = ""
    static var encodingScheme = ""
    static var currencyDecimalDigits = 2
    
    static let prefixCurrencySymbol = "$"
    static let suffixCurrencySymbol = ""
    
    static var currentDictionary: NSDictionary?
    
    class func Init() {
        
        // Get the stored defaults
        let userDefaults = NSUserDefaults.standardUserDefaults()
        
        // Get the language ID
        if let languageID = userDefaults.objectForKey("languageID") as? Int {
            
            self.languageID = languageID
            
        }
        
        // Get the language code
        if let languageCode = userDefaults.objectForKey("languageCode") as? String {
            
            self.languageCode = languageCode
            
        }
        
        // Get the language name
        if let languageName = userDefaults.objectForKey("languageName") as? String {
            
            self.languageName = languageName
            
        }
        
        // Get the alphabet
        if let alphabet = userDefaults.objectForKey("alphabet") as? String {
            
            self.alphabet = alphabet
            
        }
        
        // Get the date format
        if let dateFormat = userDefaults.objectForKey("dateFormat") as? String {
            
            self.dateFormat = dateFormat
            
        }
        
        // Get the encoding scheme
        if let encodingScheme = userDefaults.objectForKey("encodingScheme") as? String {
            
            self.encodingScheme = encodingScheme
            
        }
        
        // Get the currency decimal digits
        if let currencyDecimalDigits = userDefaults.objectForKey("currencyDecimalDigits") as? Int {
            
            self.currencyDecimalDigits = currencyDecimalDigits
            
        }
        
    }
    
    // Updates the locale from the server
    class func update(locale: (() -> Void)?) {
        
        // Get the locale settings
        HxC.getLocaleSettings(completion: {
            
            (response: GetLocaleSettingsResponse) in
            
            // Ensure it is a successful response
            if response.returnCode == "success" {
                
                // Set the locale
                Locale.languageCode = response.languageCode
                Locale.languageName = response.name
                Locale.alphabet = response.alphabet
                Locale.dateFormat = response.dateFormat
                Locale.encodingScheme = response.encodingScheme
                Locale.currencyDecimalDigits = response.currencyDecimalDigits
                
                // Save the locale
                Locale.save()
                
                // Execute the method once it is done
                if locale != nil {
                    
                    locale!()
                    
                }
                
            }
            
        })
        
    }
    
    // Saves the current locale
    class func save() {
        
        let userDefaults = NSUserDefaults.standardUserDefaults()
        
        userDefaults.setInteger(languageID, forKey: "languageID")
        userDefaults.setObject(languageCode, forKey: "languageCode")
        userDefaults.setObject(languageName, forKey: "languageName")
        userDefaults.setObject(alphabet, forKey: "alphabet")
        userDefaults.setObject(dateFormat, forKey: "dateFormat")
        userDefaults.setObject(encodingScheme, forKey: "encodingScheme")
        userDefaults.setInteger(currencyDecimalDigits, forKey: "currencyDecimalDigits")
        
        // Save
        userDefaults.synchronize()
        
    }
    
    // Converts a currency value to the formatted string
    class func toCurrency(charge: Float) -> String {
        
        let chargeFormatted = NSString(format: "%.0\(currencyDecimalDigits)f", charge)
        
        return "\(prefixCurrencySymbol)\(chargeFormatted)\(suffixCurrencySymbol)"
        
    }
    
    // Converts an id into the language string from the dictionaries
    class func toString(serviceId: String = "main", id: String) -> String {
        
        // Get the current dictionary
        if let dictionary = currentDictionary {
            
            // Get the service from teh service id
            if let _ = dictionary[serviceId] as? String {
                
                // Get the dictionary with correct language from the language code
                if let lang = dictionary[languageCode] as? NSDictionary {
                    
                    // Get the string from the dictionary
                    if let str = lang[id] as? String {
                        
                        return str
                        
                    // Else get string from the dictionary
                    } else if let str = dictionary[id] as? String {
                        
                        return str
                        
                    } else {
                        
                        return "Error, Can't find id \(id) in \(serviceId).plist"
                        
                    }
                    
                // Else use the default dictionary
                } else {
                    
                    // Get the string from the dictionary
                    if let str = dictionary[id] as? String {
                        
                        return str
                        
                    } else {
                        
                        return "Error, Can't find id \(id) in \(serviceId).plist"
                        
                    }
                    
                }
                
            // Else load the dictionary
            } else {
                
                // Load the dictionary
                if loadDictionary(serviceId) {
                    
                    return toString(serviceId, id: id)
                    
                } else {
                    
                    return "Can't find \(serviceId).plist"
                    
                }
                
            }
            
        // Else load the dictionary
        } else {
            
            // Load the dictionary
            if loadDictionary(serviceId) {
                
                // Call same method again
                return toString(serviceId, id: id)
                
            } else {
             
                return "Can't find \(serviceId).plist"
                
            }
            
        }
        
    }
    
    private class func loadDictionary(serviceId: String) -> Bool {
        
        let dictionaryPath = NSBundle.mainBundle().pathForResource(serviceId, ofType: "plist")
        currentDictionary = NSDictionary(contentsOfFile: dictionaryPath!)
        
        return currentDictionary != nil
        
    }
    
}