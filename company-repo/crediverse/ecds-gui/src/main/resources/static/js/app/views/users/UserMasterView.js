define( ['jquery', 'App', 'marionette', 'handlebars', 'utils/HandlebarHelpers',
         'models/RoleModel', 'models/DepartmentModel', 'models/UserValidationModel',
         'views/users/UserDialogView', 'views/users/PermanentUserDialogView', 'views/users/UserOperationsView',
         'utils/IsoLanguage', 'utils/CommonUtils', 'views/PasswordChangeDialogView', 'models/PasswordChangeModel', 'datatables'],
    function($, App, Marionette, Handlebars, HBHelper,
    		RoleModel, DepartmentModel, UserValidationModel,
    		UserDialogView, PermanentUserDialogView, UserOperationsView,
    		IsoLanguage, CommonUtils, PasswordChangeDialogView, PasswordChangeModel) {
        //ItemView provides some default rendering logic
        var UserManagementView =  UserOperationsView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: 'row',
        	},
        	template: "ManageUsersTableView#usermaster",
        	currentFilter: {}, // Used to keep track of filter settings for use by export.

        	i18n: App.i18ntxt.userman,

  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.userman;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.userAccess,
						iclass: "fa fa-key"
  		  			}, {
  		  				text: App.i18ntxt.navbar.users,
  		  				href: "#users"
  		  			}]
  		  		}
  		  	},

  		  	error: null,
            initialize: function (options) {
            	var self = this;
            },

            onRender: function () {
            	var self = this;
            	var labels = App.i18ntxt.userman;

            	if (this.rolesModel == null) {
            		this.loadModels();
            	} else {

                	// Render User Table
                	this.dataTable = this.$('.tableview').DataTable( {
            			//serverSide: true,
            			// data is params to send
                		autoWidth: false,
    					responsive: true,
    					processing: true,
    					"language": {
    		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
    		            },
              			"ajax": function(data, callback, settings) {
              				self.currentFilter.url = self.url;
              				self.currentFilter.data = data;
              				var jqxhr = $.ajax(self.url + "/data", {
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
                        		   data: "fullName",
                        		   title: labels.fullNameHead,
    							   class: "all",
    							   render: function(data, type, row, meta) {
    							   		return '<a href="#user/'+row['id']+'" class="routerlink">' + data + '</a>';
    							   }
                        	   },
                        	   {
                        		   data: "mobileNumber",
                        		   title: labels.mobileNumberHead,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "accountNumber",
                        		   title: labels.accountNumber,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "departmentName",
                        		   title: labels.departmentHead,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "role.name",
                        		   title: labels.roleHead,
                        		   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "actDateFormatted",
                        		   title: labels.actDateHead
                        	   },
                        	   {
                        		   data: "language",
                        		   title: labels.languageHead,
                        		   render: function(data, type, row, meta) {
                        			   return IsoLanguage[data].name;
                        		   }
                        	   },
                        	   {
                        		   data: "state",
                        		   title: labels.statusHead,
    							   className: "center",
    							   //width: "100px"
                        		   render: function(data, type, row, meta) {
                        			   var response = [];
                        			   response.push('<span class="label');
                        			   if (data === 'A') {
                        				   response.push('label-success">');
                        				   response.push(App.i18ntxt.enums.state.active);
                        				   response.push('</span>');
                        			   } else if (data === 'S') {
                        				   response.push('label-warning">');
                        				   response.push(App.i18ntxt.enums.state.suspended);
                        				   response.push('</span>');
                        			   } else if (data === 'D') {
                        				   response.push('label-danger">');
                        				   response.push(App.i18ntxt.enums.state.deactivated);
                        				   response.push('</span>');
                        			   } else {
                        				   response.push('label-default">');
                        				   response.push(App.i18ntxt.enums.state.permanent);
                        				   response.push('</span>');
                        			   }
                        			   return response.join(' ');
                        		   }
                        	   },
                        	   {
                      	            targets: -1,
                       	            data: null,
                       	            title: "",
                       	            sortable: false,
    								class: "nowrap right all",
    								width: "108px",
                       	            render: function(data, type, row, meta) {
                	            		var menuData = {};
                       	            	menuData.rowID = row.id;
                       	            	menuData.authenticationMethod = row.authenticationMethod;
                       	            	menuData.canChangePassword = false;
                       	            	menuData.canResetPassword = false;
                       	            	if(!_.isUndefined(data.authenticationMethod) && row.authenticationMethod == "A"){
                       	            		if(row.id == App.contextConfig.user.id){
                       	            			menuData.canChangePassword = true;
                       	            		} else if(App.hasPermission("WebUser", "ResetPasswords")){
                       	            			menuData.canResetPassword = true;
                       	            		}
                       	            	}
                       	            	menuData.notPermanent = (row.state==="P")?false:true;
                       	            	var $html = $(CommonUtils.getTemplateHtml("UserView#tableMenuView", menuData));
                       	            	if ($html.find('.dropdown-menu li').length > 0) {
                       	            		$html.find('.actionDropdown').show();
                       	            		$html.find('.actionButton').hide();
                       	            	}
                       	            	else {
                       	            		$html.find('.actionDropdown').hide();
                       	            		$html.find('.actionButton').show();
                       	            	}
                       	            	return $html.html();
                	            	}
                       	       }
                          ]
                      } ); // End: Render User Table

            	}

            },

            // UI Aliases for event handler
            ui: {
                user: '',
                create: '.createUserButton',
                edit: '.editUserButton',
                delete: '.deleteUserButton',
                exportUser: '.exportUserButton',
                performPasswordChange: '.changePasswordButton',
                performPasswordReset: '.resetPasswordButton',
            },

            // View Event Handlers
            events: {
            	"click @ui.user": 'viewUser',
            	"click @ui.create": 'createUser',
            	"click @ui.edit": 'editUser',
            	"click @ui.delete": 'deleteUser',
            	"click @ui.exportUser": 'exportUser',
            	"click @ui.performPasswordChange" : 'performPasswordChange',
            	"click @ui.performPasswordReset" : 'performPasswordReset'
            },

            exportUser: function(ev) {
				var self = this;

				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
			},

            createUser: function(ev) {
            	var self = this;

            	var model = new UserValidationModel({
            		availRoles: this.rolesModel.attributes.roles,
            		availDepartments: this.departmentsModel.attributes.departments
            	});

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: self.i18n.addUserTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: UserDialogView,
            		params: {
            			model: model
            		}
            	});
            },

            deleteUser: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();

            	if (data.state != "P") {
    	        	CommonUtils.delete({
    	        		itemType: App.i18ntxt.userman.user,
    	        		url: self.url+'/'+data.id,
    	        		data: data,
    	        		context: {
    	        			what: App.i18ntxt.userman.user,
    	        			name: data.fullName,
    	        			description: data.domainAccountName
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
            	}
            },
            performPasswordChange: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	var model = new PasswordChangeModel();
            	model.attributes.entityId = data.id;
            	model.attributes.entityType = 'WEBUSER';
            	App.log("performPasswordChange");

            	model.fetch({
            		url: "api/wusers/passwordrules",
            		success: _.bind(function() {
            			model.updateRules();
            			//CommonUtils.showViewDialog(model, PasswordChangeDialogView);

            			App.vent.trigger('application:dialog', {
                    		name: "viewDialog",
                    		title:  CommonUtils.renderHtml(App.i18ntxt.changePasswordDialog.changePassword, {minPinLength: model.attributes.minPinLength}),
                    		hide: function() {
        	        			self.dataTable.ajax.reload().draw();
        	        		},
                    		view: PasswordChangeDialogView,
                    		params: {
                    			model: model,
                    			url: '/api/wusers/change_password'
                    		}
                    	});

            		}, this)
        		});

            },
            performPasswordReset: function(ev) {
            	App.log("performPasswordReset");
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	var headingModel = {name: (data.firstName + " " + data.surname), email: data.email};
            	var content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetModalMessage",  headingModel).html();
            	App.vent.trigger('application:dialog', {
	        		text: content,
	        		name: "yesnoDialog",
	        		events: {
	        			"click .yesButton": 
	        			function(event) {
	        				var dialog = this;
	        				self.doPasswordReset(data, dialog);
	        			}
	        		}
        		});
            },
            
            doPasswordReset: function(data, dialog)
            {
            	var self = this;
            	var model = new PasswordChangeModel();
            	model.attributes.entityId = data.id;
            	var headingModel = {name: (data.firstName + " " + data.surname), email: data.email};
            	
            	App.log("doPasswordReset");
            	var payload = {
                		entityId : data.id
                	}; 
            	var fullurl = CommonUtils.getContextPath() + "api/wusers/reset_password";
            	
            	$.ajax({
			    	type : "POST",
			        url: fullurl,
			        headers : {
			            'Accept': 'application/json',
			            'Content-Type': 'application/json'
			        },
			        data : JSON.stringify(payload, null, 2),
			        datatype : "json",
			        timeout : self.TIMEOUT
			    }).done(function(response) {
			    	$(dialog).find(".msg-content").hide();
        	    	$(dialog).find(".yesButton").hide();
			    	if(!_.isUndefined(data.email)){
						var successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetSuccess",  headingModel).html();
						$(dialog).find(".noButton").text(App.i18ntxt.global.okBtn);	
            	    	$(dialog).find(".modal-text").html( successMessage );
			    	} else {
			    		//User does not have email
			    		var noEmailMessage = App.translate("changePasswordDialog.missingEmail");
	        	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
	        	    	$(dialog).find(".modal-text").html( noEmailMessage );     
			    	}
			    }).fail(function(response) {
			    	console.log("Password Reset Failed");
			    	var errorMessage = App.translate("enums.returncode." + response.returnCode);
			    	$(dialog).find(".msg-content").hide();
        	    	$(dialog).find(".yesButton").hide();
        	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
        	    	$(dialog).find(".modal-text").html( errorMessage );        	    	
			    });
            	
            }
        });
        return UserManagementView;
    });
