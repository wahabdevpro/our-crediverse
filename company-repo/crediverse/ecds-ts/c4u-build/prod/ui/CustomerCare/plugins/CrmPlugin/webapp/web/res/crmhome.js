//Catch all for error detection
window.onerror = function (message, file, line, col, error) {
	if (console) console.log(error.stack);
};

printStackTrace = function() 
{
	  var e = new Error('dummy');
	  var stack = e.stack.replace(/^[^\(]+?[\n$]/gm, '')
	      .replace(/^\s+at\s+/gm, '')
	      .replace(/^Object.<anonymous>\s*\(/gm, '{anonymous}()@')
	      .split('\n');
	  if (console) console.log(stack);
};

var CrmHome = {
	services : [],
	//Buttons
	
	//Divs
	subHistoryId : "subhistory",
	cccontentId : "cccontent",
	servicesPrefix : "home_",
	
	//Text Boxes
	msisdnId : "msisdn",
	
	// MSISDN Retrieval
	retrieveAccountDetails : function(noClose) {
		try {
			var self = this;
			$("#" + this.subHistoryId).html("");
			if (typeof noClose === 'undefined' || noClose == null)
				Alerts.closeAllAlerts();
			var msisdn = Utils.removeSpaces($("#" + this.msisdnId).val())
			var encodedMsisdn = Utils.enclodeField(msisdn);
			var postData = "act=retMsisdn&msisdn=" + encodedMsisdn;
			sendContentAjax("/custcare", postData, function(data) {
				resetSessionTimeout();
				self.msisdnDataRetrieved(data);
			});
		} catch(err) {
			Utils.logError("removeSpaces", err)
		}
	},
	
	msisdnDataRetrieved : function(content) {
		try {
			$("#" + this.cccontentId).html(content);
		} catch (err) {
			Utils.logError(err);
		}
	},
	
	retrieveAccountHistory : function() {
		try {
			$("#cccontent").html("retrieveAccountHistory...");
			$("#subhistory").html(createHistoryTableHtml);
			var msisdn = Utils.removeSpaces($("#msisdn").val())
			var encodedMsisdn = Utils.enclodeField(msisdn);
			var postDataUrl = "/custcare?act=getHistory&msisdn=" + encodedMsisdn;
			var langurl = "/i18n/" + cctext.tablei18n;
			$('#subhistorytable').dataTable({
				language : {
					url : langurl
				},
				ajax : postDataUrl,
				aaSorting : []
			});
			resetSessionTimeout();
		} catch (err) {
			console.error("retrieveAccountHistory: " + err);
			Utils.logError(err);
		}
	},
	
	registerButtonEvents : function() {
		var self = this;
		$("#" + self.msisdnId).on("keyup", function(event) {
			if ($("#" + self.msisdnId).val().length > 0) {
				$("#retrieve").removeClass("disabled");
				$("#retrieve").removeAttr("disabled");
				$("#searchHistory").removeClass("disabled");
				$("#searchHistory").removeAttr("disabled");
			} else {
				$("#retrieve").addClass("disabled");
				$("#retrieve").attr("disabled", "disabled");
				$("#searchHistory").addClass("disabled");
				$("#searchHistory").attr("disabled", "disabled");
			}
			var keycode = (event.keyCode ? event.keyCode : event.which);
			if (keycode == '13') {
				self.retrieveAvailableServices();
			}
		});
		
		$("#retrieve").click(function() {
			self.retrieveAvailableServices();
		});
		
		$("#searchHistory").click(function() {
			self.retrieveAccountHistory();
		});
		
		$("#goHome").on("click", function() {
			self.retrieveAvailableServices()
		});
		
	},
	
	showNavMenu : function(location) {
		$("#serviceloc").html(location);
		$(".navbarclass").show();
	},
	
	//----------------------------- PRIVATE -------------------------------------------
	retrieveAvailableServices : function(noClose) {
		try {
			var self = this;
			$(".navbarclass").hide();
			$("#" + this.subHistoryId).html("");
			if (typeof noClose === 'undefined' || noClose == null)
				Alerts.closeAllAlerts();
			
			var msisdn = Utils.removeSpaces($("#" + this.msisdnId).val())
			var encodedMsisdn = Utils.enclodeField(msisdn);
			var postData = "act=retMsisdn&msisdn=" + encodedMsisdn;
			
			sendContentAjax("/custcare", postData, function(data) {
				try {
					resetSessionTimeout();
					if (data != null && typeof data.status !== "undefined" && data.status == "fail") {
						createAlert("gui_alerts", "error", data.message);
					} else {
						if (data.length == 0)
							createAlert("gui_alerts", "info", "No Services could be retrieved for " + msisdn);
						else
							self.retrieveAccountService (encodedMsisdn, data);	
					}
				} catch(err) {
					console.error("retrieveAvailableServices[ajax]: " + err);
				}

			});
		} catch(err) {
			console.error("retrieveAvailableServices: " + err);
		}
	},
	
	refreshAccountService : function(msisdn, serviceId) {
		try {
			var index = -1;
			for(var i=0; i<this.services.length; i++) {
				if (this.services[i].id == serviceId) {
					index = i;
					break;
				}
			}
			var elid = this.servicesPrefix + serviceId;
			$("#" + elid).html("<center><img src='/img/bigwait.gif' /></center>");
			this.retrieveAccountServiceDetails("/" + this.services[index].ctrl, this.services[index].id, msisdn);
		} catch(err) {
			if (console) console.error(err);
		}
	},
	
	retrieveAccountService : function(msisdn, data) {
		try {
			//Cache and display wait
			this.services = data;
			var html = [];
			//Create 2 divs only one has a wait circle
			for(var i=0; i<data.length; i++) {
				//createDivHtml
				html[html.length] = Utils.createDivHtml(this.servicesPrefix + data[i].id, (i==0)? "<center><img src='/img/bigwait.gif' /></center>" : "");
			}
			Utils.updateDivContent(this.cccontentId, html.join(""));
			
			//For each service retrieve data
			for(var i=0; i<data.length; i++) {
				this.retrieveAccountServiceDetails("/" + data[i].ctrl, data[i].id, msisdn);
			}
		} catch(err) {
			console.error("retrieveAccountService: " + err);
		}
	},
	
	retrieveAccountServiceDetails : function(crlUrl, id, msisdn) {
		var self = this;
		
		var postData = "act=retServiceInfo&msisdn=" + msisdn;
		try {
			$.ajax({
				type : "POST",
				url : crlUrl,
				async : true,
				data : postData,
				dataType : "html"
			}).done(function(content) {
				var $updateDiv = $("#" + self.servicesPrefix + id);
				$updateDiv.html(content);
			}).fail(function(jqXHR, textStatus ) {
				console.log("fail: " + textStatus);
			});
		} catch(err) {
			console.error("retrieveAccountServiceDetails: " + err);
		}
		
	},
	
	balanceEnquiry : function(sid) {
		try {
			if (sid == "AutoXfr") return;
			var serviceId = sid.toLowerCase();
			var msisdn = $("#msisdn").val();
			$("#balanceEnquiryResult").html(createLoaderHtml());
			sendAjax("/" + serviceId, "act=performBalanceEnquiry&msisdn=" + msisdn,
					function(data) {
						if (typeof data.status !== "undefined"
								&& data.status == "fail") {
							createAlert("gui_alerts", "error", data.message);
						} else {
							if (!retrieveBalancesAutomatically) {
								createAlert("gui_alerts", "success", data.msg);
							}
							var html = createBalanceRetrivalHtml(data);
							$("#balanceEnquiryResult").html(html);
						}
					});
		} catch (err) {
			console.error(log);
		}
	}
	//----------------------------- PUBLIC --------------------------------------------
	

};

var Alerts = {
	closeAllAlerts : function() {
		try {
			$(".alert").alert("close");
		} catch (err) {
		}
	},
	
	updateAlert : function(cid, msg, alertType, fade) {
		try {
			// First remove all classes
			$("#" + cid).removeClass(INFO_CLASS);
			$("#" + cid).removeClass(SUCCESS_CLASS);
			$("#" + cid).removeClass(FAIL_CLASS);

			$("#" + cid).html(msg);
			$("#" + cid).addClass(alertType);
			$("#" + cid).removeClass("hide");
			$("#" + cid).show();
			if (fade) {
				$("#" + cid).fadeOut(FATE_OUT_TME);
			}
		} catch (err) {
			Utils.logError("updateAlert: " + err);
		}
	}
};

BootstrapDialog.showOperationsDialog = function(serviceId, dialogType, dialogTitle,
		actionLabel, cancelLabel, postData, callback, dataCallback) {
	var bsdialog = null;
	try {
		var msg = [];
		msg[msg.length] = '<div id="propertyMessageUpdateContent">';
		msg[msg.length] = '<center><img src="/img/bigwait.gif" /></center>';
		msg[msg.length] = '</div>';
		
		var servlet = "/" + serviceId.toLowerCase();

		bsdialog = new BootstrapDialog(
				{
					title : dialogTitle,
					message : msg.join(""),
					type : dialogType,
					closable : true,
					data : {
						'callback' : callback
					},
					onshow : function(dialogRef) {
						// Disable buttons before get data
						if (postData != null) {
							dialogRef.getButton("btnSend").disable();
							sendAjax(servlet, postData, function(data) {
								if (typeof data.status !== "undefined") {
									if (data.status == "pass") {
										dialogRef.getModalBody().html(data.message);
										dialogRef.getButton("btnSend").enable();
									} else {
										var html = buildAlertMessage(data.message);
										dialogRef.getModalBody().html(html);
									}
								} else {
									if (typeof dataCallback === 'function') {
										dataCallback(data);
									}
								}
							});
						} else {
							dialogRef.getButton("btnSend").enable();
							if (typeof dataCallback === 'function') {
								dataCallback(data);
							}
						}
					},
					buttons : [
							{
								id : "btnCancel",
								label : cancelLabel,
								action : function(dialog) {
									typeof dialog.getData('callback') === 'function'
											&& dialog.getData('callback')
													(false);
									dialog.close();
								}
							},
							{
								id : "btnSend",
								label : actionLabel,
								cssClass : (dialogType == BootstrapDialog.TYPE_DANGER) ? 'btn-danger'
										: 'btn-success',
								action : function(dialog) {
									typeof dialog.getData('callback') === 'function'
											&& dialog.getData('callback')(true);
								}
							} ]
				}).open();
	} catch (err) {
//		Utils.logError("showOperationsDialog: " + err);
	}
	return bsdialog;
};

showFullHelp = function() {
	try {
		$('.overlay').toggle();
//		$("main_content");
	} catch(err) {
		console.error(err);
	}
};

