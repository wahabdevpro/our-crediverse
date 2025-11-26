var collapseGlyph = "glyphicon-chevron-down";
var expandGlyph = "glyphicon-chevron-right";
var prevItem = null;

var INFO_CLASS    = "alert-info";
var SUCCESS_CLASS = "alert-success";
var FAIL_CLASS    = "alert-danger";

var MAX_INPUT = 512;
var MAX_INPUT_ERROR_MSG = "Cannot exceed 512 Characters";

var configDataToBeSaved = false;
var updateTabMenuOption = false;

function expandCollapse(evt) {
	$(evt).find("i").toggleClass(collapseGlyph + ' ' +  expandGlyph);
}

function activate(evt) {
	if(prevItem != null){
	   $(prevItem).removeClass("tree-item");
	}
	$(evt).addClass("tree-item");
	prevItem = evt;
}

var lastUpdate = {
	uid : 0,
	version : 0
};

//Load Left menu

var loadConfigurationMenu = function(menu) {
	try {
		var esc = escape(menu);
		var url = "/sysconfigloader";
		var postdata = "content=menu&menu="+esc;
		sendAsyncAjax(url, postdata, function(data) {
			try {
				if (typeof data.system !== 'undefined') {
					if ((data != null) && (typeof data.menu !== 'undefined') && (data.menu != null)) {
						data.menu.sort(
							function(a,b) {
								return a.name.localeCompare(b.name);
							}
						);
						
						if (data.lastuid == "0" && data.menu.length > 0) {
							data.lastuid = data.menu[0].uid;
						} else {
							updateTabMenuOption = true;
						}
					}	//Data null check
					
					var html = buildNavSideMenu(menu,data);
					
					//Menu Content
					$("#menu_contents").html(html);
					
					//Menu Actions
					$(".sidenavmenu").on("click", function(event) {
						clearLastTabEvent();
						var id = $(this).attr("id");
						activateMenu(id);
					});
					var serviceName = findLoadedServiceName(data);
					clearLastTabEvent();
					activateMenu(data.lastuid);
//					loadConfigurationContent(data.lastuid, serviceName);				
				}	
			} catch(err) {
				console.error("loadConfigurationMenu.sendAsyncAjax : "+ err);
			}
		}, function(error) {
			
		});
	} catch(err) {
		try {
			console.error("loadConfigurationMenu: " + err);
		} catch(error) {
			alert("loadConfigurationMenu: " + err);			
		}
	}
};

var findLoadedServiceName = function(data) {
	var serviceName = "";
	for(var i=0; i<data.menu.length; i++) {
		if (data.lastuid == data.menu[i].uid) {
			serviceName = data.menu[i].name;
			break;
		}
		if (data.menu[i].child.length > 0) {
			for(var j=0; j<data.menu[i].child.length; j++) {
				if (data.lastuid == data.menu[i].child[j].uid) {
					serviceName = data.menu[i].child[j].name;
					break;
				}
			}
		}
	}
	return serviceName;
};

var lastMenuUid = "";
var activateMenu = function(id) {
	var ids = id.split("_");
	var serviceName = $("#" + id).html();
	if(typeof String.prototype.trim !== 'function') {
		  String.prototype.trim = function() {
		    return this.replace(/^\s+|\s+$/g, ''); 
		  };
	}
	serviceName = serviceName.trim();
	
	//first check for navigating away?
	if (configDataToBeSaved) {
		mustInavigateAway(id, serviceName);
		return;
	}
	
	//Close previous menu
	$("#li" + lastMenuUid).removeClass("active");

	if (lastMenuUid.indexOf("_" >0)) {
		var lids = lastMenuUid.split("_");
		if (lids[0] != ids[0]) {
			$("#li" + lids[0]).removeClass("sub-active");
			$("#li" + lids[0]).removeClass("sub");
		}
	}
	
	//Open New menu
	if (ids.length == 2) {
		$("#li" + ids[0]).addClass("sub-active sub");
	}

	$("#li" + id).addClass("active");
	
	//Load Content
	lastMenuUid = id;
	loadConfigurationContent(id.trim(), serviceName);
};

