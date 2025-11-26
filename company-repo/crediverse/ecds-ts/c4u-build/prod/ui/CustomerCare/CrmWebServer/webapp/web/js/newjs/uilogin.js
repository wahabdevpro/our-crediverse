var soapfail = false;

$(document).ready(function() {
	try {
		getContentAjax("/login", "act=localize&lang=en", updateLocalization);
	} catch(err) {}
	
	try {
		if (/MSIE\s([\d.]+)/.test(navigator.userAgent)) {
		    //Get the IE version.  This will be 6 for IE6, 7 for IE7, etc...
		    version = new Number(RegExp.$1);
		    if (version < 9) {
				$('input, textarea').placeholder();	
		    }
		}
	} catch(err){
		//Error will be thrown if placeholder library not loaded
	}
	
	$(".selectpicker").selectpicker();
	
	$( "#signin" ).click(function() {
		loginUser();
	});
	
	$(".selectpicker").on("change", function() {
		var lang = $(this).val();
		getContentAjax("/login", "act=localize&lang=" + lang, updateLocalization);
	});
});

$(document).keypress(function(e) {

  if(e.which == 13) {
	  loginUser();
  }
});

function jDecode(str) {
	var decoded = $("<div/>").html(str).text();
	decoded = decoded.replace("&apos;", "'");
    return decoded;
}

/**
 * Language Change
 */
updateLocalization = function(content) {
	try {
		$("head").append(content);
		$("#signinbanner").text(jDecode(languagesInfo.message));
		$("#user").attr("placeholder", jDecode(languagesInfo.username));
		$("#pass").attr("placeholder", jDecode(languagesInfo.password));
		$("#signin").text(jDecode(languagesInfo.signin));
		if (soapfail)
			$("#loginfailmessage").html(jDecode(languagesInfo.soapfail));
		else 
			$("#loginfailmessage").html(jDecode(languagesInfo.fail));
		$("#language").html(jDecode(languagesInfo.language));
	} catch(err) {
		alert(err);
	}
};

loginUser = function() {
	try 
	{
		var error = $("#user").val().length == 0 || $("#pass").val().length == 0;
		
		if (!error) 
		{
			//Set up and retrieve dialog form data
			var requestData = $('#loginForm').serialize();
			var dataurl = "act=login&" + requestData;
			sendAjaxReceiveJson("/login", dataurl, updatePage);
		}
	} 
	catch(err)
	{
		alert("loginUser: " + err);
	}
};

updatePage = function(data)
{
	try 
	{
		if (data.status == "fail") {
			if (typeof data.message !== "undefined" && data.message == "soapfail") {
				soapfail = true;
				$("#loginfailmessage").html(jDecode(languagesInfo.soapfail));
			} else {
				soapfail = false;
				$("#loginfailmessage").html(jDecode(languagesInfo.fail));
			}
			$(".loginfail").removeClass("hide");
		} else {
			window.location.replace('/');
		}
	} 
	catch(err)
	{
		alert("updatePage: " +err);
	}
};

sendAjax = function(url, postdata, callback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		cache: false,
		dataType: "json",
		data: postdata
	}).done(callback);
};

getContentAjax = function(url, postdata, callback) {
	var status = $.ajax({
		type: "POST", 
		url: url, 
		async: true,
		data: postdata
	}).done( callback );
};
