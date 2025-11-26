define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/TdrModel', 'views/tdrs/TdrReverseView', 'views/tdrs/TdrAdjudicateView', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, 
    		HBHelper, TdrModel, TdrReverseView, TdrAdjudicateView) {
        //ItemView provides some default rendering logic
        var TdrView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "Transaction#transactiondetails",
  		  	url: 'api/tdrs/',
  		  	error: null,
			tierList: null,
			id: null,
			fetchSubscriberState:null,
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
            	} else {
                	var self = this;
					var actions = 0;
                	if (!_.isUndefined(this.model) && this.model.has('followUp')) {
                		var followUp = this.model.get('followUp');
                		var adjudicated = this.model.get('adjudicated');
						if ( followUp && !adjudicated ) {
                			$('#transactiondetails .tdrActions .adjudicateButton').show();
							actions++;
						}	
					}
                	if (!_.isUndefined(this.model) && this.model.has('type') && (!this.model.has('followUp') || !this.model.get('followUp'))) {
                		var type = this.model.get('type').toLowerCase();
                		if (	type == 'tx' ||
    	           		 			type == 'sl' ||
    	        	    			type == 'st'
    	           		 			) {
                			// This type could have a reversal
                			if (this.model.has('reversals')) {
                				/*
                				 * 	http://localhost:8084/#transaction/00000204005 http://localhost:8084/api/tdrs/00000204005
									http://localhost:8084/#transaction/00000204004 http://localhost:8084/api/tdrs/00000204004
									
									
									Ticket with success reversal after failed:
									http://localhost:8084/#transaction/00000204006 http://localhost:8084/api/tdrs/00000204006
                				 */
                				var reversals = this.model.attributes.reversals;
                				var alreadyReversed = false;
                				for (var i=0; i<reversals.length; i++) {
                					var reversal = reversals[i];
                    				if (reversal.returnCode === 'SUCCESS') alreadyReversed=true;
                				}
                				if (!alreadyReversed) {
                					$('#transactiondetails .tdrActions .reverseButton').show();
									actions++;
                				}
                			}
                			else {
                				$('#transactiondetails .tdrActions .reverseButton').show();
								actions++;
                			}
                		}
                	}
					if ( actions > 0 ) {
                		$('#transactiondetails .tdrActions').show();
					}
            	}

            	
            },
            
			retrieveTransactionData: function() {
            	var self = this;
            	this.model = new TdrModel({id: self.id,fetchSubscriberState: true});
            	this.model.fetch({
            		success: function(ev){
						self.render();
					},
				});
			},	
			
			reverseTransaction: function(event) {
				var that = this;
            	// Model data (MSISDN) required for transfer
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: TdrReverseView,
            		params: {
            			model: that.model
            		},
            		hide: function() {
            			that.model.fetch({
                    		success: function(ev){
                    			that.render();
        					},
        				});
	        		}
            	});
            	return false;
			},

			adjudicateTransaction: function(event) {
				var that = this;
            	// Model data (MSISDN) required for transfer
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: TdrAdjudicateView,
            		params: {
            			model: that.model
            		},
            		hide: function() {
            			that.model.fetch({
                    		success: function(ev){
                    			that.render();
        					},
        				});
	        		}
            	});
            	return false;
			},

            ui: {
                reverseButton: '.reverseButton',
                adjudicateButton: '.adjudicateButton',
            },

            // View Event Handlers
            events: {
            	"click @ui.reverseButton": 'reverseTransaction',
            	"click @ui.adjudicateButton": 'adjudicateTransaction',
            }
        });
        return TdrView;
    });
