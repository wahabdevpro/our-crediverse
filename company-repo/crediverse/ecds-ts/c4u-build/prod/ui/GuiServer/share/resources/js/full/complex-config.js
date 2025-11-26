!function($) {

	'use strict';

	/**
	 * The aim of this controller is to create framework to address: - Creating
	 * modals (use structure and create default modals) - Create title - Create
	 * default tables (content can come from server either as html / json) -
	 * Create framework for Add, Delete, Edit operations
	 */

	JSON.stringify = JSON.stringify || function (obj) {
	    var t = typeof (obj);
	    if (t != "object" || obj === null) {
	        // simple data type
	        if (t == "string") obj = '"'+obj+'"';
	        return String(obj);
	    }
	    else {
	        // recurse array or object
	        var n, v, json = [], arr = (obj && obj.constructor == Array);
	        for (n in obj) {
	            v = obj[n]; t = typeof(v);
	            if (t == "string") v = '"'+v+'"';
	            else if (t == "object" && v !== null) v = JSON.stringify(v);
	            json.push((arr ? "" : '"' + n + '":') + String(v));
	        }
	        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
	    }
	};
	
// ------------------------------------------------------------
	function AccordionComponent(accordionId, heading) {
		this.heading = heading || "Some Header";
		this.panels = [];	//Array of AccodianPanel
		this.accid = accordionId || "someaccordion";
		
		this.structure = null;
		this.parentElement = null;
		this.nextIndex = 0;
		
		this.setStructure = function(structure) {
			this.structure = structure;
		}
		
		this.createHeader = function() {
			var html = [];
			
			html[html.length] = "<div class='row modalrowcontent'>";
			html[html.length] = "<div class='col-md-12'>";
			html[html.length] = "<b><label class='control-label' for='control'>";
			html[html.length] = this.heading;
			html[html.length] = "</label></b>";
			html[html.length] = "<span style='float:right'>";
			html[html.length] = "<button type='button' class='btn btn-success btn-xs btnAddNewPanelItem' data-toggle='modal' title='Add item'><span class='glyphicon glyphicon-plus'></span></button>";
			html[html.length] = "</span>";
			html[html.length] = "</div></div>";
			return html.join("");
		}
				
		this.updateDeleteRemoveEvent = function() {
			this.parentElement.on("click", ".dialogdel", function(eventInfo) {
				try {
					var element = eventInfo.target;
					var id = $(element).attr("id");
					var pnl = "PNL_" + id.substr(id.indexOf("_") + 1);
					$("#" + pnl).remove();
				} catch(err) {
					console.error(err);
				}
			});
		}
		
		this.addAddEventHandlers = function($element, addEventHandler) {
			this.parentElement = $element;
			var self = this;
			$element.on("click", ".btnAddNewPanelItem", {accordion: self}, addEventHandler);
			this.updateDeleteRemoveEvent();
		}
		
		// panel state = in / null
		//Not dynamic (used in junction with createHtml)
		this.addPanel = function(panelId, label, body, highlightError, state) {
			var pnlState = (typeof state === "undefined" || state == null)? "" : "in";
			var panel = new AccodianPanel(panelId, label, body, highlightError, pnlState);
			this.panels[this.panels.length] = panel;
		}
		
		//This is dynamic behaviour
		this.addNewPanel = function(panelId, label, body, highlightError, state) {
			var pnlState = (typeof state === "undefined" || state == null)? "" : "in";
			var panel = new AccodianPanel(panelId, label, body, highlightError, pnlState);
			var $html = $(panel.createHtml());
			this.parentElement.find(".panel-group").append($html);
		}
		
		this.createHtml = function() {
			var html = [];
			
			html[html.length] = this.createHeader();
			html[html.length] = "<div class='panel-group' id='" + this.accid +"'>";
			
			for(var i=0; i<this.panels.length; i++) {
				html[html.length] = this.panels[i].createHtml();
			}
			
			html[html.length] = "</div>";
			return html.join("");
		}
			
		function AccodianPanel(panelId, label, body, highlightAsError, state) {
			var pnlId = panelId || "somePanel";
			var heading = label || "SomeHeading";
			var content = body || "Some Content";
			var pnlState = state || "";
			var pnlHighlightAsError = highlightAsError || false;

			this.createHtml = function() {
				var html = [];
				
				html[html.length] = "<div id='PNL_"+pnlId+"' class='panel panel-default'>";
				
				//Heading
				if (pnlHighlightAsError)
					html[html.length] = "<div class='panel-error panel-heading permissions-panel-heading'>";
				else
					html[html.length] = "<div class='panel-heading permissions-panel-heading'>";
				
				html[html.length] = "<span>";
				html[html.length] = "<h4 class='panel-title permissions-panel-title'>";
				html[html.length] = "<a data-toggle='collapse' data-parent='#accordion' href='#" + pnlId + "'>";
				html[html.length] = label;
				html[html.length] = "</a>";
				html[html.length] = "</span>";
				
				html[html.length] = "<span id='BTN_"+pnlId+"' style='float:right' class='glyphicon glyphicon-trash deletebutton dialogdel'>";
				html[html.length] = "</span>";
				
				html[html.length] = "</div>";
				
				//Body
				html[html.length] = "<div id='" + pnlId + "' class='panel-collapse collapse " + pnlState + "'>";
				html[html.length] = "<div class='panel-body'>";
				html[html.length] = content;
				html[html.length] = "</div>";
				html[html.length] = "</div>";
					
				html[html.length] = "</div>";
				
				return html.join("");
			}
		}
	}
	
	var ComplexConfigController = function(element, options, e) {
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}

		// DOM Controls
		this.id = null; // This is the same name as the variable being modified
		this.$element = $(element);
		this.$content = null;

		this.title = options.title;
		this.url = options.url; // servlet to handle page creation / update /
		// remove
		this.addHint = options.addHint;
		this.field = options.field;
		this.primaryKey = options.primaryKey;
		this.tabs = options.tabs;

		this.init();
	};

	ComplexConfigController.prototype = {
		contentID : null,
		structure : [],
		
		accordions : [],

		currentOption : null,
		currentIndex : -1,
		currentKey : null,
		modalDialog : null,
		languages : null, // .apha | .lang
		
		CONFIG_TAB : "Configuration",
		TEXT_TAB : "Multilingual Texts",

		init : function() {
			var self = this, id = this.$element.attr("id");
			this.id = id;
			this.contentID = this.id + "content";

			this.retrieveLanguages(); // Retrieve languages
			this.createControlTemplate(); // Create page structure
			this.retrieveStructure(); // Retrieve page content + structures
			this.refreshTableContent();
		},

		refreshTableContent : function() {
			this.sendJsonRequest("table", $.proxy(this.showTableConent, this));
		},

		retrieveStructure : function(data) {
			var self = this;
			this.sendJsonRequest("struct", function(data) {
				self.structure = data.params;
			});
		},

		retrieveLanguages : function(data) {
			try {
				var self = this;
				this.sendJsonRequest("lang", function(data) {
					self.languages = data;
				});
			} catch (err) {
				console.error(err);
			}
		},

		createControlTemplate : function() {
			var html = [];
			var $header = $(this.createTitle());

			this.$element.html($header);
			this.$element.on("click", ".btnAddItem", $.proxy(
					this.commandHandler, this));
			this.$content = this.$element.find("#" + this.contentID);
		},

		createTitle : function() {
			var html = [];
			html[html.length] = "<div class='row' >";
			html[html.length] = "<div class='col-xs-11 col-sm-5 col-md-7 user_heading' style='margin-left: 20px;'>";
			html[html.length] = this.title;
			html[html.length] = "</div>";
			html[html.length] = "<div class='col-xs-6 col-md-4' style='float:right; margin-right:20px' >";
			html[html.length] = "<button type='button' class='btn btn-success btn-xs btnAddItem' style='float:right;' data-toggle='modal' title='";
			html[html.length] = this.addHint;
			html[html.length] = "'><span class='glyphicon glyphicon-plus'></span></button>";
			html[html.length] = "</div>";
			html[html.length] = "</div>";
			html[html.length] = "<div id='";
			html[html.length] = this.contentID;
			html[html.length] = "'></div>";
			return html.join("");
		},

		// ------------------------------------------------------
		// -------------------- Page Content --------------------

		sendJsonRequest : function(action, callback, index, extraData) {

			try {
				var self = this;
				var data2Send = "act=" + action + "&comp=" + self.field;
				if ((typeof index !== "undefined") && (index != null)) {
					data2Send += "&index=" + index;
				}
				if ((typeof extraData !== "undefined") && (extraData != null)) {
					data2Send += "&" + extraData;
				}

				var status = $.ajax({
					type : "POST",
					url : self.url,
					async : true,
					dataType : "json",
					data : data2Send
				}).done(
						function(data) {
							try {
								if (callback != null) {
									$.proxy(callback, self)(data);
								}
								
								
//								if ((typeof data.status != null)
//										&& ((data.status == "fail"))) {
//									// Failed to retrieve
//								} else {
//									// Retrieved items
//									if (callback != null) {
//										$.proxy(callback, self)(data);
//									}
//								}
							} catch (err) {
								console.error("Ajax Error: " + err + " for call " + data2Send);
								if (callback != null)
									console.error("Callback was: " + callback)
							}
				}).fail(function(error) {

				});
			} catch (err) {
				console.error("sendJsonRequest:" + err);
			}

		},

		createAction : function(index, prefix, mode) {
			var html = [];
			try {
				var name = prefix + mode + "_" + index;
				html[html.length] = "<a id='";
				html[html.length] = name;
				html[html.length] = "' >";
				html[html.length] = "<span class='glyphicon ";
				if (mode == "view")
					html[html.length] = "glyphicon-eye-open viewbutton complexview' title='view'></span></a>";
				else if (mode == "edit")
					html[html.length] = "glyphicon-pencil editbutton complexedit' title='view'></span></a>";
				else if (mode == "del")
					html[html.length] = "glyphicon-trash deletebutton complexdel' title='view'></span></a>";
			} catch (err) {
				console.error(err);
			}
			return html.join("");
		},

		showTableConent : function(data) {
			try {
				var $html = $(this.createTableContent(data));
				$html.on("click", ".complexview", $.proxy(this.commandHandler, this));
				$html.on("click", ".complexedit", $.proxy(this.commandHandler, this));
				$html.on("click", ".complexdel", $.proxy(this.commandHandler, this));
				
				this.$content.html($html);
				this.highlightTableErrorRow();
	
				updateDataTable("tb_" + this.contentID);
			} catch(err) {
				console.error("showTableConent: " + err)
			}

		},

		createTableContent : function(data) {
			var html = [];
			try {
				// data.headers
				html[html.length] = "<table id='tb_" + this.contentID + "' class='table infotable table-hover table-bordered table-striped' >";

				html[html.length] = "<thead><tr>";
				for (var i = 0; i < data.headers.length; i++) {
					html[html.length] = "<th>";
					html[html.length] = data.headers[i];
					html[html.length] = "</th>";
				}
				html[html.length] = "<th>Action</th>";
				html[html.length] = "</tr></thead>";

				// data.items
				html[html.length] = "<tbody>";
				if (data.items.length > 0) {
					for (var i = 0; i < data.items.length; i++) {
						html[html.length] = "<tr>";

						// standarjsond data items
						for (var j = 0; j < data.items[i].length; j++) {
							html[html.length] = "<td>";
							html[html.length] = data.items[i][j];
							html[html.length] = "</td>";
						}
						// now for action
						html[html.length] = "<td>";
						html[html.length] = this.createAction(i, this.field,
								"view");
						html[html.length] = "<span>|</span>";
						html[html.length] = this.createAction(i, this.field,
								"edit");
						html[html.length] = "<span>|</span>";
						html[html.length] = this.createAction(i, this.field,
								"del");
						html[html.length] = "</td>";
						html[html.length] = "</tr>";
					}
				} else {
					// No items to display
					html[html.length] = "<td colspan='";
					html[html.length] = (data.headers.length + 1);
					html[html.length] = "'>";
					html[html.length] = "Currently no items configured";
					html[html.length] = "</td>";
				}
				html[html.length] = "</tbody>";
			} catch (err) {
				console.error("showTableConent: " + err);
			}
			return html.join("");
		},
		
		highlightTableErrorRow : function() {
			try {
				var firstErrorField= fieldErrorHelper.extractErrorField(fieldErrorHelper.currentErrorField, 0, false);
				if (firstErrorField == this.field) {
					var errorIndex = fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.currentErrorField, 0);
					var row = $("#" + firstErrorField + "edit_" + errorIndex).parent("td").parent();
					row.children('td').css('background-color', '#FFFFCC');
				}
			} catch(err) {
				console.error("highlightError: " + err);
			}
		},

		extractIndex : function(eventInfo) {
			try {
				var element = eventInfo.target;
				var id = $(element).parent().attr("id");
				var arr = id.split("_");
				return parseInt(arr[1]);
			} catch (err) {
				return -1;
			}
			return -1;
		},

		getButtonType : function(eventInfo) {
			try {
				var element = eventInfo.target;
				if ($(element).hasClass("complexview"))
					return "view";
				else if ($(element).hasClass("complexedit"))
					return "edit";
				else if ($(element).hasClass("complexdel"))
					return "del";
			} catch (err) {
				console.error(err);
			}
			return "add";
		},

		commandHandler : function(eventInfo) {
			try {
				this.currentIndex = this.extractIndex(eventInfo);
				this.currentOption = this.getButtonType(eventInfo);

				if (this.currentOption == "del") {
					this.modalDialog = this.createModal("Confirm Delete?",
							"Confirm Delete?", BootstrapDialog.TYPE_DANGER,
							"Delete", "btn-danger", $.proxy(
									this.deleteConfirmed, this));
				} else {
					var modalContent = this.createLoadingContent("propertyMessageUpdateContent");
					var modalTitle = "Add ";
					if (this.currentOption == "edit") {
						modalTitle = "Edit ";
					} else if (this.currentOption == "view") {
						modalTitle = "View ";
					}
					modalTitle += this.field;

					if (this.currentIndex < 0) {
						// Add operation
						modalContent = this.createDialogContent("add");
					}
					this.modalDialog = this.createModal(modalTitle,
							modalContent, BootstrapDialog.TYPE_SUCCESS,
							(this.currentOption == "view") ? null : "Update & Close",
							"btn-primary", $.proxy(this.updateConfirmed, this));
					
					if (this.currentIndex >= 0) {
						// View and Edit
						this.sendJsonRequest("data",
								this.componentInfoResponseReceived,
								this.currentIndex);
					} else {
						//Populate
						this.populateModalForAdd();
					}
				}
			} catch (err) {
				console.error(err);
			}
		},

		checkIfendsWith : function(str, suffix) {
			return (str.indexOf(suffix, str.length - suffix.length) !== -1);
		},

		createLoadingContent : function(id) {
			var msg = [];
			//propertyMessageUpdateContent
			msg[msg.length] = '<div style="padding-top:20px" id="';
			msg[msg.length] = id;
			msg[msg.length] = '"><center><img src="/img/bigwait.gif" /></center>';
			msg[msg.length] = '</div>';
			return msg.join("");
		},

		createIdNameForInput : function(id, name) {
			var html = [];

			html[html.length] = "id='";
			html[html.length] = id;
			html[html.length] = "' name='";
			html[html.length] = name;
			html[html.length] = "'";

			return html.join("");
		},

		createDialogInput : function(label, id, name, type, options, data) {
			var html = [];
			try {
				if (type == "boolean") {
					html[html.length] = "<div class='checkbox modalcheckbox'>";
					html[html.length] = "<label>";
					html[html.length] = "<input ";
					html[html.length] = this.createIdNameForInput(id, name);
					html[html.length] = " type='checkbox' />";
					html[html.length] = label;
					html[html.length] = "</label>";
					html[html.length] = "</div>";
				} else if (type == "sel" && (typeof options !== "undefined")) {
					html[html.length] = "<select class='selectpicker' ";
					html[html.length] = this.createIdNameForInput(id, name);
					html[html.length] = ">";
					for (var i = 0; i < options.length; i++) {
						html[html.length] = "<option value='";
						html[html.length] = options[i];
						html[html.length] = "'>";
						html[html.length] = options[i];
						html[html.length] = "</option>";
					}
					html[html.length] = "</select>";
				} else {
					html[html.length] = "<input type='text' class='form-control' ";
					html[html.length] = this.createIdNameForInput(id, name);
					html[html.length] = ""; // For class numericfields?
					html[html.length] = " placeholder='";
					html[html.length] = label;
					html[html.length] = "' ";
					
					if (typeof data !== "undefined" && data!=null) {
						html[html.length] = "value='";
						html[html.length] = data
						html[html.length] = "'";
					}
					html[html.length] = " />";
				}

			} catch (err) {
				console.error(err);
			}
			return html.join("");
		},

		isStructureTextType : function(structType) {
			return this.checkIfendsWith(structType, "Texts");
		},

		isNonTextStructureBelongToCurrentTab : function(structType,
				structGroup, currentTab) {
			return (!this.isStructureTextType(structType) && (((typeof structGroup !== 'undefined') && (structGroup == currentTab)) || ((typeof structGroup === 'undefined' || structGroup == null) && (currentTab == "genconfig"))));
		},

		createTabHeadings : function(tabs) {
			var html = [];
			html[html.length] = "<ul class='nav nav-tabs' role='tablist'>";
			for (var i = 0; i < tabs.length; i++) {
				var id = tabs[i].replace(/ /g, "");
				var desc = tabs[i];
				html[html.length] = "<li";
				if (i == 0) {
					html[html.length] = " class='active'"
				}
				html[html.length] = "><a href='#";
				html[html.length] = id;
				html[html.length] = "' data-toggle='tab'>";
				html[html.length] = desc;
				html[html.length] = "</a></li>";
			}
			html[html.length] = "</ul>";
			return html.join("");
		},

		createNonTextContent : function(evenRow, label, id, name, type, options, data) {
			var html = [];
			// Handle General typenull
			if (evenRow) {
				html[html.length] = "<div class='row modalrowcontent'>";
			}

			html[html.length] = "<div class='col-md-6";
			if (typeof fieldErrorHelper !== "undefined" && fieldErrorHelper.currentErrorField != null) {
				//Is error part of this component?
				if (fieldErrorHelper.extractErrorField(fieldErrorHelper.currentErrorField, 0, false) == this.field) {
					//Is this the correct element in the table?
					if (this.currentIndex == fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.currentErrorField, 0)) {
						var field = fieldErrorHelper.currentErrorField.substring(fieldErrorHelper.currentErrorField.indexOf(".") +1);
						if (field == id) html[html.length] = " has-error";
					}
				}
			}
			html[html.length] = "'>";
			// Label
			html[html.length] = "<label for='" + label
					+ "' class='control-label'>";
			html[html.length] = label;
			html[html.length] = "</label>";

			// Input
			html[html.length] = this.createDialogInput(label, id, name, type, options, data);

			// Error
			html[html.length] = "<span id='";
			html[html.length] = id;
			html[html.length] = "_error' class='error_message hide'></span>";

			html[html.length] = "</div>";

			if (!evenRow) {
				html[html.length] = "</div>";
			}
			return html.join("");
		},

		createTextContent : function(isFirst, label, name, type) {
			var html = [];
			html[html.length] = "<div class='panel panel-default'>";
			html[html.length] = "<div class='panel-heading permissions-panel-heading' role='tab' id='headingOne'>";
			html[html.length] = "<h4 class='panel-title permissions-panel-title'>";
			html[html.length] = "<a data-toggle='collapse' data-parent='#qt_accordion' href='#texts";
			html[html.length] = name;
			html[html.length] = "' aria-expanded='true' aria-controls='collapseOne'>";
			html[html.length] = label;
			html[html.length] = "					</a>";
			html[html.length] = "				</h4>";
			html[html.length] = "			</div>";
			html[html.length] = "			<div id='texts";
			html[html.length] = name;
			html[html.length] = "' class='panel-collapse collapse";
			if (isFirst) {
				html[html.length] = " in";
			}
			html[html.length] = "' role='tabpanel' aria-labelledby='headingOne'>";
			html[html.length] = "				<div class='panel-body'>";

			if (this.languages != null) {
				for (var j = 0; j < this.languages.lang.length; j++) {
					if (this.languages.lang[j].length > 0) {
						var textInputName = name + "_" + (j+1);

						html[html.length] = "<div class='form-group'>";
						html[html.length] = "<label for=''>";
						html[html.length] = this.languages.lang[j];
						html[html.length] = "</label>";
						html[html.length] = "<input id='";
						html[html.length] = textInputName;
						html[html.length] = "' name='";
						html[html.length] = textInputName;
						html[html.length] = "' class='form-control' type='text' placeholder='Enter Text' />";
						html[html.length] = "<span id='";
						html[html.length] = textInputName;
						html[html.length] = "_error' class='error_message hide'></span>";
						html[html.length] = "</div>";
					}
				}
			}

			html[html.length] = "				</div>";
			html[html.length] = "			</div>";

			html[html.length] = "		</div>"; // panel-default
			return html.join("");
		},

		findInStructure : function(field) {
			if (this.structure != null) {
				for(var i=0; i<this.structure.length; i++) {
					if (this.structure[i].name == field) {
						return this.structure[i];
					}
				}
			}
			return null;
		},
		
		findLabel : function(arr, lbl) {
			for(var el in arr) {
				if (el.lbl == lbl) {
					return el;
				}
			}
			return null;
		},
		
		findFieldTab : function(fieldName, fallBackText) {
			var fieldTab = null;
			if (this.tabs != null) {
				for(var el in this.tabs) {
					for(var j = 0; j < this.tabs[el].length; j++) {
						if (this.tabs[el][j] == fieldName) {
							fieldTab = el;
							break;
						}
					}
					if (fieldTab != null) break;
				}
			}
			if (fieldTab == null && (typeof fallBackText !== "undefined")) {
				fieldTab = fallBackText;
			}
			return fieldTab;
		},
		
		createDialogContent : function() {
			var html = [];
			var langCount = 0;
			var genConfigCount = 0;
			
			try {
				// Find required sets of tabs
				var tabs = [];
				for (var i = 0; i < this.structure.length; i++) {
					// Is there an entry under tabs option
					var tabLabel = this.findFieldTab(this.structure[i].name);
					
					if (tabLabel == null) {
						if (this.checkIfendsWith(this.structure[i].type, "Texts")) {
							langCount++;
						} else {
							genConfigCount++
						}
					}
				}
				
				//Create Tabs List
				if (genConfigCount > 0) {
					tabs[tabs.length] = this.CONFIG_TAB;
				}
				if (this.tabs != null) {
					for(var el in this.tabs) {
						tabs[tabs.length] = el;
					}
				}
				if (langCount > 0) {
					tabs[tabs.length] = this.TEXT_TAB;
				}

				// Create Form + Panel
				html[html.length] = "<form id='" + this.field + "Form' name='"
						+ this.field + "Form'>";
				html[html.length] = "<div role='tabpanel'>";

				// Create Tab Headings
				html[html.length] = this.createTabHeadings(tabs);

				// Tab Content
				html[html.length] = "<div class='tab-content'>";
				for (var i = 0; i < tabs.length; i++) {
					var id = tabs[i].replace(/ /g, "");

					// Modal Content Div
					html[html.length] = "<div class='tab-pane modalconent";
					if (i == 0) {
						html[html.length] = " active";
					}
					html[html.length] = "' id='" + id + "'>";

					// Content

					// Text panel start
					if (tabs[i] == this.TEXT_TAB) {
						html[html.length] = "<div class='tab-pane modalconent' id='gentexts'>";
						html[html.length] = "<div class='row modalrowcontent'>";
						html[html.length] = "	<div class='panel-group modaltexts role='tablist' aria-multiselectable='true'' id='qt_accordion' >";
					}
					var count = 0;
					for (var j = 0; j < this.structure.length; j++) {
						//First Check data type is not text message or is?
						
						if (this.isStructureTextType(this.structure[j].type)) {
							if (tabs[i] == this.TEXT_TAB && langCount > 0) {
									html[html.length] = this.createTextContent(
											(count == 0), this.structure[j].lbl,
											this.structure[j].name,
											this.structure[j].type);
									count++;
							}
						} else if ( (this.findFieldTab(this.structure[j].name, this.CONFIG_TAB) == tabs[i]) ) {
							if (this.structure[j].type != "OArray") {
								html[html.length] = this.createNonTextContent(	(count % 2 == 0), this.structure[j].lbl, this.structure[j].name, this.structure[j].name, this.structure[j].type, this.structure[j].opts);
								count++;								
							} else {
								//OArray
								html[html.length] = this.createLoadingContent(this.structure[j].name);
							}
						}
					}

					if ((tabs[i] != this.TEXT_TAB) && count % 2 != 0) {
						// Odd configuration
						html[html.length] = "</div>";
					}

					html[html.length] = "</div>"; // End of section

				}
				html[html.length] = "</div>";

				// Close Panel + Form
				html[html.length] = "</div>";
				html[html.length] = "</form>";
			} catch (err) {
				console.error(err);
			}
			return html.join("");
		},

		loadInContent : function() {
			try {

			} catch (err) {
				console.error(err);
			}
		},

		componentInfoResponseReceived : function(data) {
			var $content = $(this.createDialogContent());

			this.modalDialog.getModalBody().html($content);
			this.populateModal(data);
			
			$content.find('.selectpicker').selectpicker('refresh');
		},
		
		createOArrayContent : function(struct, data) {
			
			var ai = new AccordionComponent("acc" + struct.name, struct.lbl);
			
			try {
				if (typeof data !== "undefined" && data != null) {
					for (var i=0; i<data.length; i++) {
						var body = [];
						var count = 0;
						for(var ii=0; ii < data[i].length; ii++) {
							var id = struct.name + "[" + i + "]." + struct.parms[ii].name;
							var name = "OARR_" + struct.name + "_" + i + "_" + ii;
							body[body.length] = this.createNonTextContent(	(count % 2 == 0), struct.parms[ii].lbl, id, name, struct.parms[ii].type, struct.parms[ii].opts, data[i][ii]);
							count++;
						}
						if (count % 2 != 0) {
							body[body.length] = "</div>";
						}
						
						var isError = false;
						if (typeof fieldErrorHelper !== "undefined" && fieldErrorHelper.currentErrorField != null) {
							if (fieldErrorHelper.extractErrorField(fieldErrorHelper.currentErrorField, 0, false) == this.field) {
								if (this.currentIndex == fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.currentErrorField, 0)) {
									var field = fieldErrorHelper.extractErrorField(fieldErrorHelper.currentErrorField, 1, false);
									var index = fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.currentErrorField, 1);
									if ((field == struct.name) && (index == i)) {
										isError = true;
									}
								}
							}
						}
						ai.addPanel(struct.name + "_" + i, struct.lbl + " " + (i+1), body.join(""), isError);
					}
					ai.nextIndex = data.length;
				} else {
					ai.nextIndex = 0;
				}

			} catch(err) {
				console.error("createOArrayContent:" + err);
			}
			
			this.accordions.push(ai);

			return ai.createHtml();
		},
		
		addElementPanel : function(event) {
			try {
				var acc = event.data.accordion
				var struct = acc.structure;
				var nextIndex = acc.nextIndex;
				
				var body = [];
				var count = 0;
				for(var i=0; i < struct.parms.length; i++) {
					var id = "OARR_" + struct.name + "_" + nextIndex + "_" + i;
					var name = "OARR_" + struct.name + "_" + nextIndex + "_" + i;
					body[body.length] = this.createNonTextContent(	(count % 2 == 0), struct.parms[i].lbl, id, name, struct.parms[i].type, struct.parms[i].opts);
					count++;
				}
				if (count % 2 != 0) {
					body[body.length] = "</div>";
				}
				
				acc.addNewPanel(struct.name + "_" + nextIndex, struct.lbl + " " + (nextIndex + 1), body.join(""), false);
				acc.nextIndex++;
			} catch(err) {
				console.error(err);
			}
		},
		
		populateModalForAdd : function() {
			try {
				var $modalContent = $(this.modalDialog.getModalBody());
				for (var j = 0; j < this.structure.length; j++) {
					if (this.structure[j].type == "OArray") {
						var $sdata = $(this.createOArrayContent(this.structure[j], null));
						var $element = $modalContent.find("#" + this.structure[j].name);
						if ($element.length > 0) {
							$element.html($sdata);
						}
						this.accordions[this.accordions.length - 1].setStructure(this.structure[j]);
						this.accordions[this.accordions.length - 1].addAddEventHandlers($element, $.proxy(this.addElementPanel, this));
					}
				}
			} catch(err) {
				console.error(err);
			}
		},
		
		populateModal : function(data) {
			try {
				this.accordions = [];	//Clean Array
				var $modalContent = $(this.modalDialog.getModalBody());
				this.currentKey = null;

				for ( var key in data) {
					if (key != "render") {
						if (data.hasOwnProperty(key)) {
							try {
								if ((this.primaryKey != null)
										&& (key == this.primaryKey)) {
									this.currentKey = data[key];
								}
								
								var struct = this.findInStructure(key);
								
								if (struct.type == 'Texts') {
									for (var i = 1; i < data[key].length; i++) {
										if (data[key][i] != null) {
											var $element = $modalContent.find("#" + key + "_" + i);
											if ($element.length > 0) {
												$element.val(data[key][i]);
											}
										}
									}
								} else if (struct.type == 'OArray') {
									try {
										var $sdata = $(this.createOArrayContent(struct, data[key]));
										var $element = $modalContent.find("#" + key);
										if ($element.length > 0) {
											$element.html($sdata);
										}
										this.accordions[this.accordions.length - 1].setStructure(struct);
										this.accordions[this.accordions.length - 1].addAddEventHandlers($element, $.proxy(this.addElementPanel, this));
									} catch(err) {
										console.err(err);
									}
								} else if (data[key] == "true") {
									var $element = $modalContent
											.find("#" + key);
									if ($element.is(':checkbox')) {
										$element.prop("checked", data[key]);
									} else {
										$element.val(data[key]);
									}
								} else {
									var value = data[key];
									var field = key;

									var decimalPlaces = 0;
									var scaling = 0;
									var renderAs = null;

									if (typeof data.render[key] !== "undefined"
											&& data.render[key] != null) {
										if (typeof data.render[key].ra !== "undefined"
												&& data.render[key].ra != null) {
											// Render As
											if (data.render[key].ra == "CURRENCY") {
												var iValue = parseInt(value);
												var iCurDigits = parseInt(curDigits);
												var val = (iValue / (Math.pow(
														10, iCurDigits)))
														.toFixed(iCurDigits);
												value = val;
											}
										}
										if (typeof data.render[key].sf !== "undefined"
												&& data.render[key].sf != null) {
											var val = parseInt(value)
													/ (parseInt(data.render[key].sf));
											if (typeof data.render[key].dd !== "undefined"
													&& data.render[key].dd != null) {
												val = val
														.toFixed(parseInt(data.render[key].dd));
											}
											value = val;
										}
									}

									if ($modalContent.find("#" + field).length) {
										$modalContent.find("#" + field).val(
												value);
									} else {
										try {
											// console.error("field not found: "
											// + field);
										} catch (err) {
										}
									}
								}
							} catch (err) {
								try {
									console.error("Cannot insert: " + key
											+ ":=" + data[key] + " Error: "
											+ err)
								} catch (exerr) {
								}
							}
						}
					}

				}
				
				//Now attach events for accordion
				
			} catch (err) {
				console.error(err);
			}
		},

		deleteConfirmed : function() {
			try {
				var self = this;
				this.sendJsonRequest("del", function(data) {
					resetSessionTimeout();
					dataUpdated();
					self.refreshTableContent();
					this.modalDialog.close();
				}, this.currentIndex);
			} catch (err) {
				console.error(err);
			}
		},

		updateConfirmed : function() {
			try {
				hideErrorMessage();	//From mancommon.js
				var self = this;
				var formData = $('#' + this.field + "Form").serialize({
					checkboxesAsBools : true
				});
				
				this.sendJsonRequest("upd",
						function(data) {
					
							if ((typeof data.status !== "undefined") && (data.status != null)) {
								
								if (data.status == "pass") {
									self.modalDialog.close();
									self.refreshTableContent();
									dataUpdated(); // General Data Updated Call
								} else {
									//From mancommon.js
									if (data.field != null && data.field.length > 0) {
										var fieldId = data.field.substr(data.field.indexOf(".") + 1);
										if (fieldId.indexOf(".") < 0) {
											showErrorMessage(fieldId, data.message);
										} else {
											console.log(fieldId);
										}
									}
								}
							}
						},
						((this.currentIndex < 0) ? null : this.currentIndex),
						formData);

			} catch (err) {
				console.error("persistModalData: " + err);
			}
		},

		//
		// 'btn-danger'
		createModal : function(dialogTitle, content, dialogType, okButton,
				okButtonClass, callback) {
			var bsdialog = null;
			try {
				var buttonList = [];

				buttonList.push({
					id : "btnCancel",
					label : "Cancel",
					action : function(dialog) {
						dialog.close();
					}
				});

				if (okButton != null) {
					buttonList.push({
						id : "btnDoStuff",
						label : okButton,
						cssClass : okButtonClass,
						action : function(dialog) {
							
							if (typeof dialog.getData('callback') === 'function') {
								//callback();
								dialog.getData('callback')();
							}
						}
					});
				}

				bsdialog = new BootstrapDialog({
					id : "complexDialog",
					title : dialogTitle,
					message : content,
					type : dialogType,
					closable : true,
					data : {
						'callback' : callback
					},
					buttons : buttonList
				}).open();
			} catch (err) {
				console.error(err);
			}
			return bsdialog;
		},

	};

	$.fn.complexConfigController = function(option, event) {
		// get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this
				.each(function() {
					var $this = $(this), data = $this
							.data('complexConfigController'), options = typeof option == 'object'
							&& option;

					if (!data) {
						var mergedOptions = $.extend({},
								$.fn.complexConfigController.defaults, options,
								$(this).data());
						$this.data('complexConfigController',
								(data = new ComplexConfigController(this,
										mergedOptions, event)));
					}

					result = data;
					var elementId = $(this).attr("id");
				});
		return result;
	};

	// Model defaults
	$.fn.complexConfigController.defaults = {
//		field : null,
		title : "Service Class",
		addHint : "Add item",
		url : "",
		primaryKey : null,
		tabs : null				// Json structure of tabs
	};
	
	/**
 		Notes on Use
 		"tabs" are a json structure e.g.
 		tabs : [
 			// First tab ...
 			{
 				lbl: "Tab Label",
 				fields : [
 					"NameElement1",
 					"Element2"
 				]
 			},
			// Second tab ...

 		]
	 */

}(jQuery, window, document);