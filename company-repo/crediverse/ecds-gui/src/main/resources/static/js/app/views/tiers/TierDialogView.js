define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        //ItemView provides some default rendering logic
        var TierDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},        	
        	url: 'api/tiers',
  		  	template: "TierRules#edittierdialogview",
            initialize: function (options) {;
            },
            
            onRender: function () {
            	App.log(JSON.stringify(this.model.attributes, null, 2));
            },
            
            ui: {
                view: "",
                save: ".tierCreateButton"
            },
            
            // View Event Handlers
            events: {
            	"click @ui.save": "saveTier"
            },
            
            saveTier: function(ev) {
            	var that = this;
            	this.model.save({
            		success: function(ev){
            			var dialog = that.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		preprocess: function(data) {
            			data.maxTransactionAmount = parseFloat(data.maxTransactionAmount);
            			data.maxDailyCount = parseFloat(data.maxDailyCount);
            			data.maxDailyAmount = parseFloat(data.maxDailyAmount);
            			data.maxMonthlyCount = parseFloat(data.maxMonthlyCount);
            			data.maxMonthlyAmount = parseFloat(data.maxMonthlyAmount);
            			data.buyerDefaultTradeBonusPercentage = parseFloat(data.buyerDefaultTradeBonusPercentage);
					}
				});
            }
        });
        return TierDialogView;
    });