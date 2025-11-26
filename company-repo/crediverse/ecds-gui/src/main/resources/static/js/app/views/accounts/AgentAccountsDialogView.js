define( ['jquery', 'App', 'marionette', 'models/AgentWarningModel', 'views/accounts/WarningDialogView', 'utils/CommonUtils', 'jquery.select2'],
    function($, App, Marionette, AgentWarningModel, WarningDialogView, CommonUtils) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;
        var AgentAccountsDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
  		  	template: "AgentAccounts#agentAccountDialog",
  		  	//url: 'api/agents',
  		  	error: null,
			tierList: null,
			roleList: null,
			savedDomainAccountName: "",
			initialize: function () {
            	if(!_.isUndefined(this.model) && !_.isUndefined(this.model.attributes) && !_.isUndefined(this.model.attributes.domainAccountName)) {
            		this.savedDomainAccountName = this.model.attributes.domainAccountName;
            	}
            },
            
			onRender: function() {
				var self = this;
				
				this.$('#title').val(this.model.get("title"));
				var language = this.model.get("language");
				if (!_.isUndefined(language) && language != null) {
					language = language.toLowerCase();
				}
				if(this.model.mode === "update") {
					this.$('#authenticationMethod').val(this.model.attributes.authenticationMethod);
				} else {
					this.$('#authenticationMethod').val("P");
				}
				this.$('#language').val(language);
				this.$('#gender').val(this.model.attributes.gender);
				this.$('#state').val(this.model.attributes.state);
				
				this.$('#dateOfBirth').val(CommonUtils.formatDate(this.model.attributes.dateOfBirth));
				this.$('#expirationDate').val(CommonUtils.formatDate(this.model.attributes.expirationDate));
				
				this.$('#dateOfBirth').datepicker({format: 'yyyy-mm-dd', autoclose: true, todayHighlight: true});
				this.$('#expirationDate').datepicker({format: 'yyyy-mm-dd', autoclose: true, todayHighlight: true});
				
				let dialog = document.querySelector("#viewDialog .modal-dialog");
				if(dialog) {
					dialog.style.width = "80%";
				};

	        	var roleElement = this.$('#roleID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: roleElement,
  		  			url: "api/roles/agent/dropdown",
  		  			placeholderText: i18ntxt.editAgentRoleHint,
  		  			minLength: 0
  		  		});
  		  		
  		  		var tierElement = this.$('#tierID');
		  		CommonUtils.configureSelect2Control({
		  			jqElement: tierElement,
		  			url: "api/tiers/agents/dropdown",
		  			placeholderText: i18ntxt.editAgentTierHint,
		  			minLength: 0
		  		});
            	
            	var groupElement = this.$('#groupID');
		  		CommonUtils.configureSelect2Control({
		  			jqElement: groupElement,
		  			url: "api/groups/dropdown",
		  			placeholderText: '(select group)',
		  			minLength: 0
		  		});

            	var serviceClassID = this.$('#serviceClassID');
		  		CommonUtils.configureSelect2Control({
		  			jqElement: serviceClassID,
		  			url: "api/serviceclass/dropdown",
		  			placeholderText: '(select service class)',
		  			minLength: 0
		  		});
            	
            	var supplierAgentElement = this.$('#supplierAgentID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: supplierAgentElement,
  		  			url: "api/agents/dropdown",
  		  			placeholderText: i18ntxt.noAgent,
  		  			minLength: 2
  		  		});

            	var ownerAgentElement = this.$('#ownerAgentID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: ownerAgentElement,
  		  			url: "api/agents/dropdown",
  		  			placeholderText: i18ntxt.noAgent,
  		  			minLength: 2
  		  		});
            	
            	var areaElement = this.$('#areaID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: areaElement,
  		  			url: "api/areas/dropdown",
  		  			placeholderText: i18ntxt.noArea,
  		  			minLength: 0,
					isHtml : true
  		  		});
  		  		//Horrible Hack: validate() initializes something inside JQuery Validate that makes rule addition and removal work.
            	setTimeout(function(){self.afterRender(self);}, 500);
			},
			
			afterRender: function(self) {
	  		  	self.model.validate();
            	self.onChangeAuthenticationMethod();
            },
            
            ui: {
                save: '.agentSaveButton',
                authenticationMethodSelect: '#authenticationMethod'
            },

            // View Event Handlers
            events: {
            	'click @ui.save': 'saveAgent',
            	'change @ui.authenticationMethodSelect': 'onChangeAuthenticationMethod'
            },
            
			saveAgent: function () {
				var self = this;
				var form = $('#addAgent');
				var addAgentDialog = self.$el.closest('.modal');
				this.model.save({
					success: function (ev) {
						addAgentDialog.modal('hide');
					},
					// overriding error callback to check for duplicate mobile number
					error: function (oldModel, error) {
						// if duplicate number then hide the current modal and display warningDialog
						if (self.isDuplicateNumber(error)) {
							var mobileNumber = oldModel.form[0].elements["mobileNumber"].value;
							$.ajax({
								url: '/api/agents/by-msisdn/' + mobileNumber,
								type: 'GET',
								success: function (result) {
									var agentWarningModel = new AgentWarningModel({
										id: result.agentId
									});
									if (result.activationDate) {
										result.activationDate = CommonUtils.formatDate(result.activationDate);
									}
									if (result.lastTransactionDate) {
										result.lastTransactionDate = CommonUtils.formatDate(result.lastTransactionDate);
									}

									if (result.agentState === 'A')
										result.agentState = 'Active';
									else if (result.agentState === 'S')
										result.agentState = 'Suspended';
									else if (result.agentState === 'P')
										result.agentState = 'Permanent';
									else
										result.agentState = 'Deactivated';

									agentWarningModel.set("mobileNumber", mobileNumber);
									agentWarningModel.set(result);

									App.vent.trigger('application:dialog', {
										name: "warningDialog",
										title: "Duplicate Mobile Number",
										view: WarningDialogView,
										params: {
											model: agentWarningModel
										},
										events: {
											"click .cancelBtn": function (event) {
												var warningDialog = $("#warningDialog");
												warningDialog.modal('hide');
                                                addAgentDialog.modal('hide');
											}
										}
									});
								},
								error: function (error) {
									console.error(error);
								}
							});
						}
					}
				});
			},

			isDuplicateNumber: function (error) {
				try {
					var violations = error.responseJSON.violations;
					if (violations[0].field === "mobileNumber") {
						if (violations[0].validations[0] === "duplicateValue") {

							return true;
						}
					}
				} catch (ex) {

				}
				return false;
			},
			
			onChangeAuthenticationMethod: function() {
				var self = this;
				var authenticationMethod = this.$('#authenticationMethod').val();
				var isDomainAccountNameEnabled = (authenticationMethod == "A" || authenticationMethod == "X");
				this.$('#domainAccountName').prop('disabled', !isDomainAccountNameEnabled);
				this.$('#domainAccountNameLabel').prop('disabled', !isDomainAccountNameEnabled);
				var domainAccountLabel = App.translate('enums.usernameType.' + authenticationMethod, App.translate('agentAccounts.editDomainAccountLabel', ''));
				var domainAccountComment = App.translate('enums.usernameTypeComment.' + authenticationMethod, App.translate('agentAccounts.editDomainAccountComment', ''));
				this.$('#domainAccountNameComment').html(domainAccountComment);
				
				if(this.$('#domainAccountName').val() != "")
					this.savedDomainAccountName = this.$('#domainAccountName').val();
				
				if(!isDomainAccountNameEnabled){
					self.model.rules.domainAccountName = {required : false};
					var domainAccountNameRules = self.$('#domainAccountName').rules();
					this.$('#domainAccountName').val("");
					self.$('#domainAccountName').rules("remove", "required");
					self.$(".domainAccountNameItemGroup").hide()
				} else {
					this.$('#domainAccountName').attr("placeholder", domainAccountComment);
					this.$('#domainAccountName').val(this.savedDomainAccountName);
					this.$('#domainAccountNameLabel').html(domainAccountLabel);
					this.$('#domainAccountName').attr("placeholder", domainAccountLabel);
					self.model.rules.domainAccountName = {required : true};
					self.$('#domainAccountName').rules("add", 
                            {
                                required: true,
                            });
					self.$(".domainAccountNameItemGroup").show()
				}
				//Make email mandatory if Authentication method is Password, since MSISDN is not 
				//the communication channel with password users.
				if(authenticationMethod != "P"){
					self.model.rules.email = {required : true};
					self.$('#email').rules("add", 
                            {
                                required: true,
                            });
				} else {
					self.model.rules.email = {required : false};
					self.$('#email').rules("remove", "required");
				}

				//Highlight the incomplete fields
				self.model.validate();
				self.model.showRequiredStars();
			}
        });
        return AgentAccountsDialogView;
    });
