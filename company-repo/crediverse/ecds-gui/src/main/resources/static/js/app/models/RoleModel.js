define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, app, ValidationModel) {
        // Creates a new Backbone Model class object
        var RoleModel = ValidationModel.extend({
//            initialize:function (options) {
//            	this.url = options.url;
//            },
            events: {
            	'invalid': function() {
            	}
            },

            initialize: function(options) {
				if (!_.isUndefined(options) && (options !== null)){
					if (options.id) {
						this.url += "/" + options.id;
						this.id = options.id;
					}	
					if (options.mode) 
						this.mode = options.mode;
					if (!_.isUndefined(options.form)) 
						this.bind(options.form);
					if (!_.isUndefined(options.url)) 
						this.url = options.url
				}
			},
			
            // Default values for all of the Model attributes
            defaults:{
            	enabled: false,
            	action: 'Modify',
            	actionClass: 'roleModifyButton',
            	active: false
            },
            
            getGroups: function() {
            	//var permissions = '';
            },
            
            validate: function() {
            },
            
            updatePermission: function(state, data) {
            	var permissions = this.get('permissions');
            	if (_.isUndefined(permissions)) permissions = [];
            	
            	if (state) {
            		permissions.push(data);
            	}
            	else {
            		permissions = _.without(permissions, _.findWhere(permissions, {
              		  id: data.id
              	}));
            	}
            	
            	this.set('permissions', permissions, {slient: true});
            },
            
            saveExample: function(attrs, options) {
                options || (options = {});
                attrs || (attrs = _.clone(this.attributes));

                // Filter the data to send to the server
                //delete attrs.selected;
                //delete attrs.dontSync;

                options.data = JSON.stringify(attrs);

                // Proxy the call to the original save function
                return Backbone.Model.prototype.save.call(this, attrs, options);
            }
        });

        return RoleModel;
    }
);
