var lastSearchInformation
var lastHostPositions = [];
var TAIL_REFRESH_RATE = 10000;

$.fn.serializeForm = function(options) {
    var settings = {
        on: 'true',
        off: 'false'
    };
    if (options) {
        settings = $.extend(settings, options);
    }
    var $container = $(this).eq(0),
        $checkboxes = $container.find("input[type='checkbox']").each(function() {
            $(this).attr('value', this.checked ? settings.on : settings.off).attr('checked', true);
        });
    var s = ($container.serialize());
    $checkboxes.each(function() {
        var $this = $(this);
        $this.attr('checked', $this.val() == settings.on ? true : false);
    });
    return s;
};

var logViewer = {
	configFormData : null,
	lastHostPositions : [],
	tailTimeOutId : 0,
	recordsReceived : false,
	
	logHalted: false,
	busyTailing: false,
	
	init : function() {
		$(".selectpicker").selectpicker("refresh");
		this.allocatedEvents();
	},
	
	saveConfigFormState : function() {
		try {
			var form = $("#configModalForm");
			this.configFormData = form.serializeArray();
		} catch(err) {
			console.error(err);
		}

	},
	
	restoreFormState : function() {
		try {
			var $f = $("#configModalForm");
			$f.trigger("reset");
			var data = this.configFormData, $e;
			
			for (var i in data) {
			      $e = $f.find("[name=\"" + data[i].name + "\"]");
			      if ($e.is(":radio")) {
			    	  $e.filter("[value=\"" + data[i].value + "\"]").prop("checked", true);
			      } else if ($e.is(":checkbox") && data[i].value) {
			    	  $e.prop("checked", true);
			      } else if ($e.is("select")) {
			    	  $e.find("[value=\"" + data[i].value + "\"]").prop("selected", true);
			      } else {
			    	  $e.val(data[i].value);
			      }
			  $e.change();
			}
		} catch(err) {
			console.error(err);
		}
	},
	
	allocatedEvents : function() {
		var self = this;
		$("#logconfig").on("click", $.proxy(self.showConfigModalHandler, self));
		$("#saveLogConfig").on("click", $.proxy(self.saveLogConfigHandler, self));
		$("#logsearch").on("click", $.proxy(self.logsearchHandler, self));
		$("#logModalCancel").on("click", $.proxy(self.logModalCancelHandler, self));
		
		$("#logViewerForm").on("submit", $.proxy(self.logsearchHandler, self));
		$("#tail").on("change", $.proxy(self.tailCBHandler, self));
			
		$("#tail").click(function() {
			var isChecked = $("#tail").prop("checked");
			if (isChecked) {
				$("#endDateSection").hide();
			} else {
				$("#endDateSection").show();
			}
		});
		
	    $('.form_datetime').datetimepicker({
	        weekStart: 1,
	        todayBtn:  1,
			autoclose: 1,
			todayHighlight: 1,
			startView: 2,
			forceParse: 0
	    });
	    
	    $(".cleardate").on("click", function() {
	    	try {
	    		var parent = $(this).parent();
	    		var input = $("input", parent);
	    		input.val("");
	    	} catch(err) {
	    		console.error(err);
	    	}
	    });
	},
	
	showConfigModalHandler : function() {
		this.saveConfigFormState();
		if (this.busyTailing)
			this.logHalted = true;
		$("#logConfigModal").modal("show");
	},
	
	restartTailing : function() {
		if (this.logHalted) {
			this.logHalted = false;
			this.tailLogFile();
		}
	},
	
	saveLogConfigHandler : function() {
		hideErrorMessage("startDate", "configModalForm");
		
		var startDate = null;
		var endDate = null;
		try {
			var startDateString = $("#startDate").val();
			startDate = Date.parse(startDateString);
		} catch(err) {}
		
		try {
			var endDateString = $("#endDate").val();
			endDate = Date.parse(endDateString);
		} catch(err) {}
		
		try {
			if (startDate != null && endDate != null) {
				if (startDate > endDate) {
					showErrorMessage("startDate" , "Start Date after End Date", "configModalForm");
					return;
				}
			}
			$("#logConfigModal").modal("toggle");
		} catch(err) {
			console.error(err);
		}
		this.restartTailing();
	},
	
	logsearchHandler : function(e) {
		try {
			e.preventDefault();
		} catch(err) {
		}
		try {
			
			var self  = this;
			
			var formData = this.serializeFormInformation("logViewerForm");
			var configData = this.serializeFormInformation("configModalForm");
			
			if (configData != null) {
				formData += "&" + configData;
			}
			this.sendSearchRequest(formData, false);
		} catch(err) {
			console.error(err);
		}
	},
	
	logModalCancelHandler : function() {
		this.restoreFormState();
		hideErrorMessage("startDate", "configModalForm");
		this.restartTailing();
	},
	
	tailCBHandler : function() {
		try {
			if (! $("#tail").prop("checked")) {
				if (this.tailTimeOutId > 0) {
					clearTimeout(this.tailTimeOutId);
				}
			} else if (this.recordsReceived) {
				this.tailLogFile();
			}
		} catch(err) {
			console.error(err);
		}
	},
	
	tailLogFile : function() {
		try {
			var self  = this;
			this.busyTailing = true;
			
			//tailTimeOutId
			clearTimeout(this.tailTimeOutId);
			
			var formData = this.serializeFormInformation("logViewerForm");
			var configData = this.serializeFormInformation("configModalForm");
			
			if (configData != null) {
				formData += "&" + configData;
			}
			//lastHostPositions
			var hosts = [];
			for(host in this.lastHostPositions) {
				hosts[hosts.length] = host + ":" + this.lastHostPositions[host];
			}
			var tailPostions = hosts.join(",");
			formData += "&tinfo=" + tailPostions;
			this.sendSearchRequest(formData, true);
		} catch(err) {
			console.error(err);
		}
	},

	sendSearchRequest : function(serializedData, isTail) {
		var self = this;
		
		try {
			if (!isTail)
				this.showLoadingPanel();
			else
				this.busyTailing = true;
			
			if (isTail && this.logHalted) {
				return;
			}
			
			if (serializedData != null) {
				var status = $.ajax({
					type: "POST", 
					url: "/logview", 
					async: true, 
					data: "act=ret&" + serializedData,
					dataType: "json"
				}).done(function(data) {
					if (self.logHalted) {
						//discard information
						return;
					}
					try {
						var content = "";
						var entriesCount = 0;
						if ((typeof data.status !== "undefined" && data.status != null && data.status == "fail") || (typeof data.data === "undefined" || data.data == null)) {
							self.recordsReceived = false;
							if (typeof data.message !== "undefined" && data.message != null)
								content = self.createAlertHTML("alert-danger", data.message);
							else
								content = self.createAlertHTML("alert-danger", "Invalid Response from Server");
						} else {
							entriesCount = data.data.length;
							if (data.data.length == 0 && (!$("#tail").attr("checked"))) {
								self.recordsReceived = false;
								content = self.createAlertHTML("alert-info", "No records retrieved");
							} else {
								if (!isTail) {
									content = self.createLogHTML(data);
								}
								else
									content = self.createLogDataRowsHTML(data.data);
								
								//Store last positions for tail
								if ($("#tail").attr("checked") && (typeof data.hosts !== "undefined" || data.hosts != null)) {
									self.lastHostPositions = data.hosts;
									self.tailTimeOutId = setTimeout($.proxy(self.tailLogFile, self), TAIL_REFRESH_RATE);
								}
								self.recordsReceived = true;
							}
						}
						
						if (content.length > 0) {
							if (!isTail)
								$("#retrievedContent").html(content);
							else {
								if (! $("#latestAtTop").prop("checked")) {
									//Insert at the bottom
									$("#logTable tbody tr:last").after(content);
									
									//Remove from the top
									var count = $("#logTable tr").length;
									count -= entriesCount;
									$('#logTable tr:lt(-' + count + ')').remove();
								} else {
									//Insert at the top
									$("#logTable tbody tr:first").before(content);
									var count = $("#logTable tr").length;
									
									//Remove from bottom
									$("#logTable tr").slice(count - entriesCount).remove();
								}		
							}
							//$('#mytable tr:gt(3):lt(-1)').remove();
							if ($("#tail").prop("checked")) {
								var scrollHeight = 0;
								if (! $("#latestAtTop").prop("checked")) {
									scrollHeight = $(".scrollabletable").prop("scrollHeight");
								}
								$(".scrollabletable").animate({ scrollTop: scrollHeight}, 1000);
							} else {
								$(".scrollabletable").scrollTop(0);
							}
							
						}

					} catch(err) {
						console.error(err);
					}
				});
			}
		} catch(err) {
			console.error("validateAndSearch: " + err);
		}
	},
	
	serializeFormInformation : function(formID) {
		var serialized = null;
		try {
			var myform = $("#" + formID);
			var disabled = myform.find(':input:disabled').removeAttr('disabled');
			serialized = myform.serializeForm();
			disabled.attr('disabled','disabled');
		} catch(err) {
			concole.error(err);
		}
		return serialized;
	},
	
	showLoadingPanel : function() {
		try {
			var html = this.createLoaderHTML();
			$("#entriesCount").html("...");
			$("#retrievedContent").html(html);
			$("#retrievedPanel").show();
		} catch(err) {
			console.error(err);
		}
	},
	
	createLoaderHTML : function() {
		var html = [];
		html[html.length] = "<div>";
		html[html.length] = "<img src='/img/load.gif' />";
		html[html.length] = "<span class='retrieveLoading'>Retrieving Records...</span>";
		html[html.length] = "</div>";
		
		return html.join("");
	},
	
	createLogHTML : function(data) {
		var html = [];
		html[html.length] = "<div class='scrollabletable'>";
		html[html.length] = "<table id='logTable' class='table infotable table-hover table-bordered table-striped'>";
		html[html.length] = "<thead>";
		html[html.length] = "<tr>";
		html[html.length] = "<th>Host</th>";
		html[html.length] = "<th>Date</th>";
		html[html.length] = "<th>Level</th>";
		html[html.length] = "<th>Transaction ID</th>";
		html[html.length] = "<th>Component</th>";
		html[html.length] = "<th>Operation</th>";
		html[html.length] = "<th>Code</th>";
		html[html.length] = "<th>Text</th>";
		html[html.length] = "</tr>";
		html[html.length] = "</thead>";
		html[html.length] = "<tbody>";
		var content = this.createLogDataRowsHTML(data.data);
		html[html.length] = content;
		html[html.length] = "</tbody>";
		html[html.length] = "</table>";
		html[html.length] = "</div>";
		
		return html.join("");
	},
	
	createLogDataRowsHTML : function(data) {
		var html = [];
		var searchText = $("#search").val();
		if (searchText.length == 0) {
			searchText = null;
		}
		if (data != null) {
			for(var i=0; i<data.length; i++) {
				html[html.length] = "<tr>";
				for(var j=0; j<data[i].length; j++) {
					html[html.length] = "<td>";
					var text = "" + data[i][j];
					if (searchText != null && text != null) {
						var lcase = searchText.toLowerCase();
						var lcaseText = text.toLowerCase();
						var beginIndex = lcaseText.indexOf(lcase);
						if (beginIndex >=0) {
							//HuX Service
							text = text.substring(0,beginIndex) + "<b>" + searchText + "</b>" + text.substr(beginIndex + searchText.length);
						}
					}
					
					html[html.length] = text;
					html[html.length] = "</td>";	
				}
				html[html.length] = "</tr>";
			}
		}

		return html.join("");
	},
	
	createAlertHTML : function(severity, content) {
		var html = [];
		html[html.length] = "<div class='alert " + severity + "' role='alert'>";
		html[html.length] = content;
		html[html.length] = "</div>";
		return html.join("");
	}
};

$( document ).ready(function() {
	try {
		logViewer.init();

		$(window).keydown(function(event){
			if(event.keyCode == 13) {
				
			}
		});
	} catch(err) {
		alert("error on page log loader: " + err);
	}
});