//Ready function
$(function() {
	resetSessionTimeout();
	$("#updateDetailsBtn").click(function() {
		$("#updateDetailsModal").modal("show");
	});
	
	$("#updatePassword").click(function() {
		$("#updatePasswordModal").find("input[type=password], textarea").val("");
		$("#updatePasswordModal").modal("show");
	});
});

updatePassword = function() {
	//Modal data validation
	var error = validateNotEmpty("oldpassword", "Old password required");
	error = validateNotEmpty("password1", "Password required") || error;
	error = validateNotEmpty("password2", "Repeat password required") || error;
	if (!error) {
		error = validateSame("password1", "password2", "Passwords not the same") || error;
	}
	
	if (!error) {
		var requestData = $('#passwordform').serialize();
		var url = "/service";
		$("#waitingPassword").removeClass("hide");
		
		var status = $.ajax({
			type: "POST", 
			url: url, 
			async: false, 
			data: "page=mydetails&act=pass&"+requestData
		}).done(function(data) {
			$("#waitingPassword").addClass("hide");
			if (data.result=="success" || data=="success") {
				$('#updatePasswordModal').modal('hide');
				$('body').removeClass('modal-open');
				$('.modal-backdrop').remove();
				window.location.href = "/logout";
			} else {
				if (data=="oldpass") {
					showErrorMessage("oldpassword", "Could not be validated");
				}			
			}
		});
		$("waiting").addClass("hide");
	}
};

updateDetails = function() {
	var error = validateNotEmpty("fullname", "Full Name required");
	
	if (!error) {
		var requestData = $('#detailsForm').serialize();
		var url = "/service";
		$("#waitingDetails").removeClass("hide");
		
		var status = $.ajax({
			type: "POST", 
			url: url, 
			async: false, 
			data: "page=mydetails&act=upd&"+requestData,
			dataType: "json",
		}).done(function(data) {
			$("#waitingDetails").addClass("hide");
			if (data.result=="success") {
				$("#myname").text(data.name);
				$('#updateDetailsModal').modal('hide');
				$('body').removeClass('modal-open');
				$('.modal-backdrop').remove();
				navigateTo('mydetails','mydetails');
			} else {
				alert("failed to save!");
			}
		});
		$("waiting").addClass("hide");
	}
};