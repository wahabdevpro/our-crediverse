define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'views/tdrs/TdrSearchFormView', 
		 'views/tdrs/TdrSearchResultView', 'models/TdrModel', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, 
		TdrSearchFormView, TdrSearchResultView, TdrModel) {
        //ItemView provides some default rendering logic
        var TdrSearchView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
			regions: {
        		searchForm: ".tdrSearchForm",
        		searchResult: ".tdrSearchResult" 
        	},
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.transactions;
  		  		return {
  		  			heading: txt.transactionSearch,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.heading,
  		  				href: "#transactionList",
						iclass: "fa fa-history"
  		  			}, {
  		  				text: txt.transactionSearch,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
        	
  		  	template: "Tdrs#tdrssearch",
  		  	url: 'api/tdrs',
  		  	error: null,
            initialize: function () {
            	var self = this;
            	try {
					HBHelper.registerSelect();
				} catch(err) {
					App.error(err);
				}
				App.vent.listenTo(App.vent, 'application:tdrssearch', 
					$.proxy( function(ev){ 
  		  				try {
  		  					var resultView = new TdrSearchResultView({criteria: ev});
  		  					if (!_.isUndefined(this.searchResult))this.searchResult.show(resultView);	
  		  				} catch(err) {
  		  					if (console) console.error(err);
  		  				}
					}, self )
				);
				App.vent.listenTo(App.vent, 'application:tdrssearcherror', 
					$.proxy( function(err){
						if (!_.isUndefined(this.searchResult))this.searchResult.reset();
					}, self )
				);
            },
			onRefresh: function(ev) {
			},
            onRender: function () {
            	var self = this;
  		  		try {
  		  			var formView = new TdrSearchFormView(_.isUndefined(this.options)?{}:this.options);
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
  		  				var resultView = new TdrSearchResultView({options: ev});
  		  				self.searchResult.show(resultView);	
  		  			} catch(err) {
  		  				if (console) console.error(err);
  		  			}
           	},
        });
        return TdrSearchView;
    });
