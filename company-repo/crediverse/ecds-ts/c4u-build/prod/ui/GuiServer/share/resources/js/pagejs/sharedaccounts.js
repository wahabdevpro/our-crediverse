var pageurl = "/genshareacc", COMP_VAR = "var", COMP_SC = "sc", COMP_QUOTA = "quota";

var dialogError = {
	none :    0,
	busTab :  1,
	techTab : 2,
	msgTab :  4
};

var lastNode = {
	id : '',
	update : false,
	type : ''
};

var componentInfo = {
	system : '',
	compId : '',
	version : 0
};

messagesUpdated = function(comp) {

	if (comp != null) {
		if (comp.id == "menu_I") {

		}
	}
	lastNode.update = true;
};

loadProcessModelTree = function(json) {
	try {
		if (json != null) {
			var job = JSON && JSON.parse(json) || $.parseJSON(json);
			// $('#processModel').jstree("destroy");
			$('#processModel').jstree({
				'core' : {
					'data' : job.data
				}
			});
		}
	} catch (err) {
		try {
			console.error("loading error :> " + err);
		} catch (ex) {
		}
	}

	$("#processModel").bind("select_node.jstree", function(e, data) {
		var ref = $('#processModel').jstree(true);
		var sel = ref.get_selected();
		loadContent(sel);
	});
}

updateProcessModel = function(jsonData) {
	$('#processModel').jstree("destroy");
	$('#processModel').jstree({
		'core' : {
			'data' : jsonData.data
		}
	});
};

updateMenuText = function(title, textArray) {
	$("#menu_config_title").html(title);
	for (var i = 0; i < textArray.length; i++) {
		if (i == 0)
			$("#menu_I").html(textArray[i]);
		else {
			$("#menu_" + (i - 1)).html(textArray[i]);
		}
	}
	$("#menu_config").removeClass("hide");
};

updateEmptyText = function(textArray) {
	for (var i = 0; i < textArray.length; i++) {
		if (i == 0)
			$("#menuempty_I").html(textArray[i]);
		else {
			$("#menuempty_" + (i - 1)).html(textArray[i]);
		}
	}
	$("#menuitems_empty").removeClass("hide");
};

getNodeText = function(id) {
	var ref = $('#processModel').jstree(true);
	var sel = ref.get_node(id);
	return ref.get_text(sel);
};

updateNodeText = function(id, text) {
	var ref = $('#processModel').jstree(true);
	var sel = ref.get_node(id);
	ref.set_text(sel, text);

	// var currentSelectedNode = ref.get_selected();
	// ref.set_text(sel, text);
	// var text = $("#menu_I").html();
	// ref.set_text(sel, text);
};

checkAndSaveMessageData = function(callback) {
	if (lastNode.update && (lastNode.id != '')) {
		saveLastNodeUpdates(callback);
		var oldText = getNodeText(lastNode.id);
		var updatedText = oldText.substring(0, oldText.indexOf('[') + 1)
				+ $("#menu_I").text() + "]";
		updateNodeText(lastNode.id, updatedText);
	} else if (callback != null) {
		callback();
	}
};

sendNodeRequest = function(data2Send, callback) {
	var status = $.ajax({
		type : "POST",
		url : pageurl,
		async : true,
		dataType : "json",
		data : data2Send
	}).done(function(data) {
		if (callback != null) {
			callback(data);
		}

	});
};

saveLastNodeUpdates = function(callback) {
	var data2Send = "node=" + lastNode.id + "&act=upd";

	var msgData = extractNotifications();
	data2Send += msgData;
	sendNodeRequest(data2Send, callback);
};

updateValidateAndSave = function(system, compId, version) {
	resetSessionTimeout();
	componentInfo.system = system;
	componentInfo.compId = compId;
	componentInfo.version = version;

	// Save current State
	checkAndSaveMessageData(updateValidateAndSaveCallback);
};

// Save page to server
updateValidateAndSaveCallback = function() {
	validateAndSave(componentInfo.system, componentInfo.compId,
			componentInfo.version);
};

// / For Process Grid
var processModel = null;

retrievePropertyData = function(nid, aid) {
	var data2Send = "aid=" + aid + "&act=propdata&nid=" + nid;
	if (processModel != null) {
		sendNodeRequest(data2Send, processModel.updateDialog);
	}

};

