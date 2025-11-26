var currentPageState = null;

var SESSION_LIFE = 1140000;	//19mins
var SESSION_CLOCK = 60;
var sessionTimer;

var serviceOptions = null;

var hashIDStore =  {
	dict : {},
	addItem : function(id, data) {
		this.dict[id] = data;
	},
	retrieve : function(id) {
		return this.dict[id];
	}
};

var changePageTitle = function() {
	try {
		// Update Page Title
		var loc =window.location.pathname.split('/');
		loc = loc[loc.length - 1].replace(/_/g, ' ');
		loc = (loc == "")? "Menu" : (loc == "manusers")? "Manage Users" : (loc =="manroles")? "Manage Roles" : loc;
		loc = (loc == "logview")? "Log Viewer" : (loc == "alarmview")? "Alarm Viewer" : (loc == "fitness")? "System Fitness" : (loc == "reportview")? "Reports" : loc;
		loc = (loc == "mydetails")? "User Details" : loc;
		document.title = "Credit4U - " + loc;
	} catch(err) {}
};

$( document ).ready(function() {
	changePageTitle();
	try {
		//document.title = (loc.length == 0)? ;
		
		getSessionTimeoutMinutes();
		
		//Load system menu
		loadSystemMenu();
		
		//Establish page stat
//		currentPageState = History.getState();
		
//		if ($("#posible_menu") != null) {
//			var menu = $("#posible_menu").val();
//			var system = $("#posible_system").val();
//			if (system != null && system.length > 0) {
//				loadService(menu, system);
//			}
//		}
		
		// Bind to State Change
//		History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate
//			var refreshState = History.getState();
//			//check the old state compared to the new state
//			
//			//TODO: THIS NEEDS A SERIOUS UPDATE
////			if (refreshState.system != currentPageState ||  refreshState.menu != menu) {
////				loadService(refreshState.menu, refreshState.system);
////			}
//			
//			// Bind the new state
//			currentPageState = History.getState(); // Note: We are using History.getState() instead of event.state
//		});
		resetSessionTimeout();
	} catch(err) {
		alert("error on page laoder: " + err);
	}

});

serializeForm = function(formid) {
	var requestData = $("#" + formid + " :input[type != checkbox]").serialize();
	var chkbs = [];
	$("#" + formid + ' input[type=checkbox]').each(function () {
		try {
			if (chkbs.length > 0) {
				chkbs[chkbs.length] = "&";
			}
			chkbs[chkbs.length] = $(this).attr("name");
			chkbs[chkbs.length] = "=";
			chkbs[chkbs.length] = (this.checked ? "true" : "false");
		} catch(err) {
			console.log("serializeForm: " + err)
		}
	});
	var cbstring = chkbs.join("");
	if (cbstring != null && cbstring.length > 0) {
		requestData += ((requestData.length > 0)? "&" : "")  + cbstring;
	}
	return requestData;
};

//---------------------------------------------------------
// Register tab that was clicked and store

registerTabEventListener = function() {
	$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
		try {
			var target = $(e.target).attr("href");
			var className = $(e.target).attr("class");
			var dataurl = "";
			if ((typeof className !== "undefined" && className != null) && (className == "toptab")) {
				dataurl = "nav=" + target.substring(1);
			} else {
				dataurl = "con=" + target.substring(1);
			}
			sendAsyncAjax("/nav", dataurl, function(data) {}, function(error) {});
		} catch(err) {
//			console.log(err);
		}
	});
};

clearLastTabEvent = function() {
	try {
		sendAsyncAjax("/nav", "clr=1", function(data) {}, function(error) {});
	} catch(err) {
	}
};

loadAndshowLastTab = function() {
	sendAsyncAjax("/nav", "ret=1", function(data) {
		if (typeof data.nav !== 'undefined' && data.nav != null && data.nav.length > 0) {
			showTab(data.nav);
//			$('[href=#'+data.nav+']').tab('show');
		}
		if (typeof data.con !== 'undefined' && data.con != null && data.con.length > 0) {
			showTab(data.con);
//			$('[href=#'+data.con+']').tab('show');
		}		
	}, function(error) {});
};

