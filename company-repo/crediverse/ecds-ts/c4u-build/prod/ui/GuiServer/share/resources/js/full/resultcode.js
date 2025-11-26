!function($) {

	'use strict';

	var ResultCodeController = function(element, options, e) {
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}

		// Expose public Methods
		// this.registerDialogMetricEvent =
		// ResultCodeController.prototype.someMethod;

		// DOM Controls
		this.$element = $(element);
		this.$selectLanguage = null;
		this.$selectReturnCode = null;
		this.delButtons = [];

		// Properties
		this.id = null; // This is the same name as the variable being modified
		this.currentLanguageIndex = options.currentLanguageIndex;
		this.updateMadeToPageMethod = options.updateMadeToPageMethod;
		this.currentLanguageName = null;
		this.currentOption = null;
		this.languageDivsIds = [];
		this.returnCodes = [];		// alreadyAdded, alreadyMember, alreadyOtherMember, ...
		this.languageIds = [];		// eng, fre, ...
		this.rcDefaults = null;		// Data structure containing defaults, {langs : ["eng", "fre"], codes: }
		// Constructor
		this.init();
	};

	ResultCodeController.prototype = {
		init : function() {
			var self = this, id = this.$element.attr("id");

			// Extract ID
			this.id = id;

			// Retrieve default return code information
			this.retrieveDefaultReturnCodes();

			// Extract Selectors
			this.$element.find("select").each(function() 
			{
				var $this = $(this);
				var id = $this.attr("id");
				if (id.indexOf("IGNORERCADD") == 0) {
					self.$selectReturnCode = $this;
				} else if (id.indexOf("IGNORERCLAN") == 0) {
					self.$selectLanguage = $this;
					self.currentLanguageName = $this.val();
				}
			});

			this.refreshDeleEvents();
			
			// Language Selector
			this.$selectLanguage.on({
				'change' : $.proxy(this.languageSelectorChanged, this)
			});

			$(".addResultCode").on({
				'click' : $.proxy(this.addReturnCodeButtonPress, this)
			});

			// Div arrays
			this.languageDivsIds = [];
			this.languageIds = [];
			this.$selectLanguage.find("option").each(function() {
				try {
					var $this = $(this);
					var value = $this.attr("value");
					self.languageDivsIds[self.languageDivsIds.length] = value;
					var arr = value.split("_");
					self.languageIds[self.languageIds.length] = arr[2];
				} catch (err) {
					console.error(err);
				}

			});

			this.returnCodes = [];
			this.$selectReturnCode.find("option").each(function() {
				var $this = $(this);
				var value = $this.attr("value");
				self.returnCodes[self.returnCodes.length] = value;
			});

			this.checkAndHideReturnCodeDiv();
		},

		checkAndHideReturnCodeDiv : function() {
			try {
				if (this.returnCodes.length == 0) {
					$(".addReturnCodeDiv").addClass("hide");
				} else {
					$(".addReturnCodeDiv").removeClass("hide");
				}
			} catch (err) {

			}
		},

		suggestPageUpdate : function() {
			try {
				this.updateMadeToPageMethod();
			} catch (err) {
				console.error(err);
			}
		},

		refreshDeleEvents : function() {
			try {
				var self = this;
				$(".removeReturnCodes").unbind('click');
				$(".removeReturnCodes").on('click', function(e) {
					var id = $(this).attr("id");
					self.deleteReturnCodeButtonPress(id);
				});
				// $(".removeReturnCodes").on({
				// 'click' : $.proxy(this.deleteReturnCodeButtonPress, this)
				// });
			} catch (err) {
				console.error(err);
			}
		},

		// Select combo updated
		languageSelectorChanged : function(e) {
			try {
				this.currentLanguageIndex = e.target.selectedIndex;
				this.currentLanguageName = e.target.options[e.target.selectedIndex].text;
				this.currentOption = e.target.options[e.target.selectedIndex].value;
				this.updateResultCodeDiv();
			} catch (err) {
				console.error(err);
			}
		},

		// Show the correct div
		updateResultCodeDiv : function() {
			try {
				var divId = "OPT_" + this.id
						+ this.languageIds[this.currentLanguageIndex];

				for (var i = 0; i < this.languageDivsIds.length; i++) {
					var $element = $("#" + this.languageDivsIds[i]);
					if (this.languageDivsIds[i] == this.currentOption) {
						$element.show();
						$element.removeClass("hide");
					} else {
						$element.hide();
						$element.addClass("hide");
					}
				}
			} catch (err) {
				console.error(err);
			}
		},

		// Request delete (then remove)
		deleteReturnCodeButtonPress : function(btnId) {
			try {
				var arr = btnId.split("_");
				var divBlockId = this.id + "_" + arr[1] + "_";
				var self = this;

				this.confirmDeleteReturnCode(function(result) {
					if (result) {
						self.updateReturnCodesList(arr[1], "del");
						for (var i = 0; i < self.languageIds.length; i++) {

							var removeID = divBlockId + self.languageIds[i];
							$("#" + removeID).remove();
						}
						self.suggestPageUpdate();
					}
				});

			} catch (err) {
				console.error(err);
			}
		},

		camelCaseToNormal : function(camelCase) {
			try {
				var ret = camelCase.replace(/([A-Z])/g, ' $1').replace(/^./,
						function(str) {
							return str.toUpperCase();
						});
				return ret;
			} catch (err) {
				console.error(err);
			}
			return camelCase;
		},

		updateReturnCodesList : function(attribute, operation) {
			try {
				var optionId = attribute;

				if (operation == 'add') {
					var index = -1;
					for (var i = 0; i < this.returnCodes.length; i++) {
						if (this.returnCodes[i] == attribute) {
							index = i;
							break;
						}
					}
					if (index >= 0) {
						this.returnCodes.splice(index, 1);
					}
				} else if (operation == 'del') {
					for (var i = 0; i < this.returnCodes.length; i++) {
						if (this.returnCodes[i] == attribute) {
							index = i;
							break;
						}
					}
					this.returnCodes[this.returnCodes.length] = attribute;
				}

				// var text = this.camelCaseToNormal(attribute);

				this.$selectReturnCode.empty();

				for (var i = 0; i < this.returnCodes.length; i++) {
					if (typeof this.returnCodes[i] !== 'undefined') {
						var text = this.camelCaseToNormal(this.returnCodes[i]);
						var html = "<option value='" + this.returnCodes[i]
								+ "'>" + text + "</option>";
						this.$selectReturnCode.append(html);
					}
				}

			} catch (err) {
				console.error(err);
			}
			this.checkAndHideReturnCodeDiv();
		},

		// Confirmation for delete
		confirmDeleteReturnCode : function(callback) {
			var bsdialog = new BootstrapDialog(
					{
						title : "Confirm Delete",
						message : "Confirm Return Code removal?",
						type : BootstrapDialog.TYPE_DANGER,
						closable : true,
						data : {
							'callback' : callback
						},
						buttons : [
								{
									id : "btnCancel",
									label : "Cancel",
									action : function(dialog) {
										typeof dialog.getData('callback') === 'function'
												&& dialog.getData('callback')(
														false);
										dialog.close();
									}
								},
								{
									id : "btnRemove",
									label : "Delete",
									cssClass : 'btn-danger',
									action : function(dialog) {
										typeof dialog.getData('callback') === 'function'
												&& dialog.getData('callback')(
														true);
										dialog.close();
									}
								} ]
					}).open();

		},

		createLanguageHtml : function(returnCode, languageId, text) {
			var html = [];
			try {
				var mainDivId = this.id + "_" + returnCode + "_" + languageId;
				var btnId = "DEL_" + returnCode;
				var label = this.camelCaseToNormal(returnCode);
				var btnTitle = "Remove " + label;
				var inputId = "RC_" + this.id + "_" + returnCode + "_"
						+ languageId;

				html[html.length] = "<div id='" + mainDivId + "'>";
				html[html.length] = " <div class='row' style='margin-top: 10px; margin-bottom: 0px;'>";
				html[html.length] = "  <div class='col-sm-10'><label for='something' >"
						+ label + "</label></div>";
				html[html.length] = "    <div class='col-sm-1'>";
				html[html.length] = "      <button id='"
						+ btnId
						+ "' type='button' class='btn btn-danger btn-xs removeReturnCodes' style='float: right;'  title='"
						+ btnTitle
						+ "'><span class='glyphicon glyphicon-remove'></span></button>";
				html[html.length] = "    </div>";
				html[html.length] = "  </div>";
				html[html.length] = "  <div class='row'>";
				html[html.length] = "    <div class='col-sm-11'>";
				html[html.length] = "      <input id='" + inputId + "' name='"
						+ inputId
						+ "' type='text' class='form-control' value='" + text
						+ "' />";
				html[html.length] = "    </div>";
				html[html.length] = "  </div>";
				html[html.length] = "</div>";

			} catch (err) {
				console.error(err);
			}
			return html.join("");
		},

		addReturnCodeButtonPress : function(e) {
			try {
				var self = this;
				var text = $("#RCADD_" + this.id).val();
				var returnCode = this.$selectReturnCode.val();
				this.updateReturnCodesList(returnCode, 'add');
				this.suggestPageUpdate();

				var arr = (this.$selectLanguage.val()).split("_");
				var selectedLanguage = arr[2];

				for (var i = 0; i < this.languageIds.length; i++) {
					//Get text to put on page
					var languageId = this.languageIds[i];	// e.g. eng
					var inputText = ((languageId != selectedLanguage) || (text.length == 0)) ? this.extractDefaultReturnCodeText(returnCode, languageId) : text;
					inputText = inputText.replace(/\'/g, "&#39;");	// Unprintable chars
					var html = this.createLanguageHtml(returnCode, languageId, inputText);
					
					// Update page DOM
					var $element = $(html);
					$element.keypress(function() {
						self.suggestPageUpdate();
					});
					var appendToID = "OPT_" + this.id + "_" + languageId;
					$("#" + appendToID).append($element);
				}
				this.refreshDeleEvents();
			} catch (err) {
				console.error(err);
			}
		},

		retrieveDefaultReturnCodes : function() {
			try {
				var self = this;
				sendAsyncAjax("/rct", "", function(data) {
					try {
						if (data !== "undefined" && data.codes !== "undefined") {
							self.rcDefaults = data;
						}
					} catch (err) {
						throw err;
					}
				}, function(err) {
					throw err;
				});
			} catch (err) {
				if (console) console.error("retrieveReturnCodes: " + err);
			}
		},
		
		// Retrieve language index for default code text data struct
		findDefaultsLanguageIndex: function(languageID) {
			for(var i=0; i<this.rcDefaults.langs.length; i++) {
				if (this.rcDefaults.langs[i] == languageID)
					return i;
			}
		},

		// Retrieve Default text
		extractDefaultReturnCodeText : function(returnCode, languageID) {
			var result = null;
			try {
				// Find language index
				var langIndex = this.findDefaultsLanguageIndex(languageID);
				for(var code in this.rcDefaults.codes) {
					if (code == returnCode) {
						result = this.rcDefaults.codes[code][langIndex];
						break;
					}
				}
			} catch (err) {
				console.error(err);
			}
			return result;
		}

	};

	$.fn.resultCodeController = function(option, event) {
		// get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this
				.each(function() {
					var $this = $(this), data = $this
							.data('resultCodeController'), options = typeof option == 'object'
							&& option;
					if (!data) {
						var mergedOptions = $.extend({},
								$.fn.resultCodeController.defaults, options, $(
										this).data());
						$this.data('resultCodeController',
								(data = new ResultCodeController(this,
										mergedOptions, event)));
					}

					result = data;
					var elementId = $(this).attr("id");
				});
		return result;
	};

	// Model defaults
	$.fn.resultCodeController.defaults = {
		currentLanguageIndex : 0,
		updateMadeToPageMethod : dataUpdated
	};

}(jQuery, window, document);