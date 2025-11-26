/**
 * Object Reference Guide:
 * Utils > utilities.js
 */

var INFO_CLASS = "alert-info";
var SUCCESS_CLASS = "alert-success";
var FAIL_CLASS = "alert-danger";
var FATE_OUT_TME = 3500;
var currentSid;
var currentVid;
var immidiateAddQuota = true;
var lastBeneficiary = null;
var retrieveBalancesAutomatically = false;

$(document).ready(function() {
	try {

		Utils.refreshPlaceholders();
		Utils.configureToolTip($("#retrieve"), 'top');
		Utils.configureToolTip($("#searchHistory"), 'top');
		
		CrmHome.registerButtonEvents();
		
		Utils.refreshSelectPickers();
		Utils.numericFieldChecker();
	} catch (err) {
		Utils.logError("load error: " + err);
	}
});


var updateServiceListJS = function() {
	try {
		Utils.configureToolTip($(".viewSubscription"), 'top');
		Utils.configureToolTip($(".unsubscribeMsisdn"), 'top');
		Utils.configureToolTip($(".subscribeMsisdn"), 'top');
	} catch (err) {
	}
};


// History Retrieval
var createHistoryTableHtml = function() {
	var html = [];
	html[html.length] = "<table id='subhistorytable' class='display' width='100%' cellspacing='0'>";
	html[html.length] = "<thead>";
	html[html.length] = "<tr>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryDateHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryTimeHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryMSISDNAHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryMSISDNBHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryChannelHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryChangeHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistoryProcessHeading;
	html[html.length] = "</th>";
	html[html.length] = "<th>";
	html[html.length] = cctext.subHistorySuccessHeading;
	html[html.length] = "</th>";
	html[html.length] = "</tr>";
	html[html.length] = "</thead>";
	html[html.length] = "</table>";
	return html.join("");
};



// Service Subscription
var enableSubscription = function() {
	$("#subscribe").prop('disabled', false);
};

var performReplace = function(message, replaceCode, update) {
	try {
		return message.replace(new RegExp(replaceCode, 'g'), update);
	} catch (err) {
		Utils.logError(err);
	}
};

// Generic Subscribe
var subscribeUserToService = function(msisdn, serviceId, variantId, serviceName) {
	try {
		var dialogTitleText = Utils.stringFormat(cctext.confirmSubscribeTitle, [serviceName]);
		
		var dialogTitle = "<span id='subscribeTitle'>"
				+ dialogTitleText + "</span>";
		var actionLabel = cctext.confirmSubscribeAction;
		var cancelLabel = cctext.confirmSubscribeCancel;
		var msisdn = Utils.removeSpaces(msisdn);
		var encodedMsisdn = Utils.enclodeField(msisdn);
		var postData = "act=subscribeTest&sid=" + Utils.enclodeField(serviceId)
				+ "&vid=" + Utils.enclodeField(variantId) + "&msisdn="
				+ encodedMsisdn;
		var servlet = "/" + serviceId.toLowerCase();
		
		var dialog = BootstrapDialog.showOperationsDialog(serviceId, BootstrapDialog.TYPE_SUCCESS,
				dialogTitle, actionLabel, cancelLabel, postData, function(result) {
					if (result) {
						var actPostData = "act=subscribe&sid="
								+ Utils.enclodeField(serviceId) + "&vid="
								+ Utils.enclodeField(variantId) + "&msisdn="
								+ encodedMsisdn;
						sendAjax(servlet, actPostData,
								function(data) {
									dialog.close(); // Close BootstrapDialog.showOperationsDialog
									$("#subscribeAlert").removeClass("hide");
									$("#subscribeAlertDialog").removeClass(
											"alert-success");
									$("#subscribeAlertDialog").removeClass(
											"alert-danger");
									$("#alertContent").html(data.message);
									if (data.status == "pass") {
										// Refresh cccontent
										CrmHome.refreshAccountService(msisdn, serviceId);
										// gui_alerts
										createAlert("gui_alerts", "success",
												data.message);
									} else {
										// Display Fail message + reason
										createAlert("gui_alerts", "error",
												data.message);
										// $("#subscribeAlertDialog").addClass("alert-danger");
									}
								});
					}
				});

	} catch (err) {
		console.error("subscribeUserToService: " + err);
	}
};

// View Service Details
var viewService = function(serviceId, variantId) {
	var msisdn = Utils.removeSpaces($("#msisdn").val())
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var postData = "act=viewService&msisdn=" + encodedMsisdn + "&sid="
			+ serviceId + "&vid=" + variantId;
	sendContentAjax("/" + serviceId.toLowerCase(), postData, function(content) {
		$("#cccontent").html(content);
		Utils.numericFieldChecker();
	});
};

