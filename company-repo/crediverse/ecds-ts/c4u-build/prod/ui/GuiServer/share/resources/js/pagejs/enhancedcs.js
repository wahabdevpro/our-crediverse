var COMP_VAR = "var", COMP_SC = "sc", COMP_TM = "tm";

updateTrasferOptions = function() {
	try {
		optionsInfoRequest(COMP_VAR, "VariantID", function(data) {
			updateOptionsElement(data);
		});
	} catch(err) {
		console.error(err);
	}
};

var giveElementId = function(primaryId, filterId, assignID) {
	var $element = $("#" + primaryId).siblings().find(filterId);
	$element.attr("id", assignID);
};

var updateSelectPickerButtonIds = function() {
	try {
		$(".selectpicker").each(function(i, obj) {
			if ($(obj).prop('tagName') == "SELECT") {
				var btnId = $(obj).attr("id");
				//Give Button ID for testing framework
				giveElementId(btnId, ":button", btnId + "btn");
				giveElementId(btnId, ".dropdown-menu.open", btnId + "Menu");
				giveElementId(btnId, ".dropdown-menu.inner", btnId + "Inner");				
			}
		});
	} catch(err) {
		console.error(err);
	}
};

var transferModeOnceOffRuleUpdate = function() {
	try {
		 var selected = $("#transferType").find("option:selected").val();
		 if (selected == "OnceOff") {
			 if ($("#retryIntervalMinutes").val().length == 0)
				 $("#retryIntervalMinutes").val("0");
			 if ($("#maxRetries").val().length == 0)
				 $("#maxRetries").val("0");
		 }
		 $("#retryIntervalMinutes").prop("disabled", (selected == "OnceOff"));
		 $("#maxRetries").prop("disabled", (selected == "OnceOff"));
	} catch(err) {
		console.error(err);
	}
};

var transferModeOnceOffRuleEvent = function() {
	try {
	  $('#transferType').on('change', function(){
		  transferModeOnceOffRuleUpdate();
	  });
	} catch(err) {
		console.error(err);
	}
};

var hideShowValidityPeriodGroup = function() {
	if ( $("#isperpetual").is(':checked') ) {
		$("#validityPeriodGroup").hide();
	} else {
		$("#validityPeriodGroup").show();
	}
};

var variantIsPerpetualRule = function() {
	$("#isperpetual").on("change", function() {
		hideShowValidityPeriodGroup();
	});
};

/** DOCUMENT READY **/
(function($) {
	$(document).ready(function() {
		try {
			variantIsPerpetualRule();
			
			//Commmon Loading
			$(".selectpicker").selectpicker();
			
			initializeAdvParameters("/encreditservice");
			numericFieldChecker();	//Assign Numeric
			loadLanguages();
			loadReturnCodes();
			
			//Return Code initialization
			$(".returncodetexts").resultCodeController({
			});
			
			ussdMenuInitialize();
			
	
			$("#vasCommands").vasCommandController({
				servlet : "/vct",
				field : "Commands",
				dataservlet : "/encreditservice"
			});
			
			updateTrasferOptions();
			
			//Register Errors
			var hash = {};
			hash["serviceClasses"] = "sc";
			ErrorHighlighter.register(hash);
			
			updateSelectPickerButtonIds();
			transferModeOnceOffRuleEvent();
			
		} catch(err){
			alert("CreditSharing Error:" + err);
		}
		waitAndRegisterInputs();
	});
})(jQuery);
//---------------------------------- SERVICE CLASS ------------------------------------------

showAddServiceClass = function() {
	resetModal("serviceClassModal", "Service Class", "add", "Save", COMP_SC, "scinfo");
	showTab("scconfig");
};

/** View / Edit **/
showServiceclass = function(mode, index) {
	resetModal("serviceClassModal", "Service Class", mode, "Update & Close", COMP_SC, "scinfo", index);
	componentInfoRequest(COMP_SC, index, "serviceClassModal", updateConfigInfoDialog, "serviceClassID");
	ErrorHighlighter.highlightDialogFieldError("serviceClasses", index);
	modalInfo.index = index;
	modalInfo.component = COMP_SC;
	modalInfo.divIdToUpdate = "scinfo";
	showTab("scconfig");
};

/** Delete **/
deleteServiceclass = function(index, hint) {
	showDelete(COMP_SC, "Service class " + hint, index, "scinfo");
};


