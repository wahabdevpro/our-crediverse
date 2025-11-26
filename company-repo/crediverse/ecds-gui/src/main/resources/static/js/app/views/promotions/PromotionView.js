define( ['jquery', 'underscore', 'App', 'backbone', 'marionette'],
    function($, _,  App, BackBone, Marionette) {
		
		var i18ntxt =  App.i18ntxt.promotions;
	
        var PromotionView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
  		  	template: "PromotionView#promotionView",
  		  	url: 'api/promos/',
  		  	
  		  	model: null,
  		  	
  		  	breadcrumb: function() {
  		  		return {
  		  			heading: i18ntxt.promotion,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: i18ntxt.heading,
  		  				href: "#promotionsList",
						iclass: "fa fa-trophy"
  		  			}, {
  		  				text: i18ntxt.promotion,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},  		  	
  		  	
            initialize: function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.id = options.id;
            		}
            	}
            },
            
            retrieveData: function() {
            	App.log("Retriving promotion data for " + this.id);
            	var self = this;
            	this.model = new Backbone.Model();
            	this.model.url = this.url + this.id;
            	this.model.fetch({
            		error: function(err) {
            			App.error(err);
            		},
            		success: function(ev){
            			var tp = "enums.period." + self.model.get("targetPeriodInfo");
            			self.model.set("targetPeriodInfo", App.translate(tp, tp));
						self.render();
					}
				});
			},
			
			onRender: function() {
				if (this.model == null) {
					this.retrieveData();
				}
			}
      
    });
    
    return PromotionView;
});
      