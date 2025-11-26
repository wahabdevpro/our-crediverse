define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        //ItemView provides some default rendering logic
        var AgentUserDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},        	
        	url: 'api/wusers',
  		  	template: "AgentAccount#agentuserdialogview",
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) 
            		this.model = options.model;
            	if(!_.isUndefined(this.model) && !_.isUndefined(this.model.attributes) && !_.isUndefined(this.model.attributes.domainAccountName)) {
            		this.savedDomainAccountName = this.model.attributes.domainAccountName;
            	}
            },
            
            onRender: function () {
            	var self = this;
            	// Set Select Options
            	this.$("#language").val(this.model.get("language"));
            	this.$("#state").val(this.model.get("state"));
            	this.$("#roleID").val(this.model.get("roleID"));
            	if(this.model.mode === "create"){
            		this.$("#authenticationMethod").val("P");
            	}
            	var useDomainAccount = (this.model.attributes.authenticationMethod == "X");
            	this.$("#useDomainAccount").prop('checked', useDomainAccount);
            	//Horrible Hack: validate() initializes something inside JQuery Validate that makes rule addition and removal work.
            	setTimeout(function(){self.afterRender(self);}, 500);
            },
            
            afterRender: function(self) {
	  		  	self.model.validate();
            	self.onChangeUseDomainAccount();
            },
            
            ui: {
                view: "",
                save: ".userCreateButton",
                useDomainAccountCheckbox: "#useDomainAccount"
            },
            
            // View Event Handlers
            events: {
            	"click @ui.save": "saveUser",
            	"change @ui.useDomainAccountCheckbox": "onChangeUseDomainAccount"
            },
            
            saveUser: function() {
            	var self = this;
            	if(this.$("#useDomainAccount").prop('checked')){
            		this.model.attributes.authenticationMethod = "X";
        		} else {
        			this.model.attributes.authenticationMethod = "P";
        			this.model.attributes.domainAccountName = "";
        		}
            	
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
            
            onChangeUseDomainAccount: function() {
            	var self = this;
				var isDomainAccountNameEnabled = this.$("#useDomainAccount").prop('checked');
				this.$('#domainAccountName').prop('disabled', !isDomainAccountNameEnabled);		
				this.$('#domainAccountNameLabel').prop('disabled', !isDomainAccountNameEnabled);
				
				if(!isDomainAccountNameEnabled){					
					self.model.rules.domainAccountName = {required : false};
					self.$('#domainAccountName').rules("remove");
					self.$(".domainAccountNameItemGroup").hide();
				} else {
					self.model.rules.domainAccountName = {required : true};
					self.$('#domainAccountName').rules("add", 
                            {
                                required: true,
                            });
					self.$(".domainAccountNameItemGroup").show();
				}
				//Highlight the incomplete fields
				self.model.validate();
				self.model.showRequiredStars();
            }
            
            
        });
        return AgentUserDialogView;
    });
