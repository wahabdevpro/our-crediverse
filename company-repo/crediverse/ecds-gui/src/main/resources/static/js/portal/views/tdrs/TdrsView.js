define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/TdrModel', 'views/tdrs/TdrTableView', 'utils/CommonUtils', 'clockpicker', 'jquery.maskedinput', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, TdrModel, TdrTableView, CommonUtils, clockpicker, maskedinput) {
        

		var i18ntxt = App.i18ntxt.transactions;
        var TdrsView =  TdrTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'tdrsmaster'
        	},
  		  	template: "Tdrs#tdrsmaster",
  		  	url: 'papi/tdrs',
  		  	error: null,
  		  	model: new TdrModel(),
  		  	
			ui: {
				exportTdrs: '.exportTdrsButton',
				searchInput: '.advancedSearchInput .searchInput',
				search: '.tdrSearchButton',
				tdrSearchExpand: '.tdrSearchExpandButton',
        		tdrSearchCancel: '.tdrSearchCancelButton',
        		tdrSearchReset: '.tdrSearchResetButton',
			},
			
			// View Event Handlers
			events: {
				"click @ui.exportTdrs": 'exportTdrs',
				"focus @ui.searchInput": 'displayAdvancedSearch',
				"click @ui.search": 'tdrSearch',
				"click @ui.tdrSearchExpand": 'displayAdvancedSearch',
				"click @ui.tdrSearchCancel": 'tdrSearchCancel',
				"click @ui.tdrSearchReset": 'tdrSearchReset',
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
				this.$('#time-from').val('00:00');
				this.$('#time-to').val('23:59');
				this.$('#date-from').mask('9999-99-99');
				this.$('#date-to').mask('9999-99-99');
				this.$('#time-from').mask('99:99');
				this.$('#time-to').mask('99:99');
				this.$('.time-from-picker').clockpicker({
					placement: 'left',
					align: 'top',
					autoclose: true,
					donetext: 'Done',
				});
				this.$('.time-to-picker').clockpicker({
					placement: 'left',
					align: 'top',
					autoclose: true,
					donetext: 'Done',
				});
				var baseUrl = '#transactionList';
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');

				var groupIDA = this.$('#group-id-a');
				var ajaxConfig = {
            			type: "GET",
            		    url: "api/groups/dropdown",
            		    dataType: 'json',
            		    delay: 250,
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: i
                                    }
                                })
                            };
                        }
            		};
				groupIDA.data('config', ajaxConfig);
            	groupIDA.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.selectGroupHintA,
            	})//Selectbox workaround:
				//Stop the selectbox from dropping down when it is cleared.
				.on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});
				//Selectbox workaround end.
				
				var groupIDB = this.$('#group-id-b');
				groupIDB.data('config', ajaxConfig);
            	groupIDB.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.selectGroupHintB,
            	})//Selectbox workaround:
				//Stop the selectbox from dropping down when it is cleared.
				.on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});
				//Selectbox workaround end.;
            	
            	var tierIDA = this.$('#a_TierID');
            	var tierIDB = this.$('#b_TierID');
            	var ajaxTierConfig = {
            			type: "GET",
            		    url: "api/tiers/dropdown",
            		    dataType: 'json',
            		    delay: 250,
            		    processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: i
                                    }
                                })
                            };
                        }
            		};
            	
            	tierIDA.data('config', ajaxTierConfig);
            	tierIDA.select2({
            		ajax: ajaxTierConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.transactionTierA,
            	}).on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});
            	
            	tierIDB.data('config', ajaxTierConfig);
            	tierIDB.select2({
            		ajax: ajaxTierConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.transactionTierB,
            	}).on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});
            	
            	setTimeout(function(){
					if (CommonUtils.urlDecodeForm(pcs, form)) {
						self.tdrSearch({encode: false});
					}
				}, 0);

            	//checkbox hack:
				this.$('#withcount').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
				});
				
				this.$('#withquery').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
				});
							
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
            		//alert('got event');
                } )
            	this.configureSearchForm();
  		  		/*try {
  		  			self.renderTable({
  		  				searchBox: false
  		  			});
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}*/
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
            
            verifyCriteria: function(criteria) {
            	var result = false;
            	var count = 0;
            	/*
            	 * For performance reasons, verify that at least 1 criteria has been set.
            	 */
            	if (!_.isUndefined(criteria) || _.isObject(criteria)) {
            		App.log(JSON.stringify(criteria, null, 2));
            		for (var property in criteria) {
            		    if (criteria.hasOwnProperty(property)) {
            		        switch (property) {
            		        case "relation":
            		        case "withcount":
            		        case "withquery":
            		        	break;
            		        default:
            		        	if (criteria[property].trim().length > 0)
            		        		count++;
            		        	break;
            		        }
            		    }
            		}
            		result = (count > 0);
            	}
            	
            	return result;
            },
            
            exportTdrs: function(ev) {
				var self = this;
				if (self.$( "#searchform" ).valid()) {
					var table = this.$('.tdrstable');
					var pos = self.url.indexOf('?')
					var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
					CommonUtils.exportAsCsv(ev, baseUrl+'/search', self.currentFilter.data, self.criteria, true);
				}
				return false;
			},
            
			tdrSearchCancel: function(ev) {
            	var self = this;
				this.hideAdvancedSearch();
				self.model.formCleanup();
            	return false;
            },
			
			tdrSearchReset: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
				self.$("#group-id-a,#group-id-b,#a_TierID,#b_TierID").val(null).trigger("change");
				self.$('.advancedSearchResults').hide();
        		//self.model.formCleanup();
			},
			
			tdrSearch: function(ev) {
            	var self = this;
            	if (self.$( "#searchform" ).valid()) {
            		self.disableTdrSearchButton();
	        		var url = self.url+'/search?advanced=true&'+self.getFormData();
	        		if (self.verifyCriteria(self.criteria)) {
	        			App.appRouter.navigate(url, {trigger: false, replace: true});
		        		if (!_.isUndefined(self.dataTable)) self.dataTable.destroy();
		        		var options = self.getFormData()
		        		self.renderTable(options, url, self.enableTdrSearchButton);
		        		$('.advancedSearchResults .dataTables_filter label').show();
		        		self.$('.advancedSearchResults').show();
	        		}
	        		else {
	        			CommonUtils.showOkDialog({
							title: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaTitle),
							text: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaText),
							callback: function() {
								self.enableTdrSearchButton();
							}
						});
	        		}
	            }
            	return false;
            },
            
            enableTdrSearchButton: function(isEnabled) {
           		self.$('.tdrSearchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            },
            
            disableTdrSearchButton: function() {
           		self.$('.tdrSearchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            }
        });
        return TdrsView;
    });