/** Load language data * */
storeLanguageDetails = function(data) {
	if (processModel != null) {
		processModel.languageData = data;
	}
};

loadLanguages = function() {
	var data2Send = "act=lang";
	sendNodeRequest(data2Send, storeLanguageDetails);
};

loadNodeIdContent = function(nodeId) {
	try {
		if (nodeId.indexOf("act") >= 0) {
			$("#processOptions").html(createLoadingIconHtml());
			var data2Send = "node=" + nodeId + "&act=data";
			sendNodeRequest(data2Send, updateRightContent);
		} else {
			updateRightContent(null);
		}
	} catch (err) {
		alert(err);
	}
};

loadContent = function(node) {
	try {
		var nodeId = node[0];
		loadNodeIdContent(nodeId);
	} catch (err) {
		alert(err);
	}
};

updateRightContent = function(data) {
	if (processModel != null) {
		if (data != null)
			processModel.update(data);
		else
			processModel.update(data, true);
	}
};

loadReturnCodes = function() {
	var data2Send = "act=rcdata";
	sendNodeRequest(data2Send, updateReturnCodes);
};

updateReturnCodes = function(data) {
	try {
		if (typeof data.status !== 'undefined') {
			if (data.status == 'fail' && data.message == 'USER_INVALID') {
				resetAndLogout();
				return;
			}
		}
		if (data.returncodes != null) {
			resetSessionTimeout();
			data.returncodes.sort();
			if (processModel != null) {
				processModel.returnCodes = data.returncodes;
			}
		}
	} catch (err) {
		console.error(err);
	}
};

/**
 * General Methods for tables
 */
updatePropertyCallback = function() {
};

// ---------------------------------- COMMON DIALOG UPDATES
// ----------------------------------
var dialogMode = null;
var modalInfo = {
	modalId : '',
	index : 0,
	component : '',
	divIdToUpdate : ''
};

resetModal = function(modalId, title, mode, buttonText, component,
		divIdToUpdate, index) {
	try {
		$("#" + modalInfo.modalId + "Waiting").addClass("hide");

		modalInfo.modalId = modalId;
		modalInfo.component = component;
		modalInfo.divIdToUpdate = divIdToUpdate;
		modalInfo.index = index;

		// Clear all errors
		try {
			$("#" + modalInfo.modalId + " span[id$='_error']").parent("div")
					.removeClass("has-error");
			$("#" + modalInfo.modalId + " span[id$='_error']").addClass("hide");
		} catch (err) {
			console.error("Error in there " + err);
		}

		var fullTitle = (mode == "add") ? "Add " : (mode == "edit") ? "Edit "
				: "View ";
		fullTitle += title;
		dialogMode = mode;
		// Hide Error messages?
		$("#" + modalInfo.modalId + "Form").trigger("reset"); // Form
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
		$("#" + modalInfo.modalId + "Form :input").prop("disabled",
				(mode == "view"));
		$("#" + modalInfo.modalId).modal("show");
		$("#" + modalInfo.modalId + "Error").addClass("hide");
	} catch (err) {
		console.error("resetModal Error: " + err);
	}
};

generalJsonCall = function(data2Send, callback) {
	var status = $.ajax({
		type : "POST",
		url : pageurl,
		async : true,
		dataType : "json",
		data : data2Send
	}).done(function(data) {
		callback(data);
	});
};

componentInfoRequest = function(component, index, modalId, callback, postUpdateCallBack) {
	var data2Send = "act=data&index=" + index + "&comp=" + component;
	var status = $.ajax({
		type : "POST",
		url : pageurl,
		async : true,
		dataType : "json",
		data : data2Send
	}).done(function(data) {
		callback(modalId, data);
		if (typeof postUpdateCallBack !== "undefined")
			postUpdateCallBack();
	});
};

