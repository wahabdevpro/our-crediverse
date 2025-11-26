define( ['jquery', 'App', 'backbone', 'marionette', 'handlebars', 'models/AgentModel', 'models/TransferModel', 'views/accounts/TransferDialogView',
         'collections/TierCollection', 'utils/CommonUtils', 'models/TransferModel', 'views/operations/TransferFundsDialogView',
         'views/users/PermanentUserDialogView', 'views/users/ProfileDialogView', 'views/PasswordChangeDialogView', 'models/PasswordChangeModel',
         'datatables'],
    function($, App, BackBone, Marionette, Handlebars, AgentModel, TransferModel, TransferDialogView,
    		TierCollection, CommonUtils, TransferModel, TransferFundsDialogView,
    		PermanentUserDialogView, ProfileDialogView, PasswordChangeDialogView, PasswordChangeModel) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;
		var i18nPin = App.i18ntxt.changePasswordDialog;

        var AgentAccountsTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
			baseUrl: 'papi/agents',
			currentFilter: {}, // Used to keep track of filter settings for use by export.
        	attributes: {
        		class: "row"
        	},

        	renderTable: function(newurl) {
            	var self = this;
            	var tableUrl = (_.isUndefined(newurl)?self.url:newurl);

            	var table = this.$('.accountstable');
            	this.dataTable = table.DataTable({
            		//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
            		'stateSave': 'hash',
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},
					"processing": true,
					//"searching": false,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Account #, Mobile Number, Agent Name ...",
		            },
		            "initComplete": function(settings, json) {
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
		              },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
          			"ajax": function(data, callback, settings) {
          				self.currentFilter.url = tableUrl;
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(tableUrl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
							table.DataTable()
							   .columns.adjust()
							   .responsive.recalc();
                      	  })
                      	  .fail(function(dataResponse) {
                      			self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:accountsterror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "id",
                    		   title: i18ntxt.tableUniqueIDTitle,
							   class: "all center",
							   render: function(data, type, row, meta) {
								   return '<a class="routerlink" href="#account/' + row['id'] + '">' + data + '</a>';
							   		//return '<a target="_blank" href="#account/'+row['id']+'">' + data + '</a>';
							   }
                    	   },
                    	   {
                    		   data: "accountNumber",
                    		   title: i18ntxt.tableAccountNumberTitle,
                    		   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "mobileNumber",
                    		   title: i18ntxt.tableMobileNumberTitle
                    	   },
                    	   {
                    		   data: "firstName",
                    		   title: i18ntxt.tableFirstNameTitle,
							   render: function(data, type, row, meta) {
							   		return row['firstName'] + " " + row['surname'];
							   }
                    	   },

                    	   //Stuart will probably ask for this later - at the moment the TS only sends the ownerAgentID in the list.
                    	   /*{
                    		   data: "ownerAgent",
                    		   title: i18ntxt.tableOwnerAgentTitle,
							   sortable: false,
							   defaultContent: "(none)"
                    	   },*/
                    	   {
                    		   data: "tierName",
                    		   title: i18ntxt.tableTierNameTitle,
							   //sortable: false,
							   defaultContent: "<span class='label label-danger'>(invalid)</span>"
                    	   },
                    	   {
                    		   data: "groupName",
                    		   title: i18ntxt.tableGroupNameTitle,
                    		   defaultContent: App.i18ntxt.global.none,
                    		   //sortable: false
                    	   },
                    	   {
                    		   data: "balance",
                    		   title: i18ntxt.tableBalanceTitle,
							   className: "right",
							   render: function(data, type, row, meta) {
							   		return CommonUtils.formatNumber(data);
							   }
                    	   },


						   /*
                    	   {
                    		   data: "bonusBalance",
                    		   title: i18ntxt.tableBonusBalanceTitle,
							   sortable: false,
							   className: "right"
                    	   },
						   */
                    	   {
                    		   data: "currentState",
                    		   title: i18ntxt.tableCurrentStateTitle,
							   className: "center",
                    		   render: function(data, type, row, meta) {
                    			   var response = [];
                    			   response.push('<span class="label');
                    			   if (data === 'ACTIVE') {
                    				   response.push('label-success">'+i18ntxt.responseStateActive+'</span>');
                    			   } else if (data === 'SUSPENDED') {
                    				   response.push('label-warning">'+i18ntxt.responseStateSuspended+'</span>');
                    			   } else if (data === 'DEACTIVATED') {
                    				   response.push('label-danger">'+i18ntxt.responseStateDeactivated+'</span>');
                    			   } else {
                    				   response.push('label-default">'+i18ntxt.responseStatePermanent+'</span>');
                    			   }
                    			   return response.join(' ');
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
							    className: "right all",
								width: "108px",
                   	            sortable: false,
                   	            render: function(data, type, row, meta) {
	               	            	var menuData = {};
	               	            	menuData.rowID = row.id;
	               	            	menuData.canChange = (row.currentState!=="SUSPENDED" && row.currentState!=="DEACTIVATED")?true:false;
	               	            	menuData.transferFromRoot = ((row.tierType == "WHOLESALER") || (row.tierType == "STORE") || (row.tierType == "RETAILER"))?true:false;
	               	            	menuData.isRoot = (row.tierType === "ROOT")?true:false;
	               	            	menuData.notPermanent = (row.currentState==="PERMANENT")?false:true;
	               	            	menuData.isActive = (row.currentState==="ACTIVE")?true:false;
	               	            	menuData.isSuspended = (row.currentState==="SUSPENDED")?true:false;
	               	            	menuData.isDeactivated = (row.currentState==="DEACTIVATED")?true:false;
	               	            	menuData.authenticationMethod = row.authenticationMethod;

	               	            	var $html = $(CommonUtils.getTemplateHtml("AgentAccounts#tableMenuView", menuData));
	               	            	if ($html.find('.dropdown-menu li').length > 0) {
	               	            		$html.find('.actionDropdown').show();
	               	            		$html.find('.actionButton').hide();
	               	            	}
	               	            	else {
	               	            		$html.find('.actionDropdown').hide();
	               	            		$html.find('.actionButton').show();
	               	            	}
	               	            	return $html.html();
                   	            }
                   	        }
                    	  ]
                  })

				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#accountSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');

				table.find('.dataTables_filter input').unbind();
				table.find('.dataTables_filter input').bind('keyup', function(e) {
					if(e.keyCode == 13) {
						self.dataTable.fnFilter(this.value);
					}
				});

            	//var table = $('#tabledata');
            	if (this.error === null) {

            	}
        	},

            onRender: function () {
            	this.renderTable();
            },

            ui: {
                role: '',
                performTransfer: '.performTransferButton',
                pinReset: '.pinResetButton',
                editAgent:	'.editAgentButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.role": 'viewRole',
            	"click @ui.performTransfer": 'performTransfer',
            	"click @ui.pinReset": 'pinReset',
            	"click @ui.editAgent": 'editSubAgent'
            },

            editSubAgent: function(ev) {
            	var self = this;
            	var clickedRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var userData = clickedRow.data();
            	var model = new ProfileModel();
            	model.url = 'papi/profile/subagent';
            	model.set(userData);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: (userData.state == 'P')? PermanentUserDialogView : ProfileDialogView,
            		title: CommonUtils.renderHtml(App.i18ntxt.userman.editUserTitle, {user: userData.domainAccountName, uniqueID: userData.id}),
            		hide: function() {
            			self.model.set(model.attributes);
        				self.render();
            		},
            		params: {
            			model: model
            		}
            	});
            	return false;
            },

			pinReset: function(ev) {
            	var self = this;
            	var clickedRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var data = clickedRow.data();
            	var title = "";
            	var body = "";
            	var resultTitle = "";
            	var resultText = "";
            	if(data.authenticationMethod == "A"){
                	title = "<h3>" + i18nPin.passwordResetHeading + "</h3>";
                	body = "<p>" + i18nPin.passwordResetMessage + "</p>";
                	resultTitle = "<h3>" + i18nPin.passwordResetHeadingSuccess + "</h3>";
                	resultText = "<p>" + i18nPin.passwordResetMessageSuccess + "</p>";
            	} else {
                	title = "<h3>" + i18nPin.pinResetHeading + "</h3>";
                	body = "<p>" + i18nPin.pinResetMessage + "</p>";
                	resultTitle = "<h3>" + i18nPin.pinResetHeadingSuccess + "</h3>";
                	resultText = "<p>" + i18nPin.pinResetMessageSuccess + "</p>";
            	}
            	var headingModel = {name: (data.firstName + " " + data.surname), msisdn: data.mobileNumber};
        		App.vent.trigger('application:dialog', {
	        		text: CommonUtils.renderHtml(title + body, headingModel),
	        		name: "yesnoDialog",
	        		events: {
	        			"click .yesButton":
	        			function(event) {
	        				$.ajax({
	                    	    url: self.baseUrl+'/pinreset/'+data.id,
	                    	    type: 'PUT',
	                    	    success: function(result) {
									CommonUtils.showOkDialog({
										title: CommonUtils.renderHtml(resultTitle),
										text: CommonUtils.renderHtml(resultText),
										callback: function() {}
									});
	                    	    }
	                    	});
	        				this.modal('hide');
	        			}
	        		}
        		});
            },

            performTransfer: function(ev) {
            	var self = this;
            	var tableData = this.dataTable.row($(ev.currentTarget).closest('tr')).data();
            	var model = new TransferModel({
            		agentId: tableData.id,
            		agentTransfer: true,
            		targetMSISDN: tableData.mobileNumber
            	});

            	// Open Dialog and request Amount
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: TransferFundsDialogView,
            		params: {
            			model: model,
            			table: self.dataTable
            		}
            	});

            	return false;
            },

            dataFromEvent: function(ev) {
        		var self = this;
        		var checkType = this.$('#viewSingleAgent');
            	var data = {};
            	if (checkType.length == 1) {
            		data = this.model.attributes;
            		data.row = checkType;
            		data.redraw = function() {
            			Backbone.history.loadUrl(Backbone.history.fragment);
	            	}
            	}
            	else {
            		var row = $(ev.currentTarget).closest('tr');
            		var clickedRow = this.dataTable.row(row);
	            	data = clickedRow.data();
	            	data.row = row;
	            	data.redraw = function() {
	            		data.row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	            	}
            	};
            	return data;
        	},

            suspendAgent: function(ev) {
            	var self = this;
            	var data = this.dataFromEvent(ev);
            	if (data.currentState === "ACTIVE") {
            		CommonUtils.delete({
    	        		title: i18ntxt.suspendAgentMessage,
    	        		msg: "Suspend User account <b>{{account}}</b> ({{name}} {{surname}})",
    	        		context: {
    	        			account: data.accountNumber,
    	        			name: data.firstName,
    	        			surname: data.surname
    	        		},
    	        		url: self.baseUrl+'/suspend/'+data.id,
    	        		actionBtnText: "Suspend",
    	        		actionBtnClass: "btn-warning",
    	        		modalType: CommonUtils.modalType.put,
    	        		data: data,
    	        		rowElement: data.row,
    	        		highlightCss: "warningHighlight",
    	        	}, {
    	        		success: function(model, response) {
    	        			data.redraw();
    	        		},
    	        		error: function(model, response) {
    	        			App.error(reponse);
    	        		}
    	        	});
            	}
            },

            activateAgent: function(ev) {
            	var self = this;
            	var data = this.dataFromEvent(ev);
            	if (data.currentState === "SUSPENDED" || data.currentState === "DEACTIVATED") {
            		CommonUtils.delete({
    	        		title: i18ntxt.activateAgentMessage,
    	        		msg: "Re-Activate User account <b>{{account}}</b> ({{name}} {{surname}})",
    	        		context: {
    	        			account: data.accountNumber,
    	        			name: data.firstName,
    	        			surname: data.surname
    	        		},
    	        		url: self.baseUrl+'/activate/'+data.id,
    	        		actionBtnText: "Activate",
    	        		actionBtnClass: "btn-warning",
    	        		modalType: CommonUtils.modalType.put,
    	        		data: data,
    	        		rowElement: data.row,
    	        		highlightCss: "warningHighlight",
    	        	}, {
    	        		success: function(model, response) {
    	        			data.redraw();
    	        		},
    	        		error: function(model, response) {
    	        			App.error(reponse);
    	        		}
    	        	});
            	}
            },

            deactivateAgent: function(ev) {
            	var self = this;
            	var data = this.dataFromEvent(ev);
            	if (data.currentState === "SUSPENDED" || data.currentState === "ACTIVE") {
    	        	CommonUtils.delete({
    	        		title: i18ntxt.deactivateAgentMessage,
    	        		msg: "Deactivate User account <b>{{account}}</b> ({{name}} {{surname}})",
    	        		context: {
    	        			account: data.accountNumber,
    	        			name: data.firstName,
    	        			surname: data.surname
    	        		},
    	        		url: self.baseUrl+'/deactivate/'+data.id,
    	        		actionBtnText: "Deactivate",
    	        		modalType: CommonUtils.modalType.put,
    	        		data: data,
    	        		rowElement: data.row,
    	        		highlightCss: "warningHighlight",
    	        	}, {
    	        		success: function(model, response) {
    	        			data.redraw();
    	        		},
    	        		error: function(model, response) {
    	        			App.error(reponse);
    	        		}
    	        	});
            	}
            },
        });
        return AgentAccountsTableView;
    });
