/**
 *	Responsible for the livedemos.html page
 */

var context = "/c4u";
var user = null;
var terminal1 = null, terminal2 = null;

// If the admin demo is clicked
$("#admin-demo").click(function() {

	// Display the browser for the admin GUI
	$("#admin-modal-demo .modal-content").html("<iframe frameborder='0' id='admin-object' src='http://" + location.hostname + ":8082/login' />");
	
	// Clear the toastr
	toastr.clear();

	// Show the username and password
	toastr.info("administrator", "Username");
	toastr.info("password1", "Password");
	
});

// If the crm demo link is clicked
$("#crm-demo").click(function() {

	// Display the browser for the crm GUI
	$("#crm-modal-demo .modal-content").html("<iframe frameborder='0' src='http://" + location.hostname + ":8083/login' />");
	
	// Clear the toastr
	toastr.clear();

	// Show the username and password for the crm
	toastr.info("crm", "Username");
	toastr.info("crm", "Password");
	
	// Get the user details for session
	$.ajax({
		
		type: "POST",
		url: context + "/user",
		async: true
		
		
	}).success(function(result) {
		
		// Assign the user
		user = result;
		
		// Get the number range
		if (user != null && user.numberRange != null) {
			
			toastr.info(user.numberRange, "Available Msisdn\'s");
			
		} else {
			
			toastr.error("No Msisdn's Available", "Available Msisdn's");
			
		}
		
	});
	
	
	
});

// When the simobi link is clicked
$("#simobi-demo").click(function() {

	// Ensure the terminals have the greeting method
	if (terminal1 != null)
		termina1.greeting();
	
	// Ensure the terminals have the greeting method
	if (terminal2 != null)
		terminal2.greeting();
	
	// Clear the toastr
	toastr.clear();

	// Show certain information
	toastr.error("Coming Soon", "Simobi");
	toastr.info("This is a temporary phone emulator to test the USSD menu for the services.");
	toastr.info("*183#", "Credit Sharing Service");
	toastr.info("*143#", "Automatic Credit Transfer");
	toastr.warning("Please ensure that any pin entered is 4 digits long.");
	
	$("div.cmd").focus();
	
	// Get the user information
	$.ajax({
		
		type: "POST",
		url: context + "/user",
		async: true
		
		
	}).success(function(result) {
		
		// Assign the user
		user = result;
		
		// Set the number of simobi 1
		$("#simobi-number-1").html("Number: " + user.simobiNumber + "1");
		
		// Set the number of simobi 2
		$("#simobi-number-2").html("Number: " + user.simobiNumber + "2");
		
	});
	
});

// Closes the modals
$(".close-modal-button").click(function() {
	
	// Clears the toastr
	toastr.clear();
	
});

// Sends the terminal command
var terminalFunc = function(command, term) {
	
	// Gets the number of the terminal
	var number = user.simobiNumber + (this.name == "simobi-1" ? "1" : "2");
	
	// Get the terminal
	if (terminal1 == null && this.name == "simobi-1") {
		
		terminal1 = term;
		
	}
	
	// Gets the terminal 2
	if (terminal2 == null && this.name == "simobi-2") {
		
		terminal2 = term;
		
		$("#simobi-number-2").html(number);
	
	}
	
	// Check the command is not blank
	if (command !== "") {
		
        try {
        	
        	// Sends the ussd command
        	$.ajax({
        		
        		type: "POST",
        		url: context + "/simobi",
        		data: {number: number, command: command},
        		async: true,
        		
        	}).success(function(result) {
        		
        		// Clears the terminal
        		term.clear();
        		
        		// Echos the result
        		term.echo(result);
        		
        	}).error(function(error) {
        		
        		// Else display the error message
        		term.error(error.statusText);
        		
        	});
        	
        } catch(e) {
            term.error(new String(e));
        }
    } else {
       term.echo("");
    }
	
}

// Set the terminal 1 window
$("#simobi-term-1").terminal(terminalFunc, {
    greetings: "USSD EMULATOR\n"
		 + "=============\n\n"
		 + "Click on the screen to type.\n"
		 + "Press Enter to proceed once\n"
		 + "done typing.",
name: "simobi-1",
height: 200,
prompt: ""
});

// Set the terminal 2 window
$("#simobi-term-2").terminal(terminalFunc, {
    greetings: "USSD EMULATOR\n"
		 + "=============\n\n"
		 + "Click on the screen to type.\n"
		 + "Press Enter to proceed once\n"
		 + "done typing.",
name: "simobi-2",
height: 200,
prompt: ""
});

// If clicked on the tutorial link
$("#tutorialLink").click(function() {
	
	// Iterate through the images and add it to the array
	var items = [];
	for (var i = 0; i < 46; i++) {
		
		items[i] = {
				src: 'resources/img/tutorial/showcase' + (i + 1) + '.png'
		}
		
	}
	
	// Open the popup with the images
	$.magnificPopup.open({
		  items: items,
		  type: 'image',
		  tLoading: 'Loading image #%curr%...',
		  mainClass: 'mfp-fade',
		  gallery: {
			  enabled: true,
			  navigateByImgClick: true,
			  preload: [0,1] // Will preload 0 - before current, and 1
							// after the current image
		  },
		  image: {
			  tError: '<a href="%url%">The image #%curr%</a> could not be loaded.',
			  titleSrc: function(item) {
				  return '<small>by Concurrent Systems</small>';
			  }
		  }
		});
	
});