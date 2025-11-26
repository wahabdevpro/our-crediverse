/*
$(document).ready(function() {

	
	//Load system menu
	loadSystemMenu();
	
	if ($("#posible_menu") != null) {
		var menu = $("#posible_menu").val();
		var system = $("#posible_system").val();
		if (system != null && system.length > 0) {
			loadService(system, menu);
		}
	}
	
	
});
 */

$( document ).ready(function() {
	try {
		//Load system menu
		loadSystemMenu();
		
		//Establish page stat
		var pageState = History.getState();
		
		// Log Initial State
//		History.log('initial:', State.data, State.title, State.url);
		
		
		// Bind to State Change
		History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate
			// Log the State
			pageState = History.getState(); // Note: We are using History.getState() instead of event.state
//			History.log('statechange:', State.data, State.title, State.url);
		});
		
	} catch(err) {
		alert("error on page laoder: " + err);
	}
	
// ------------------------------------------------------------------------------------------------------------------	
	
	try {
		// Establish Variables
//		var
//			State = History.getState(),
//			$log = $('#log');
//
//		// Log Initial State
//		History.log('initial:', State.data, State.title, State.url);
//
//		// Bind to State Change
//		History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate
//			// Log the State
//			var State = History.getState(); // Note: We are using History.getState() instead of event.state
//			History.log('statechange:', State.data, State.title, State.url);
//		});

		// Test
		// Prepare Buttons
		var
			buttons = document.getElementById('buttons'),
			scripts = [
			    'History.pushState({state:"/page1",rand:Math.random()}, "Page 1", "/page1");',
			    'History.pushState({state:"/page1",rand:Math.random()}, "Page 2", "/page2");',
				'History.pushState(null, null, "?state=4"); // logs {}, "", "?state=4"',
				'History.back(); // logs {state:3}, "State 3", "?state=3"',
				'History.back(); // logs {state:1}, "State 1", "?state=1"',
				'History.back(); // logs {}, "The page you started at", "?"',
				'History.go(2); // logs {state:3}, "State 3", "?state=3"'
			],
			buttonsHTML = '';

		// Add Buttons
		for ( var i=0,n=scripts.length; i<n; ++i ) {
			var _script = scripts[i];
			buttonsHTML +=
				'<li><button onclick=\'javascript:'+_script+'\'>'+_script+'</button></li>';
		}
		buttons.innerHTML = buttonsHTML;
	} catch(err) {
		alert(err);
	}
});

