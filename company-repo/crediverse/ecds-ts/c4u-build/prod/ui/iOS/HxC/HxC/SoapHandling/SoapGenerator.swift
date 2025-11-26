//
//  SoapRequest.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/07.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class SoapGenerator: NSObject {
    
    var url: NSURL
    var namespace: String = "http://hxc.concurrent.com/"
    
    // Soap Request Information
    let envelopeIdentifier = "env"
    let hxcIdentifier = "hxc"
    
    init(url: NSURL) {
        self.url = url
    }
    
    init(url: NSURL, namespace: String) {
        self.url = url
        self.namespace = namespace
    }
    
    // Creates the SOAP Generator object
    class func create() -> SoapGenerator {
        let url = NSURL(string: User.server)
        return SoapGenerator(url: url!)
    }
    
    // Adds a header to the url request
    private func addHeader(request: NSMutableURLRequest, length: Int) -> NSMutableURLRequest {
        
        // Basic header elements
        request.HTTPMethod = "POST"
        request.addValue("text/xml; charset=UTF-8", forHTTPHeaderField: "Content-Type")
        request.addValue("\"\"", forHTTPHeaderField: "SOAPAction")
        request.addValue(NSString(format: "%d", length) as String, forHTTPHeaderField: "Content-Length")
        
        // Credential data
        let username = User.username
        let password = User.password
        
        // Add authorization to the header
        let authorization = "\(username):\(password)"
        let data: NSData = authorization.dataUsingEncoding(NSUTF8StringEncoding)!
        let encoded: String = data.base64EncodedStringWithOptions(NSDataBase64EncodingOptions())
        request.addValue("Basic \(encoded)", forHTTPHeaderField: "Authorization")
        
        return request
    }
    
    // Creates the soap request body
    private func createSoapBody(request: AnyObject) -> String {
        
        // Create the body
        var body: String = "<\(envelopeIdentifier):Envelope xmlns:\(envelopeIdentifier)=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:\(hxcIdentifier)=\"\(namespace)\">"
        body += "<\(envelopeIdentifier):Header/>"
        body += "<\(envelopeIdentifier):Body>"
        
        // Reflect the request object and get the name of the request
        let mirror = Mirror(reflecting: request)
        let name = extractRequestName(mirror)
        
        // Append to the body
        body += "<\(hxcIdentifier):\(name)>"
        body += "<request>"
        
        // Reflect the request to get the fields
        body += createXml(mirror)
        
        // Append to the body the closing tags
        body += "</request>"
        body += "</\(hxcIdentifier):\(name)>"
        body += "</\(envelopeIdentifier):Body>"
        body += "</\(envelopeIdentifier):Envelope>"
        
        return body
        
    }
    
    private func createXml(mirror: Mirror) -> String {

        var body: String = ""
        
        // Check the super class
        if let superMirror = mirror.superclassMirror() {
            
            // Add to the body
            body += createXml(superMirror)
            
        }
        
        // Iterate through the children
        for (optionalPropertyName, value) in mirror.children {
            
            if String(value) == "nil" || String(value).isEmpty {
                continue;
            }
            
            // Get the type of field
            let type = Mirror(reflecting: value)
            
            // Add the name of the property
            body += "<\(optionalPropertyName!)>"
            
            // Check the type
            if type.subjectType is String.Type || type.subjectType is Int.Type ||
                type.subjectType is Float.Type || type.subjectType is Double.Type ||
                type.subjectType is Bool.Type {
            
                body += String(value)
                
            } else if type.subjectType is String?.Type || type.subjectType is Int?.Type ||
                type.subjectType is Float?.Type || type.subjectType is Double?.Type ||
                type.subjectType is Bool?.Type {
                    
                var stringValue = String(value)
                let startIndex = stringValue.startIndex.advancedBy(9)
                stringValue = stringValue.substringFromIndex(startIndex)
                stringValue = stringValue.substringToIndex(stringValue.endIndex.advancedBy(-1))
                    
                body += stringValue
                
            } else {
                
                body += createXml(type)
                
            }
            
            // End the element
            body += "</\(optionalPropertyName!)>"
            
        }
        
        return body
    }
    
    // Extract the name
    private func extractRequestName(mirror: Mirror) -> String {
        
        // String manipulation to get the name of the request
        let requestName = String(mirror.subjectType).stringByReplacingOccurrencesOfString("request", withString: "", options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil)
        let name = requestName.substringToIndex(requestName.startIndex.advancedBy(1))
        
        return name.lowercaseString + requestName.substringFromIndex(requestName.startIndex.advancedBy(1))
        
    }
    
    // Gets url request from the request object
    func get(requestObject: RequestHeader) -> NSMutableURLRequest {
        
        // Create the soap body
        let soapBody = createSoapBody(requestObject)
        
        // Add the request header
        var request = NSMutableURLRequest(URL: url)
        request = addHeader(request, length: soapBody.characters.count)
        request.HTTPBody = soapBody.dataUsingEncoding(NSUTF8StringEncoding)
        
        return request
        
    }
}
