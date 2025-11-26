define( ['jquery', 'underscore', 'App', 'backbone', 'marionette'],
    function($, _, App, BackBone, Marionette) {
		var CellView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "CellView#cellView",
			url: 'api/cells/',
  		  	error: null,
			id: null,
			i18ntxt: App.i18ntxt.cells,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.cell,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.cellManagement,
  		  				href: "#cells",
						iclass: "fa fa-cloud"
  		  			}, {
  		  				text: txt.cell,
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
            	App.log("Retriving cells data for " + this.id);
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
        return CellView;
    });
