var Act = {
		creditTransfers : [],
		dialogButtonRef : null,
		memberMsisdn : null,
		serviceId : null,
		variantId : null,
		msisdn : null,
		
		extractModeOptions : function(data) {
			var html = [];
			try {
				for(var i=0; i < data.length; i++) {
					if (data[i].requiresSubscription) {
						html[html.length] = "<option value='";
						html[html.length] = data[i].transferModeID;
						html[html.length] = "'>";
						html[html.length] = data[i].name;
						html[html.length] = "</option>";
					}
				}
			} catch(err) {
				console.error(err);
			}
			return html.join("");
		},
		
		buildAddTransferDialogContent : function(data) {
			var html = [];
			try {
				html[html.length] = "<div class='row'>";
				html[html.length] = "	<div class='col-xs-10 col-sm-10 col-md-10' id='transferModeDiv' class='transferModeDiv'>";
				html[html.length] = "		<label class='control-label' for='transferModeID'>";
				html[html.length] = "Transfer Mode";
				html[html.length] = "</label>";
				html[html.length] = "		<select id='transferModeID' name='transferModeID' class='form-control selectpicker' onchange='Act.transferModeChanged(this);'>";
				html[html.length] = Act.extractModeOptions(data);
				html[html.length] = "		</select>";
				html[html.length] = "	</div>";
				html[html.length] = "	</div></br>";
				
				html[html.length] = "<div class='row'>";
				
				html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6' id='transferAmount'>";
				html[html.length] = "		<label class='control-label' for='amount' >";
				html[html.length] = "Amount";
				html[html.length] = "</label>";
				html[html.length] = "		<input type='text' id='amount' class='form-control currency' name='amount' placeholder='Transfer Amount (USD)' />";
				html[html.length] = "	</div>";
				
				html[html.length] = "	<div class='col-xs-6 col-sm-6 col-md-6' id='transferLimit'>";
				html[html.length] = "		<label class='control-label' for='limit' >";
				html[html.length] = "Transfer Limit";
				html[html.length] = "</label>";
				html[html.length] = "		<input type='text' id='limit' class='form-control currency' name='limit' placeholder='Transfer Limit (USD)' />";
				html[html.length] = "	</div>";
				
				html[html.length] = "</div>";
				html[html.length] = "</form><BR/>";
				html[html.length] = "<div id='busy_updating_config' class='row hide'>";
				html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md-12'>";
				html[html.length] = "		<div class='well well-lg'>";
				html[html.length] = "			<img alt='Waiting...' src='/img/cell_wait.gif'>";
				html[html.length] = "Busy ...";
				html[html.length] = " 		</div>";
				html[html.length] = "	</div>";
				html[html.length] = "</div>";

				html[html.length] = "<div id='quota_add_failed' class='row hide'>";
				html[html.length] = "	<div class='col-xs-12 col-sm-12 col-md16'>";
				html[html.length] = "		<div id='quota_failed_reason' class='alert alert-danger'>";
				html[html.length] = cctext.error;
				html[html.length] = " 		</div>";
				html[html.length] = "	</div>";
				html[html.length] = "</div>";

				html[html.length] = "<script>Utils.refreshSelectPickers();Utils.numericFieldChecker();Utils.refreshPlaceholders();Utils.currencyFieldChecker();Act.transferModeChanged();";
				html[html.length] = "</script>";
				
			} catch(err) {
				console.error(err);
			}
			return html.join("");
		},
		
		addTransfer : function(msisdn, sid, vid, ben) {
			try {
				
				Act.memberMsisdn = Utils.enclodeField(ben);
				Act.serviceId = sid;
				Act.variantId = ben;
				Act.msisdn = Utils.enclodeField(msisdn);
				
				var dialogTitle = "Add Transfer";
				var actionLabel = "Add";
				var cancelLabel = "Cancel";
				var postData = "act=addTransferModataData&msisdn=" + Act.msisdn + "&sid=" + sid + "&vid=" + vid + "&ben=" + Act.memberMsisdn;
				

				Act.dialogButtonRef = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_SUCCESS,
						dialogTitle, actionLabel, cancelLabel, postData, function(result) {
					if (result) {
						Act.getAddTransferInfo();
					}
				}, function(data) {
					try {
						Act.creditTransfers = data;
						//Build Dialog Content
						var $html = $(Act.buildAddTransferDialogContent(data));
						Act.dialogButtonRef.getModalBody().html($html);
						
						var element = $html.find("#amount");
						element.on("keyup", function() {
							var value = $(this).val();
							if (value.length > 0)
								Act.dialogButtonRef.getButton("btnSend").enable();
							else
								Act.dialogButtonRef.getButton("btnSend").disable();
						});
					} catch(err){
						console.error("addTransfer: " + err);
					}
				});
				
			} catch(err) {
				console.error("addTransfer: " + err);
			}
		},
		
		getAddTransferInfo : function () {
			try {
				var amount = $("#amount").val();
				var transferMode = $("#transferModeID").val();
				var limit = "";
				var data = Act.getTransferModeSelected();
				if (data.transferType != "PERIODIC") {
					limit = $("#limit").val();
				}
				
				var postInfo = "sid=" + Utils.enclodeField(Act.serviceId)
				+ "&vid=" + Utils.enclodeField(Act.variantId) + "&msisdn="
				+ Act.msisdn + "&ben=" + Act.memberMsisdn + "&amount=" 
				+ amount + "&mode=" + transferMode;
				
				if (data.transferType != "PERIODIC") {
					postInfo += "&limit=" + limit;
				}
				
				var postData = "act=addCreditTransferTest&" + postInfo;
				var servlet = "/" + Act.serviceId.toLowerCase();
				
				//Check if everything is correct with the crm suppplied info
				sendAjax(servlet, postData, function(data) {
					$("#busy_updating_config").addClass("hide");
					if (data.status == 'fail') {
						$("#quota_failed_reason").html(data.message);
						$("#quota_add_failed").removeClass("hide");
					} else {
						// Close this dialog and open next (To confirm action)
						try {
							Act.dialogButtonRef.close();	//Seems to close and throw and exception	
						} catch(err) {}
						
						Act.confirmQuotaAddTransfer(postInfo, data);
					}
				});
			} catch(err) {
				console.error("getAddTransferInfo: " + err);
			}
		},
		
		confirmQuotaAddTransfer : function(postInfo, data) {			
			var msg = data.message;
			var dialogTitle = "<span id='subscribeTitle'>Confirm Transaction</span>";
			var actionButton = "Confirm";
			var cancelButton = "Cancel";
			var encodedMsisdn = Utils.enclodeField(msisdn);
			var postData = "act=addCreditTransfer&" + postInfo;
			
			BootstrapDialog.confirmDialog(dialogTitle, actionButton, cancelButton, data.message, 
					BootstrapDialog.TYPE_SUCCESS, function(result) {
				if (result) {
					sendAjax("/" + Act.serviceId.toLowerCase(), postData, function(data) {
						try {
							if (data.status == "pass") {
								createAlert("gui_alerts", "success", data.message);
								showMemberInfo(Act.msisdn, Act.serviceId, Act.variantId, Act.memberMsisdn);
							} else {
								// Display Fail message + reason
								createAlert("gui_alerts", "error", data.message);
							}	
						} catch(err) {
							console.error("confirmQuotaAddTransfer: " + err);
						}
					});
				}
			});
		},
		
		removeTransfer : function(msisdn, sid, vid, ben, tid) {
			try {
				var dialogTitle = "Confirm remove Credit Transfer";
				var actionLabel = "Remove";
				var cancelLabel = "Cancel";
				var msisdn = Utils.removeSpaces(msisdn);
				var encodedMsisdn = Utils.enclodeField(msisdn);
				var encodedTid = Utils.enclodeField(tid);
				var encodedBen = Utils.enclodeField(ben);
				
				var postInfo = "msisdn=" + encodedMsisdn + "&sid="
						+ sid + "&vid=" + vid + "&ben=" + encodedBen + "&tid=" + tid;
				var postData = "act=removeTransferTest&" + postInfo;

				var dialog = BootstrapDialog.showOperationsDialog(sid, BootstrapDialog.TYPE_DANGER,
						dialogTitle, actionLabel, cancelLabel, postData, function(result) {
							if (result) {
								// Remove Benificiary
								var postData = "act=removeTransfer&" + postInfo;
								sendAjax("/" + sid.toLowerCase(), postData, function(data) {
									// Show message and update
									dialog.close();
									if (data.status == "pass") {
										createAlert("gui_alerts", "success", data.message);
										if (typeof perspective !== 'undefined'
												&& perspective == 'prov') {
											CrmHome.refreshAccountService(msisdn, sid);
//											CrmHome.retrieveAvailableServices();
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
			} catch(err) {
				console.error("removeTransfer: " + err);
			}
		},
		
		updateTransfersEvents : function() {
			try {
				$(html).find(".selectpicker").on("change", function() {
					var selected = $(this).find("option:selected").val();
					alert(selected);
				});
			} catch(err) {
				if (console) console.error(err);
			}
		},
		
		getTransferModeSelected : function() {
			var data = null;
			try {
				var value = $("#transferModeID").val();
				for(var i=0; i<Act.creditTransfers.length; i++) {
					if (Act.creditTransfers[i].transferModeID ==value) {
						data = Act.creditTransfers[i];
						break;
					}
				}
			} catch(err) {
				if (console) console.error("getTransferModeSelected: " + err);
			}
			return data;
		},
		
		transferModeChanged : function(el) {
			try {
				var data = Act.getTransferModeSelected();
				
				if (data.transferType == "PERIODIC")
					$("#transferLimit").hide();
				else {
					$("#transferLimit").show();
					$("#limit").attr("placeholder", "Transfer Limit (" + data.units + ")");
				}
					
				$("#amount").attr("placeholder", "Transfer Amount (" + data.units + ")");
				
			} catch(err) {
				if (console) console.error(err);
			}
			
		}
		
};
