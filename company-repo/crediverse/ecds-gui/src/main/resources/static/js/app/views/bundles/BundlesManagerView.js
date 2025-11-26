define( ['jquery', 'underscore', 'App', 'marionette', 
         'views/bundles/BundleDialogView', 'models/BundleModel',
         'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, 
    		BundleDialogView, PromotionModel, 
    		CommonUtils) {
		
		var i18ntxt = App.i18ntxt.bundles;
		
        var BundlesManagerView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageBundles#managebundles",
  		  	url: 'api/bundles',
  		  	error: null,
  		  	
  		  	breadcrumb: function() {
  		  		return {
  		  			heading: i18ntxt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.bundles,
  		  				href: window.location.hash,
						iclass: "fa fa-trophy"
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            },
            
            ui: {
                createBundle: '.createBundleButton',
                editBundle: '.editBundleButton',
                deleteBundle: '.deleteBundleButton',
                exportBundles: '.exportBundlesButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.createBundle": 'addBundle',
            	"click @ui.editBundle": 'editBundle',
            	"click @ui.deleteBundle": 'deleteBundle',
            	"click @ui.exportBundles": 'exportbundles'
            },
            
            loadData: function() {
            	var self = this;
            	
            	var table = this.$('.tableview');
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
            		"autoWidth": false,
					"responsive": true,
					"processing": true,
					language: {
		                url: "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
                      	
          				var jqxhr = $.ajax(self.url, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      		App.error(dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
                      "order": [[ 8, "asc" ]],
                      "columns": [
                   	 		{
                   		 	   	data: "id",
                   		 	   	title: App.i18ntxt.global.uniqueID,
								class: "all center",
								width: "80px",
								render: function(data, type, row, meta) {
									if (data == 0)
										return "-";
									else
										return data;
								}
                    		},
                    	   {
                    		   data: "name",
                    		   title: i18ntxt.name,
							   class: "all"
                    	   },
                    	   {
                    		   data: "description",
                    		   title: i18ntxt.description,
                    		   class: "right"
                    	   },
                    	   {
                    		   data: "type",
                    		   title: i18ntxt.type,
                    		   class: "right"
                    	   },
                    	   {
                    		   data: "tag",
                    		   title: i18ntxt.tag,
                    		   class: "right"
                    	   },
                    	   {
                    		   data: "ussdCode",
                    		   title: i18ntxt.ussdCode,
                    		   defaultContent: "-",
                    		   class: "right"
                    	   },
                    	   {
                    		   data: "price",
                    		   title: i18ntxt.price,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "tradeDiscountPercentage",
                    		   title: i18ntxt.tradeDiscountPercentage,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
								   if (!_.isUndefined(data))
									   return CommonUtils.formatNumber(data * 100);
                    		   }
                    	   },
                    	   {
                    		   data: "bundleState",
                    		   title: i18ntxt.state,
                    		   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.renderStatus(data.toLowerCase());
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
                   	            sortable: false,
								class: "nowrap right all",
								width: "85px",
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if ((data.bundleState == "NotConfigured" && App.hasPermission("Bundle", "Add")) 
                   	            			|| (data.bundleState != "NotConfigured" && App.hasPermission("Bundle", "Update"))) {
                   	            		buttons.push("<button class='btn btn-primary editBundleButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	
                   	            	if (App.hasPermission("Bundle", "Delete")) {
                       	            	if (data.bundleState != "NotConfigured") {
                       	            		buttons.push("<button class='btn btn-danger deleteBundleButton btn-xs'><i class='fa fa-times'></i></button>");
                       	            	} else {
                       	            		buttons.push("<button class='btn btn-default disabled btn-xs'><i class='fa fa-times'></i></button>");
                       	            	}
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            },
            
            onRender: function () {
            	var token = $("meta[name='_csrf']").attr("content");
        		var header = $("meta[name='_csrf_header']").attr("content");
            	var customeHeaders = {};
            	customeHeaders[header] = token;
            	
            	this.loadData();
            },
            
            addBundle: function(ev) {
            	App.log("Add Bundle Called");
            	var self = this;
            	
            	var model = new PromotionModel();
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg',
					title: i18ntxt.addDialogTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: BundleDialogView,
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
            
            extractUssdCodes: function() {
            	var data = this.dataTable.rows().data();
            	var codes = {};
            	for (var i=0; i<data.length; i++) {
            		if ( !_.isUndefined(data[i].ussdCode) ) {
            			codes[data[i].ussdCode] = data[i].name;
            		}
            	}
            	return codes;
            },
            
            editBundle: function(ev) {
            	var self = this;
            	var tableData = this.dataTable.row($(ev.currentTarget).closest('tr')).data();
            	var model = new PromotionModel(tableData);
            	model.set("existingUssdCodes", this.extractUssdCodes());
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg',
            		view: BundleDialogView,
            		title:CommonUtils.renderHtml( i18ntxt.editDialogTitle, {name: model.get("name")} ),
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
            
            deleteBundle: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.bundles.bundle,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.bundles.bundle,
	        			name: data.name,
	        			description: data.name
	        		},
	        		rowElement: row,
	        	},
	        	{
	        		success: function(model, response) {
	        			self
				        .dataTable.ajax.reload().draw();	        			
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            	return false;
            },
            
            exportPromotion: function(ev) {
            	
            },
            
        });
        return BundlesManagerView;
    });
