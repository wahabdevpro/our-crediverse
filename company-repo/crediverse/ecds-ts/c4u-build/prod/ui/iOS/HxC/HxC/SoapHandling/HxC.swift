//
//  HxC.swift
//  Credit4U
//
//  Created by Justin Guedes on 2014/11/09.
//  Copyright (c) 2014 Justin Guedes. All rights reserved.
//

import UIKit

class HxC {
    
    static var error: ((name: String, message: String) -> Void)?
    
    private static var transactionID = 0;
    private static var sessionID = NSDate().format;
    private static var cache: [String: (NSDate, RequestHeader, ResponseHeader)] = [:]
    
    private static let TEST_MODE = "testOnly"
    private static let MAX_ALLOWED_INTERVAL: NSTimeInterval = 5 * 60
    
    private static var soap: SoapGenerator = SoapGenerator.create()
    private static var connection: SoapConnection {
        
        get {
            return SoapConnection()
        }
        
    }
    
    // Populates a soap request header
    class func populate(request: RequestHeader) {
        
        request.callerID = User.number
        request.channel = "SMART_APP"
        
        // Get the device ID
        request.hostName = UIDevice.currentDevice().identifierForVendor!.UUIDString
        
        request.transactionID = NSDate().format + (NSString(format: "%05d", transactionID++) as String)
        request.sessionID = self.sessionID
        request.version = "1"
        request.mode = "normal"
        request.languageID = Locale.languageID
        
    }
    
    // Sends a soap request
    class func send(request: RequestHeader, returnType: ResponseHeader) -> ResponseHeader {
        
        // Gets the xml from the request
        let data = soap.get(request)
        
        // Sends the soap request
        return connection.sendRequest(data, returnType: returnType)
        
    }
    
    // Sends a soap request asynchronously
    class func send(request: RequestHeader, returnType: ResponseHeader, completion: (response: ResponseHeader) -> Void) {
        
        // Gets the xml from the request
        let data = soap.get(request)
        
        // Sends the soap request
        connection.sendRequest(data, returnType: returnType, completion: completion)
        
    }
    
    // Resets the cache
    class func refresh() {
        
        cache = [:]
        
    }
    
