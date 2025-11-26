var pageurl = "/airsimconfig"

var deleteInfo = {
	index : 0,
	name : ''
};

resetUsageModal = function(title, showUpdateButton, buttonText) {
	
	
//	hideErrorMessage("fileType");

	$('#configInfomodalForm').trigger("reset");
	$("#configInfomodalTitle").html(title);
	$("#configInfomodalError").addClass("hide");
//	$("#cancelUpdate").prop('disabled', false);
};

showAddUsageTimer = function() {
	$("#configInfomodalError").removeClass("show");
	resetUsageModal("Add New Configuration", true, "Add");
	currenConfigIndex = -1;
//	$("#configInfomodalForm :input").prop('disabled', false);
	$('#configInfoModal').modal('show');
};

refreshConfigContent = function(name) {
	if (name == "sms") {
		$("#smsHistory").load(pageurl + "?act=refresh&name=sms", function() {
			var sms = updateDataTable("smshistory_table");
			sms.order([[ 0, "desc" ]]).draw();
		});
	} else if (name == "email") {
		$("#emailHistory").load(pageurl + "?act=refresh&name=email", function() {
			var email = updateDataTable("emailhistory_table");
			email.order([[ 0, "desc" ]]).draw();
		});
	} else {
		$("#usagetimers").load(pageurl + "?act=refresh", function() {
			updateDataTable("fileconfigurations");
		});
	}
};

addUsageTimer = function() {
	try {
		
		// Check for Modal Errors
		var error = validateLengthMinMax("msisdna", 5, 20, "Between 5 and 20 characters") || error;
		if ($("#msisdnb").val().length > 0)
		{
			error = validateLengthMinMax("msisdnb", 5, 20, "Between 5 and 20 characters") || error;
			error = validateLengthMinMax("msisdnb", $("#msisdna").val().length, $("#msisdna").val().length, "Length not same as MSISDN A") || error;
		}
		error = validateNumber("account", "Numeric value required") || error;
		error = validateNumber("amount", "Numeric value required") || error;
		error = validateNumber("interval", "Numeric value required") || error;
		error = validateNumber("standardDeviation", "Numeric value required") || error;
		if (!error) 
		{
			var requestData = $('#configInfomodalForm').serialize();
			var daturl = "act=add&" + requestData;
			$("#updateWaiting").addClass("show");
			
			//Post info to server
			var status = $.ajax({
				type: "POST", 
				url: pageurl, 
				data: daturl,
				dataType: "json"
			}).done(function(data) {
				$("#updateWaiting").removeClass("show");
				resetSessionTimeout();

				if (data.status == "fail")
				{
					$("#configInfomodalError").addClass("show");
					$("#configInfomodalError").html(data.message);
				}
				else
				{
					$('#configInfoModal').modal('hide');
					$("#configInfomodalError").removeClass("show");
					refreshConfigContent();
				}
			});
		}
	} catch(err) {
		if (console)
			console.error("addUsageTimer Error: " + err);
	}
};

//-----------------------------------------------------------------------
/** DELETE MODAL **/
//-----------------------------------------------------------------------
var deleteInfo = {
	index : 0,
	name : ''
};

