var COMP_VAR = "Variants";

function test(stuff) {
	console.log(stuff);
}

/** DOCUMENT READY **/
$(document).ready(function() {
	try {
		//Commmon Loading
		$(".selectpicker").selectpicker();
		
		initializeAdvParameters("/pinservice");
		numericFieldChecker();	//Assign Numeric
		loadLanguages();
		
		$("#vasCommands").vasCommandController({
			servlet : "/vct",
			field : "Commands",
			dataservlet : "/pinservice"
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
		error = validateLengthMinMax("MaxRetries", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("MaxRetries", "Numeric value required") || error;
		
		error = validateLengthMinMax("MinLength", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("MinLength", "Numeric value required") || error;
		
		error = validateLengthMinMax("MaxLength", 1, 20, "Field Required (max 20 digits)") || error;
		error = validateNumber("MaxLength", "Numeric value required") || error;
		
//		error = validateLengthMinMax("defaultPin", 1, 20, "Field Required (max 20 digits)") || error;
//		error = validateNumber("defaultPin", "Numeric value required") || error;
		
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

