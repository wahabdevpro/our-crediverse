define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'views/auditlog/AuditLogSearchFormView', 
		 'views/auditlog/AuditLogSearchResultView', 'models/AuditLogModel', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, HBHelper, 
		AuditLogSearchFormView, AuditLogSearchResultView, AuditLogModel) {
        //ItemView provides some default rendering logic
        var AuditLogSearchView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
			regions: {
        		searchForm: ".auditLogSearchForm",
        		searchResult: ".auditLogSearchResult" 
        	},
        	
        	i18ntxt: null,
        	
  		  	breadcrumb: function() {
				var txt = App.i18ntxt.auditLog;
  		  		return {
  		  			heading: txt.auditLogSrchHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.auditLogHeading,
  		  				href: "#auditLogList",
						iclass: "fa fa-history"
  		  			}, {
  		  				text: txt.auditLogSrchHeading,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
        	
  		  	template: "AuditLog#auditlogsearch",
  		  	url: 'api/auditlog',
  		  	error: null,
            initialize: function (options) {
            	if (!_.isUndefined(options)) this.i18ntxt = options;
            	
            	var self = this;
            	try {
					HBHelper.registerSelect();
				} catch(err) {
					if (console) console.error(err);
				}
				App.vent.listenTo(App.vent, 'application:auditlogsearch', 
					$.proxy( function(ev){ 
  		  				try {
  		  					var resultView = new AuditLogSearchResultView({criteria: ev});
  		  					this.searchResult.show(resultView);	
  		  				} catch(err) {
  		  					if (console) console.error(err);
  		  				}
					}, self )
				);
				App.vent.listenTo(App.vent, 'application:auditlogsearcherror', 
					$.proxy( function(err){
						this.searchResult.reset();
					}, self )
				);
            },
			onRefresh: function(ev) {
			},
            onRender: function () {
            	var self = this;
  		  		try {
  		  			var formView = new AuditLogSearchFormView();
  		  			this.searchForm.show(formView);	
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
            },

            ui: {
                performSearch: '.performSearch',
            },

            events: {
            	"click @ui.performSearch": 'performSearch',
            },
            
			performSearch: function(ev) {
            	var self = this;

  		  			try {
  		  				var resultView = new AuditLogSearchResultView({options: ev});
  		  				self.searchResult.show(resultView);	
  		  			} catch(err) {
  		  				if (console) console.error(err);
  		  			}
           	},
        });
        return AuditLogSearchView;
    });
