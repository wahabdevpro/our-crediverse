define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 'collections/TierCollection', 
         'utils/HandlebarHelpers', 'models/ReportModel', 'views/reports/AccountBalanceSummaryReportTableView', 'utils/CommonUtils', 'jquery.maskedinput', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, TierCollection, HBHelper, ReportModel, AccountBalanceSummaryReportTableView, CommonUtils, maskedinput) {

		var i18ntxt = App.i18ntxt.reports.accountBalanceSummary;
        var AccountBalanceSummaryReportView =  AccountBalanceSummaryReportTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'accountbalancesummary'
        	},
  		  	template: "Reports#accountbalancesummary",
  		  	url: 'api/reports/accountbalancesummary',
  		  	error: null,
  		  	model: null,
			sorting: null,
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
  		  			heading: txt.accountBalanceSummary.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.report,
						iclass: "fa fa-file-text"
  		  			},{
						text: txt.accountBalanceSummary.headingBC,
						href: "#accountBalanceSummaryReportList",
					}]
  		  		}
  		  	},
        	
            initialize: function (options) {
				if (typeof options !== 'undefined')
					this.id = options.id;
            },
            
			loadModel: function () {
            	var self = this;
            	
				if (self.id != null) {
	            	self.model = new ReportModel({id: self.id, report:'accountbalancesummary'});
	            	self.model.fetch({
	            		success: function(ev){
							if (self.model.attributes.parameters) {
								var params = self.model.attributes.parameters;
								console.log( params );
								if (params.filter && params.filter.items.length) {
									for (var i = 0; i < params.filter.items.length; ++i) {
										var item = params.filter.items[i];
										switch(item.field)
										{
										case "TIER_TYPE": self.model.set("tierType", item.value); break;
										case "TIER_NAME": self.model.set("tierName", item.value); break;
										case "GROUP_NAME": self.model.set("groupName", item.value); break;
										case "INCLUDE_ZERO_BALANCE": self.model.set("includeZeroBalance", item.value); break;
										case "INCLUDE_DELETED": self.model.set("includeDeleted", item.value); break;
										case "ACTIVITY_SCALE": console.log(item); self.model.set("activityScale", item.value); break;
										case "ACTIVITY_VALUE": console.log(item); self.model.set("activityValue", item.value); break;
										}
									}
								}
							}
	  		  		
							self.loadTiers();
						}
					});
				}	
				else {
	            	self.model = new ReportModel({id: null, report:'accountbalancesummary'});
					self.loadTiers();
				}	
            	this.model.displayRequiredStars = false;
            	this.model.formGeneralMessage = i18ntxt.formGeneralMessage;
			},

			loadTiers: function() {
				var self = this;
				var tiers = new TierCollection();
            	tiers.fetch({
  		    		success: function(ev){
						var tierData = tiers.toJSON();
	       				self.model.set('tierList', tierData);
	    				self.render();
						//self.viewReport();
	      	  		}
	         	});
			},
            
            configureSearchForm: function() {
            	var self = this;
				var baseUrl = '#accountBalanceSummaryReport';
				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				var form = $('form');

            	var groupID = self.$('#groupName');
				var groupAjaxConfig =  self.getGroupConfig('groups');
            	groupID.select2({
            		ajax: groupAjaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.groupNameHint,
            	});
            	groupID.data('config', groupAjaxConfig);
            	
            	var tierID  = self.$('#tierName');
            	tierID.select2({
            		ajax: null,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.tierNameHint,
            	});
            	
				var tierTypeID  = self.$('#tierType');
            	tierTypeID.select2({
            		ajax: null,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: i18ntxt.tierTypeHint,
            	});
            	
            	var activityScale  = self.$('#activityScale');
            	activityScale.select2({
            		ajax: null,
            		minimumInputLength: 0,
					allowClear: false, 
					sorter: function(data) {return data},
					placeholder: i18ntxt.activityScaleHint,
					minimumResultsForSearch: Infinity
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
    						if ( self.$('#tierName').val() )
    							params.tierID = self.$("#tierName option:selected").data('tier-id');
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
							case 'GROUP_NAME': colIx = 0; break;
							//case 'AGENT_TOTAL_COUNT': colIx = 1; break;
							case 'AGENT_TRANSACTED_COUNT': colIx = 1; break;
							case 'TRANSACTION_COUNT': colIx = 2; break;
							case 'AGENT_AVERAGE_AMOUNT': colIx = 3; break;
							case 'TRANSACTION_AVERAGE_AMOUNT': colIx = 4; break;
							case 'TOTAL_AMOUNT': colIx = 5; break;
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
					} )

					self.$("#tierType").val(self.model.get("tierType")).trigger("change");
					self.$("#tierName").val(self.model.get("tierName")).trigger("change");
					self.$("#activityValue").val(self.model.get("activityValue"));
					var scale = self.model.get("activityScale");
					if (typeof scale !== 'undefined') self.$("#activityScale").val(scale).trigger("change");

					self.$('#includeZeroBalance').on('change',function(){
						$(this).val($(this).is(':checked') ? "1" : "");
					});
					self.$('#activityScale').on('change',function(event){
						var value = $(this).val();
						if (value === 'disabled') {
							console.log('activityScale '+value);
							self.$("#activityValue").prop( "disabled", true );
							self.$("#activityCutOffDiv").prop( "class", "form-group" );
						}
						else {
							self.$("#activityValue").prop( "disabled", false );
							self.$("#activityCutOffDiv").prop( "class", "form-group required dialog-spacing" );
						}
						//console.log($(this).val());
					});
					var value = $('#activityScale').val();
					if (value === 'disabled') {
						self.$("#activityValue").prop( "disabled", true );
					}
					else {
						self.$("#activityValue").prop( "disabled", false );
					}
				
				}
				
				if (self.id != null) {
					
					var baseUrl = '#accountBalanceSummaryReport';
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
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" && criteria[key] !== null ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            
            exportReport: function(ev) {
				var self = this;
				self.criteria = Backbone.Syphon.serialize($('form'));
				if (self.$( "#searchform" ).valid()) {
					var table = this.$('.accountbalancesummarytable');
					var pos = self.url.indexOf('?')
					var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
					CommonUtils.exportAsCsv(ev, baseUrl, {}, self.criteria);
				}
            	return false;
			},
            
			resetReport: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
				self.$('#name,#description').val('');
				self.$("#tierType,#tierName,#groupName").val(null).trigger("change");
				self.currentFilter.data = "";
				self.configureSearchForm();
			},
			cancelReport: function(ev) {
				App.appRouter.navigate('#accountBalanceSummaryReportList', {trigger: true, replace: true});
			},

			saveReport: function(ev) {
            	var self = this;
				self.$('#searchform #report-saved-message').hide();
				self.$('#searchform #report-error-message').hide();
				//debugger;
            	if (self.$( "#searchform" ).valid()) {
            		var valid = true;
            		var activityScale  = self.$('#activityScale').val();
            		if (typeof activityScale !== 'undefined' && activityScale !== 'disabled') {
            			var activityValue  = self.$('#activityValue').val().trim();
            			if (typeof activityValue !== 'string' || activityValue.length < 1) {
            				valid = false;
            				
                			self.$('#searchform #report-error-message').show().find('.alert').text(i18ntxt.activityCutoffInvalid);
                			self.$('#activityValue').focus();
            			}
            		}

            		if (valid) {
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
									App.appRouter.navigate('#accountBalanceSummaryReportList', {trigger: true, replace: true});
								}, 1500);
    							self.id = data;
    						},
						});
            		}
            		
	            }
            	return false;
			},
            
            viewReport: function(ev) {
            	var self = this;
            	//if (_.isUndefined(ev.encode)) App.appRouter.navigate('#accountSearch' + CommonUtils.urlEncodeForm($('form')), {trigger: false, replace: true});
            	var valid = true;
        		var activityScale  = self.$('#activityScale').val();
        		if (typeof activityScale !== 'undefined' && activityScale !== 'disabled') {
        			var activityValue  = self.$('#activityValue').val().trim();
        			if (typeof activityValue !== 'string' || activityValue.length < 1) {
        				valid = false;
        				
            			self.$('#searchform #report-error-message').show().find('.alert').text(i18ntxt.activityCutoffInvalid);
            			self.$('#activityValue').focus();
        			}
        		}
        		if(valid) {
  		  			try {
  		  				self.renderTable({
  		  					searchBox: false,
							order: self.sorting,
  		  				});
  		  			} catch(err) {
  		  				if (console) console.error(err);
  		  			}
	            	var ajax = this.dataTable.ajax;
	        		var url = self.url+'?'+self.getFormData();
					self.currentFilter.data = self.getFormData();
	        		ajax.url(url).load( function(){}, true );
	        		self.$('#searchform #report-error-message').hide();
	        		//$('.advancedSearchResults .dataTables_filter label').show();
	            	//self.renderTable(self.getFormData());
        		}
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
        return AccountBalanceSummaryReportView;
    });
