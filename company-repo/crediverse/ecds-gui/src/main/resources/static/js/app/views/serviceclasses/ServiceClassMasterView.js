define( ['jquery', 'App', 'marionette', 'backbone', 'handlebars', 'utils/HandlebarHelpers', 
         'models/ServiceClassValidationModel', 'views/serviceclasses/ServiceClassDialogView', 'utils/CommonUtils', 'datatables'],
    function($, App, Marionette, Backbone, Handlebars, HBHelper, 
    		ServiceClassValidationModel, ServiceClassDialogView, CommonUtils) {
        //ItemView provides some default rendering logic
        var ServiceClassMasterView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: 'row',
        	},
        	template: 'ManageServiceClasses#serviceClassMaster',
  		  	url: 'api/serviceclass',
  		  	dataTable: null,
  		  	
  		  	i18ntxt: App.i18ntxt.serviceclass,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.serviceClasses,
  		  				href: window.location.hash,
						iclass: "fa fa-flag"
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function () {
            	try {
            		HBHelper.registerSelect();
            	} catch(err) {
            		App.error(err);
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
					"language": {
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
                      		App.error(dataResponse);
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
                        	title: this.i18ntxt.name,
							class: "all",
                    	   	render: function(data, type, row, meta) {
                    			return _.isUndefined(row)?data:'<a target="_blank" href="#serviceClass/'+row.id+'" class="routerlink">'+data+'</a>';
                    		}
                        },
                        {
                        	data: "description",
                        	title: this.i18ntxt.description
                        },
                        {
                        	data: "maxTransactionAmount",
                        	title: this.i18ntxt.maxTransactionAmount,
							defaultContent: "-",
							class: "right",
							render: function(data, type, row, meta) {
                 			   return CommonUtils.formatNumber(data);
                 		   }
                        },
                        {
                        	data: "maxDailyCount",
                        	title: this.i18ntxt.maxDailyCount,
							defaultContent: "-",
							class: "right",
							render: function(data, type, row, meta) {
                 			   return CommonUtils.formatNumber(data);
                 		   }
                        },
                        {
                        	data: "maxDailyAmount",
                        	title: this.i18ntxt.maxDailyAmount,
							defaultContent: "-",
							class: "right",
							render: function(data, type, row, meta) {
                 			   return CommonUtils.formatNumber(data);
                 		   }
                        },
                        {
                        	data: "maxMonthlyCount",
                        	title: this.i18ntxt.maxMonthlyCount,
							defaultContent: "-",
							class: "right",
							render: function(data, type, row, meta) {
                 			   return CommonUtils.formatNumber(data);
                 		   }
                        },
                        {
                        	data: "maxMonthlyAmount",
                        	title: this.i18ntxt.maxMonthlyAmount,
							defaultContent: "-",
							class: "right",
							render: function(data, type, row, meta) {
                 			   return CommonUtils.formatNumber(data);
                 		   }
                        },
                        {
               	            targets: -1,
               	            data: null,
               	            title: "",
               	            sortable: false,
							class: "nowrap right all",
							width: "80px",
                        	render: function(data, type, row, meta) {
                        		var buttons = [];
                        		//buttons.push("<button class='btn btn-primary viewScButton btn-xs'>View</button>");
                        		if (App.hasPermission("ServiceClass", "Update")) {
                        			buttons.push("<button class='btn btn-primary editScButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                        		}
                        		if (App.hasPermission("ServiceClass", "Delete")) {
                        			buttons.push("<button class='btn btn-danger deleteScButton btn-xs'><i class='fa fa-times'></i></button>");
                        		}
           	            		return buttons.join('');
                        	}
                        }
                        
                    ]
            	} );	// End dataTable
            },
            
            // UI Aliases
            ui: {
            	serviceClasses: '',
            	viewSc: 	'.viewScButton',
            	createSc: 	'.createScButton',
            	editSc: 	'.editScButton',
            	deleteSc: 	'.deleteScButton',
            	exportServiceClasses: '.exportScButton'
            },
            
            // DOM Events
            events: {
            	"click @ui.viewSc": 	'viewSc',
            	"click @ui.createSc": 	'createSc',
            	"click @ui.editSc":		'editSc',
            	"click @ui.deleteSc":	'deleteSc',
            	"click @ui.exportServiceClasses": 'exportServiceClasses'
            },
            
            exportServiceClasses: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            
            // This is going to change to a 
            viewSc: function(ev) {
            	var self = this;
            	var dialogModel = null;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var scData = _.extend({}, currentRow.data());
            	
            	App.vent.trigger('application:dialog', {
            		name: 'serviceClassModal',
            		title: "Edit Service Class " + scData.name,
            		init: function() {
            			var form = $('#serviceClassModal form');
            			$("#serviceClassModal .scCreateButton").addClass("hide");
//            			form.find('input').prop("disabled", true);
            		},
            		model: new Backbone.Model(scData),	// For Auto-Form text insert
            		events: {
            			'init': function(event) {
            				
            			}
            		}
            	});
            	return false;
            },
            
            createSc: function(ev) {
            	var self = this;
            	var dialogModel = new ServiceClassValidationModel();
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: ServiceClassDialogView,
            		title:'Add Service Class', //self.i18ntxt.addNewServiceClass,
            		hide: function() {
	        			self
	        			.dataTable.ajax.reload().draw();
	        		},
	        		init: function(event) {
        				$("#serviceClassModal .scCreateButton").removeClass("hide");
        				$("#serviceClassModal .scCreateButton").text("Create");
        			},
            		params: {
            			model: dialogModel
            		}
            	});

            	return false;
            },
            
            editSc: function(ev) {
            	var self = this;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var scData = _.extend({}, currentRow.data());
            	var dialogModel = new ServiceClassValidationModel(scData);
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: ServiceClassDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.serviceclass.editServiceClass, {name: scData.name}),
            		hide: function() {
	        			self
	        			.dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: dialogModel
            		}
            	});
            	
            	return false;
            },
                        
            deleteSc: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.serviceclass.serviceClass,
	        		url: 'api/serviceclass/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.serviceclass.serviceClass,
	        			name: data.name,
	        			description: data.description
	        		},
	        		rowElement: row,
	        	},
	        	{
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            }
            
        });
        
        return ServiceClassMasterView;
    });            
