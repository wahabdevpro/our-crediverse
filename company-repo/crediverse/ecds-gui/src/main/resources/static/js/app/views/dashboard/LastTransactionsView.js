define( ['jquery', 'App', 'backbone', 'marionette', 'views/dashboard/LastTransactionsView', 'models/AgentModel'],
    function($, App, BackBone, Marionette, LastTransactionsView, AgentModel) {
        //ItemView provides some default rendering logic
        var LastTransactionsView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
        	
        	ui: {
                //replenish: '.replenishButton'
        	},
        	events: {
            	//"click @ui.replenish": 'replenishLastTransactions',
        	},
        	
  		  	template: "ManageDashBoard#lastTransactionsView",
  		  	url: 'api/tdrs/last',
            
  		  	initialize: function (options) {
  		  		var self = this;
            },
            
            onRender: function () {

            	App.log( 'rendering table' );
            	var self = this;
            	
            	var table = this.$('.tdrstable');
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
					"dom": '<t>',
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
          				App.log('fetching data: ' + self.url);	
          				var jqxhr = $.ajax(self.url, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      		if (console) console.dir( dataResponse);
                      	    callback(dataResponse);
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "language": {
					  	"emptyTable": "No transactions found."
					  },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "number",
                    		   title: "Transaction #",
                   	           render: function(data, type, row, meta) {
							   		return '<a class="routerlink" href="#transaction/' + row['number'] + '">' + data + '</a>';
							   },
                    	   },
                    	   {
                    		   data: "transactionTypeName",
                    		   title: "Type",
                    	   },
                    	   {
                    		   data: "amount",
                    		   title: "Amount",
							   class: "right",
							   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "buyerTradeBonusAmount",
                    		   title: "Bonus",
							   class: "right",
							   defaultContent: "-"
                    	   },
						   /*
                    	   {
                    		   data: "chargeLevied",
                    		   title: "Charge",
							   class: "right",
							   defaultContent: "-"
                    	   },
						   */
                    	   {
                    		   data: "channelName",
                    		   title: "Channel",
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "endTimeString",
                    		   title: "Time",
                    	   },
                    	   {
                    		   //data: "a_AgentID",
                    		   data: "apartyName",
                    		   title: "(A) Agent",
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			return '<a class="routerlink" href="#account/' + row['a_AgentID'] + '">' + data + '</a>';
									return '-';	
							   },
                    	   },
                    	   {
                    		   data: "a_MSISDN",
                    		   title: "(A) MSISDN",
							   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "bpartyName",
                    		   title: "(B) Agent",
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			return '<a class="routerlink" href="#account/' + row['b_AgentID'] + '">' + data + '</a>';
									return '-';	
							   },
                    	   },
                    	   {
                    		   data: "b_MSISDN",
                    		   title: "(B) MSISDN",
							   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "returnCode",
                    		   title: "Code",
                   	           render: function(data, type, row, meta) {
							   		return '<span class="label label-' + (data == 'SUCCESS' ? 'success' : 'danger') + '">' + data + '</span>';
							   },
                    	   },
						   /*
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
							    className: "right",
                   	            sortable: false,
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	           		buttons.push("<button class='btn btn-primary performViewButton btn-xs'>View</button>");
            	            		return buttons.join(' ');
            	            	}
                   	        }
							*/
                    	  ]
                  } );
            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
  		  	},
        });
        return LastTransactionsView;
    });
