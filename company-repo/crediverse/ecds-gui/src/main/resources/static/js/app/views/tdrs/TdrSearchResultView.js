define( ['jquery', 'App', 'backbone', 'marionette', 'models/TdrModel', 'views/tdrs/TdrTableView', 'utils/CommonUtils'],
    function($, App, BackBone, Marionette, AgentModel, TdrTableView, CommonUtils) {
        //ItemView provides some default rendering logic
        var TdrSearchResultView =  TdrTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
        	ui: {
                exportTdrs: '.exportTdrsButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.exportTdrs": 'exportTdrs'
            },
        	
			template: "Tdrs#tdrssearchresult",
			url: 'api/tdrs/search',
            
  		  	initialize: function (options) {
  		  		var self = this;
				var args = "";
				for (var key in options.criteria) {
					if ( options.criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(options.criteria[key]);
					}	
				}
				this.url += '?' + args;
            }
        });
        return TdrSearchResultView;
    });
