define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/TdrModel', 'views/tdrs/TdrReverseView', 'models/ReversalModel', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, AgentModel, TdrReverseView, ReversalModel, CommonUtils) {
        //ItemView provides some default rendering logic
        var TdrTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.transactions,
        	
        	attributes: {
        		class: "row"
        	},
            
            renderTable: function(options, urlToLoad, callback) {
            	App.log( 'rendering table' );
            	var self = this;

				// defaults to TRUE
				const searchBox = typeof (options || {}).searchBox === 'boolean' ? options.searchBox : true;

            	var tableSettings = {
            			searchBox,
            			newurl: self.url
            	};
            	if (!_.isUndefined(urlToLoad) && urlToLoad.length > 0) {
            		tableSettings.newurl = urlToLoad;
  				}
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);
            	var withcount = $('#withcount').val();
            	var pagingType = "simple";
				var bInfo = false;
            	if(withcount == 'true'){
    				pagingType = "full_numbers";
    				bInfo = true;	        				
    			}
            	var table = this.$('.tdrstable');
            	this.dataTable = table.DataTable({
            		'stateSave': 'hash',
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},

					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					pagingType: pagingType,
					bInfo : bInfo,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "MSISDN A",
		            },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
					//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
				
						let label = self.$('.dataTables_filter label').html();
						label = label.replace('Quick filter', 'Filter by MSISDN A')
						self.$('.dataTables_filter label').html(label);

						self.$('.dataTables_filter input').unbind();
						self.$('.dataTables_filter input').numeric({decimal:false, negative:false, decimalPlaces:0});
						self.$('.dataTables_filter input').bind('keyup', function(e) {
							if(e.keyCode == 13 && (this.value.length == 0 || this.value.length >= 3)) {
								$(this).attr('data-original-title', '').tooltip('hide');
								//self.dataTable.search(this.value).draw();	
								self.agentSearch(this.value);
							} else {
								var newTitle = null;
								var title = $(this).attr('data-original-title');
								if(this.value.length >= 3) newTitle = App.translate('transactions.tooltipQSInstructions');
								else newTitle = App.translate('transactions.tooltipQSMinCharactersMSISDN');
								if (title !== newTitle)
									$(this).attr('data-original-title', newTitle).tooltip('show');
							}
						});	
						self.$('.dataTables_filter input').on('focus', function(){
							 $(this).tooltip({
							 	placement: 'right',
								title: App.translate('transactions.tooltipQSMinCharactersMSISDN'),
								trigger: 'manual',
								container: '.dataTables_wrapper',
								trigger: 'focus',
							 }).tooltip('show'); 
						});
						self.$('.dataTables_filter input').on('blur', function(){
							 $(this).tooltip('destroy');
						});
						if(!_.isUndefined(callback))
							callback();
		              },
					"createdRow": function( row, data, dataIndex ) {
						if ( data["returnCode"] != "SUCCESS" ) 
							$(row).addClass('danger');
						else if ( data["followUp"] == true ) 
							$(row).addClass('warning');
					},
          			"ajax": function(data, callback, settings) {
          				App.log('fetching data: ' + tableSettings.newurl);
          				
          				self.currentFilter.url = tableSettings.newurl;          				
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(tableSettings.newurl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      		  callback(dataResponse);
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:tdrssearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 5, "desc" ]],	// Column index should be named according to the column
                      "columns": [
                    	   {
                    		   data: "number",
                    		   title: self.i18ntxt.transactionNo,
                   	           render: function(data, type, row, meta) {
                   	        	   return '<a class="routerlink" href="#transaction/' + row['number'] + '">' + data + '</a>';
							   	//return '<a class="" href="#transaction/' + row['number'] + '">' + data + '</a>';
							   },
                    	   },
                    	   {
                    		   data: "transactionTypeName",
                    		   title: self.i18ntxt.type,
                    		   render: function(data, type, row, meta) {
                    			   if (!_.isUndefined(data) && data.length == 2)
                    				   return App.translate("enums.transactionTypeCode." + data, data);
                    			   else
                    				   return App.translate("enums.transactionType." + data, data);
                    			    
                    		   }
                    	   },
                    	   {
                    		   data: "amount",
                    		   title: self.i18ntxt.amount,
							   class: "right",
							   defaultContent: "-",
							   render: function(data, type, row, meta) {
								   return CommonUtils.formatNumber(data);
							   }
								   
                    	   },
                    	   {
                    		   data: "buyerTradeBonusAmount",
                    		   title: self.i18ntxt.buyerTradeBonus,
							   class: "right",
							   defaultContent: "-",
							   render: function(data, type, row, meta) {
								   return CommonUtils.formatNumber(data);
							   }
                    	   },
						   /*
                    	   {
                    		   data: "chargeLevied",
                    		   title: self.i18ntxt.charge,
							   class: "right",
							   defaultContent: "-"
                    	   },
						   */
                    	   {
                    		   data: "channelName",
                    		   title: self.i18ntxt.channel,
							   defaultContent: "-",
							   render: function(data, type, row, meta) {
								   return App.translate("enums.channel." + data, data);
							   }
                    	   },
                    	   {
                    		   data: "endTimeString",
                    		   title: self.i18ntxt.time,
							   render: function(data, type, row, meta) {
							   	   if(type == 'sort') return row['endTime'];
								   return data;
							   }
                    	   },
                    	   {
                    		   //data: "a_AgentID",
                    		   data: "apartyName",
                    		   title: self.i18ntxt.agentA,
							   //sortable: false,
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			if (App.hasPermission("Agent", "View")) {
							   				return '<a class="routerlink" href="#account/' + row['a_AgentID'] + '">' + data + '</a>';	
							   			} else {
							   				return data;
							   			}
							   			
									return '-';	
							   },
                    	   },
                    	   {
                    		   data: "a_MSISDN",
                    		   title: self.i18ntxt.msisdnA,
							   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "bpartyName",
                    		   title: self.i18ntxt.agentB,
							   //sortable: false,
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			if (App.hasPermission("Agent", "View")) {
							   				return '<a class="routerlink" href="#account/' + row['b_AgentID'] + '">' + data + '</a>';
							   			} else {
							   				return data;
							   			}
									return '-';	
							   },
                    	   },
                    	   {
                    		   data: "b_MSISDN",
                    		   title: self.i18ntxt.msisdnB,
							   defaultContent: "-"
                    	   },
			      {
				      data: "itemDescription",
				      title: self.i18ntxt.bundleName,
				      defaultContent: "-"
			      },
                    	   {
                    		   data: "followUp",
							   class: "center",
                    		   title: self.i18ntxt.followUp,
                   	           render: function(data, type, row, meta) {
							   		if ( data == true )
								   		return '<span class="label label-warning">FOLLOW-UP</span>';
									return '-';
							   },
                    	   },
                    	   {
                    		   data: "returnCode",
                    		   title: self.i18ntxt.code,
                   	           render: function(data, type, row, meta) {
							   		return '<span class="label label-' + (data == 'SUCCESS' ? 'success' : 'danger') + '">' + App.translate('enums.returncode.' + data,data) + '</span>';
							   },
                    	   }
                    	  ]
                  } );
            },

            agentSearch: function() {
				//debugger;
            	var self = this;
				let errorPanel = $(".failAgentSearch");
				errorPanel.css("display", "none");

				$('#tdrAgentInfo').html('');

				const msisdn = self.$('.dataTables_filter input').val();

				if (!msisdn) {
					if ( self.dataTable ) self.dataTable.destroy();

            		self.renderTable();
					return;
				}

				self.$('.dataTables_filter input').val('');

            	self.enableSearchButton(false);

	        	var url = 'api/tdr-agent/agents-by-msisdn/'+msisdn;
				let agentStates = { A: 'Active', S: 'Suspended', D: 'Deactivated' };

				$.ajax({
					url,
					success: function(agentsHavingMsisdn) {
						let agentTable = $('.agent-info-table');
						agentTable.text('');
						// 33300105
						if (agentsHavingMsisdn.length === 0) {
							$(".failAgentSearch .error-message").text('There is no agent with MSISDN ' + msisdn);
							errorPanel.css("display", "block");
						} else if (agentsHavingMsisdn.length === 1) {
							// 1 AGENT FOUND -- move directly to TRANSACTION SEARCH
							self.singleAgent = agentsHavingMsisdn[0];
							self.renderTableFromAgent(self.singleAgent);
						} else {
							agentsHavingMsisdn.forEach((agent) => {
								agentTable.append(
										`<tr class="pointer chooseAgent" data-id="${agent.id}">` +
										`<td><i class="fa fa-user fa-3x"></i></td>` +
										`<td><strong>ID</strong><br />${agent.id}</td>` +
										`<td><strong>Full&nbsp;Name</strong><br />${agent.firstName} ${agent.surname}</td>` +
										`<td><strong>Status</strong><br />${agentStates[agent.state]}</td>` +
										'</tr>'
								);
							})
							self.findAgentDialog(msisdn, agentsHavingMsisdn);
							$('.chooseAgent').click(function() {
								$('.chooseAgent').removeClass('selected');
								$(this).addClass('selected');
							});
						}
					},
					error: function(data) {
						App.log(data);
						window.location = window.location.origin+tmp+"/login";
					}
				});

            	return false;
            },

			findAgentDialog: function (msisdn, agents) {
				var self = this;
				App.vent.trigger('application:dialog', {
					name: "AgentsByMSISDNDialog",
					title: "Multiple Agents have transacted using the mobile number " + msisdn + ". Please select an Agent:",
					init: function () {
						$(".failAgentSearch").css("display", "none");
						$(".failAgentSearchDialog").css("display", "none");
					},
					hide: function () {
						console.log('Called Hide');
						// self.dataTable.ajax.reload().draw();
					},
					events: {
						"click .getAgentTransactionsBtn": function (event) {
							let selectedAgentsList = $('.chooseAgent.selected');
							if (selectedAgentsList.length === 0) {
								$(".failAgentSearchDialog .error-message").text('Please select an agent to proceed');
								$(".failAgentSearchDialog").css("display", "block");
								return;
							}

							const agentId = selectedAgentsList.data('id');

							console.log('getAgentTransactionsBtn Clicked for MSISDN: ' + msisdn);
							this.modal("hide");
							self.singleAgent = agents.find((x) => x.id === agentId);
							self.renderTableFromAgent(self.singleAgent);
						}
					}
				});

				return false;
			},

			renderTableFromAgent: function (agent) {

				$('#tdrAgentInfo').html(
					`Transactions for <strong>${agent.firstName} ${agent.surname}</strong> ` +
					`(ID: <strong>${agent.id}</strong>, Mobile Number: <strong>${agent.mobileNumber}</strong>)`
				)

				const url = 'api/tdrs/search?agentID=' + agent.id;

				if ( this.dataTable ) this.dataTable.destroy();

				this.renderTable({}, url, function() {
					// FIXME - the 'search box' does not get hidden, we manually hide it here
					$('.searchResults .dataTables_wrapper').find('.row').first().hide();
				});
				
				$('.searchResults .dataTables_filter label').show();
			},
            
            onRender: function () {
            	this.renderTable();
  		  	}
            
        });
        return TdrTableView;
    });
