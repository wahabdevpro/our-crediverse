define( ['jquery', 'backbone', 'App', 'marionette', 'models/FeatureBarModel'],
    function($, BackBone, App, Marionette, FeatureBarModel) {
        //ItemView provides some default rendering logic
        var ApiUserDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},        	
        	url: 'api/wusers',
  		  	template: "AgentAccount#apiuserdialogview",

			featureBar: null,

            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) 
            		this.model = options.model;
            	if(!_.isUndefined(this.model) && !_.isUndefined(this.model.attributes) && !_.isUndefined(this.model.attributes.domainAccountName)) {
            		this.savedDomainAccountName = this.model.attributes.domainAccountName;
            	}
				this.featureBar = new FeatureBarModel();

				const self = this;
				let featureName = 'channelTypeFeature'

				self.featureBar.setFeature(featureName);
				self.featureBar.fetch().then(isEnabled => {
					self.model.set(featureName, isEnabled ? "enabled" : "disabled");
					self.render();
				});
            },



            onRender: function () {
            	var self = this;
            	// Set Select Options
            	this.$("#language").val(this.model.get("language"));
            	this.$("#state").val(this.model.get("state"));
            	this.$("#roleID").val(this.model.get("roleID"));
            	this.$("#channelType").val(this.model.get("channelType"));
            	if(this.model.mode === "create"){
            		this.$("#authenticationMethod").val("A");
            	}
            	
            },

            ui: {
                view: "",
                save: ".userCreateButton",
                useDomainAccountCheckbox: "#useDomainAccount"
            },
            
            // View Event Handlers
            events: {
            	"click @ui.save": "saveUser"
            },
            
            saveUser: function() {
            	var self = this;
            	this.model.save({
            		success: function(ev){
            			var dialog = self.$el.closest(".modal");
            			dialog.modal("hide");
            		},
            		error: function(ev){
            			var dialog = self.$el.closest(".modal");
            		}
				});
            },
        });
        return ApiUserDialogView;
    });
