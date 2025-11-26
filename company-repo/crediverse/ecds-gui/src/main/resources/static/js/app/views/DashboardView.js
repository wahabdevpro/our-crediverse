define( ['jquery', 'underscore', 'App', 'marionette', 'views/dashboard/RootAccountView', 'views/dashboard/LastTransactionsView'],
    function($, _, App, Marionette, RootAccountView, LastTransactionsView) {
        //ItemView provides some default rendering logic
        var DashboardView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
        	regions: {
        		rootAccount: ".rootAccountDashBoard",
        		lastTransactions: ".lastTransactionsDashBoard" 
        	},
        	
  		  	template: "Dashboard#dashboardContent",
  		  	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.dashboard;
  		  		return {
  		  			heading: txt.heading,
  		  			breadcrumb: [{
  		  				text: txt.homeBC,
  		  				href: "#dashboard",
  		  				iclass: "fa fa-dashboard"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	initialize: function (options) {
            },
            
            onRender: function () {
  		  		try {
  		  			var rootView = new RootAccountView();
  		  			this.rootAccount.show(rootView);
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
  		  		try {
  		  			var ltView = new LastTransactionsView();
  		  			this.lastTransactions.show(ltView);	
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
  		  	},
            
            ui: {
                view: ''
            },

            // View Event Handlers
            events: {
            	"click @ui.view": 'viewDashboard'
            },
            
            viewDashboard: function() {
            	
            }

        });
        return DashboardView;
    });
