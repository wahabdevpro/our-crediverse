var pageurl = "/fileconfig";

var fieldStructs = null;

/*
<div class="row">
	<div class="col-md-6">
		<label for="testtime" class="control-label">Some Time Thing</label>
		<div class="input-group" id="mytimePickerTest"></div>
		<span id="timeControl_error" class="error_message hide"></span>
	</div>
</div

 */

initTimeControl = function() {
	//C4UTimepicker
	$("#mytimePickerTest").c4utimepicker();
	
//	$("#timePicker").click(function() {
//		var id = $(this).attr("id");
//		
//	});
};

updateStruct = function(data) {
	fieldStructs = data.struct;
};


$(document).ready(function() {
	try
	{
		sendAsyncAjax(pageurl, "act=struct", updateStruct, null);
		updateDataTable("fileconfigurations");
		
        try {
        	$(".selectpicker").selectpicker();
//        	initTimeControl();
        } catch(err) {
        	alert(err);
        }
        $("#processStartTimeOfDayPicker").datetimepicker({
            pickDate: false,
            format: 'HH:mm',
            language: 'en',
            pick12HourFormat: true
        });
        $("#processEndTimeOfDayPicker").datetimepicker({
            pickDate: false,
            format: 'HH:mm',
            language: 'en',
            pick12HourFormat: true
        });
        
        $("#copyCommandHelpBtn").on("click", function() {
        	if ($(".copyCommandInfo").is(":visible") ) {
        		$(".copyCommandInfo").hide();
        	} else {
        		$(".copyCommandInfo").show();
        	}
        });
		
//		$("#startTimeOfDay").timepicker({showMeridian:false,showSeconds:true,minuteStep:5,secondStep:5,defaultTime:false});
//		$("#startTimeOfDay").timepicker("setTime", "00:00:00");
	} catch(err){
		alert(err);
	}
});



//-----------------------------------------------------------------------
/** DELETE MODAL **/
//-----------------------------------------------------------------------
var deleteInfo = {
	index : 0,
	name : ''
};

deleteRecordInfo = function(index, name) {
	$("#delErrorMessage").addClass("hide");
	try {
		$("#toRemoveMessage").html("Please confirm removal of configuration <b>" + name + "</b>");
		deleteInfo.index = index;
		deleteInfo.name = name
		$("#delModal").modal("show");
	} catch(err) {
		alert(err);
	}
};

validateAndDelete = function() {
	$("#delwaiting").removeClass("hide");
	var data2Send = "act=del&index="+deleteInfo.index+"&name="+deleteInfo.name;
	
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
			refreshConfigContent();			
			$("#delModal").modal("hide");
		}
	});
};
var currenConfigIndex = 0;
//-----------------------------------------------------------------------
/** ADD  **/
//-----------------------------------------------------------------------

refreshConfigContent = function() {
	$("#fileconfigs").load(pageurl + "?act=refresh", function() {
		updateDataTable("fileconfigurations");
	});
};

showAddConfig = function() {
	resetConfigModal("Add New Configuration", true, "Add");
	currenConfigIndex = -1;
	resetModal("Add new Configuration", true, "Add Config");
	$("#configInfomodalForm :input").prop('disabled', false);
	updateDefaults();
	$('#configInfoModal').modal('show');
};


//-----------------------------------------------------------------------
/** EDIT  **/
//-----------------------------------------------------------------------

showRecordInfo = function(mode, index) {
	try {
//		hideErrorMessage("serverhost");
//		hideErrorMessage("peerhost");
		
		getInfoRequest(index, updateConfigInfoDialog);
		
		if (mode=='view')
		{
			resetConfigModal("View Configuration", false);
			currenConfigIndex = -1;
		}
		else 
		{
			resetConfigModal("Edit Configuration", true, "Update & Close");
			currenConfigIndex = index;
		}
	} catch(err) {
		alert(err);
	}
};

updateConfigInfoDialog = function(data)
{	
	
		for (var key in data) {
		  if (data.hasOwnProperty(key)) {
			  try {
//			  console.log(key + " -> " + data[key]);
				  
				  if (data[key] == true) {
					  $("#" + key).prop("checked", data[key]);
				  } else {
					  $("#" + key).val(data[key]);
				  }
				  
			  } catch(err){}
		  }
		}
//		var isExclusive = (getValueIfDefined(data.exclusive) == "")? false : getValueIfDefined(data.exclusive);
//		$('#exclusive').prop('checked', isExclusive);	
	adjustFileType();
       
	$('#configInfoModal').modal('show');
};
//-----------------------------------------------------------------------
/** MODAL **/
//-----------------------------------------------------------------------
updateDefaults = function() {
	try {
		if (fieldStructs != null) {
			for(var i=0; i<fieldStructs.length; i++) {
				if (fieldStructs[i].defaultValue.length > 0) {
					$("#" + fieldStructs[i].field).val(fieldStructs[i].defaultValue);
				}
			}
		}
		resetFileType();
	} catch(err) {
		alert(err);
	}
};