updateConfigInfoDialog = function(modalId, data) {

	try {
		for ( var key in data) {
			if (data.hasOwnProperty(key)) {
				try {
					if (typeof data[key] === 'object') {
						// Texts
						for (var i = 0; i < data[key].texts.length; i++) {
							if (data[key].texts.length > 0) {
								var $element = $("#" + modalId).find(
										"#" + key + "_" + i);
								if ($element.length > 0) {
									$element.val(data[key].texts[i]);
								}
							}
						}
					} else if (data[key] == "true") {
						var $element = $("#" + modalId).find("#" + key);
						if ($element.is(':checkbox')) {
							$element.prop("checked", data[key]);
						} else {
							$element.val(data[key]);
						}
					} else {
						// TODO: This needs to be made generic for currency
						// conversions
						var value = data[key];
						var field = key;

						if ($("#" + modalId).find("#" + field).length) {
							$("#" + modalId).find("#" + field).val(value);
						} else {
							try {
								// console.log("field not found: " + field + "["
								// + modalId + "]");
							} catch (err) {
							}
						}
					}
				} catch (err) {
					try {
						console.error("3RroR Cannot insert: " + key + ":="
								+ data[key])
					} catch (exerr) {
					}
				}
			}
		}
		$('.selectpicker').selectpicker('refresh');
	} catch (err) {
		console.error(err);
	}
	if (modalId == "serviceClassModal") {
		scEligibilityRule();
	}
};

// Request Delete feedback
showDelete = function(component, hint, index, divIdToUpdate) {
	$("#toRemoveMessage").html("Please confirm removal of <b>" + hint + "</b>");
	modalInfo.index = index;
	modalInfo.component = component;
	modalInfo.divIdToUpdate = divIdToUpdate;
	$("#delModal").modal("show");
}

// Server call to delete
validateAndDelete = function() {
	$("#delwaiting").removeClass("hide");
	var data2Send = "act=del&index=" + modalInfo.index + "&comp="
			+ modalInfo.component;

	var status = $.ajax({
		type : "POST",
		url : pageurl,
		async : true,
		dataType : "json",
		data : data2Send
	}).done(function(data) {
		resetSessionTimeout();
		$("#delwaiting").addClass("hide");
		if (data.status == "fail") {
			$("#delErrorMessage").html(data.message);
			$("#delErrorMessage").removeClass("hide");
		} else {
			dataUpdated();
			refreshConfigContent(modalInfo.component, modalInfo.divIdToUpdate);
			$("#delModal").modal("hide");
		}
	});
};

refreshConfigContent = function(component, divIdToUpdate) {
	try {
		$("#" + divIdToUpdate).html(
				"<center><img src='/img/bigwait.gif' /></center>");
		$("#" + divIdToUpdate).load(
				pageurl + "?act=refresh&comp=" + component,
				function() {
					var tableName = "quotasTable";
					if (component == "var")
						tableName = "variantsTable";
					else if (component == "sc") 
						tableName = "servicesTable";
					
					updateDataTable(tableName);
				});
	} catch (err) {
		console.error(err);
	}
};

persistModalData = function() {
	try {

		$("#" + modalInfo.modalId + "Waiting").removeClass("hide");

		var requestData = $('#' + modalInfo.modalId + "Form").serialize();
		var data2Send = "act=upd&comp=" + modalInfo.component;
		if ((typeof modalInfo.index !== 'undefined')
				&& (modalInfo.index != null)) {
			data2Send += "&index=" + modalInfo.index;
		}
		data2Send += "&" + requestData;

		var status = $.ajax({
			type : "POST",
			url : pageurl,
			async : true,
			dataType : "json",
			data : data2Send
		})
				.done(
						function(data) {
							if (data.status == "fail") {
								$("#" + modalInfo.modalId + "Error")
										.removeClass("hide");
								$("#" + modalInfo.modalId + "Error").html(
										data.message);
							} else {
								dataUpdated();
								$("#" + modalInfo.modalId + "Error").addClass(
										"hide");
								$('#' + modalInfo.modalId).modal('hide');
								refreshConfigContent(modalInfo.component,
										modalInfo.divIdToUpdate);
							}
						});
	} catch (err) {
		console.error(err);
	}
}

checkForErrors = function() {
	var errors = false;
	if (modalInfo.component = COMP_SC) {
	} else if (modalInfo.component = COMP_QUOTA) {
	} else if (modalInfo.component = COMP_VAR) {
	}
	return errors;
}

