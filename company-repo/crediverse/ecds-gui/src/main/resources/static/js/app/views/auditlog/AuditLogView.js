define( ['jquery', 'App', 'backbone', 'marionette', 
         'views/auditlog/AuditLogTableView', 'models/AuditLogModel',
         'utils/CommonUtils', 'datatables'],
    function($, App, BackBone, Marionette, 
    		AuditLogTableView, AuditLogModel, CommonUtils) {
        //ItemView provides some default rendering logic
        var AuditLogView =  AuditLogTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "AuditLogView#auditlogmaster",
  		  	url: 'api/auditlog',
  		  	error: null,
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.auditLog;
  		  		return {
  		  			heading: txt.auditLog,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.auditLog,
  		  				href: window.location.hash,
						iclass: "fa fa-history"
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
        		exportLog: '.exportAuditLogButton'
            },
            
            events: {
            	"click @ui.searchExpand":	'displayAdvancedSearch',
            	"click @ui.search":      	'search',
            	"click @ui.searchReset": 	'searchReset',
            	"click @ui.searchCancel":	'searchCancel',
            	"click @ui.exportLog": 		'exportLog'
            },
            
            displayAdvancedSearch: function(ev) {
            	$('.advancedSearchInput').hide();
            	$('.advancedSearchForm').slideDown({
            		duration: 'slow',
            		easing: 'linear',
            		start: function() {
            			$('.advancedSearchForm #id').focus();
            		},
            		complete: function() {
            			
            		}
            	});
//				App.appRouter.navigate(window.location.hash.replace('/asf!off','/asf!on'), {trigger: false, replace: true});
            },
            
            search: function(ev) {
            	var self = this;
            	self.disableSearchButton();
            	var formData = CommonUtils.getFormData( $("#searchForm") );
            	this.criteria = formData.criteria;
            	var url = this.url + '/search?' + formData.args;
            	App.appRouter.navigate(url, {trigger: false, replace: true});
            	this.dataTable.destroy();
            	self.renderTable(formData.args, url, this.enableSearchButton);
        		return false;            	
            },
            
            searchReset: function(ev) {
            	var self = this;
            	self.enableSearchResetButton(false);
            	var form = self.$('#searchForm')[0];
				form.reset();
				
				// TODO: Put #areaID back in list in future iteration (C1456)
				self.$("#dataType, #action").val("").trigger("change");
            	var ajax = self.dataTable.ajax;
        		ajax.url(this.url).load( function(){
        			self.enableSearchResetButton(true);
        		}, true );
        		return false;
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
//				App.appRouter.navigate(window.location.hash.replace('/asf!on','/asf!off'), {trigger: false, replace: true});
            },  
            
            configureSearchForm:function() {
  		  		var self = this;
  		  		
  		  		this.$('#timestamp-from').datepicker({autoclose: true, todayHighlight: true});
				this.$('#timestamp-to').datepicker({autoclose: true, todayHighlight: true});

				this.$('#dataType').select2({
            		placeholder: App.i18ntxt.enums.dataType.all,
            		ajax: null,
            		data: [
            			{id:"",  text:App.translate("enums.dataType.all")},
            			{id:"Agent",  text:App.translate("enums.dataType.agent")},
            			{id:"AuditEntry",  text:App.translate("enums.dataType.auditEntry")},
            			{id:"Batch",  text:App.translate("enums.dataType.batch")},
            			{id:"Configuration", text:App.translate("enums.dataType.configuration")},
            			{id:"Group", text:App.translate("enums.dataType.group")},
            			{id:"Permission", text:App.translate("enums.dataType.permission")},
            			{id:"Role", text:App.translate("enums.dataType.role")},
            			{id:"ServiceClass", text:App.translate("enums.dataType.serviceClass")},
            			{id:"Tier", text:App.translate("enums.dataType.tier")},
            			{id:"TransferRule", text:App.translate("enums.dataType.transferRule")},
            			{id:"WebUser", text:App.translate("enums.dataType.webUser")}
            		]
            	});
            	this.$('#action').select2({
            		placeholder: App.i18ntxt.enums.action.all,
            		ajax: null,
            		data: [
            			{id:"", text:App.translate("enums.action.all")},
            			{id:"C", text:App.translate("enums.action.create")},
            			{id:"U", text:App.translate("enums.action.update")},
            			{id:"D", text:App.translate("enums.action.delete")}
            		]
            	});
            	
				self.$("#action,#dataType").val("").trigger("change");
//				self.validator = self.$( "#searchform" ).validate({
//					rules: {
//					},
//					messages: {
//					},
//					submitHandler: function(form) {
//						return false;
//					},
//				});
				//checkbox hack:				
				this.$('#withcount').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
				});				
            },
            
            exportLog: function(ev) {
				var table = this.$(this.tableClass);
				var pos = this.url.indexOf('?')
				var baseUrl= (pos >=0)?this.url.substr(0, pos):this.url;
				CommonUtils.exportAsCsv(ev, baseUrl+'/search', this.currentFilter.data, this.criteria, true);				
            },
            
            enableSearchButton: function() {
            	self.$('.searchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            },
            
            disableSearchButton: function() {
            	self.$('.searchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            },
            
            enableSearchResetButton: function(isEnabled) {
            	self.$('.searchResetButton').prop('disabled', !isEnabled);
            },
            
        });
        
        
        return AuditLogView;
    });
