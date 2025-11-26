//
//  ServiceTableViewCell.swift
//  HxC
//
//  Created by Justin Guedes on 2015/06/04.
//  Copyright (c) 2015 Concurrent Systems. All rights reserved.
//

import UIKit

class ServiceTableViewCell: UITableViewCell {

    @IBOutlet weak var serviceName: UILabel!
    @IBOutlet weak var serviceDescription: UILabel!
    @IBOutlet weak var serviceImage: UIImageView!
    @IBOutlet weak var serviceVariant: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
}
