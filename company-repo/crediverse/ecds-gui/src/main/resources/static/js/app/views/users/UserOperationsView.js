define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/IsoLanguage', 
         'models/UserModel', 'utils/CommonUtils', 'models/UserValidationModel',
         'views/users/UserDialogView', 'views/users/PermanentUserDialogView'],
    function($, _,  App, BackBone, Marionette, IsoLanguage, 
    		UserModel, CommonUtils, UserValidationModel,
    		UserDialogView, PermanentUserDialogView) {
	
	
	var UserOperationsView =  Marionette.ItemView.extend( {
		url: 'api/wusers',
		urlRoles: 'api/roles/webuser/data',
		urlDepartments: 'api/departments',
		
		// Models
		rolesModel: null,
		departmentsModel: null,
		
		preloadModels: function() {
        	var self = this;
        	this.rolesModel = new Backbone.Model();
        	this.rolesModel.url = this.urlRoles;
        	
    		this.departmentsModel = new Backbone.Model();
    		this.departmentsModel.url = this.urlDepartments;
    		return [
    			this.rolesModel.fetch().then(function(){
        			self.rolesModel.set( "roles", self.addDefaultItem(self.rolesModel) );
        		}),
        		this.departmentsModel.fetch().then(function(){
        			self.departmentsModel.set("departments", self.addDefaultItem(self.departmentsModel));
        		})
    		];
        },
        
        // Add "0" item to roles and departments list
        addDefaultItem: function (model) {
			var data = model.get("data");
			data.unshift({id:0, name:App.i18ntxt.notSet, description:App.i18ntxt.notSet});
			return data;
        },
        
        editUser: function(ev) {
        	var self = this;
        	var userData = null;
        	if (_.isUndefined(this.dataTable)) {
        		userData = _.extend({}, this.model.attributes);
        	}
        	else {
        		var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
        		userData = _.extend({}, currentRow.data());
        	}
        	
        	// url ?!?
        	var model = new UserValidationModel({
        		availRoles: this.rolesModel.attributes.roles,
        		availDepartments: this.departmentsModel.attributes.departments
        	});
        	
        	model.set(userData);
        	
        	App.vent.trigger('application:dialog', {
        		name: "viewDialog",
        		view: (userData.state == 'P')? PermanentUserDialogView : UserDialogView,
        		title: CommonUtils.renderHtml(App.i18ntxt.userman.editUserTitle, {user: userData.domainAccountName, uniqueID: userData.id}),
        		hide: function() {
        			if (_.isUndefined(self.dataTable)) {
        				self.model.set(model.attributes);
        				self.render();
        			}
        			else {
        				self
            			.dataTable.ajax.reload().draw();
        			}
        		},
        		params: {
        			model: model
        		}
        	});
        	return false;
        },
	});
	
	return UserOperationsView;
});