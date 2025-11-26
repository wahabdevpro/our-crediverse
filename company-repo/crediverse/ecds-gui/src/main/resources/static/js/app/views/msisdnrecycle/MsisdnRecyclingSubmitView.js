define( ['jquery', 'underscore', 'backbone', 'App', 'marionette', 'models/CoAuthModel', 'models/WorkItemImportModel', 'utils/CommonUtils', 'utils/HandlebarHelpers', 'datatables', 'file-upload'],
    function($, _, BackBone, App, Marionette, CoAuthModel, WorkItemImportModel, CommonUtils, HBHelper) {
	
		var i18n = CommonUtils.i18nLookup('batch');
		
        var ManageBatchProcessingView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "MsisdnRecyclingUpload#manageupload",
//  		  	url: 'api/groups',
  		  	error: null,
  		  	timer: null,

        	coAuth: null,
        	
        	breadcrumb: function() {
  		  		return {
  		  			heading: i18n.translate('heading'),
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: i18n.translate('sectionBC'),
						iclass: "glyphicon glyphicon-import"
  		  			}, {
  		  				text: i18n.translate('pageBC'),
  		  				href: "#batch"
  		  			}]
  		  		}
  		  	},
            
            issues: null,
            feedback: false,
            overview: true,
            displayBatchStatus: function(progressbar, status, name) {
            	var output = $('#'+name);
            	var linesProcessed = (_.isUndefined(status))?0:status.lineCount;
            	if (_.isFunction(progressbar.html)) progressbar.html((linesProcessed - 1) + ' '+i18n.translate('progressBarSuffix'));
            	//output.html(JSON.stringify(status, null, 2));
            },
            displayOutcome: function(data, outcomeType) {
            	var that = this;
            	var table = this.$('.issuetable');
            	
            	this.$('#outcomeStatus').hide();
            	this.$('#outcomeIssues').hide();
            	this.$('#outcomeErrors').hide();
            	this.$('#outcomeMessage').hide();
            	
            	$('#importWizard').wizard('selectedItem', {
					step: 6
				});
            	if (!_.isUndefined(data.filename)) {
            		$('#outcomepane .filename ').html(data.filename);
            	}
            	else if (!_.isUndefined(data.file)) {
            		$('#outcomepane .filename ').html(data.file.name);
            	}
            	else if (!_.isUndefined(data.localFilename)) {
            		$('#outcomepane .filename ').html(data.localFilename);
            	}
            	else if (!_.isUndefined(data.batchStatus) && !_.isUndefined(data.batchStatus.filename)) {
            		$('#outcomepane .filename ').html(data.batchStatus.filename);
            	}
            	else {
            		$('#outcomepane .filename ').html('Uknown File');
            	}
            	
            	if (!_.isUndefined(data.responseJSON) && data.responseJSON.status == 'FORBIDDEN') {
            		outcomeType = 'FORBIDDEN';
            	}
            	
            	if (outcomeType === 'FORBIDDEN') {
            		$('#outcomepane .status ').html(' '+i18n.translate('outcomeStatusErrorPrefix'));
            		$('#outcomepane .box-solid').removeClass('box-success');
            		$('#outcomepane .box-solid').addClass('box-danger');
        			this.$('#outcomeErrors').html(i18n.translate('batchForbidden'));
        			this.$('#outcomeErrors').show();
            	}
            	else if (outcomeType === 'ERROR' && !_.isUndefined(data) && _.isUndefined(data.issues)) {
            		$('#outcomepane .status ').html(' '+i18n.translate('outcomeStatusErrorPrefix'));
            		$('#outcomepane .box-solid').removeClass('box-success');
            		$('#outcomepane .box-solid').addClass('box-danger');
            		this.$('#outcomeErrors').html(i18n.translate(data.message));
        			this.$('#outcomeErrors').show();
            	}
            	else if (!_.isUndefined(data) && (!_.isUndefined(data.issues) || !_.isUndefined(data.error))) {
            		$('#outcomepane .box-solid').removeClass('box-success');
            		$('#outcomepane .box-solid').addClass('box-danger');

            		if (_.isUndefined(data.error)) {
	            		$('#outcomepane .status ').html(' '+i18n.translate('outcomeStatusErrorPrefix'));
	            		
	            		
	            		this.$('#outcomeIssues').show();
	            		this.dataTable = table.DataTable( {
	            			"autoWidth": false,
	    					"responsive": true,
	    					"language": {
	    		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
	    		            },
	            			data: data.issues,
	                		destroy: true,
	                          "columns": [
	                        	   {
	                        		   data: "lineNumber",
	                        		   title: i18n.translate('lineNumberTableHeading')
	                        	   },
	                        	   {
	                        		   data: "returnCode",
	                        		   title: i18n.translate('returnCodeTableHeading'),
	                        		   render: function(data, type, row, meta) {
	                        			   return that.decodeBatchIssue(row);
	                        		   }
	                        	   },
	                        	   {
	                        		   data: "additionalInformation",
	                        		   title: i18n.translate('additionalInformationTableHeading'),
	                        		   render: function(data, type, row, meta) {
	                        			   var text = '';
	                        			   var item = row.property;
	                        			   if (!_.isUndefined(item) && _.isString(item)) {
	                        				   text = item;
	                        			   }
	                        			   return text;
	                        		   }
	                        	   }
	                        	  ]
	                      } );
            		}
            		else {
            			this.$('#outcomeErrors').html(i18n.translate(data.error));
            			this.$('#outcomeErrors').show();
            		}
            	}
            	else {
            		$('#outcomepane .box-solid').removeClass('box-danger');
            		$('#outcomepane .box-solid').addClass('box-success');
            		$('#outcomepane .status ').html(' '+i18n.translate('outcomeStatusSuccess'));
            		
            		if (!_.isUndefined(data) && !_.isUndefined(data.batchStatus)) {
                		var status = data.batchStatus;
                		$('#outcomeStatus').show();
                		$('#outcomepane .filename ').html(status.filename);
                		$('#outcomepane .recordtype ').html(status.type);
                		$('#outcomepane .recordcount ').html(status.lineCount - 1);
                		$('#outcomepane .recordinserts ').html(status.insertCount);
                		$('#outcomepane .recordupdates ').html(status.updateCount);
                		$('#outcomepane .recorddeletes ').html(status.deleteCount);
                		//$('#outcomeJson').html(JSON.stringify(status, null, 2));
                		//that.displayBatchStatus(data.batchStatus, 'outcomeStatus');
                	}
            		else {
            			$('#outcomeMessage').html("<h3>File "+data.localFilename+" submitted for authorization</h3>");
            			$('#outcomeMessage').show();
            		}
            	}
            	
            	
            	/*
            	 * {
            	 * 		"language":"en",
            	 * 		"seperators":{
            	 * 			"decimal":".",
            	 * 			"group":",",
            	 * 			"money":"."
            	 * 		},
            	 * 		"uuid":"e5b2337f-f5c0-4f6b-b1fc-3984bd2dcfe0",
            	 * 		"bufferSize":0,
            	 * 		"progress":392,
            	 * 		"localFilename":"ci_ecds_adjust_20170217_021841.csv",
            	 * 		"size":402,
            	 * 		"uuid":"e5b2337f-f5c0-4f6b-b1fc-3984bd2dcfe0",
            	 * 		"status":"QUEUED",
            	 * 		"batchID":217,
            	 * 		"responseCode":"SUCCESS",
            	 * 		"characterOffset":392
            	 * }
            	 * 
            	 */
            },
            
            decodeBatchIssue: function(issue) {
            	if (!_.isUndefined(issue) && !_.isUndefined(issue.returnCode)) {
            		if (!_.isUndefined(issue.additionalInformation) && issue.additionalInformation != null) {
            			if (issue.returnCode == "CANNOT_ADD" && (issue.additionalInformation.indexOf("Overlaps with Promotion") >= 0)) {
            				var faultId = issue.additionalInformation.substring(23);
            				return CommonUtils.renderHtml(i18n.translate("PROMOTION_OVERLAP"), {id: faultId});
            			}
            		}
            	}
            	return i18n.translate(issue.returnCode);
            },
            
            processUploadedBatch: function(uuid, coauth, file, isRequest) {
            	var that = this;
            	$('#importWizard').wizard('selectedItem', {
					step: 5
				});
            	
            	if (coauth) {
            		$.ajax({
                	    url: "import/status/"+uuid,
                	    success: function(status) {
                	    	that.displayOutcome(status, status.status);
                	    },
                	    error: function(data) {
                	    	that.displayOutcome(data, 'ERROR');
                	    }
        	    	});
            	}
            	else {
            		$.ajax({
                	    url: "/import/process/"+uuid,
                	    method: 'POST',
                	    success: function(data) {
                	    	$.ajax({
                        	    url: "import/status/"+uuid,
                        	    success: function(status) {
                        	    	that.displayOutcome(status, data.status);
                        	    },
                        	    error: function(data) {
                        	    	that.displayOutcome(data, 'ERROR');
                        	    }
                	    	});
                	    },
                	    error: function(data) {
                	    	that.displayOutcome(data, 'ERROR');
                	    }
                	});
            	}
            },
            verifyBatch: function(uuid, coauth, file) {
            	$('#importWizard').wizard('selectedItem', {
					step: 3
				});
            	var that = this;
            	var progressbar = $('#verifyProgress .progress-bar');
            	$.ajax({
            	    url: "import/status/"+uuid,
            	    success: function(data) {
            	    	var batchStatus = data.batchStatus;
            	    	var filename = 'Unknown';
            	    	if (_.isUndefined(batchStatus)) {
            	    		filename = file.name;
            	    	}
            	    	else {
            	    		filename = batchStatus.filename;
            	    	}
            	    	$('#breadcrumb H1').html(i18n.translate('heading') + ' - '+filename);
            	    	if (_.isUndefined(data.filename)) {
            	    		data.filename = filename;
            	    	}
            	    	progressbar.css(
                            'width',
                            data.progress + '%'
                        );
            	    	if (data.status === 'ERROR') {
            	    		progressbar.closest('.modal').modal('hide');
            	    		/*App.vent.trigger('application:dialog', {
            	        		text: JSON.stringify(data, null, 2),
            	        		name: "okDialog"
                    		});*/
            	    		that.displayOutcome(data, data.status);
            	    	}
            	    	else if (data.status === 'VERIFIED' || data.status === 'PENDING_AUTHORIZATION') {
            	    		that.displayBatchStatus(progressbar, data.batchStatus, 'verifyStatus');
            	    		that.timer = setTimeout($.proxy(that.performCoauthorisation, that, uuid, coauth, file), 1000);
            	    	}
            	    	else if (data.status !== 'COMPLETE') {
            	    		that.displayBatchStatus(progressbar, data.batchStatus, 'verifyStatus');
            	    		that.timer = setTimeout($.proxy(that.verifyBatch, that, uuid, coauth, file), 1000);
            	    	}
            	    	else {
            	    		// Needed to allow progress animation to complete.
            	    		setTimeout(function(){
            	    			//progressbar.closest('.modal').modal('hide');
            	    			//that.processUploadedBatch(data, data.status);
            	    			//$.proxy(that.processUploadedBatch, that, uuid, coauth, file)
            	    			App.log('Next step, status = '+data.status);
            	    		}, 0);
            	    	}
            	    	
            	    },
            	    error: function(data) {
            	    	//App.error('error::'+JSON.stringify(data, null, 2));
            	    	if (!_.isUndefined(data.responseJSON)) {
            	    		that.displayOutcome(data.responseJSON, 'ERROR');
            	    	}
            	    	else {
            	    		that.displayOutcome(data, 'ERROR');
            	    	}
            	    }
            	  });
            },
            resetImport: function() {
            	if (this.coAuth != null) {
            		var parent = this.$("#coauthorisation").parent();
            		this.coAuth.detach();
            		parent.append("<div id='coauthorisation' class='coauthorisation tab-pane fade in active'/>");   		
            	}
            	if (this.timer !== null) {
            		clearTimeout(this.timer);
            		this.timer = null;
            	}
            	
            	$('#tablist a:first').tab('show');
    	    	$('#breadcrumb H1').html(i18n.translate('heading'));
            	
            	$('#importWizard').wizard('selectedItem', {
					step: 1
				});
            	$('#verifyProgress .progress-bar').css(
                        'width',
                       '0%'
                    );
            	$('#uploadProgress .progress-bar').css(
                        'width',
                       '0%'
                    );
            },
            createAuthorisationRequest: function(uuid, coauth, file, isRequest) {
            	var that = this;
            	
            	//var model = new CoAuthModel();
            	var model = new BackBone.Model();
            	model.set('language', App.contextConfig.languageID);
        		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
        		model.set('uuid', uuid);
        		
        		var myReason = that.$('#reason');
        		model.set('reason', myReason.val());
        		
        		model.url = 'api/workflow/batch';
        		
        		model.save({}, {
            		success: function(model, transaction) {
            			that.displayOutcome(model.attributes, 'SUCCESS');
            		},
            	    error: function(data) {
            	    	//App.error('error::'+JSON.stringify(data, null, 2));
            	    	if (!_.isUndefined(data.responseJSON)) {
            	    		that.displayOutcome(data.responseJSON, 'ERROR');
            	    	}
            	    	else {
            	    		that.displayOutcome(data, 'ERROR');
            	    	}
            	    }
            	});
            },
            performCoauthorisation: function(uuid, coauth, file) {
            	var that = this;
            	var status = null;

        		if (coauth) {
        			that.$('#reason').val('');
        			$('#importWizard').wizard('selectedItem', {
    					step: 4
    				});
        			var authForm = that.$('#batchCoauthForm');
        			var authModel = new WorkItemImportModel();
        			if (authForm.length > 0)
        				authModel.bind(authForm);
        			
        			var authButton = $('#coauthorisationRequestButton');
        			if (authButton.length > 0) {
        				authButton.off().on('click', function(ev) {
            				if (authModel.valid()) {
            					that.createAuthorisationRequest(uuid, coauth, file, true);
                				$('#authorisationTabs a[href="#coauthorisation"]').tab('show');
                				$('#coauthorisation').empty();
                				return false;
            				}
            				else {
            					return false;
            				}
            			});
        			}

            		this.coAuth = that.$("#coauthorisation");
            		this.coAuth.authController({
           		 		coauth: coauth,
           		 		uuid: uuid,
           		 		authComplete: function(details) {
	           		 		$('#importWizard').wizard('selectedItem', {
	    						step: 5
	    					});
	           		 		//that.displayOutcome(details, 'SUCCESS');
	            			that.processUploadedBatch(uuid, coauth, file, false);
           		 		},
           		 		errorCallback: function(details, showError, ctxt) {
           		 			showError(details.message+' ( ref. '+details.correlationId+')', ctxt);
           		 			return true;
           		 		}
           		 	});
            	}
            	else {
        			$('#importWizard').wizard('selectedItem', {
						step: 5
					});
        			this.processUploadedBatch(uuid, coauth, file, false);
            	}
            },
            onRender: function () {
            	var token = $("meta[name='_csrf']").attr("content");
        		var header = $("meta[name='_csrf_header']").attr("content");
            	var importb = this.$(".file-import");
            	var customeHeaders = {};
            	customeHeaders[header] = token;
            	var that = this;
            	var filename = null;
            	var progress = 0;
            	
    	    	$('#breadcrumb H1').html(i18n.translate('heading'));
            	
            	var wizard = this.$('#manageupload');
            	wizard.on('stepclicked.fu.wizard', function(event, data) {
            		if (data.step !== 1) {
            			event.preventDefault(); 
            		}
            		else {
            			that.resetImport();
            		}
            	})
            	
            	
            	importb.off();
            	importb.fileupload({
            	    url: 'import',
            	    sequentialUploads: true,
            	    dataType: 'json'
            	})
            	.on('fileuploadstart', function (e, data) {
            		progress = 0;
            		$('#uploadProgress .progress-bar').css(
                        'width',
                        0 + '%'
                    );
                })
            	.on('fileuploadprogressall', function (e, data) {
                    progress = parseInt(data.loaded / data.total * 100, 10);
                    $('#uploadProgress .progress-bar').css(
                        'width',
                        progress + '%'
                    );
                })
            	.on('fileuploadsubmit', function (e, data) {
            		// data.formData = {filename: filename};
            		//alert(JSON.stringify(data, null, 2))
                })
            	.on('fileuploaddone', function (e, data) {
            		/*
            		 * To here, the file is uploaded to the GUI server.  Now need to monitor the
            		 * upload from the GUI server to the transaction server and the processing
            		 * on the transaction server.
            		 */
            		var success = false;
            		var coauth = false;
            		var uuid = null;
            		if (!_.isUndefined(data.result)) {
            			if (!_.isUndefined(data.result.success)) {
                			success = data.result.success;
                			uuid = data.result.uuid;
                			coauth = (_.isUndefined(data.result.coauth))?false:data.result.coauth;
                		}
            		}
            		
            		setTimeout(function() {
            			if (success) {
            				that.verifyBatch(uuid, coauth, data.files[0]);
            			}
            			else {
            				var response = _.isUndefined(data.result.response)?data:data.result.response;
            				that.displayOutcome(response, response.responseCode);
            			}
            		}, 1000);
                })
            	.on('fileuploadfail', function (e, data) {
            		that.displayOutcome(data.jqXHR.responseJSON, 'ERROR');
            		//alert(JSON.stringify(data.jqXHR, null, 2))
            		//alert(JSON.stringify(data.jqXHR, null, 2))
            		
            		//progressbar.closest('.modal').modal('hide');
                    	/*var self = this;
                    	App.vent.trigger('application:dialog', {
        	        		text: JSON.stringify(data.jqXHR, null, 2),
        	        		name: "okDialog",
        	        		hide: function() {
        	        			
        	        		},
        	        		events: {

        	        		}
                		});*/
                })
            	.on('fileuploadadd', function (e, data) {
            		jqXhr = data;
            		$('#importWizard').wizard('selectedItem', {
						step: 2
					});
            		$('body').focus();// Avoid displaying large cursor in IE on Windows
            		//alert(JSON.stringify(data, null, 2));
        	    	$('#breadcrumb H1').html(i18n.translate('heading') + ' - '+data.files[0].name);
            	});
            },
            
            downloadExample: function(e) {
            	e.preventDefault();
            	var id = $(e.currentTarget).data("id");
            	
            	window.location.href = "/import/examples/" + id;
            },
            
            ui: {
            	view: '',
                restart: '.btn-cancel',
            	downloadsample: '.downloadsample'
            },

            // View Event Handlers
            events: {
            	"click @ui.restart": 'resetImport',
            	"click @ui.downloadsample": 'downloadExample',
            },
        });
        return ManageBatchProcessingView;
    });
