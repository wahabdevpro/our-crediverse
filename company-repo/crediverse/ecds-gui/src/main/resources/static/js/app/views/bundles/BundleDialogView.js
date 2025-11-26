define( ['jquery', 'underscore', 'App', 'marionette', 'utils/CommonUtils', 'datepicker', 'jquery.select2'],
    function($, _, App, Marionette, CommonUtils) {
        //ItemView provides some default rendering logic
	
		var i18ntxt = App.i18ntxt.promotions;
	
        var BundleDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
  		  	template: "ManageBundles#bundleDialog",
  		  	
  		  	ui: {
  		  		view: '',
  		  		save: '.bundleSaveButton'
  		  	},

  		  	events: {
  		  		"click @ui.save": 'saveBundle'
  		  	},
  		  	
  		  	initialize: function() {
  		  		
  		  	},
  		  	
  		  	onRender: function() {
  		  		var self = this;
  		  		
  		  		self.$('#tag').attr('disabled', true);
  		  		
  		  		if (_.isUndefined(this.model.get("id"))) {
	  		  		var control = CommonUtils.configureSelect2Control({
	  		  			jqElement: self.$('#pccBundleSelect'),
	  		  			url: "api/bundles/availbundles",
	  		  			placeholderText: i18ntxt.pccBundleSelectPlaceholder,
	  		  			key: "tag",
	  		  			value: "description"
	  		  		});
	  		  		
	  		  		control.on("change", function(e) {
	  		  			
	  		  			var idSelected = $(this).val();
	  		  			var items = $(this).select2('data'); 
	  		  			var itemData = null;
	  		  			
	  		  			for(var i=0; i<items.length; i++) {
	  		  				var obj = items[i];
	  		  				if (obj.id == idSelected) {
	  		  					itemData = obj.item;
	  		  					break;
	  		  				}
	  		  			}
	  		  			
	  		  			self.$('#tag').val(itemData.tag);
	  		  			self.$('#type').val(itemData.type);
	  		  			self.$('#name').val(itemData.name);
	  		  			self.$('#description').val(itemData.description);
	  		  		});
  		  		} else {
  		  			self.$('.pccBundleSelectDiv').hide();
  		  		}
  		  		
  		  		
  		  		
		  	},
		  	
		  	saveBundle: function() {
            	var self = this;
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
        
        return BundleDialogView;
    }
);