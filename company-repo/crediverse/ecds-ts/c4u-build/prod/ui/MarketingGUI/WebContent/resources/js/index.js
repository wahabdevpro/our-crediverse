
var context = "/c4u";

// IP Information
var ip = null;

//Assign the HTML, Body as a variable...
var $viewport = $('html, body');

// Document Ready
$(function() {
	
	// Get IP Information	
	$.ajax({
		
		type: "GET",
		dataType: "json",
		url: "http://ipinfo.io/json",
		async: true
		
	}).success(function (response) {
		
		ip = response.ip;
		
		// Call the visitor request to add it to the database
		$.ajax({
			
			type: "POST",
			url: context + "/visitor",
			data: {
				
				ip: ip,
				country: response.country,
				region: response.region,
				city: response.city,
				postal: response.postal,
				location: response.loc,
				isp: response.org
				
			},
			async: true
			
		});
		
	});
	
	// jQuery Scrolling
    $("a.page-scroll").bind("click", function(event) {
        var $anchor = $(this);
        $viewport.stop().animate({
            scrollTop: $($anchor.attr("href")).offset().top
        }, 1500, "easeInOutExpo");
        event.preventDefault();
    });
    
    // Configure toastr plugin
    toastr.options.newestOnTop = false;
    toastr.options.closeButton = true;
    toastr.options.showEasing = "easeOutBounce";
    toastr.options.timeOut = 0;
    toastr.options.extendedTimeOut = 0;
    toastr.options.positionClass = "toast-bottom-right";
    toastr.options.showMethod = "slideDown";
    toastr.options.tapToDismiss = false;
    
    // Load session
    checkSession();
    
    // Check if it is safari
    if (isSafari()) {
    	
    	// Warn the user
    	$("#warning_message").html("WARNING: Browser is unsupported. You may experience some issues.");
    	
    	// Animate it sliding up
    	$("#warning_message").animate({
    		
    		height: 20
    		
    	}, 1000, "easeInOutExpo");
    	
    }
    
});

// Checks the user Agent of the browser for Safari
function isSafari() {
    return /^((?!chrome).)*safari/i.test(navigator.userAgent);
}

//Stop the animation if the user scrolls. Defaults on .stop() should be fine
$viewport.bind("scroll mousedown DOMMouseScroll mousewheel keyup", function(e){
    if ( e.which > 0 || e.type === "mousedown" || e.type === "mousewheel"){
         $viewport.stop(); // This identifies the scroll as a user action, stops the animation, then unbinds the event straight after (optional)
    }
});

// Once loaded, then load the recaptcha component
var recaptchaResponse = null;
var onloadCallback = function() {
	
	// Check if the captcha component is on the browser
	$("#livedemo-items").find("#captcha-goes-here").attr("id", "recaptcha");
	try {
		
		// Call the render command to render the recaptcha
		grecaptcha.render("recaptcha", {
	        'sitekey' : '6LfVAQcTAAAAAOLrYzuFbFdvF7jrSBCQtB9wfeu_',
	        'theme' : 'dark',
	        "callback": function(response) {
	        	recaptchaResponse = response;
	        }
	    });
	} catch (e) {
		
	}
	
}

// Check the session of the current user browsing
checkSession = function() {
	
	// Call the user request
	$.ajax({
		
		type: "POST",
		url: context + "/user",
		async: true
		
	}).success(function(user) {
		
		// Check the status
		if (user.status == 0) {
			
			// Log the user in
			login(user);
			
		} else {
			
			// Else log the user out
			logout();
			
		}
		
	});
		
}

// Highlight the top nav as scrolling occurs
$("body").scrollspy({
    target: ".navbar-fixed-top"
})

// Closes the Responsive Menu on Menu Item Click
$(".navbar-collapse ul li a").click(function() {
    console.log($(this).parent());
	if (!$($(this).parent()).hasClass("dropdown"))
		$(".navbar-toggle:visible").click();
});

