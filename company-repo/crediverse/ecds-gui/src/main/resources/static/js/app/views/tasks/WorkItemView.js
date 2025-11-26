define( ['jquery', 'App', 'backbone', 'marionette', 'utils/CommonUtils', 'models/WorkItemModel'],
    function($, App, BackBone, Marionette, CommonUtils, WorkItemModel) {
		var i18ntxt = App.i18ntxt.tasks;
		var i18n = CommonUtils.i18nLookup('tasks');
	
        var WorkItemView =  Marionette.ItemView.extend( {
        	tagName: 'div',
			baseUrl: 'api/batch/history/csv/',
			template: "Tasks#taskItemView",
			currentFilter: {}, // Used to keep track of filter settings for use by export.
        	attributes: {
        		class: "row"
        	},
        	dataType: null,
        	itemAction: null,
        	taskData: null,
        	tableConfig: null,
        	
        	configureDataTable: function(config) {
        		var that = this;
        		var newurl = that.baseUrl+config.row.batchID;
        		//$.extend({}, this.tableConfig, options);
        		$.extend(that.tableConfig, config, {
        			columns: config.data,
        			ajax: function(data, callback, settings) {
          				that.currentFilter.url = newurl;
          				that.currentFilter.data = data;
          				var jqxhr = $.ajax(newurl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
//						  console.dir( dataResponse);
                      	    callback(dataResponse);
                      	  //$('.csvtableview').DataTable().draw();
							//table.DataTable().columns.adjust().responsive.recalc();
                      	  })
                      	  .fail(function(dataResponse) {
                      			self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:accountsterror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      }
        		});
        	},
        	
        	initialize: function (options) {
        		var that = this;
        		that.taskData = options;
        		that.dataType = options.type;
        		that.itemAction = options.action;
        		that.tableConfig = {
        			autoWidth: false,
    				responsive: true,
    				searching: false,
    				lengthChange: false,
    				serverSide: true,
    				autoWidth: false,
    				responsive: true,
    				language: {
    	                url: "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
    					searchPlaceholder: "Whatever ...",
    	            }
          			
            	}
        		
        		switch(that.dataType) {
	            	case 'BATCHUPLOAD':
	            		that.configureDataTable(options);
	            		break;
	        		default:
	        			
	        			break;
	        	}
        	},
        	
        	hideAll: function() {
        		this.$('.tasktableview').hide();
        		this.$('.taskdetailview').hide();
        		this.$('.descriptionField').hide();
        		this.$('.reasonField').hide();
        		this.$('.amountField').hide();
        		this.$('.bonusField').hide();
        		this.$('.updateReasonField').hide();
        		this.$('.formButtonSection').show();
        		this.$('.otpButtonSection').hide();
        		this.$('.workItemOTPField').hide();
        	},
        	
        	configureView: function(type) {
        		this.hideAll();
        		if (this.options.requiresReason) {
        			this.$('.updateReasonField').show();
        		}
        		
        		switch(type) {
        		case 'BATCHUPLOAD':
        			this.$('.tasktableview').show();
        			break;
        		case 'ADJUSTMENT':
        			this.$('.taskdetailview').show();
        			this.$('.descriptionField').show();
        			this.$('.amountField').show();
        			this.$('.reasonField').show();
        			break;
        		case 'TRANSFER':
        			this.$('.taskdetailview').show();
        			this.$('.descriptionField').show();
        			this.$('.amountField').show();
        			break;
        		case 'REPLENISH':
        			this.$('.taskdetailview').show();
        			this.$('.descriptionField').show();
        			this.$('.amountField').show();
        			this.$('.bonusField').show();
        			break;
        		case 'ADJUDICATION':
        			this.$('.taskdetailview').show();
        			this.$('.descriptionField').show();
        			this.$('.amountField').show();
        			break;
        		case 'REVERSAL':
        		case 'PARTIALREVERSAL':
        			this.$('.taskdetailview').show();
        			this.$('.descriptionField').show();
        			this.$('.amountField').show();
        			this.$('.reasonField').show();
        			//this.$('.bonusField').show();
        			break;
        		default:
        			break;
        		}
        	},
        	
        	onRender: function () {
            	var that = this;
            	that.model = new WorkItemModel();
            	if (this.options.requiresReason) {
            		var form = that.$('form');
            		this.model.bind(form);
            	}

            	var taskTable = that.$('.tasktableview');
            	var taskDetail = that.$('.taskdetailview');
            	that.configureView(that.dataType);
            	
            	that.$('#taskItemView #itemType').html(i18ntxt[that.dataType]);
            	that.$('#taskItemView #itemAction').html(i18ntxt[that.itemAction]);
            	
            	switch(that.dataType) {
	            	case 'BATCHUPLOAD':
	                	this.dataTable = taskTable.DataTable(that.tableConfig);
	            		break;
	            	case 'ADJUSTMENT':
	            		that.$('#description').html(that.taskData.row.description);
	            		that.$('#reason').html(that.taskData.data.adjustment.reason);
	            		that.$('#amount').html(that.taskData.data.adjustment.amount);
	            		break;
	            	case 'TRANSFER':
	            		that.$('#description').html(that.taskData.row.description);
	            		that.$('#amount').html(that.taskData.data.transfer.amount);
	            		break;
	            	case 'REPLENISH':
	            		that.$('#description').html(that.taskData.row.description);
	            		that.$('#reason').html(that.taskData.data.replenish.reason);
	            		that.$('#amount').html(that.taskData.data.replenish.amount);
	            		that.$('#bonus').html(that.taskData.data.replenish.bonusProvision);
	            		break;
	            	case 'ADJUDICATION':
	            		that.$('#description').html(that.taskData.row.description);
	            		that.$('#amount').html(that.taskData.data.adjudication.amount);
	            		break;
	            	case 'REVERSAL':
	        		case 'PARTIALREVERSAL':
	        			that.$('#description').html(that.taskData.row.description);
	            		that.$('#reason').html(that.taskData.data.reversal.reason);
	            		that.$('#amount').html(that.taskData.data.reversal.amount);
	        			break;
	        		default:
	        			taskDetail.show();
	        			taskDetail.html('<pre>'+JSON.stringify(that.taskData, null, 2)+'</pre>');
	        			//this.reviewtransaction(inboxTable, tableData);
	        			break;
	        	}
            },

            ui: {
            	workItem: '',
            	processTransaction:'.previewOKButton',
            	processAuthorisation:'.authorizeButton',
            	processOtpAuthorisation: '.approveOTPButton',
            	updateReason: '#updateReason',
            	workItemOTP: '#workItemOTP',
            	resendpin: '.workItemOTPField .resendPIN'
            },

            // View Event Handlers
            events: {
            	"click @ui.workItem": 'viewWorkItem',
            	"click @ui.resendpin": 'requestOTP',
            	"click @ui.processTransaction": 'processTransaction',
            	"click @ui.processAuthorisation": 'processAuthorisation',
            	"click @ui.processOtpAuthorisation": 'processOtpAuthorisation',
            	"keypress @ui.updateReason": 'capture',
            	"keyup @ui.workItemOTP": 'captureOTP'
            },
            
            capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.processAction();
                }
        	},
        	
        	captureOTP: function(ev) {
        		if (ev.keyCode == 13) {
        			this.processOtpAuthorisation();
                }
        		else {
        			var value = $('#templates #workItemOTP').val();
        			if (_.isEmpty(value) || value.length < 3) {
        				if (!$('#templates .approveOTPButton').hasClass('disabled'))
        					$('#templates .approveOTPButton').addClass('disabled');
        			}
        			else {
        				$('#templates .approveOTPButton').removeClass('disabled');
        			}
        		}
        	},
        	
        	requestOTP: function() {
        			$('#templates #workItemOTP').empty();
        			$('#templates .workItemOTPField').show();
            		$('#templates .formButtonSection').hide();
            		$('#templates .approveOTPButton').addClass('disabled');
            		$('#templates .otpButtonSection').show();
            		var jqxhr = $.post('api/workflow/sendotp/'+this.options.row.uuid, function(data) {
            			//alert("success"+ JSON.stringify(data, null, 2));
            		})
            		  .done(function() {
            		    //alert( "second success" );
            		  })
            		  .fail(function(data) {
            		    //alert( "error"+ JSON.stringify(data, null, 2));
            		  })
            		  .always(function() {
            		   // alert( "finished" );
            		  });
            		return false;
        	},
        	
        	updateWorkflow: function() {
        		var that = this;
        		var model = new WorkItemModel(this.options.row);
        		if (this.options.requiresReason) {
        			model.set('reason', $('#updateReason').val());
        		}
        		model.save({
            		action: this.options.action
            	},
            	{
            		success: function(data){
            			$('#templates .modal').modal('hide');
            		},
            		preprocess: function(data) {
            			
            		},
            		error: function(model, response) {
            			var errorMessage = App.i18ntxt.config.errorMessages[response.responseJSON.status];
            			if (_.isUndefined(errorMessage)) {
            				errorMessage = App.i18ntxt.config.errorMessages[response.responseJSON.message];
            				if (_.isUndefined(errorMessage)) {
            					errorMessage = response.responseJSON.message;
            				}
            			}
            			App.vent.trigger('application:dialog', {
            				title: i18ntxt[model.attributes.workType] + ' '+i18ntxt.wordOf+' ' + i18ntxt[model.attributes.requestType]+' '+i18ntxt.errorDialogTitleEnd,
            				//text: action+" :: "+JSON.stringify(response, null, 2)+"<br><br>"+JSON.stringify(model, null, 2),
        	        		text: errorMessage+' '+i18ntxt.wordFor+'<br>'+model.attributes.description,
        	        		name: "errorDialog"
                		});
            			that.updateTimeouts(false);
            		}
				});

        	},
            
        	processTransaction: function() {
            	var canContinue = false;
            	if (this.options.requiresReason) {
            		if (this.model.valid()) {
            			canContinue = true;
                	}
            	}
            	else {
            		canContinue = true;
            		
            	}
            	if (canContinue) {
            		if (this.options.action === 'APPROVE') {
            			this.requestOTP();
            		}
            		else {
            			this.updateWorkflow();
            		}
            	}
            	return false;
            },
            
            processAuthorisation: function() {
            	/*if (!_.isUndefined(this.options.callback) && _.isFunction(this.options.callback)) {
            		this.options.row.reason = $('#updateReason').val();
            		this.options.callback(this.options.row); // process action
            	}
            	$('#templates .modal').modal('hide');*/
            },
            
            processOtpAuthorisation: function() {
            	var enteredOtp = $('#workItemOTP').val();
            	var that = this;
        		var model = new WorkItemModel(this.options.row);
        		
        		model.set('workItemOTP', enteredOtp);
        		model.save({
            		action: this.options.action
            	},
            	{
            		success: function(data){
            			$('#templates .modal').modal('hide');
            		},
            		preprocess: function(data) {
            			
            		},
            		error: function(model, response) {
            			var errorMessage = App.i18ntxt.config.errorMessages[response.responseJSON.status];
            			if (_.isUndefined(errorMessage)) {
            				errorMessage = App.i18ntxt.config.errorMessages[response.responseJSON.message];
            				if (_.isUndefined(errorMessage)) {
            					errorMessage = response.responseJSON.message;
            				}
            			}
            			//that.$('.errorText').html(i18n.translate(errorMessage));
            			that.$('.errorText').html(errorMessage);
            			that.$('.errorField').show();
            			that.updateTimeouts(false);
            		}
				});
            }
        });
        return WorkItemView;
    });
