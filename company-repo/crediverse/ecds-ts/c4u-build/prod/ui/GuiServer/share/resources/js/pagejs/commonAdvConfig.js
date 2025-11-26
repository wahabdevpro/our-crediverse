
// defined on config page: currencyCode, curDigits
var currentKey = null;

var dialogError = {
	none: 0,
	firstTab: 1,
	secondTab: 2,
	bothTabs: 3
};

var componentInfo = {
		system: '',
		compId: '',
		version: 0
};

var pageurl = null;

//------------ Common Dialog Scripts -----------------------------

var dialogMode = null;
var modalInfo = {
	modalId : '',
	index : 0,
	component : '',
	divIdToUpdate : '',
	afterUpdateCallback : null
};

initializeAdvParameters = function(pgeUrl) {
	pageurl = pgeUrl;
};

resetModal = function(modalId, title, mode, buttonText, component, divIdToUpdate, index, afterUpdateCallback) {
	try {
		try {
			currentKey = null;
		} catch(err) {}
		
		$("#" + modalInfo.modalId + "Waiting").addClass("hide");
		
		modalInfo.modalId = modalId;
		modalInfo.component = component;
		modalInfo.divIdToUpdate = divIdToUpdate;
		modalInfo.index = index;
		modalInfo.afterUpdateCallback = null;
		
		//Clear all errors
		try {
			$("#" + modalInfo.modalId + " span[id$='_error']").parent("div").removeClass("has-error");
			$("#" + modalInfo.modalId + " span[id$='_error']").addClass("hide");
		} catch(err) {
			console.error("Error in there " + err);
		}
		
		var fullTitle = (mode == "add")? "Add " : (mode == "edit")? "Edit " : "View ";
		fullTitle += title;
		dialogMode = mode;
		//Hide Error messages?
		$("#" + modalInfo.modalId + "Form").trigger("reset"); //Form
		$("#" + modalInfo.modalId + "Title").html(fullTitle);
		$("#" + modalInfo.modalId + "Error").addClass("hide");
		if (mode == "view") {
			$("#" + modalInfo.modalId).find(".savebtn").hide();
		} else {
			$("#" + modalInfo.modalId).find(".savebtn").html(buttonText);
			$("#" + modalInfo.modalId).find(".savebtn").show();
			if (mode == "add") {
				$("select[name=fileType]").val(1);
				$(".selectpicker").selectpicker("refresh");
			}
		}
		$("#" + modalInfo.modalId + "Form :input").prop("disabled", (mode == "view"));
		$("#" + modalInfo.modalId).modal("show");
		$("#" + modalInfo.modalId + "Error").addClass("hide");
		
		if (typeof afterUpdateCallback !== "undefined" && afterUpdateCallback != null) {
			modalInfo.afterUpdateCallback = afterUpdateCallback;
		}
	} catch(err) {
		console.error("resetModal Error: " + err);
	}
};

/** Load language data **/
loadLanguages = function() {
	var data2Send = "act=lang";
	generalJsonCall(data2Send, storeLanguageDetails);
};

storeLanguageDetails = function(data) {
	if (typeof processModel !== 'undefined' && processModel != null) {
		processModel.languageData = data;		
	}
};

generalJsonCall = function(data2Send, callback) {
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		if (callback!=null) {
			callback(data);
		}
	});
};

renderhintsRequest = function(component, callback) {
	var data2Send = "act=render&comp=" + component;
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		callback(component, data);
	});
};

componentInfoRequest = function(component, index, modalId, callback, primaryKey, postUpdateCallBack) {
	var data2Send = "act=data&index=" + index + "&comp=" + component;
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		callback(modalId, data, ((typeof primaryKey !== 'undefined')? primaryKey : null));
		if (typeof postUpdateCallBack !== "undefined")
			postUpdateCallBack();
	});
};

optionsInfoRequest = function(component, field, callback) {
	var data2Send = "act=options&comp=" + component + "&field=" + field;
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		callback(data);
	});
};

