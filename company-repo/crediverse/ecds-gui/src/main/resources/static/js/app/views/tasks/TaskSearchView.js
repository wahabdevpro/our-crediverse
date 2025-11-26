define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.tasks;
	
        var TaskSearchView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	template: "Tasks#taskSearch",
			baseUrl: 'api/tasks',
			currentFilter: {}, // Used to keep track of filter settings for use by export.
        	attributes: {
        		class: "row"
        	},
  		  	
            onRender: function () {
            	var self = this;
            	
            	var table = this.$('.tasktable');
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
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
          				var testData = [{
          					time: "now",
          					taskId: "21",
          					description: "task 21",
          					from: "John",
          					status: "NEW"
          				},{
          					time: "now",
          					taskId: "22",
          					description: "task 22",
          					from: "John 2",
          					status: "NEW"
          				}];
          				callback({
          					data: testData,
          					recordsTotal: 2,
          					recordsFiltered: 2
          					});
          				/*App.log('fetching data: ' + self.url);
          				self.currentFilter.url = self.url;
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(self.url, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
//						  console.dir( dataResponse);
                      	    callback(dataResponse);
							table.DataTable()
							   .columns.adjust()
							   .responsive.recalc();
                      	  })
                      	  .fail(function(dataResponse) {
                      			self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:accountssearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });*/
                      },
					  "language": {
					  	"emptyTable": i18ntxt.emptyTableMessage
					  },
					  "order": [[ 0, "desc" ]],
					  "columns": [
									{
										   data: "time",
										   title: i18ntxt.timeTitle,
										   defaultContent: "(none)"
									},
		                    	   {
		                    		   data: "taskId",
		                    		   title: i18ntxt.taskIdTitle,
									   defaultContent: "(none)",
									   class: "all",
									   render: function(data, type, row, meta) {
										   return '<a class="routerlink" href="#task/' + row['id'] + '">' + data + '</a>';
									   }
		                    	   },
		                    	   
		                    	   {
		                    		   data: "description",
		                    		   defaultContent: "(none)",
		                    		   title: i18ntxt.descriptionTitle
		                    	   },
		                    	   {
		                    		   data: "from",
		                    		   title: i18ntxt.fromTitle,
									   defaultContent: "(none)"
		                    	   },
		                    	   {
		                    		   data: "status",
		                    		   title: i18ntxt.statusTitle,
									   sortable: false,
									   defaultContent: "(none)"
		                    	   },
		                    	   {
		                   	            targets: -1,
		                   	            data: null,
		                   	            title: "",
									    className: "right all",
										width: "108px",
		                   	            sortable: false,
		                   	            render: function(data, type, row, meta) {
		                   	            	var buttons = [];
		                   	            	/*if ((data.tierType == "WHOLESALER") || (data.tierType == "STORE")) 
		                   	            		buttons.push("<li style='cursor:pointer;'><a class='performTransferButton'><i class='fa fa-fw fa-random'></i>&nbsp;&nbsp;"+i18ntxt.menuTransferBtn+"</a></li>");
		                   	            	if (data.tierType != "ROOT") 
		                   	            		buttons.push("<li style='cursor:pointer;'><a class='performAdjustmentButton'><i class='fa fa-fw fa-money'></i>&nbsp;&nbsp;"+i18ntxt.menuAdjustBtn+"</a></li>");
											else	
		                   	            		buttons.push("<li style='cursor:pointer;'><a class='performReplenishButton'><i class='fa fa-fw fa-money'></i>&nbsp;&nbsp;"+i18ntxt.menuReplenishBtn+"</a></li>");
											if (row.currentState!=="PERMANENT") {
		                   	           			buttons.push("<li style='cursor:pointer;'><a class='pinResetButton'><i class='fa fa-fw fa-key'></i>&nbsp;&nbsp;"+i18ntxt.menuPinResetBtn+"</a></li>");
												buttons.push('<li role="separator" class="divider"></li>');
											}	
		                   	        		buttons.push("<li style='cursor:pointer;'><a class='editAgentButton'><i class='fa fa-fw fa-pencil'></i>&nbsp;&nbsp;"+i18ntxt.menuEditBtn+"</a></li>");
											if (row.currentState!=="PERMANENT") {
												buttons.push('<li role="separator" class="divider"></li>');
												if (row.currentState==="ACTIVE") 
			                   	            		buttons.push("<li style='cursor:pointer;'><a class='suspendAgentButton'><i class='fa fa-fw fa-ban'></i>&nbsp;&nbsp;"+i18ntxt.menuSuspendBtn+"</a></li>");
												if (row.currentState==="SUSPENDED") 
			                   	            		buttons.push("<li style='cursor:pointer;'><a class='unsuspendAgentButton'><i class='fa fa-fw fa-check'></i>&nbsp;&nbsp;"+i18ntxt.menuUnsuspendBtn+"</a></li>");
												if (row.currentState==="DEACTIVATED") 
			                   	            		buttons.push("<li style='cursor:pointer;'><a class='reactivateAgentButton'><i class='fa fa-fw fa-check'></i>&nbsp;&nbsp;"+i18ntxt.menuReactivateBtn+"</a></li>");
												if (row.currentState!=="DEACTIVATED")
			           	        	           		buttons.push("<li style='cursor:pointer;'><a class='deactivateAgentButton'><i class='fa fa-fw fa-trash-o'></i>&nbsp;&nbsp;"+i18ntxt.menuDeactivateBtn+"</i></a></li>");
											}	*/	
										
											var html = '';
											html += '<div class="btn-group">';
											html += ' <a class="btn btn-primary btn-xs routerlink" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#account/'+row['id']+'">'+i18ntxt.menuViewBtn+'</a>';
											html += ' <button type="button" class="btn btn-primary btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="margin-right:0;">';
											html += '    <span class="caret"></span>';
											html += ' </button>';
											html += ' <ul class="dropdown-menu dropdown-menu-right">';
											html += buttons.join(' ');
											html += ' </ul>';
											html += '</div>';

		            	            		return html;
		            	            	}
		                   	        }
		                    	  ]
		                  })
		                  
		                  .on('xhr.dt', function ( e, settings, json, xhr ) {
		                	  //self.currentFilter.url = self.url;
		        				//self.currentFilter.data = data;
		        				
		        				/*var jqxhr = $.ajax(self.url, {
		        					data: data
		        				})
		                    	  .done(function(dataResponse) {
		                    	    callback(dataResponse);
		                    	  })
		                    	  .fail(function(dataResponse) {
		                    		  self.error = dataResponse;
		                    		App.error(dataResponse);
		                    	  })
		                    	  .always(function(data) {
		                    	  });*/
						    } );

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

            ui: {
                createAgent: '.createAgentButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.createAgent": 'createAgent'
            }
        });
        return TaskSearchView;
    });
