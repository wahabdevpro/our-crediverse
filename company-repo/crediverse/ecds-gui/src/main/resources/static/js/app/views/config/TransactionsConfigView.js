define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView', 'models/TransactionsConfigModel', 'views/config/TransactionsConfigDialogView', 'models/ValidationModel', ],
    function($, App, _, Marionette, Handlebars, GenericConfigView, TransactionsConfigModel, TransactionsConfigDialogView, ValidationModel) {

        var TransactionsConfigView = GenericConfigView.extend( {
        	template: 'configuration/Transaction#transactionsConfig',
        	url: 'api/config/transactions',
        	variables: null,
        	ui: {
        	    showUpdateDialog: '.showTransactionsConfigDialog',
        	    downloadTdrStructure: '.download-tdr-structure',
        	},

			events: {
				'click @ui.showUpdateDialog' : 'showUpdateDialog',
				'click @ui.downloadTdrStructure' : 'downloadTdrStructure'
			},

			dataFromEvent: function(ev) {
				var self = this;
				var data = {};
					data = this.model.attributes;
					data.redraw = function() {
						Backbone.history.loadUrl(Backbone.history.fragment);
					}
				return data;
			},

			showUpdateDialog: function(ev) {
				var self = this;
				App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: TransactionsConfigDialogView,
            		title: self.dialogTitle,
            		hide: function() {
            			self.retrieveConfigData(true);
	        		},
            		params: {
            			model: self.model,
            			variables:self.variables,
            			textAreaHeight: self.dialogTextAreaHeight,
            		}
            	});

				return false;
			}, 

			tdrStructureVersionMap: {
				"1_0_0": {
					"version": App.i18ntxt.config.tdrVersionLabel_1_0_0,
					"columns": [
						/** Removing seller bonus related columns - as it is removed from the system */
						// { "columnName": "Seller Trade Bonus", "position": "39", "added": false },
						// { "columnName": "Seller Bonus Percentage", "position": "40", "added": false },
						// { "columnName": "Seller Bonus Provision", "position": "41", "added": false }
					],
				},
				"1_13_0": {
					"version": App.i18ntxt.config.tdrVersionLabel_1_13_0,
					"columns": [
						/** Removing seller bonus related columns - as it is removed from the system */
						// { "columnName": "Seller Trade Bonus", "position": "39", "added": true },
						// { "columnName": "Seller Bonus Percentage", "position": "40", "added": true },
						// { "columnName": "Seller Bonus Provision", "position": "41", "added": true }
					],
				}
			},

        	dialogTitle: App.i18ntxt.config.transModalTitle,
        	dialogTextAreaHeight: 60,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.transBC,
	  				href: "#config-transactions"
	  			};
        	},
        
			afterRender: function(option) {
				var self = this;
				if (self.model.attributes) {
					if (self.model.attributes["tdrStructureVersion"]) {
						var structureVersionServer = self.model.attributes["tdrStructureVersion"];
						var structureVersion = structureVersionServer.replaceAll(".","_");
						var findVersionFromMap = "";
						if (self.tdrStructureVersionMap) {
							if (self.tdrStructureVersionMap[structureVersion]) {
								var specificVersion = self.tdrStructureVersionMap[structureVersion];
								findVersionFromMap = specificVersion["version"];
								self.model.set("tdrStructureVersionLabel", findVersionFromMap);
								$("#download-text").html(findVersionFromMap);
							} else {
								console.log(`Version ${structureVersion} not found in Map`);
							}
						} else {
							console.log("Map not found");
						}
						var availableStructureVersions = [];
						for(key in self.tdrStructureVersionMap) {
							var version = self.tdrStructureVersionMap[key];
							var labelKey = `tdrVersionLabel_${key}`;
							var optionLabel = App.i18ntxt.config[labelKey];
							var value = key.replaceAll("_",".");
							var checked = structureVersionServer === value ? "true" : "false";
							var addedColumns = self.tdrStructureVersionMap[key]["addedColumns"];
							var rdoObj = {
								value: value,
								optionLabel: optionLabel,
								version: version,
								index: key,
								checked: checked,
								addedColumns: addedColumns
							};
							availableStructureVersions.push(rdoObj);
						}
						self.model.set("availableStructureVersions", availableStructureVersions);
						self.model.set("tdrStructureVersionMap", self.tdrStructureVersionMap);
					}
				}
			},

			downloadTdrStructure: function(ev) {
				ev.preventDefault();
				var version = this.model.attributes.tdrStructureVersion;
				version = encodeURI(version);
            	window.location.href = "/api/tdrs" + "/template/" + version;
			},

        	dialogOnRender: function(el) {
        		var val =  el.find("#channelRequestTimeoutSeconds").val();
        		if (val == "" ) {
        			el.find("#applyChannelRequestTimeoutDiv").hide();
        		}
        		
        		el.find("#applyChannelRequestTimeout").on("change", function(ev) {
        			if(this.checked) {
        				el.find("#applyChannelRequestTimeoutDiv").show();
        				el.find("#channelRequestTimeoutSeconds").val("20");
        			} else {
        				el.find("#applyChannelRequestTimeoutDiv").hide();
        				el.find("#channelRequestTimeoutSeconds").val("");
        			}
        		});
        	}
        	
        });
        
        return TransactionsConfigView;

});