var consumerEvents = function(sid, vid) {
	currentSid = sid;
	currentVid = vid;

	$("#newBenMsisdn").on("keyup", function(event) {
		if ($("#newBenMsisdn").val().length > 0) {
			$("#btnAddBen").removeClass("disabled");
		} else {
			$("#btnAddBen").addClass("disabled");
		}
		var keycode = (event.keyCode ? event.keyCode : event.which);
		if (keycode == '13') {
			addBenificiary($("#msisdn").val(), currentSid, currentVid);
		}
	});
	
	$("#newBenMsisdnName").on("keyup", function(event) {
		var keycode = (event.keyCode ? event.keyCode : event.which);
		if (keycode == '13') {
			if ($("#newBenMsisdn").val().length > 0) {
				addBenificiary($("#msisdn").val(), currentSid, currentVid);
			}
		}
	});

	if ($("#balanceEnquiryBtn").length) {
		$("#balanceEnquiryBtn").on("click", function() {
			try {
				CrmHome.balanceEnquiry(sid);
			} catch (err) {
				console.error(err);
			}
		});
	} else {
		if ($("#balanceEnquiryRefreshBtn").length) {
			$("#balanceEnquiryRefreshBtn").on("click", function() {
				try {
					CrmHome.balanceEnquiry(sid);
				} catch (err) {
					console.error(err);
				}
			});
		}
		retrieveBalancesAutomatically = true;
		CrmHome.balanceEnquiry(sid);
	}

	Utils.configureToolTip($("#btnAddBen"), 'top');
};

var createBalanceRetrivalHtml = function(data) {
	var html = [];
	try {

		if (data.bal.length > 0) {
			html[html.length] = '<center>';
			html[html.length] = "	<div style='width: 450px'>";

			html[html.length] = '<table class="table infotable table-hover table-bordered table-striped">';
			html[html.length] = '<thead><tr>';
			//
			html[html.length] = '<th>'+cctext.balTableHeadingServices+'</th>';
			html[html.length] = '<th>'+cctext.balTableHeadingBalance+'</th>';
			html[html.length] = '<th>'+cctext.balTableHeadingExpiry+'</th>';
			html[html.length] = '</tr></thead>';
			html[html.length] = '<tbody>';

			if (typeof data.bal !== 'undefined' && data.bal != null) {
				for (var i = 0; i < data.bal.length; i++) {
					html[html.length] = '<tr>';
					html[html.length] = '<td>';
					html[html.length] = data.bal[i].name;
					html[html.length] = '</td>';

					html[html.length] = '<td>';
					html[html.length] = data.bal[i].value;
					html[html.length] = '&nbsp;';
					html[html.length] = data.bal[i].unit;
					html[html.length] = '</td>';

					html[html.length] = '<td>';
					html[html.length] = data.bal[i].expiry;
					html[html.length] = '</td>';

					html[html.length] = '</tr>';
				}
			}

			html[html.length] = '</tbody>';

			html[html.length] = '</table>';

			html[html.length] = "</div>";
			html[html.length] = '</center>';
		} else {
			html[html.length] = "<div class='row' style='padding-left:15px; padding-top:10px;'>";
			html[html.length] = "	<div class='col-xs-12 col-lg-12 col-sm-12 col-md-12'>";
			html[html.length] = data.title;
			html[html.length] = "	</div>";
			html[html.length] = "</div>";
		}
	} catch (err) {
		console.error(err);
	}
	return html.join("");
};




// Add new Beneficiary
var addBenificiary = function(msisdn, sid, vid, addTypeName) {
	var ben = Utils.removeSpaces($("#newBenMsisdn").val());
	var benName = $("#newBenMsisdnName").val();
	var dialogTitle = Utils.stringFormat( cctext.confirmAddConsumerTitle , ["Beneficiary"])
	var actionLabel = cctext.confirmAddConsumerAction;
	var cancelLabel = cctext.confirmAddConsumerCancel;
	var msisdn = Utils.removeSpaces(msisdn);
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var encodedBenMsisdn = Utils.enclodeField(ben);
	var encodedBenName = Utils.enclodeField(benName);
	
	var postData = "act=addBeneficiaryTest&msisdn=" + encodedMsisdn + "&sid="
			+ sid + "&vid=" + vid + "&ben=" + encodedBenMsisdn + "&benName=" + encodedBenName;

	var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_SUCCESS,
			dialogTitle, actionLabel, cancelLabel, postData, function(result) {
				if (result) {
					// Add Benificiary
					var actPostData = "act=addBeneficiary&msisdn="
							+ encodedMsisdn + "&sid=" + sid + "&vid=" + vid
							+ "&ben=" + encodedBenMsisdn + "&benName=" + encodedBenName;
					sendAjax("/" + sid.toLowerCase(), actPostData, function(data) {
						dialog.close();
						if (data.status == "pass") {
							$("#newBenMsisdn").val("");
							$("#newBenMsisdnName").val("");
						}
						if (data.status == "pass") {
							if (immidiateAddQuota) {
								lastBeneficiary = ben;
							}
							createAlert("gui_alerts", "success", data.message);
							updateMembers(data, msisdn, sid, vid,
									immidiateAddQuota);
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
						}
					});
				}
			});
};

// UNREGISTERING / DELETING MODAL
var delPost = "";

var unsubscribe = function(msisdn, service, sid, vid) {

	var dialogTitle = cctext.confirmUnsubscribeTitle;
	var actionLabel = cctext.confirmUnsubscribeAction;
	var cancelLabel = cctext.confirmSubscribeCancel;
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var postData = "act=unsubscribeTest&msisdn=" + encodedMsisdn + "&sid="
			+ sid + "&vid=" + vid;

	var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_DANGER,
			dialogTitle, actionLabel, cancelLabel, postData, function(result) {
				if (result) {
					// Remove Benificiary
					var actPostData = "act=unsubscribe&msisdn=" + encodedMsisdn
							+ "&sid=" + sid + "&vid=" + vid;
					sendAjax("/" + sid.toLowerCase(), actPostData, function(data) {
						dialog.close();
						if (data.status == "pass") {
							// Refresh cccontent
							CrmHome.refreshAccountService(msisdn, sid);
							createAlert("gui_alerts", "success", data.message);
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
							// $("#subscribeAlertDialog").addClass("alert-danger");
						}
					});
				}
			}
	);

};

