define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 'collections/TierCollection', 'models/AgentModel',
         'utils/HandlebarHelpers', 'models/ReportModel', 'views/reports/RetailerPerformanceReportTableView', 'utils/CommonUtils', 'jquery.maskedinput', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, TierCollection, AgentModel, HBHelper, ReportModel, RetailerPerformanceReportTableView, CommonUtils, maskedinput) {

		var i18ntxt = App.i18ntxt.reports.retailerPerformance;
        var RetailerPerformanceReportView =  RetailerPerformanceReportTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'retailerperformance'
        	},
  		  	template: "Reports#retailerperformance",
  		  	url: 'api/reports/retailerperformance',
  		  	error: null,
  		  	model: null,
			sorting: null,
			id: null,
			waitingToLoad: {
				loadModel: 0, // must be integer
			},
  		  	
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
  		  			heading: txt.retailerPerformance.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
						text: txt.report,
						iclass: "fa fa-file-text"
					},{
  		  				text: txt.retailerPerformance.headingBC,
						href: '#retailerPerformanceReportList',
  		  			}]
  		  		}
  		  	},
        	
            initialize: function (options) {
				if (typeof options !== 'undefined')
					this.id = options.id;
            },

			renderWhenReady: function () {
				this.waitingToLoad.loadModel -= 1;
				if (this.waitingToLoad.loadModel === 0) {
					this.render();
					self.$('#searchform .form-group').removeClass('has-feedback').removeClass('has-success');
				}
			},

			loadModel: function () {
            	var self = this;
            	
				if (self.id != null) {
	            	self.model = new ReportModel({id: self.id, report:'retailerperformance'});
	            	self.model.fetch({
	            		success: function (ev) {
							self.waitingToLoad.loadModel = 1;
							if (self.model.attributes.parameters) {
								var params = self.model.attributes.parameters;
								if (params.relativeTimeRange)
									self.model.set("period", params.relativeTimeRange);
								if (params.timeInterval) {
									var interval = params.timeInterval;
									if (interval.startDate)
										self.model.set("dateFrom", CommonUtils.formatDate(interval.startDate));
									if (interval.endDate)
										self.model.set("dateTo", CommonUtils.formatDate(interval.endDate));
								}

								if (params.filter && params.filter.items.length) {
									/**
									 * FIXME -- ONLY ADDED FOR TESTING, if uncommented .... removed before committing
									 * /
									params.filter.items.push({ field: 'A_MOBILE_NUMBER', value: '8745689' })
									params.filter.items.push({ field: 'A_OWNER_MOBILE_NUMBER', value: '8745689' })
									/** */

									for (var i = 0; i < params.filter.items.length; ++i) {
										var item = params.filter.items[i];
										switch(item.field)
										{
										case "TRANSACTION_TYPE":
											self.model.set("transactionType", item.value); break;
										case "FOLLOW_UP":
											self.model.set("followUp", item.value); break;
										case "TRANSACTION_STATUS":
											self.model.set("transactionStatus", item.value == true ? "true" : "false"); break;
										case "A_AGENT_ID":
											self.loadAgentField("a_AgentID", item.value);
											break;
										case "A_OWNER_ID":
											self.loadAgentField("a_OwnerID", item.value);
											break;
										case "A_MOBILE_NUMBER":
											// DEPRECATED --- not setting this value
											// self.model.set("a_MobileNumber", item.value);
											break;
										case "A_OWNER_MOBILE_NUMBER":
											// DEPRECATED --- not setting this value
											// self.model.set("a_OwnerMobileNumber", item.value);
											break;
										case "A_TIER_NAME":
											self.model.set("a_TierName", item.value); break;
										case "A_GROUP_NAME":
											self.model.set("a_GroupName", item.value); break;
										case "A_SERVICE_CLASS_NAME":
											self.model.set("a_ServiceClassName", item.value); break;
										case "TOTAL_AMOUNT": 
											if (item.operator == "GREATER_THAN_OR_EQUAL") 
												self.model.set("amount_from", item.value); 
											else if (item.operator == "LESS_THAN_OR_EQUAL")
												self.model.set("amount_to", item.value); 
											break;
										}
									}
								}
								
							}
							
							self.loadTiers();
							self.waitingToLoad.loadModel -= 1;
						},
					});
				}	
				else {
	            	self.model = new ReportModel({id: null, report:'retailerperformance'});
					self.loadTiers();
				}	
            	this.model.displayRequiredStars = false;
            	this.model.formGeneralMessage = i18ntxt.formGeneralMessage;
			},

			loadAgentField(fieldName, fieldValue) {
				const self = this;
				this.waitingToLoad.loadModel += 1;
				const agentModel = new AgentModel({ id: fieldValue });
				agentModel.fetch({
					success: function(r) {
						const agent = r.attributes;
						const { id, mobileNumber, firstName, surname } = agent;
						let state  = agent.currentState.charAt(0).toUpperCase() + agent.currentState.slice(1).toLowerCase();
						self.model.set(fieldName, `ID: ${id} | Mobile-Number: ${mobileNumber} | Full-Name: ${firstName} ${surname} | ${state}`);
						self.renderWhenReady();
					}
				})
			},

			loadTiers: function() {
				var self = this;
				self.waitingToLoad.loadModel += 1;
				var tiers = new TierCollection();
            	tiers.fetch({
  		    		success: function(ev){
						function filterTiers(val){
							return (val.type == "R");
						}
						var tierData = tiers.toJSON();
	       				self.model.set('tierList', tierData.filter(filterTiers));
	    				self.renderWhenReady();
						//self.viewReport();
	      	  		}
	         	});
			},
            
            configureSearchForm: function() {
            	var self = this;
            	this.$('#date-from').datepicker({autoclose: true, todayHighlight: true});
				this.$('#date-to').datepicker({autoclose: true, todayHighlight: true});
				this.$('#date-from').mask('9999-99-99');
				this.$('#date-to').mask('9999-99-99');
				//var now = new Date().toJSON().slice(0,10);
				//this.$('#date-from').val(now);
				//this.$('#date-to').val(now);
				var baseUrl = '#retailerPerformanceReport';
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');

				self.$('#period').on('change', function(){
					if( $(this).val() == '' )
						self.$('#period-custom-range').fadeIn();
					else
						self.$('#period-custom-range').fadeOut();
				});
				
            	var groupID = self.$('#a_GroupName');
				var groupAjaxConfig =  self.getGroupConfig('groups');
            	groupID.select2({
            		ajax: groupAjaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.groupNameHint,
            	});
            	groupID.data('config', groupAjaxConfig);

				// //////////////////////////////////////////////////
            	var getIDByMsisdnAjaxConfig =  self.getAjaxConfig('reports/agentsByMsisdn');
            	var a_AgentID = self.$('#a_AgentID');
            	a_AgentID.select2({
            		ajax: getIDByMsisdnAjaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.searchAgentByMsisdnNameHint,
            	});
            	a_AgentID.data('config', getIDByMsisdnAjaxConfig);
				
            	var a_OwnerID = self.$('#a_OwnerID');
            	a_OwnerID.select2({
            		ajax: getIDByMsisdnAjaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.searchOwnerByMsisdnNameHint,
            	});
            	a_OwnerID.data('config', getIDByMsisdnAjaxConfig);
				// //////////////////////////////////////////////////
				
            	var serviceClassID = self.$('#a_ServiceClassName');
            	var scAjaxConfig =  self.getAjaxConfig('serviceclass');
            	serviceClassID.select2({
            		ajax: scAjaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.serviceClassNameHint,
            	});
            	serviceClassID.data('config', scAjaxConfig);
				
            	var tierID  = self.$('#a_TierName');
            	tierID.select2({
            		ajax: null,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.tierNameHint,
            	});
          	
            	setTimeout(function(){
					//if (CommonUtils.urlDecodeForm(pcs, form)) {
					//	//self.displayAdvancedSearch();
					//	self.viewReport({encode: false});
					//}
				}, 0);
			
				//self.validator = self.$( "#searchform" ).validate();
            },
            
			getGroupConfig: function() {
				var self = this;
            	var config = {
            			type: "GET",
            			url: "api/groups/dropdown",
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
               			data: function (params) {
    						if ( self.$('#a_TierName').val() )
    							params.tierID = self.$("#a_TierName option:selected").data('tier-id');
    						return params;
    					},
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: item
                                    }
                                })
                            };
                        }
            		};
            	return config;
            },
            
            getAjaxConfig: function(type) {
            	var config = {
            			type: "GET",
            			url: "api/"+type+"/dropdown",
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    //data: selectedItem,
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: item
                                    }
                                })
                            };
                        }
            		};
            	return config;
            },
            
            onRender: function () {
            	var self = this;
				
				self.configureSearchForm();

				if (self.model == null) {
					self.loadModel();

				} else {
					var searchForm = this.$('form');
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
							case 'A_AGENT_ID': colIx = 0; break;
							case 'DATE': colIx = 1; break;
							case 'TRANSACTION_TYPE': colIx = 2; break;
							case 'TRANSACTION_STATUS': colIx = 3; break;
							case 'A_ACCOUNT_NUMBER': colIx = 4; break;
							case 'A_MOBILE_NUMBER': colIx = 5; break;
							case 'A_IMSI': colIx = 6; break;
							case 'A_NAME': colIx = 7; break;
							case 'A_TIER_NAME': colIx = 8; break;
							case 'A_GROUP_NAME': colIx = 9; break;
							case 'A_SERVICE_CLASS_NAME': colIx = 10; break;
							case 'A_OWNER_IMSI': colIx = 11; break;
							case 'A_OWNER_ID': colIx = 12; break;
							case 'A_OWNER_MOBILE_NUMBER': colIx = 13; break;
							case 'A_OWNER_NAME': colIx = 14; break;
							case 'TOTAL_AMOUNT': colIx = 15; break;
							case 'TOTAL_BONUS': colIx = 16; break;
							case 'TRANSACTION_COUNT': colIx = 17; break;
							case 'A_OWNER_ID': colIx = 18; break;
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
			
					self.$('#transactionType').val(self.model.get("transactionType"));
					self.$('#transactionStatus').val(self.model.get("transactionStatus"));
					self.$('#period').val(self.model.get("period")).trigger('change');
					self.$('#followUp').val(self.model.get("followUp"));
					self.$("#a_TierName").val(self.model.get("a_TierName")).trigger("change");
					self.$("#a_AgentID").val(self.model.get("a_AgentID")).trigger("change");
					self.$("#a_OwnerID").val(self.model.get("a_OwnerID")).trigger("change");
				}

				if (self.id != null) {
					
					var baseUrl = '#retailerPerformanceReport';
					var parameters = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];

					if ( parameters[2] === "edit" ) {
						$('.editReportButton').hide();
					} else {

						self.disableFormContent();
					}
					
				} else {

					$('.editReportButton').hide();
				}
				
            },

            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize($('form'));
            	var args = "";

				// FIXME ...
				//   the a_ServiceClassName is the only one with this problem ...
				//   somehow all other empty fields are "" ... but it is null ...
				//
				// fix null value being converted to string "null"
				//  if null, should become empty string
				criteria.a_ServiceClassName = criteria.a_ServiceClassName || "";

				//             ID: 31; | msisdn: 123456789 | .......
				//                 ^
				// We want the ID
				if (criteria.a_AgentID) {
					criteria.a_AgentID = criteria.a_AgentID.split('|')[0].split(':')[1].replace(/[^0-9]/, '').trim();
				}
				if (criteria.a_OwnerID) {
					criteria.a_OwnerID = criteria.a_OwnerID.split('|')[0].split(':')[1].replace(/[^0-9]/, '').trim();
				}

            	for (var key in criteria) {
					if ( criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            
            exportReport: function(ev) {
				var self = this;
				if (self.$( "#searchform" ).valid()) {
					var table = this.$('.retailerperformancetable');
					var pos = self.url.indexOf('?')
					var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
					CommonUtils.exportAsCsv(ev, baseUrl, {}, self.criteria, true);
				}
            	return false;
			},
            
			resetReport: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
				
				self.$('#name,#description').val('');
				self.$('#a_MobileNumber,#a_OwnerMobileNumber,#date-from,#date-to,#amount-from,#amount-to').val('');
				self.$('#transactionType,#transactionStatus').val('');
				self.$("#a_AgentID,#a_OwnerID,#a_TierName,#a_GroupName,#a_ServiceClassName").val(null).trigger("change");
				self.$('#period').val('').trigger('change');
				self.currentFilter.data = "";
				self.configureSearchForm();
			},

			cancelReport: function(ev) {
				App.appRouter.navigate('#retailerPerformanceReportList', {trigger: true, replace: true});
			},

			saveReport: function(ev) {
            	var self = this;
				self.$('#searchform #report-saved-message').hide();
				self.$('#searchform .form-error-panel').hide();
            	if (self.$( "#searchform" ).valid()) {
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
								App.appRouter.navigate('#retailerPerformanceReportList', {trigger: true, replace: true});
							}, 1500);
							self.id = data;
						},
					})
	            }
            	return false;
			},
            
            viewReport: function(ev) {
            	var self = this;
            	//if (_.isUndefined(ev.encode)) App.appRouter.navigate('#accountSearch' + CommonUtils.urlEncodeForm($('form')), {trigger: false, replace: true});
				var url = self.url+'?'+self.getFormData();
				self.currentFilter.data = self.getFormData();

				if (!this.dataTable) {
					try {
						self.renderTable({
							searchBox: false,
							order: self.sorting,
							newurl: url,
						});
					} catch(err) {
						if (console) console.error(err);
					}
				} else {
					var ajax = this.dataTable.ajax;
					ajax.url(url).load( function(){}, true );
				}
				//$('.advancedSearchResults .dataTables_filter label').show();
				//self.renderTable(self.getFormData());
            	return false;
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
        return RetailerPerformanceReportView;
    });