findElement = function(modalId, elementKey) {
	var lowerkey = elementKey.toLowerCase();
	var $element = $("#" + modalId + " *").filter(function() {
		if (typeof $(this).attr('id') === 'undefined')
			return false;
		else
		{
			var id = $(this).attr('id').toLowerCase();
			return (lowerkey == id);
		}
	});
	if ($element != null && $element.length > 0)
		return $element;
	else
		return null;
};

updateConfigInfoDialog = function(modalId, data, primaryKey) {
	try {
		currentKey = null;
		//Extract values
		for (var key in data) {
			if (key != "render") {
				if (data.hasOwnProperty(key)) {
					try {
						if (key == primaryKey) {
							currentKey = data[key];
						}
						
						if (typeof data[key] === 'object') {
							//Texts
							if (typeof data[key].texts !== "undefined") {
								for(var i=0;  i<data[key].texts.length; i++) {
									if (data[key].texts.length > 0) {
										var $element = findElement(modalId, key + "_" + i);
										if ($element != null) {
											$element.val(data[key].texts[i]);
										}
									}
								}
							} else {
								for(var i=0;  i<data[key].length; i++) {
									if (data[key].length > 0) {
										var $element = findElement(modalId, key + "_" + i);
										if ($element != null) {
											$element.val(data[key][i]);
										}
									}
								}
							}
							

						} else if (data[key] == "true") {
							var $element = findElement(modalId, key);
							if ($element != null) {
								if ($element.is(':checkbox')) {
									$element.prop("checked", data[key]);
								} else {
									$element.val(data[key]);
								}
							}
						} else {
							var value = data[key];
							var field = key; 
							
							var decimalPlaces = 0;
							var scaling = 0;
							var renderAs = null;
							
							if (typeof data.render[key] !== "undefined" && data.render[key] != null) {
								if (typeof data.render[key].ra !== "undefined" && data.render[key].ra != null) {
									//Render As
									if (data.render[key].ra == "CURRENCY") {
										var iValue = parseInt(value);
										var iCurDigits = parseInt(curDigits);
										var val = (iValue / (Math.pow(10, iCurDigits))).toFixed(iCurDigits);
										value = val;
									}
								}
								if (typeof data.render[key].sf !== "undefined" && data.render[key].sf != null) {
									var val = parseInt(value) / (parseInt(data.render[key].sf));
									if (typeof data.render[key].dd !== "undefined" && data.render[key].dd != null) {
										val = val.toFixed(parseInt(data.render[key].dd));
									}
									value = val;
								}
							}
							var $element = findElement(modalId, key);
							
							if ($element != null) {
								var inputType = $element.prop('tagName');
//								console.log(key + " := " + inputType);
								if (inputType == 'SELECT') {
									if (value.indexOf(",") > 0) {
										var options = value.split(',');
										$element.selectpicker('val', options);
									} else {
										$element.selectpicker('val', value);
									}
								} else {
									$element.val(value);		
								}
							}
						}
				  	} catch(err){
				  		try {
				  			console.error("Cannot insert: " + key + ":=" + data[key] + " Error: " + err)			  			
				  		} catch(exerr){}
				  	}
				}
			}
		}
		$('.selectpicker').selectpicker('refresh');
	} catch(err) {
		console.error("updateConfigInfoDialog: " + err);
	}
};

//Request Delete feedback
showDelete = function(component, hint, index, divIdToUpdate) {
	$("#toRemoveMessage").html("Please confirm removal of <b>" + hint + "</b>");
	modalInfo.index = index;
	modalInfo.component = component;
	modalInfo.divIdToUpdate = divIdToUpdate;
	$("#delModal").modal("show");
}