resetModal = function(title, showUpdateButton, buttonText) {
	hideErrorMessage("serverhost");
	hideErrorMessage("peerhost");
	
//	$('#configInfomodalForm').trigger("reset");
	$("#serverInfoModalTitle").html(title);
	$("#serverInfoModalError").addClass("hide");
	
	if (showUpdateButton) {
		$("#saveConfig").removeClass("hide");
		$("#saveConfig").html(buttonText);
	} else {
		$("#saveConfig").addClass("hide");
	}
};

resetConfigModal = function(title, showUpdateButton, buttonText) {
	hideErrorMessage("fileType");
	hideErrorMessage("serverRole");
	hideErrorMessage("filenameFilter");
	hideErrorMessage("fileProcessorType");
	hideErrorMessage("inputDirectory");
	hideErrorMessage("outputDirectory");
	hideErrorMessage("copyCommand");
	hideErrorMessage("processStartTimeOfDay");
	hideErrorMessage("processEndTimeOfDay");

	$('#configInfomodalForm').trigger("reset");
	$("#configInfomodalTitle").html(title);
	$("#configInfomodalError").addClass("hide");
	
	
	if (showUpdateButton) {
		$("#saveConfig").removeClass("hide");
		$("#saveConfig").html(buttonText);
		$("#configInfomodalForm :input").prop('disabled', false);
	} else {
		$("#saveConfig").addClass("hide");
		$("#configInfomodalForm :input").prop('disabled', true);
	}
	$("#cancelUpdate").prop('disabled', false);
};

closeServerInfoModal = function()
{};

//OK For Modal!
validateAndSendConfig = function()
{
	try 
	{
		var error = validateLengthMinMax("serverRole", 5, 100, "Between 5 and 100 characters");
		error = validateLengthMinMax("filenameFilter", 1, 255, "Between 1 and 255 characters") || error;
//		error = validateRegexExpression("filenameFilter", 255) || error;
		error = validateLengthMinMax("inputDirectory", 1, 255, "Between 1 and 255 characters") || error;
		error = validateLengthMinMax("outputDirectory", 0, 255, "Between 0 and 255 characters") || error;
		error = validateLengthMinMax("copyCommand", 1, 255, "Between 1 and 255 characters") || error;
		error = validateTimeField("processStartTimeOfDay") || error;
		error = validateTimeField("processEndTimeOfDay") || error;
			
		if (!error) 
		{
			var requestData = $('#configInfomodalForm').serialize();
			var action = (currenConfigIndex >=0)? "upd" : "add";
			$("#updateWaiting").removeClass("hide");
			var daturl = "act=" + action + "&" + requestData;
			if (currenConfigIndex >=0 ) {
				daturl += "&index=" + currenConfigIndex;
			}
			
			//Post info to server
			var status = $.ajax({
				type: "POST", 
				url: pageurl, 
				data: daturl,
				dataType: "json"
			}).done(function(data) {
				$("#updateWaiting").addClass("hide");
				resetSessionTimeout();

				if (data.status == "fail")
				{
					$("#configInfomodalError").removeClass("hide");
					$("#configInfomodalError").html(data.message);
				}
				else
				{
					dataUpdated();
					$('#configInfoModal').modal('hide');
					refreshConfigContent();
				}
			});
		}
		
	} catch(err){
		alert(err);
	}
};

getInfoRequest = function(index, callback) {
	var data2Send = "act=data&index="+index;
	
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

resetFileType = function() {
	$('select[name=fileType]').val(1);
	$('.selectpicker').selectpicker('refresh');
};

adjustFileType = function() {
	try {
		//$('select[name=fileType]').val(1);
		$('.selectpicker').selectpicker('refresh');
		
//		//Get the text using the value of select
//		var text = $("select[name=fileType] option[value='1']").text();
//		console.log(text);
//		//We need to show the text inside the span that the plugin show
//		$('.bootstrap-select .filter-option').text(text);
//		//Check the selected attribute for the real select
//		$('select[name=fileType').val(1);
	} catch(err) {
		alert(err);
	}

};