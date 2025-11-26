define( ['jquery', 'underscore', 'App', 'marionette'],
    function($, _, App, Marionette) {
        //ItemView provides some default rendering logic
        var TransactionsASearchView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
        	regions: {
        		rootAccount: ".rootAccountDashBoard",
        		lastTransactions: ".lastTransactionsDashBoard" 
        	},
        	
  		  	template: "MainView#dashboardContent",
  		  	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.transactionsA;
  		  		return {
  		  			heading: txt.searchHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.searchPageBC,
  		  				href: "#transactionASearch",
						iclass: "fa fa-history"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	initialize: function (options) {
            },
            
            onRender: function () {
            	
  		  	},
            

        });
        return TransactionsASearchView;
    });