var buildNavSideMenu = function(menu,data) {
	var system = data.system;
	var lastuid = data.lastuid;
	var html = [];
	
	lastMenuUid = lastuid + "";
	
	try {
		html[html.length] = "<div class='nav-main fw-nav' id='configmenu' value='" + menu + "'>";
		html[html.length] = "<ul>";
		for(var i=0; i<data.menu.length; i++) {
			html[html.length] = "\n<li ";
			html[html.length] = "id='li" + data.menu[i].uid + "' ";
			html[html.length] = "class='";

			if (data.menu[i].child.length > 0) {
				html[html.length] = "sub-menu-avail";	//sub-active active
			}
			
			if (lastuid == data.menu[i].uid) {
				html[html.length] = " active";
			}
			html[html.length] = "'>";
			html[html.length] = "	<a class='sidenavmenu' id='";
			html[html.length] = data.menu[i].uid;
			html[html.length] = "'>";
			
			html[html.length] = data.menu[i].name;
			html[html.length] = "	</a>";
			if (data.menu[i].child.length > 0) {
				html[html.length] = "<ul>";
				for(var j=0; j<data.menu[i].child.length; j++) {
					html[html.length] = "<li ";
					html[html.length] = "id='li" + data.menu[i].uid + "_" + data.menu[i].child[j].uid +"' ";
					html[html.length] = "class=''>";
					html[html.length] = "	<a class='sidenavmenu' id='" + data.menu[i].uid + "_" + data.menu[i].child[j].uid + "'>";
					html[html.length] = data.menu[i].child[j].name;
					html[html.length] = "	</a>";
					html[html.length] = "</li>";
				}
				html[html.length] = "</ul>";
			}
			html[html.length] = "</li>";
		}
		
		html[html.length] = "</ul>";
		html[html.length] = "</div>";
	} catch(err) {
		console.error("buildNavSideMenu: " + err);
	}
	return html.join("");
};

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




function dataUpdated() {
	try {
		configDataToBeSaved = true;
		$("#submit_config").prop('disabled', false);
		$("#submit_msg").prop('disabled', false);
	} catch(err) {
		alert(err);
	}
}

var waitAndRegisterInputs = function() {
	var timer;
	var delay = 600;
	timer = window.setTimeout(function() {
		try {
			registerInputs();
		} catch(err) {}
	}, delay);
};

var registerInputs = function() {
	$("input[type='text']").not(".modalInput").on('change input', function() {
		var id = $(this).attr("id");
		dataUpdated();
	});
};

$('#sidebar').affix({
    offset: {
      top: 0
    }
});	


var updateAlert = function(cid, msg, alertType, fade) {
	try {
		//First remove all classes
		$("#" + cid).removeClass(INFO_CLASS);
		$("#" + cid).removeClass(SUCCESS_CLASS);
		$("#" + cid).removeClass(FAIL_CLASS);
		
		$("#" + cid).addClass("alert");
		$("#" + cid).html(msg);
		$("#" + cid).addClass(alertType);
		$("#" + cid).show();
		if (fade) {
			$("#" + cid).fadeOut(2000);
		}
	} catch(err) {
		alert(err);
	}
};

var createErrorMessageAlert = function(msg) {
	var html = [];
	html[html.length] = "<div class='alert alert-warning alert-dismissible' role='alert'>";
	html[html.length] = "<button type='button' class='close' data-dismiss='alert'>";
	html[html.length] = "	<span aria-hidden='true' class='glyphicon glyphicon-remove'></span><span class='sr-only'>Close</span>";
	html[html.length] = "</button>";
	html[html.length] = msg;
	html[html.length] = "</div>";
	return html.join("");
};

