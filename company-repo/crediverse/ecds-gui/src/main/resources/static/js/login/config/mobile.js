requirejs.config({
	baseUrl:"js/login",
	
	waitSeconds : 30,
	
	paths: {
		"jquery": "../lib/jquery-1.11.3",
		"rsa": "../lib/rsa",
		"jqauth": "app/jqauth",
		"underscore":"../lib/underscore",
		"i18n":"../lib/plugins/i18n",
		"text":"../lib/plugins/text",
		"handlebars":"../lib/handlebars-v4.0.5",
	},
	
	map: {
		"*" : {
			"template/login" : "text!templates/mobile-login-test.handlebars",
			"common/auth" :		"../common/nls/auth"
		}
	},
	
	shim: {
		"rsa": ["jquery"],
		"jqauth": ["rsa", "underscore", "text", "i18n"],
	},
	
});

requirejs.config({
	locale:csLoginLanguage
});
	
require(["jquery", "rsa", "jqauth"],
    function ($) {
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");
		$(document).ajaxSend(function(e,xhr,options) {
			xhr.setRequestHeader(header, token);
		});
		
		// RSA
		rng_seed_time();
	
		$("#content").authController({
			authComplete: function(resp) {
	 			window.location.replace(resp.redirectUrl);
	 		}
	 	});
	}
);