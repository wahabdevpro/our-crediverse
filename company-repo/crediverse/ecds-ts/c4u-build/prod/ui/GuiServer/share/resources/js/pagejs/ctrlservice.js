//For delete:

var pageurl = "/ctrlservice";

$(document).ready(function() {
	try
	{
		var pasted = false;
		updateDataTable("serverinfo_table");
		updateDataTable("serverroles_table");
		
	    $(".numericfields").keydown(function (e) {
	        // Allow: backspace, delete, tab, escape, enter and .
	        if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110, 190]) !== -1 ||
	             // Allow: Ctrl+A Ctrl+A Ctrl+C
	            ((e.keyCode == 65 || e.keyCode == 86 || e.keyCode ==67) && e.ctrlKey === true) || 
	             // Allow: home, end, left, right
	            (e.keyCode >= 35 && e.keyCode <= 39)) {
	                 // let it happen, don't do anything
	                 return;
	        }

	        // Ensure that it is a number and stop the keypress
	        if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
	            e.preventDefault();
	        }
	    });
	    
	    $(".numericfields").keyup(function(e)
		{
		    if (/\D/g.test(this.value))
		    {
		        // Filter non-digits from input value.
		        this.value = this.value.replace(/\D/g, '');
		    }
		});

	} catch(err) {
		alert(err);
	}

});

validateAndSendServerInfo = function() 
{
	alert("validateAndSendServerInfo");
};


// ////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// Server Host
// ////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
var currentHostIndex = -1;

resetHostModal = function(title, showUpdateButton, buttonText) {
	hideErrorMessage("serverhost");
	hideErrorMessage("peerhost");
	hideErrorMessage("transactionNumberPrefix");
	$('#hostmodalform').trigger("reset");
	$("#serverInfoModalTitle").html(title);
	$("#serverInfoModalError").addClass("hide");
	if (showUpdateButton) {
		$("#saveHost").removeClass("hide");
		$("#saveHost").html(buttonText);
	} else {
		$("#saveHost").addClass("hide");
	}
};

showAddHost = function()
{
	resetHostModal("Add new Host", true, "Add Host");
	$("#hostmodalform :input").prop('disabled', false);
	$('#serverInfoModal').modal('show');
};

closeHostModal = function()
{
	$('#hostmodalform').trigger("reset");
	$('#serverInfoModal').modal('close');
};

validateAndSendHost = function()
{
	try 
	{
		var error = validateHostName("serverhost") |  validateHostName("peerhost");
		error = validateLengthMinMax("transactionNumberPrefix", 2, 2, "Numbers required (2 digits)") || error;
		error = validateNumber("transactionNumberPrefix", "Numbers required (2 digits)") || error;
		
		if (!error) 
		{
			//Set up and retrieve dialog form data
			var requestData = $('#hostmodalform').serialize();
			var action = ($("#saveHost").html().indexOf("Add") >=0)? "add" : "upd";
			$("#serverInfoWaiting").removeClass("hide");
			
			var daturl = "comp=host&act=" + action + "&"+requestData;
			if (currentHostIndex >=0 ) {
				daturl += "&index=" + currentHostIndex;
			}
			
			//Post info to server
			var status = $.ajax({
				type: "POST", 
				url: pageurl, 
				data: daturl,
				dataType: "json"
			}).done(function(data) {
				$("#serverInfoWaiting").addClass("hide");
				resetSessionTimeout();

				if (data.status == "fail")
				{
					$("#serverInfoModalError").removeClass("hide");
					$("#serverInfoModalError").html(data.message);
				}
				else
				{
					dataUpdated();
					$('#serverInfoModal').modal('hide');
					refreshContent("serverinfo", "/ctrlservice", "comp=host&act=refresh");
				}
			});
		}
	} 
	catch(err)
	{
		alert(err);
	}
};