//Called when clicking on left navigation menu
var loadConfigurationContent = function(uid, title, reload) {
	try {
		if (configDataToBeSaved && (typeof reload == 'undefined')) {
			mustInavigateAway(uid, title);
			return;
		}
		
		lastUpdate.uid = uid;
		lastUpdate.version = 0;
		//updateTabMenuOption
		var pagesLoaded = 0;
		
		//Clear current Errors
		fieldErrorHelper.clearErrors();

		$("#content-configuration").html(createLoadingIconHtml());
		$("#content-configuration").load("/sysconfigloader?content=config&uid="+uid, function( response, status, xhr ) {
			if (status == 'error') {
				$("#content-configuration").html(createErrorMessageAlert("Server Error prevented configuration retrieval"));
			}
			if (reload != null) {
				updateAlert("update_msg_config", "Configuration data refreshed", INFO_CLASS, true);
			}

			$("#submit_config").prop('disabled', true);
			configDataToBeSaved = false;
			registerTabEventListener();
			pagesLoaded++;
			if (pagesLoaded >=2) {
				if (updateTabMenuOption) {
					loadAndshowLastTab();
				}
			}
		});
		$("#msg-configuration").html(createLoadingIconHtml());
		$("#msg-configuration").load("/sysconfigloader?content=msg&uid="+uid, function( response, status, xhr ) {
			if (status == 'error') {
				$("#msg-configuration").html(createErrorMessageAlert("Server Error prevented configuration retrieval"));
			}
			if (reload != null) {
				updateAlert("update_msg_not", "Configuration data refreshed", INFO_CLASS, true);
			}
			$("#submit_msg").prop('disabled', true);
			configDataToBeSaved = false;
			pagesLoaded++;
			if (pagesLoaded >=2) {
				if (updateTabMenuOption) {
					loadAndshowLastTab();
				}
			}
		});
		$("#service_heading").html(title);
		registerTabEventListener();
	} catch(err) {
		alert(err);
	}
};

var mustInavigateAway = function(uid, title, newmenu, newsystem) {
	try {
		var msg = [];
		msg[msg.length] = '<div style="height:60px;"><div style="float:left; width:80px;margin:10px auto; height:50px; padding-left:20px;"><img src="/img/timeout-orange.png"/></div>';
		msg[msg.length] = '<div style="float:left; width:400px; padding-left:30px; padding-top:10px;">';
		msg[msg.length] = '<p>You have unsaved configuration data. Naviagating away from this page will cause changes to be lost</p>';
		msg[msg.length] = '<p style="font-weight: bold">Would you still like to navigate away?</p>';
		msg[msg.length] = '</div></div>';
		
		var dialog = BootstrapDialog.show({
	        type: BootstrapDialog.TYPE_DEFAULT,
	        title: "<b>Navigate away?</b>",
	        message: msg.join(""),
	        onhide: function(dialog){
            },
			buttons: [{
			          label: 'Yes, loose changes',
			          cssClass: 'btn-danger',
			          action : function(dialog) {
			        	  //newLocation?
			        	  configDataToBeSaved = false;
			        	  dialog.close();
			        	  if (typeof newmenu == 'undefined' || newmenu == null)
			        		  activateMenu(uid);
			        	  else {
			        		  configDataToBeSaved = false;
			        		  navigateTo(newmenu, newsystem, true);
			        	  }
			        		  
			          }
				}, {
			          label: 'No, stay on page',
			          action : function(dialog) {
			        	  dialog.close();
			          }
				}
			]
	    });
	} catch(err) {
		alert(err);
	}
};

var btnFormStore = {};

var attachSave = function(buttonId, contentForm) {
	try {
		btnFormStore[buttonId] = contentForm;
	} catch(err) {
		alert(err);
	}
	
	
	$("#" + buttonId).click(function() {
		try {
			var id = this.id;
			var form = btnFormStore[id];
			validateAndSave(form);
		} catch(err) {
			alert(err);
		}

//		validateAndSave(contentForm);
	});
};

var showUpdateWell = function(updateCalled, isConfig) {
	try {
		var updateButtonID = (isConfig)? "update_buttons_config" : "update_buttons_msg";
		var busyUpdateID = (isConfig)? "busy_updating_config" : "busy_updating_msg";
		if (updateCalled) {
			$("#" + updateButtonID).addClass("hide");
			$("#" + busyUpdateID).removeClass("hide");
		} else {
			$("#" + updateButtonID).removeClass("hide");
			$("#" + busyUpdateID).addClass("hide");	
		}	
	} catch(err) {
		alert(err);
	}
};

