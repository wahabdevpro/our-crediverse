define( ['jquery', 'App', 'backbone', 'marionette', 'models/AuditLogModel', 'views/auditlog/AuditLogTableView'],
    function($, App, BackBone, Marionette, AgentModel, AuditLogTableView) {
        //ItemView provides some default rendering logic
        var AuditLogSearchResultView =  AuditLogTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
        	ui: {
        		exportLog: '.exportAuditButton'
        	},
        	events: {
        		"click @ui.exportLog": 'exportLog',
        	},
        	
			template: "AuditLog#auditlogsearchresult",
			url: 'api/auditlog/search',
            
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
            },
        });
        return AuditLogSearchResultView;
    });
