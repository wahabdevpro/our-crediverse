define( ['jquery', 'underscore', 'App',
		 'views/config/GenericConfigDialog'
         ],
    function($, _, App, GenericConfigDialog) {
	
        //ItemView provides some default rendering logic
        var TransactionConfigDialogView =  GenericConfigDialog.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},

        	template: "configuration/Transaction#transactionsConfigModal",
        	
			tdrStructureInitialState: '',
        	ui: {
				addWarning: '.addWarning',
				tdrDownloadBtn: '.tdr-download-btn'
			},

        	events: {
        		'click .saveButton' : 'saveConfiguration',
				'click @ui.addWarning': 'addWarning',
				'click @ui.tdrDownloadBtn': 'downloadTdrStructure'
        	},

			downloadTdrStructure: function(ev) {
				var version = ev.target.getAttribute("tdrDownloadVersion");
				ev.preventDefault();
            	window.location.href = "/api/tdrs" + "/template/" + version;
			},
			
			addWarning : function(evt) {
				var self = this;
				var version = evt.target.getAttribute("value");
				version = version.replaceAll(".","_");
				$(".rdoStructureVersions").each(function(index, elm) {
					if ($(elm).is(":checked")) {
						if(self.tdrStructureInitialState === $(elm).val()) {
							$("#warningBox").removeClass("show-element");
							$("#warningBox").addClass("hide-element");
						} else {
							$("#warningBox").removeClass("hide-element");
							$("#warningBox").addClass("show-element");
							var columns = self.model.attributes.tdrStructureVersionMap[version]["columns"];
							self.createWarningBox(columns);
						}
					}
				});
			},

			createWarningBox : function(columns) {
				var html = "";
				columns.forEach(element => {
					html += `<li> <strong> ${element.columnName} </strong> ${element.added ? App.i18ntxt.config.tdrAddedColumnsLabel : App.i18ntxt.config.tdrRemovedColumnsLabel} ${App.i18ntxt.config.tdrPositionLabel} <strong> ${element.position} </strong> </li>`;
				});
				
				if (columns.length < 1) {

					$("#tdrColumnsWarning").removeClass("show-element");
					$("#tdrColumnsWarning").addClass("hide-element");

				} else {

					$("#tdrColumnsInfo").html(html);
					$("#tdrColumnsWarning").removeClass("hide-element");
					$("#tdrColumnsWarning").addClass("show-element");

				}
			},

			isTdrStructureChange : function() {
				var self = this;
				var foundChanged = true;
				$(".rdoStructureVersions").each(function(index, elm) {
					if ($(elm).is(":checked") && self.tdrStructureInitialState === $(elm).val()) {
						foundChanged = false;
						return false;
					}
				});
				return foundChanged;
			},

			afterRender: function() {
            	var self = this;
            	this.$el.on('shown.bs.tab', 'a[data-toggle="tab"]', function() {
            		self.refreshEditors(self);
            	});
            	
            	setTimeout(function() {
            		self.refreshEditors(self);
            	}, 500);

				if(this.model.attributes) {
					if(this.model.attributes['tdrStructureVersion']) {
						this.tdrStructureInitialState = this.model.attributes['tdrStructureVersion'];
					} else {
						console.log("No tdrStructureVersion");
					}
				} else {
					console.log("No attributes");
				}
            },

			beforeSaveConfiguration: function(ev) {
				var self = this;
				var structureChangeConfirmation = $("#structureChangeConfirmation").is(":checked");
				if(self.isTdrStructureChange() && !structureChangeConfirmation) {
					App.error("Confirm TDRs Structure Change");
					$("#errorMessage").addClass("show-element");
					$("#confirmText").addClass("display-error");
					$("#confirmationError").html(App.i18ntxt.config.tdrConfirmationText);
					return false;
				}
				return true;
			},
        	
        });
        
        return TransactionConfigDialogView;
	}
);
	