//Pesist data back to server
var lastComponent = null;
var validateAndSave = function(comp, uid, version) {

	resetSessionTimeout();
	
	var forms = comp.split(",");
	comp = forms[0]; 
	
	try {
		var msgData = extractNotifications();
		
		//Validate that none of the fields exceed 512 characters
		var errorFound = false;
		
		$('input').each(function(index,data) {
			
			try {
				var value = $(this).val();
				if ($(this).is("input")) {
					if (typeof this.id !== "undefined" && this.id != null && this.id.length > 0) {
						if (value.length > MAX_INPUT) {
							errorFound = true;
							$("#" + this.id).parent("div").addClass("has-error");
							var element =$("#" + this.id + "_error");
							element.removeClass("hide");
							element.html(MAX_INPUT_ERROR_MSG);
						}
						else {
							$("#" + this.id).parent("div").removeClass("has-error");
							var element =$("#" + this.id + "_error");
							element.addClass("hide");
						}
					}
				}
			} catch(err){
				console.error("validateAndSave: " + err);
			}
			
		});
		
		
		if (errorFound) {
			return;
		}
//		var requestData = serializeForm(comp + "Form");

		var requestData = "";
		for(var i=0; i<forms.length; i++) {
			if (requestData.length > 0) {
				requestData += "&";
			}
			requestData += serializeForm(forms[i] + "Form");
		}
		
		
		
		var url = "/sysconfigloader";
		if (comp=='msg') {
			url = "/msgpersist";
		}
		
		lastComponent = comp;
		
		if (lastUpdate.version > version) {
			version = lastUpdate.version;
		}
		showUpdateWell(true, (lastComponent=="config"));
		startProgressTimer();	//Start Count up

		var status = $.ajax({
			type: "POST", 
			url: url, 
			async: true, 
			data: "config_comp=" + comp + "&config_act=upd&config_uid=" + uid  + "&config_ver=" + version + "&"+requestData + msgData,
			dataType: "json"
		}).done(function(data) {
			try {
				clearProgressTimer();
				resetSessionTimeout();
				if (data.version != null) {
					lastUpdate.version = data.version;
				}
				
				$("#waitingPassword").addClass("hide");
				$("#update_msg").addClass("alert");
				if (lastComponent=="config") {
					displayMessage(data, "update_msg_config");
				} else {
					displayMessage(data, "update_msg_not");
				}
				showUpdateWell(false, (lastComponent=="config"));
				if (typeof data.update !== "undefined" && data.update == "success") {
					reloadConfigurationMenu();
				}
			} catch(err) {
				alert("validateAndSave.status: " + err);
			}
		});
	} catch(err) {
		alert("validateAndSave: " + err);
	}
};

//----------------------------- Field Error Resolver
var fieldErrorHelper = {
	previousErrorField : null,
	currentErrorField : null,
	errorListeners : [],
	
	registerListener : function(observer) {
		this.errorListeners.push( observer );
	},
	
	clearErrors : function() {
		this.errorListeners = [];
		this.previousErrorField = null;
		this.currentErrorField  = null;
	},
	
	extractFieldCount : function() {
		var fields = this.currentErrorField.split(".");
		return fields.length;
	},
	
	//this.currentErrorField.
	extractErrorField : function(field, fieldIndex, includeIndex) {
		if (field == null)
			return null;
		try {
			var fields = field.split(".");
			if (fieldIndex >=0) {
				var result = fields[fieldIndex];
				if (typeof includeIndex !== "undefined" && (!includeIndex)) {
					result = result.substring(0, result.indexOf("["));
				}
				return result;
			}
		} catch(err) {
			console.error("extractErrorField: " + err);
		}
		return null;
	},
	
	extractErrorFieldIndex : function(field, fieldIndex) {
		
		if (field == null)
			return -1;
		var index = -1;
		try {
			var result = this.extractErrorField(field, fieldIndex, true);
			
			if (result.indexOf("[") > 0) {
				index = parseInt(result.substring(result.indexOf("[") + 1, result.indexOf("]")));
			}
		} catch(err) {
			console.error("extractErrorFieldIndex: " + err);
		}
		return index;
	},
	
	updateErrorField : function(errorField) {
		this.previousErrorField = this.currentErrorField;
		this.currentErrorField = errorField;
		if (this.currentErrorField != null) {
			//TODO: HIGHLIGHT FIELD
		}
		this.notifyListeners(this.extractErrorField(this.currentErrorField, 0, false), this.extractErrorFieldIndex(this.currentErrorField, 0));
	},
	
	notifyListeners: function(fieldComponent, index) {
		if (this.errorListeners != null) {
			for( var i = 0; i < this.errorListeners.length; i++ ) {
				this.errorListeners[i](fieldComponent, index);
			}
		}
	}
};