saveModalCall = function() {
	try {
		var errorStatus = window[modalInfo.modalId + "Validate"]();
		if (errorStatus == dialogError.none) {
			persistModalData();
		} else {
			var tab = null;
			if (modalInfo.modalId == 'serviceClassModal') {
				if (errorStatus == dialogError.busTab) {
					tab = "#scbusconfig";
				} else if (errorStatus == dialogError.techTab) {
					tab = "#sctecconfig";
				} else if (errorStatus == dialogError.msgTab) {
					tab = "#sctexts";
				}
			} else if (modalInfo.modalId == 'quotasModal') {
				if (errorStatus == dialogError.busTab) {
					tab = "#quotasModalBusConfig";
				} else if (errorStatus == dialogError.techTab) {
					tab = "#quotasModalConfig";
				} else if (errorStatus == dialogError.msgTab) {
					tab = "#quotasModalTexts";
				}
			} else if (modalInfo.modalId == 'variantsModal') {
				if (errorStatus == dialogError.busTab) {
					tab = "#variantsModalBusConfig";
				} else if (errorStatus == dialogError.techTab) {
					tab = "#variantsModalConfig";
				} else if (errorStatus == dialogError.msgTab) {
					tab = "#variantsModalTexts";
				}
			}
			if (tab != null) {
				$('a[href="' + tab + '"]').tab('show');
			}

			var $element = $("#" + modalInfo.modalId + "Error");
			var errorMessage = "Check ";
			if (errorStatus == dialogError.busTab) {
				errorMessage += "Business Configuration tab";
			} else if (errorStatus == dialogError.techTab) {
				errorMessage += "Technical Configuration tab";
			} else if (errorStatus == dialogError.msgTab) {
				errorMessage += "Multiligual text tab";
			} else {
				errorMessage += "All tabs";
			}
			errorMessage += " for errors";
			
			$("#" + modalInfo.modalId + "Error").html(errorMessage);
			$("#" + modalInfo.modalId + "Error").removeClass("hide");
		}
	} catch (err) {
		console.error(err);
	}
}
// ---------------------------------- SERVICE CLASS
// ------------------------------------------
var insertServiceClassDefault = function() {
	if ($("#addConsumerCharge").val().length == 0) $("#addConsumerCharge").val("0");
	if ($("#removeConsumerCharge").val().length == 0) $("#removeConsumerCharge").val("0");
	if ($("#removeQuotaCharge").val().length == 0) $("#removeQuotaCharge").val("0");
	if ($("#providerBalanceEnquiryCharge").val().length == 0) $("#providerBalanceEnquiryCharge").val("0");
	if ($("#unsubscribeCharge").val().length == 0) $("#unsubscribeCharge").val("0");
	if ($("#maxConsumers").val().length == 0) $("#maxConsumers").val("0");
};

/** Add * */
showAddServiceClass = function() {
	resetModal("serviceClassModal", "Service Class", "add", "Save", COMP_SC,
			"scinfo");
	showTab("#sctexts");
	insertServiceClassDefault();
	scEligibilityRule();
};

/** View / Edit * */
showServiceclass = function(mode, index) {
	resetModal("serviceClassModal", "Service Class", mode, "Update & Close", COMP_SC, "scinfo", index);
	componentInfoRequest(COMP_SC, index, "serviceClassModal", updateConfigInfoDialog);
	modalInfo.index = index;
	modalInfo.component = COMP_SC;
	modalInfo.divIdToUpdate = "scinfo";
	showTab("#sctexts");
};

var scEligibilityRule = function() {
	try
	{
		var isProsumerEligible = $("#eligibleForProsumer").is(':checked');		
		var isProviderEligible = $("#eligibleForProvider").is(':checked');
		var isConsumerEligible = $("#eligibleForConsumer").is(':checked');

		$("#addConsumerCharge").prop("disabled", !(isProviderEligible || isProsumerEligible));
		$("#removeQuotaCharge").prop("disabled", !(isProviderEligible || isProsumerEligible));
		$("#unsubscribeCharge").prop("disabled", !(isProviderEligible || isProsumerEligible));
		$("#providerBalanceEnquiryCharge").prop("disabled", !(isProviderEligible || isProsumerEligible));
		$("#maxConsumers").prop("disabled", !(isProviderEligible || isProsumerEligible));
		$("#removeConsumerCharge").prop("disabled", !(isProviderEligible || isProsumerEligible));
		
		if	(!(isProviderEligible || isConsumerEligible || isProsumerEligible)) {
			$("#eligibleWarning").removeClass("hide");
		} else {
			$("#eligibleWarning").addClass("hide");
		}
	} catch(err) 
	{
		if (console) console.error("scEligibilityRule: " + err);
	}
};

