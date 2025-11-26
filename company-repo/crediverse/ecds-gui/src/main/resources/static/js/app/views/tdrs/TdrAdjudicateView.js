define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/AdjudicateModel', 'jqauth'],
    function($, _, App, BackBone, Marionette, AdjudicateModel) {
        var TdrAdjudicateView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentTransaction: null,
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "Tdrs#adjudicateView",
        	
        	initialize: function (options) {
        		if (!_.isUndefined(options))
        			this.transaction = options.model;
        	},
        	
        	ui: {
        		view: '',
                adjudicateTransaction: '.adjudicateButton',
                authorize: '.authorizeButton',
                reason: '#reason',
                adjudicateImmediate: '.immediateBtn',
                adjudicateRequest: '.requestBtn'
        	},
        	events: {
        		"click @ui.view": 'view',
            	"click @ui.adjudicateTransaction": 'adjudicateTransaction',
            	"click @ui.adjudicateRequest": 'adjudicateRequest',
            	"click @ui.adjudicateImmediate": 'adjudicateImmediate',
            	"click @ui.authorize": 'startAuthorisation',
            	"click @ui.adjudicate": 'adjudicateButton',
            	"keypress @ui.reason": 'capture'
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
            view: function(ev) {
            },
            
            onRender: function () {
            	var self = this;

				self.$('.config-tip-panel').hide();

				self.$('.adjudicate-buttons button').on('click', function(){
					self.$('.adjudicate-buttons button').removeClass('btn-success btn-danger');
					self.$(this).addClass($(this).data('active-class') + ' active');
            		self.$('.adjudicateButton').removeAttr('disabled');
				});
				
            	//App.log(self.model);
            },
            
            adjudicateTransaction: function(ev) {
            	var self = this;
            	this.startAuthorisation(ev);
            	return false;
            },
            
            startAuthorisation: function(ev) {
            	if (this.model.valid()) {
            		this.$('form :input').attr('disabled', 'disabled');
            		this.$('.adjudicateButton').attr('disabled', 'disabled');
                	try
                	{
                		var self = this;
                		var data = Backbone.Syphon.serialize(this);

						var action = 'F';
						if( self.$('.successBtn').hasClass('active') )
							action = 'S';
                		
                		var updated = {
                        		transactionNumber: this.model.get('number'),
                        		type: this.model.get('type'),
                        		amount: this.model.get('amount'),
                        		//reason: data.reason,
                        		action: action,
                        	};
                    	var model = new AdjudicateModel();
						
                    	if (self.authorisationType === 'request') {
                    		model.set('language', App.contextConfig.languageID);
                    		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
                    		
                    		model.bind(this.$('form'));
                        	model.url = 'api/workflow/adjudicate';
                        	model.save(updated, {
                        		success: function(model, transaction) {
                        			var transId = transaction.uuid;
                        			$('#templates .modal').modal('hide');
                        		}
                        	});
                        	
                    	}
                    	else 
						
						if (self.authorisationType === 'immediate') {
                    		
                    		model.bind(this.$('form'));
                        	model.url = 'api/transactions/adjudicate';
                        	model.save(updated, {
                        		success: function(transaction) {
                        			var controlDiv = self.$(".authorisation");
                        			controlDiv.authController({
            	           		 		coauth: true,
            	           		 		uuid: transaction.attributes.uuid,
            	           		 		authComplete: function(details) {
            	           		 			$('#templates .modal').modal('hide');
            	           		 			App.vent.trigger('application:adjustmentcomplete');
            	           		 		},
            	           		 		errorCallback: function(details, showError, ctxt) {
            	           					try {

            	           						$.proxy(model.defaultErrorHandler(details), model);
            	           						controlDiv.hide();
            	           					} catch(err) {
            	           						App.error(err);
            	           					}
            	           		 		}
            	           		 	});  
                        		}
                        	});
                        	
                    	}
                    	else {
                    		App.log('Unknown type: '+self.authorisationType);
                    	}          		 
    	           	} catch(err) {
    	           		if (console) console.error(err);
    	           	}
            	}
            	
	           	return false;
            },
            
            adjudicateRequest: function(ev) {
            	$('.adjudication-type .requestBtn').addClass('active');
            	$('.adjudication-type .immediateBtn').removeClass('active');
            	//self.$('.adjudicateButton').removeAttr('disabled');
            	//this.configureSelect2($('#authorizedBy'), "api/wusers/dropdown");
            	//$('.authorisation-request').show();
            	//$('.adjudicate-action').hide();
            	$('.adjudication-request').hide();
            	this.authorisationType = 'request';
            	return false;
            },
            
            adjudicateImmediate: function(ev) {
        		$('.adjudication-type .immediateBtn').addClass('active');
            	$('.adjudication-type .requestBtn').removeClass('active');
            	if ($('.adjudicate-buttons .active').length < 1) {
            		self.$('.adjudicateButton').attr('disabled', 'disabled');
            	}
            	$('.adjudicate-action').show();
            	$('.adjudication-request').hide();
            	this.authorisationType = 'immediate';
            	//$("#authorizedBy").select2('destroy'); 
            	return false;
            }

        });
        return TdrAdjudicateView;
    });
