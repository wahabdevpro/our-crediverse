define( ['jquery', 'underscore', 'App', 'marionette'],
    function($, _, App, Marionette) {
        //ItemView provides some default rendering logic
        var TransactionsAListView =  Marionette.LayoutView.extend( {
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
  		  			heading: txt.listHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.listPageBC,
  		  				href: "#transactionAList",
						iclass: "fa fa-history"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	initialize: function (options) {
            },
            
            onRender: function () {
            	
  		  	},
            

        });
        return TransactionsAListView;
    });