deleteServerInfo = function(index, serverHost, peerHost) {
	$("#delErrorMessage").addClass("hide");
	
	try {
		$("#toRemoveMessage").html("Please confirm removal of host configuration [" + serverHost + " | " + peerHost + "]");
		delIndex = index;
		delComponent = "host";
		delServerHost = serverHost;
		delPeerHost = peerHost;
		$("#delModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

showServerInfo = function(mode, index)
{
	try
	{
		//mode = 'view' / 'edit'
		hideErrorMessage("serverhost");
		hideErrorMessage("peerhost");
		hideErrorMessage("transactionNumberPrefix");
		
		getInfoRequest("host", index, updateServerInfoDialog);
		$("#serverhost").prop('disabled', (mode=='view'));
		$("#peerhost").prop('disabled', (mode=='view'));
		$("#transactionNumberPrefix").prop('disabled', (mode=='view'));
		if (mode=='view')
		{
			$("#serverInfoModalTitle").html("View Host");
			$("#saveHost").addClass("hide");
		}
		else 
		{
			currentHostIndex = index;
			$("#serverInfoModalTitle").html("Edit Host");
			$("#saveHost").removeClass("hide");
			$("#saveHost").html("Update Host");
		}
	}
	catch(err)
	{
		alert(err);
	}

};

updateServerInfoDialog = function(data) {	
	try {
		$("#serverhost").val(data.serverHost);
		$("#peerhost").val(data.peerHost);
		$("#transactionNumberPrefix").val(data.transactionNumberPrefix);
	}
	catch(err)
	{
		alert(err);
	}
	$('#serverInfoModal').modal('show');
};



closeServerInfoModal = function()
{
	$("#serverhost").val("");
	$("#peerhost").val("");
};
//////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// Server Roles
// ////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
var currenRoleIndex = -1;

resetRoleModal = function(title, showUpdateButton, buttonText) {
	hideErrorMessage("roleName");
	hideErrorMessage("attachCommand");
	hideErrorMessage("detachCommand");

	$('#serverRoleModalForm').trigger("reset");
	$("#serverRoleModalTitle").html(title);
	$("#serverRoleModalError").addClass("hide");
	
	if (showUpdateButton) {
		$("#saveRole").removeClass("hide");
		$("#saveRole").html(buttonText);
	} else {
		$("#saveRole").addClass("hide");
	}	
};

showAddRole = function()
{
	resetRoleModal("Add new Role", true, "Add Role");
	$("#serverRoleModalForm :input").prop('disabled', false);
	$('#serverRoleModal').modal('show');
};

closeRoleModal = function()
{
	$('#serverRoleModalForm').trigger("reset");
	$('#serverRoleModal').modal('close');
};


validateAndSendRole = function()
{
	try 
	{
		var error = validateLengthMinMax("roleName", 2, 32, "2 to 32 characters");
		error = validateLengthMinMax("attachCommand", 0, 255, "up to 255 characters") || error;
		error = validateLengthMinMax("detachCommand", 0, 255, "up to 255 characters") || error;
		
		if (!error) 
		{
			//Set up and retrieve dialog form data
			var requestData = $('#serverRoleModalForm').serialize();
			var action = ($("#saveRole").html().indexOf("Add") >=0)? "add" : "upd";
			$("#serverRoleWaiting").removeClass("hide");
			var daturl = "comp=role&act=" + action + "&"+requestData;
			
			if (currenRoleIndex >=0 ) {
				daturl += "&index=" + currenRoleIndex;
			}
			
			//Post info to server
			var status = $.ajax({
				type: "POST", 
				url: pageurl, 
				data: daturl,
				dataType: "json"
			}).done(function(data) {
				$("#serverRoleWaiting").addClass("hide");
				resetSessionTimeout();

				if (data.status == "fail")
				{
					$("#serverRoleModalError").removeClass("hide");
					$("#serverRoleModalError").html(data.message);
				}
				else
				{
					dataUpdated();
					$('#serverRoleModal').modal('hide');
					refreshContent("serverroles", "/ctrlservice", "comp=role&act=refresh");
				}
			});
		}
	} 
	catch(err)
	{
		alert(err);
	}
};

deleteServerRole = function(index, roleName) {
	$("#delErrorMessage").addClass("hide");
	
	try {
		$("#toRemoveMessage").html("Please confirm removal of role [" + roleName + "]");
		delIndex = index;
		delComponent = "role";
		delRoleName = roleName;
		$("#delModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

showServerRole = function(mode, index)
{
	try
	{
		//mode = 'view' / 'edit'
		hideErrorMessage("roleName");
		hideErrorMessage("attachCommand");
		hideErrorMessage("detachCommand");
		
		getInfoRequest("role", index, updateServerRoleDialog);
		
		$("#serverRoleModalForm :input").prop('disabled', (mode=='view'));
		$("#cancelRole").prop('disabled', false);
		
		if (mode=='view')
		{
			resetRoleModal("View Role", false);
		}
		else 
		{
			resetRoleModal("Edit Role", true, "Update Role");
			currenRoleIndex = index;
		}
	}
	catch(err)
	{
		alert(err);
	}

};

updateServerRoleDialog = function(data) {	
	try {		
		$("#roleName").val(getValueIfDefined(data.serverRoleName));
		var isExclusive = (getValueIfDefined(data.exclusive) == "")? false : getValueIfDefined(data.exclusive);
		$('#exclusive').prop('checked', isExclusive);		
		$("#attachCommand").val(getValueIfDefined(data.attachCommand));
		$("#detachCommand").val(getValueIfDefined(data.detachCommand));
	}
	catch(err)
	{
		alert(err);
	}
	$('#serverRoleModal').modal('show');
};

getValueIfDefined = function(value) {
	if (typeof value !== 'undefined' && value != null)	
		return value;
	else
		return "";
};

closeServerRoleModal = function()
{
	$('#serverRoleModalForm').trigger("reset");
};
// ////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// Content update
// ////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

refreshContent = function(divId, contentUrl, postData)
{

	$("#" + divId).html("<img src='/img/cell_wait.gif' />");
	
	$.ajax({
		type: "POST",
		url: contentUrl,
		data: postData,
		success: function(content, textStatus, jqXHR) {
			try {
				$("#" + divId).html(content);
				updateDataTable(divId + "_table");
			} catch (err) {
				alert(err);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			//data - response from server
			alert("error!!: " + errorThrown);
		}
	});
}

// //////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// Common update
// //////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
var delIndex = null;
var delComponent = null;
var delServerHost = null;
var delPeerHost = null;
var delRoleName = null;

validateAndDelete = function() {
	$("#delwaiting").removeClass("hide");
	
	var data2Send = "comp="+delComponent+"&act=del&index="+delIndex;
	data2Send += ((delComponent=="host")? "&serverhost=" +  escape(delServerHost) + "&peerhost=" + escape(delPeerHost) : "&roleName=" + escape(delRoleName));
	
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		async: true,
		dataType: "json",
		data: data2Send
	}).done(function(data) {
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
			if (delComponent == "host") 
			{
				$("#serverinfo").load(pageurl + "?comp="+delComponent+"&act=refresh", function() {
					updateDataTable(delComponent + "_table");
				});
			}
			else
			{
				$("#serverroles").load(pageurl + "?comp="+delComponent+"&act=refresh", function() {
					updateDataTable(delComponent + "_table");
				});
			}
			
			$("#delModal").modal("hide");
		}
	});
};

getInfoRequest = function(component, index, callback) {
	var data2Send = "comp="+component+"&act=data&index="+index;
	
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