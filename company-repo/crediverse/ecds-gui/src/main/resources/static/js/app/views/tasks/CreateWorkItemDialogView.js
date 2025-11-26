define( ['jquery', 'underscore', 'App', 'marionette', 'models/WorkItemModel'],
    function($, _, App, Marionette, WorkItemModel) {
		var i18ntxt = App.i18ntxt.tasks;
        //ItemView provides some default rendering logic
        var CreateWorkItemDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	workItemType: 'transfer',
        	attributes: {
        		class: "modal-content",
        		id: "createWorkItem"
        	},
        	url: 'api/workflow/create',
  		  	template: "Tasks#createWorkItem",
  		  	//model: App.permissions,
  		  	error: null,
  		  	tierList: null,
            initialize: function (options) {
 
            },
            
            configureSelect2: function(element, url) {
        		var ajaxConfig =  {
            			type: "GET",
            		    url: url,
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    /*data: function (params) {
                            return  JSON.stringify({
                                term: params.term
                            });
                        },*/
                        processResults: function (data) {
                        	var mapped = $.map(data, function (item, i) {
				                                return {
				                                    text: item,
				                                    id: i
				                                }
				                            });
                            return {
                                results: mapped
                            };
                        }
            		};
        		
        		
        		
        		element.select2({
        			//dropdownParent: $('.ruledetails'),
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: {
					    id: 'none', // the value of the option
					    text: 'Account to modify'
					  }
            	});
        	},

            onRender: function () {
            	$('#viewDialog').hide();
            	var that = this;
            	$('#viewDialog').off('shown.bs.modal').on('shown.bs.modal', function(ev) {
            		that.configureSelect2($('#destination'), "api/wusers/dropdown");
            		$('#viewDialog').show();
            	});
            },
            
            ui: {
                view: '',
                save: '.workItemCreateButton',
                tasktype: '#taskType'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveItem',
            	"click @ui.tasktype": 'switchtaskType'
            },
            switchtaskType: function() {
            	var taskType = $("#taskType").val();
            	
            	if (taskType === 'transfer') {
            		$('.destination-account').show();
            		$('.destination-bonus').hide();
            	}
            	else if (taskType === 'replenish') {
            		$('.destination-account').hide();
            		$('.destination-bonus').show();
            	}
            	else {
            		App.log('Unknown Task Type - '+taskType);
            	}
            },
            
            preprocessSave: function(data) {
            	//data.maximumAmount = 
        		//data.minimumAmount =
            },
            saveItem: function() {
            	var that = this;
            	var taskType = $("#taskType").val();
            	
    			if (taskType === 'replenish') {
    				this.model.set('rootAccount', true);
				}
    			else {
    				this.model.set('rootAccount', false);
    			}
    			this.model.set('taskType', taskType.toUpperCase());

				var form = $('#createTask');
				this.model.save({
            		success: function(ev){
            			var dialog = that.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		preprocess: function(data) {
            			
            		}
				});
            }
        });
        return CreateWorkItemDialogView;
    });
