
function HtmlCreator() {
	this.html = [];
	
	this.add = function(text) {
		this.html[this.html.length] = text;
	};
	
	this.create = function() {
		return this.html.join("");
	};
}

function TableCreator() {
	this.html = new HtmlCreator();
	this.startedBody = false;
	
	this.init = function() {
		this.html.add("<table class='table infotable table-hover table-bordered table-striped'>");
	};
	
	this.addHeadings = function(headings) {
		this.html.add("<thead>");
		this.html.add("<tr>");
		
		
		for(var i=0; i<headings.length; i++) {
			this.html.add("<th>");
			this.html.add(headings[i]);
			this.html.add("</th>");
		}
		this.html.add("</tr>");
		this.html.add("</thead>")
	};
	
	this.addRow= function(columns) {
		if (!this.startedBody) {
			this.html.add("<tbody>");
			this.startedBody = true;
		}
		this.html.add("<tr>");
		for(var i=0; i<columns.length; i++) {
			this.html.add("<td>");
			this.html.add(columns[i]);
			this.html.add("</td>");
			
		}
		this.html.add("</tr>");
	};
	
	this.create = function() {
		this.html.add("</tbody>");
		this.html.add("</table>");
		return this.html.create();
	};
}

convertDateFormatToHumanReadable = function (dateFormat) {
	try {
		var sep = dateFormat.split("/");
		for(var i=0; i<sep.length; i++) {
			sep[i] = sep[i].toLowerCase();
			if (sep[i].length == 1) {
				sep[i] = sep[i] + sep[i];
			}
		}
		return sep.join("/");
	} catch(err) {
		console.error(err);
	}
	return dateFormat;
};


function ModalContentCreator() {
	this.html = new HtmlCreator();
	this.elementNumber = 0;
	
	this.splitCamelCase = function(stringToSplit) {
		var str = stringToSplit.replace(/([a-z]|[0-9])([A-Z])/g,"$1 $2").replace(/^([a-z])/, function(a){ return a.toUpperCase();});
		str = str.replace(/([a-z])([0-9])/g,"$1 $2");
		return str;
	};
	
	this.addElement = function(field, selected) {
		try {
			//DateRange$Periods
			if (field.type=="DateRange" && (this.elementNumber % 2) != 0 && this.html.length > 0) {
				this.html.add("</div>");
			}
			
			if ((this.elementNumber % 2) == 0 || field.type=="DateRange") {
				this.html.add("<div class='row'>");
			}
			
			this.html.add("<div class='col-md-6'>");
			this.html.add("<label for='userid' class='control-label'>" + field.desc + "</label>");
			if (typeof field.sel !== 'undefined' && field.sel != null) {
				this.html.add("<select id='" + field.field + "' name='" + field.field + "' class='form-control selectpicker'>");
				for(var i=0; i<field.sel.length; i++) {
					var value = field.sel[i];
					var description = "";
					if (field.sel[i].indexOf(":") > 0) {
						var sa = field.sel[i].split(":");
						value = sa[0];
						description = sa[1];
					} else {
						description = this.splitCamelCase(field.sel[i]);
					}
					
//					if (value != "Custom") {
//						//Custom type not allowed (for now)
						this.html.add("<option value='" + value + "' ");
						if (typeof selected !== "undefined" && selected == i) {
							this.html.add("selected='true'");
						}
						this.html.add(">" + description + "</option>")
//					}
				}
				this.html.add("</select>");
			} else {
				this.html.add("<input type='text' class='form-control' id='" + field.field + "' name='" + field.field + "' placeholder='" + field.desc + "' />");
			}
			
			//DateRange$Periods
			if (field.type == "DateRange") {
				this.html.add("</div>");
				this.html.add("</div>");
				
				this.html.add("<div class='row'>");
				var humanReadableDateFormat  = convertDateFormatToHumanReadable(field.dateFormat);
				
				//From Date
				this.html.add( this.createDateRangeHtml(field.field + "_from", field.desc + " (From Date)", humanReadableDateFormat) );	
				
				//To Date
				this.html.add( this.createDateRangeHtml(field.field + "_to", field.desc + " (To Date)", humanReadableDateFormat) );
				
				//End Row
				this.html.add("</div>");
			} else if ((this.elementNumber % 2) != 0) {
				this.html.add("</div>");
			}
			
			this.elementNumber++;
		} catch(err) {
			console.error("addElement Error:" + err);
		}
	};

	
	//fielName includes Type (i.e. field_from)
	
	this.createDateRangeHtml = function(fieldName, fieldDescription, placeHolder) {
		var html = [];
		html[html.length] = "<div class='col-md-6' id='" + fieldName + "div'>";
		html[html.length] = "<label for='userid' class='control-label'>" + fieldDescription + "</label>";
		html[html.length] = "<div class='input-group datetimers' id='"+fieldName+"_group'>";
		html[html.length] = "	<input type='text' class='form-control' id='" + fieldName + "' name='" + fieldName + "' placeholder='" + placeHolder + "' />"
		html[html.length] = "	<span class='input-group-addon glyphicon glyphicon-time' style='cursor: pointer;'></span>";		
		html[html.length] = "</div>";
		html[html.length] = "<span id='" + fieldName + "_error' class='error_message hide'></span>"
		html[html.length] = "</div>";
		return html.join("");
	};
	
	this.create = function() {
		if ((this.elementNumber % 2) != 0) {
			this.html.add("</div>");
		}
		return this.html.create();
	};
}