var removeOwner = function(benMsisdn, sid, vid, owner) {
	var dialogTitle = cctext.confirmRemoveConsumerTitle;
	var actionLabel = cctext.confirmRemoveConsumerAction;
	var cancelLabel = cctext.confirmRemoveConsumerCancel;
	var benMsisdn = Utils.removeSpaces(benMsisdn);
	var encodedMsisdn = Utils.enclodeField(benMsisdn);
	var postData = "act=removeOwnerTest&owner=" + owner + "&sid=" + sid
			+ "&vid=" + vid + "&msisdn=" + benMsisdn;

	var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_DANGER,
			dialogTitle, actionLabel, cancelLabel, postData, function(result) {
				if (result) {
					// Remove Benificiary
					var actPostData = "act=removeOwner&owner=" + owner
							+ "&sid=" + sid + "&vid=" + vid + "&msisdn="
							+ benMsisdn;
					sendAjax("/" + sid.toLowerCase(), actPostData, function(data) {
						dialog.close();
						if (data.status == "pass") {
							// Refresh cccontent
							CrmHome.retrieveAvailableServices(true);
							createAlert("gui_alerts", "success", data.message);
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
						}
					});
				}
			});
};

var updateMemberListEvents = function() {
	try {
		$(".removeconsumer").attr("title", cctext.delconsumerbtn);
		$(".viewconsumer").attr("title", cctext.viewconsumerquotas);
		Utils.configureToolTip($(".removeconsumer"), 'top');
		Utils.configureToolTip($(".viewconsumer"), 'top');
	} catch (err) {
		Utils.logError("updateMemberListEvents: " + err);
	}
};

var removeMember = function(msisdn, sid, vid, ben) {
	var dialogTitle = cctext.confirmRemoveConsumerTitle;
	var actionLabel = cctext.confirmRemoveConsumerAction;
	var cancelLabel = cctext.confirmRemoveConsumerCancel;
	var msisdn = Utils.removeSpaces(msisdn);
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var postData = "act=removeBeneficiaryTest&msisdn=" + encodedMsisdn
			+ "&sid=" + sid + "&vid=" + vid + "&ben=" + ben;

	var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_DANGER,
			dialogTitle, actionLabel, cancelLabel, postData, function(result) {
				if (result) {
					// Remove Benificiary
					var actPostData = "act=removeBeneficiary&msisdn="
							+ encodedMsisdn + "&sid=" + sid + "&vid=" + vid
							+ "&ben=" + ben;
					sendAjax("/" + sid.toLowerCase(), actPostData, function(data) {
						dialog.close();
						if (data.status == "pass") {
							// Refresh cccontent
							updateMembers(data, msisdn, sid, vid);
							createAlert("gui_alerts", "success", data.message);
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
						}
					});
				}
			});

};

// retieveMemberServiceCall > retieveQuotaInfo
var showMemberInfo = function(msisdn, sid, vid, ben, callAddQuota, retieveMemberServiceCall) {
	var msisdn = Utils.removeSpaces($("#msisdn").val())
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var postData = "act=viewQuotas&msisdn=" + encodedMsisdn + "&sid=" + sid
			+ "&vid=" + vid + "&ben=" + ben;
	sendContentAjax("/" + sid.toLowerCase(), postData, function(content) {
		$("#benbody" + ben).html(content);


		$("#benbody" + ben).show('afterShow', function() {
			//retieveMemberServiceCall
			retieveQuotaInfo(msisdn, sid, vid, ben, callAddQuota); // Refresh content for quota dialog
		});
	});
};

var expandMember = function(msisdn, sid, vid, ben, callAddQuota) {

	try {
		if ($("#benbtn" + ben).hasClass("glyphicon-eye-open")) {
			$("#benbtn" + ben).removeClass("glyphicon-eye-open");
			$("#benbtn" + ben).addClass("glyphicon-eye-close");

			// Filler
			$("#benbody" + ben).html(
					"<p><img src='/img/load.gif' /> loading [" + ben
							+ "] ...</p>");
			$("#benbody" + ben).removeClass("hide");
			showMemberInfo(msisdn, sid, vid, ben, callAddQuota);

		} else {
			$("#benbtn" + ben).removeClass("glyphicon-eye-close");
			$("#benbtn" + ben).addClass("glyphicon-eye-open");
			$("#benbody" + ben).addClass("hide");
		}
	} catch (err) {
		Utils.logError("expandMember: " + err);
	}

};

var addQuota = function(msisdn, sid, vid, ben) {
	BootstrapDialog.addQuota(msisdn, sid, vid, ben);
};

