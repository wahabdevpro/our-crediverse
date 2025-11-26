define( ['jquery', 'App', 'backbone', 'marionette', 'views/ReplenishView', 'models/AgentModel', 'models/RootAccountModel'],
    function($, App, BackBone, Marionette, ReplenishView, AgentModel, RootAccountModel) {
        //ItemView provides some default rendering logic
        var RootAccountView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
        	ui: {
                replenish: '.replenishButton'
        	},
        	events: {
            	"click @ui.replenish": 'replenishRootAccount',
        	},
        	
  		  	template: "ManageDashBoard#rootAccountBalanceView",
            
  		  	initialize: function (options) {
  		  		var self = this;
            },
            
            onRender: function () {
  		  		try {
  		  			var self = this;
  		  			var agent = new AgentModel({account: 'root'});
  		  			agent.fetch({
  		  				success: function(data){
  		  					self.$('#accountBalance').html(data.get('balance'));
  		  				}
  		  			});
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
  		  	},
            
            replenishRootAccount: function(ev) {
            	var self = this;
            	            	
            	var model = new RootAccountModel({
            		url: this.url
            	});
            	model.set({amount : 0});
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: ReplenishView,
            		//backdrop: true,
            		//view:"views/roles/PermissionView",
            		params: {
            			model: model
            		}
            	});

            	return false;
            }
        });
        return RootAccountView;
    });
