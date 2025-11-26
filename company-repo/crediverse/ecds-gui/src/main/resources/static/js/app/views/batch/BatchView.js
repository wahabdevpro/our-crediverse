define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/BatchModel', 'utils/CommonUtils', 'datatables'],
    function($, App, BackBone, Marionette,Handlebars, 
		HBHelper, BatchModel, CommonUtils) {
        //ItemView provides some default rendering logic
        var BatchView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "BatchView#batchdetails",
  		  	url: 'api/batch/',
  		  	error: null,
			tierList: null,
			id: null,
			i18ntxt: App.i18ntxt.batch,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.batchViewHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.heading,
  		  				href: "#batchHistory",
						iclass: "glyphicon glyphicon-import"
  		  			}, {
  		  				text: txt.batchViewHeading,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function () {
            },
            
            onRender: function () {
            	if (_.isUndefined(this.model)) {
            		this.retrieveBatchData();
            	} else {
                	var self = this;
            	}

				if (!_.isUndefined(this.model.attributes.timestamp))
	            	this.$('#batchTimestamp').html(CommonUtils.formatTimeStamp(this.model.attributes.timestamp));	
            },
            
			retrieveBatchData: function() {
            	var self = this;
            	this.model = new BatchModel({id: self.id});
            	this.model.fetch({
            		success: function(ev){
						self.render();
					},
				});
			},	
			
            ui: {
                downloadBatchButton: '.downloadBatchButton',
            },

            // View Event Handlers
            events: {
            	"click @ui.downloadBatchButton": 'downloadBatchButton',
            },

			downloadBatchButton: function(e){
            	e.preventDefault();
            	
            	window.location.href = this.url + "download/csv/" + this.id;
			}
        });
        return BatchView;
    });
