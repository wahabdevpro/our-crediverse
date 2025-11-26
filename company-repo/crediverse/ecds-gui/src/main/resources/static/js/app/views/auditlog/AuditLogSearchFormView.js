define( ['jquery', 'App', 'backbone', 'marionette', 'views/auditlog/AuditLogSearchResultView', 
         'models/AuditLogModel', 'utils/CommonUtils', 'datepicker', 'jquery.validate', 'jquery.select2'],
    function($, App, BackBone, Marionette, AuditLogSearchResultView, AuditLogModel, CommonUtils, datepicker, validator) {
        //ItemView provides some default rendering logic
        var AuditLogSearchFormView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
			validator: null,
        	
        	ui: {
                search: '.auditLogSearchButton'
        	},
        	events: {
            	"click @ui.search": 'auditLogSearch',
        	},
        	
			template: "AuditLog#auditlogsearchform",
            
  		  	initialize: function (options) {
  		  		var self = this;
				App.vent.listenTo(App.vent, 'application:auditlogsearcherror', 
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
							violations[ violation.field ] = violation.msgs[0];
						}
						validator.showErrors(violations);
					}, self )
				);
            },
            
            onRender: function () {
  		  		var self = this;
  		  		var baseUrl = '#auditLogSearch';
				this.$('#timestamp-from').datepicker({autoclose: true, todayHighlight: true});
				this.$('#timestamp-to').datepicker({autoclose: true, todayHighlight: true});
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');
			
				setTimeout(function(){
					if (CommonUtils.urlDecodeForm(pcs, form)) {
						self.auditLogSearch({encode: false});
					}
				}, 0);
			
				self.validator = self.$( "#searchform" ).validate({
					rules: {
					},
					messages: {
					},
					submitHandler: function(form) {
						return false;
					},
				});
  		  	}
        });
        return AuditLogSearchFormView;
    });
