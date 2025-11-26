define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 
	'utils/CommonUtils', 'datatables',],
    function($, _, App, BackBone, Marionette,
    		CommonUtils) {
        //ItemView provides some default rendering logic
        var TestInfoView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	template: "TestInfo#testinfo",
        	
        	attributes: {
        		class: "row",
        		id: "testinfo"
        	},
        	
        	breadcrumb: function() {
  		  		var txt = "Test Info";
  		  		return {
  		  			heading: txt,
  		  			breadcrumb: [{
  		  				text: txt,
  		  				href: "#testinfo",
  		  				iclass: "fa fa-users"
  		  			}]
  		  		}
  		  	},
            
            renderTable: function(options) {
            	App.log( 'rendering table' );
            	var self = this;
            	var table = this.$('.datatablesview');
            	
            	var jqxhr = $.ajax("api/tdrs/rawheadings")
            	  .done(function(headings) {
            		var columns = [], j=0;;
            		for (i=0; i<headings.length; i++) {
            			columns[j++] = {
            					'data': headings[i],
            					'title': headings[i],
            					'class': 'testformat'
            			}
            		}
            		  
            		this.dataTable = table.DataTable({
                		//'stateSave': 'hash',
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
    					"search": false,
    					"language": {
    		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
    						"searchPlaceholder": "Batch filename",
    		            },
    		            "order": [[ 0, "desc" ]],
    					"serverSide": true,
    		            
    		            "ajax": function(data, callback, settings) {
    		            	self.$('.dataTables_filter').hide();
    		            	var serverData = {
    		            			length: data.length,
    		            			start: data.start,
    		            			search: data.search
    		            	};
              				var jqxhr = $.ajax('api/tdrs/raw', {
              					data: serverData
              				})
                          	  .done(function(dataResponse) {
                          	    callback(dataResponse);
                          	  })
                          	  .fail(function(dataResponse) {
                          		  self.error = dataResponse;
                          			App.error(dataResponse);
                          	  })
                          	  .always(function(data) {
                          	  });
                          },
    		            
    		            
                          "columns": columns
              		});
            		
            		
            	  })
            	  .fail(function(dataResponse) {
            	  })
            	  .always(function(data) {
            	  });
            	
            	
            	
            	/*var jqxhr = $.ajax("api/tdrs/rawheadings")
              	  .done(function(headings) {
              		var columns = [], j=0;;
              		for (i=0; i<headings.length; i++) {
              			columns[j++] = {
              					'data': headings[i],
              					'title': headings[i],
              					'class': 'testformat'
              			}
              		}
              		  
              		
              		
              	  })
              	  .fail(function(dataResponse) {
              	  })
              	  .always(function(data) {
              	  });*/

            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            onRender: function () {
            	this.renderTable();

            	var $heading = this.$('#headingslink');
            	var $transaction = this.$('#transactionlink');

            	$heading.text(window.location.protocol + '://'+ window.location.hostname+ '/api/tdrs/rawheadings');
            	$heading.attr('href', '/api/tdrs/rawheadings');
            	$transaction.text(window.location.protocol + '://'+ window.location.hostname+ '/api/tdrs/raw');
            	$transaction.attr('href', '/api/tdrs/raw');
  		  	}
            
        });
        return TestInfoView;
    });