//Server call to delete
validateAndDelete = function() {
	$("#delwaiting").removeClass("hide");
	var data2Send = "act=del&index="+modalInfo.index+"&comp="+modalInfo.component;
	
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		try {
			resetSessionTimeout();
			$("#delwaiting").addClass("hide");
			if (data.status=="fail")
			{
				$("#delErrorMessage").html(data.message);
				$("#delErrorMessage").removeClass("hide");
			}
			else
			{
				dataUpdated();
				refreshConfigContent(modalInfo.component, modalInfo.divIdToUpdate);
				$("#delModal").modal("hide");
				
				if (modalInfo.afterUpdateCallback != null) {
					modalInfo.afterUpdateCallback();
				}
			}	
		} catch(err) {
			console.error("validateAndDelete: " + err);
		}

	});
};

refreshConfigContent = function(component, divIdToUpdate) {
	try {
		$("#" + divIdToUpdate).html("<center><img src='/img/bigwait.gif' /></center>");
		$("#" + divIdToUpdate).load(pageurl + "?act=refresh&comp=" + component, function() {
			updateDataTable(component + "_table");
		});	
	} catch(err) {  
		console.error("refreshConfigContent: " + err);
	}
};

persistModalData = function() {
	try {
		$("#" + modalInfo.modalId + "Waiting").removeClass("hide");
		var requestData = $('#' + modalInfo.modalId + "Form").serialize({ checkboxesAsBools: true });
		var data2Send = "act=upd&comp=" + modalInfo.component;
		if ((typeof modalInfo.index !== 'undefined') && (modalInfo.index != null)) {
			data2Send += "&index=" + modalInfo.index;
		}
		data2Send += "&" + requestData;
		
		var status = $.ajax({
			type: "POST", 
			url: pageurl, 
			async: true,
			dataType: "json",
			data: data2Send
		}).done(function(data) {
			if (data.status == "fail")
			{
				$("#" + modalInfo.modalId + "Error").removeClass("hide");
				$("#" + modalInfo.modalId + "Error").html(data.message);
			}
			else
			{
				dataUpdated();
				$("#" + modalInfo.modalId + "Error").addClass("hide");
				$('#' + modalInfo.modalId).modal('hide');
				refreshConfigContent(modalInfo.component, modalInfo.divIdToUpdate);
				if (modalInfo.afterUpdateCallback != null) {
					modalInfo.afterUpdateCallback();
				}
			}
		});
	} catch(err) {
		console.error("persistModalData: " + err);
	}
};

saveModalCall = function() {
	try {
		var errorStatus = window[modalInfo.modalId + "Validate"]();	//NOTE: ModalName + "Validate" used to call validate method
		if (errorStatus == dialogError.none) {
			persistModalData();
		} else {
			var tab = null;
			if (modalInfo.modalId == 'serviceClassModal') {
				if (errorStatus == dialogError.firstTab) {
					tab = "#scconfig";
				} else if (errorStatus == dialogError.secondTab) {
					tab = "#sctexts";
				}
			} else if (modalInfo.modalId == 'transferModal') {
				if (errorStatus == dialogError.firstTab) {
					tab = "#transferModalConfig";
				} else if (errorStatus == dialogError.secondTab) {
					tab = "#transferModalTexts";
				}
			} else if (modalInfo.modalId == 'variantsModal') {
				if (errorStatus == dialogError.firstTab) {
					tab = "#variantsModalConfig";
				} else if (errorStatus == dialogError.secondTab) {
					tab = "#variantsModalTexts";
				}
			}
			if (tab != null) {
				$('a[href="'+tab+'"]').tab('show');
			}
			
			var $element = $("#" + modalInfo.modalId + "Error");
			var errorMessage = "Check " + ((errorStatus == dialogError.firstTab)? "Configuration tab" : ((errorStatus == dialogError.secondTab)? "Multilingual Text tab" : "Both tabs")) + " for errors";
			$("#" + modalInfo.modalId + "Error").html(errorMessage);
			$("#" + modalInfo.modalId + "Error").removeClass("hide");
		}
	} catch(err) {
		console.error("saveModalCall: " + err);
	}
};