var scEligibilityEvents = function() {
	$("#eligibleForProvider").on("click", scEligibilityRule );
	$("#eligibleForConsumer").on("click", scEligibilityRule );
	$("#eligibleForProsumer").on("click", scEligibilityRule );
};

/** Delete * */
deleteServiceclass = function(index, hint) {
	showDelete(COMP_SC, "Service class " + hint, index, "scinfo");
};

serviceClassModalValidate = function() {
	var errorStatus = dialogError.none;
	
	//Business configuration
	var error = validateLengthMinMax("addConsumerCharge", 1, 20,
			"Field Required (max 20 digits)", "serviceClassModal");
	error = validateCurrency("addConsumerCharge", "Currency value required",
			"serviceClassModal")
			|| error;
	
	error = validateLengthMinMax("removeConsumerCharge", 1, 20,
			"Field Required (max 20 digits)", "serviceClassModal")
			|| error;
	error = validateCurrency("removeConsumerCharge", "Currency value required",
			"serviceClassModal")
			|| error;
	error = validateLengthMinMax("removeQuotaCharge", 1, 20,
			"Field Required (max 20 digits)", "serviceClassModal")
			|| error;
	error = validateCurrency("removeQuotaCharge", "Currency value required",
			"serviceClassModal")
			|| error;
	error = validateLengthMinMax("providerBalanceEnquiryCharge", 1, 20,
			"Field Required (max 20 digits)", "serviceClassModal")
			|| error;
	error = validateCurrency("providerBalanceEnquiryCharge",
			"Currency value required", "serviceClassModal")
			|| error;
	error = validateLengthMinMax("unsubscribeCharge", 1, 20,
			"Field Required (max 20 digits)", "serviceClassModal")
			|| error;
	error = validateCurrency("unsubscribeCharge", "Currency value required",
			"serviceClassModal")
			|| error;
	error = validateLengthMinMax("maxConsumers", 1, 20,
			"Field Required (max 20 digits)")
			|| error;
	error = validateNumber("maxConsumers", "Numeric value required") || error;
	
	if (error) {
		errorStatus += dialogError.busTab;
	}

	//Technical configuration
	error = validateLengthMinMax("serviceClassID", 1, 10, "Field Required (max 10 digits)");
	error = validateNumber("serviceClassID", "Numeric value required") || error;
	
	if (error) {
		errorStatus += dialogError.techTab;
	}
	
	//Texts configuration
	error = validateIPhrase("names", 1, 255,
			"Default Required (max 255 characters)", "qt_name1",
			"serviceClassModal");
	
	if (error) {
		errorStatus += dialogError.msgTab;
	}
	
	return errorStatus;
};

// ---------------------------------- QUOTA
// --------------------------------------------------
/** Add * */
showAddQuotas = function() {
	resetModal("quotasModal", "Quotas", "add", "Save", COMP_QUOTA, "qtinfo");
	showTab("quotasModalBusConfig");
	$("#quotaIDdiv").show();
};

/** View / Edit * */
showQuotas = function(mode, index) {
	resetModal("quotasModal", "Quotas", mode, "Update & Close", COMP_QUOTA, "qtinfo",
			index);
	componentInfoRequest(COMP_QUOTA, index, "quotasModal",
			updateConfigInfoDialog);
	showTab("quotasModalBusConfig");
	//$("#quotaIDdiv").hide();
};

/** Delete * */
deleteQuotas = function(index, hint) {
	showDelete(COMP_QUOTA, hint, index, "qtinfo");
};

