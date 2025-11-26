$(document).ready(function() {
	
	//General Initialization
	try {
		initializeAdvParameters("/genericconfig");
		initMessaging();
		
		//Build page common objects
		buildTabContents();
	} catch(err) {
		alert("page error: " + err);
	}
	
	//Return Code Texts ?!?
	
	//Vas Commands ?!?
	
	
});