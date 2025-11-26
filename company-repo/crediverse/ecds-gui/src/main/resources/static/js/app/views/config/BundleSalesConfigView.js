define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'utils/CommonUtils',
         'views/config/GenericConfigView', 'views/menuconfig/UssdMenuView',
         'models/UssdMenuModel' ],
    function($, App, _, Marionette, Handlebars, CommonUtils, 
    		GenericConfigView, UssdMenuView,
    		UssdMenuModel) {

        var BundleSalesConfigView = GenericConfigView.extend( {
        	template: 'configuration/BundleSales#bundleSalesConfig',
        	dialogTemplate: "configuration/BundleSales#bundleSalesConfigModal",
        	
        	regions: {
        		ussdMenuRegion: "#bundleSalesUssdMenu",
        		ussdDeduplicationMenuRegion: "#bundleSalesDeduplicationUssdMenu"
        	},
        	
        	url: 'api/config/bundle_sales',
        	
        	ui: {
        	    showUpdateDialog: '.showBundleSalesConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.bundleSalesTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.bundleSalesBC,
	  				href: "#config-nonairtimesales"
	  			};
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
        		
        		this.$('#bundleSalesConfig .nav-tabs').on('shown.bs.tab', function(ev) {
        			var tabId = $(ev.target).attr('href');
        			that.updateQueryString('tab', tabId);
        			//return false;
        		});
        	},
        	
        	renderRegions: function(callback) {
        		var that = this;
        		
        		var menuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu/bundle_sales',
        			showCommandField: false,
        			variableSet: 'recipientCompleteNotification'
        		})
        		.fetch({
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
        			url: '/api/config/ussdmenu/bundle_sales_deduplication',
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
        
        return BundleSalesConfigView;
        
});
