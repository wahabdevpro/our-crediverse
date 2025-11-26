//Includes Desktop Specific JavaScript files here (or inside of your Desktop router)
define('jquery', [], function() {
    return jQuery;
});

define('bootstrap', [], function() {
    return jQuery;
});

define('datatables.net', [], function() {
    return jQuery;
});

define('datatables.net-bs', [], function() {
    return jQuery;
});

define('datatables.net-responsive', [], function() {
    return jQuery;
});

define('DataTable.Responsive', [], function() {
    return jQuery;
});


require(["jquery", "underscore", "App", "routers/AppRouter", 'views/DialogView', "utils/PreloadPartials", "handlebars", "handlebars-intl", "utils/HandlebarHelpers", "config/TemplateEngine", "adminguilte"],
    function ($, _, App, AppRouter, DialogView, PreloadPartials, Handlebars, HandlebarsIntl, HBHelper) {
		
		// This corrects Select2 Behaviour on certain browsers
		$.fn.modal.Constructor.prototype.enforceFocus = function () {};
	
		HandlebarsIntl.registerWith(Handlebars);
		PreloadPartials.initialize();
		
    	// Register Helpers Here
    	try {
    		HBHelper.registerI18NHelper();
    		HBHelper.registerSelect();
    		HBHelper.registerUnsortedSelect();
    		HBHelper.registerIfCondition();
    		HBHelper.registerConcatHelper();
    		HBHelper.registerStringHelpers();
    		HBHelper.registerDateHelpers();
    		HBHelper.registerContextHelpers();
    		HBHelper.registerIn();
    		HBHelper.registerNotIn();
    	} catch(err) {
    		App.error(err);
    	}
		
        /*
         * Preload permission data
         */
        App.decorateAjax();
        var ctxt = null;
        
        $(window).on('beforeunload', function(){
        	var status = {
    	    		msg: App.i18ntxt.navigateWarning
    	    };
			Backbone.trigger('application:beforenavigate', status);
			if (App.unsavedChanges) {
				App.unsavedChanges = false;
				return status.msg || App.i18ntxt.navigateWarning;
			}
        });

        $.when(
            	$.get(App.contextPath, function(context) {
            		ctxt = context;
            		App.contextConfig = ctxt;
            	}),
            		
//            	$.get("papi/permissions", function(data) {
//            		App.permissions = data;
//            	}),
            	
            	$.get("api/uperms", function(data) {
            		App.assignPerms(data);
            		HBHelper.registerHasPermissions();
            	})
            	
            ).then(function() {
        		App.jsDebug = false;
        		if (!_.isUndefined(ctxt.jsDebug)) App.jsDebug = ctxt.jsDebug;
        		
        		App.localeSeperators = ctxt.seperators[ctxt.languageID];
        		
            	try {
            		$.extend($.fn.autoNumeric.defaults, {              
                        aSep: App.localeSeperators.group,              
                        aDec: App.localeSeperators.decimal,
                        mDec: 0
                    });    			
        		} catch(err) {
        			App.error(err);
        		}
        		
        		$.sessionTimeout({
            		keepAliveUrl: App.contextPath,
            	    logoutUrl: 'logout',
            	    redirUrl: 'login',
            	    ignoreUserActivity: false,
            	    warnAfter: (App.contextConfig.timeout * 1000),
            	    redirAfter: ((App.contextConfig.timeout+30) * 1000),
            	    keepAliveInterval: ctxt.keepalive,
            	    countdownMessage: 'Redirecting in {timer} seconds.',
            	    countdownBar: true
            	});
        		
        		App.appRouter = new AppRouter();
            	App.dialog.show(new DialogView());
            	App.start();
            });

    }
);