define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/ReportModel', 'models/ReportScheduleModel', 'models/ReportScheduleExecuteModel', 
		 'views/reports/ReportScheduleDialogView', 'views/reports/ReportScheduleExecuteDialogView',
		 'utils/CommonUtils', 'collections/UserCollection', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, ReportModel, ReportScheduleModel, ReportScheduleExecuteModel, 
		ReportScheduleDialogView, ReportScheduleExecuteDialogView, 
		CommonUtils, UserCollection) {
        //ItemView provides some default rendering logic
        var ReportListTableView =  Marionette.ItemView.extend( {

			formatSchedule: function(data, channelSelection, options) {
				var output = '';

				let excludeHourlyHTMLData = '';
				let { excludeHourly } = options || {};
				if (excludeHourly === true) {
					excludeHourlyHTMLData = 'data-exclude-hourly="true"';
				}

				$.each(data, function(){
					var channelIcons = '';
					if (channelSelection) {
						var channels = this.channels;
						if (_.isUndefined(channels)) channels = 'SMS';	
						channels = channels.split(',');
						for (var i=0; i<channels.length; ++i) {
							switch(channels[i]) {
								case 'EMAIL': if(channelIcons) channelIcons += '&nbsp;'; channelIcons += '<i class="fa fa-envelope-o" style="font-size:90%; position:relative; top:-1px;"></i>'; break;
								case 'SMS': if(channelIcons) channelIcons += '&nbsp;'; channelIcons += '<i class="fa fa-mobile-phone"></i>'; break;
							}
						}
						if ( channelIcons !== '' ) channelIcons = ' ('+channelIcons+')';
					}
					output += '<div data-schedule-id="'+this.id+'" class="report-schedule-block ' + (this.enabled ? '' : 'report-schedule-disabled') + '" style="margin-bottom:5px;">' + 
						'<div>' +
						'<i class="fa fa-clock-o"></i> <strong>' + this.description  + '</strong>' + channelIcons + ': <span class="text-muted">sent</span> <strong>' + App.translate("reports.schedule.period"+this.period, this.period) + '</strong> ';
					if(this.period=='HOUR' && this.startTimeOfDay!=null && this.endTimeOfDay!=null) 
						output += ' <span class="text-muted">from</span> <strong>' + CommonUtils.formatTimeSecAsHM(this.startTimeOfDay) + '</strong> <span class="text-muted">to</span> <strong>' + CommonUtils.formatTimeSecAsHM(this.endTimeOfDay) + '</strong>';
					else if(this.period=='HOUR' && this.startTimeOfDay!=null) 
						output += ' <span class="text-muted">from</span> <strong>' + CommonUtils.formatTimeSecAsHM(this.startTimeOfDay) + '</strong>';
					else if(this.period=='HOUR' && this.endTimeOfDay!=null) 
						output += ' <span class="text-muted">to</span> <strong>' + CommonUtils.formatTimeSecAsHM(this.startTimeOfDay) + '</strong>';
					else if(this.timeOfDay!=null)
						output += ' <span class="text-muted">at or after</span> <strong>' + CommonUtils.formatTimeSecAsHM(this.timeOfDay) + '</strong>';
					output += '&nbsp;&nbsp;<a href="javascript:void(0)" class="btn btn-xs btn-primary executeSchedule" style="padding:1px 2px; font-size:11px;"><i class="fa fa-flash fa-fw"></i></a>';
					output += '<a href="javascript:void(0)" '+(channelSelection ? 'data-channel-selection="1"' : '')+' class="btn btn-xs btn-primary updateSchedule" '+excludeHourlyHTMLData+' style="padding:1px 2px; font-size:11px;"><i class="fa fa-gear fa-fw"></i></a>';
					if ( this.originator != 'MINIMUM_REQUIRED_DATA' ) { 
						output += '<a href="javascript:void(0)" class="btn btn-xs btn-danger deleteSchedule" style="padding:1px 2px; font-size:11px;"><i class="fa fa-times fa-fw"></i></a>';
					}	
					output += '</div>';
					if (this.webUsers && this.webUsers.length) {
						output += '<div class="fadeout-bottom-wrapper" style="max-height:110px;">';
						output += '<div class="faded-user-list-div" style="padding-left:20px; line-height:22px; max-height:110px; overflow:hidden;">';
						for(var i = 0; i < this.webUsers.length; ++i) {
							output += '<div><i class="fa fa-user"></i> '+this.webUsers[i].firstName+' '+this.webUsers[i].surname+'</div>';
						}
						output += '</div>';
						if (this.webUsers.length > 5) {
							output += '<div class="fadeout-bottom" style="text-align:center;"><span style="position:relative; top:32px;"><a href="javascript:void(0)"><i class="fa fa-arrow-circle-down fa-lg"></i>&nbsp;&nbsp;&nbsp;see all '+this.webUsers.length+' users&nbsp;&nbsp;&nbsp;<i class="fa fa-arrow-circle-down fa-lg"></i></a></span></div>';
							output += '<div class="fadeout-collapse" style="display:none; text-align:center;"><span style="position:relative; top:32px;"><a href="javascript:void(0)"><i class="fa fa-arrow-circle-up fa-lg"></i>&nbsp;&nbsp;&nbsp;see less users&nbsp;&nbsp;&nbsp;<i class="fa fa-arrow-circle-up fa-lg"></i></a></span></div>';
						}	
						output += '</div>';
					}	
					output += '</div>';
				});
			   return output;
			},
            
            deleteSchedule: function(ev) {
				ev.preventDefault();
				ev.stopPropagation();
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var block = $(ev.currentTarget).closest('.report-schedule-block');
				var scheduleId = block.data('schedule-id');
            	var data = this.dataTable.row(row).data();
				var schedule = null;
				for (var i = 0; i < data.schedules.length; ++i){
					if(data.schedules[i].id==scheduleId) {
						schedule = data.schedules[i];
						break;
					}
				}
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.reports.reportSchedule,
	        		url: self.urlSchedule+'/'+data.id+'/schedule/'+scheduleId,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.reports.reportSchedule,
	        			name: schedule.description,//data.name,
	        			//description: '',//data.description
	        		},
	        		rowElement: null,//row,
	        	}, {
	        		success: function(model, response) {
		            	block.fadeOut("slow", function() {
		            		//block.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            },
			
			executeSchedule: function(ev) {
				ev.preventDefault();
				ev.stopPropagation();
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var block = $(ev.currentTarget).closest('.report-schedule-block');
				var scheduleId = block.data('schedule-id');
            	var data = this.dataTable.row(row).data();
				var schedule = null;
				for (var i = 0; i < data.schedules.length; ++i){
					if(data.schedules[i].id==scheduleId) {
						schedule = data.schedules[i];
						break;
					}
				}

            	var model = new ReportScheduleModel({
            		url: self.urlSchedule+'/'+data.id+'/schedule/'+scheduleId+'/execute',
					reportType: self.reportType,
					reportId: data.id,
					reportName: data.name,
					scheduleId: scheduleId,
            	});
            	model.set(schedule);
            	model.set('editMode', false);

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.reports.schedule.headingExecute,
            		hide: function() {
	        		},
            		view: ReportScheduleExecuteDialogView,
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
			
			updateSchedule: function(ev) {
				ev.preventDefault();
				ev.stopPropagation();
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var block = $(ev.currentTarget).closest('.report-schedule-block');
				var scheduleId = block.data('schedule-id');
            	var data = this.dataTable.row(row).data();
				var schedule = null;
				for (var i = 0; i < data.schedules.length; ++i){
					if(data.schedules[i].id==scheduleId) {
						schedule = data.schedules[i];
						break;
					}
				}

            	var model = new ReportScheduleModel({
            		url: self.urlSchedule+'/'+data.id+'/schedule/'+scheduleId,
					reportType: self.reportType,
					reportId: data.id,
					reportName: data.name,
					scheduleId: scheduleId,
            	});

            	var excludeHourly = $(ev.currentTarget).data('excludeHourly');
				if (excludeHourly === true) {
            		model.set('excludeHourly', true);
				}

            	model.set(schedule);
            	model.set('editMode', true);
            	model.set('userList', self.model.get("userList"));

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.reports.schedule.headingUpdate,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: ReportScheduleDialogView,
            		params: {
            			model: model,
						selectChannels: $(ev.currentTarget).data('channel-selection') == '1'
            		}
            	});
            	return false;
            },
            
			addSchedule: function(ev) {
				ev.preventDefault();
				ev.stopPropagation();
            	var self = this;

            	var row = $(ev.currentTarget).closest('tr');
            	var data = this.dataTable.row(row).data();
            	var model = new ReportScheduleModel({
            		url: self.urlSchedule+'/'+data.id+'/schedule',
					reportType: self.reportType,
					reportId: data.id,
					reportName: data.name,
					scheduleId: null,
            	});
            	//model.set(data);
            	var excludeHourly = $(ev.currentTarget).data('excludeHourly');
				if (excludeHourly === true) {
            		model.set('excludeHourly', true);
				}
            	model.set('editMode', false);
            	model.set('userList', self.model.get("userList"));

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.reports.schedule.headingAdd,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: ReportScheduleDialogView,
            		params: {
            			model: model,
						selectChannels: $(ev.currentTarget).data('channel-selection') == '1'
            		}
            	});
            	return false;
            }
        });
        return ReportListTableView;
    });
