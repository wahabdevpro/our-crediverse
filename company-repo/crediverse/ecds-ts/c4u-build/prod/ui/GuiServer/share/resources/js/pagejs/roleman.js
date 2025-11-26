var modal1 = "Permissions";
var modal2 = "General";
var viewMode = true;

var toDelete = "";
var toUpdate = "";

//Ready function
$(function() 
{
	resetSessionTimeout();
	updateDataTable("manroles");
	
	//Assign
	$("#modalDelete").click(function() {
		validateAndDelete();
	});
});

showAdd = function() 
{
	displayModalWindow(modal1);
	configureRolesDialog('add');
	$('input:checkbox[id^="perm_"]:checked').prop('checked', false);
	$("#myModalLabel").html("Add New Security Role");
	toUpdate = "";
	$("#saveRole").html("Add Role");
	$('#addRoleModal').modal('show');
}

deleteRole = function(roleId) {
	try {
		$("#toRemoveMessage").html("Please confirm removal of role " + roleId);
		toDelete = roleId;
		$("#delModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

configureRolesDialog = function(mode)
{
	hideErrorMessage("name");
	hideErrorMessage("description");
	
	viewMode = (mode=='view');
	$("#name").prop('disabled', viewMode);
	$("#description").prop('disabled', viewMode);
	$("#description").prop('disabled', viewMode);
	$("#saveRole").prop('disabled', viewMode);
	$("#saveRole").html((mode=='add')? "Add Role" : "Update Role");
	$("#viewPermissions").addClass("hide");
	$("#editPermissions").addClass("hide");	
	if (mode=='add') {
		$("#name").val("");
		$("#description").val("");
	}
};

validateAndDelete = function() {
	var requestData = $('#modalform').serialize();
	var url = "/service";
	$("#delwaiting").removeClass("hide");
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: false, 
		data: "page=manroles&act=del&roleid=" + toDelete
	}).done(function(data) {
		resetSessionTimeout();
		$("#delwaiting").addClass("hide");
		$('#delModal').modal('hide');
		if (data=="success") {
			$('body').removeClass('modal-open');
			$('.modal-backdrop').remove();
			navigateTo('userman','manroles');
		} else {
			alert("failed to delete!");
		}
	});
};

modalSwitch = function() 
{
	try {
		var page = $("#nextmodalwindow").html();
		if (page==modal1) {
			displayModalWindow(modal2);
		} else {
			displayModalWindow(modal1);
		}
	} catch(err) {
		alert(err);
	}
};

displayModalWindow = function(page) 
{
	var newRoleWindow = (viewMode? "view" : "edit") + modal1;
	if (page==modal2) {
		$("#nextmodalwindow").html(modal2);
		$("#"+modal2).addClass("hide");
		$("#"+newRoleWindow).removeClass("hide");
		hideSelectButtons(viewMode);
	} else {
		hideSelectButtons(true);
		$("#nextmodalwindow").html(modal1);
		$("#"+newRoleWindow).addClass("hide");
		$("#"+modal2).removeClass("hide");
	}
};

hideSelectButtons = function(hide)
{
	if (hide)
	{
		$("#selectall").addClass("hide");
		$("#unselectall").addClass("hide");
	}
	else
	{
		$("#selectall").removeClass("hide");
		$("#unselectall").removeClass("hide");
	}
};

//For add and update
validateAndSend = function()
{
	try {
		var error = validateLengthMinMax("name", 3, 60, "3 to 60 characters required") || error;
		error = validateLengthMinMax("description", 5, 100, "5 to 100 characters required") || error;
		
		if (!error) {
			var requestData = $('#modalform').serialize();
			var url = "/service";
			$("#waiting").removeClass("hide");

			var status = $.ajax({
				type: "POST", 
				url: url, 
				async: false, 
				dataType: "json",
				data: "page=manroles&act=add&"+requestData + ((toUpdate.length==0)? "" : "&roleid=" + toUpdate)
			}).done(function(data) {
				resetSessionTimeout();
				$("#waiting").addClass("hide");
				if (data.status == "pass") {
					$('#addRoleModal').modal('hide');
					$('body').removeClass('modal-open');
					$('.modal-backdrop').remove();
					navigateTo('userman','manroles');
				} else {
					displayModalWindow(modal1);
					if (data.message == "RoleName exists")
					{
						showErrorMessage("name", "Duplicate role found");
					}
					else
					{
						showErrorMessage("description", data.message);	
					}
				}
			});
			$("waiting").addClass("hide");
		} else {
			displayModalWindow(modal1);
		}	
	} catch(err) {
		alert(err);
	}
	
};

clearField = function(compid) 
{
	$("#" + compid).val("");
	hideErrorMessage(compid);
};

clearModal = function() 
{
	clearField("name");
	clearField("description");
	$("#name").attr('readonly', false);
	$("#description").attr('readonly', false);
	$("#saveRole").attr('readonly', false);
	$('input:checkbox[id^="perm_"]').attr('readonly', false);	
	return true;
};


showRoleInfo = function(roleid, isUpdate)
{
	try {	
		var status = $.ajax({
			type: "POST", 
			url: "/service", 
			async: true, 
			data: "page=manroles&act=ret&roleid=" + roleid
		}).done(function(data) 
		{
			if(data != 'fail') {			
				resetSessionTimeout();
				var obj = jQuery.parseJSON(data);
				if (isUpdate) {
					$("#myModalLabel").html("Edit Security Role <b>" + obj.name + "</b>");
					configureRolesDialog('edit');
				}
				else {
					$("#myModalLabel").html("View Security Role <b>" + obj.name + "</b>");
					configureRolesDialog('view');
				}
				
				$("#name").val(obj.name);
				$("#description").val(obj.description);
				$("#saveRole").html("Update Role");
				
				//Remove checkboxes first
				$('input:checkbox[id^="perm_"]:checked').prop('checked', false);
				$('input:checkbox[id^="viewperm_"]:checked').prop('checked', false);
				
				//Extract permissions
				
				if (obj.permissionIds != null) 
				{
					for(var i=0; i<obj.permissionIds.length; i++)
					{
						var escaped = obj.permissionIds[i].replace(/\./g, "\\.");
						$("#perm_" + escaped).prop('checked', true);
						$("#viewperm_" + escaped).prop('checked', true);
					}
				}
				
				$('#addRoleModal').modal('show');
				toUpdate = obj.roleId;
			}
			displayModalWindow(modal1);
		});
	} 
	catch(err) 
	{
		alert(err);
	}

};

checkPermission = function(permId, implyId, impliedBy)
{
	
	try
	{
		var permPath = permId.substring(0, permId.lastIndexOf("."));
		if (implyId != "null") {
			implyId = permPath + "." + implyId;
		}
		if (impliedBy != "null") {
			impliedBy = permPath + "." + impliedBy;
		}
		
		//escape paths
		permId = permId.replace(/\./g, "\\.");
		implyId = implyId.replace(/\./g, "\\.");
		impliedBy = impliedBy.replace(/\./g, "\\.");
		
		console.log(permId);
		console.log(impliedBy);
		
		if ($("#perm_" + permId).prop('checked'))
		{
			var changeCheckedId = "perm_" + implyId;
			$
			$('input:checkbox[id="'+changeCheckedId+'"]').prop('checked', true);
		}
		else
		{
			var strArr = impliedBy.replace(/[\[\]']+/g,'')
			var arr= strArr.split(",");
			for(var i=0; i<arr.length; i++)
			{
				$('input:checkbox[id="perm_'+arr[i]+'"]:checked').prop('checked', false);
			}
		}
	}
	catch(err)
	{
		alert(err);
	}
};

selectAllPermissions = function()
{
	$('input:checkbox[id^="perm_"]:not(:disabled)').prop('checked', true);
};

unselectAllPermissions = function()
{
	$('input:checkbox[id^="perm_"]:not(:disabled)').prop('checked', false);
};

