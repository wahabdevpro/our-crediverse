define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/BatchModel', 'views/batch/BatchTableView', 'utils/CommonUtils', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, BatchModel, BatchTableView, CommonUtils) {
        

		var i18ntxt = App.i18ntxt.transactions;
        var BatchHistoryView =  BatchTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		   id: 'batchhistorymaster'
        	},
  		  	template: "Batch#batchhistorymaster",
  		  	url: 'api/batch',
  		  	error: null,
  		  	model: new BatchModel(),
  		  	
			ui: {
				exportBatchHistory: '.exportBatchHistoryButton',
				searchInput: '.advancedSearchInput .searchInput',
				search: '.batchHistorySearchButton',
				batchHistorySearchExpand: '.batchHistorySearchExpandButton',
        		batchHistorySearchCancel: '.batchHistorySearchCancelButton',
        		batchHistorySearchReset: '.batchHistorySearchResetButton',
        		viewBatchHistoryButton: '.viewBatchHistoryButton'
			},
			
			// View Event Handlers
			events: {
				"click @ui.exportBatchHistory": 'exportBatchHistory',
				"focus @ui.searchInput": 'displayAdvancedSearch',
				"click @ui.search": 'batchHistorySearch',
				"click @ui.batchHistorySearchExpand": 'displayAdvancedSearch',
				"click @ui.batchHistorySearchCancel": 'batchHistorySearchCancel',
				"click @ui.batchHistorySearchReset": 'batchHistorySearchReset',
				"click @ui.viewBatchHistoryButton": 'viewBatchHistory',
			},
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.transactions;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.historyHeading,
  		  				href: "#transactionList",
						iclass: "fa fa-history"
  		  			}]
  		  		}
  		  	},
  		  	
  		  displayAdvancedSearch: function(ev) {
          	$('.advancedSearchInput').hide();
          	$('.advancedSearchForm').slideDown({
          		duration: 'slow',
          		easing: 'linear',
          		start: function() {
          			$('.advancedSearchForm #msisdn-a').focus();
          		},
          		complete: function() {
          			
          		}
          	});
			App.appRouter.navigate(window.location.hash.replace('/asf!off','/asf!on'), {trigger: false, replace: true});

          	/*$('.advancedSearchForm').fadeIn({
          		duration: '5000',
          		easing: 'easeInBounce'
          	});*/
          	
          },
  		  	
            hideAdvancedSearch: function(ev) {
            	$('.advancedSearchInput').show().focus();
            	$('.advancedSearchForm').slideUp({
            		duration: 'slow',
            		easing: 'linear',
            		start: function() {
            		},
            		complete: function() {
            		}
            	});
				App.appRouter.navigate(window.location.hash.replace('/asf!on','/asf!off'), {trigger: false, replace: true});
            },
        	
            initialize: function (options) {
            	this.model.displayRequiredStars = false;
            	this.model.formGeneralMessage = i18ntxt.formGeneralMessage;
            },
            
            configureSearchForm: function() {
            	var self = this;
            	this.$('#date-from').datepicker({autoclose: true, todayHighlight: true});
				this.$('#date-to').datepicker({autoclose: true, todayHighlight: true});
				var now = new Date().toJSON().slice(0,10);
				this.$('#date-from').val(now);
				this.$('#date-to').val(now);
				var baseUrl = '#batchHistory';
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');
				
				$('input[data-toggle="tooltip"]').tooltip({
     				placement: "top",
	      			trigger: "manual",
					container:'.advancedSearchForm',
					template: '<div class="tooltip" role="tooltip"><div class="tooltip-inner"></div></div>',
		  		});
            	
            	setTimeout(function(){
					if (CommonUtils.urlDecodeForm(pcs, form)) {
						self.displayAdvancedSearch();
						self.batchHistorySearch({encode: false});
					}
				}, 0);
			
				//self.validator = self.$( "#searchform" ).validate();
            },
            
            onRender: function () {
            	var self = this;
	    		var searchForm = this.$('form');
	    		this.model.bind(searchForm);
            	this.$el.on( 'error.dt', function ( e, settings, techNote, message ) {
            		var jqXhr = settings.jqXHR;
            		
            		
            		if (!_.isUndefined(jqXhr)) {
            			$.proxy(self.model.defaultErrorHandler(jqXhr), self.model);
            		}
                } )
            	this.configureSearchForm();
  		  		try {
  		  		self.renderTable({
  		  			searchBox: false
  		  		});
  		  			
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
            },
            
            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize($('form'));
            	var args = "";
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            
            exportBatchHistory: function(ev) {
				var self = this;
				if (self.$( "#searchform" ).valid()) {
					var table = this.$('.batchhistorytable');
					var pos = self.url.indexOf('?')
					var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
					CommonUtils.exportAsCsv(ev, baseUrl, self.currentFilter.data, self.criteria);
				}
            	return false;
			},
            
			batchHistorySearchCancel: function(ev) {
            	var self = this;
				this.hideAdvancedSearch();
				self.model.formCleanup();
            	return false;
            },
			
			batchHistorySearchReset: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
            	var ajax = self.dataTable.ajax;
        		var url = self.url;
        		ajax.url(url).load( function(){}, true );
        		self.model.formCleanup();
			},
            
            batchHistorySearch: function(ev) {
            	var self = this;
            	//if (_.isUndefined(ev.encode)) App.appRouter.navigate('#accountSearch' + CommonUtils.urlEncodeForm($('form')), {trigger: false, replace: true});
            	if (self.$( "#searchform" ).valid()) {
	            	var ajax = this.dataTable.ajax;
	        		var url = self.url+'?'+self.getFormData();
	        		ajax.url(url).load( function(){}, true );
	        		$('.advancedSearchResults .dataTables_filter label').show();
	            	//self.renderTable(self.getFormData());
	            }
            	return false;
            },
            
            viewBatchHistory: function(ev) {
            	alert('csv view');
            	return false;
            }
        });
        return BatchHistoryView;
    });
