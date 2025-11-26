define( ['jquery', 'underscore', 'App', 'marionette'],
    function($, _, App, Marionette) {
        //ItemView provides some default rendering logic
        var TransactionsBListView =  Marionette.LayoutView.extend( {
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
  		  		var txt = App.i18ntxt.transactionsB;
  		  		return {
  		  			heading: txt.listHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.listPageBC,
  		  				href: "#transactionBList",
						iclass: "fa fa-history"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	initialize: function (options) {
            },
            
            onRender: function () {
            	
  		  	},
            

        });
        return TransactionsBListView;
    });