var removeQuota = function(msisdn, sid, vid, ben, qid, perspective) {
	var dialogTitle = cctext.confirmRemoveQuotaTitle;
	var actionLabel = cctext.confirmRemoveQuotaAction;
	var cancelLabel = cctext.confirmRemoveQuotaCancel;
	var msisdn = Utils.removeSpaces(msisdn);
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var encodedQid = Utils.enclodeField(qid);
	var encodedBen = Utils.enclodeField(ben);
	var postData = "act=removeQuotaTest&msisdn=" + encodedMsisdn + "&sid="
			+ sid + "&vid=" + vid + "&ben=" + encodedBen + "&qid=" + encodedQid;

	var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_DANGER,
			dialogTitle, actionLabel, cancelLabel, postData, function(result) {
				if (result) {
					// Remove Benificiary
					var postData = "act=removeQuota&msisdn=" + encodedMsisdn
							+ "&sid=" + sid + "&vid=" + vid + "&ben=" + ben
							+ "&qid=" + encodedQid;
					sendAjax("/" + sid.toLowerCase(), postData, function(data) {
						// Show message and update
						dialog.close();
						if (data.status == "pass") {
							createAlert("gui_alerts", "success", data.message);
							if (typeof perspective !== 'undefined'
									&& perspective == 'prov') {
								CrmHome.refreshAccountService(msisdn, sid);
							} else {
								// Refresh cccontent
								showMemberInfo(msisdn, sid, vid, ben);
								CrmHome.balanceEnquiry(sid);
							}
							
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
						}
					});
				}
			});
};

// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

var updateQuotaQuantity = function(msisdn, sid, vid, ben, qid, quantity, units) {
	var dialogTitle = "<span id='updateQuotaTitle'>"
			+ cctext.updateQuotaQuantityTitle + "</span>";
	var actionLabel = cctext.updateQuotaQuantityAction;
	var cancelLabel = cctext.updateQuotaQuantityCancel;
	var msisdn = Utils.removeSpaces(msisdn);
	var encodedMsisdn = Utils.enclodeField(msisdn);

	var html = [];
	html[html.length] = "<form id='updatedlgcontent' autocomplete='off' onsubmit='return false;'>";
	html[html.length] = "<div class='row'>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label id='qty' class='control-label' for='qty'>";
	html[html.length] = cctext.updateQuotaQuantityLabel;
	html[html.length] = " (" + units + ")</label>";

	html[html.length] = "		<input type='text' id='quantity' name='quantity' class='numericfields form-control' value='"
			+ quantity + "' placeholder='Quantity' />";
	html[html.length] = "	</div>";
	html[html.length] = "</div>";
	html[html.length] = "</form>";

	html[html.length] = "<div id='busy_updating_config' class='row hide'>";
	html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md-12'>";
	html[html.length] = "		<div class='well well-lg'>";
	html[html.length] = "			<img alt='Waiting...' src='/img/cell_wait.gif'>";
	html[html.length] = cctext.busy;
	html[html.length] = " 		</div>";
	html[html.length] = "	</div>";
	html[html.length] = "</div>";

	html[html.length] = "<div id='quota_add_failed' class='row hide'>";
	html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md12'>";
	html[html.length] = "		<div id='quota_failed_reason' class='alert alert-danger'>";
	html[html.length] = cctext.error;
	html[html.length] = " 		</div>";
	html[html.length] = "	</div>";
	html[html.length] = "</div>";
	html[html.length] = "<script>Utils.refreshPlaceholders();</script>";

	new BootstrapDialog({
		title : dialogTitle,
		message : html.join(""),
		type : BootstrapDialog.TYPE_SUCCESS,
		closable : true,
		// onshown: function(dialogRef){
		//        	
		// },
		buttons : [
				{
					id : 'updqtycancelbtn',
					label : cancelLabel,
					action : function(dialog) {
						typeof dialog.getData('callback') === 'function'
								&& dialog.getData('callback')(false);
						dialog.close();
					}
				},
				{
					id : 'updqtybtn',
					hotkey : 13,
					label : actionLabel,
					cssClass : 'btn-primary',
					icon : 'glyphicon glyphicon-send',
					action : function(dialog) {
						dialog.enableButtons(false);
						dialog.setClosable(false);
						$("#busy_updating_config").removeClass("hide");

						var formData = $('#updatedlgcontent').serialize();
						getUpdateQuotaInfo(msisdn, sid, vid, ben, dialog, qid,
								quantity, formData);
					}
				} ]
	}).open();
};

var getUpdateQuotaInfo = function(msisdn, sid, vid, ben, dialog, qid, oldQuantity,
		formData) {
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var encodeQid = Utils.enclodeField(qid);
	var postData = "act=updateQuantityTest&sid=" + escape(sid) + "&vid="
			+ escape(vid) + "&ben=" + ben + "&msisdn=" + encodedMsisdn + "&"
			+ formData + "&oldqty=" + oldQuantity + "&qid=" + encodeQid;
	sendAjax("/" + sid.toLowerCase(), postData, function(data) {
		$("#busy_updating_config").addClass("hide");
		dialog.enableButtons(true);
		dialog.setClosable(true);

		// Pass a test through to obtain cost of update.
		sendAjax("/" + sid.toLowerCase(), postData, function(data) {
			$("#busy_updating_config").addClass("hide");
			dialog.enableButtons(true);
			dialog.setClosable(true);

			if (data.status == 'fail') {
				$("#quota_failed_reason").html(data.message);
				$("#quota_add_failed").removeClass("hide");
			} else {
				// Close this dialog and open next (To confirm action)
				confirmQuotaUpdate(msisdn, sid, vid, ben, qid, oldQuantity, formData, data);
				dialog.close();
			}
		});

	});
};

