var COMP_VAR = "Variants", COMP_SC="ServiceClasses";

function test(stuff) {
	console.log(stuff);
}

/** DOCUMENT READY **/
$(document).ready(function() {
	try {
		//Common Loading
		$(".selectpicker").selectpicker();
		
		initializeAdvParameters("/callmebackservice");
		numericFieldChecker();	//Assign Numeric
		loadLanguages();
		
		// Attach VAS Command JQuery Control
		$("#vasCommands").vasCommandController({
			servlet : "/vct",
			field : "Commands",
			dataservlet : "/callmebackservice"
		});
		
		//Return Code initialization
		loadReturnCodes();
		$(".returncodetexts").resultCodeController({
		});
		
		updateDataTable("Variants_table");
	} catch(err){
		alert("CreditSharing Error:" + err);
	}
	
});

//---------------------------------- VARIANTS -----------------------------------------------
showAddVariants = function() {
	resetModal("variantsModal", "Variants", "add", "Save", COMP_VAR, "varinfo");
	showTab("variantsModalConfig");
};

showVariants = function(mode, index) {
	resetModal("variantsModal", "Variant", mode, "Update & Close", COMP_VAR, "varinfo", index);
	componentInfoRequest(COMP_VAR, index, "variantsModal", updateConfigInfoDialog, "VariantID");
	showTab("variantsModalConfig");
};

deleteVariants = function(index, hint) {
	showDelete(COMP_VAR, "Variant " + hint, index, "varinfo");
};

variantsModalValidate = function() {
	var errorStatus = dialogError.none;
	
	//First Check tab 1	
	try {
		var error = validateLengthMinMax("VariantID", 1, 30, "Field Required (max 30 characters)") || error;

		error = validateLengthMinMax("variantType", 1, 30, "Field Required (max 30 characters)") || error;
		
		error = validateLengthMinMax("freeDailyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("freeDailyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("maxDailyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("maxDailyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("freeWeeklyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("freeWeeklyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("maxWeeklyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("maxWeeklyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("freeMonthlyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("freeMonthlyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("maxMonthlyRequests", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("maxMonthlyRequests", "Numeric value required") || error;
		
		error = validateLengthMinMax("charge", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateCurrency("charge", "Currency value required") || error;		
		
		error = validateLengthMinMax("numberPlanString", 5, 200, "Field Required (max 200 digits)") || error;
		
		error = validateNoPrimaryKeyDuplicate("VariantID", currentKey, hashIDStore.retrieve("variantIDs"), "Variant ID is not unique") || error;		
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

//---------------------------------- SERVICE CLASS -----------------------------------------------
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

	error = validateLengthMinMax("maxBal", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateNumber("maxBal", "Numeric value required") || error;
	
	error = validateLengthMinMax("minBal", 1, 20, "Field Required (max 20 digits)") || error;
	error = validateNumber("minBal", "Numeric value required") || error;
	
	error = validateLengthMinMax("variantString", 5, 200, "Field Required (max 200 digits)") || error;

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
