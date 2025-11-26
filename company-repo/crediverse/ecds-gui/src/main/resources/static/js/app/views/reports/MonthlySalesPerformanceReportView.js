define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 'collections/TierCollection', 
         'utils/HandlebarHelpers', 'views/reports/MonthlySalesPerformanceReportTableView', 'utils/CommonUtils', 'jquery.maskedinput', 'models/ReportModel', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, TierCollection, HBHelper, MonthlySalesPerformanceReportTableView, CommonUtils, maskedinput, ReportModel ) {

		var i18ntxt = App.i18ntxt.reports.monthlySalesPerformance;
        var MonthlySalesPerformanceReportView =  MonthlySalesPerformanceReportTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'monthlysalesperformance'
        	},
  		  	template: "Reports#monthlysalesperformance",
  		  	url: 'api/reports/monthlysalesperformance',
  		  	error: null,
  		  	model: null,
			sorting: [],
			id: null,
  		  	
			ui: {
				viewReport: '.viewReportButton',
				saveReport: '.saveReportButton',
				scheduleReport: '.scheduleReportButton',
				exportReport: '.exportReportButton',
				resetReport: '.resetReportButton',
				cancelReport: '.cancelReportButton',
				editReportButton: '.editReportButton'
				
			},
			
			// View Event Handlers
			events: {
				"click @ui.viewReport": 'viewReport',
				"click @ui.saveReport": 'saveReport',
				"click @ui.scheduleReport": 'scheduleReport',
				"click @ui.exportReport": 'exportReport',
				"click @ui.resetReport": 'resetReport',
				"click @ui.cancelReport": 'cancelReport',
				"click @ui.editReportButton": 'enableFormContent',
				
			},
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.reports;
  		  		return {
  		  			heading: txt.monthlySalesPerformance.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
						text: txt.report,
						iclass: "fa fa-file-text"
					},{
  		  				text: txt.monthlySalesPerformance.headingBC,
						href: '#monthlySalesPerformanceReportList',
  		  			}]
  		  		}
  		  	},
        	
            initialize: function (options) {
				if (typeof options !== 'undefined'){
					if(!_.isUndefined(options.id)) {
						this.id = options.id;
					}
				}
            },

			loadModel: function () {
            	var self = this;
				if (self.id != null) {
	            	self.model = new ReportModel({id: self.id, report:'monthlysalesperformance'});
	            	self.model.fetch({
	            		success: function(ev){
							if (self.model.attributes.parameters) {
								var params = self.model.attributes.parameters;
								self.model.set("period", params.relativeTimeRange);
								if (!_.isUndefined(params.filter) && !_.isUndefined(params.filter.items) && params.filter.items.length > 0) {
									for (var i = 0; i < params.filter.items.length; ++i) {
										var item = params.filter.items[i];
										switch(item.field)
										{
										case 'TRANSACTION_TYPES':self.model.set("transactionTypes", item.value); break;
										case 'TRANSACTION_STATUS':self.model.set("transactionStatus", item.value); break;
										case 'TIERS':self.model.set("tiers", item.value); break;
										case 'GROUPS':self.model.set("groups", item.value); break;
										case 'AGENTS':self.model.set("agents", item.value); break;
										case 'OWNER_AGENTS':self.model.set("ownerAgents", item.value); break;
										}
									}
								}
							}
							self.render();
						}
					});
				}	
				else {
	            	self.model = new ReportModel({id: null, report:'monthlysalesperformance'});
				}	
            	this.model.displayRequiredStars = false;
            	this.model.formGeneralMessage = i18ntxt.formGeneralMessage;
			},

			resetModel: function () {
            	var self = this;
				if (self.id != null) {
	            	self.model = new ReportModel({id: self.id, report:'monthlysalesperformance'});
	            	self.model.set("period", null);
					self.model.set("transactionTypes", null);
					self.model.set("transactionStatus", null);
					self.model.set("tiers", null);
					self.model.set("groups", null);
					self.model.set("agents", null);
					self.model.set("ownerAgents", null);
				}	
				else {
	            	self.model = new ReportModel({id: null, report:'monthlysalesperformance'});
				}	
            	this.model.displayRequiredStars = false;
            	this.model.formGeneralMessage = i18ntxt.formGeneralMessage;
			},

			mapFunction: function (item, i) {
                return {text: item, id: item}
			},

            configureSearchForm: function() {
            	var self = this;
				var baseUrl = '#monthlySalesPerformanceReport';
				//var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
            	if(!_.isUndefined(this.model) && this.model != null && !_.isUndefined(this.model.attributes) && this.model.attributes != null) {
            		var periodElem = this.$('#period');
            		periodElem.val(this.model.attributes.period);
            		var transactionTypesElem = this.$('#transactionTypes');
            		if(!_.isUndefined(this.model.attributes.transactionTypes) && this.model.attributes.transactionTypes != null)
            			transactionTypesElem.val(this.model.attributes.transactionTypes);
            		var transactionStatusElem = this.$('#transactionStatus');
            		if(!_.isUndefined(this.model.attributes.transactionStatus) && this.model.attributes.transactionStatus != null)
            			transactionStatusElem.val(this.model.attributes.transactionStatus + "");
            	}
            	this.getSelect2Data("groups", "api/groups/dropdown", 0, i18ntxt.groupNameHint, self.mapFunction);
            	this.getSelect2Data("tiers", "api/tiers/dropdown", 0, i18ntxt.tierNameHint, self.mapFunction);
            	this.getSelect2Data("ownerAgents", "api/agents/dropdown/msisdn", 5, i18ntxt.ownerAgentHint, self.mapFunction);
            	this.getSelect2Data("agents", "api/agents/dropdown/msisdn", 5, i18ntxt.agentHint, self.mapFunction);
            },
            
            getSelect2Data: function(elementID, ajaxurl, minLength, placeholderText, mapFunction) {
				var jqElement = this.$('#' + elementID);
            	var ajaxConfig = {
            			type: "GET",
            		    url: ajaxurl,
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    //data: selectedItem,
                        processResults: function (data) {
                            var result = {
                                results: $.map(data, function (item, i) {
                                	if(!_.isUndefined(mapFunction)){
                                		return mapFunction(item, i);
                                	} else return { text: item, id: i }
                                })
                            };
                            return result;
                        }
            		};
				jqElement.data('config', ajaxConfig);
				jqElement.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: minLength,
					allowClear: true, 
					placeholder: placeholderText,
            	});
				/*.on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});*/
				return jqElement;
			},
			
			getPeriodConfig: function() {
				var self = this;
            	var config = {
            			type: "GET",
            			url: "api/config/transactions",
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
               			data: function (params) {
    						return params;
    					},
                        processResults: function (data) {
                        	var months = data.olapTransactionRetentionDays / 30;
                        	var resultArr = [];
                        	for(var i = 0; i < Math.min(2, months); i++){ //Remove min limit when other periods are supported.
                        		switch(i)
                        		{
                        		//case 0: resultArr.push({ text : i18ntxt.periods.currentMonth, id: i.toString()});CURRENT_MONTH
                        		case 0: resultArr.push({ text : i18ntxt.periods.currentMonth, id: "CURRENT_MONTH"});
                        		break;
                        		//case 1: resultArr.push({ text : i18ntxt.periods.lastMonth, id: i.toString()});
                        		case 1: resultArr.push({ text : i18ntxt.periods.lastMonth, id: "PREVIOUS_MONTH"});
                        		break;
                        		default: resultArr.push({ text : i + " " + i18ntxt.periods.monthsAgo, id: "MONTHS_AGO_" + i.toString() });
                        		}
                        	}
                            return {results: resultArr}
                        }
            		};
            	return config;
            },
            
            onRender: function () {
            	var self = this;
            	self.configureSearchForm();
            	var searchForm = self.$('form');
            	if(_.isUndefined(self.model) || self.model == null){            		
            		self.loadModel();
					this.model.bind(searchForm);
            		
            	} else {

					this.model.bind(searchForm);	    		
					// deal with restoring sort order
					var params = this.model.get('parameters');
					self.sorting = [];
					if(params && params.sort) {
						for(var i = 0; i < params.sort.items.length; ++i) {
							var sort = params.sort.items[i];
							var colIx = -1;
							switch(sort.field)
							{
							case 'month': colIx = 0; break;
							//case 'AGENT_TOTAL_COUNT': colIx = 1; break;
							case 'groupName': colIx = 1; break;
							case 'ownerMsisdn': colIx = 2; break;
							case 'msisdn': colIx = 3; break;
							case 'totalAmount': colIx = 4; break;
							}
							var colDir = 'asc';
							switch(sort.operator)
							{
							case 'ASCENDING': colDir = 'asc'; break;
							case 'DESCENDING': colDir = 'desc'; break;
							}
							if(colIx !== -1)
								self.sorting.push([colIx, colDir]);
						}
					}	
					if( self.sorting.length == 0 ) self.sorting.push([0,'desc']);

					this.$el.on( 'error.dt', function ( e, settings, techNote, message ) {
						var jqXhr = settings.jqXHR;
						
						if (!_.isUndefined(jqXhr)) {
							$.proxy(self.model.defaultErrorHandler(jqXhr), self.model);
						}
					} );
				
				}

				
				if (self.id != null) {
					
					var baseUrl = '#monthlySalesPerformanceReport';
					var parameters = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];

					if ( parameters[2] === "edit" ) {

						$('.editReportButton').hide();
					} else {

						self.disableFormContent();
					}
					
				} else {

					setTimeout(function() {
						$('.editReportButton').hide();
					}, 100);
				}

            },

            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize($('form'));
            	var args = "";
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" && criteria[key] !== null ) {
    					if (args != "") args += "&";
    					var value = "";
    					if(Array.isArray(criteria[key])){
    						value = JSON.stringify(criteria[key]);
    					} else {
    						value = criteria[key];
    						//value = criteria[key]
    					}
   					args += key + "=" + encodeURIComponent(value);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            
			exportReport: function(ev) {
				var self = this;
            	self.$('#searchform #report-saved-message').hide();
				self.$('#searchform #report-error-message').hide();
				self.$('.form-error-panel').hide();
				self.initializeValidation();
            	$('#name').rules('add', {required: false});
            	$('#description').rules('add', {required: false});
				if (self.$( "#searchform" ).valid()) {
					var table = this.$('.monthlysalesperformancetable');
					var pos = self.url.indexOf('?')
					var formData = self.getFormData();
	        		var baseUrl = (pos >=0)?self.url.substr(0, pos):self.url;
	        		var url = baseUrl + '?' + formData;
					CommonUtils.exportAsCsv(ev, url, {}, self.criteria, true);
				}
            	return false;
			},
            
			resetReport: function(ev) {
				var self = this;
				var form = self.$('form')[0];
				form.reset();
				self.$('#name,#description').val('');
				self.$("#tiers,#groups,#ownerAgents,#agents").val(null).trigger("change");
				self.$("#period,#transactionTypes,#transactionStatus").val('').trigger("change");
				var url = self.url;
				self.currentFilter.data = "";
				self.resetModel();
				self.configureSearchForm();
				self.initializeValidation();
			},

			cancelReport: function(ev) {
				App.appRouter.navigate('#monthlySalesPerformanceReportList', {trigger: true, replace: true});
			},

			saveReport: function(ev) {
				var self = this;
				self.initializeValidation();
				self.$('#searchform #report-saved-message').hide();
				self.$('#searchform .form-error-panel').hide();
				if (self.$("#searchform" ).valid()) {
					/**
					 * FIXME -	Data is passed into the URL instead of the body
					 * 			It should be in the body, not url.
					 */
					var dtData = '&' + (!_.isUndefined(self.currentFilter.dtData) ? $.param(self.currentFilter.dtData) : '');
					var url = self.url+(self.id != null ? '/'+self.id : '')+'?'+self.getFormData() + dtData;

					self.model.mode = 'create';
					self.model.url = url;
					self.model.save({
						success: function(data) {
							self.$('#searchform #report-saved-message').show(); //.delay(5000).fadeOut();
							self.$('#searchform .saveReportButton').attr("disabled", true);
							setTimeout(() => {
								App.appRouter.navigate('#monthlySalesPerformanceReportList', {trigger: true, replace: true});
							}, 1500);
							self.id = data;
						},
					});
				}
				return false;
			},
            
            viewReport: function(ev) {
				var self = this;
				self.$('#searchform #report-saved-message').hide();
				self.$('#searchform #report-error-message').hide();
				self.$('.form-error-panel').hide();
				self.initializeValidation();
            	$('#name').rules('add', {required: false});
            	$('#description').rules('add', {required: false});
            	if (self.$( "#searchform" ).valid()) {
            		try {
            			self.renderTable({
            				searchBox: false/*,
            				order: self.sorting,*/
		  				});
		  			} catch(err) {
		  				if (console) console.error(err);
		  			}
	            	var ajax = this.dataTable.ajax;
	            	var formData = self.getFormData();
	            	var url = self.url+'?' + formData;
					self.currentFilter.data = formData;
	        		ajax.url(url).load( function(){}, true );
            	}
            	return false;
            },
			
            initializeValidation: function(ev) {
            	$('form').validate();
				$('#name').rules('add', {required: true});
				$('#description').rules('add', {required: true});
				$('#period').rules('add', {required: true});
				$('#transactionTypes').rules('add', {required: true});
				$('#transactionStatus').rules('add', {required: true});
            },

			enableFormContent: function(){
				$("input, select, option, textarea", "#searchform").prop('disabled',false);
				$('.viewReportButton').show();
				$('.saveReportButton').show();
				$('.scheduleReportButton').show();
				$('.exportReportButton').show();
				$('.resetReportButton').show();
				$('.editReportButton').hide();
			},

			disableFormContent: function(){
				$("input, select, option, textarea", "#searchform").prop('disabled',true);
				$('.viewReportButton').hide();
				$('.saveReportButton').hide();
				$('.scheduleReportButton').hide();
				$('.exportReportButton').hide();
				$('.resetReportButton').hide();
			}
        });
        return MonthlySalesPerformanceReportView;
    });
