define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'utils/CommonUtils', 'models/WorkItemModel', 'views/tasks/WorkItemView', 'views/tasks/CreateWorkItemDialogView', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, CommonUtils, WorkItemModel, WorkItemView, CreateWorkItemDialogView) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.tasks;
		var tableTimer = {};
	
        var TaskListView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	template: "Tasks#taskList",
			baseUrl: 'api/workflow',
			currentFilter: {}, // Used to keep track of filter settings for use by export.
			currentUserId: null,
        	attributes: {
        		class: "row",
        		id: "taskList"
        	},
        	
        	initialize: function (options) {
  		  		var self = this;
  		  		if (!_.isUndefined(App.contextConfig) && !_.isUndefined(App.contextConfig.user)) {
  		  			this.currentUserId = App.contextConfig.user.id;
  		  		}
        	},
        	
        	breadcrumb: function() {
  		  		var txt = i18ntxt;
  		  		return {
  		  			heading: txt.taskManagement,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.taskList,
  		  				href: window.location.hash,
						iclass: "fa fa-tasks"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	configureDataTable: function(table, dataUrl, timerName) {
  		  		var that = this;
  		  		table.DataTable({
        		//'stateSave': 'hash',
    			serverSide: true,
    			// data is params to send
				//"pagingType": "simple",
				//"infoCallback": function( settings, start, end, max, total, pre ) {
				//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
				//},
    			"searching": false,
    			sorting: false,
    			ordering: false,
				"processing": true,
				"serverSide": true,
				"autoWidth": false,
				"responsive": true,
				"language": {
	                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
	            },
      			"ajax": function(data, callback, settings) {
      				//self.currentFilter.url = self.baseUrl;
      				//self.currentFilter.data = data;
      				var jqxhr = $.ajax(dataUrl, {
      					data: data
      				})
                  	  .done(function(dataResponse) {
                  		callback(dataResponse);
                  	    /*callback({
                  	    	data: dataResponse,
                  	    	recordsTotal: dataResponse.length,
                  	    	recordsFiltered: dataResponse.length
                  	    });*/
						table.DataTable()
						   .columns.adjust()
						   .responsive.recalc();
                  	  })
                  	  .fail(function(error) {
                  			self.error = error;
                  			App.error(error);
                  			
                  			var generalMsg = error.responseJSON.message;
                  			if ( error.responseJSON.status == 'NOT_ACCEPTABLE' ) {
                  				// generalMsg = 'Failure';
                  			}
    							
    						$('#taskList .general_error').html(generalMsg).show();
                  			
                  	  })
                  	  .always(function(data) {
                  	  });
                  },
				  "language": {
				  	"emptyTable": i18ntxt.emptyTableMessage
				  },
                  "columns": [
                    	{
                    	   	data: "id",
                    	   	title: App.i18ntxt.global.uniqueID,
							class: "all center",
							width: "80px",
                    	},
						{
							   data: "creationTime",
							   title: i18ntxt.timeTitle,
							   defaultContent: "n/a",
							   render: function(data, type, row, meta) {
								   return CommonUtils.formatTimeStamp(data);
							   }
						},
						
					   {
                		   data: "requestType",
                		   defaultContent: "none",
                		   title: i18ntxt.typeTitle,
                		   // public enum WorkflowRequestType{REPLENISH, TRANSFER, ADJUSTMENT, REVERSAL, PARTIALREVERSAL}
                		   render: function(data, type, row, meta) {
                			   var response = '';
                			   var requestType;
                			   if (!_.isUndefined(data) && data === 'WORKFLOWREQUEST') {
                				   data = row['uri'].toUpperCase();
                				   response = 'Create ';
                			   }

                			   switch(data) {
                			   case "REPLENISH":
                				   requestType = i18ntxt.replenishDescription;
                				   break;
                			   case "TRANSFER":
                				   requestType = i18ntxt.transferDescription;
                				   break;
                			   case "ADJUSTMENT":
                				   requestType = i18ntxt.adjustmentDescription;
                				   break;
                			   case "REVERSAL":
                				   requestType = i18ntxt.reversalDescription;
                				   break;
                			   case "PARTIALREVERSAL":
                				   requestType = i18ntxt.partialReversalDescription;
                				   break;
                			   case "BATCHUPLOAD":
                				   requestType = i18ntxt.batchUploadDescription;
                				   break;
                			   case "ADJUDICATION":
                				   requestType = i18ntxt.batchAdjudicateDescription;
                				   break;
                				   
                			   case "WORKFLOWREQUEST":
                				   break;
                				   default:
                					   requestType = data;
                				   break;
                			   }
							   return response+=requestType;
						   }
                	   },
                	   
                	   {
                		   data: "description",
                		   defaultContent: "(none)",
                		   title: i18ntxt.detailsTitle
                	   },
                	   {
                		   data: "reason",
                		   defaultContent: "(none)",
                		   title: i18ntxt.reasonTitle
                	   },
                	   {
                		   data: "createdByName",
                		   title: i18ntxt.fromTitle,
						   defaultContent: "(none)"
                	   },
                	   {
                		   data: "workItemStatus",
                		   title: i18ntxt.statusTitle,
						   sortable: false,
						   defaultContent: "(none)",
						   render: function(data, type, row, meta) {
							   
							   var value = row["workItemStatus"];// Don't trust the content of data as it recently started returning the string as the first element of an array.
							   var badge = [];
							   var i=0;
							   badge[i++] = '<span class="item-status label label-';
							   
							   switch (value) {
								   case 'NEW':
									   badge[i++] = 'primary';
									   break;
								   case 'INPROGRESS':
									   badge[i++] = 'info';
									   break;
								   case 'ONHOLD':
									   badge[i++] = 'warning';
									   break;
								   case 'COMPLETED':
									   badge[i++] = 'success';
									   break;
								   case 'CANCELLED':
									   badge[i++] = 'default';
									   break;
								   case 'DECLINED':
									   badge[i++] = 'warning';
									   break;
								   case 'FAILED':
									   badge[i++] = 'danger';
									   break;
								   default:
									   badge[i++] = 'default';
									   break;
							   }
							   badge[i++] = '">';
							   badge[i++] = value;
							   badge[i++] = '</span>';
							   return badge.join('');
						   }
                	   },
                	   {
					        targets: -1,
					        data: null,
					        title: "",
						    className: "right all",
							width: "108px",
					        sortable: false,
					        "orderable": false,
					        render: function(data, type, row, meta) {
					        	var html = '';
						        var buttons = [];
						        	
					        	var taskOwner = false;
					        	if (!_.isNull(that.currentUserId) && (that.currentUserId === data.createdByWebUserID)) {
					        		taskOwner = true;
					        	}
					        	if (taskOwner) {

					        	}
					        	else if (App.hasPermission("WorkItem", "Update")) { 
						        	if (data.workItemStatus === 'ONHOLD') {
						        		// TODO ensure only original user can approve
						        		buttons.push("<li style='cursor:pointer;'><a class='approveTransactionBtn'><i class='fa fa-fw fa-random'></i>&nbsp;&nbsp;"+i18ntxt.menuApproveBtn+"</a></li>");
						        		buttons.push("<li style='cursor:pointer;'><a class='declineTransactionButton'><i class='fa fa-fw fa-thumbs-down'></i>&nbsp;&nbsp;"+i18ntxt.menuDeclineBtn+"</a></li>");
						        	}
						        	else if (data.workItemStatus === 'NEW' && data.requestType === 'WORKFLOWREQUEST') {
						        		buttons.push("<li style='cursor:pointer;'><a class='holdTransactionButton'><i class='fa fa-fw fa-pause'></i>&nbsp;&nbsp;"+i18ntxt.menuHoldBtn+"</a></li>");
						        		buttons.push("<li style='cursor:pointer;'><a class='createTransactionBtn'><i class='fa fa-fw fa-random'></i>&nbsp;&nbsp;"+i18ntxt.menuCreateBtn+"</a></li>");
						        		buttons.push("<li style='cursor:pointer;'><a class='declineTransactionButton'><i class='fa fa-fw fa-thumbs-down'></i>&nbsp;&nbsp;"+i18ntxt.menuDeclineBtn+"</a></li>");
						        	}
						        	else if (data.workItemStatus === 'COMPLETED') {
						        		buttons.push('');
						        	}
						        	else {
						        		buttons.push("<li style='cursor:pointer;'><a class='holdTransactionButton'><i class='fa fa-fw fa-pause'></i>&nbsp;&nbsp;"+i18ntxt.menuHoldBtn+"</a></li>");
						        		buttons.push("<li style='cursor:pointer;'><a class='approveTransactionBtn'><i class='fa fa-fw fa-random'></i>&nbsp;&nbsp;"+i18ntxt.menuApproveBtn+"</a></li>");
						        		buttons.push("<li style='cursor:pointer;'><a class='declineTransactionButton'><i class='fa fa-fw fa-thumbs-down'></i>&nbsp;&nbsp;"+i18ntxt.menuDeclineBtn+"</a></li>");
						        	}
					        	}
					        	
					        	if (taskOwner) {
									if (data.workItemStatus !== 'ONHOLD' && data.workItemStatus !== 'COMPLETED' && data.workItemStatus !== 'DECLINED' && data.workItemStatus !== 'FAILED'  && data.workItemStatus !== 'CANCELLED') {
										html += '<div class="btn-group">';
										html += ' <a class="btn btn-primary btn-xs cancelTaskBtn" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#taskList/'+row['id']+'">'+i18ntxt.menuCancelBtn+'</a>';
										html += '</div>';
					        		}
								}
								else if (App.hasPermission("WorkItem", "Update"))
								{
									if (data.workItemStatus !== 'COMPLETED' && data.workItemStatus !== 'DECLINED' && data.workItemStatus !== 'FAILED'  && data.workItemStatus !== 'CANCELLED') {
										html += '<div class="btn-group">';
										if (data.workItemStatus === 'ONHOLD') {
											html += ' <a class="btn btn-primary btn-xs unHoldTransactionBtn" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#taskList/'+row['id']+'">'+i18ntxt.menuUnHoldBtn+'</a>';
										}
										else if (data.workItemStatus === 'NEW' && data.requestType === 'WORKFLOWREQUEST') {
											html += ' <a class="btn btn-primary btn-xs createTransactionBtn" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#taskList/'+row['id']+'">'+i18ntxt.menuCreateBtn+'</a>';
										}
										else {
											html += ' <a class="btn btn-primary btn-xs approveTransactionBtn" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#taskList/'+row['id']+'">'+i18ntxt.menuApproveBtn+'</a>';
										}
										if (data.workItemStatus !== 'ONHOLD') {
											html += ' <button type="button" class="btn btn-primary btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="margin-right:0;">';
											html += '    <span class="caret"></span>';
											html += ' </button>';
											html += ' <ul class="dropdown-menu dropdown-menu-right">';
											html += buttons.join(' ');
											html += ' </ul>';
											html += '</div>';
										}
									}
								}
						
						 		return html;
						 	}
					    },
                	  ]
              })
              .on( 'draw.dt', function () {
            	  if (!_.isUndefined(tableTimer[timerName])) {
            		  clearTimeout(tableTimer[timerName].timer);
            	  }
            	  tableTimer[timerName] = {
            			timer: setTimeout( function () {
            				if (!tableTimer[timerName].pause) {
            					table.DataTable().ajax.reload(null, false);
            				}
	  					}, 30000 ),
	  					table: table,
	  					pause: false
	            	  }
				} );
  		  		
	  		  	table.find('.dataTables_filter input').unbind();
				table.find('.dataTables_filter input').bind('keyup', function(e) {
					if(e.keyCode == 13) {
						self.dataTable.fnFilter(this.value);	
					}
				});
  		  	},
  		  	
            onRender: function () {
            	var self = this;
            	
            	var myTaskTable = this.$('.mytasktable');
            	var inboxTable = this.$('.inboxtable');
            	var historyTable = this.$('.historytable');
            	this.configureDataTable(myTaskTable, this.baseUrl+"/taskList", "list");
            	this.configureDataTable(inboxTable, this.baseUrl+"/inBox", "inbox");
            	this.configureDataTable(historyTable, this.baseUrl+"/history", "history");

					

            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            processRequest: function(action, ev) {
            	var that = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var dataTable = $(ev.currentTarget).closest('table').DataTable();
            	var clickedRow = dataTable.row(row);
            	var data = clickedRow.data();
            	var model = new WorkItemModel(data);
            	
            	App.log(JSON.stringify(model, null, 2));

            	model.save({
            		action: action
            	},
            	{
            		success: function(data){
            			//var dialog = that.$el.closest('.modal');
            			//dialog.modal('hide');
            			that.updateTimeouts(false);
            			clickedRow.data(data.attributes).draw();
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
            
            updateTimeouts: function(clear) {
            	if (!_.isUndefined(tableTimer)) {
            		if (clear) {
	            		for (var key in tableTimer) {
	            		  if (tableTimer.hasOwnProperty(key)) {
	            			  tableTimer[key].pause = true;
	            		  }
	            		}
            		}
            		else {
            			for (var key in tableTimer) {
  	            		  if (tableTimer.hasOwnProperty(key)) {
  	            			tableTimer[key].timer = setTimeout( function () {
  	            				tableTimer[key].table.DataTable().ajax.reload(null, false);
	  	  					}, 0 );
  	            			tableTimer[key].pause = false;
  	            		  }
  	            		}
            		}
            	}
            },
            
            deleteWorkItem: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var table = $(ev.currentTarget).closest('table');
            	var dataTable = $(ev.currentTarget).closest('table').DataTable();
            	var clickedRow = dataTable.row(row);
            	var data = clickedRow.data();

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.tasks.workItem,
	        		url: 'api/workflow/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.tasks.workItem,
	        			name: data.description,
	        			description: data.createdByName
	        		},
	        		rowElement: row,
	        	},
	        	{
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            	return false;
            },
            
            createTask: function() {
            	var that = this;
            	
            	var model = new WorkItemModel({
            		url: that.baseUrl+'/create',
            		'language': App.contextConfig.languageID,
        			'seperators': App.contextConfig.seperators[App.contextConfig.languageID]
            	});
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'work-item',
            		view: CreateWorkItemDialogView,
            		//title:CommonUtils.renderHtml(i18ntxt.workItemApproveDialogTitle, {name: tableData.name}),
            		title:CommonUtils.renderHtml(i18ntxt.createWorkItemDialogTitle),
            		hide: function() {
            			//that
				        //.dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
            },
            
            reviewTask: function(ev, requiresReason, action, callback) {
            	var that = this;
            	var inboxTable = this.$('.inboxtable');
            	var currentRow = $(ev.currentTarget).closest('tr');
            	var tableData = inboxTable.DataTable().row(currentRow).data();
            	var dialogParams = {
            		type: tableData.requestType,
            		action: action,
            		row: tableData,
            		callback: callback,
            		requiresReason: _.isUndefined(requiresReason)?false:requiresReason
            	};
            	currentRow.addClass("actionhiglight").fadeIn();
            	
            	switch(tableData.requestType) {
	            	case 'BATCHUPLOAD':
	            		dialogParams.url = 'api/batch/history/header/'+tableData.batchID;
	            		dialogParams.id = tableData.batchID;
	            		break;
            		default:
            			dialogParams.url = 'api/workflow/detail/'+tableData.uuid;
            			dialogParams.id = tableData.uuid;
            			break;
            	}
            	var jqxhr = $.get(dialogParams.url, function(data) {
        			
        			// Now configure datatables
        			// 'api/batch/history/csv/'
            		dialogParams.data = data;
        			
        			App.vent.trigger('application:dialog', {
                		name: "viewDialog",
                		view: WorkItemView,
                		title:"Test",
                		/*hide: function() {
                			currentRow.animate({
                			    'opacity': '0',
                			    complete: function() {
                					currentRow.removeClass("actionhiglight");
                					inboxTable.DataTable().ajax.reload().draw();
                				}
                			}, 1000);
    	        		},*/
    	        		hide: function() {
                			currentRow.animate({
                			    'opacity': '0.5'
                			}, 200, function () {
                				currentRow.removeClass("actionhiglight");
                				currentRow.css({
                			        'opacity': '1'
                			    });
                				
                				inboxTable.DataTable().ajax.reload().draw();
                			});
    	        		},
                		params: dialogParams
                	});
        		})
        		  .done(function() {
        		    //alert( "second success" );
        		  })
        		  .fail(function() {
        		    alert( "error" );
        		  })
        		  .always(function() {
        		   // alert( "finished" );
        		  });
            	return false;
            },
            
            previewTransaction: function(action, requiresReason, ev) {
            	var that = this;

            	that.updateTimeouts(true);
            	that.reviewTask(ev, requiresReason, action);
            	return false;
            },

            holdtransaction: function(ev) {
            	return this.previewTransaction('HOLD', true, ev);
            },
            
            declineTransaction: function(ev) {
            	return this.previewTransaction('DECLINE', true, ev);
            },
            
            approveTransaction: function(ev) {
            	this.previewTransaction('APPROVE', false, ev);
            	return false;
            },
            
            unHoldTransaction: function(ev) {
            	this.previewTransaction('UNHOLD', false, ev);
            	return false;
            },
            
            createTransactionFromWorkflow: function(ev) {
            	this.processRequest('EXECUTEWORKFLOW', ev);
            	return false;
            },
            
            cancelWorkItem: function(ev) {
            	this.processRequest('CANCELLED', ev);
            	return false;
            },

            ui: {
                approveTransaction: 	'.approveTransactionBtn',
                holdtransaction: 		'.holdTransactionButton',
                declineTransaction: 	'.declineTransactionButton',
                createTransactionFromWorkflow:	'.createTransactionBtn',
                createTask:				'.createTaskBtn',
                cancelWorkItem:			'.cancelTaskBtn',
                unHoldTransaction: 		'.unHoldTransactionBtn',
                viewTaskBtn:			'.viewTaskBtn'
            },

            // View Event Handlers
            events: {
            	"click @ui.createTransactionFromWorkflow": 'createTransactionFromWorkflow',
            	"click @ui.approveTransaction": 'approveTransaction',
            	"click @ui.holdtransaction": 'holdtransaction',
            	"click @ui.declineTransaction": 'declineTransaction',
            	"click @ui.createTask": 'createTask',
            	"click @ui.cancelWorkItem": 'cancelWorkItem',
            	"click @ui.unHoldTransaction": 'unHoldTransaction',
            	"click @ui.viewTaskBtn": 'reviewTask'
            }
        });
        return TaskListView;
    });