serviceClassModalValidate = function() {
	var errorStatus = dialogError.none;
	
	var error = validateLengthMinMax("serviceClassID", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateNumber("serviceClassID", "Numeric value required") || error;
	error = validateLengthMinMax("maxRecipients", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateNumber("maxRecipients", "Numeric value required") || error;
	error = validateLengthMinMax("addRecipientCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("addRecipientCharge", "Currency value required", "serviceClassModalForm") || error;
	
	error = validateLengthMinMax("removeRecipientCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("removeRecipientCharge", "Currency value required") || error;
	
	error = validateLengthMinMax("addTransferCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("addTransferCharge", "Currency value required") || error;
	
	error = validateLengthMinMax("changeTransferCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("changeTransferCharge", "Currency value required") || error;
	
	error = validateLengthMinMax("removeTransferCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("removeTransferCharge", "Currency value required") || error;
	
	error = validateLengthMinMax("unsubscribeCharge", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateCurrency("unsubscribeCharge", "Currency value required") || error;
	
	error = validateNoPrimaryKeyDuplicate("serviceClassID", currentKey, hashIDStore.retrieve("serviceClassIDs"), "Service Class ID is not unique") || error;
	
	if (error) {
		errorStatus += dialogError.firstTab;
	}
	
	//Now name tags
	error = false;
	error = validateIText("name", 1, 255, "Default Required (max 255 characters)", "qt_scname", "serviceClassModal") || error;
	if (error) {
		errorStatus += dialogError.secondTab;
	}
	return errorStatus;
};

//---------------------------------- VARIANTS -----------------------------------------------
showAddVariants = function() {
	resetModal("variantsModal", "Variant", "add", "Save", COMP_VAR, "varinfo", null, updateTrasferOptions);
	showTab("variantsModalConfig");
};

showVariants = function(mode, index) {
	resetModal("variantsModal", "Variant", mode, "Update & Close", COMP_VAR, "varinfo", index, updateTrasferOptions);
	componentInfoRequest(COMP_VAR, index, "variantsModal", updateConfigInfoDialog, "variantID", function() {
		hideShowValidityPeriodGroup();
	});
	ErrorHighlighter.highlightDialogFieldError("variants", index);
	showTab("variantsModalConfig");
};

deleteVariants = function(index, hint) {
	modalInfo.afterUpdateCallback = updateTrasferOptions;
	showDelete(COMP_VAR, "Variant " + hint, index, "varinfo");
};

variantsModalValidate = function() {
	var errorStatus = dialogError.none;
	
	//First Check tab 1	
	try {
		var error = validateLengthMinMax("variantID", 1, 30, "Field Required (max 30 characters)") || error;
		
		// Only check "Validity Period" if Perpetual Variant is not checked
		if ( !$("#isperpetual").is(':checked') ) {
			error = validateLengthMinMax("validityPeriod", 1, 20, "Field Required (max 20 digits)") || error;
			error = validateNumber("validityPeriod", "Numeric value required") || error;
		}
		
		if (! $("#recurring").is(":checked")) {
			if ( $("#firstRenewalWarningHoursBefore").val().length == 0)
				$("#firstRenewalWarningHoursBefore").val("0");
			if ( $("#secondRenewalWarningHoursBefore").val().length == 0)
				$("#secondRenewalWarningHoursBefore").val("0");
			$("#renewalCharge").val("0");
		}
		
		error = validateLengthMinMax("firstRenewalWarningHoursBefore", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("firstRenewalWarningHoursBefore", "Numeric value required") || error;
		error = validateLengthMinMax("secondRenewalWarningHoursBefore", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("secondRenewalWarningHoursBefore", "Numeric value required") || error;

		error = validateLengthMinMax("subscriptionCharge", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("subscriptionCharge", "Currency value required") || error;
		
		error = validateLengthMinMax("renewalCharge", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("renewalCharge", "Currency value required") || error;
		
		error = validateNoPrimaryKeyDuplicate("variantID", currentKey, hashIDStore.retrieve("variantIDs"), "Variant ID is not unique") || error;		
		if (error) {
			errorStatus += dialogError.firstTab;
		}
		//Now name tags
		error = false;
		error = validateIText("name", 1, 255, "Default Required (max 255 characters)", "qt_varname", "variantsModal") || error;
		if (error) {
			errorStatus += dialogError.secondTab;
		}
	} catch(err) {
		console.error("variantsModalValidate() :" + err);
	}

	return errorStatus;
};

//---------------------------------- TRANSFER MODES -----------------------------------------------

showAddTransferMode = function() {
	resetModal("transferModal", "Transfer Mode", "add", "Save", COMP_TM, "tminfo");
	showTab("transferModalTransfer");
	transferModeOnceOffRuleUpdate();
};

showTransferMode = function(mode, index) {
	resetModal("transferModal", "Transfer Mode", mode, "Update & Close", COMP_TM, "tminfo", index);
	componentInfoRequest(COMP_TM, index, "transferModal", updateConfigInfoDialog, "transferModeID");
	ErrorHighlighter.highlightDialogFieldError("transferModes", index);
	showTab("transferModalTransfer");
};

deleteTransferMode = function(index, hint) {
	showDelete(COMP_TM, "Transfer Mode " + hint, index, "tminfo");
};

transferModalValidate = function() {
	var errorStatus = dialogError.none;
	
	//First Check tab 1	
	try {
		var error = validateLengthMinMax("transferModeID", 1, 255, "Field Required (max 255 characters)");
		error = validateLengthMinMax("donorAccountID", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("donorAccountID", "Numeric value required") || error;
		error = validateLengthMinMax("donorMinBalanceUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("donorMinBalanceUI", "Numeric value required") || error;
		error = validateLengthMinMax("donorMaxBalanceUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("donorMaxBalanceUI", "Numeric value required") || error;
		error = validateLengthMinMax("recipientAccountID", 1, 20, "Field Required (max 20 digits)", "transferModalForm") || error;
		error = validateNumber("recipientAccountID", "Numeric value required", "transferModalForm") || error;
		error = validateLengthMinMax("recipientMinBalanceUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("recipientMinBalanceUI", "Numeric value required") || error;
		error = validateLengthMinMax("recipientMaxBalanceUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("recipientMaxBalanceUI", "Numeric value required") || error;
		error = validateLengthMinMax("conversionRate", 1, 20, "Choice Required") || error;
		error = validateLengthMinMax("interval", 1, 20, "Field Required (max 20 digits)") || error;
		
		error = validateLengthMinMax("retryIntervalMinutes", 1, 20, "Field Required (max 20 digits)", "transferModalForm") || error;
		error = validateCurrency("retryIntervalMinutes", "Numeric value required", "transferModalForm") || error;
		
		error = validateLengthMinMax("maxRetries", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("maxRetries", "Numeric value required") || error;
		
		//Requirement T1422: When setting up a transfer mode, which is periodic, the threshold ID should not be mandatory.
		var transferType = $("#transferType").val();
		if ((transferType == "Periodic") && ($("#thresholdID").val().length == 0)) {
			$("#thresholdID").val("0");
		} else {
			error = validateLengthMinMax("thresholdID", 1, 20, "Field Required (max 20 digits)") || error;
			error = validateCurrency("thresholdID", "Numeric value required") || error;
		}
		
		error = validateNumber("interval", "Numeric value required") || error;
		error = validateLengthMinMax("thresholdID", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("thresholdID", "Numeric value required") || error;
		error = validateLengthMinMax("minAmountUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("minAmountUI", "Numeric value required") || error;
		error = validateLengthMinMax("maxAmountUI", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("maxAmountUI", "Numeric value required") || error;
		error = validateLengthMinMax("commissionAmount", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("commissionAmount", "Numeric value required") || error;
		error = validateLengthMinMax("commissionPercentage", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("commissionPercentage", "Numeric value required") || error;
		
		error = validateLengthMinMax("donorUnitsDisplayConversion", 1, 20, "Choice required") || error;
		error = validateLengthMinMax("recipientUnitsDisplayConversion", 1, 20, "Choice required") || error;
		
		error = validateNoPrimaryKeyDuplicate("transferModeID", currentKey, hashIDStore.retrieve("transferModeIDs"), "Transfer Mode ID is not unique") || error;
		
//		error = validateLengthMinMax("validDonorServiceClasses", 1, 1024, "Field Required") || error;
//		error = validateLengthMinMax("validRecipientServiceClasses", 1, 1024, "Field Required") || error;
//		error = validateLengthMinMax("requiredSubscriptionVariants", 1, 1024, "Field Required") || error;
		
		//var error = validateLengthMinMax("subscriptionOfferID", 1, 20, "Field Required (max 20 digits)", "variantsModal");
		
		if (error) {
			errorStatus += dialogError.firstTab;
		}
		//Now name tags
		error = false;
		error = validateIText("name", 1, 255, "Default Required (max 255 characters)", "qt_tmname", "transferModal") || error;
		error = validateIText("units", 1, 255, "Default Required (max 255 characters)", "qt_tmunits", "transferModal") || error;
		if (error) {
			errorStatus += dialogError.secondTab;
		}
	} catch(err) {
		console.error("transferModalValidate() :" + err);
	}

	return errorStatus;
};

updateValidateAndSave = function(system, compId, version) 
{
	resetSessionTimeout();
	componentInfo.system = system;
	componentInfo.compId = compId;
	componentInfo.version = version;
	
	// Save current State
	checkAndSaveMessageData(updateValidateAndSaveCallback);
};

//Save page to server
updateValidateAndSaveCallback = function() {
	resetSessionTimeout();
	validateAndSave(componentInfo.system, componentInfo.compId, componentInfo.version);
};

updateOptionsElement = function(data) {
	try {
		var $el = $("#requiredSubscriptionVariants");
		$el.empty();
		for (var i=0; i<data.options.length; i++) {
			$el.append($("<option></option>").attr("value", data.options[i]).text(data.options[i]));
		}
		$("#requiredSubscriptionVariants").selectpicker('refresh');
		updateSelectPickerButtonIds();
	} catch(err) {
		console.error(err);
	}
};