define( ['jquery', 'backbone', 'App', 'marionette', 'models/ReportScheduleExecuteModel', 'utils/CommonUtils', 'jquery.maskedinput',  'moment'],
    function($, BackBone, App, Marionette, ReportScheduleExecuteModel, CommonUtils, maskedinput, moment) {
        //ItemView provides some default rendering logic
        var ReportScheduleExecuteDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'papi/reports',
  		  	template: "Reports#scheduleexecutedialogview",
  		  	//model: App.permissions,
  		  	error: null,
			usersShown: null,
			usersSelected: null,
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            },
				
            onRender: function () {

				var self = this;

				var dnow = moment.utc().format('YYYY-MM-DD');
				var tnow = moment.utc().format('HH:mm:ss');
				self.$('#referenceDt').val(dnow);
				self.$('#referenceTm').val(tnow);
				self.$('#referenceDt').mask('9999-99-99');
				self.$('#referenceTm').mask('99:99:99');
			
				if(this.model) {
					
				}	
            },
            
            ui: {
                view: '',
                save: '.executeSubmitButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'executeSchedule'
            },
            
            executeSchedule: function() {
            	var self = this;
				self.$('#execution-result').hide();
				self.$('.executeSubmitButton').prop('disabled', true);
				this.model.set( 'referenceDate', self.$('#referenceDt').val() + 'T' + self.$('#referenceTm').val() );
            	this.model.save({
            		success: function(model, response){
						self.$('.executeSubmitButton').prop('disabled', false);
						if ( response.returnCode == 'SUCCESS' ) { 
							if ( response.executed ) {
								self.$('#execution-result .alert').addClass('alert-success').removeClass('alert-danger');
								self.$('#execution-result .alert').html(App.translate("reports.schedule.scheduleExecuted"));
							} else {
								self.$('#execution-result .alert').addClass('alert-warning').removeClass('alert-success');
								self.$('#execution-result .alert').html(App.translate("reports.schedule.notExecutedReason." + response.notExecutedReason, App.translate("reports.schedule.scheduleNotExecuted")));
							}
						} else {
							self.$('#execution-result .alert').addClass('alert-danger').removeClass('alert-success');
							self.$('#execution-result .alert').html(App.translate("reports.schedule.scheduleExecuteError"));
						}
						self.$('#execution-result').show();
            			//dialog.modal('hide');
            		},
            		error: function(ev){
						self.$('.executeSubmitButton').prop('disabled', false);
						self.$('#execution-result .alert').addClass('alert-danger').removeClass('alert-success');
						self.$('#execution-result .alert').html(App.translate("reports.schedule.scheduleExecuteError"));
            			///dialog.modal('hide');
            		}
				});
            }
        });
        return ReportScheduleExecuteDialogView;
    });
