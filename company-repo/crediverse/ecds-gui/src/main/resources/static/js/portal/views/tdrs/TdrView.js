define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 'models/TdrModel', 'views/tdrs/TdrReverseView', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, TdrModel, TdrReverseView) {
        //ItemView provides some default rendering logic
        var TdrView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "Transaction#transactiondetails",
  		  	url: 'papi/tdrs/',
  		  	error: null,
			tierList: null,
			id: null,
			i18ntxt: App.i18ntxt.transactions,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.tdrViewHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.heading,
  		  				href: "#transactionList",
						iclass: "fa fa-history"
  		  			}, {
  		  				text: txt.tdrViewHeading,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function () {
            },
            
            onRender: function () {
            	if (_.isUndefined(this.model)) {
            		this.retrieveTransactionData();
            	}
            },
            
			retrieveTransactionData: function() {
            	var self = this;
            	this.model = new TdrModel({id: self.id});
            	this.model.fetch({
            		success: function(ev){
						self.render();
					},
				});
			},	
			
			reverseTransaction: function(event) {
//				var that = this;
//            	// Model data (MSISDN) required for transfer
//            	App.vent.trigger('application:dialog', {
//            		name: "viewDialog",
//            		view: TdrReverseView,
//            		params: {
//            			model: that.model
//            		},
//            		hide: function() {
//            			that.model.fetch({
//                    		success: function(ev){
//                    			that.render();
//        					},
//        				});
//	        		}
//            	});
//            	return false;
			},

            ui: {
                reverseButton: '.reverseButton',
            },

            // View Event Handlers
            events: {
//            	"click @ui.reverseButton": 'reverseTransaction',
            }
        });
        return TdrView;
    });
