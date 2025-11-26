//
//  PhoneBook.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/10.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit
import AddressBook
import AddressBookUI

class PhoneBook: NSObject {
    
    // Stores basic information of a contact
    class Contact {
        
        var msisdn: String = ""
        
        var firstName: String = ""
        var middleName: String = ""
        var lastName: String = ""
        
        var image: UIImage?
        
    }
    
    // Reference to the address book
    static var addressBook : ABAddressBookRef? = ABAddressBookCreateWithOptions(nil, nil).takeRetainedValue()
    
    // An array of the contacts
    private static var contacts: [Contact] = []
    
    // Request authorization from the address book
    class func requestAuthorization() {
        
        // Get permission
        ABAddressBookRequestAccessWithCompletion(addressBook, {
            
            (granted : Bool, error: CFError!) -> Void in
            
            if granted == true {
                
                // Set the address book
                PhoneBook.addressBook = ABAddressBookCreateWithOptions(nil, nil).takeRetainedValue()
                
                // Load the contacts
                self.loadContacts()
                
            }
        
        })
        
        
    }
    
    // Get a contact from number
    class func lookup(msisdn: String) -> Contact? {
        
        // Get status od permission
        let status = ABAddressBookGetAuthorizationStatus()
        
        // If authorized
        if status == ABAuthorizationStatus.Authorized {
            
            // Check if contacts are empty
            if contacts.isEmpty {
                
                // Load the contacts
                self.loadContacts()
                
            }
            
            // Format the msisdn
            let formattedMsisdn = msisdn.stringByReplacingOccurrencesOfString("[^0-9]", withString: "", options: NSStringCompareOptions.RegularExpressionSearch, range: nil)
            
            // Iterate through the contacts
            for contact in contacts {
                
                // Compare the msisdn
                if contact.msisdn == formattedMsisdn {
                    
                    return contact
                    
                }
                
            }
            
        // Else handle if status is denied or restricted
        } else if status == ABAuthorizationStatus.Denied || status == ABAuthorizationStatus.Restricted {
            
            print("Do Something")
            
        // Else if status is not determined
        } else if status == ABAuthorizationStatus.NotDetermined {
            
            // Request authorization
            requestAuthorization()
            
            // return lookup(msisdn)
            
        }
        
        return nil
        
    }
    
    // Open the contact view controller
    class func open(viewController: UIViewController, delegate: ABPeoplePickerNavigationControllerDelegate) {
        
        let picker = ABPeoplePickerNavigationController()
        picker.peoplePickerDelegate = delegate
        viewController.presentViewController(picker, animated: true, completion: nil)
        
    }
    
    // Convert a record to a contact
    class func toContact(person: ABRecordRef) -> Contact {
        
        // Get the details of the person
        let numbers: AnyObject = ABRecordCopyValue(person, kABPersonPhoneProperty).takeUnretainedValue()
        
        // Get the names
        let firstName = ABRecordCopyValue(person, kABPersonFirstNameProperty) != nil ? ABRecordCopyValue(person, kABPersonFirstNameProperty).takeUnretainedValue() as? String : nil
        let middleName = ABRecordCopyValue(person, kABPersonMiddleNameProperty) != nil ? ABRecordCopyValue(person, kABPersonMiddleNameProperty).takeUnretainedValue() as? String : nil
        let lastName = ABRecordCopyValue(person, kABPersonLastNameProperty) != nil ? ABRecordCopyValue(person, kABPersonLastNameProperty).takeUnretainedValue() as? String : nil
        
        // Get the image
        let image: NSData? =  ABPersonCopyImageDataWithFormat(person, kABPersonImageFormatThumbnail) != nil ? ABPersonCopyImageDataWithFormat(person, kABPersonImageFormatThumbnail).takeUnretainedValue() : nil
        
        // Get the msisdn
        var msisdn: String?
        
        // Make sure there is a number
        if ABMultiValueGetCount(numbers) > 0 {
            
            msisdn = ABMultiValueCopyValueAtIndex(numbers, 0).takeUnretainedValue() as? String
            
        }
        
        // Create the contact
        let contact: Contact = Contact()
        
        // Get the respective values
        
        if let kMsisdn = msisdn {
            contact.msisdn = kMsisdn.stringByReplacingOccurrencesOfString("[^0-9]", withString: "", options: NSStringCompareOptions.RegularExpressionSearch, range: nil)
        }
        
        if let kFirstName = firstName {
            contact.firstName = kFirstName
        }
        
        if let kMiddleName = middleName {
            contact.middleName = kMiddleName
        }
        
        if let kLastName = lastName {
            contact.lastName = kLastName
        }
        
        if let kImage = image {
            contact.image = UIImage(data: kImage)
        }
        
        return contact
        
    }
    
    class func updateContactName(msisdn: String, name: String) {
        
        for (var i = 0; i < contacts.count; i++) {
            
            if contacts[i].msisdn == msisdn {
                
                contacts[i].firstName = name
                
                return
            }
            
        }
        
        let contact = Contact()
        contact.msisdn = msisdn
        contact.firstName = name
        
        self.contacts.append(contact)
        
    }
    
    // Loads the contacts array
    private class func loadContacts() {
        
        // Get all the contacts
        let allContacts : NSArray = ABAddressBookCopyArrayOfAllPeople(self.addressBook).takeRetainedValue()
        
        // Iterate through the contacts
        for contactRef:ABRecordRef in allContacts {
            
            // Add the contacts to the array
            self.contacts.append(self.toContact(contactRef))
            
        }
        
    }
    
}
