$(document).ready(function() {
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
});