// Create the popover for the sign in
$("#sign-in-link").popover({
    html : true, 
    content: function() {
      return $('#sign-in-popover').html();
    },
    placement: "bottom",
    container: 'body'
});

// When the sign in link is opened
$("#sign-in-link").click(function() {
	
	// Assign a click function to the one hyper link
	$("a.page-scroll.need-account").bind("click", function(event) {
		
		// Make it scroll to the registration form if clicked
        var $anchor = $(this);
        $viewport.stop().animate({
            scrollTop: $($anchor.attr("href")).offset().top
        }, 1500, "easeInOutExpo");
        event.preventDefault();
        
        // Hide the popover
        $("#sign-in-link").popover("toggle");
    });
	
});

// Logout the user if clicked
$("#account-logout").click(function() {
	
	// Logs the user out
	logout();
	
});

// Allows the popover to be toggled
$('[data-toggle="popover"]').popover();

$('body').on('click', function (e) {
  $('[data-toggle="popover"]').each(function () {
    //the 'is' for buttons that trigger popups
    //the 'has' for icons within a button that triggers a popup
    if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
      $(this).popover('hide');
    }
  });
});

// Create a bounce effect
var bounce = new Bounce();
bounce
  .scale({
    from: { x: 1, y: 1 },
    to: { x: 1.1, y: 1.1 },
    easing: "bounce",
    duration: 800,
    delay: 65,
    stiffness: 1
  });

// Assign the bounce affect
bounce.define("bounce-effect");

// When hovered over the image, activate the bounce effect
$(".fa-stack").hover(function() {
	
	// Apply the effect
	bounce.applyTo($(this), { loop: true });
	
}, function() {
	
	// Remove the style when not hovering
	$(this).attr("style", "");
	
});

// ".autoxfr-popup, .crshr-popup, .re2u-popup"
$("#more-information").magnificPopup({
	disableOn: 700,
	type: "iframe",
	mainClass: "mfp-fade",
	removalDelay: 160,
	preloader: false,

	fixedContentPos: false,
	
	callbacks: {
		
		close: function() {
			
			// Closes the popup and scrolls to the services section
			$viewport.stop().animate({
		        scrollTop: $("#services").offset().top
		    }, 1500, "easeInOutExpo");
			
		}
		
	}
});

// Assigns the popup to the class
$('.popup-with-zoom-anim').magnificPopup({
	type: 'inline',

	fixedContentPos: false,
	fixedBgPos: true,

	overflowY: 'auto',

	closeBtnInside: true,
	preloader: false,
	
	midClick: true,
	removalDelay: 300,
	mainClass: 'my-mfp-zoom-in'
});

// The sign in function to log the user in
signIn = function(event, form) {
	
	// Prevent the submit from changing the page
	if (event.preventDefault) {
		event.preventDefault();
	} else {
		// For IE
		event.returnValue = false;
	}
	
	// Get the form group
	var formGroup = $($($(form).parent()).parent()).parent();
	
	// Get the status bar
	var status = $(formGroup).find("#status");
	
	// Validate the form
	if (!validateForm(formGroup)) {
		return false;
	}
	
	// Get the email
	var email = $(formGroup).find("#sign-in-email").val();
	
	// Get the password
	var password = $(formGroup).find("#sign-in-password").val();
	
	// Authenticate with the email and password
	$.ajax({
		
		type: "POST",
		url: context + "/authenticate",
		data: {
			email: email,
			password: password,
		},
		async: true,
		
	}).success(function(result) {
		
		// Successful
		if (result.status == 0) {
			
			login(result);
		
		// User Authentication Failed
		} else if (result.status == 1) {
			
			alert(status, "danger", "Your details seem to incorrect.");
			
		// User Not Active
		} else if (result.status == 2) {
			
			alert(status, "warning", "Your account has not been activated yet. If urgent please contact us below.");
			
		}
		
	});
	
	// Prevent form from reloading
	return false;
}