var confirmQuotaUpdate = function(msisdn, sid, vid, ben, qid, oldQuantity,
		formData, data) {

	var dialogTitle = cctext.confirmUpdateQuotaQtyTitle;
	var actionButton = cctext.confirmUpdateQuotaQtyAction;
	var cancelButton = cctext.confirmUpdateQuotaQtyCancel;
	var encodedMsisdn = Utils.enclodeField(msisdn);

	BootstrapDialog.confirmDialog(dialogTitle, actionButton, cancelButton,
			data.message, BootstrapDialog.TYPE_SUCCESS, function(result) {
				if (result) {
					var encodedMsisdn = Utils.enclodeField(msisdn);
					var encodeQid = Utils.enclodeField(qid);
					var postData = "act=updateQuantity&sid=" + escape(sid)
							+ "&vid=" + escape(vid) + "&ben=" + ben
							+ "&msisdn=" + encodedMsisdn + "&" + formData
							+ "&oldqty=" + oldQuantity + "&qid=" + encodeQid;
					sendAjax("/" + sid.toLowerCase(), postData, function(data) {
						if (data.status == "pass") {
							createAlert("gui_alerts", "success", data.message)
							showMemberInfo(msisdn, sid, vid, ben);
							if (retrieveBalancesAutomatically) {
								CrmHome.balanceEnquiry(sid);
							}
						} else {
							// Display Fail message + reason
							createAlert("gui_alerts", "error", data.message);
						}
					});
				}
			});
};

var updateQuotaEvents = function() {
	Utils.configureToolTip($("#addquota"), 'top');
	Utils.configureToolTip($(".removeQuoata"), 'top');
	Utils.configureToolTip($(".updateQuoata"), 'top');
}

var quotaInfo = null;
var retieveQuotaInfo = function(msisdn, sid, vid, ben, callAddQuota) {
//	if (quotaInfo == null
//			|| (typeof callAddQuota !== 'undefined' && callAddQuota != null && callAddQuota)) {
		var encodedMsisdn = Utils.enclodeField(msisdn);
		var postData = "act=retrieveQuaotaInfo&msisdn=" + encodedMsisdn
				+ "&sid=" + sid + "&vid=" + vid + "&ben=" + ben;
		sendAjax("/" + sid.toLowerCase(), postData, function(data) {
			quotaInfo = data;
			
			if (typeof callAddQuota !== 'undefined' && callAddQuota != null
					&& callAddQuota) {
				if (sid.toLowerCase() == "crshr" || sid.toLowerCase() == "gsa")
					addQuota(msisdn, sid, vid, ben);
				else
					Act.addTransfer(msisdn, sid, vid, ben);
			}
		});
//	}
};

// Helper methods
var updateMembers = function(guiResponse, msisdn, sid, vid, callAddQuota) {
	try {
		// Display Success/Fail
		if (guiResponse.status == "pass") {
			// Update memebers list
			var encodedMsisdn = Utils.enclodeField(msisdn);
			var postData = "act=viewBeneficiaries&msisdn=" + encodedMsisdn
					+ "&sid=" + sid + "&vid=" + vid;
			sendContentAjax("/" + sid.toLowerCase(), postData, function(content) {
				var $html = $(content);
				$("#membersinfo").html($html);
				Utils.numericFieldChecker();
				if (typeof callAddQuota !== 'undefined' && callAddQuota != null
						&& callAddQuota) {
					// Need to wait for html to be loaded into the DOM
					// updateMembersHtmlInDOM(msisdn, sid, vid);
					$("#membersinfo").show('afterShow', function() {
						updateMembersHtmlInDOM(msisdn, sid, vid);
					});
				}
			});
		}
	} catch (err) {
		Utils.logError("updateMembers: ", err);
	}
};

var updateMembersHtmlInDOM = function(msisdn, sid, vid) {
	try {
		$("#membersinfo .viewconsumer").each(
				function() {
					var $this = $(this);
					var id = $this.attr("id");
					var benMsisdn = id.substr(3);
					var compareWith = lastBeneficiary
							.substr(lastBeneficiary.length - benMsisdn.length);
					if (benMsisdn == compareWith) {
						expandMember(msisdn, sid, vid, benMsisdn,
								immidiateAddQuota);
					}
				});
	} catch (err) {
		Utils.logError("updateMembersHtmlInDOM: " + err);
	}
};

BootstrapDialog.confirmDialog = function(titleMessage, actionLabel,
		cancelLabel, message, messageType, callback) {
	new BootstrapDialog(
			{
				title : titleMessage,
				message : message,
				type : messageType,
				closable : true,
				data : {
					'callback' : callback
				},
				buttons : [
						{
							id : 'cancelbtn',
							label : cancelLabel,
							action : function(dialog) {
								typeof dialog.getData('callback') === 'function'
										&& dialog.getData('callback')(false);
								dialog.close();
							}
						},
						{
							id : 'confirmbtn',
							label : actionLabel,
							hotkey : 13,
							cssClass : (messageType == BootstrapDialog.TYPE_DANGER) ? 'btn-danger'
									: 'btn-success',
							action : function(dialog) {
								typeof dialog.getData('callback') === 'function'
										&& dialog.getData('callback')(true);
								dialog.close();
							}
						} ]
			}).open();
};

