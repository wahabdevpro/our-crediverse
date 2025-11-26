define( ['jquery', 'backbone', 'App', 'marionette', 'models/ReportScheduleModel', 'utils/CommonUtils', 'jquery.maskedinput'],
    function($, BackBone, App, Marionette, ReportScheduleModel, CommonUtils, maskedinput) {
        //ItemView provides some default rendering logic
        var ReportScheduleDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'papi/reports',
  		  	template: "Reports#scheduledialogview",
  		  	//model: App.permissions,
  		  	error: null,
			usersShown: null,
			usersSelected: null,
			selectChannels: false,
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            	if (!_.isUndefined(options) && !_.isUndefined(options.selectChannels)) this.selectChannels = options.selectChannels;
            },
				
			showOnMatch: function(self, el, value){
				if ( $(el).find('label').text().toUpperCase().indexOf(value) >= 0 ) {
		            $(el).show();
					self.usersShown++;	
				} else 
					$(el).hide();
			},

            onRender: function () {

				var self = this;

				if ( !self.selectChannels )
					self.$('.channel-selection').remove();
	
				self.$('#period').on('change',function(){
					switch($(this).val()) {
					case 'HOUR': 
						self.$('#timeBlock').hide();
						self.$('#timeIntervalBlock').show();
						break;

					case 'DAY':
					case 'WEEK':
					case 'MONTH':
						self.$('#timeBlock').show();
						self.$('#timeIntervalBlock').hide();
						break;
					}
				});
				
				self.$('#deliveryChannelEmail').val('false');
				self.$('#deliveryChannelSms').val('false');
					
				if(this.model) {
					if(this.model.get('period'))
						self.$('#period').val(this.model.get('period')).trigger('change');
					if(this.model.get('timeOfDay')!=null)
						self.$('#timeOfDayString').val(CommonUtils.formatTimeSecAsHM(this.model.get('timeOfDay')));
					if(this.model.get('startTimeOfDay')!=null)
						self.$('#startTimeOfDayString').val(CommonUtils.formatTimeSecAsHM(this.model.get('startTimeOfDay')));
					if(this.model.get('endTimeOfDay')!=null)
						self.$('#endTimeOfDayString').val(CommonUtils.formatTimeSecAsHM(this.model.get('endTimeOfDay')));
					var users = this.model.get('agentUsers');
					if (users && users.length) {
						for(var i = 0; i < users.length; ++i) {
							self.$('#agent-user-'+users[i].id).prop('checked', true);
						}
						self.usersSelected = users.length;
						self.$('#agent-users-selected').text(self.usersSelected);
					}
					var channels = this.model.get('channels');
					if (_.isUndefined(channels)) channels = 'SMS';
					if (channels) {
						channels = channels.split(',');
						for(var i = 0; i < channels.length; ++i) {
							switch(channels[i]) {
								case 'EMAIL': self.$('#deliveryChannelEmail').prop('checked', true); self.$('#deliveryChannelEmail').val('true'); break;
								case 'SMS': self.$('#deliveryChannelSms').prop('checked', true); self.$('#deliveryChannelSms').val('true'); break;
							}
						}
					}
				}	

				self.$('#agent-users-total').text(self.$('#agent-users-list .agent-user-selector').length);
				self.$('#agent-users-shown').text(self.$('#agent-users-list .agent-user-selector').length);
				
				self.$('.channel-selection input.delivery-channel').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
					if (self.$('.delivery-channel:checked').length !== 0) {
						self.$('#deliveryChannelError').hide();
					}
				});

				self.$('#agent-users-list').on('change', function(){
					self.usersSelected = 0;
					self.$('#agent-users-list input[type="checkbox"]').each(function() { 
						if($(this).is(':checked')) self.usersSelected++;
					});
					self.$('#agent-users-selected').text(self.usersSelected);
				});
				self.$('#timeOfDayString').mask('99:99');
				self.$('#startTimeOfDayString').mask('99:99');
				self.$('#endTimeOfDayString').mask('99:99');
				self.$('#user-filter-input').on('keyup',function(){
					var value = $(this).val().toUpperCase();
					self.usersShown = 0;
					self.$("#agent-users-list > .agent-user-div").each(function() { 
						self.showOnMatch(self, this, value); 
					});
					self.$('#agent-users-shown').text(self.usersShown);
				});

				self.$('.scheduleAddRecipientEmailButton').on('click', function(){
					var div = self.$('#recipientEmailTemplate .row').clone();
					div.find('input[type="email"]').attr('name', 'recipientEmails[]');
					div.appendTo('#recipientEmailsList').find('input').focus();
				});
				self.$('#recipientEmailsList').on('click', '.scheduleRemoveRecipientButton', function(){
					$(this).closest('.row').remove();
				});
            },
            
            ui: {
                view: '',
                save: '.scheduleSubmitButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveSchedule'
            },
            
            saveSchedule: function() {
            	var self = this;
				if (self.$('.delivery-channel').length === 2 && self.$('.delivery-channel:checked').length === 0) {
					self.$('#deliveryChannelError').show();
					return false;
				}

				if (self.$('#recipientEmailsList > div').size() == 0)
					this.model.set('recipientEmails', null);
					
            	this.model.save({
            		success: function(ev){
            			var dialog = self.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		error: function(ev){
            			var dialog = self.$el.closest('.modal');
            			///dialog.modal('hide');
            		}
				});
            }
        });
        return ReportScheduleDialogView;
    });
