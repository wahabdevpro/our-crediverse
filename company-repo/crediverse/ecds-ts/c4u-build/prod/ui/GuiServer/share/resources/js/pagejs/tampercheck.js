var pageurl = "/tampercheck"

actionOnContent = function(name, action) {
	if ((name == "account" || name == "agent") && (action == "reset")) {
		$(".tamperedAgentCheckReplyDiv").removeClass("hide");
		$(".tamperedAgentReply").html("Resetting...");
		var daturl = "act=" + action + "&name=" + name;
		var status = $.ajax({
			type: "POST", 
			url: pageurl, 
			data: daturl,
			dataType: "json"
		}).done(function(data) {
			resetSessionTimeout();			
			if (data.status == "fail")
			{
				$(".tamperedAgentReply").html("Reset Failed");
			}
			else
			{
				$(".tamperedAgentReply").html( data.message );
			}
		});
	
	} else if (name == "auditentry") {	
		$("#tamperedauditentries").load(pageurl + "?act=" + action + "&name=auditentry", function() {
			var auditEntriesTable = updateDataTable("tamperedAuditEntries_table");			
			auditEntries.order([[ 0, "desc" ]]).draw();
		});
	} else if (name == "batch") {
		$("#tamperedbatches").load(pageurl + "?act=" + action + "&name=batch", function() {
			var batchesTable = updateDataTable("tamperedBatches_table");			
			batchesTable.order([[ 0, "desc" ]]).draw();
		});
	}
};

agentEvent = function(action, entity, requestData) {
	var regexMsisdn = /^[+]{0,1}[0-9]+$/g;				
	var error = validateRegex("msisdn", regexMsisdn, "Agent's MSISDN required");								
	if (error) return;
	
	var daturl = "act=" + action + "&name=" + entity + "&" + requestData;
	//Post info to server
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		data: daturl,
		dataType: "json"
	}).done(function(data) {					
		$(".tamperedAgentCheckReplyDiv").removeClass("hide");
		resetSessionTimeout();

		if (data.status == "fail")
		{
			$(".tamperedAgentReply").html("Action failed.");
		}
		else
		{
			$(".tamperedAgentReply").html( data.message );
		}
	});

};

/** DOCUMENT READY **/
(function($) {
	$(document).ready(function() {
		try {			
			var accountsTable = updateDataTable("tamperedAccounts_table");
			accountsTable.order([[ 0, "desc" ]]).draw();
			
			var auditEntriesTable = updateDataTable("tamperedAuditEntries_table");
			auditEntriesTable.order([[ 0, "desc" ]]).draw();
			
			var batchesTable = updateDataTable("tamperedBatches_table");
			batchesTable.order([[ 0, "desc" ]]).draw();
			//Commmon Loading
			$(".selectpicker").selectpicker();		
			
			$(".refreshTamperedAuditEntries").on("click", function(ev) {
				try {
					actionOnContent("auditentry", "refresh");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			
			$(".refreshTamperedBatches").on("click", function(ev) {
				try {
					actionOnContent("batch", "refresh");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			//Reset...
			$(".resetTamperedAccounts").on("click", function(ev) {
				try {
					actionOnContent("account", "reset");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			$(".resetTamperedAgents").on("click", function(ev) {
				try {
					actionOnContent("agent", "reset");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			$(".resetTamperedAuditEntries").on("click", function(ev) {
				try {
					actionOnContent("auditentry", "reset");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			$(".resetTamperedBatches").on("click", function(ev) {
				try {
					actionOnContent("batch", "reset");
				} catch(err) {
					if (console) console.error(err);
				}
			});
			
			$(".checkAgent").on("click", function(ev) {				
				var requestData = $('#tamperedAgentForm').serialize();
				agentEvent("checkAgent", "agent", requestData);
			});
			$(".resetAgent").on("click", function(ev) {
				var requestData = $('#tamperedAgentForm').serialize();
				agentEvent("resetAgent", "agent", requestData);
			});
			$(".resetAccount").on("click", function(ev) {				
				var requestData = $('#tamperedAgentForm').serialize();
				agentEvent("resetAccount", "account", requestData);
			});
		} catch(err){
			alert("CreditSharing Error:" + err);
		}
		waitAndRegisterInputs();
	});
})(jQuery);