var updateOptionsArray = function(filter, fieldChanged, curService, curDestination,
		curTow, curTod, fieldRequired) {

	var arr = [];
	for (var i = 0; i < filter.length; i++) {
		var value = null;

		if (filter[i].service == curService) {
			if (fieldRequired == "destination") {
				value = filter[i].dest;
			} else if ((fieldRequired == "dow")
					&& (filter[i].dest == curDestination)) {
				value = filter[i].dow;
			} else if ((fieldRequired == "tod")
					&& (filter[i].dest == curDestination)
					&& (filter[i].dow == curTow)) {
				value = filter[i].tod;
			}
		}

		if (value != null) {
			var add = true;
			for (var j = 0; j < arr.length; j++) {
				if (arr[j] == value) {
					add = false;
					break;
				}
			}
			if (add)
				arr[arr.length] = value;
		}
	}
	return arr;
};

var extractOptionsArray = function(data, filter, service, dest, dow, tod) {
	var arr = [];
	for (var i = 0; i < data.length; i++) {
		var update = true;
		try {
			if (typeof filter !== 'undefined') {
				if (service == null)
					break;
				update = false;

				for (var j = 0; j < filter.length; j++) {
					if ((dest == null && ((filter[j].service == service)
							&& (filter[j].dest == data[i])
							&& (filter[j].dow == dow) && (filter[j].tod == tod)))
							|| (dow == null && ((filter[j].service == service)
									&& (filter[j].dest == dest)
									&& (filter[j].dow == data[i]) && (filter[j].tod == tod)))
							|| (tod == null && ((filter[j].service == service)
									&& (filter[j].dest == dest)
									&& (filter[j].dow == dow) && (filter[j].tod == data[i])))) {
						update = true;
						break;
					}
				}
			}
			if (update) {
				for (var k = 0; k < (arr.length - 1); k++) {
					if (arr[k] == data[i])
						update = false;
				}
				if (update)
					arr[arr.length] = data[i];
			}
		} catch (err) {
			Utils.logError("extractOptionsArray: " + err);
		}
	}
	return arr;
};

var extractOption = function(data, selected) {

	var html = [];

	if (data != null) {
		for (var i = 0; i < data.length; i++) {

			html[html.length] = "<option value='";
			html[html.length] = data[i];
			html[html.length] = "'";
			if ((typeof selected !== 'undefined') && (selected == data[i])) {
				html[html.length] = " selected='selected' ";
			}
			html[html.length] = ">";
			html[html.length] = data[i];
			html[html.length] = "</option>";
		}
	}

	return html.join("");
};

var checkIfValue = function(arr, option) {
	for (var i = 0; i < arr.length; i++) {
		if (arr[i] == option) {
			return true;
		}
	}
	return false;
};

var extractUnit = function(service) {
	var result = "";
	for (var i = 0; i < quotaInfo.filters.length; i++) {
		if (quotaInfo.filters[i].service == service) {
			result = quotaInfo.filters[i].units;
			break;
		}
	}
	return result;
};

var quotaOptionUpdate = function(idupdate) {
	try {
		var dest = null;
		var dow = null;
		var tod = null;

		var currentService = $("#service option:selected").val();
		var currentDest = $("#destination option:selected").val();
		var currentDow = $("#dow option:selected").val();
		var currentTod = $("#tod option:selected").val();
		var newValue = (idupdate == "service") ? currentService
				: (idupdate == "destination") ? currentDest
						: (idupdate == "dow") ? currentDow : currentTod;

		var destArray = null;
		var dowArray = null;
		var todArray = null;

		if (idupdate == "service") {
			destArray = updateOptionsArray(quotaInfo.filters, idupdate,
					currentService, currentDest, currentDow, currentTod,
					"destination");
			var quatoUnit = extractUnit(currentService);
			if (currentService.toLowerCase()=="airtime") 
				$("#quantity").addClass("currency");
			else {
				$("#quantity").removeClass("currency");
				if (/[^+0-9]+/g.test($("#quantity").val())) {
					// Filter non-digits from input value.
					$("#quantity").val($("#quantity").val().replace(/[^+0-9]+/g, ''));
				}
			}
			$("#quatoUnit").html(cctext.quotaQuantityLabel + " (" + quatoUnit + ")");
		}

		// Update arrays
		if (destArray != null) {
			if (!checkIfValue(destArray, currentDest)) {
				currentDest = destArray[0];
			}
			var html = extractOption(destArray, currentDest);
			$("#destination").html(html);
		}

		dowArray = updateOptionsArray(quotaInfo.filters, idupdate,
				currentService, currentDest, currentDow, currentTod, "dow");
		if (dowArray != null) {
			if (!checkIfValue(dowArray, currentDow)) {
				currentDow = dowArray[0];
			}
			var html = extractOption(dowArray, currentDow);
			$("#dow").html(html);
		}

		todArray = updateOptionsArray(quotaInfo.filters, idupdate,
				currentService, currentDest, currentDow, currentTod, "tod");
		if (todArray != null) {
			if (!checkIfValue(todArray, currentTod)) {
				currentTod = todArray[0];
			}
			var html = extractOption(todArray, currentTod);
			$("#tod").html(html);
		}

		$('.selectpicker').selectpicker('refresh');
	} catch (err) {
		Utils.logError("quotaOptionUpdate: " + err);
	}
};

var addQuotaDialog = null;