var reportController = {

		reports : [],
		
		parmModel : null,
		
		reportFields : null,
		reportType : null,
		reportIndex : null,
		parametersAvailable : false,
		momentDateFormat : null,
		
		reportsUrl : "/reports",
		
		init : function() {
			this.loadAvailableReports();
			this.initModalParameters();
		},
		
		createLoadingContent : function(msg) {
			return "<center><img src='/img/load.gif' /><span style='margin-left:10px;'></span>" + msg + "</center>";
		},
		
		/**
		 * Content
		 */
		createShowContent : function(html) {
			$("#retrievedPanel").removeClass("hide");
			$("#retrievedPanel").show();
			$("#retrievedContent").html(html);
		},
		
		createTabAndShowLoading : function() {
			this.createShowContent(this.createLoadingContent("Generating Report"));
		},
		
		/**
		 * Modal Methods
		 */
		initModalParameters : function() {
			this.parmModel = $("#generalModal");
			$("#actionButton").on("click", $.proxy(this.modalOkPressed, this));
		},
		
		extractDateFormatFromFields : function() {
			try {
				if (this.reportFields != null) {
					for(var i=0; i<this.reportFields.length; i++) {
						if (typeof this.reportFields[i].dateFormat !== "undefined") {
							var dateFormat = this.reportFields[i].dateFormat;
							dateFormat = dateFormat.replace(new RegExp("d", 'g'), "D");
							this.momentDateFormat = dateFormat.replace(new RegExp("y", 'g'), "Y");
							break;
						}
					}
				}
			} catch(err) {
				console.log("extractDateFormatFromFields: " + err);
			}
		},
		
		updateModalContent : function(html) {
			$("#generalModalContent").html(html);
			this.refreshSelectPickers();
			this.extractDateFormatFromFields();
			this.refreshDateTimePickers();
		},
		
		modalLoadingContent : function(msg) {
			this.updateModalContent(this.createLoadingContent(msg));
		},
		
		openWindow : function (url, name, props) {
			try {
				if(/*@cc_on!@*/false){ //do this only in IE
					var windowRef = window.open("", name);
					windowRef.close();
				}
				var windowRef = window.open(url, name);
				if (!windowRef.opener) {
					windowRef.opener = self;
				}
				windowRef.focus();
				return windowRef;	
			} catch(err) {
				return null;
			}
		},
		
		showDateErrorMessage : function(fieldId, msg) {
			if ($("#" + fieldId).val().length == 0) {
				showErrorMessage(fieldId, msg);
				return true;
			} else {
				hideErrorMessage(fieldId);
				return false;
			}
		},
		
		isFieldEmpty : function(fieldId) {
			if ($("#" + fieldId).val().length == 0)
				return true;
			else 
				return false;
		},
		
		dateFormatError : function(field) {
			var testDate = $("#" + field).val();
			return (!moment(testDate, this.momentDateFormat, true).isValid());
		},
		
		dateErrorCheck : function(fieldId) {
			var error = false;
			if (this.isFieldEmpty(fieldId)) {
				showErrorMessage(fieldId, "Date Required");
				error = true;
			} else if (this.dateFormatError(fieldId)) {
				showErrorMessage(fieldId, "Invalid format (" + convertDateFormatToHumanReadable(this.momentDateFormat) + ")");
				error = true;
			} else {
				hideErrorMessage(fieldId);
			}
			return error;
		},
		
		modalOkPressed : function()
		{
			try {
				var isError = false;
				//First Check the validity of the OK call
				if (this.reportFields != null) {
					for(var i=0; i<this.reportFields.length; i++) {
						if (this.reportFields[i].type == "DateRange" && $("#" + this.reportFields[i].field).val() == "Custom") {
							isError = this.dateErrorCheck(this.reportFields[i].field + "_from") || isError;
							isError = this.dateErrorCheck(this.reportFields[i].field + "_to") || isError;
						}
					}
					if (isError) return false;
				}
				
				var self = this;
				var reportName = this.reports[this.reportIndex]; 
				var parms = "name=" + reportName + "&act=rep&type=" + this.reportType;
				if (this.parametersAvailable) {
					parms += "&" +  $("#generalModalForm").serialize({ checkboxesAsBools: true });
					this.hideModal();
				}
				
				var path = this.reportsUrl + "?" + parms;
				if (this.reportType == 'HTML')
				{
					var myWindow  = this.openWindow("",'_blank');
					if (typeof myWindow !== "undefined" && myWindow != null) {
						myWindow.document.location.href = path;
					} else {
						window.location.href = path;
					}
				}
				else
				{
					$.fileDownload(path, {
						failCallback: function (html, url) {
							alert( 'Here was the resulting error: \r\n' + html);
						}
					});
				}
			} catch(err) {
				alert(err);
			}

				
		},
		
		showModal : function() {
			$("#generalModal").modal("show");
		},
		
		hideModal : function() {
			this.parmModel.modal("hide");
		},
		
		refreshSelectPickers : function() {
			try {
				$(".selectpicker").selectpicker();
			} catch (err) {
			}
		},
		
		updateDateTimePicker : function(identifier) {
			var self = this;
		    
			$(identifier).datetimepicker({
					pickTime: false,
					format: self.momentDateFormat,
					ignoreChanges: true
					
			});
		},
		
		refreshDateTimePicker : function(id) {
			this.updateDateTimePicker('#' + id);
		},
		
		refreshDateTimePickers : function() {
			this.updateDateTimePicker(".datetimers");
		},
		
		refreshPeriodCusomHiding : function(fields) {
			try {
				  $('.selectpicker').on('change', function(ev){
					    var selected = $(this).find("option:selected").val();
					    var id = $(ev.target).attr("id");
					    if (selected == "Custom") {
					    	$("#" + id + "_todiv").show();
					    	$("#" + id + "_fromdiv").show();
					    } else {
					    	$("#" + id + "_todiv").hide();
					    	$("#" + id + "_fromdiv").hide();
					    }
				  });
			} catch(err) {
				console.error(err);
			}
		},
		
		/**
		 * Step 1: Get Available Reports
		 */
		loadAvailableReports : function() {
			sendAsyncAjax("/reports", "act=avail", $.proxy(this.dataloadedSuccess, this), this.dataloadedFailure);
		},
		
		dataloadedSuccess : function(data) {
			var $html = null;
			if (typeof data !== "undefinedf" && data != null) {
				if (typeof data.status !== "undefinedf" && data.status != null && data.status == "fail") {
					$html = $(this.createErrorHtml(data.message));
				} else {
					this.reports = data.reports;
					$html = $(this.createAvailableReportsTable());
					this.addActionEvents($html);
				}
				$("#availableReports").html($html);
			}
		},

		dataloadedFailure : function(error) {
			console.error("Failed to load");
		},
		
		/**
		 * Step 2: Create reports table
		 */
		createAvailableReportsTable : function() {
			var tc = new TableCreator();
			tc.init();
			tc.addHeadings(["Report Name", "Download Type"]);
			for(var i=0; i<this.reports.length; i++) {
				var htmlActions = this.createActions(i);
				tc.addRow([this.reports[i], htmlActions]);
			}
			return tc.create();
		},
		
		addActionEvents : function($html) {
			var self = this;
			$html.find(".downloadreport").on("click", function() {
				var id = $(this).attr("id");
				var arr = id.split("_");
				self.downloadActionClicked(arr[0], arr[1]);
			});
		},
		
		createActions : function(index) {
			var html = new HtmlCreator();
			html.add("<a id='HTML_" + index + "' href='#' class='downloadreport'>");
			html.add("<span title='HTML' class='glyphicon glyphicon-globe viewbutton'></span></a>");
			html.add("|");
			
			html.add("<a id='PDF_" + index + "' href='#' class='downloadreport'>");
			html.add("<span title='PDF' class='glyphicon glyphicon-file viewbutton'></span></a>");
			html.add("|");
			
			html.add("<a id='EXEL_" + index + "' href='#' class='downloadreport'>");
			html.add("<span title='Excel' class='glyphicon glyphicon-th-list viewbutton'></span></a>");
			
			return html.create();
		},
		
		createErrorHtml : function(msg) {
			var html = [];
			html[html.length] = "<div class='alert alert-danger'>";
			html[html.length] = msg;
			html[html.length] = "</div>";
			return html.join("");
		},
		
		/**
		 * Step 3: Option clicked on now find if there are parameters
		 */
		downloadActionClicked : function(type, index) {
			this.reportType = type;
			this.reportIndex = index;
			
			//Load parameters with 
			var name = jQuery("<div />").text(this.reports[index]).html();
			sendAsyncAjax(this.reportsUrl, "act=parms&name=" + name, $.proxy(this.gotReportParametersSuccess, this), $.proxy(this.gotReportParametersFailure));
		},
		
		gotReportParametersSuccess : function(data) {
			
			if (data.fields.length == 0) {
				this.reportFields = null;
				this.parametersAvailable = false;
				this.modalOkPressed();
			} else {
				this.reportFields = data.fields;
				var html = null;
				this.modalLoadingContent("Loading Report Parameters");
				this.showModal();
				
				this.parametersAvailable = true;
				this.showModal();
				var mc = new ModalContentCreator();
				for(var i=0; i<data.fields.length; i++) {
					mc.addElement(data.fields[i]);
				}
				html = mc.create();
				
				this.updateModalContent(html);
				this.refreshPeriodCusomHiding(data.fields);
			}
			
		},
		
		gotReportParametersFailure : function(data) {
			console.error("report data failed");
		},
};


//Ready function
$(function() {
	reportController.init();
});

