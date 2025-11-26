define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars',
         'views/config/GenericConfigView', 'views/menuconfig/UssdMenuView',
         'models/UssdMenuModel'],
    function($, App, _, Marionette, Handlebars, 
    		GenericConfigView, UssdMenuView,
    		UssdMenuModel) {
    		
        var showDisregardBonusOption = false;

        var TransfersConfigView = GenericConfigView.extend( {
        	template: 'configuration/Transfers#transfersConfig',
        	dialogTemplate: "configuration/Transfers#transfersConfigModal",
        	
			dialogOnRender: function(element) {
				if (!showDisregardBonusOption) {
					element.find('#disregardTradeBonusCalculationDialogId').remove();
				}
			},
        	
        	url: 'api/config/transfers',
        	
        	regions: {
        		ussdMenuRegion: "#transferUssdMenu",
        		ussdDeduplicationMenuRegion: "#transferDeduplicationUssdMenu"
        	},
        	
        	ui: {
        	    showUpdateDialog: '.showTransfersConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.transferModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.transferBC,
	  				href: "#config-transfers"
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
				
				var AppPropertiesModel = Backbone.Model.extend({
					url: 'api/config/app_properties',
				});
				var appPropertiesModel = new AppPropertiesModel();

            	appPropertiesModel.fetch().then(function() {
					showDisregardBonusOption = appPropertiesModel.attributes.showDisregardBonusOption;
				});
        	},
        	
        	afterRender: function(options) {
        		var that = this;
        		
        		this.$('#transfersConfig .nav-tabs').on('shown.bs.tab', function(ev) {
        			var tabId = $(ev.target).attr('href');
        			that.updateQueryString('tab', tabId);
        			//return false;
        		});
        		
				if (!showDisregardBonusOption) {
        			$('#disregardTradeBonusCalculationId').remove();
				}
        	},
        	
        	renderRegions: function(callback) {
        		var that = this;
        		var menuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu/transfers',
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
        				Console.log('Failed to load USSD menu data');
        			}
        		});
				
				var ddMenuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu/transfersdeduplication',
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
        
        return TransfersConfigView;
});
