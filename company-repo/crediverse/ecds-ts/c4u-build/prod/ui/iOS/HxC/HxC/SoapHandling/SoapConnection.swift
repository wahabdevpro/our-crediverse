//
//  BridgeConnection.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/07.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class SoapConnection: NSObject, NSURLConnectionDataDelegate, NSXMLParserDelegate {
    
    let debug: Bool = false
    var data: NSMutableData = NSMutableData()
    var returnType: ResponseHeader = ResponseHeader()
    var mirror: Mirror?
    var completion: ((response: ResponseHeader) -> Void)?
    
    private var xmlParser: NSXMLParser?
    private var header = Mirror(reflecting: ResponseHeader())
    
    // Sends a request synchronously
    func sendRequest(request: NSMutableURLRequest, returnType: ResponseHeader) -> ResponseHeader {
        
        // Initialise the mirror
        self.returnType = returnType
        self.mirror = Mirror(reflecting: self.returnType)
        
        // Create the references
        var response: NSURLResponse?
        
        // Initiate the connection
        let data: NSData?
        do {
            data = try NSURLConnection.sendSynchronousRequest(request, returningResponse: &response)
        } catch _ as NSError {
            data = nil
        }
        
        // Create the xml parser
        xmlParser = NSXMLParser(data: data!)
        xmlParser?.delegate = self
        
        // Parse the request
        xmlParser?.parse()
        
        // Return the response
        return self.returnType
    }
    
    func sendRequest(request: NSMutableURLRequest, returnType: ResponseHeader, completion: (response: ResponseHeader) -> Void) {
        
        // Initialise the mirror
        self.returnType = returnType
        self.mirror = Mirror(reflecting: self.returnType)
        
        // Set the completion handler
        self.completion = completion
        
        // Initiate the connection
        _ = NSURLConnection(request: request, delegate: self)
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // MARK: NSURL Connection Data Delegate
    //
    /////////////////////////////////////
    
    // When the response is recieved
    func connection(connection: NSURLConnection, didReceiveResponse response: NSURLResponse) {
        
        if (debug) {
            
            debugPrint("Received response.")
            
        }
        
        // Set the data
        self.data = NSMutableData()
    }
    
    // Get the data from the response
    func connection(connection: NSURLConnection, didReceiveData data: NSData) {
        
        // Add it to the data
        self.data.appendData(data)
        
    }
    
    // Finished recieving the data
    func connectionDidFinishLoading(connection: NSURLConnection) {
        
        // Finished with the data
        if (debug) {
            
            debugPrint(NSString(data: self.data, encoding: NSUTF8StringEncoding)!)
            
        }
        
        // Create the xml parser
        xmlParser = NSXMLParser(data: self.data)
        xmlParser?.delegate = self
        
        // Parse the data
        xmlParser?.parse()
    }
    
    // If an error occurred during the connection
    func connection(connection: NSURLConnection, didFailWithError error: NSError) {

        // Error occurred during connection
        if (debug) {
            
            debugPrint(error.description)
            
        }
        
        // Ensure the completion is not null
        if self.completion != nil {
            
            // Execute the completion
            self.completion!(response: ErrorResponse(error: error.localizedDescription))
            
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // MARK: NSXML Parser Delegate
    //
    /////////////////////////////////////
    
    private var record: Bool = false
    private var element: String = ""
    
    private var array: String = ""
    private var arrType: Any? = NSObject()
    private var obj: NSObject = NSObject()
    private var objMirror: Mirror = Mirror(reflecting: NSObject())
    
    // When an element has started
    func parser(parser: NSXMLParser, didStartElement elementName: String, namespaceURI: String?, qualifiedName qName: String?, attributes attributeDict: [String : String]) {
        
        record = false
        element = elementName
        
        // Check if the response contains the element
        if (contains(element)) {
            
            // Set record to true
            record = true
            
            // Get the element type
            let (n, t) = iterate(element)
            if (n != nil && t != nil) {
                
                // Check if it is of type array
                if (t is NSArray) {
                    
                    // Set the array element to keep track
                    array = element
                    arrType = t
                    
                    // Check the type of array at the time, swift never had a way to instantiate arrays of type T
                    if (arrType is [VasServiceInfo]) {
                        
                        obj = VasServiceInfo()
                        
                    } else if (arrType is [ServiceBalance]) {
                        
                        obj = ServiceBalance()
                        
                    } else if (arrType is [Number]) {
                        
                        obj = Number()
                        
                    } else if (arrType is [ServiceQuota]) {
                        
                        obj = ServiceQuota()
                        
                    } else if (arrType is [ContactInfo]) {
                        
                        obj = ContactInfo()
                        
                    }
                    
                    // Reflect the array
                    objMirror = Mirror(reflecting: obj)
                    
                }
                
            }
            
            return
        }
        
        // Check if the response body contains the field
        if (contains(objMirror, fieldName: element)) {
            
            record = true
            
        }
        
    }
    
    // The characters found in the element
    func parser(parser: NSXMLParser, foundCharacters string: String) {
        
        // Check if it must record the characters
        if (record && array.characters.count == 0) {
            
            // Set the value of the field
            returnType.setValue(type(element, value: string), forKey: element)
            
            return
            
        // Else add the characters to the array
        } else if (record && array.characters.count > 0) {
            
            obj.setValue(type(objMirror, fieldName: element, value: string), forKey: element)
            
            return
            
        }
        
        returnType.array.append("\(element):\(string)")
    }
    
    // When an element has ended
    func parser(parser: NSXMLParser, didEndElement elementName: String, namespaceURI: String?, qualifiedName qName: String?) {
        
        // Ensure that an array was filled
        if (array.characters.count > 0 && array == elementName) {
            
            // Get the array type and add an object to it
            if (arrType is [VasServiceInfo]) {
                
                var arr: [VasServiceInfo] = (returnType.valueForKey(array) as! [VasServiceInfo])
                arr.append(obj as! VasServiceInfo)
                returnType.setValue(arr, forKey: array)
                
            } else if (arrType is [ServiceBalance]) {
                
                var arr: [ServiceBalance] = (returnType.valueForKey(array) as! [ServiceBalance])
                arr.append(obj as! ServiceBalance)
                returnType.setValue(arr, forKey: array)
                
            } else if (arrType is [Number]) {
                
                var arr: [Number] = (returnType.valueForKey(array) as! [Number])
                arr.append(obj as! Number)
                returnType.setValue(arr, forKey: array)
                
            } else if (arrType is [ServiceQuota]) {
                
                var arr: [ServiceQuota] = (returnType.valueForKey(array) as! [ServiceQuota])
                arr.append(obj as! ServiceQuota)
                returnType.setValue(arr, forKey: array)
                
            } else if (arrType is [ContactInfo]) {
                
                var arr: [ContactInfo] = (returnType.valueForKey(array) as! [ContactInfo])
                arr.append(obj as! ContactInfo)
                returnType.setValue(arr, forKey: array)
                
            }
            
            array = ""
            
        }
        
    }
    
    // End of the xml
    func parserDidEndDocument(parser: NSXMLParser) {
        
        // Execute the completion command if not null
        if self.completion != nil {
            
            self.completion!(response: returnType)
            
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // MARK: Helper Methods
    //
    /////////////////////////////////////
    
    // Check if the object contains a field name
    private func contains(fieldName: String) -> Bool {
        
        if (!contains(header, fieldName: fieldName)) {
            
            return contains(mirror!, fieldName: fieldName)
            
        }
        
        return true
        
    }
    
    // Check if the object contains a field name
    private func contains(mirror: Mirror, fieldName: String) -> Bool {
        
        return iterate(mirror, fieldName: fieldName).0 != nil
        
    }
    
    // Find the field from the field name
    private func iterate(fieldName: String) -> (String?, Any?) {
        
        let (n, t) = iterate(header, fieldName: fieldName)
        
        if (n != nil && t != nil) {
            
            return (n, t)
        
        }
        
        return iterate(mirror!, fieldName: fieldName)
        
    }
    
    // Get the field name and type
    private func iterate(mirror: Mirror, fieldName: String) -> (String?, Any?) {
        
        let mirrorChildrenCollection = AnyRandomAccessCollection(mirror.children)!
        
        // Iterate through the children
        for (optionalPropertyName, value) in mirrorChildrenCollection {
         
            if (optionalPropertyName! == fieldName) {
                
                return (optionalPropertyName!, value)
                
            }
            
        }
        
        return (nil, nil)
    }
    
    // Gets the type of the field
    private func type(fieldName: String, value: String) -> AnyObject? {
        
        // First check the header
        if let v: AnyObject = type(header, fieldName: fieldName, value: value) {
            
            return v;
            
        }
        
        // Else check the body
        return type(mirror!, fieldName: fieldName, value: value)
    }
    
    // Gets the type of field
    private func type(mirror: Mirror, fieldName: String, value: String) -> AnyObject? {
        
        let (n, t) = iterate(mirror, fieldName: fieldName)
        
        if (n != nil && t != nil) {
            
            // Check if string
            if (t is String) {
                
                return value
                
            // Else check if is an Int
            } else if (t is Int) {
                
                return (value as NSString).integerValue
                
            // Else check if is a Float
            } else if (t is Float) {
                
                return (value as NSString).floatValue
                
            // Else check if is a NSDate
            } else if (t is NSDate) {
                
                // Format the date
                let dateFormatter = NSDateFormatter()
                dateFormatter.timeZone = NSTimeZone(abbreviation: "GMT")
                dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZZZ"
                return dateFormatter.dateFromString(value)
                
            }
            
        }
        
        return nil
    }
    
}