//---- Return Codes
loadReturnCodes = function() {
	try {
		var data2Send = "act=rcdata";
		generalJsonCall(data2Send, updateReturnCodes);
	} catch(err) {
		console.error("loadReturnCodes: " + err);
	}
};

updateReturnCodes = function(data) {
	try {
		
		if (typeof data !== 'undefined' && data != null) {
			if (typeof data.status !== 'undefined') {
				if (data.status == 'fail' && data.message == 'USER_INVALID') {
					resetAndLogout();
					return;
				}
			}
			if (data.returncodes != null) {
				resetSessionTimeout();
				data.returncodes.sort();
				if (typeof processModel !== 'undefined' && processModel != null) {
					processModel.returnCodes = data.returncodes;
				}
			}
		} else {
			console.error("updateReturnCodes data null");
		}
	} catch(err) {
		console.error("updateReturnCodes: " + err);
	}
};

// ----------------------------------------------------------------------------------------------------------
buildTabContents = function() {
	try {
		var data2Send = "act=tabsinfo";
		var status = $.ajax({
			type: "POST", 
			url: pageurl, 
			async: true,
			dataType: "json",
			data: data2Send
		}).done(function(data) {
			if (data != null) {
				for(var field in data) {
					if (data[field] == "VasCommand") {
						$("#" + field).vasCommandController({
							servlet : "/vct",
							field : field,
							dataservlet : pageurl,
							useFilteredTable : true
						});
					}
				}
			}
		}).fail(function() {
//		    alert( "error" );
		});
	} catch(err) {
		console.error("buildTabContents: " + err);
	}
};

// -------------------------------------------

var ErrorHighlighter = {
	fieldMap : {},
	
	register : function(fieldsMap) {
		var self = this;
		if (typeof fieldsMap !== "undefined" && fieldsMap != null) {
			fieldMap = fieldsMap;
		}
		fieldErrorHelper.clearErrors();
		fieldErrorHelper.registerListener(function(fieldComponent, fieldIndex) {
			try {
				//Clear previous Errors
				if (fieldErrorHelper.previousErrorField != null) {
					self.highlightTableRow(fieldErrorHelper.previousErrorField, false);
				}
				
				//Update new Errors
				if (typeof fieldComponent !== "undefined" && fieldComponent != null) {
					self.highlightTableRow(fieldErrorHelper.currentErrorField, true);
				}
			} catch(err) {
				console.error("registerToHighlightErrors.registerListener: " + err);
			}
		});
	},
	
	highlightTableRow : function(fullFieldName, highlight) {
		try {
			var field = fieldErrorHelper.extractErrorField(fullFieldName, 0, false);
			if (field != null) {
				var index = fieldErrorHelper.extractErrorFieldIndex(fullFieldName, 0);
				var elementID = field + "edit_" + index;
				var row = $("#" + elementID).parent("td").parent();
				if (highlight)
					row.children('td').addClass("hightlightTableError");
				else {
					row.children('td').removeClass("hightlightTableError");
					row.children('td').attr("style", "");
				}
					
			}
		} catch(err) {
			console.error("highlightTableRow: " + err);
		}
	},
	
	highlightDialogFieldError : function(componentName, index) {
		try {
			if (fieldErrorHelper.currentErrorField != null) {
				var errorComponentName = fieldErrorHelper.extractErrorField(fieldErrorHelper.currentErrorField, 0, false);
				var errorIndex = fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.currentErrorField, 0);
				if (errorComponentName == componentName && errorIndex == index) {
					var field = fieldErrorHelper.currentErrorField.substring(fieldErrorHelper.currentErrorField.indexOf(".") +1);
					if ($("#" + field).length > 0) {
						$("#" + field).parent().closest('div').addClass("has-error");
					} else {
						console.log(field + " not Found");
					}
				}
			}
		} catch(err) {
			console.error("highlightDialogFieldError: "  + err);
		}
	},
	
	checkForError : function() {
		if (fieldErrorHelper.currentErrorField != null) {
			this.highlightTableRow(fieldErrorHelper.currentErrorField, true);
		}
	}
};