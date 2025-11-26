define( ['jquery', 'underscore', 'App', 'marionette', 'views/departments/DepartmentDialogView', 'models/DepartmentModel', 'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, DepartmentDialogView, DepartmentModel, HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
        var ManageDepartmentsView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageDepartments#managedepartments",
  		  	url: 'api/departments',
  		  	error: null,
  		  	
  		  	i18ntxt: App.i18ntxt.departments,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.departments;
  		  		return {
  		  			heading: txt.departmentManagement,
  		  			defaultHome: false,
  		  			breadcrumb: [{
		  				text: App.i18ntxt.navbar.userAccess,
						iclass: "fa fa-key"
		  			}, {
		  				text: App.i18ntxt.navbar.departments,
		  				href: window.location.hash
		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            	var model = new DepartmentModel({url: 'api/departments'});
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
                   	            	if (App.hasPermission("Department", "Update")) {
                   	            		buttons.push("<button class='btn btn-primary editDepartmentButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	if (App.hasPermission("Department", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deleteDepartmentButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            },
            
            ui: {
                create: '.createDepartmentButton',
                editDepartment: '.editDepartmentButton',
                deleteDepartment: '.deleteDepartmentButton',
                exportDepartments: '.exportDepartmentsButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.create": 'addDepartment',
            	"click @ui.editDepartment": 'editDepartment',
            	"click @ui.deleteDepartment": 'deleteDepartment',
            	"click @ui.exportDepartments": 'exportDepartments'
            },
            
            exportDepartments: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            deleteDepartment: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.departments.department,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.departments.department,
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
            
            editDepartment: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var data = this.dataTable.row(row).data();
            	var model = new DepartmentModel();
            	model.set(data);
            	model.set('editMode', true);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: DepartmentDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.departments.editModalTitle, {name: data.name}),
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
            
            addDepartment: function(ev) {
            	var self = this;
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.departments.addModalTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: DepartmentDialogView,
            		params: {
            			model: new DepartmentModel()
            		}
            	});
            	return false;
            }
        });
        return ManageDepartmentsView;
    });
