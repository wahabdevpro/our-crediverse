function Property() {
	this.name = "";
	this.details = "";
	this.fieldId = "";
	this.editable = false;
}

/**
	Data Formats
	
	languageData {
		alpha []
		lang  []
	}
	
 */

!function($) {
	
	'use strict';

	var C4UProgressGrid = function(element, options, e) {
		//Properties
		this.$element = $(element);
		this.test = true;
		this.dialogHandle = null;
		this.serverurl = options.serverurl;
		this.languageData = options.languageData;
		this.returnCodes = options.returnCodes;
		this.retrieveMethod = options.retrieveMethod;
		this.updatePropertyCallback = options.updatePropertyCallback;
		this.usedMessageErrorCodes = [];
		this.defaultErrorMessage = "";
		
		//Events
        if (e) {
            e.stopPropagation();
            e.preventDefault();
        }
        //Constructor
        this.init();
	};
	
	C4UProgressGrid.prototype = {
		init: function() {
			var that = this, id = this.$element.attr('id');
			this.update(null);
		},
		
		update: function(model, endNode) {
			try {
				var html = [];
				html[html.length] = this.createHeaderHtml();
				
				if (model != null) {
					for(var i=0; i<model.groups.length; i++) {
						html[html.length] = this.createGroupTitle(model.groups[i].cat, model.groups[i].cat);
						html[html.length] = this.createPropertyTable(model.groups[i].cat + "_data", model.groups[i].props, model.nid);
					}
					
				} else {
					if(typeof endNode === 'undefined') {
						html[html.length] = this.createGeneralMessage("Click on left tree to view properties");
					} else {
						html[html.length] = this.createGroupTitle("EndNode", "EndNode");
					}
					
				}
				this.$element.html(html.join(""));
				
				//Assign events
				this.updateEvents();
			} catch(err) {
				console.error(err);
			}
		},
		
		log: function(message) {
			try {
				console.error(message);
			} catch(err) {
				alert(err);
			}
		},
		
		createHeaderHtml: function() {
			var html = [];
			html[html.length] = '<table class="proptable">';
			html[html.length] = '<thead>';
			html[html.length] = '<tr>';
			html[html.length] = '<td>Name</td>';
			html[html.length] = '<td>Value</td>';
			html[html.length] = '<td></td>';
			html[html.length] = '</tr>';
			html[html.length] = '</thead>';
			html[html.length] = '</table>';
			return html.join('');
		},
		
		createGeneralMessage: function(msg) {
			var html = [];
			html[html.length] = '<table class="proptable" >';
			html[html.length] = '<tr>';
			html[html.length] = '<tdcolspan="3"><span style="padding-left:10px;">' + msg + '</span></td>';
			html[html.length] = '</tr></table>';
			return html.join("");
		},
		
		createGroupTitle: function(titleId, title) {
			var html = [];
			html[html.length] = '<div class="propgrid-title-outer">';
			html[html.length] = '<div id="' + titleId + '" class="propgrid-title-expand propgroup">' + title + '</div>';
			html[html.length] = '</div>';
			return html.join('');
		},
		
		createPropertyTable: function(sectionId, properties, nid) {
			var html = [];
			
			html[html.length] = '<table class="proptable" id="'+sectionId+'" >';
			if ((properties !== 'undefined')
					&& (properties !=null)) {
				for(var i=0; i<properties.length; i++) {
					html[html.length] = '<tr>';
					html[html.length] = '<td>' + properties[i].name + '</td>';
					html[html.length] = '<td>' + properties[i].value + '</td>';
					html[html.length] = '<td>';
					if (properties[i].edit) {
						html[html.length] = '<a id="';
						html[html.length] = properties[i].name + "_" + nid; 
						html[html.length] = '" class="editproperty" href="#"><span class="glyphicon glyphicon-info-sign" title="edit"></span></a>';
					}
					html[html.length] = '</td>';
					html[html.length] = '</tr>';
				}
				html[html.length] = '</table>';
			}
			return html.join('');
		},
		
		//Property updating
		updateEvents: function() {
			$(".editproperty").on('click', $.proxy(this.clickToEditProperty, this));
		},
		
		clickToEditProperty: function(e) {
			try {
				e.stopPropagation();
				e.preventDefault();
				
				var $input = $(e.target),
		          id = $input.closest('a').attr("id");
				
				var ids = id.split("_"),
					nid = ids[1], aid = ids[0];
				
				//Show Dialog (which will wait!)
				this.showPropertyUpdateDialog(nid, aid);
				
				
			} catch(err) {
				console.error("clickToEditProperty Error: " + err);
			}
		},
		
		serializeForm : function(formId) {
			var content = $("#" + formId).serialize();
			return content;
		},
		
		showDialogHelp : function() {
			
		},
		
		getContentEditableText : function(jqelement) {
		    var ce = $("<pre />").html(jqelement.html());
		    try {
		        var browser = navigator.userAgent.toLowerCase();
				var isMozilla = /mozilla/.test(browser);
				var isIE = /msie/.test(browser);
				var isWebkit = /webkit/.test(browser);
		        
		        if (isWebkit)
					ce.find("div").replaceWith(function() { return "\n" + this.innerHTML; });
				if (isIE)
					ce.find("p").replaceWith(function() { return this.innerHTML + "<br>"; });
				if (isMozilla)
					ce.find("br").replaceWith("\n");
		        
		    } catch(err) {
		        console.error(err);
		    }
		    return ce.text();
		},
		
		extractContentEditableFromForm : function(formId) {
			var parms = [], index=0;
			$("#" + formId).find(".inputor").each(function() {
				parms[index++] = "&";
				parms[index++] = this.id;
				parms[index++] = "=";
				
				var str = getContentEditableText($(this));
				//var str = $(this).text();
				//str = str.replace(/\u00a0/g, " ");
//				var send = escape(str);
				parms[index++] = str;
			});
			var str = parms.join("");
			return str;
		},
		
		//Display Dialog for updating properties
		showPropertyUpdateDialog: function(nid, aid) {
			var title = "<b>Update " + aid +"</b>";
						
			var msg = [];
			msg[msg.length] = '<div id="propertyUpdateContent">';
			msg[msg.length] = '<center><img src="/img/bigwait.gif" /></center>';
			msg[msg.length] = '</div>';

			var self = this;
			this.dialogHandle = BootstrapDialog.show({
		        type: BootstrapDialog.TYPE_DEFAULT,
		        title: title,
		        message: msg.join(""),
		        onhide: function(dialog){
	            },
	            onshow: function(dialogRef){
//	                alert('Dialog is popping up, its message is ' + dialogRef.getMessage());
	            	self.ajaxCallForData(nid, aid, self, dialogRef);
	            },
				buttons: [{
				          label: 'Update',
				          cssClass: 'btn-primary',
				          action : function(dialog) {
				        	  
				        	  if (self.updatePropertyCallback != null) {
				        		  var content = self.serializeForm("processModelPropertyDialog");
				        		  var msgs = self.extractContentEditableFromForm("processModelPropertyDialog");
				        		  content += ((content.length > 0)? "&" : "") + msgs;
				        		  self.ajaxCallToUpdateProperty(nid, aid, content, self, self.dialogHandle);
				        	  }
				          }
					}, {
				          label: 'Cancel',
				          action : function(dialog) {
				        	  self.dialogHandle.close();
				          }
					}
				]
		    });
			
			//Load content
		},
		
		camelCaseToText: function(camelCaseText) {
			return camelCaseText.replace(/([A-Z])/g, ' $1').replace(/^./, function(str){ 
				return str.toUpperCase(); 
			});
		},
		
		ajaxCallForData: function(nid, aid, that, dialogHandle) {
			var data2Send = "aid=" + aid + "&act=propdata&nid=" + nid;
			var status = $.ajax({
				type : "POST",
				url : that.serverurl,
				async : true,
				dataType : "json",
				data : data2Send
			}).done(function(data) {
				if (typeof data.status !== 'undefined') {
					if (data.status == 'fail' && data.message == 'USER_INVALID') {
						resetAndLogout();
						return;
					}
				}
				resetSessionTimeout();
				that.updateDialog(data, that, dialogHandle);
			});
		},
		
		/** Called when UPDATE button pressed **/
		ajaxCallToUpdateProperty: function(nid, aid, content, that, dialogHandle) {
			var data2Send = "aid=" + aid + "&act=propupd&nid=" + nid + "&" + content;
			var status = $.ajax({
				type : "POST",
				url : that.serverurl,
				async : true,
				dataType : "json",
				data : data2Send
			}).done(function(data) {
				if (typeof data.status !== 'undefined') {
					if (data.status == 'fail' && data.message == 'USER_INVALID') {
						resetAndLogout();
						return;
					}
				}
				resetSessionTimeout();
				that.messageDataSaved(data, that, dialogHandle, nid);
				
			});
		},
		
		//Form Builder
		returnCodeOption : function(existingCodes) {
			var html = [];
			for(var rci=0; rci<this.returnCodes.length; rci++) {
				var process = true;
				if (existingCodes != null) {
					for (var ix=0; ix<existingCodes.length; ix++) {
						if (existingCodes[ix] == this.returnCodes[rci]) {
							process = false;
							break;
						}
					}
				}
				if (process) {
					var text = this.camelCaseToText(this.returnCodes[rci]);
					html[html.length] = '	<option value="'+this.returnCodes[rci]+'">'+text+'</option>';
				}
			}
			return html.join("");
		},
		
		returnCodeSelectHtml : function(existingCodes) {
			var html = [];
			
			html[html.length] = '<select id="returncode" name="returncode" class="selectpicker" name="returncode">';
			html[html.length] = this.returnCodeOption(existingCodes);
			html[html.length] = '</select>';
			
			return html.join("");
		},
		
		addReturnCodeHtml: function(existingCodes, nid, aid) {
			var html = [];
			html[html.length] = '<div class="panel panel-default">';
			html[html.length] = '	<div class="panel-heading permissions-panel-heading">';
			html[html.length] = '		<div class="row">';
			
			html[html.length] = '			<div class="col-md-3 col-lg-3">';
			html[html.length] = '					<label for="returncode" style="margin-top:5px;">Return Code</label>';
			html[html.length] = '			</div>';	//col-md-4 col-lg-4
			
			html[html.length] = '			<div id="returnCodeContainer" class="col-md-4 col-lg-4" style="margin-left:-20px;">';
			
			html[html.length] = this.returnCodeSelectHtml(existingCodes);

			html[html.length] = '			</div>';	//col-md-4 col-lg-4
			html[html.length] = '';
			
			html[html.length] = '			<div class="col-md-offset-4 col-lg-offset-4" style="position:relative">';
			html[html.length] = '				<span style="position:absolute; top:5px; right:2px;">';
			html[html.length] = '					<button id="btnAddReturnCode" type="button" class="btn btn-success btn-xs" style="margin-right: 10px;" onclick="javascript:processModel.messageMapButtonClicked(\''+nid+'\', \''+aid+'\')">';
			html[html.length] = '						<span class="glyphicon glyphicon-plus"></span>';
			html[html.length] = '					</button>';
			html[html.length] = '				</span>';
			html[html.length] = '			</div>';
			
			html[html.length] = '		</div>';	//row
			html[html.length] = '	</div>';		//panel-heading
			html[html.length] = '</div>'; 			//panel panel-default
					
			return html.join("");
		},
		
		updateErrorMessageDialogPlaceholdText : function(element) {
			try {
				var el = $(element);
				var id = el.attr("id");
				var arr = id.split("_");
				var lang = arr[3];
				var text =el.val();
				
				if (text.length == 0) {
					text = this.defaultErrorMessage[0];
					$("#" + id).attr("placeholder", text);
				}
				this.defaultErrorMessage[lang] = text;				
				
				$("#processModelPropertyDialog input[type='text']").each(function(ev) {
					var id = this.id;
					var arr2 = id.split("_");
					if (arr2.length == 4) {
						if ((arr2[3] == lang) && (arr2[2] != 'msg')) {
							$("#" + id).attr("placeholder", text);
						}
					}
				});
			} catch(err) {
				console.error(err);
			}
		},
		
		buildMessageInputHTML : function(id, name, label, placeHolderText, text) {
			
			// Get language direction
			var index = id[id.length - 1] - 1;
			var dir = "ltr";
			if (this.languageData.dir.length && index >= 0 && index < this.languageData.dir.length)
				dir = this.languageData.dir[index];
			
			var html = [];
			html[html.length] = '<div class="form-group" style="margin-left: 10px; margin-right:10px;">';
			html[html.length] = "<label for='" + id + "'>" + label + "</label>" ;
			html[html.length] = "<div lang='EN' contenteditable='true' id='" + id + "' ";
			html[html.length] = "name='" + name + "' spellcheck='false' dir='" + dir + "' class='ui-corner-all typeaheadsm inputor'>";
			html[html.length] = text;
			html[html.length] = "</div>";
			html[html.length] = "</div>";
			return html.join("");
		},
		
		//Create message content
		messageContent: function(nid, aid, code, text, defaultText, isDefault) {
			var html = [];
						
//			for(var j=0; j<text.length; j++) {
			for(var j=1; j< this.languageData.lang.length+1; j++) {
				var inputId = nid + "_" + aid + "_" + code + "_" + j;
				var langName = (j==0)? "Context" : this.languageData.lang[j-1];
				if (langName != "") {
					var placeHolderText = (defaultText[j].length == 0)? defaultText[0] : defaultText[j];
					var value = "";
					if (typeof text !== 'undefined') {
						value = text[j];						
					} else if (this.defaultErrorMessage != null) {
						value = this.defaultErrorMessage[j];	
					}
					html[html.length] = this.buildMessageInputHTML(inputId, inputId, langName, placeHolderText, value);
					
//					html[html.length] = '<div class="form-group" style="margin-left:20px; ">';
//					html[html.length] = '<label for="'+inputId+'" >'+langName+'</label>';
//					
//					html[html.length] = '<input type="text" class="form-control inputor modalInput" id="'+ inputId +'" name="'+inputId+'" placeholder="'+placeHolderText+'" onfocus="initMessaging();" ';
//					
//					if (isDefault) {
//						html[html.length] = ' onchange="javascript:processModel.updateErrorMessageDialogPlaceholdText(this);" ';
//					}
//					
//					html[html.length] = 'value="';
//					if (typeof text !== 'undefined') {
//						html[html.length] = text[j];						
//					} else if (this.defaultErrorMessage != null) {
//						html[html.length] = this.defaultErrorMessage[j];	
//					}
					
					//Update text messages
					
//					html[html.length] = '" />';
					html[html.length] = '<script>initMessaging();</script>';
				}
			}
			return html.join("");
		},
		
		//Inner accordian content
		acordianPanel: function(nid, aid, code, titleId, title, text, isDefault) {
			var html = [];
			html[html.length] = '	<div id="acc_' + titleId + '" class="panel panel-default">';
			html[html.length] = '		<div class="panel-heading permissions-panel-heading accordion-heading">';
			html[html.length] = '			<h4 class="panel-title permissions-panel-title">';
			html[html.length] = '				<a data-toggle="collapse" data-parent="#messagesAccordionPanel" href="#'+titleId+'">' + title + "</a>";
			html[html.length] = '			</h4>';
			
			if (titleId != "DefaultMessage") {
				html[html.length] = '			<button type="button" class="btn btn-danger btn-xs btn-acc"  onclick="javascript:processModel.messageMapButtonRemove(\'acc_' + titleId +'\')">';
				html[html.length] = '				<span class="glyphicon glyphicon-remove"></span>';
				html[html.length] = '			</button>';
			}
			
			html[html.length] = '		</div>';
			
			html[html.length] = '		<div id="'+titleId+'" class="panel-collapse collapse" >';
			html[html.length] = '			<div class="panel-body" style="margin-left:10px; margin-right:10px;">';
			
			html[html.length] = this.messageContent(nid, aid, code, text, this.defaultErrorMessage, isDefault);				
							
			html[html.length] = '           </div>'; 	//panel-body 
			html[html.length] = '		</div>'; 	//panel-collapse 	
			html[html.length] = '	</div>';		//panel panel-default
			return html.join("");
		},
		
		messageMapPanelContentHtml: function(data) {
			
			//Extract exising codes
			this.usedMessageErrorCodes = [];
			for(var i=0; i<data.data.length; i++) {
				this.usedMessageErrorCodes[this.usedMessageErrorCodes.length] =  data.data[i].code;
			}

			var html = [];
			html[html.length] = '<form id="processModelPropertyDialog" class="form-horizontal">';
			html[html.length] = '<fieldset>';
			html[html.length] = this.addReturnCodeHtml(this.usedMessageErrorCodes, data.nid, data.aid);
			html[html.length] = '<div class="panel-group" id="messagesAccordionPanel">';
			
			for(var i=0; i<data.data.length; i++) {
				var title = (data.data[i].code == 'msg')? "Default Message" : data.data[i].code;
				if (data.data[i].code == 'msg') {
					this.defaultErrorMessage = data.data[i].text;
				}				
				var titleId = title.replace(" ", "");

				title = this.camelCaseToText(title);
				html[html.length] = this.acordianPanel(data.nid, data.aid, data.data[i].code, titleId, title, data.data[i].text, (i==0));
			}

			html[html.length] = '<div id="insertErrorHere" style="margin-top:5px;margin-bottom:5px;"></div>';
			
			html[html.length] = '</div>';	//Main (accordion)
			
			/*
			<div class="panel-group" id="accordion">
				<div class="panel panel-default" th:each="permCat : ${permCats}">
					<div class="panel-heading permissions-panel-heading">
					    <h4 class="panel-title permissions-panel-title">
        					<a data-toggle="collapse" data-parent="#accordion" th:href="${'#' + #strings.replace(permCat.description,' ', '')}" th:text="${permCat.description}">Collapsible Group Item #1</a>
        				</h4>
					</div>
					<div th:id="${#strings.replace(permCat.description,' ', '')}" class="panel-collapse collapse">
						<div class="panel-body">
							<div th:each="perm : ${permCat.permissions}" class="row checkbox roleindent">
								<input type="checkbox" id="tl"  th:id="${'viewperm_' + perm.permId}" th:text="${perm.description}" th:disabled="${true}" />
							</div>
						</div>
					</div>
				</div>
			</div>			 
			 */
			html[html.length] = '</fieldset>';
			html[html.length] = '</form>';
			
			//Refresh selectpickers
			html[html.length] = "<script>refreshSelectPickers();</script>";
			
			var endHtml = html.join("");
			return html.join("");
		},
		
		symbolHelpHtml : function(symbol, description) {
			var html = [];
			
			html[html.length] = "<div class='row'>";
			html[html.length] = "	<div class='col-md-2'><center><b>"+symbol+"</b></center></div>";
			html[html.length] = "	<div class='col-md-8'>"+description+"</div>";
			html[html.length] = "</div>";
			
			return html.join("");
		},
		
		captionContentHtml: function(data) {
			var html = [];
			var helpLink = null;

			if (data.ntype == "MenuItem" && data.aid == "Text") {
				html[html.length] = "<div class='alert alert-info'>";
				html[html.length] = "<div class='row' style='padding-left:10px;'>The following symbols are available for Menu Items</div>";
				html[html.length] = this.symbolHelpHtml("#", "Auto-generated item number");
				html[html.length] = this.symbolHelpHtml("##", "Display #");
				html[html.length] = this.symbolHelpHtml("#*", "Display *");
				html[html.length] = this.symbolHelpHtml("#n", "Display fixed number (e.g. #2 = 2)");
				html[html.length] = "</div>";	//End Alert
			} else if (data.ntype == "MenuItems" && data.aid == "Text") {
				html[html.length] = "<div class='alert alert-info'>";
				html[html.length] = "<div class='row' style='padding-left:10px;'>The following symbols are available for Menu Lists</div>";
				html[html.length] = this.symbolHelpHtml("%s", "Place holder for list items provided by Input");
				html[html.length] = "</div>";	//End Alert
			}
			
			html[html.length] = '<div class="row">';
			html[html.length] = '<div class="col-md-12">';
			
			html[html.length] = '<form id="processModelPropertyDialog" class="form-horizontal">';
			html[html.length] = '<fieldset>';

			for(var i=0; i<data.data[0].text.length; i++) {
				var langName = (i==0)? "Context" : (this.languageData.lang[i-1] == "")? "Unspecified (changed in Locale settings)" : this.languageData.lang[i-1];
				var inputId = data.nid + "_" + data.aid + "_" + data.data[0].code + "_" + i;
				
				html[html.length] = this.buildMessageInputHTML(inputId, inputId, langName, "Enter Text", data.data[0].text[i]);
				
//				html[html.length] = '<div class="form-group" style="margin-left: 10px; margin-right:10px;">';
//				html[html.length] = '<label for="'+inputId+'">'+langName+'</label>';
//				html[html.length] = '<input type="text" class="form-control inputor modalInput" id="'+ inputId +'" name="'+inputId+'" placeholder="Enter Text" onfocus="initMessaging();" ';
//				html[html.length] = 'value="';
//				html[html.length] = data.data[0].text[i];
//				html[html.length] = '" />';
//				html[html.length] = '</div>';
			}
			
			html[html.length] = '</fieldset>';
			html[html.length] = '</form>';
			html[html.length] = '</div>';
			html[html.length] = '</div><script>initMessaging();</script>';
			
			return html.join("");
		},
		
		messageMapHtml: function(data) {
			var html = [];
			return html.join("");
		},
		
		// Error Message added here
		messageMapButtonClicked: function(nid, aid) {
			var code = $("#returncode").val();
			var text = $("#returncode :selected").text();
			
			var html = this.acordianPanel(nid, aid, code, code, text, false);
			$("#insertErrorHere").append(html);
			
			//Remove Id from current list
			this.usedMessageErrorCodes[this.usedMessageErrorCodes.length] = code;
			
			var optionsHtml = this.returnCodeOption(this.usedMessageErrorCodes);
			$("#returncode").html(optionsHtml);
			$('.selectpicker').selectpicker('refresh');
			
		},
		
		messageMapButtonRemove: function(titleId) {
			try {
				$("#" + titleId).remove();
			} catch(err) {
				console.error(err);
			}
		},
		
		bindMessageMapButton: function(ref) {
			try {
				$("#btnAddReturnCode").on({
//					'click':  $.proxy(this.messageMapButtonClicked, this)
					'click' : function() {
						alert("sd");
					}
				});
			} catch(err) {
				console.error(err);
			}
		},
		
		updateDialogTitle : function(dialogRef, data) {
			var addHelp = false;
			var title = "<b>Update " + data.aid +"</b>";
			title += "<span id='propertyUpdateHelp' class='glyphicon glyphicon-question-sign' style='margin-left:10px; color:blue; cursor:pointer;' onclick='processModel.showDialogHelp();'></span>";
			
			if (typeof data !== "undefined") {
				if (data.ntype == "MenuItem" ) {
					addHelp = true;
				}
			}
			
			if (addHelp) {
				dialogRef.getModalHeader().html(title);
			}
		},
		
		updateDialog: function(data, that, dialogRef) {
			try {
				var html = "Problem loading content [content type not found]";
				if (data.ptype == "ITexts") {
					html = that.captionContentHtml(data);
				} else if (data.ptype == "MessageMap") {
					html = that.messageMapPanelContentHtml(data);
				}
				dialogRef.getModalBody().html(html);
			} catch(err) {
				console.error(err);
			}
		},
		
		messageDataSaved: function(data, that, dialogRef, nid) {
			try {
				if (data.status == 'pass') {
					dialogRef.close();
					dataUpdated();
					loadNodeIdContent("act_" + nid);
				} else {
					alert(data.message);
				}
			} catch(err) {
				console.error("messageDataSaved: " + err);
			}
		}
	};
	
	$.fn.c4uprocessgrid = function(option, event) {
		//get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this.each(function() {
			var $this = $(this),
				data = $this.data('c4uprocessgrid'),
				options = typeof option == 'object' && option;
			
//				$.extend({}, $.fn.c4uprocessgrid.defaults, options, $(this).data());
				if (!data) {
					var mergedOptions = $.extend({}, $.fn.c4uprocessgrid.defaults, options, $(this).data());
	                $this.data('c4uprocessgrid', (data = new C4UProgressGrid(this, mergedOptions, event)));
	            }
			
				result = data;
			var elementId = $(this).attr("id");
		});
		return result;
	};
	
	$.fn.c4uprocessgrid.defaults = {
		retrieveMethod: null,				// REMOVE
		serverurl: null,					// 
		updatePropertyCallback: null,		// 
		languageData: null,					// 
		returnCodes: []
	};
	
//	var htmlOriginal = $.fn.html;
//	$.fn.html = function(html,callback){
//		var ret = htmlOriginal.apply(this, arguments);
//		if(typeof callback == "function"){
//			callback();
//		}
//		// make sure chaining is not broken
//		return ret;
//	};
}(jQuery, window, document);

//(function($) {
//	var eventName = 'html-change';
//	// Save a reference to the original html function
//	jQuery.fn.originalHtml = jQuery.fn.html;
//	// Let's redefine the html function to include a custom event
//	jQuery.fn.html = function() {
//		var currentHtml = this.originalHtml();
//		if(arguments.length) {
//			this.trigger(eventName + '-pre', jQuery.merge([currentHtml], arguments));
//			jQuery.fn.originalHtml.apply(this, arguments);
//			this.trigger(eventName + '-post', jQuery.merge([currentHtml], arguments));
//			return this;
//		} else {
//			return currentHtml;
//		}
//	}
//})(jQuery);