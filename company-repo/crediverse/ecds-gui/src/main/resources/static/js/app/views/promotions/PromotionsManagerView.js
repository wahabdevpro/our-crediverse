define( ['jquery', 'underscore', 'App', 'marionette', 
         'views/promotions/PromotionsDialogView', 'models/PromotionModel',
         'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, 
    		PromotionsDialogView, PromotionModel,
    		CommonUtils) {
        //ItemView provides some default rendering logic
		
		var i18ntxt =  App.i18ntxt.promotions;
		
        var PromotionsManagerView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManagePromotions#managepromotions",
  		  	url: 'api/promos',
  		  	error: null,
  		  	dataTable: null,
  		  	currentFilter: {}, // Used to keep track of filter settings for use by export.
  		  
  		  	breadcrumb: function() {
  		  		return {
  		  			heading: i18ntxt.heading,
  		  			defaultHome: false,
					
  		  			breadcrumb: [{
  		  				text: i18ntxt.heading,
  		  				href: window.location.hash,
						iclass: "fa fa-trophy"
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
			},
            
            ui: {
            	searchExpand: '.searchExpandButton',
        		search:       '.searchButton',
        		searchReset:  '.searchResetButton',
        		searchCancel: '.searchCancelButton',
                
                createPromo: '.createPromotionButton',
                editPromo:   '.editPromotionButton',
                deletePromo: '.deletePromotionButton',
                exportPromo: '.exportPromotionButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.searchExpand": 'displayAdvancedSearch',
            	"click @ui.search":      'search',
            	"click @ui.searchReset": 'searchReset',
            	"click @ui.searchCancel": 'searchCancel',
            	
            	"click @ui.createPromo": 'addPromotion',
            	"click @ui.editPromo":   'editPromotion',
            	"click @ui.deletePromo": 'deletePromotion',
            	"click @ui.exportPromo": 'exportPromotion'
            },
            
        	renderTable: function(options) {
				$(window).load(this.dropupAdjust);
				$(window).bind('resize scroll touchstart touchmove mousewheel', this.dropupAdjust);
            	
				var self = this;
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url
            	};
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);

            	var table = this.$('.accountstable');
            	this.dataTable = table.DataTable({
            		//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					stateSave: 'hash',
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},
					processing: true,
					//"searching": false,
//					"serverSide": true,
					autoWidth: false,
					stateSave: 'hash',
					responsive: true,
					language: {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Account #, Mobile Number, Agent Name ...",
		            },
		            "initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
		              },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
          			"ajax": function(data, callback, settings) {
          				App.log('fetching data: ' + tableSettings.newurl);
          				self.currentFilter.url = tableSettings.newurl;
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(tableSettings.newurl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
//						  console.dir( dataResponse);
                      	    callback(dataResponse);
							table.DataTable()
							   .columns.adjust()
							   .responsive.recalc();
							self.dropupAdjust();
                      	  })
                      	  .fail(function(dataResponse) {
                      			self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:accountsterror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    		{
                    		   	data: "id",
                    		   	title: App.i18ntxt.global.uniqueID,
								class: "all center",
								width: "80px",
                    		},
                    	   {
                    		   data: "name",
                    		   title: i18ntxt.name,
							   class: "all",
                    		   render: function(data, type, row, meta) {
                    			   return '<a class="routerlink" href="#promotion/' + row['id'] + '">' + data + '</a>';
                    		   }
                    	   },
                    	   {
                    		   data: "startTime",
                    		   title: i18ntxt.startTime,
                    		   render: function(data, type, row, meta) {
                    			   if (_.isUndefined(data))
                    				   return "-";
                    			   else
                    				   return CommonUtils.timeStampToISODateTime(data);
                    		   }                    		   
                    	   },
                    	   {
                    		   data: "endTime",
                    		   title: i18ntxt.endTime,
                    		   render: function(data, type, row, meta) {
                    			   if (_.isUndefined(data))
                    				   return "-";
                    			   else
                    				   return CommonUtils.timeStampToISODateTime(data);
                    		   }   
                    	   },
                    	   {
                    		   data: "targetAmount",
                    		   title: i18ntxt.targetAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "targetPeriod",
                    		   title: i18ntxt.targetPeriod,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
								   switch(data) {
									   case 1:
										   return App.i18ntxt.enums.period.perDay;
									   case 2:
										   return App.i18ntxt.enums.period.perWeek;
									   case 3:
										   return App.i18ntxt.enums.period.perMonth;
									   case 11:
										   return App.i18ntxt.enums.period.perCalendarDay;
									   case 12:
										   return App.i18ntxt.enums.period.perCalendarWeek;
									   case 13:
										   return App.i18ntxt.enums.period.perCalendarMonth;
									   default:
										   return "-";
								   } 
                    		   }
                    	   },
                    	   {
                    		   data: "rewardAmount",
                    		   title: i18ntxt.rewardAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "rewardPercentage",
                    		   title: i18ntxt.rewardPercentage,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data * 100);
                    		   }
                    	   },
                    	   {
                    		   data: "state",
                    		   title: i18ntxt.state,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.renderStatus(data);
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
                   	            sortable: false,
								class: "nowrap right all",
								width: "85px",
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if (App.hasPermission("Promotion", "Update")) { 
                   	            		buttons.push("<button class='btn btn-primary editPromotionButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	if (App.hasPermission("Promotion", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deletePromotionButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	}
            	            		return buttons.join('');
            	            	}
                    	   }
                     	  ]
                   })

 				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#accountSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');  
 				
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
            	var self = this;
            	
            	this.$('#transferRuleID').select2({
            		url: "api/transfer_rules/dropdown",
            		placeholder: App.i18ntxt.promotions.anyRule,
            		ajax: self.defaultSelectDataProcessor,
            		minimumInputLength: 0,
					allowClear: true, 
            	});
            	var areaElement = this.$('#areaID');
            	CommonUtils.configureSelect2Control({
					jqElement: areaElement,
            		url: "api/areas/dropdown",
            		placeholderText: i18ntxt.areaPlaceHolder,
					isHtml: true
            	});
            	
            	this.$('#serviceClassID').select2({
            		url: "api/serviceclass/dropdown",
            		placeholder: App.i18ntxt.promotions.anyServiceClass,
            		ajax: self.defaultSelectDataProcessor
            	});
            	
            	this.$('#bundleID').select2({
            		url: "api/bundles/dropdown",
            		placeholder: App.i18ntxt.promotions.anyBundle,
            		ajax: self.defaultSelectDataProcessor
            	});

            	this.$('#state').select2({
            		placeholder: App.i18ntxt.agentAccounts.searchAccountStatusHint,
            		ajax: null,
            		data: [
            			{id:"A", text:App.translate("enums.state.active")},
            			{id:"D", text:App.translate("enums.state.deactivated")}
            		]
            	});
            	
            	//targetPeriod
            	this.$('#targetPeriod').select2({
            		placeholder: App.i18ntxt.promotions.anyPeriod,
            		ajax: null,
            		data: [
            			{id:"1",  text:App.translate("enums.period.perDay")},
            			{id:"2",  text:App.translate("enums.period.perWeek")},
            			{id:"3",  text:App.translate("enums.period.perMonth")},
            			{id:"11", text:App.translate("enums.period.perCalendarDay")},
            			{id:"12", text:App.translate("enums.period.perCalendarWeek")},
            			{id:"13", text:App.translate("enums.period.perCalendarMonth")}
            		]
            	});

            	self.$("#state,#transferRuleID,#areaID,#serviceClassID,#bundleID").val("").trigger("change");
            	this.renderTable();            	
            },
            
			dropupAdjust: function() {
				$(".tableview .dropdown-toggle").each(CommonUtils.adjustDropdownDir);
			},
			
            defaultSelectDataProcessor : {
    			processResults : function(data) {
                	return {
                		results: $.map(data, function (item, i) {
                			return {
                				id: i,
                				text: item,
                        		item: item
                            }
                        })
                    };
                },
            },
            
            displayAdvancedSearch: function(ev) {
            	$('.advancedSearchInput').hide();
            	$('.advancedSearchForm').slideDown({
            		duration: 'slow',
            		easing: 'linear',
            		start: function() {
            			$('.advancedSearchForm #firstName').focus();
            		},
            		complete: function() {
            			
            		}
            	});
				App.appRouter.navigate(window.location.hash.replace('/asf!off','/asf!on'), {trigger: false, replace: true});
            },
            
            search: function(ev) {
            	var self = this;
            	self.enableSearchButton(false);
            	var formData = CommonUtils.getFormData( $("#promoSearchForm") );
            	this.criteria = formData.criteria;
        		
            	var url = this.url + '?' + formData.args;
        		this.dataTable.ajax.url(url).load( function(){
        			self.enableSearchButton(true);
        		}, true );
            	
        		return false;            	
            },
            
            enableSearchButton: function(isEnabled) {
            	if(isEnabled)
            		self.$('.searchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            	else
            		self.$('.searchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            },
            
            disableSearchButton: function() {
            	self.$('.searchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            },
            
            searchReset: function(ev) {
            	var self = this;
            	self.enableSearchResetButton(false);
            	var form = self.$('#promoSearchForm')[0];
				form.reset();
				
				self.$("#state, #transferRuleID, #areaID, #serviceClassID, #bundleID").val("").trigger("change");
            	var ajax = self.dataTable.ajax;
        		var url = this.url;
        		ajax.url(url).load( function(){
        			self.enableSearchResetButton(true);
        		}, true );
            },

            enableSearchResetButton: function(isEnabled) {
            	self.$('.searchResetButton').prop('disabled', !isEnabled);
            },

            searchCancel: function(ev) {
				this.hideAdvancedSearch();
            	return false;            	
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
            
            promoSearch: function(ev) {
            	var ajax = this.dataTable.ajax;
        		var url = self.url + '?' + this.getFormData();
        		ajax.url(url).load( function(){}, true );
            	return false;
            },
            
            addPromotion: function(ev) {
            	var self = this;
            	
            	var model = new PromotionModel();
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg modal-xl',
					title: i18ntxt.addDialogTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: PromotionsDialogView,
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
            
            editPromotion: function(ev) {
            	var self = this;
            	var tableData = this.dataTable.row($(ev.currentTarget).closest('tr')).data();
            	var model = new PromotionModel(tableData);

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg modal-xl',
            		view: PromotionsDialogView,
            		title:CommonUtils.renderHtml( i18ntxt.editDialogTitle, {name: model.get("name")} ),
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
            
            deletePromotion: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.promotions.promotion,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.promotions.promotion,
	        			name: data.name,
	        			description: data.name
	        		},
	        		rowElement: row,
	        	},
	        	{
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            	return false;
            },
            
            exportPromotion: function(ev) {
				var table = this.$('.accountstable');
				var pos = this.url.indexOf('?')
				var baseUrl= (pos >=0)?this.url.substr(0, pos):this.url;
				CommonUtils.exportAsCsv(ev, baseUrl+'/search', this.currentFilter.data, this.criteria);				
            },
            
        });
        return PromotionsManagerView;
    });