var displayMessage = function(res, msgId) {
	try {
		if (res.update!="success") {
			var errorMessage = res.error;
			if (typeof res.field !== "undefined") {
				
				errorMessage = "Value rejected: " + res.error;
				fieldErrorHelper.updateErrorField(res.field);
			}
			updateAlert(msgId, errorMessage, FAIL_CLASS, false);
		} else {
			fieldErrorHelper.updateErrorField(null);
			lastUpdate.version = res.version;
			updateAlert(msgId, "Update successful!", SUCCESS_CLASS, true);
			$("#submit_config").prop('disabled', true);
			$("#submit_msg").prop('disabled', true);
			configDataToBeSaved = false;	//Reset Nothing to save
		}
	} catch(err) {
		alert(err);
	}
};

var callMethod = function(uid, method) 
{
	try {
		var url = "/sysconfigloader";
		
		showUpdateWell(true, true);
		
		
		var status = $.ajax({
			type: "POST", 
			url: url, 
			async: true, 
			data: "config_uid=" + uid  + "&config_method=" + method,
			dataType: "json"
		}).done(function(data) {
			resetSessionTimeout();
			if (data.update == "success") {
				updateAlert("update_msg_config", data.message, SUCCESS_CLASS, true);
			} else {
				updateAlert("update_msg_config", data.message, FAIL_CLASS, false);
			}
			showUpdateWell(false, true);
		});
	} catch(err) {
		alert(err);
	}
};


//For messages
var convertToContentEditable = function(text) {
	var index = 0, len = text.length,  varread = false, variable = [], htm = [];
	try {
		while(index<=len) {
			var currentChar = text.charAt(index);
			if (currentChar == '{') {
				variable = [];
				varread = true;
			} else if (currentChar == '}' && varread) {
				htm[htm.length] = '<span contenteditable="true">';
				htm[htm.length] = '<span class="atwho-view-flag atwho-view-flag-at-mentions" contenteditable="false">';
				htm[htm.length] = '<span class="variable-formatting">{'+variable.join('')+'}</span>';
				htm[htm.length] = '</span></span>';
				varread = false;
			} else if (varread) {
				variable[variable.length] = currentChar;
			} else {
				htm[htm.length] = currentChar;
			}
			index++;
		}
		return htm.join('');
	} catch(err) {
		alert(err);
	}
	return "";
};

//For update of progress bar
var updateProgressTimer = null;
var percentValue;

var startProgressTimer = function() {
	if (updateProgressTimer!= null) {
		clearTimeout(updateProgressTimer);
	}
	percentValue = 10;
	updateProgressTimer = setInterval(updateProgressBar, 1000);
};

var clearProgressTimer = function() {
	clearTimeout(updateProgressTimer);
	showProgressBarValue(0);
};

var updateProgressBar = function() {
	
	if (percentValue < 50) {
		percentValue += 10;
	} else {
		percentValue += ((100-percentValue) / 3);
		if (percentValue>100) percentValue = 100;
	}
	
	showProgressBarValue(percentValue);
};

var showProgressBarValue = function(percent) {
	var value = parseInt(percent, 10);
	$('.progress-bar').html(value + "%");
	$('.progress-bar').css('width', value+'%').attr('aria-valuenow', value);
};

var reloadConfigurationMenu = function() {
	if ($("#configmenu").length > 0) {
		var menu = $("#configmenu").attr("value");
		loadConfigurationMenu(menu);
	}
}

$(document).ready(function() {
	//Load system menu
	try {
		resetSessionTimeout();
		if ($("#configmenu").length > 0) {
			var menu = $("#configmenu").val();
			loadConfigurationMenu(menu);
		}
		//Find the tab that clicked
	} catch(err) {
		console.error("servceconfig.ready: " + err);
	}
});
