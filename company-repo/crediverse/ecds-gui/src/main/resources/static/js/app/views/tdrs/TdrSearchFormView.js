define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'views/tdrs/TdrSearchResultView', 'models/TdrModel', 
         'collections/TierCollection', 'utils/CommonUtils', 'datepicker', 'jquery.validate', 'jquery.select2'],
    function($, _, App, BackBone, Marionette, TdrSearchResultView, TdrModel, TierCollection, CommonUtils, datepicker, validator) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.transactions;
        var TdrSearchFormView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
			validator: null,

			model: null,
        	
        	ui: {
                search: '.tdrSearchButton'
        	},
        	events: {
            	"click @ui.search": 'tdrSearch',
        	},
        	
			template: "Tdrs#tdrssearchform",
            
  		  	initialize: function (options) {
  		  		var self = this;

				self.model= new Backbone.Model();

				App.vent.listenTo(App.vent, 'application:tdrssearcherror', 
					$.proxy( function(err){
						//App.log( '*** handling search error: status: ' + err.responseJSON.status );
						var generalMsg = err.responseJSON.message;
						if ( err.responseJSON.status == 'NOT_ACCEPTABLE' )
							generalMsg = 'Search failed! Please check your search criteria and try again.';
						this.$('#error_general_message').html(generalMsg).show();

						var validator = this.$( "#searchform" ).validate();
						var violations = {};
						for (i = 0; i < err.responseJSON.violations.length; i++) 
						{
							var violation = err.responseJSON.violations[ i ];
							if (!_.isUndefined(violation.field)) {
								violations[ violation.field ] = violation.msgs[0];
							}
						}
						validator.showErrors(violations);
					}, self )
				);
            },
            
            onRender: function () {
  		  		var self = this;
				this.$('#date-from').datepicker({autoclose: true, todayHighlight: true});
				this.$('#date-to').datepicker({autoclose: true, todayHighlight: true});
				var baseUrl = '#transactionSearch';
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');

				var groupIDA = this.$('#group-id-a');
				var ajaxConfig = {
            			type: "GET",
            		    url: "api/groups/dropdown",
            		    dataType: 'json',
            		    delay: 250,
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: i
                                    }
                                })
                            };
                        }
            		};
				groupIDA.data('config', ajaxConfig);
            	groupIDA.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.selectGroupHintA,
            	});
				
				var groupIDB = this.$('#group-id-b');
				groupIDB.data('config', ajaxConfig);
            	groupIDB.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.selectGroupHintB,
            	});
            	
            	setTimeout(function(){
					if (CommonUtils.urlDecodeForm(pcs, form)) {
						self.tdrSearch({encode: false});
					}
				}, 0);
			
				self.validator = self.$( "#searchform" ).validate({
					rules: {
						amountFrom: {	
							digits: true,
						},	
						amountTo: {	
							digits: true,
						},	
						bonusAmountFrom: {	
							digits: true,
						},	
						bonusAmountTo: {	
							digits: true,
						},	
						chargeAmountFrom: {	
							digits: true,
						},	
						chargeAmountTo: {	
							digits: true,
						},	
					},
					messages: {
						amountFrom: {
							digits: 'Must be a numeric value',
						},
						amountTo: {
							digits: 'Must be a numeric value',
						},
						bonusAmountFrom: {	
							digits: 'Must be a numeric value',
						},	
						bonusAmountTo: {	
							digits: 'Must be a numeric value',
						},	
						chargeAmountFrom: {	
							digits: 'Must be a numeric value',
						},	
						chargeAmountTo: {	
							digits: 'Must be a numeric value',
						},	
					},
					submitHandler: function(form) {
						return false;
					},
				});
  		  	}
        });
        return TdrSearchFormView;
    });
