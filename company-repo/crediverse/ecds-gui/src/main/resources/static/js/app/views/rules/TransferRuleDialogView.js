define( ['jquery', 'underscore', 'App', 'marionette', 'models/RuleModel', 'utils/CommonUtils', 'jquery.select2'],
	function($, _, App, Marionette, RuleModel, CommonUtils) {
		//ItemView provides some default rendering logic
		var TransferRuleDialogView =  Marionette.ItemView.extend( {
			tagName: 'div',
			attributes: {
				class: "modal-content"
			},
			url: 'api/permissions',
			template: "TransferRules#singleruleview",
			i18ntxt: App.i18ntxt.rules,
			error: null,
			tierList: null,
			featureBar: null,

			initialize: function (options) {
				if (_.isUndefined(this.model)) {
					this.model = new RuleModel();
					this.model.url = "api/transfer_rules";
				}

				options = options || {};

				const self = this;
				this.model.set('tierList', options.tierList);

				(options.features || []).forEach((feature) => {
					self.model.set(feature.name, feature.state ? 'enabled' : 'disabled');
				})

				let dialog = document.querySelector("#viewDialog .modal-dialog");
				if(dialog) {
					dialog.style.width = "80%";
				}
			},

			updateCumulativeBonus: function(){
				var opt = $('#targetTierID').find("option:selected");
				if (!opt.length || opt.val()=='') {
					$('#cumulativeTransferBonusPercent').text('(select target tier)');
					return;
				}
				var cmPercent = parseFloat(opt.attr('data-down-percent'));
				if (isNaN(cmPercent)) 
					cmPercent = 0.0;

				var buyerBonus = parseFloat($('#buyerTradeBonusPercentageString').val());

				const ctb = (((cmPercent * 100.0) + buyerBonus + (((cmPercent * 100.0) * buyerBonus) / 100.0)));
				$('#cumulativeTransferBonusPercent').text( ctb.toFixed(6) + ' %');
			},

			onSourceTierChange: function(select,clearGroup) {
				var opt = $(select).find("option:selected");
				if(clearGroup) {
					// NOT WORKING
					//$("#groupID").select2("val", null);
					//$("#groupID").select2('data', {id: null, text: null});
				}	
				$("#targetTierID option").attr('disabled', false );
				if (opt.length && opt.val()!=''){
					var toIds = opt.attr('data-to').split(',');
					$("#targetTierID option").attr('disabled', true ); 
					toIds.forEach(function(sid){
						$('#targetTierID option[value="'+sid+'"]').attr('disabled', false ); 
					});
				}
			},

			onTargetTierChange: function(select,clearGroup) {
				var opt = $(select).find("option:selected");
				if(clearGroup) {
					// NOT WORKING
					//$("#targetGroupID").select2("val", null);
					//$('#targetGroupID').select2({'data': [{id: '', text: ''}]});
				}	
				$("#sourceTierID option").attr('disabled', false ); 
				if (opt.length && opt.val()!=''){
					var bonus = opt.attr('data-bonus');
					if (bonus=='true')
						$('.show-on-bonus').show();
					else	
						$('.show-on-bonus').hide();

					var fromIds = opt.attr('data-from').split(',');
					$("#sourceTierID option").attr('disabled', true ); 
					fromIds.forEach(function(sid){
						$('#sourceTierID option[value="'+sid+'"]').attr('disabled', false ); 
					});
					this.updateCumulativeBonus();
				}	
			},

			onRender: function () {
				/**
				 * FIXME
				 *  Very special Black Magic F#$%^&*-ery
				 *  Here be dragons ... 
				 *  Leave Timeout to be
				 */
				setTimeout(() => {
					var self = this;

					var daysSelected = this.model.get('currentDays');
					_.each(daysSelected, function(item, id){
						var selector = ":checkbox[name='currentDays[]'][value='"+item+"']";
						var currentItem = $(selector);
						currentItem.prop("checked","checked");
					});

					$('#buyerTradeBonusPercentageString').bind('change keyup input paste',function(){
						self.updateCumulativeBonus();
						// TODO: if this value is not 0.0 then set disable seller trade bonus field.  
						// else enable buyer trade bonus field. 
					});
					
					$('button[role="days-all"]').on('click',function(){
						$(":checkbox[name='currentDays[]']").prop("checked","checked");
					});
					$('button[role="days-none"]').on('click',function(){
						$(":checkbox[name='currentDays[]']").removeProp("checked");
					});
					$('button[role="days-invert"]').on('click',function(){
						$(":checkbox[name='currentDays[]']").each(function(ix,item){
							var checked = $(this).is(":checked");
							if(checked)
								$(this).removeProp("checked");
							else	
								$(this).prop("checked","checked");
						});	
					});

					$('#groupID').select2({
						url: "api/groups/dropdown",
						placeholder: App.i18ntxt.rules.ruleGroupIdPlaceHolder,
						ajax: {
							data: function (params) {
								if ( $('#sourceTierID').val() )
									params.tierID = $('#sourceTierID').val();
								return params;
							}
						}
					});

					$('#targetGroupID').select2({
						url: "api/groups/dropdown",
						placeholder: App.i18ntxt.rules.ruleGroupIdPlaceHolder,
						ajax: {
							data: function (params) {
								if ( $('#targetTierID').val() )
									params.tierID = $('#targetTierID').val();
								return params;
							}
						}
					});


					$('#targetServiceClassID').select2({
						url: "api/serviceclass/dropdown",
						placeholder: App.i18ntxt.rules.ruleServiceClassPlaceHolder,
					});

					$('#serviceClassID').select2({
						url: "api/serviceclass/dropdown",
						placeholder: App.i18ntxt.rules.ruleServiceClassPlaceHolder,
					});

					$('#sourceTierID').val(this.model.get('sourceTierID'));
					$('#sourceTierID option[value="'+this.model.get('sourceTierID')+'"]').attr('selected', 'selected');

					$('#targetTierID').val(this.model.get('targetTierID'));
					$('#targetTierID option[value="'+this.model.get('targetTierID')+'"]').attr('selected', 'selected');

					self.onSourceTierChange('#sourceTierID',false);
					self.onTargetTierChange('#targetTierID',false);

					$('#sourceTierID').on('change', function(){
						self.onSourceTierChange(this,true);
					});

					$('#targetTierID').on('change', function(){
						self.onTargetTierChange(this,true);
					});

					var areaElement = this.$('#areaID');
					CommonUtils.configureSelect2Control({
						jqElement: areaElement,
						url: "api/areas/dropdown",
						placeholderText: this.i18ntxt.noArea,
						minLength: 0,
						isHtml: true
					});

					self.updateCumulativeBonus();
				}, 50);
			},

			ui: {
				view: '',
				save: '.ruleCreateButton'
			},

			// View Event Handlers
			events: {
				"click @ui.save": 'saveRule'
			},
			preprocessSave: function(data) {
				//data.maximumAmount = 
				//data.minimumAmount =
			},
			saveRule: function() {
				var that = this;

				var form = this.$('form');
				var currentDays = [];
				form.find('input[name="currentDays[]"]').each(function(){
					if($(this).is(':checked'))
						currentDays.push($(this).val());
				});
				this.model.set('currentDays',currentDays);
				this.model.save({
					success: function(ev){
						var dialog = that.$el.closest('.modal');
						dialog.modal('hide');
					},
					preprocess: function(data) {
						if (data.ruleActive) {
							data.currentState = 'ACTIVE';
						}
						else {
							data.currentState = 'INACTIVE';
						}
					}
				});
			}
		});
		return TransferRuleDialogView;
	});
