var modal1 = "Roles";
var modal2 = "General";
var toDelete = "";
var toUpdate = "";
var viewMode = true;
var currentDialogMode = "";

$(document).ready(function() {
	try {
		updateDataTable("manusers");
	} catch(err){
	}
});

addNewUser = function() {
	configureUserDialog("add");
	$("#myModalLabel").html("Add New User");
	try {
		$("#userid").val("");
		$("#fullname").val("");
		$("#mobile").val("");
		$('input:checkbox[id^="role_"]:checked').prop('checked', false);
		displayModalWindow(modal1);
		$("#addUserModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

configureUserDialog = function(mode)
{
	currentDialogMode = mode;
	$(".passstrength").html("");
	$("#password1").val("");
	$("#password2").val("");
	
	viewMode = (mode=='view');
	$("#userid").prop('disabled', ((mode=='view') || (mode=='edit')));
	$("#fullname").prop('disabled', viewMode);
	$("#mobile").prop('disabled', viewMode);
	$("#password1").prop('disabled', viewMode);
	$("#password2").prop('disabled', viewMode);
	$("#saveUser").prop('disabled', viewMode);
	$("#saveUser").html((mode=='add')? "Add User" : "Update User");
	$("#viewRoles").addClass("hide");
	$("#editRoles").addClass("hide");
};

showUserInfo = function(userid, isUpdate)
{
	if (isUpdate)
		$("#myModalLabel").html("Edit User <b>" + userid + "</b>");
	else
		$("#myModalLabel").html("View User <b>" + userid + "</b>");
		
	try {	
		var status = $.ajax({
			type: "POST", 
			url: "/service", 
			async: true, 
			data: "page=manusers&act=ret&userid=" + userid
		}).done(function(data) {

			resetSessionTimeout();
			if (data == 'self') {
				window.location.href = "/logout";
			} else if(data != 'fail') {
				try
				{
					var obj = jQuery.parseJSON(data);
					$("#userid").val(obj.userId);
					$("#fullname").val(obj.name);
					$("#mobile").val(obj.mobileNumber);
					
					//Remove checkboxes first
					$('input:checkbox[id^="role_"]:checked').prop('checked', false);
					
					//Extract roles
					if (obj.roleIds != null) {
						for(var i=0; i<obj.roleIds.length; i++)
						{
							$("#role_" + obj.roleIds[i]).prop('checked', true);
							$("#viewrole_" + obj.roleIds[i]).prop('checked', true);
						}
					}
					configureUserDialog(isUpdate? "edit" : "view");
					
					toUpdate = obj.userId;
					$("#saveUser").html("Update User");
					displayModalWindow(modal1);
					$("#addUserModal").modal("show");
				}
				catch(err) {
					alert(err);
				}

				
			} else {
				alert("Could not retrieve user information");
			}
		});
	} catch(err) {
		alert(err);
	}

};

deleteUser = function(userId) {
	$("#toRemoveMessage").html("Please confirm removal of user " + userId);
	toDelete = userId;
	$("#delModal").modal("show");
};

displayModalWindow = function(page) {
	var newRoleWindow = (viewMode? "view" : "edit") + modal1;
	
	if (page==modal2) {
		$("#nextmodalwindow").html(modal2);
		$("#"+modal2).addClass("hide");
		$("#"+newRoleWindow).removeClass("hide");
	} else {
		$("#nextmodalwindow").html(modal1);
		$("#"+newRoleWindow).addClass("hide");
		$("#"+modal2).removeClass("hide");
	}
};

modalSwitch = function() 
{
	var page = $("#nextmodalwindow").html();
	if (page==modal1) {
		displayModalWindow(modal2);
	} else {
		displayModalWindow(modal1);
	}
};

validateAndSend = function()
{
	try {
		//Precheck for errors
		var error = validateLengthMinMax("userid", 2, 15, "2 to 15 characters required");
		error = validateLengthMinMax("fullname", 5, 60, "5 to 60 characters required") || error;		
		error = validateLengthMinMax("mobile", 0, 15, "up to 15 characters allowed") || error;

		error = validateSame("password1", "password2", "Passwords not the same") || error;
		
		//No Errors send and save
		if (!error) {
			var requestData = $('#modalform').serialize();
			var url = "/service";
			$("#waiting").removeClass("hide");
			
			var updateData = "page=manusers&act=" + ((currentDialogMode=='add')? "add&" : "upd&userid="+toUpdate+"&") + requestData;
			var status = $.ajax({
				type: "POST", 
				url: url, 
				async: false, 
				data: updateData
			}).done(function(data) {
				try 
				{
					resetSessionTimeout();
					$("#waiting").addClass("hide");
					if (data=="success") {
						$('#addUserModal').modal('hide');
						$('body').removeClass('modal-open');
						$('.modal-backdrop').remove();
						navigateTo('userman','manusers');
					} else if (data=="name_fail") {
						showErrorMessage("userid", "Duplicate User ID");
					}
				}
				catch(err) {
					alert(err);
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

clearField = function(compid) {
	$("#" + compid).val("");
	hideErrorMessage(compid);
};

clearModal = function() {
	clearField("userid");
	clearField("fullname");
	clearField("mobile");
	clearField("password1");
	clearField("password2");
	return true;
};

validateAndDelete = function() {
	var requestData = $('#modalform').serialize();
	var url = "/service";
	$("#delwaiting").removeClass("hide");
	
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: false, 
		data: "page=manusers&act=del&userid=" + toDelete
	}).done(function(data) {
		resetSessionTimeout();
		$("#delwaiting").addClass("hide");
		$('#delModal').modal('hide');
		if (data=="self") {
			window.location.href = "/logout";
		} else if (data=="success") {
			$('body').removeClass('modal-open');
			$('.modal-backdrop').remove();
//			loadService("manusers");
			navigateTo('userman','manusers');
		} else {
			alert("failed to delete!");
		}
	});
};

$('#password1').keyup(function(e) {
    var strongRegex = new RegExp("^(?=.{8,})(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*\\W).*$", "g");
    var mediumRegex = new RegExp("^(?=.{7,})(((?=.*[A-Z])(?=.*[a-z]))|((?=.*[A-Z])(?=.*[0-9]))|((?=.*[a-z])(?=.*[0-9]))).*$", "g");
    var enoughRegex = new RegExp("(?=.{6,}).*", "g");
    if (false == enoughRegex.test($(this).val())) {
            $('.passstrength').html('<span class="pass_chars">Too short</span>');
    } else if (strongRegex.test($(this).val())) {
            $('.passstrength').className = 'ok';
            $('.passstrength').html('<span class="pass_strong">Strong!</span>');
    } else if (mediumRegex.test($(this).val())) {
            $('.passstrength').className = 'alert';
            $('.passstrength').html('<span class="pass_medium">Medium!</span>');
    } else {
            $('.passstrength').className = 'error';
            $('.passstrength').html('<span class="pass_weak">Weak!</span>');
    }
    return true;
});