    // Sends an addMember request
    class func addMember(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), completion: (response: AddMemberResponse) -> Void) {
        
        // Create soap request
        let request = AddMemberRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: AddMemberResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "AddMemberRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! AddMemberResponse)
            
        })
        
    }
    
    // Sends an addMember test request
    class func addMemberTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), force: Bool = false, completion: (response: AddMemberResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["addMemberTest"] {
                
                // Get the request
                let request = request as! AddMemberRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.variantID == variantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits &&
                    request.memberNumber.addressDigits == memberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! AddMemberResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = AddMemberRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: AddMemberResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "AddMemberTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["addMemberTest"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! AddMemberResponse)
            
        })
        
    }
    
    // Sends an addQuota request
    class func addQuota(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), quota: ServiceQuota = ServiceQuota(), completion: (response: AddQuotaResponse) -> Void) {
        
        // Create soap request
        let request = AddQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.quota = quota
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: AddQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "AddQuotaRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! AddQuotaResponse)
            
        })
        
    }
    
    // Sends an addQuota test request
    class func addQuotaTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), quota: ServiceQuota = ServiceQuota(), completion: (response: AddQuotaResponse) -> Void) {
        
        // Create soap request
        let request = AddQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.quota = quota
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: AddQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "AddQuotaTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! AddQuotaResponse)
            
        })
        
    }
    
    // Sends an changeQuota request
    class func changeQuota(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), oldQuota: ServiceQuota = ServiceQuota(), newQuota: ServiceQuota = ServiceQuota(), completion: (response: ChangeQuotaResponse) -> Void) {
        
        // Create soap request
        let request = ChangeQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.oldQuota = oldQuota
        request.newQuota = newQuota
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: ChangeQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "ChangeQuotaRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! ChangeQuotaResponse)
            
        })
        
    }
    
    // Sends an changeQuota test request
    class func changeQuotaTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), oldQuota: ServiceQuota = ServiceQuota(), newQuota: ServiceQuota = ServiceQuota(), completion: (response: ChangeQuotaResponse) -> Void) {
        
        // Create soap request
        let request = ChangeQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.oldQuota = oldQuota
        request.newQuota = newQuota
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: ChangeQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "ChangeQuotaTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! ChangeQuotaResponse)
            
        })
        
    }
    
    // Sends an getBalances request
    class func getBalances(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", force: Bool = false, completion: (response: GetBalancesResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getBalances"] {
                
                // Get the request
                let request = request as! GetBalancesRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.variantID == variantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetBalancesResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetBalancesRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetBalancesResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetBalancesRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getBalances"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetBalancesResponse)
            
        })
        
    }
    
    // Sends an getLocaleSettings request
    class func getLocaleSettings(force: Bool = false, completion: (response: GetLocaleSettingsResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getLocaleSettings"] {
                
                // Get the request
               _ = request as! GetLocaleSettingsRequest
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) {
                    
                    // Execute the completion command
                    completion(response: response as! GetLocaleSettingsResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetLocaleSettingsRequest()
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetLocaleSettingsResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetLocaleSettingsRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getLocaleSettings"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetLocaleSettingsResponse)
            
        })
        
    }
    
    // Sends an getMembers request
    class func getMembers(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", force: Bool = false, completion: (response: GetMembersResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getMembers"] {
                
                // Get the request
                let request = request as! GetMembersRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.variantID == variantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetMembersResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetMembersRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetMembersResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetMembersRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getMembers"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetMembersResponse)
            
        })
        
    }
    
    // Sends an getOwners request
    class func getOwners(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", force: Bool = false, completion: (response: GetOwnersResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getOwners"] {
                
                // Get the request
                let request = request as! GetOwnersRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetOwnersResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetOwnersRequest()
        
        // Fill the request object
        request.memberNumber = subscriberNumber
        request.serviceID = serviceID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetOwnersResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetOwnersRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["GetOwners"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetOwnersResponse)
            
        })
        
    }
    
    // Sends an getQuotas request
    class func getQuotas(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), activeOnly: Bool = false, force: Bool = false, completion: (response: GetQuotasResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getQuotas"] {
                
                // Get the request
                let request = request as! GetQuotasRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.variantID == variantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits &&
                    request.memberNumber.addressDigits == memberNumber.addressDigits &&
                    request.activeOnly == activeOnly
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetQuotasResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetQuotasRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.activeOnly = activeOnly
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetQuotasResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetQuotasRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getQuotas"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetQuotasResponse)
            
        })
        
    }
    
    // Sends an getService request
    class func getService(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", force: Bool = false, completion: (response: GetServiceResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getService"] {
                
                // Get the request
                let request = request as! GetServiceRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetServiceResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetServiceRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetServiceResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetServiceRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getService"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetServiceResponse)
            
        })
        
    }
    
    // Sends an getServices request
    class func getServices(subscriberNumber: Number = Number(addressDigits: User.number), activeOnly: Bool = false, force: Bool = false, completion: (response: GetServicesResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["getServices"] {
                
                // Get the request
                let request = request as! GetServicesRequest
                
                // Check the request parameters match the cached parameters
                let same = request.activeOnly == activeOnly &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! GetServicesResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = GetServicesRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.activeOnly = activeOnly
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: GetServicesResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "GetServicesRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["getServices"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! GetServicesResponse)
            
        })
        
    }
    
    // Sends an migrate request
    class func migrate(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", newServiceID: String = "", variantID: String = "", newVariantID: String = "", completion: (response: MigrateResponse) -> Void) {
        
        // Create soap request
        let request = MigrateRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.newServiceID = newServiceID
        request.variantID = variantID
        request.newVariantID = newVariantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: MigrateResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "MigrateRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! MigrateResponse)
            
        })
        
    }
    
    // Sends an migrate test request
    class func migrateTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", newServiceID: String = "", variantID: String = "", newVariantID: String = "", force: Bool = false, completion: (response: MigrateResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["migrateTest"] {
                
                // Get the request
                let request = request as! MigrateRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.newServiceID == newServiceID &&
                    request.variantID == variantID &&
                    request.newVariantID == newVariantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! MigrateResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = MigrateRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.newServiceID = newServiceID
        request.variantID = variantID
        request.newVariantID = newVariantID
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: MigrateResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "MigrateRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["migrateTest"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! MigrateResponse)
            
        })
        
    }
    
    // Sends an removeMember request
    class func removeMember(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), completion: (response: RemoveMemberResponse) -> Void) {
        
        // Create soap request
        let request = RemoveMemberRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: RemoveMemberResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "RemoveMemberRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! RemoveMemberResponse)
            
        })
        
    }
    
    // Sends an removeMember test request
    class func removeMemberTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), completion: (response: RemoveMemberResponse) -> Void) {
        
        // Create soap request
        let request = RemoveMemberRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: RemoveMemberResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "RemoveMemberTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! RemoveMemberResponse)
            
        })
        
    }
    
    // Sends an removeQuota request
    class func removeQuota(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), quota: ServiceQuota = ServiceQuota(), completion: (response: RemoveQuotaResponse) -> Void) {
        
        // Create soap request
        let request = RemoveQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.quota = quota
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: RemoveQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "RemoveQuotaRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! RemoveQuotaResponse)
            
        })
        
    }
    
    // Sends an removeQuota test request
    class func removeQuotaTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", memberNumber: Number = Number(), quota: ServiceQuota = ServiceQuota(), completion: (response: RemoveQuotaResponse) -> Void) {
        
        // Create soap request
        let request = RemoveQuotaRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.memberNumber = memberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.quota = quota
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: RemoveQuotaResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "RemoveQuotaTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! RemoveQuotaResponse)
            
        })
        
    }
    
    // Sends an subscribe request
    class func subscribe(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", completion: (response: SubscribeResponse) -> Void) {
        
        // Create soap request
        let request = SubscribeRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: SubscribeResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "SubscribeRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! SubscribeResponse)
            
        })
        
    }
    
    // Sends an subscribe test request
    class func subscribeTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", force: Bool = false, completion: (response: SubscribeResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["subscribeTest"] {
                
                // Get the request
                let request = request as! SubscribeRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                            request.variantID == variantID &&
                            request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! SubscribeResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = SubscribeRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: SubscribeResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "SubscribeTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["subscribeTest"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! SubscribeResponse)
            
        })
        
    }
    
    // Sends an unsubscribe request
    class func unsubscribe(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", completion: (response: UnsubscribeResponse) -> Void) {
        
        // Create soap request
        let request = UnsubscribeRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: UnsubscribeResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "UnsubscribeRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! UnsubscribeResponse)
            
        })
        
    }
    
    // Sends an unsubscribe test request
    class func unsubscribeTest(subscriberNumber: Number = Number(addressDigits: User.number), serviceID: String = "", variantID: String = "", force: Bool = false, completion: (response: UnsubscribeResponse) -> Void) {
        
        // If must force the request or use the cache
        if !force {
            
            // Get the cache
            if let (date, request, response) = cache["unsubscribeTest"] {
                
                // Get the request
                let request = request as! UnsubscribeRequest
                
                // Check the request parameters match the cached parameters
                let same = request.serviceID == serviceID &&
                    request.variantID == variantID &&
                    request.subscriberNumber.addressDigits == subscriberNumber.addressDigits
                
                // Check the cache has not expired and that the request parameters are the same
                if !date.isExpired(MAX_ALLOWED_INTERVAL) && same {
                    
                    // Execute the completion command
                    completion(response: response as! UnsubscribeResponse)
                    return
                    
                }
                
            }
            
        }
        
        // Create soap request
        let request = UnsubscribeRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.variantID = variantID
        request.mode = TEST_MODE
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: UnsubscribeResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "UnsubscribeTestRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Add response to cache
            self.cache["unsubscribeTest"] = (NSDate(), request, response)
            
            // Execute the completion command
            completion(response: response as! UnsubscribeResponse)
            
        })
        
    }
    
    class func updateContactInfo(subscriberNumber: Number, serviceID: String = "", contactInfo: ContactInfo, completion: (response: UpdateContactInfoResponse) -> Void) {
        
        // Create soap request
        let request = UpdateContactInfoRequest()
        
        // Fill the request object
        request.subscriberNumber = subscriberNumber
        request.serviceID = serviceID
        request.contactInfo = contactInfo
        
        // Get the xml and send the request
        let data = soap.get(request)
        connection.sendRequest(data, returnType: UpdateContactInfoResponse(), completion: {
            
            (response: ResponseHeader) in
            
            // Check response is not ErrorResponse
            if response is ErrorResponse {
                
                // Execute the error function if it is set
                if self.error != nil {
                    
                    self.error!(name: "UpdateContactInfoRequest", message: response.message)
                    
                }
                
                return
                
            }
            
            // Execute the completion command
            completion(response: response as! UpdateContactInfoResponse)
            
        })
        
    }
    
}