// Registers the user into the database
register = function(event, form) {
	
	// Prevent the submit from changing the page
	if (event.preventDefault) {
		event.preventDefault();
	} else {
		event.returnValue = false;
	}
	
	// Get the form group
	var formGroup = $($($(form).parent()).parent()).parent();
	
	// Get the status bar
	var status = $(formGroup).find("#status");
	
	// Validate the form
	if (!validateForm(formGroup)) {
		
		// Reset the captcha
		grecaptcha.reset();
		return false;
		
	}
	
	// Get the email
	var email = $(formGroup).find("#register-email").val();
	
	// Get the organisation
	var organisation = $(formGroup).find("#register-organisation").val();
	
	// Get the password
	var password = $(formGroup).find("#register-password").val();
	
	// Authenticate
	$.ajax({
		
		type: "POST",
		url: context + "/register",
		data: {
			email: email,
			organisation: organisation,
			password: password,
			ip: ip,
			recaptcha: recaptchaResponse
		},
		async: true,
		
	}).success(function(result) {
		
		// Success
		if (result == 0) {
			
			alert(status, "success", "You have been registered! Please be patient as we validate your account, which may take up to 2 days.");
			
		// Failed to Verify
		} else if (result == 1) {
			
			alert(status, "danger", "Are you human?");
			
		// Organisation Already Exists
		} else if (result == 2) {
			
			alert(status, "warning", "That email address already exists.");
			
		}
		
	}).complete(function() {
		
		// Reset the fields to empty
		$(formGroup).find("#register-email").val("");
		$(formGroup).find("#register-organisation").val("");
		$(formGroup).find("#register-password").val("");
		$(formGroup).find("#register-retype-password").val("");
		
		// Reset the recaptcha
		grecaptcha.reset();
		recaptchaResponse = null;
		
	});
	
	return false;
		
}

// Sends an email from the contact us form
contact = function(event, form) {
	
	// Prevent the submit from changing the page
	if (event.preventDefault) {
		event.preventDefault();
	} else {
		// For IE
		event.returnValue = false;
	}
	
	// Get the form group
	var formGroup = $($($(form).parent()).parent()).parent();
	
	// Get the status bar
	var status = $(formGroup).find("#status");
	
	// Validate the form
	if (!validateForm(formGroup)) {
		
		return false;
		
	}
	
	// Get the name of the person
	var name = $(formGroup).find("#contact-name").val();
	
	// Get the email of the person
	var email = $(formGroup).find("#contact-email").val();
	
	// Get the phone number of the person
	var phone = $(formGroup).find("#contact-phone").val();
	
	// Get the message
	var message = $(formGroup).find("#contact-message").val();
	
	// Try to get the first name of the person
	var firstName = name;
    
	// Try get the name before the first space
    if (firstName.indexOf(' ') >= 0) {
        firstName = name.split(' ').slice(0, -1).join(' ');
    }
	
    // Send the email
	$.ajax({
		
		type: "POST",
		url: context + "/mail",
		data: {
			name: name,
			phone: phone,
			email: email,
			message: message
		},
		async: true
		
	}).success(function(result) {
		
		// Alert that it was successful
		alert(status, "success", "Your message has been sent.");
		
	}).error(function() {
		
		// Alert it was unsuccessful
		alert(status, "danger", "Sorry " + firstName + ", it seems that our mail server is not responding. Please try again later!")
		
	});
	
	return false;
	
}

// Logs the user in
login = function(user) {
	
	// Hide the popover
	$("#sign-in-link").popover("hide");
	
	// Animates the sign in link fading out
	$("#sign-in-link").animate({
		
		opacity: 0.0
		
	}, 500, function() {
		
		// Hide the link
		$("#sign-in-link").css("display", "none");
		
		// Show the account name
		$("#account-name").html(user.name);
		
		// Make it visible by animating the opacity of the account name
		$("#account-dropdown").css("opacity", "0.0");
		$("#account-dropdown").css("display", "block");
		$("#account-dropdown").animate({
			
			opacity: 1.0
			
		}, 500, function() {
			
			// Scroll to the live demos
			$viewport.stop().animate({
	            scrollTop: $("#livedemo").offset().top
	        }, 1500, "easeInOutExpo");
			
		});
		
		
	});
	
	// Load the demo page
	$.ajax({
		
		type: "POST",
		url: context + "/components/livedemos",
		async: true
		
	}).success(function(result) {
		
		// Set the html for the demo items
		$("#livedemo-items").html(result);
		
	});
	
}

