//
//  AvailableSevicesTableTableViewController.swift
//  HxC
//
//  Created by Justin Guedes on 2015/04/13.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class SevicesTableViewController: UITableViewController, UISearchBarDelegate {

    var active: Bool = false
    
    private var searchText: String?
    private var services: [VasServiceInfo] = []
    
    // Provides a unique list of Vas Service Infos
    private var unique: [VasServiceInfo] {
        
        get {
            
            var u: [VasServiceInfo] = []
            
            outer: for service in self.services {
                
                // Filter the list if need be
                if let filter = searchText {
                    
                    if service.serviceName.rangeOfString(filter, options: NSStringCompareOptions.CaseInsensitiveSearch, range: nil, locale: nil) == nil {
                        
                        continue
                        
                    }
                    
                }
                
                // Ensure the service is not repeated
                for u2 in u {
                    
                    if (service.serviceID == u2.serviceID) {
                        
                        if (u2.state == "notActive" && service.state == "active") {
                            
                            u2.state = service.state
                            u2.variantID = service.variantID
                            u2.variantName = service.variantName
                            
                        }
                        
                        continue outer
                        
                    }
                    
                }
                
                u.append(service)
                
            }
            
            // Sort according to state of service
            u.sortInPlace { (service1, service2) -> Bool in
                
                return service1.state < service2.state
                
            }
            
            return u
            
        }
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Apply theme to page
        ThemeEngine.applyTheme(self)
        
        // Enable the refresh control for the table view
        self.refreshControl?.addTarget(self, action: "refresh:", forControlEvents: UIControlEvents.ValueChanged)
        
    }
    
    override func viewWillAppear(animated: Bool) {
        
        // Refresh the page when view is displayed
        refresh(self)
        
    }
    
    // Load the services from C4U
    func refresh(sender: AnyObject) {
        
        // Do a get services call
        HxC.getServices(activeOnly: active, force: sender is UIRefreshControl, completion: {
            
            (response: GetServicesResponse) in
            
            // Stop refreshing the table view
            self.refreshControl?.endRefreshing()
            
            // Check the response code
            if (response.returnCode == "success") {
                
                // Set the services from the response
                self.services = response.serviceInfo
                
            } else {
                
                // Display the error message
                Alert.error(self, title: "Services", message: response.message)
                
            }
            
            // Reload the table view
            self.tableView.reloadData()
            
        })
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        
        // Return the number of sections.
        let numberOfSections = 1
        
//        if self.services.count > 0 {
//            
//            numberOfSections = 1
//            self.tableView.backgroundView = nil
//            
//        } else {
//            
//            let noServicesLabel = UILabel(frame: CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height))
//            noServicesLabel.text = "No Services" //Locale.toString(id: "no_services")
//            //noServicesLabel.textColor = UIColor.blackColor()
//            //noServicesLabel.textAlignment = NSTextAlignment.Center
//            self.tableView.backgroundView = noServicesLabel
//            self.tableView.separatorStyle = UITableViewCellSeparatorStyle.None
//            
//            return 0
//            
//        }
        
        return numberOfSections
        
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        // Return the number of rows in the section.
        return unique.count
        
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell: ServiceTableViewCell = tableView.dequeueReusableCellWithIdentifier("Service", forIndexPath: indexPath) as! ServiceTableViewCell

        let service = unique[indexPath.row]
        
        // Configure the cell...
        var image = UIImage(named: service.serviceID)
        if (image == nil) {
            image = UIImage(named: "Unknown")
        }
        
        // Load the image of the service
        cell.serviceImage.image = image
        
        // Load the name of the service
        cell.serviceName.text = service.serviceName
        
        // Load the description of the service
        if let plugin = PluginEngine.instance.getPlugin(service.serviceID) {
            
            cell.serviceDescription.text = plugin.description;
            
        } else {
            
            cell.serviceDescription.text = "No available description at this time."
            
        }
        
        cell.serviceVariant?.text = service.state == "active" ? service.variantName : ""
        cell.accessoryType = service.state == "active" ? UITableViewCellAccessoryType.Checkmark : UITableViewCellAccessoryType.DisclosureIndicator
        
        ThemeEngine.applyTheme(cell)

        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        // Get the current service
        let service = unique[indexPath.row]
        
        
        if (!PluginEngine.instance.present(viewController: self, serviceID: service.serviceID, params: {
            
            (pluginViewController: UIPluginViewController) in
            
            // Set the view controllers serviceInfo variable
            pluginViewController.objects["serviceInfo"] = service
            
        })) {
            
            // If it failed to load the service
            
        }
        
    }
    
    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        
        // Filter the table view
        if !searchText.isEmpty {
            
            self.searchText = searchText
            
        } else {
            
            self.searchText = nil
            
        }
        
        // Reload the table view data
        self.tableView.reloadData()
        
    }
    
    func searchBarCancelButtonClicked(searchBar: UISearchBar) {
        
        searchBar.text = ""
        searchBar.resignFirstResponder()
        
        self.searchText = nil
        
        self.tableView.reloadData()
        
    }

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return NO if you do not want the item to be re-orderable.
        return true
    }
    */
    
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        // Prepare the destination controller
        _ = segue.destinationViewController
        let indexPath = self.tableView.indexPathForSelectedRow
        _ = unique[indexPath!.row]
        
        // Set the serviceInfo variable for the destination controller
        // destinationViewController.setValue(service, forKey: "serviceInfo")
        
    }

}
