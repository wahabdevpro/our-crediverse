var bamMonitor = null;

//registerAddMetricSelectEvents = function(type) {
////	bamMonitor.registerDialogMetricEvent(type);			
//};

$(document).ready(function() {
	try
	{		
		bamMonitor = $("#content").bamMonitor({
			addButtonId: "addbamwidget",
		});
		
		bamMonitor.simpleTest();
	} catch(err) {
		console.error(err);
	}
});



