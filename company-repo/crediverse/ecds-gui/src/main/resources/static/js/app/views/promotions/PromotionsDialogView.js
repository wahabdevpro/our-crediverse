define( ['jquery', 'underscore', 'App', 'marionette', 'utils/CommonUtils', 'datepicker', 'jquery.select2'],
    function($, _, App, Marionette, CommonUtils) {
        //ItemView provides some default rendering logic
	
		var i18ntxt = App.i18ntxt.promotions;
	
        var PromotionsDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
  		  	template: "ManagePromotions#promotionsDialog",
  		  	
  		  	ui: {
  		  		view: '',
  		  		save: '.promoSaveButton',
  		  		transferRuleID: '#transferRuleID',
  		  		amountCheckbox: '#rewardTypeAmount',
  		  		percentageCheckbox: '#rewardTypePercentage'
  		  	},

  		  	events: {
  		  		"click @ui.save": 'savePromotion',
  		  		"change @ui.transferRuleID": 'onChangeTransferRuleID',
  		  		"change @ui.amountCheckbox": 'amountChecked',
  		  		"change @ui.percentageCheckbox": 'percentageChecked'
  		  	},
  		  	
  		  	initialize: function() {
  		  	},
  		  	
  		  	onRender: function() {
  		  		var self = this;

  		  		this.$('#startDateString').datepicker({
  		  			format: 'yyyy-mm-dd', autoclose: true, todayHighlight: true, startDate: new Date()
  		  		});

  		  		this.$('#endDateString').datepicker({
  		  			format: 'yyyy-mm-dd', autoclose: true, todayHighlight: true, startDate: new Date()
  		  		});

  		  		var transferRuleElement = this.$('#transferRuleID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: transferRuleElement,
  		  			url: "api/transfer_rules/dropdown",
  		  			placeholderText: i18ntxt.transferRulePlaceholder
  		  			
  		  		});
  		  		
  		  		var serviceClassElement = this.$('#serviceClassID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: serviceClassElement,
  		  			url: "api/serviceclass/dropdown",
  		  			placeholderText: i18ntxt.serviceClassPlaceHolder
  		  		});
  		  		
  		  		var areaElement = this.$('#areaID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: areaElement,
  		  			url: "api/areas/dropdown",
  		  			placeholderText: i18ntxt.areaPlaceHolder,
					isHtml: true
  		  		});
  		  		
  		  		var bundleElement = this.$('#bundleID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: bundleElement,
  		  			url: "api/bundles/dropdown",
  		  			placeholderText: i18ntxt.bundlePlaceHolder
  		  		});


				let dialog = document.querySelector("#viewDialog .modal-dialog");
				if(dialog) {
					dialog.style.width = "80%";
				}

  		  		if (!this.$("#rewardTypeAmount").prop('checked')) {
  		  			this.$("#rewardAmount").prop("disabled", true);
  		  		} else {
  		  			this.$("#rewardAmount").prop("disabled", false);
  		  		}
  		  		
  		  		if (!this.$("#rewardTypePercentage").prop('checked')) {
  		  			this.$("#guiRewardPercentage").prop("disabled", true);
  		  		} else {
  		  			this.$("#guiRewardPercentage").prop("disabled", false);
  		  		}
		  	},
		  	
		  	amountChecked: function(e) {
	  			if ($(e.currentTarget).is(':checked')) {
					self.$("#rewardAmount").attr("disabled", false);
				} else {
					self.$("#rewardAmount").attr("disabled", true);
				}
		  	},
		  	
		  	percentageChecked: function(e) {
		  			if ($(e.currentTarget).is(':checked')) {
  		  				self.$("#guiRewardPercentage").attr("disabled", false);
  		  			} else {
  		  				self.$("#guiRewardPercentage").attr("disabled", true);
  		  			}
		  	},
		  	
		  	onChangeTransferRuleID: function() {
		  		var transferRule = $("#transferRuleID").val();
		  		if( transferRule == null){
		  			this.$("#areaID").attr("disabled", true);
		  			this.selectedAreaID = this.$("#areaID").val();
		  			this.$("#areaID").val("").trigger('change');;
		  		} else {
		  			this.$("#areaID").attr("disabled", false);
		  			this.$("#areaID").val(this.selectedAreaID).trigger('change');;
		  		}
		  		
		  	},
		  	
		  	savePromotion: function() {
            	var self = this;

            	if (!this.$("#rewardTypeAmount").prop('checked')) {
            		this.$("#rewardAmount").val("0");
            	}
            	
            	if (!this.$("#rewardTypePercentage").prop('checked')) {
            		this.$("#guiRewardPercentage").val("0");
            	}
		  		
            	this.model.save({
            		success: function(ev){
            			var dialog = self.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		error: function(ev){
            		}
				});
		  	}
        });
        
        return PromotionsDialogView;
    }
);