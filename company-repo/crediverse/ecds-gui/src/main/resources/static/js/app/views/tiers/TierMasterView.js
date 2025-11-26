define( ['jquery', 'App', 'marionette', 'handlebars', 'utils/HandlebarHelpers', 'models/TierModel', 'utils/CommonUtils', 'datatables'],
    function($, App, Marionette, Handlebars, HBHelper, TierModel, CommonUtils) {
        //ItemView provides some default rendering logic
        var TierMasterView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: 'row',
        	},
        	template: "ManageTiers#tiermaster",
  		  	url: 'api/tiers',
  		  	dataTable: null,
  		  	i18ntxt: App.i18ntxt.tiers,
  		  	
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.tierManagement,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.tiers,
  		  				href: window.location.hash,
						iclass: "fa fa-sitemap"
  		  			}]
  		  		}
  		  	},
  		  	
            // Fetch Data on page retrieval
            onRender: function () {
            	var self = this;
            	var table = this.$('.tableview');
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
            		autoWidth: false,
					responsive: true,
					processing: true,
					language: {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
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
                      	})
                      	.always(function(data) {
                      	});
                      },
                      "columns": [
                    	   {
                    		   data: "id",
                    		   title: App.i18ntxt.global.uniqueID,
							   class: "all center",
							   width: "80px",
                    	   },
                    	   {
                    		   data: "name",
                    		   title: App.i18ntxt.global.name,
							   class: "all",
                    	   },
                    	   {
                    		   data: "description",
                    		   title: App.i18ntxt.global.description
                    	   },
						   {
								data: "buyerDefaultTradeBonusPercentage",
								title: App.i18ntxt.tiers.buyerDefaultTradeBonusPercentage
					       },
                    	   {
                    		   data: "type",
                    		   title: "Type",
                    		   // TODO: Do on server please
                    		   render: function(data) {
                    			   if (data == ".")
                    				   return App.i18ntxt.enums.tierType.root;
                    			   else if (data == "T")
                    				   return App.i18ntxt.enums.tierType.store;
                    			   else if (data == "W")
                    				   return App.i18ntxt.enums.tierType.wholesaler;
                    			   else if (data == "R")
                    				   return App.i18ntxt.enums.tierType.retailer;
                    			   else if (data == "S")
                    				   return App.i18ntxt.enums.tierType.subscriber;
                    		   }
                    	   },
                    	   {
                    		   data: "allowIntraTierTransfer",
                    		   title: self.i18ntxt.allowIntraTierTransfer,
                    		   width: "200px",
                    		   render: function(data) {
                    			   if (data == true)
                    				   return App.i18ntxt.enums.yesNo.yes;
                    			   else 
                    				   return App.i18ntxt.enums.yesNo.no;
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
                   	            sortable: false,
								class: "nowrap right all",
								width: "120px",
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	buttons.push("<button class='btn btn-primary viewTierButton btn-xs'>"+App.i18ntxt.global.viewBtn+"</button>");
                   	            	if (App.hasPermission("Tier", "Update")) {
                   	            		buttons.push("<button class='btn btn-primary editTierButton btn-xs' "+(row.permanent?"style='visibility:hidden;'":"")+">"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	if (App.hasPermission("Tier", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deleteTierButton btn-xs' "+(row.permanent?"style='visibility:hidden;'":"")+"><i class='fa fa-times'></i></button>");
                   	            	}
               	            		
            	            		return buttons.join('');
            	            	}
                    	   }
                	  ]
                  } );
            },
            
            // UI Aliases
            ui: {
            	tiers: '',
            	viewTier: '.viewTierButton',
            	createTier: '.createTierButton',
            	editTier: 	'.editTierButton',
            	deleteTier: '.deleteTierButton',
            	exportTiers: '.exportTiersButton'
            },
            
            // DOM Events
            events: {
            	"click @ui.viewTier": 	'viewTier',
            	"click @ui.createTier": 'createTier',
            	"click @ui.editTier": 'editTier',
            	"click @ui.deleteTier": 'deleteTier',
            	"click @ui.exportTiers": 'exportTiers'
            },
            
            exportTiers: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            viewTier: function(ev) {
            	var clickedRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var data = clickedRow.data();
            	App.vent.trigger('application:route', 'tierrules/' + data.id);
            },
            
            deleteTier: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	if ((!_.isUndefined(data.permanent)) && (!data.permanent)) {
		        	CommonUtils.delete({
		        		url: self.url+'/'+data.id,
		        		itemType: App.i18ntxt.tiers.tier,
		        		context: {
		        			what: App.i18ntxt.tiers.tier,
		        			name: data.name,
		        			description: data.description
		        		},
		        		data: data,
		        		rowElement: row,
		        	}, {
		        		success: function(model, response) {
			            	row.fadeOut("slow", function() {
			            		clickedRow.remove().draw();
			            	});
		        		},
		        		error: function(model, response) {
		        			App.error(response);
		        		}
		        	});
            	}
            },

            createTier: function(ev) {
            	var self = this;
            	var dialogModel = null;
            	
            	App.vent.trigger('application:dialog', {
            		name: 'tierModal',
            		title: self.i18ntxt.addTierTitle,
            		init: function() {
            			var form = $('#tierModal form');
            			dialogModel = new TierModel();
            			dialogModel.bind(form);
						$("#buyerDefaultTradeBonusPercentage").bind('change keyup input paste',function(evt){
							evt.target.value = evt.target.value.replace(/[^0-9\.]/g, '').replace(/(\..*)\./g, '$1');
						});
            		},
            		events: {
            			'init': function(event) {
            				$("#tierModal .tierCreateButton").text("Create");
            			},
            			'click .tierCreateButton': function(event) {
            				var dialog = this;
        					dialogModel.save({
        						success: function(mdl, resp) {
        							dialog.modal('hide');
        							self.render();
        						}
        					});
            			}
            		}
            	});
            	return false;
            },
            
            editTier: function(ev) {
            	var self = this;
            	var dialogModel = null;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var tierData = _.extend({}, currentRow.data());
            	
            	App.vent.trigger('application:dialog', {
            		name: 'tierModal',
            		title: CommonUtils.renderHtml(self.i18ntxt.editTierTitle, {name: tierData.name}),
            		init: function() {
            			var form = $('#tierModal form');
            			dialogModel = new TierModel({id: tierData.id});
            			dialogModel.bind(form);
						$("#buyerDefaultTradeBonusPercentage").bind('change keyup input paste',function(evt){
							evt.target.value = evt.target.value.replace(/[^0-9\.]/g, '').replace(/(\..*)\./g, '$1');
						});
            		},
            		model: new TierModel(tierData),
            		events: {
            			'click .tierCreateButton': function(event) {
            				var dialog = this;
        					dialogModel.save({
        						success: function(mdl, resp) {
        							dialog.modal('hide');
        							self.render();
        						},
        						error: function(mdl, error) {
            				    	if (console) console.error("Save problem: " + error);
        						},
        						preprocess: function(data) {
        	            			//data.maxDailyCount = self.$('#maxDailyCount').autoNumeric('get');
        	                    	//data.minimumAmount = $('#minimumAmount').autoNumeric('get');
        						}
        					});
            			}
            		}
            	});
            	return false;
            }
            
    });
        
    return TierMasterView;
});