quotasModalValidate = function() {
	var errorStatus = dialogError.none;

	
	// Business configuration
	var error = validateLengthMinMax("minUnits", 1, 10,
				"Field Required (max 10 digits)");
	error = validateNumber("minUnits", "Numeric value required") || error;
	error = validateLengthMinMax("maxUnits", 1, 10,
			"Field Required (max 10 digits)")
		|| error;
	error = validateNumber("maxUnits", "Numeric value required") || error;
	error = validateMinMax("minUnits", "maxUnits", "Min larger than Max Units") || error;
	error = validateLengthMinMax("priceCents", 1, 20,"Field Required (max 20 digits)")|| error;
	error = validateCurrency("priceCents", "Currency value required") || error;
	error = validateLengthMinMax("warningMargin", 1, 10, "Field Required (max 10 digits)") || error;
	error = validateNumber("warningMargin", "Numeric value required") || error;

	if (error) {
		errorStatus += dialogError.busTab;
	}
	
	// Technical configuration
	error = validateLengthMinMax("sponsorOfferID", 1, 10, 	"Field Required (max 10 digits)");
	error = validateNumber("sponsorOfferID", "Numeric value required") || error;
	
	if (dialogMode == "add") { 
		error = validateLengthMinMax("quotaID", 1, 40,
			"Field Required (max 40 characters)") || error;
	}
	
	error = validateLengthMinMax("sponsorUsageCounterID", 1, 20,
		"Field Required (max 20 digits)")
		|| error;
	error = validateNumber("sponsorUsageCounterID", "Numeric value required")
		|| error;
	error = validateLengthMinMax("beneficiaryOfferID", 1, 20,
		"Field Required (max 20 digits)")
		|| error;
	error = validateNumber("beneficiaryOfferID", "Numeric value required")
		|| error;
	error = validateLengthMinMax("beneficiaryUsageCounterID", 1, 20,
		"Field Required (max 20 digits)")
		|| error;
	error = validateNumber("beneficiaryUsageCounterID",
		"Numeric value required")
		|| error;
	error = validateLengthMinMax("beneficiaryWarningUsageThresholdID", 1, 20,
		"Field Required (max 20 digits)")
		|| error;
	error = validateNumber("beneficiaryWarningUsageThresholdID",
		"Numeric value required")
		|| error;
	error = validateLengthMinMax("beneficiaryTotalThresholdID", 1, 20,
		"Field Required (max 20 digits)")
		|| error;
	error = validateNumber("beneficiaryTotalThresholdID",
		"Numeric value required")
		|| error;
	
	error = validateLengthMinMax("unitConversionFactor", 1, 10, "Field Required (max 10 digits)") || error;
	error = validateNumber("unitConversionFactor", "Numeric value required") || error;
	
	try {
	var conversionFactor = parseInt($("#unitConversionFactor").val());
	var minUnits = parseInt($("#minUnits").val());
	var maxUnits = parseInt($("#maxUnits").val());
	var value = (maxUnits * conversionFactor)
			- (minUnits * conversionFactor);
	error = validateValueLowerEqualThan("warningMargin", value, "Warning Level invalid") || error;
	} catch (err) {
	// alert(err);
	}
	
	if (error) {
		errorStatus += dialogError.techTab;
	}

	// Texts configuration
	error = validateIPhrase("name", 1, 255,
			"Default Required (max 255 characters)", "qt_quotasname",
			"quotasModal");
	error = validateIPhrase("service", 1, 255,
			"Default Required (max 255 characters)", "qt_service",
			"quotasModal")
			|| error;
	error = validateIPhrase("destination", 1, 255,
			"Default Required (max 255 characters)", "qt_destination",
			"quotasModal")
			|| error;
	error = validateIPhrase("daysOfWeek", 1, 255,
			"Default Required (max 255 characters)", "qt_daysOfWeek",
			"quotasModal")
			|| error;
	error = validateIPhrase("timeOfDay", 1, 255,
			"Default Required (max 255 characters)", "qt_timeOfDay",
			"quotasModal")
			|| error;
	error = validateIPhrase("unitName", 1, 255,
			"Default Required (max 255 characters)", "qt_unitName",
			"quotasModal")
			|| error;

	if (error) {
		errorStatus += dialogError.msgTab;
	}

	return errorStatus;
};
// ---------------------------------- VARIANTS
// -----------------------------------------------
/** Add **/
showAddVariants = function() {
	resetModal("variantsModal", "Variant", "add", "Save", COMP_VAR, "varinfo");
	showTab("variantsModalBusConfig");
	$("#variantIDdiv").show();
//	dialogMode = "add";
};

/** View / Edit **/
showVariants = function(mode, index) {
	resetModal("variantsModal", "Variant", mode, "Update & Close", COMP_VAR, "varinfo", index);
	componentInfoRequest(COMP_VAR, index, "variantsModal", updateConfigInfoDialog, function() {
		hideShowValidityPeriodGroup();
	});
	showTab("variantsModalBusConfig");
	$("#variantIDdiv").hide();
};