BootstrapDialog.addQuota = function(msisdn, sid, vid, ben) {
	var html = [];

	var qidestinations = extractOptionsArray(quotaInfo.destinations,
			quotaInfo.filters, quotaInfo.types[0], null,
			quotaInfo.daysOfWeek[0], quotaInfo.timesOfDay[0]);
	var qidow = extractOptionsArray(quotaInfo.daysOfWeek, quotaInfo.filters,
			quotaInfo.types[0], qidestinations[0], null,
			quotaInfo.timesOfDay[0]);
	var qitod = extractOptionsArray(quotaInfo.timesOfDay, quotaInfo.filters,
			quotaInfo.types[0], qidestinations[0], qidow[0], null);

	var dialogTitle = cctext.addQuotaTitle;
	var actionButton = cctext.addQuotaAction;
	var cancelButton = cctext.addQuotaCancel;

	html[html.length] = "<form onsubmit='return false;' autocomplete='off' id='serviceForm' name='serviceForm'>";
	html[html.length] = "<div class='row'>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label class='control-label' for='service'>";
	html[html.length] = cctext.quotaServiceLabel;
	html[html.length] = "</label>";
	html[html.length] = "		<select id='service' name='service' class='selectpicker' onchange='quotaOptionUpdate(this.id)'>";
	html[html.length] = extractOption(quotaInfo.types);
	html[html.length] = "		</select>";
	html[html.length] = "	</div>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label class='control-label' for='destination'>";
	html[html.length] = cctext.quotaDestinationLabel;
	html[html.length] = "</label>";
	html[html.length] = "		<select id='destination' name='destination' class='selectpicker' onchange='quotaOptionUpdate(this.id)'>";
	html[html.length] = extractOption(qidestinations);
	html[html.length] = "		</select>";
	html[html.length] = "	</div>";
	html[html.length] = "</div></br>";

	html[html.length] = "<div class='row'>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label class='control-label' for='dow'>";
	html[html.length] = cctext.quotaDaysofWeekLabel;
	html[html.length] = "</label>";
	html[html.length] = "		<select id='dow' name='dow' class='selectpicker' onchange='quotaOptionUpdate(this.id)'>";
	html[html.length] = extractOption(qidow);
	html[html.length] = "		</select>";
	html[html.length] = "	</div>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label class='control-label' for='tod'>";
	html[html.length] = cctext.quotaTimeofDayLabel;
	html[html.length] = "</label>";
	html[html.length] = "		<select id='tod' name='tod' class='selectpicker' onchange='quotaOptionUpdate(this.id)'>";
	html[html.length] = extractOption(qitod);
	html[html.length] = "		</select>";
	html[html.length] = "	</div>";
	html[html.length] = "</div></br>";

	var units = extractUnit(quotaInfo.types[0]);
	html[html.length] = "<div class='row'>";
	html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6'>";
	html[html.length] = "		<label id='quatoUnit' class='control-label' for='tod'>";
	html[html.length] = cctext.quotaQuantityLabel;
	html[html.length] = "(" + units + ")</label>";

	html[html.length] = "		<input type='text' id='quantity' name='quantity' class='form-control' placeholder='Quantity' />";
	html[html.length] = "	</div>";
	html[html.length] = "</div></br>";

	html[html.length] = "</form>";

	html[html.length] = "<div id='busy_updating_config' class='row hide'>";
	html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md-12'>";
	html[html.length] = "		<div class='well well-lg'>";
	html[html.length] = "			<img alt='Waiting...' src='/img/cell_wait.gif'>";
	html[html.length] = cctext.busy;
	html[html.length] = " 		</div>";
	html[html.length] = "	</div>";
	html[html.length] = "</div>";

	html[html.length] = "<div id='quota_add_failed' class='row hide'>";
	html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md12'>";
	html[html.length] = "		<div id='quota_failed_reason' class='alert alert-danger'>";
	html[html.length] = cctext.error;
	html[html.length] = " 		</div>";
	html[html.length] = "	</div>";
	html[html.length] = "</div>";

	html[html.length] = "<script>Utils.refreshSelectPickers();Utils.refreshPlaceholders();quotaOptionUpdate('service');Utils.mutiFieldChecker('quantity');";
	html[html.length] = "</script>";

	addQuotaDialog = new BootstrapDialog(
			{
				title : dialogTitle,
				message : html.join(""),
				type : BootstrapDialog.TYPE_SUCCESS,
				closable : true,
				onshown : function(dialogRef) {
					Utils.refreshPlaceholders();
				},
				buttons : [
						{
							id : 'qtacancelbtn',
							label : cancelButton,
							action : function(dialog) {
								typeof dialog.getData('callback') === 'function'
										&& dialog.getData('callback')(false);
								dialog.close();
							}
						},
						{
							id : 'qtaddbtn',
							hotkey : 13,
							label : actionButton,
							cssClass : 'btn-primary',
							icon : 'glyphicon glyphicon-send',
							action : function(dialog) {
								dialog.enableButtons(false);
								dialog.setClosable(false);
								$("#busy_updating_config").removeClass("hide");
								var formData = $('#serviceForm').serialize();
								getAddQuotaInfo(msisdn, sid, vid, ben, dialog,
										formData);
							}
						} ]
			}
	).open();
	
};

