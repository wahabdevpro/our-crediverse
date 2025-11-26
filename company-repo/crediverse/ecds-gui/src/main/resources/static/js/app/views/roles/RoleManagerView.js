define( ['jquery', 'App', 'marionette', 'views/roles/PermissionView', 'models/RoleModel', 'models/RoleValidationModel', 'utils/CommonUtils', 'utils/HandlebarHelpers', 'datatables'],
    function($, App, Marionette, PermissionView, RoleModel, RoleValidationModel, CommonUtils, HandlebarHelpers) {
        //ItemView provides some default rendering logic
        var RoleManagerView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageRoles#rolemaster",
  		  	url: 'api/roles',
  		  	error: null,
        	i18ntxt: null,
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.roleman;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
		  			breadcrumb: [{
		  				text: App.i18ntxt.navbar.userAccess,
						iclass:"fa fa-key"
		  			}, {
		  				text: App.i18ntxt.navbar.roles,
		  				href: window.location.hash
		  			}]  		  		
  		  		}
  		  	},

  		  	initialize: function (options) {
  		  		if (!_.isUndefined(options)) this.i18ntxt = options;
            },
            
            onRender: function () {
            	var self = this;
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url
            	};
            	
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
                      "columns": [
                	 	   	{
                   		 	   	data: "id",
                   		 	   	title: App.i18ntxt.global.uniqueID,
								class: "all center",
								width: "80px",
                   		 	},
                    	   {
                    		   data: "name",
                    		   title: App.i18ntxt.roleman.name,
							   class: "all",
                    	   },
                    	   {
                    		   data: "description",
                    		   title: App.i18ntxt.roleman.description,
							   class: "min-tablet",
                    	   },
                    	   {
                    		    data: "type",
                    		    title: App.i18ntxt.roleman.type,
                   	            render: function(data, type, row, meta) {
									if(data=='A') return 'Agent';
									if(data=='W') return 'Web User';
									return '-';
								}
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
                   	            sortable: false,
								class: "nowrap right all",
								width: "170px",
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if (!row.permanent) {
                   	            		if (App.hasPermission("Role", "Update")) {
                   	            			buttons.push("<button class='btn btn-primary editPermButton btn-xs'>"+App.i18ntxt.roleman.permissions+"</button>");
                   	            			buttons.push("<button class='btn btn-primary editRoleButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            		}
                   	            		if (App.hasPermission("Role", "Delete")) {
                   	            			buttons.push("<button class='btn btn-danger deleteRoleButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            		}
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            ui: {
                role: '',
                create: '.createRoleButton',
                editRole: '.editRoleButton',
                deleteRole: '.deleteRoleButton',
                editPerm: '.editPermButton',
                exportRoles:'.exportRolesButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.role": 'viewRole',
            	"click @ui.create": 'createRole',
            	"click @ui.editRole": 'editRole',
            	"click @ui.deleteRole": 'deleteRole',
            	"click @ui.editPerm": 'editPerm',
            	"click @ui.exportRoles": 'exportRoles',
            },
            
            exportRoles: function(ev) {
				var self = this;
				
				var table = this.$('.tableview');
				var pos = self.url.indexOf('?')
				var searchValue = this.dataTable.search();
				var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
				CommonUtils.exportAsCsv(ev, baseUrl+'/search', searchValue, self.criteria, true);
			},
            
            editPerm: function(ev) {
            	var data = this.dataTable.row($(ev.currentTarget).closest('tr')).data();
            	var model = new RoleModel();
            	model.url = this.url + '/' + data.id; 
            	
            	model.fetch({
            		success: function(data) {
//            			App.log(JSON.stringify(data, 2, null));
            			App.vent.trigger('application:dialog', {
            				name: "viewDialog",
            				class: 'modal-lg',
                    		view: PermissionView,
                    		//view:"views/roles/PermissionView",
                    		params: {
                    			model: model
                    		}
                    	});
            		}
            	});
            	return false;
            },

            deleteRole: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.roleman.role,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.roleman.role,
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

            editRole: function(ev) {
            	var self = this;
            	var dialogModel = null;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var roleData = _.extend({}, currentRow.data());
            	
            	App.vent.trigger('application:dialog', {
            		name: 'roleModal',
            		title: CommonUtils.renderHtml(App.i18ntxt.roleman.editRoleTitle, {role: roleData.name}),
            		init: function() {
            			var form = $('#roleModal form');
            			dialogModel = new RoleValidationModel({id: roleData.id, url: (self.url+'/details/'+roleData.id)});
            			dialogModel.bind(form);
            		},
            		model: new Backbone.Model(roleData),	// For Auto-Form text insert
            		events: {
            			'click .roleCreateButton': function(event) {
            				var dialog = this;
        					dialogModel.save({
        						preprocess: function(data) {
        							data = _.extend(roleData, data);
        							return data;
        						},
        						success: function(mdl, resp) {
        							dialog.modal('hide');
        							self.render();
        						},
        						error: function(mdl, error) {
            				    	if (console) console.error("Save problem: " + error);
        						}
        					});
            			}
            		}
            	});
            	return false;
            },
            
            createRole: function(ev) {
            	var self = this;
            	var dialogModel = null;
            	
            	App.vent.trigger('application:dialog', {
            		name: 'roleModal',
            		title: App.i18ntxt.roleman.addRoleTitle,
            		init: function() {
            			var form = $('#roleModal form');
            			dialogModel = new RoleValidationModel();
            			dialogModel.bind(form);
            		},
            		events: {
            			'click .roleCreateButton': function(event) {
            				var dialog = this;
        					dialogModel.save({
        						success: function(mdl, resp) {
        							dialog.modal('hide');
        							self.render();
        						},
        						error: function(mdl, error) {
            				    	if (console) console.error("Save problem: " + error);
        						}
        					});
            			}
            		}
            	});
            	return false;
            },
            
        });
        return RoleManagerView;
    });
