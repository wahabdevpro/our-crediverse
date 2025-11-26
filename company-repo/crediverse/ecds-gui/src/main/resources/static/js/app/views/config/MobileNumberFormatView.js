define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {
    	$ = window.$;
    
    	Handlebars.registerHelper( 'eachInMap', function(map, block) {
		   var out = '';
		   Object.keys( map ).map(function( prop ) {
			  out += block.fn( {key: prop, value: map[ prop ]} );
		   });
		   return out;
		});
		
		function isDefined(value, path) {
			path.split('.').forEach(function(key) { value = value && value[key]; });
			return (typeof value != 'undefined' && value !== null);
		};
		
		function updatePhaseLabel(config) {
			$("#phase-label-id").remove();
			
			if (config) {
				var phase = config.attributes.phase;
				if (phase == 'MAINTENANCE_WINDOW' || phase == 'DUAL_PHASE' || phase == 'AFTER') {
					var phaseLabel = '<span id="phase-label-id"><span style="font-weight:bold">Phase: </span><span>' + phase + '</span></span>';
					$("#msisdn-transformation-run-wrapper-id").prepend(phaseLabel);
				}
			} else {
				var MobileNumberFormatConfig = Backbone.Model.extend({url: 'api/mobile_number_transformation/config'});
				var config = new MobileNumberFormatConfig();
				
				config.fetch().then(function() {
					if (config.attributes.error) {
						alert(config.attributes.message);
						return;
					}
					
					var phase = config.attributes.phase;
					if (phase == 'MAINTENANCE_WINDOW' || phase == 'DUAL_PHASE' || phase == 'AFTER') {
						var phaseLabel = '<span id="phase-label-id"><span style="font-weight:bold">Phase: </span><span>' + phase + '</span></span>';
						$("#msisdn-transformation-run-wrapper-id").prepend(phaseLabel);
					}
				});
			}
		}

		function showProgress(phase, printInConsole) {
			if (typeof printInConsole == 'undefined') {
				printInConsole = true;
			}
			var MobileNumberTransformationProgress = Backbone.Model.extend({url: 'api/mobile_number_transformation/progress'});
			var progress = new MobileNumberTransformationProgress();
			progress.fetch().then(function() {
				if (printInConsole) {
					var progressConsole = $('#mobile-transformation-console-id');
					var currentTextareaValue = progressConsole.val();
					progressConsole.val(currentTextareaValue + progress.attributes.consoleMessage + "\n");
					if(progressConsole.length) {	// Autoscroll
						progressConsole.scrollTop(progressConsole[0].scrollHeight - progressConsole.height());
       				}
				}
				
				if (progress.attributes.progress.running) {
					$('#msisdn-transformation-run-button-id').html('Stop');
					$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
					setTimeout(showProgress, 2000);
				} else {
					$('#msisdn-transformation-run-button-id').html('Run');
					
					var MobileNumberFormatConfig = Backbone.Model.extend({url: 'api/mobile_number_transformation/config'});
					var config = new MobileNumberFormatConfig();
					config.fetch().then(function() {
						if (config.attributes.error) {
							alert(config.attributes.message);
						} else {
							phase = config.attributes.phase;
						}
						updatePhaseLabel(config);
						enableOrDisableButtons(phase);
					});
				}
			});
		}
		
		function enableOrDisableButtons(phase) {
			if (phase == 'BEFORE') {
				$('#msisdn-transformation-run-button-id').html('Run');
				$('#msisdn-transformation-run-button-id').attr('disabled', false);
				$('#msisdn-transformation-dual-phase-button-id').html('Enable Dual-phase');
				$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
			} else if (phase == 'MAINTENANCE_WINDOW') {
				$('#msisdn-transformation-run-button-id').html('Run');
				$('#msisdn-transformation-run-button-id').attr('disabled', false);
				$('#msisdn-transformation-dual-phase-button-id').html('Enable Dual-phase');
				$('#msisdn-transformation-dual-phase-button-id').attr('disabled', false);
			} else if (phase == 'DUAL_PHASE') {
				$('#msisdn-transformation-run-button-id').html('Run');
				$('#msisdn-transformation-run-button-id').attr('disabled', true);
				$('#msisdn-transformation-dual-phase-button-id').html('Disable Dual-phase');
				$('#msisdn-transformation-dual-phase-button-id').attr('disabled', false);
			} else if (phase == 'AFTER') {
				$('#msisdn-transformation-run-button-id').html('Run');
				$('#msisdn-transformation-run-button-id').attr('disabled', true);
				$('#msisdn-transformation-dual-phase-button-id').html('Enable Dual-phase');
				$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
			}
		}
		
		function createMappingRow(oldCodes, newPrefix) {
			var tr = document.createElement('TR');
			tr.className = "mapping-row";
			
			var td = document.createElement('TD');
			$('<input>').attr({
				type: 'text',
				class: 'mapping-old-codes',
				value: oldCodes.sort().join(', '),
				style: 'width:100%',
			}).appendTo(td);
			tr.appendChild(td);
			
			td = document.createElement('TD');
			$('<input>').attr({
				type: 'text',
				class: 'mapping-new-prefix',
				value: newPrefix,
				style: 'width:100%',
			}).appendTo(td);
			tr.appendChild(td);
			
			td = document.createElement('TD');
			td.style.cssText = "text-align: center";
			$('<button>').attr({
				type: 'button',
				class: "btn btn-danger removeMappingButton btn-xs",
				name: "removeMappingButton",
				value: '<i class="fa fa-times"></i>',
			}).html('Remove').
			click(function(){ $(this).closest('tr').remove(); }).
			appendTo(td);
			
			tr.appendChild(td);
			return tr;
		}
		
		function populateTable(mapping) {
			$('#mappingTable').empty();
			var mappingTable = $('#mappingTable')[0];
			if(mappingTable != null) {
				for (var key in mapping) {
					mappingTable.append(createMappingRow(mapping[key], key));
				}
			}
		}
		
		function fetchMapping() {
           	var MobileNumberFormatMapping = Backbone.Model.extend({url: 'api/mobile_number_transformation/mapping'});
       		var	mobileNumberFormatMapping = new MobileNumberFormatMapping();
       		mobileNumberFormatMapping.fetch().then(function() {
				if (mobileNumberFormatMapping.attributes.error) {
					alert(mobileNumberFormatMapping.attributes.error);
					return;
				}
				populateTable(mobileNumberFormatMapping.attributes.mapping);
			});
   		}
		
		function fetchConfig() {
			var MobileNumberFormatConfig = Backbone.Model.extend({url: 'api/mobile_number_transformation/config'});
			var config = new MobileNumberFormatConfig();
			
			var phase = 'AFTER';
			config.fetch().then(function() {
				if (config.attributes.error) {
					alert(config.attributes.message);
				} else {
					phase = config.attributes.phase;
				}
			
				updatePhaseLabel(config);
			
				$('#oldMobileNumberLengthId').val(config.attributes.oldNumberLength);
				$('#wrongFormatEnMessageId').val(config.attributes.wrongBNumberMessageEn);
				$('#wrongFormatFrMessageId').val(config.attributes.wrongBNumberMessageFr);
			
				showProgress(phase, false);
			});
   		}

        var MobileNumberFormatView = GenericConfigView.extend( {
        	template: 'Configuration#mobileNumberFormat',        	
        	dialogTemplate: "Configuration#mobileNumberFormatModal",
        	
        	url: 'api/mobile_number_transformation/config',
        	
        	ui: {
        	    runButton: '#msisdn-transformation-run-button-id',
        	    progressConsole: '#mobile-transformation-console-id',
        	    dualPhaseButton: '#msisdn-transformation-dual-phase-button-id',
        	    addMapping: '#addMappingButton',
        	    saveConfigButtonId: '#save-config-button-id',
        	},
        	
        	events: {
        		"click @ui.runButton": 'runButtonClicked',
        		"click @ui.dualPhaseButton": 'dualPhaseButtonClicked',
            	"click @ui.showUpdateDialog": 'showUpdateDialog',
        		"click @ui.addMapping": 'addMappingButtonClicked',
        		"click @ui.saveConfigButtonId": 'saveConfigButtonClicked',
            },
        	
        	onRender: function () {
        		fetchConfig();
				fetchMapping();
        	},
        	
        	runButtonClicked: function(ev) {
        		if (confirm("Are you sure? Action cannot be reversed.")) {
					var StartTransactionServerResponse = Backbone.Model.extend({url: 'api/mobile_number_transformation/start'});
					var startTransformation = new StartTransactionServerResponse();
					
					var StopTransactionServerResponse = Backbone.Model.extend({url: 'api/mobile_number_transformation/stop'});
					var stopTransformation = new StopTransactionServerResponse();
	
					var MobileNumberTransformationProgress = Backbone.Model.extend({url: 'api/mobile_number_transformation/progress'});
					var progress = new MobileNumberTransformationProgress();
					
					progress.fetch().then(function() {
						if (progress.attributes.progress.running) {
							stopTransformation.fetch({type: "post"}).then(function() {
								if (stopTransformation.attributes.error) {
									alert(stopTransformation.attributes.message);
								}
	
								$('#msisdn-transformation-run-button-id').html('Run');
								$('#msisdn-transformation-dual-phase-button-id').attr('disabled', false);
							});
						} else {
							startTransformation.fetch({type: "post"}).then(function() {
								if (startTransformation.attributes.error) {
									alert(startTransformation.attributes.message);
								} else {
									$('#msisdn-transformation-run-button-id').html('Stop');
									$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
								}
								
								setTimeout(showProgress, 500);
							});
						}
					});
				}
        	},
        	
        	dualPhaseButtonClicked: function(ev) {
        		if (confirm("Are you sure? Action cannot be reversed.")) {
					var MobileNumberFormatConfig = Backbone.Model.extend({url: 'api/mobile_number_transformation/config'});
					var config = new MobileNumberFormatConfig();
	
					var EnableDualPhase = Backbone.Model.extend({url: 'api/mobile_number_transformation/dual_phase/enable'});
					var enableDualPhase = new EnableDualPhase();
	
					var ForceEnableDualPhase = Backbone.Model.extend({url: 'api/mobile_number_transformation/dual_phase/enable?force=true'});
					var forceEnableDualPhase = new ForceEnableDualPhase();
	
					var DisableDualPhase = Backbone.Model.extend({url: 'api/mobile_number_transformation/dual_phase/disable'});
					var disableDualPhase = new DisableDualPhase();
	
					var ForceDisableDualPhase = Backbone.Model.extend({url: 'api/mobile_number_transformation/dual_phase/disable?force=true'});
					var forceDisableDualPhase = new ForceDisableDualPhase();
	
					config.fetch().then(function() {
						if (config.attributes.error) {
							alert(config.attributes.message);
							return;
						}
	
						if (config.attributes.phase == 'MAINTENANCE_WINDOW') {
							enableDualPhase.fetch({type: "post"}).then(function() {
								if (enableDualPhase.attributes.error) {
									var force = confirm(enableDualPhase.attributes.message + " Do you want to force Dual-phase?");
									if(force == true) {
										forceEnableDualPhase.fetch({type: "post"}).then(function() {
											if (forceEnableDualPhase.attributes.error) {
												alert(forceEnableDualPhase.attributes.message);
												return;
											} else {
												$('#msisdn-transformation-dual-phase-button-id').html('Disable Dual-phase');
												$('#msisdn-transformation-run-button-id').attr('disabled', true);
												updatePhaseLabel();
											}
										});
									} else {
										return;
									}
								} else {
									$('#msisdn-transformation-dual-phase-button-id').html('Disable Dual-phase');
									$('#msisdn-transformation-run-button-id').attr('disabled', true);
									updatePhaseLabel();
								}
							});
						} else if (config.attributes.phase == 'DUAL_PHASE') {
							disableDualPhase.fetch({type: "post"}).then(function() {
								if (disableDualPhase.attributes.error) {
									var force = confirm(disableDualPhase.attributes.message + " Do you want to force disable Dual-phase?");
									if(force == true) {
										forceDisableDualPhase.fetch({type: "post"}).then(function() {
											if (forceDisableDualPhase.attributes.error) {
												alert(forceDisableDualPhase.attributes.message);
												return;
											} else {
												$('#msisdn-transformation-dual-phase-button-id').html('Enable Dual-phase');
												$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
												updatePhaseLabel();
											}
										});
									} else {
										return;
									}
								} else {
									$('#msisdn-transformation-dual-phase-button-id').html('Enable Dual-phase');
									$('#msisdn-transformation-dual-phase-button-id').attr('disabled', true);
									updatePhaseLabel();
								}
							});
						}
					});
				}
        	},
        	
        	addMappingButtonClicked: function(ev) {
        		if ($('#mappingTable tr').length > 0) {
        			$('#mappingTable tr:last').after(createMappingRow(new Array(), ""));
        		} else {
        			$('#mappingTable').append(createMappingRow(new Array(), ""));
        		}
        	},
        	
        	saveConfigButtonClicked: function(ev) {
        		var oldCodesNewPrefixMap = {};
        		$(".mapping-row").each(function(index) {
					var oldCodes = $(this).find(".mapping-old-codes")[0].value;
					var newPrefix = $(this).find(".mapping-new-prefix")[0].value;
					if (oldCodes && newPrefix) {
						oldCodesNewPrefixMap[newPrefix] = oldCodes.replace(/\s/g, '').split(',');
					}
				});
        		
        		var MobileNumberFormatMapping = Backbone.Model.extend({url: 'api/mobile_number_transformation/mapping'});
				var	mobileNumberFormatMapping = new MobileNumberFormatMapping();
				
				mobileNumberFormatMapping.attributes.mapping = oldCodesNewPrefixMap;
				mobileNumberFormatMapping.save(oldCodesNewPrefixMap, {
					wait:true,
					success:function(model, response) {
						fetchMapping();
						
						var MobileNumberFormatConfig = Backbone.Model.extend({url: 'api/mobile_number_transformation/config'});
						var	config = new MobileNumberFormatConfig();
						
						config.save({
							oldNumberLength: $('#oldMobileNumberLengthId').val(),
							wrongBNumberMessageEn: $('#wrongFormatEnMessageId').val(),
							wrongBNumberMessageFr: $('#wrongFormatFrMessageId').val()
						}, {
							wait:true,
							success:function(model, response) {
								alert('Configuration successfully saved!');
								fetchConfig();
							},
							error: function(model, error) {
								console.log(error);
								if (isDefined(error, 'responseJSON.message')) {
									alert(error.responseJSON.message);
								} else {
									alert('' + error);
								}
								fetchMapping();
								fetchConfig();
							}
						});
					},
					error: function(model, error) {
						console.log(error);
						if (isDefined(error, 'responseJSON.message')) {
							alert(error.responseJSON.message);
						} else {
							alert('' + error);
						}
						fetchMapping();
						fetchConfig();
					}
				});
        	},
        	        	
        	dialogTitle: App.i18ntxt.config.mobileNumberFormatModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.mobileNumberFormat,
	  				href: "#config-mobileNumberFormat"
	  			};
        	}
        });
        
        return MobileNumberFormatView;
});