deleteRecordInfo = function(index, msisdn) {
	$("#delErrorMessage").addClass("hide");
	try {
		if (typeof msisdn !== "undefined")
		{
			$("#toRemoveMessage").html("Please confirm removal of MSISDN <b>" + msisdn + "</b>");
			deleteInfo.index = index;
			deleteInfo.name = msisdn;
		}
		else
		{
			$("#toRemoveMessage").html("Please confirm removal of all MSISDNs");
			deleteInfo.index = 0;
			deleteInfo.name = "all";
		}
		
		$("#delModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

validateAndDelete = function() {
	$("#delwaiting").addClass("show");
	var data2Send = "act=del&index="+deleteInfo.index+"&name="+deleteInfo.name;
	
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
		$("#delwaiting").removeClass("show");
		resetSessionTimeout();
		
		$("#delwaiting").addClass("hide");
		
		
		if (data.status=="fail")
		{
			$("#delErrorMessage").html(data.message);
			$("#delErrorMessage").removeClass("hide");
		}
		else
		{
			refreshConfigContent(deleteInfo.name);			
			$("#delModal").modal("hide");
		}
	});
};

/** DOCUMENT READY **/
(function($) {
	$(document).ready(function() {
		try {
			updateDataTable("fileconfigurations");
			var sms = updateDataTable("smshistory_table");
			sms.order([[ 0, "desc" ]]).draw();

			var email = updateDataTable("emailhistory_table");
			email.order([[ 0, "desc" ]]).draw();
			
			//Commmon Loading
			$(".selectpicker").selectpicker();
			
			$(".clearSMSs").on("click", function(ev) {
				$("#delErrorMessage").addClass("hide");
				try {
					$("#toRemoveMessage").html("Please confirm clearing of all SMS<br/><br/>");
					deleteInfo.index = 0;
					deleteInfo.name = "sms";
					
				} catch(err) {
					if (console) console.error(err);
				}
				$("#delModal").modal("show");
			});
			
			$(".refreshSMSs").on("click", function(ev) {
				try {
					refreshConfigContent("sms");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			$(".clearEmails").on("click", function(ev) {
				$("#delErrorMessage").addClass("hide");
				try {
					$("#toRemoveMessage").html("Please confirm clearing of all emails<br/><br/>");
					deleteInfo.index = 0;
					deleteInfo.name = "email";
					
				} catch(err) {
					if (console) console.error(err);
				}
				$("#delModal").modal("show");
			});
			
			$(".refreshEmails").on("click", function(ev) {
				try {
					refreshConfigContent("email");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			var sendUssdFunction = function(ev) {
				//Validate input
				var regexUssd = /^[*#0-9]+$/g;
				var regexMSISDN = /^[+]{0,1}[0-9]+$/g;
				var error = validateRegex("ussdCommand", regexUssd, "Invalid USSD Command");
				error |= validateRegex("ussdMsisdn", regexMSISDN, "Invalid MSISDN");
				if (error) return;
				
				var requestData = $('#airSimUssdForm').serialize();
				var daturl = "act=ussd&" + requestData;
				
				//Post info to server
				var status = $.ajax({
					type: "POST", 
					url: pageurl, 
					data: daturl,
					dataType: "json"
				}).done(function(data) {
					$(".ussdReplyDiv").removeClass("hide");
					resetSessionTimeout();
						
					if (data.status == "fail")
					{
						$(".ussdReply").html("Send Ussd Failed");
					}
					else
					{
						$(".ussdReply").html( data.message );
					}
				});
			};
			
			$("#ussdCommand").on("keypress", function(ev) {
                if (ev.which != 13) return;
                sendUssdFunction(ev);
			});
			
			$(".sendUssd").on("click", sendUssdFunction);
			
			$(".sendSms").on("click", function(ev) {
				var regexFrom = /^[+]{0,1}[0-9]+$/g;
				var regexTo = /^[+]{0,1}[0-9]+$/g;
				
				var error = validateRegex("smsFrom", regexFrom, "From address required");
				error |= validateRegex("smsTo", regexTo, "To address required");
				error |= validateLengthMinMax("smsText", 1, 1000, "SMS needs to be between 1 and 1000 characters");
				
				if (error) return;
				
				var requestData = $('#airSimSmsForm').serialize();
				var daturl = "act=sms&" + requestData;
				
				//Post info to server
				var status = $.ajax({
					type: "POST", 
					url: pageurl, 
					data: daturl,
					dataType: "json"
				}).done(function(data) {
					$(".smsReplyDiv").removeClass("hide");
					resetSessionTimeout();

					if (data.status == "fail")
					{
						$(".smsReply").html("Send Ussd Failed");
					}
					else
					{
						$(".smsReply").html( data.message );
					}
				});
			});
			
			$(".sendAirResponseUpdate").on("click", function(ev) {
				var error = validateLengthMinMax("airCall", 1, 30, "Air call type required");
				error |= validateNumber("responseCode", "Numeric value required");
				error |= validateNumber("delay", "Numeric value required");
				
				if (error) return;
				
				var requestData = $('#airResponseForm').serialize();
				var daturl = "act=sendAirResponseUpdate&" + requestData;
				
				//Post info to server
				var status = $.ajax({
					type: "POST", 
					url: pageurl, 
					data: daturl,
					dataType: "json"
				}).done(function(data) {
					$(".updateReplyDiv").removeClass("hide");
					resetSessionTimeout();

					if (data.status == "fail")
					{
						$(".updateReply").html("Air update failed");
					}
					else
					{
						$(".updateReply").html( data.message );
					}
				});
			});	
			
			$(".sendAirResponseReset").on("click", function(ev) {
				var error = validateLengthMinMax("airCall", 1, 30, "Air call type required");
				if (error) return;				
				var requestData = $('#airResponseForm').serialize();
				var daturl = "act=sendAirResponseReset&" + requestData;
				
				//Post info to server
				var status = $.ajax({
					type: "POST", 
					url: pageurl, 
					data: daturl,
					dataType: "json"
				}).done(function(data) {
					$(".updateReplyDiv").removeClass("hide");
					resetSessionTimeout();

					if (data.status == "fail")
					{
						$(".updateReply").html("Air update failed");
					}
					else
					{
						$(".updateReply").html( data.message );
					}
				});
			});	
		} catch(err){
			alert("CreditSharing Error:" + err);
		}
		waitAndRegisterInputs();
	});
})(jQuery);

