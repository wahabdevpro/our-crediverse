var currentPageState = null;

var SESSION_LIFE = 720000;	//12 minutes
var SESSION_CLOCK = 60;		//time in sec
var sessionTimer;
var timeoutDialog = null;

var loader = "<div class='content-center'><img src='/img/load.gif'/></div><div class='content-center'><center>Loading Content...</center></div>";
var logging = false;

randomString = function() { 
	var s = Math.random().toString(36).slice(2); return s.length===16 ? s : randomString();
//	return s;
};

jQuery(document).ready(function($)
{
	try {
		//help reference
		$("#helpButton").on("click", function() {
			$('#myHelpModal').modal('show');
			$("#myHelpModal").focus();
		});
		
		resetSessionTimeout();
		//Page content load
		if (typeof content !== "undefined" && content != null && (content.length>0)) {
			loadPageContent(content);
		}
	} catch(err) {
		alert("error on page laoder: " + err);
	}
});

loadPageContent = function(menu, referrer) {
	try {
		$('#myModal').modal({ show: false})
		
		$("#main_content").html("<center><img src='/img/load.gif' /></center>");
		
		if (menu!=null) {
			changeMenuHighlight(menu);	
		}
		var toload = "/" + escape(menu);
		if (typeof referrer !== "undefined" && referrer != null)
		{
			toload += "/" + escape(menu);
		}
		toload += "?act=content";
		//toload += "?rsie=" + randomString();
		$("#main_content").load(toload, function(response, status, xhr) {
			try {
				if (status == "error") {
					 var msg = "Sorry but there was an error: " + xhr.status + " " + xhr.statusText
					 $("#main_content").html("<b>" + msg  + "</b>");
				}
			} catch(err) {
				console.error(err);
			}
		});	
	} catch(err) {
		alert(err);
	}
};

var lastMenu = null;
changeMenuHighlight = function(newMenu) {
	try {
		if (lastMenu!=null) {
			$('#'+lastMenu).removeClass("active");
		}
		$('#'+newMenu).addClass("active");
		lastMenu = newMenu;
	} catch(err) {
		alert(err);
	}
};

navigateTo = function(newmenu, newsystem) {
	
	try {
		if (currentPageState==null || (currentPageState.menu != newmenu)) {
			//Display loader (Should not take long to load but what the heck!)
			showContentloader();
			
			// Update page history
			var page = "/" + newmenu;
			
			if (typeof newsystem !== "undefined" && newsystem != null) {
				page += "/" + newsystem;
			}
			
//			History.pushState({system: newsystem, menu:newmenu, rand:Math.random()}, "Credit4U", page);

			//load page content
			loadPageContent(newmenu, newsystem);
		}		
	} catch(err) {
		alert("Naviagation error: " + err);
	}
};

showContentloader = function()
{
//	$("#main_content").html(loader);
};

//Load Service Configuration menu item
/*
loadConfigItem = function() {
	try {
		if (menu!=null) {
			changeMenuHighlight("config");	
		}
		var esc = escape(referrer);
		$("#main_content").load("/service?referrer="+esc);
		
		var stateObj = { foo: "bar" };	
	} catch(err) {
		alert(err);
	}
};
*/


//For page timeout
resetSessionTimeout = function() {
	clearTimeout(sessionTimer);
	sessionTimer = setInterval(sessionTimeout, SESSION_LIFE);
};

var timeout = 0;
sessionClock = function() {
	timeout--;
	if (timeout > 0) {
		$("#time_remaining").html(timeout);
		//sessionTimer = setInterval(sessionClock, 1000);
	} else {
		clearTimeout(sessionTimer);
		window.location = "/logout";
	}
};

stopSessionClock = function() {
	clearTimeout(sessionTimer);
};

sessionTimeout = function() {
	try {

		var msg = [];
		msg[msg.length] = '<div style="height:60px;"><div style="float:left; width:80px;margin:10px auto; height:50px; padding-left:20px;"><img src="/img/timeout-orange.png"/></div>';
		msg[msg.length] = '<div style="float:left; width:300px; padding-left:30px; padding-top:10px;">';
		msg[msg.length] = '<p>You will be logged out in <b><span id="time_remaining">60</span></b> seconds</p>';
		msg[msg.length] = '<p>Would you like to stay logged in?</p>';
		msg[msg.length] = '</div></div>';
		
		timeout = SESSION_CLOCK;
		clearTimeout(sessionTimer);
		timeoutDialog = BootstrapDialog.show({
	        type: BootstrapDialog.TYPE_DEFAULT,
	        title: "<b>Your session is about to expire!</b>",
	        message: msg.join(""),
	        onhide: function(dialog){
                //Keep user logged in
            },
			buttons: [{
			          label: 'Yes, keep logged in',
			          cssClass: 'btn-primary',
			          action : function(dialog) {
			        	  stopSessionClock();
			        	  //Keep logged in
			        	  sendNoResetAjax("/session", "", function(data) {
			        		  try {
				        		  if (!data.valid) {
				        			  window.location = "/logout";
				        		  } else {
				        			  resetSessionTimeout();
				        		  }  
			        		  } catch(err) {
			        			  console.error(err);
			        		  }
			        		  dialog.close();
			        	  });
			          }
				}, {
			          label: 'No, sign me out',
			          action : function(dialog) {
			        	  //log user out
			        	  window.location = "/logout";
			          }
				}
			]
	    });
		
		sessionTimer = setInterval(sessionClock, 1000);
	} catch(err) {
		alert(err);
	}
};

sendNoResetAjax = function(url, postdata, callback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		dataType: "json",
		data: postdata
	}).done(callback);
};

sendAjax = function(url, postdata, callback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		dataType: "json",
		cache: false,
		data: postdata
	}).done( function(data) {
		resetSessionTimeout();
		callback(data);		
		}
	);
};

sendContentAjax = function(url, postdata, callback) {
	if (logging) {
		console.log("send called...");
		console.log(postdata);
	}
	
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		data: postdata,
		cache: false
	}).done( function(content) {
		if (logging) {
			console.log("Content: ");
			console.log(content);
		}
		resetSessionTimeout();
		callback(content);		
		
	}).fail( function(xhr, status, msg) {
		console.error("URL: " + url + " post: " + postdata);
		console.error("status: " + status + " msg: " + msg);
		console.error(xhr);
	});
};