var getAddQuotaInfo = function(msisdn, sid, vid, ben, dialog, formData) {
	var encodedMsisdn = Utils.enclodeField(msisdn);
	var postData = "act=addQuotaTest&sid=" + escape(sid) + "&vid="
			+ escape(vid) + "&ben=" + ben + "&msisdn=" + encodedMsisdn + "&"
			+ formData;
	
	sendAjax("/" + sid.toLowerCase(), postData, function(data) {
		$("#busy_updating_config").addClass("hide");
		dialog.enableButtons(true);
		dialog.setClosable(true);

		if (data.status == 'fail') {
			$("#quota_failed_reason").html(data.message);
			$("#quota_add_failed").removeClass("hide");
		} else {
			// Close this dialog and open next (To confirm action)
			dialog.close();
			confirmQuotaAdd(msisdn, sid, vid, ben, formData, data);
		}
	});
};

var createConfirmQuaotaLabel = function(label, info) {
	var msg = [];
	msg[msg.length] = "<label style='margin-left:50px; width:120px;'>";
	msg[msg.length] = label;
	msg[msg.length] = "</label>";
	msg[msg.length] = "<span>";
	msg[msg.length] = info;
	msg[msg.length] = "</span><br/>";
	return msg.join("");
};

var confirmQuotaAdd = function(msisdn, sid, vid, ben, formData, data) {
	var msg = [];
	msg[msg.length] = data.message + "<br/>";
	msg[msg.length] = "</br>";
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaConsumerLabel,
			data.benMsisdn);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaServiceLabel,
			data.service);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaDestinationLabel,
			data.destination);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaDaysofWeekLabel,
			data.dow);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaTimeofDayLabel,
			data.tod);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaQuantityLabel,
			data.quantity + " " + data.units);
	msg[msg.length] = createConfirmQuaotaLabel(cctext.quotaCostLabel, data.cost
			+ " USD");

	var dialogTitle = cctext.confirmAddQuotaTitle;
	var actionButton = cctext.confirmAddQuotaAction;
	var cancelButton = cctext.confirmAddQuotaCancel;
	var encodedMsisdn = Utils.enclodeField(msisdn);

	BootstrapDialog.confirmDialog(dialogTitle, actionButton, cancelButton, msg
			.join(""), BootstrapDialog.TYPE_SUCCESS, function(result) {
		if (result) {
			var postData = "act=addQuota&sid=" + escape(sid) + "&vid="
					+ escape(vid) + "&ben=" + ben + "&msisdn=" + encodedMsisdn
					+ "&" + formData;
			sendAjax("/gsa", postData, function(data) {
				try {
					if (data.status == "pass") {
						createAlert("gui_alerts", "success", data.message)
						showMemberInfo(msisdn, sid, vid, ben);
						CrmHome.balanceEnquiry(sid);
					} else {
						// Display Fail message + reason
						createAlert("gui_alerts", "error", data.message);
					}	
				} catch(err) {
					console.error("BootstrapDialog.confirmDialog: " + err);
				}
			});
		}
	});
};

// createAddQuotaConfirm = function(dic)
// alertType= alert-success alert-info alert-error
var alertCounter = 0;

var lostFocus = function(msisdn){
	console.log("Hi there");
};

var startUpdateContact = function(msisdn, sid, vid, benMsisdn, feedback) {
	try {
		var $label = $("#lblcontact_" + benMsisdn);
		var $input = $("#inpcontact_" + benMsisdn);
		var isLabelMode = !($input.is(":visible"));
		
		if (isLabelMode) {
			//Put in edit mode
			var text = $label.html();
			$input.val(text);
			$input.show();
			$label.hide();
			$input.focus();

			// Handle Enter on Input
			$input.on("keyup", function(event) {
				var keycode = (event.keyCode ? event.keyCode : event.which);
				if (keycode == '13') {
					startUpdateContact(msisdn, sid, vid, benMsisdn);
				} else if (keycode == '27') {
					$label.html(text);
					$input.hide();
					$label.show();
				}
				if (keycode == '13' || keycode == '27') {
					$input.unbind("keyup");
					$input.unbind("blur");
				}
			}).on("blur", function(event) {
				startUpdateContact(msisdn, sid, vid, benMsisdn);
				$input.unbind("keyup");
				$input.unbind("blur");
			});
		} else {
			try {
				var contactName = $input.val();
				var encodedMsisdn = Utils.enclodeField(msisdn);
				var postData = "act=updateContactDetails&sid=" + escape(sid) + "&vid="
				+ escape(vid) + "&ben=" + benMsisdn + "&msisdn=" + encodedMsisdn
				+ "&contact=" + escape(contactName);
				
				//Send Contact Update
				sendAjax("/gsa", postData, function(data) {
					if (data.status == 'fail') {
						createAlert("gui_alerts", "error", data.message);
					} else {
						$label.html(contactName);
						createAlert("gui_alerts", "success", data.message);
					}
					$input.hide();
					$label.show();
				});

			} catch(err) {
				console.error("DMAN: " + err);
			}

		}
	} catch(err) {
		if (console) console.error(err);
	}

	
	
};


// ------------------------------------------
var buildAlertMessage = function(message) {
	var html = [];
	html[html.length] = "<div class='alert alert-danger' role='alert'>";
	html[html.length] = "<span style='font-weight:bold; margin-right: 12px;'>";
	html[html.length] = "!</span>";
	html[html.length] = message;
	html[html.length] = "</div>";
	return html.join("");
};