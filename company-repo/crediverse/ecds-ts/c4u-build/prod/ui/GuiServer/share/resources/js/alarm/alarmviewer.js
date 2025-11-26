/**
 * 
 */

var interval = 1;
var currentId = -1;
var sortAlpha, sortPrior;

// Poll when ready
$(document).ready(function() {
	poll(true);
});

// Constructor for the alarm
function Alarm(id, name, description, severity, state, timestamp) {
	
	// Alarm properties
	this.id = id;
	this.name = name;
	this.description = description;
	this.severity = severity;
	this.state = state;
	this.timestamp = timestamp;
	
	// Method to convert the object to html
	this.toString = function() {
		// Create an array
		var item = [];
		
		// Add the information of the alarm
		item[item.length] = "<div class=\"span2\" ontablet=\"span4\" ondesktop=\"span2\">";
		item[item.length] = "<div class=\"alarm-item alarm-background\">";
		item[item.length] = "<div class=\"header\">" + this.name + "</div>";
		item[item.length] = "<a id=\"alarm-button\" onclick=\"javascript:showModal(" + this.id + ");\">";
		item[item.length] = "<img src=\"/img/alarms/lg" + this.severity.toLowerCase() + ".png\"/>";
		item[item.length] = "</a>";
		item[item.length] = "<div class=\"footer\">";
		item[item.length] = "<span class=\"heading\">Severity</span>";
		item[item.length] = "<span class=\"text\">" + this.severity.toLowerCase() + "</span>";
		item[item.length] = "</div>";
		item[item.length] = "</div>";
		item[item.length] = "</div>";
		
		return item.join(" ");
	}
}

// Polls the backend for information on the alarms
function poll(recursive) {
	
	// Poll at certain intervals
	setTimeout(function() {
		
		// Send ajax to get the information
		$.ajax({
			type : "POST",
			url : "/alarmview",
			async : true,
			data : "act=poll&host=" + $("#hosts").val(),
			dataType : "json",
			success: function(data) {			
				
				// Ensure the data is valid
				if (data == null) {
					$("#alarm_content").html("Cannot connect to host.");
					return;
				}
				
				// Set the available hosts
				if (data.hosts != null && data.hosts.length) {
					$("#hosts").find("option").remove();
					$.each(data.hosts, function() {
						$("#hosts").append($("<option />").val(this).text(this));
					});
				}
				
				// Get the alarms from the data
				alarms = loadAlarms(data.indications);
				
				// Check if the alarms is more than one
				if (alarms.length > 0)
					// Then add them to the content
					$("#alarm_content").html(print(alarms));
				
				// Update the modal according to the current alarm
				if (currentId > -1) {
					updateModal(alarms[currentId]);
				}
			},
			complete: function() {
				
				// Check if the alarm content still exists
				if ($('#alarm_content').length && recursive) {
					
					// Set the interval
					if (interval < 1000)
						interval = 5000;
					
					// Recursively execute the poll method
					poll(recursive);
				}
			}
		});
	}, interval);
}

// Creates an array of alarms
function loadAlarms(alarms) {
	
	// Create the results array
	var res = [];
	
	// Iterate through the data and add the alarms
	for (var i = 0; i < alarms.length; i++) {
		// Create the alarm
		res[res.length] = new Alarm(i, alarms[i][0], alarms[i][1], alarms[i][2], alarms[i][3], alarms[i][4]);
	}
	
	// Sore the result according to the filters
	res.sort(function(a, b) {
		
		// Sort according to the name of the alarm
		if (sortAlpha) {
			if (a.name.toLowerCase() < b.name.toLowerCase()) return -1;
			if (a.name.toLowerCase() > b.name.toLowerCase()) return 1;
			return 0;
		// Else sort according to the priority
		} else if (sortPrior) {
			if (a.severity > b.severity) return -1;
			if (a.severity < b.severity) return 1;
			return 0;
		}
	});
	
	// Check if there are any dangerous alarms
	var error = false;
	for (var i =0; i < res.length; i++) {
		res[i].id = i;
		
		if (res[i].severity.toLowerCase() != 'clear') {
			error = true;
		}
	}
	
	// If so, then set the panel to red
	if (error) {
		$("#alarm_panel").removeClass("panel-default");
		$("#alarm_panel").addClass("panel-danger");
	// Else set the panel to normal
	} else {
		$("#alarm_panel").removeClass("panel-danger");
		$("#alarm_panel").addClass("panel-default");
	}
	
	// Return the result
	return res;
}

// Updates and prints the alarms
function updateAlarms(alarms) {
	print(loadAlarms(alarms));
}

// Creates html of all the alarms
function print(alarms) {
	var html = [];
	for (var i = 0; i < alarms.length; i++) {
		html[html.length] = alarms[i];
	}
	return html.join("");
}

// Displays the modal with the alarm
function showModal(alarmId) {
	
	// Set the current alarm
	currentId = alarmId;
	
	// Set the alarm
	var alarm = alarms[alarmId];
	
	// Update the modal with the alarm
	updateModal(alarm);
	
	// Show the modal
	$("#alarm-modal").modal("show");
}

// Updates the modal page with the alarm information
function updateModal(alarm) {
	$("#alarm-modal-name").html(alarm.name);
	$("#alarm-modal-description").html(alarm.description);
	$("#alarm-modal-severity").html(alarm.severity);
	$("#alarm-modal-state").html(alarm.state);
	$("#alarm-modal-timestamp").html(alarm.timestamp);
	$("#alarm-modal-img").html("<img src=\"/img/alarms/lg" + alarm.severity.toLowerCase() + ".png\"/>");
}

// Sort alphabetically
$("#sort-alpha").click(function() {
	sortAlpha = true;
	sortPrior = false;
	interval = 1;
	poll(false);
});

// Sort priority
$("#sort-prior").click(function() {
	sortAlpha = false;
	sortPrior = true;
	interval = 1;
	poll(false);
});

// Sort in natural order
$("#sort-natur").click(function() {
	sortAlpha = false;
	sortPrior = false;
	interval = 1;
	poll(false);
});

// Change the host
$("#hosts").change(function() {
	interval = 1;
	poll(false);
});
