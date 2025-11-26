define( ['jquery', 'underscore', 'App', 'marionette', 'views/cellgroups/CellGroupDialogView', 'models/CellGroupModel', 'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, CellGroupDialogView, CellGroupModel, HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
        var ManageCellGroupsView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageCellGroups#managecellgroups",
  		  	url: 'api/cellgroups',
  		  	error: null,
  		  	
  		  	i18ntxt: App.i18ntxt.cellgroups,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.cellgroups;
  		  		return {
  		  			heading: txt.cellGroupManagement,
  		  			defaultHome: false,
  		  			breadcrumb: [{
		  				text: txt.sectionBC,
						iclass: "fa fa-key"
		  			}, {
		  				text: App.i18ntxt.navbar.cellGroups,
		  				href: window.location.hash
		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            	var model = new CellGroupModel({url: 'api/cellgroups'});
            	try {
					HBHelper.registerSelect();
				} catch(err) {
					App.log(err);
				}
            },
            
            logEvents: function(ctrl, list) {
            	var self = this;
            	_.each(list, function(item, index) {
            		ctrl.on(item, $.proxy(function(event, data) {
            			App.log('Fired event '+item+' '+JSON.stringify(data, null, 2));
            		}, self));
            	})
            },
            
            onRender: function () {
            	var token = $("meta[name='_csrf']").attr("content");
        		var header = $("meta[name='_csrf_header']").attr("content");
            	var customeHeaders = {};
            	customeHeaders[header] = token;
            	var that = this;
            	
            	var self = this;
            	var table = this.$('.tableview');
            	
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
					autoWidth: false,
					responsive: true,
					processing: true,
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
                      "columns": [
                   	 		{
                   	 		   	data: "id",
                   	 		   	title: App.i18ntxt.global.uniqueID,
								class: "all center",
								width: "80px",
                   		 	},
                    	   {
                    		   data: "code",
                    		   title: this.i18ntxt.code,
							   class: "all",                    		   
                    	   },
                    	   {
                    		   data: "name",
                    		   title: this.i18ntxt.name,
							   class: "all",                    		   
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
                   	            	if (App.hasPermission("CellGroups", "Update")) {
                   	            		buttons.push("<button class='btn btn-primary editCellGroupButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	if (App.hasPermission("CellGroups", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deleteCellGroupButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            },
            
            ui: {
                create: '.createCellGroupButton',
                editCellGroup: '.editCellGroupButton',
                deleteCellGroup: '.deleteCellGroupButton',
                exportCellGroups: '.exportCellGroupsButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.create": 'addCellGroup',
            	"click @ui.editCellGroup": 'editCellGroup',
            	"click @ui.deleteCellGroup": 'deleteCellGroup',
            	"click @ui.exportCellGroups": 'exportCellGroups'
            },
            
            exportCellGroups: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            deleteCellGroup: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.cellgroups.cellgroup,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.cellgroups.cellGroup,
	        			name: data.name,
	        			description: data.description
	        		},
	        		rowElement: row,
	        	}, {
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
	        	
            },
            
            editCellGroup: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var data = this.dataTable.row(row).data();
            	var model = new CellGroupModel();
            	model.set(data);
            	model.set('editMode', true);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: CellGroupDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.cellgroups.editModalTitle, {name: data.name}),
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
            
            addCellGroup: function(ev) {
            	var self = this;
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.cellgroups.addModalTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: CellGroupDialogView,
            		params: {
            			model: new CellGroupModel()
            		}
            	});
            	return false;
            }
        });
        return ManageCellGroupsView;
    });
