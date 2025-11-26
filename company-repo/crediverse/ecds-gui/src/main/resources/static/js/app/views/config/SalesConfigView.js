define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 
         'views/config/GenericConfigView', 'views/menuconfig/UssdMenuView',
         'models/UssdMenuModel' ],
    function($, App, _, Marionette, Handlebars, 
    		GenericConfigView, UssdMenuView,
    		UssdMenuModel) {

        var SalesConfigView = GenericConfigView.extend( {
        	template: 'configuration/AirtimeSales#salesConfig',
        	dialogTemplate: "configuration/AirtimeSales#salesConfigModal",
        	
        	regions: {
        		ussdMenuRegion: "#airTimeSalesUssdMenu",
        		ussdDeduplicationMenuRegion: "#airTimeSalesDeduplicationUssdMenu"
        	},
        	
        	url: 'api/config/sales',
        	
        	ui: {
        	    showUpdateDialog: '.showSalesConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.salesModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.salesBC,
	  				href: "#config-sales"
	  			};
        	},
        	
        	processBeforeSave: function(data) {
        		if (!_.isUndefined(data.nonDeterministicErrorCodes)) {
        			data.nonDeterministicErrorCodes = data.nonDeterministicErrorCodes.split(',');
        		}
        	},
        	
        	beforeRender: function() {
        		var that = this;
        		var params = that.getQueryDetails();
				if (!_.isEmpty(params)) {
					that.$("a[href='#"+params.tab+"']").tab('show');
				}
        	},
        	
        	afterRender: function(options) {
        		var that = this;
        		
        		this.$('#salesConfig .nav-tabs').on('shown.bs.tab', function(ev) {
        			var tabId = $(ev.target).attr('href');
        			that.updateQueryString('tab', tabId);
        			//return false;
        		});
        	},
        	
        	renderRegions: function(callback) {
        		var that = this;
        		var menuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu/airtimesales',
        			showCommandField: false,
        			variableSet: 'recipientNotification'
        		}).fetch({
        			success: function(mdl) {
        				var options = $.extend({
    						model: mdl,
    						prompt: false,
    						save: function(mdl){
    							mdl.save();
    						}
    					}, that.options)
    					var view = new UssdMenuView(options);
        				that.ussdMenuRegion.show(view);
        				callback();
        			},
        			error: function(ev) {
        				App.log('Failed to load USSD menu data');
        			}
        		});
        		
				var ddMenuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu/airtimesalesdeduplication',
        			showCommandField: false,
        			variableSet: 'recipientNotification'
        		}).fetch({
        			success: function(mdl) {
        				var options = $.extend({
    						model: mdl,
    						prompt: false,
    						save: function(mdl){
    							mdl.save();
    						}
    					}, that.options)
    					var view = new UssdMenuView(options);
        				that.ussdDeduplicationMenuRegion.show(view);
        				callback();
        			},
        			error: function(ev) {
        				App.log('Failed to load deduplication USSD menu data');
        			}
        		});
        	}
        	
        });
        
        return SalesConfigView;
        
});