showTab = function(href) {
	try {
		
		$('[href=#'+href+']').tab('show');
	} catch(err) {
		console.error("Error showing tab: " + err);
	}
};
//---------------------------------------------------------


getSessionTimeoutMinutes = function() {
	$.ajax({
		url: "/sessioninfo",
		async: true,
		dataType: "json",
		type: "post",
		success: function(data, textStatus, jqXHR) {
			if (data.status == "pass") {
				var itime = parseInt(data.message);
				SESSION_LIFE = (itime - 2) * 60 * 1000;
				resetSessionTimeout();
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
		}
	});
};

loadService = function(menu, referrer) {
	try {
		if (typeof referrer !== "undefined" && referrer != null)
		{
			if (menu!=null) {
				changeMenuHighlight(menu);	
			}
			var esc = escape(referrer);
			$("#main_content").load("/service?referrer="+esc, function() {
				changePageTitle();
			});
		}		
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
var loadMenuCount = 0;
var loadMenuTimeout = 5;
//<li><a href='#' onclick=\"javascript:navigateTo('sysconf','"+system+"');return false;\">" + path.getName() + "</a></li>
buildSystemMenu = function(menu) {
	var html = [];
	for(var i=0; i < menu.length; i++) {
		html[html.length] = "<li>";
		html[html.length] = "<a href='#' onclick=\"javascript:navigateTo('sysconf','";
		html[html.length] = menu[i].sys;
		html[html.length] = "');return false;\">";
		html[html.length] = menu[i].name;
		html[html.length] = "</a>";
		html[html.length] = "</li>";
	}
	return html.join("");
};

showBAM = function() {
	$("#main_content").html(createLoadingIconHtml("Loading BAM..."));
	$("#main_content").load("/bammon");
};

showFrontPage = function() {
	$("#main_content").html(createLoadingIconHtml("Loading content..."));
	$("#main_content").load("/fpc");
}

loadSystemMenu = function() {
	var menu = $("#posible_menu").val();
	var system = $("#posible_system").val();

	$("#main_content").html(createLoadingIconHtml("Building system configuration menu..."));
	
	sendAsyncAjax("/loadmenu", "", function(data) {
		//message came back
		if (data.status=="pass") {
			serviceOptions = data.menu;
			var html = buildSystemMenu(data.menu);
			$("#system_menu").html(html);
			$("#main_content").html("");
			
			if (system != null && system.length > 0) {
				loadService(menu, system);
			} else {
				showFrontPage();
			}

		} else {
			loadMenuCount++;
			if (loadMenuCount < loadMenuTimeout) {
				loadSystemMenu();
			}
		}
	}, function(data) {
		loadMenuCount++;
		//fail (mesage did not come back)
		if (loadMenuCount < loadMenuTimeout) {
			loadSystemMenu();
		}
	});
	
};

navigateTo = function(newmenu, newsystem, ignoreCheck) {
	if (typeof ignoreCheck == 'undefined') {
		if (typeof configDataToBeSaved !== 'undefined' && configDataToBeSaved) {
			mustInavigateAway(null, null, newmenu, newsystem);
			return;
		} else if (typeof bamCloseCheck !== 'undefined' && bamCloseCheck) {
			beforeClosingNavigateAway(newmenu, newsystem);
			return;
		}
	}
	
	try {
		if (newmenu =='logout') {
			window.location = "/logout";
			return;
		}
		
		if (currentPageState==null || (currentPageState.menu != newmenu && currentPageState.system != newsystem)) {
			var page = "/" + newmenu + "/" + newsystem;
			
			// Update page history
			History.pushState({system: newsystem, menu:newmenu, rand:Math.random()}, "Credit4U", page);

			//load page content
			loadService(newmenu, newsystem);
		}		
	} catch(err) {
		alert("Naviagation error: " + err);
	}
};

//Load Service Configuration menu item
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


//For page timeout
var timeoutDialog = null;

resetSessionTimeout = function() {
	clearTimeout(sessionTimer);
	sessionTimer = setInterval(preSessionTimeout, SESSION_LIFE);
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

preSessionTimeout = function() {
	clearTimeout(sessionTimer);
	sendAjax("/session?act=check", "", preSessionTimeoutCallback, resetAndLogout);
};

preSessionTimeoutCallback = function(data) {
	if (data.valid) 
		sessionTimeout();
	else
		 window.location = "/logout";
};

resetAndLogout = function(data) {
	clearTimeout(sessionTimer)
	window.location = "/logout";
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
			        	  //Keep logged in
			        	  sendAjax("/session", "", function(data) {
			        		  if (!data.valid) {
			        			  window.location = "/logout";
			        		  } else {
			        			  resetSessionTimeout();
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

sendAjax = function(url, postdata, callback, errorCallback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: false,
		dataType: "json",
		data: postdata
	}).done(callback).fail(errorCallback);
};

sendAsyncAjax = function(url, postdata, callback, errorCallback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		dataType: "json",
		data: postdata
	}).done(callback).fail(errorCallback);
};

createLoadingIconHtml = function(msg) {
	var loadMsg = "Loading Content...";
	if ((typeof msg !== 'undefined') && (msg!=null)) {
		loadMsg = msg;
	}
	
	var html = []
	html[html.length] = "<div style='text-align:center;'>";
	html[html.length] = "<img src='/img/load.gif'/>";
	html[html.length] = "&nbsp;";
	html[html.length] = loadMsg
	html[html.length] = "</div>";
	return html.join("");
};

logJsError = function(error) {
	try{
		console.error(error);
	} catch(err){
	}
};

updateServiceConfgurationOptions = function() {
	try {
		if (serviceOptions == null || serviceOptions.length==0) {
			$("#serviceconfigavail").html("No Configuration avilable to configure");
		} else {
			var html = [];
			for(var i=0; i<serviceOptions.length; i++) {
				html[html.length] = "<h5 class='page-ref'>";
				html[html.length] = "<a href='#' onclick=\"javascript:navigateTo('sysconf','";
				html[html.length] = serviceOptions[i].sys;
				html[html.length] = "');return false;\">";
				html[html.length] = serviceOptions[i].name;
				html[html.length] = "</a>";
				html[html.length] = "</h5>";
				
			}
			$("#serviceconfigavail").html(html.join(""));
		}
	} catch(err) {
		console.error("updateServiceConfgurationOptions: " + error);
	}
};

var tableInfo = [];

var updateDataTable = function(tableId, reset) {
	var table = null;
	try {
		var tob = tableInfo[tableId];
		if ((typeof reset !== "undefined" && reset == true) || (typeof tob === "undefined") || (tob == null)) {
			tob = {
					showLength : 25,
					search : ""
			};
			tableInfo[tableId] = tob;
		}
		
		table = $("#" + tableId).DataTable({
			"aoColumnDefs": [ { 'bSortable': false, aTargets: [ -1 ] } ],
			"pageLength" : tob.showLength
		});


		table.search(tob.search).draw();
		$("#" + tableId).on("length.dt", function( e, settings, len ) {
			var tid = this.id;
			var obj = {
					showLength : 25,
					search : ""
			};
			if (typeof tableInfo[tid] !== "undefined" && tableInfo[tid] != null) {
				obj = tableInfo[tid];
			}
			obj.showLength = len;
			tableInfo[tid] = obj;
		});
		

		$("#" + tableId).on("search.dt", function() {
			var tid = this.id;
			var searchId = tid + "_filter";
			var value = $("#" + searchId).find("input").val();
			var obj = {
					showLength : 25,
					search : ""
			};
			
			if (typeof tableInfo[tid] !== "undefined" && tableInfo[tid] != null) {
				obj = tableInfo[tid];
			}
			obj.search = value;
			tableInfo[tid] = obj;
		});
	} catch(err) {
		console.log("updateDataTable error: " + err + " for table: " + tableId);
	}

	return table; 
};

var bindDragable = function(parentModalId, headerId) {
	try {
		var dragMe = document.getElementById( parentModalId );
		var withMe = document.getElementById( headerId );
		var dragRef = DragDrop.bind(dragMe, {
			anchor: withMe
		});
	} catch(err) {
		if (console) console.error("bindDragable: " + err);
	}
};