define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/TdrModel', 'views/tdrs/TdrReverseView', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, AgentModel, TdrReverseView, CommonUtils) {
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
            	
            	var tableSettings = {
            			searchBox: true,
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
					searching: false,
					pagingType: pagingType,
					bInfo : bInfo,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Transaction #, MSISDN ...",
		            },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
					//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						if (json.recordsTotal > 0)
							self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
						if(!_.isUndefined(callback))
							callback();
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
					  "order": [[ 0, "desc" ]],
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
                    		   title: self.i18ntxt.bonus,
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
                    	   },
                    	   {
                    		   data: "startTimeString",
                    		   title: self.i18ntxt.time,
                    	   },
                    	   {
                    		   //data: "a_AgentID",
                    		   data: "apartyName",
                    		   title: self.i18ntxt.agentA,
							   //sortable: false,
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			return '<a class="routerlink" href="#account/' + row['a_AgentID'] + '">' + data + '</a>';
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
							   			return '<a class="routerlink" href="#account/' + row['b_AgentID'] + '">' + data + '</a>';
									return '-';	
							   },
                    	   },
                    	   {
                    		   data: "b_MSISDN",
                    		   title: self.i18ntxt.msisdnB,
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
							   		return '<span class="label label-' + (data == 'SUCCESS' ? 'success' : 'danger') + '">' + App.translate('enums.returncode.' + data, data) + '</span>';
							   },
                    	   }
                    	  ]
                  });
				
				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#transactionSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');  

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
  		  	}
            
        });
        return TdrTableView;
    });