/** Delete * */
deleteVariants = function(index, hint) {
	showDelete(COMP_VAR, hint, index, "varinfo");
};

variantsModalValidate = function() {
	var errorStatus = dialogError.none;

	try {
		
		// Business configuration
		var error = validateLengthMinMax("consumerOfferID", 1, 20,
				"Field Required (max 20 digits)", "variantsModal");
		error = validateNumber("consumerOfferID", "Numeric value required",
				"variantsModal")
				|| error;

		if ( !$("#isperpetual").is(':checked') ) {
			error = validateLengthMinMax("validityPeriodDays", 1, 10,
					"Field Required (max 10 digits)", "variantsModal") || error;
			error = validateNumber("validityPeriodDays", "Numeric value required",
					"variantsModal") || error;
		}
		
		error = validateLengthMinMax("subscriptionCharge", 1, 20,
				"Field Required (max 20 digits)", "variantsModal")
				|| error;
		error = validateCurrency("subscriptionCharge", "Currency value required",
				"variantsModal")
				|| error;

		error = validateLengthMinMax("firstRenewalWarningHoursBefore", 1, 20,
				"Field Required (max 20 digits)", "variantsModal")
				|| error;
		error = validateNumber("firstRenewalWarningHoursBefore",
				"Numeric value required", "variantsModal")
				|| error;
		error = validateLengthMinMax("secondRenewalWarningHoursBefore", 1, 20,
				"Field Required (max 20 digits)", "variantsModal")
				|| error;
		error = validateNumber("secondRenewalWarningHoursBefore",
				"Numeric value required", "variantsModal")
				|| error;

		error = validateLengthMinMax("renewalCharge", 1, 20,
				"Field Required (max 20 digits)", "variantsModal")
				|| error;
		error = validateCurrency("renewalCharge", "Currency value required",
				"variantsModal")
				|| error;
		
		if (error) {
			errorStatus += dialogError.busTab;
		}
		
		// Technical configuration
		error = validateLengthMinMax("subscriptionOfferID", 1, 20,
				"Field Required (max 20 digits)", "variantsModal");
		error = validateNumber("subscriptionOfferID", "Numeric value required",
				"variantsModal")
				|| error;
		if (dialogMode == "add") {
			error = validateLengthMinMax("variantID", 1, 40,
			"Field Required (max 40 characters)")
			|| error;
		}
		
		if (error) {
			errorStatus += dialogError.techTab;
		}
		
		// Texts configuration
		error = validateIPhrase("names", 1, 255,
				"Default Required (max 255 characters)", "qt_varname",
				"variantsModal");
		if (error) {
			errorStatus += dialogError.msgTab;
		}

	} catch (err) {
		console.error("variantsModalValidate() :" + err);
	}

	return errorStatus;
};


var hideShowValidityPeriodGroup = function() {
	if ( $("#isperpetual").is(':checked') ) {
		$("#validityPeriodDays").attr('disabled', true);
	} else {
		$("#validityPeriodDays").attr('disabled', false);
	}
};

var variantIsPerpetualRule = function() {
	$("#isperpetual").on("change", function() {
		hideShowValidityPeriodGroup();
	});
};

//(function($) {
	$(document).ready(function() { // wait for document ready
		
		variantIsPerpetualRule();

		try {
			$(".selectpicker").selectpicker();

			numericFieldChecker(); // Assign Numeric
			loadLanguages();
			loadReturnCodes();

		} catch (err) {
			alert("CreditSharing Error:" + err);
		}

		try {
			processModel = $("#processOptions").c4uprocessgrid({
				retrieveMethod : retrievePropertyData,
				serverurl : pageurl,
				updatePropertyCallback : updatePropertyCallback
			});
		} catch (err) {
			alert("Creating Process Model: " + err);
		}

		try {
			$(".returncodetexts").resultCodeController({});
			waitAndRegisterInputs();
		} catch(err) {
			console.error("creditsharing.ready (input registration): " + err);
		}

		try {
			updateDataTable("servicesTable");
			updateDataTable("quotasTable");
			updateDataTable("variantsTable");
		} catch(err) {
			console.error("creditsharing.ready(updateDataTable):" + err);
		}
		
		//Bind Rules
		scEligibilityEvents();
	});
//})(jQuery);