// Logs the user out
logout = function() {
	
	// Send the log out request
	$.ajax({
		
		type: "POST",
		url: context + "/logout",
		async: true
		
	});
	
	// Hide the popover
	$("#sign-in-link").popover("hide");
	
	// Show the authentication forms
	$("#livedemo-items").html($("#authentication-forms").html());
	
	// Reload the recaptcha
	onloadCallback();
	
	// Animate the account name to disappear
	$("#account-dropdown").animate({
		
		opacity: 0.0
		
	}, 500, function() {
		
		// Hide the account name
		$("#account-dropdown").css("display", "none");
		$("#account-name").html("Account Name");
		
		// Show the sign in link by animating the opacity
		$("#sign-in-link").css("opacity", "0.0");
		$("#sign-in-link").css("display", "block");
		$("#sign-in-link").animate({
			
			opacity: 1.0
			
		}, 500, function() {
			
			
			
		});
		
	});	
	
}

// Validates any form
validateForm = function(form) {
	
	var passed = true;
	
	var password = null;
	
	try {
		
		// Looks through all the input and text areas
		$(form).find("input,textarea").each(function() {
			
			// Check if there is a required attribute
			var required = $(this).attr("required");
			
			// If it is not, then don't bother
			if (required == undefined || required == null)
				return;
			
			// Get the helper bar
			var helper = $(this).next();
			if (helper == undefined || helper == null)
				helper = $(form).find("#status");
			
			// Get the type for the field
			var type = $(this).attr("type");
			
			// Get the value
			var value = $(this).val();
			
			// Check the value is valid
			if (value == undefined || value == null || value.length == 0) {
				
				// Else show a message asking for the value
				fade(helper, "Please enter in your " + $(this).attr("placeholder"));
				
				passed = false;
				
				return;
			}
			
			// If the type is email, then validate the email
			if (type == "email" && !validateEmail(value)) {
				
				// Else show an erro message
				fade(helper, "Please enter in a valid email address.");
				
				passed = false;
				
				return;
				
			}
			
			// Check if it is a password that must match
			var match = $(this).attr("data-match");
			
			// Check if password
			if (type == "password" && (match != undefined || match != null)) {
				
				// Check if there was a previous password, if so, check that the fields equal
				if (password != value) {
					
					// If not display error message
					fade(helper, "The passwords do not match.");
					
					passed = false;
					
					return;
					
				}
				
			} else if (type == "password") {
				
				// Else just assign the password
				password = value;
				
			}
			
			// Clear the helper
			helper.html("");
			
		});
		
	} catch (e) {
		passed = false;
	}
	
	// Whether the validation passed
	return passed;
	
}

// Validates the email with regex
var validateEmail = function(email) {
	var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,10}(?:\.[a-z]{2})?)$/i;
	return re.test(email);
}

// Helper to create a message
alert = function(element, type, message) {

	$(element).html("<div class='alert alert-" + type + "'><a href='#' class='close' data-dismiss='alert'>&times;</a>" + message + "</div>");
	$(element).animate({ opacity: 1 });
	
	// Fades the message out
	setTimeout(function() {
		
		$(element).animate({ opacity: 0 }, 1000, function() {
			$(element).html("");
		});
		
	}, 3500);
	
}

// Fades the element with a message
fade = function(element, html) {
	$(element).html(html);
	$(element).css("opacity", "1.0");
	setTimeout(function() {
		$(element).animate({ opacity: 0 }, 500, function() {
			$(element).html("");
		});
	}, 2500);
} 
