$(document).ready(function() {
	initializeAdvParameters("/credittransfer");
	try {
		initMessaging();
	} catch(err) {
		alert("page error: " + err);
	}
	
	//Update tables
	
	$(".returncodetexts").resultCodeController({
	});
	
	$("#variants").complexConfigController({
		url : "/credittransfer",
		field : "Variants",
		title : "Variants",
		tabs : {
			"Donor" : ["DonorMinBalanceUI", "DonorMaxBalanceUI", "DonorAccountID", "DonorAccountType", "DonorServiceClassIds", "UnitCostPerDonorUI", "CumulativeDonorLimitDailyUI", "CumulativeDonorLimitWeeklyUI", "CumulativeDonorLimitMonthlyUI", "DonorQuotasUI"],
			"Recipient Data" : ["RecipientServiceClassIds", "RecipientMinBalanceUI", "RecipientMaxBalanceUI", "RecipientAccountType", "RecipientAccountType", "RecipientAccountID", "UnitCostPerBenefitUI", "UnitCostPerBenefitUI", "CumulativeRecipientLimitDailyUI", "CumulativeRecipientLimitWeeklyUI", "CumulativeRecipientLimitMonthlyUI"],
			"Charges" : ["TransactionChargeBands"]
		}		
	});
	
	$("#vasCommands").vasCommandController({
		servlet : "/vct",
		field : "Commands",
		dataservlet : "/credittransfer",
		useFilteredTable : true
	});
	
	$("#donorQuotas").complexConfigController({
		url : "/credittransfer",
		field : "DonorQuotas",
		title : "Donor Quotas"
	});
	
	//Register for errors
	fieldErrorHelper.clearErrors();
	fieldErrorHelper.registerListener(function(fieldComponent, fieldIndex) {
		
		try {
			//Clear previous Errors
			if (fieldErrorHelper.previousErrorField != null) {
				var field = fieldErrorHelper.extractErrorField(fieldErrorHelper.previousErrorField, 0, false);
				if (field != null) {
					var index = fieldErrorHelper.extractErrorFieldIndex(fieldErrorHelper.previousErrorField, 0);
					var elementID = field + "edit_" + index;
					var row = $("#" + elementID).parent("td").parent();
					row.children('td').attr("style", "");
				}
			}

			//Update new Errors
			if (fieldComponent != null) {
				if (fieldComponent == "Commands") {
					fieldComponent = "vas";
				}
				var elementID = fieldComponent + "edit_" + fieldIndex;
				var row = $("#" + elementID).parent("td").parent();
				row.children('td').addClass("hightlightTableError");
			}
		} catch(err) {
			console.error(err);
		}
	});
});