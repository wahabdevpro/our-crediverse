define( ['jquery', 'underscore', 'backbone', 'App', 'marionette', 'models/CoAuthModel', 'models/WorkItemImportModel', 'utils/CommonUtils', 'utils/HandlebarHelpers', 'datatables', 'file-upload'],
    function($, _, BackBone, App, Marionette, CoAuthModel, WorkItemImportModel, CommonUtils, HBHelper) {
		var i18n = CommonUtils.i18nLookup('msisdnRecycling');
        var MsisdnRecyclingUploadView =  Marionette.ItemView.extend( {
        	tagName: 'div',
			currentFilter: {},
        	attributes: {
        		class: "row"
        	},
  		  	template: "MsisdnRecyclingUpload#manageuploads",
  		  	url: 'msisdn_recycle',
  		  	error: null,
  		  	timer: null,
			i18ntxt: App.i18ntxt.msisdnRecycling,
			filename: null,
			dataTable: null,
			uuid: null,

        	coAuth: null,
        	
        	breadcrumb: function() {
  		  		return {
  		  			heading: i18n.translate('heading'),
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: i18n.translate('sectionBC')
  		  			}, {
  		  				text: i18n.translate('pageBC'),
  		  				href: "#msisdnRecyclingUpload"
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
            displayResults: function(uuid) {

				this.uuid = uuid;
		
				var self = this;
				var tableSettings = {
					searchBox: true,
					newurl: self.url + '/list/' + uuid
				};

				//if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);

				var table = this.$('.recyclepreviewtable');
				if( this.dataTable == null )
				{
					this.dataTable = table.DataTable({
						dom: '<t>',
						//dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
						'stateSave': 'hash',
						"paging": false,
						//serverSide: true,
						// data is params to send
						//"pagingType": "simple",
						//"infoCallback": function( settings, start, end, max, total, pre ) {
						//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
						//},
						"processing": true,
						//"searching": false,
						//"serverSide": true,
						"autoWidth": false,
						'stateSave': 'hash',
						"responsive": true,
						"language": {
							"url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
							"searchPlaceholder": "Account #, Mobile Number, Agent Name ...",
						},
						"initComplete": function(settings, json) {
							//if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
							self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
						},
						"createdRow": function( row, data, dataIndex ) {
							if( !data.recyclable ) {
								$(row).addClass('cs-recycle-disabled');	
								$(row).find('td.select-checkbox').removeClass('select-checkbox');	
							} else {
								table.DataTable().row( row ).select();
							}
						},
						//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
						"ajax": function(data, callback, settings) {
							var url = self.url + '/list/' + self.uuid;
							App.log('fetching data: ' + url);
							self.currentFilter.url = url;
							self.currentFilter.data = data;
							var jqxhr = $.ajax(url, {
								data: data
							})
								.done(function(dataResponse) {
							  console.dir( dataResponse);
									callback(dataResponse);
									table.DataTable()
										.columns.adjust()
										.responsive.recalc();
								})
								.fail(function(dataResponse) {
									self.error = dataResponse;
									App.error(dataResponse);
									App.vent.trigger('application:msisdnrecycleerror', dataResponse);
								})
								.always(function(data) {
								});
						},
						"order": [[ 0, "asc" ]],
						"select": {
				             style:    'multi',
				        },
						"columns": [
							{
								data: "agentId",
								title: App.i18ntxt.msisdnRecycling.tableAccountIdTitle,
								class: "all center",
								width: "80px",
								render: function(data, type, row, meta) {
									return '<a class="routerlink" href="#account/' + row['id'] + '">' + data + '</a>';
								}
							},
							{
								data: "accountNumber",
								title: App.i18ntxt.msisdnRecycling.tableAccountNumberTitle,
								defaultContent: "-"
							},
							{
								data: "mobileNumber",
								title: App.i18ntxt.msisdnRecycling.tableMobileNumberTitle
							},
							{
								data: "firstName",
								title: App.i18ntxt.msisdnRecycling.tableFirstNameTitle,
								render: function(data, type, row, meta) {
									return row['firstName'] + " " + row['lastName'];
								}
							},
							{
								data: "balance",
								title: App.i18ntxt.msisdnRecycling.tableBalanceTitle,
								className: "right field-balance",
								render: function(data, type, row, meta) {
									return CommonUtils.formatNumber(data || 0);
								}
							},
							{
								data: "bonusBalance",
								title: App.i18ntxt.msisdnRecycling.tableBonusBalanceTitle,
								className: "right",
								render: function(data, type, row, meta) {
									return CommonUtils.formatNumber(data || 0);
								}
							},
							{
								data: "onHoldBalance",
								title: App.i18ntxt.msisdnRecycling.tableOnHoldBalanceTitle,
								className: "right",
								render: function(data, type, row, meta) {
									return CommonUtils.formatNumber(data || 0);
								}
							},
							{
								data: "state",
								title: App.i18ntxt.msisdnRecycling.tableStateTitle,
								className: "center",
								render: function(data, type, row, meta) {
									var response = [];
									response.push('<span class="label');
									if (data === 'A') {
	                    			   	response.push('label-success">'+App.i18ntxt.agentAccounts.responseStateActive+'</span>');
	                    			} else if (data === 'S') {
	                    				response.push('label-warning">'+App.i18ntxt.agentAccounts.responseStateSuspended+'</span>');
	                    			} else if (data === 'D') {
	                    				response.push('label-danger">'+App.i18ntxt.agentAccounts.responseStateDeactivated+'</span>');
	                    		   	} else {
	                    				response.push('label-default">'+App.i18ntxt.agentAccounts.responseStatePermanent+'</span>');
	                    			}
	                    			return response.join(' ');
								}
							},
							{
								title: App.i18ntxt.msisdnRecycling.tableSelectTitle,
								className: "select-checkbox right all",
								width: "60px",
								sortable: false,
								orderable: false,
							}
						]
					});
				}	
				else
					this.dataTable.ajax.reload();

				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#accountSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');  

				table.find('.dataTables_filter input').unbind();
				table.find('.dataTables_filter input').bind('keyup', function(e) {
					if(e.keyCode == 13) {
						self.dataTable.fnFilter(this.value);
					}
				});

				this.dataTable.on('select', function(e, dt, type, indexes) {
  					if (type === 'row') {
						var data = self.dataTable.rows( indexes ).data();
    					var rows = self.dataTable.rows(indexes).nodes().to$();
    					$.each(rows, function() {
      						if( $(this).hasClass('cs-recycle-disabled') ) self.dataTable.row($(this)).deselect();
    					});
  					}
				});

				//var table = $('#tabledata');
				if (this.error === null) {

				}
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
            	else {
					var hasIssues = _.isUndefined(data.issues) ? false : (data.issues.length > 0);

            		$('#outcomepane .box-solid').removeClass('box-danger box-warning box-success');
            		$('#outcomepane .box-solid').addClass(hasIssues ? 'box-warning' : 'box-success');
            		$('#outcomepane .status ').html(' '+i18n.translate(hasIssues ? 'outcomeStatusPartialSuccess' : 'outcomeStatusSuccess'));
            		
            		if (!_.isUndefined(data) /*&& !_.isUndefined(data.batchStatus)*/) {
						var recycledRec = !_.isUndefined(data.agentsRecycled) && _.isArray(data.agentsRecycled) ? data.agentsRecycled.length : 0;
						var nonRecycledRec = !_.isUndefined(data.nonRecyclableAgents) && _.isArray(data.nonRecyclableAgents) ? data.nonRecyclableAgents.length : 0;
						var totalRec = recycledRec + nonRecycledRec;
                		$('#outcomeStatus').show();
                		$('#outcomepane .filename ').html(that.filename);
                		$('#outcomepane .recordcount ').html(totalRec ? totalRec : '-');
                		$('#outcomepane .recordsuccess ').html(recycledRec ? recycledRec : '-');
                		$('#outcomepane .recordfailed ').html(nonRecycledRec ? nonRecycledRec : '-');

						if( hasIssues ) {
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
		                        		   data: "property",
		                        		   title: i18n.translate('fieldNameTableHeading')
		                        	   },
		                        	   {
		                        		   data: "returnCode",
		                        		   title: i18n.translate('errorCodeTableHeading'),
		                        		   render: function(data, type, row, meta) {
		                        			   return data.replace(/_/g, ' ');
		                        		   }
		                        	   },
		                        	   {
		                        		   data: "additionalInformation",
		                        		   title: i18n.translate('errorDescriptionTableHeading'),
		                        		   render: function(data, type, row, meta) {
										   	   return data;
		                        		   }
		                        	   }
		                        	  ]
		                      } );
						}
                	}
					/*
            		else {
            			$('#outcomeMessage').html("<h3>File "+data.localFilename+" submitted for authorization</h3>");
            			$('#outcomeMessage').show();
            		}
					*/
            	}
            	
           
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
                	    url: "msisdn_recycle/status/"+uuid,
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
                	    url: "/msisdn_recycle/process/"+uuid,
                	    method: 'POST',
                	    success: function(data) {
							$.ajax({
                        	    url: "msisdn_recycle/status/"+uuid,
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
            	    url: "msisdn_recycle/status/"+uuid,
            	    //url: "msisdn_recycle/results/"+uuid,
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
            	    	    that.displayAgentsToRecycle(uuid);
            	    		//that.timer = setTimeout($.proxy(that.performCoauthorisation, that, uuid, coauth, file), 1000);
            	    	}
            	    	else if (data.status !== 'COMPLETE') {
            	    		that.displayBatchStatus(progressbar, data.batchStatus, 'verifyStatus');
            	    		that.displayAgentsToRecycle(uuid);
            	    		//that.timer = setTimeout($.proxy(that.verifyBatch, that, uuid, coauth, file), 1000);
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
			displayAgentsToRecycle: function(uuid) {
				var that = this;
				$('#importWizard').wizard('selectedItem', {
					step: 4
				});

				that.displayResults(uuid);
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
            	
            	var wizard = this.$('#manageuploads');
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
            	    url: 'msisdn_recycle/upload',
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
            		
                })
            	.on('fileuploadadd', function (e, data) {
            		jqXhr = data;
            		$('#importWizard').wizard('selectedItem', {
						step: 2
					});
            		$('body').focus();// Avoid displaying large cursor in IE on Windows
            		//alert(JSON.stringify(data, null, 2));
        	    	$('#breadcrumb H1').html(i18n.translate('heading') + ' - '+data.files[0].name);
					that.filename = data.files[0].name;
            	});
            },
            
            downloadExample: function(e) {
            	e.preventDefault();
            	var id = $(e.currentTarget).data("id");
            	
            	window.location.href = "/import/examples/" + id;
            },

			submitRecycle: function(e) {
            	$('#importWizard').wizard('selectedItem', {
					step: 5
				});
				var self = this;
				var rows = this.dataTable.rows( { selected: true } ).nodes().to$();
				var selected = [];
    			$.each(rows, function() {
					var data = self.dataTable.row($(this)).data();
					selected.push( data.agentId );
    			});

				$.ajax({
			    	type : "POST",
			        url: self.url + '/submit',
			        headers : {
			            'Accept': 'application/json',
			            'Content-Type': 'application/json'
			        },
			        data : JSON.stringify( selected, null, 2 ),
			        datatype : "json",
			        timeout : self.TIMEOUT
			    }).done(function(response) {
           		 	$('#importWizard').wizard('selectedItem', {
						step: 6
					});
            	  	self.displayOutcome(response, response.returnCode);

			    }).fail(function(response) {
					console.log( 'submit failed: ' );
					console.log( response );
			    });
			},
            
            ui: {
            	view: '',
                restart: '.btn-cancel',
            	downloadsample: '.downloadsample',
				msisdnRecycleSubmit: '.msisdnRecycleSubmitButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.restart": 'resetImport',
            	"click @ui.downloadsample": 'downloadExample',
            	"click @ui.msisdnRecycleSubmit": 'submitRecycle',
            },
        });
        return MsisdnRecyclingUploadView;
    });
