//Static Configuration
require.config({
    baseUrl:"js/portal",
    waitSeconds : 30,
    // recommended way of loading code mirror
    packages: [{
    	name:     "codemirror",
    	location: "../lib/codemirror",
    	main:     "lib/codemirror"
    }],
    // 3rd party script alias names (Easier to type "jquery" than "libs/jquery, etc")
    // probably a good idea to keep version numbers in the file names for updates checking
    paths:{
        // Core Libraries
        "backbone":"../lib/backbone/backbone",
        "underscore":"../lib/underscore",
        "jquery":"../lib/jquery-1.11.3",
        "marionette.lib":"../lib/backbone/backbone.marionette",
        "handlebars":"../lib/handlebars-v4.0.5",
        "marionette": "../lib/backbone/marionette.intl",
        "bootstrap":"../lib/bootstrap/bootstrap",
        "i18n":"../lib/plugins/i18n",
        "i18nbs":"../lib/plugins/i18nbs",
        "datatables.net": "../lib/datatables/jquery.dataTables",
        "datatables": "../lib/datatables/dataTables.bootstrap",

        "datatables.complete": "../common/cscomponent/cs.datatables.complete",
        "datatables.net-responsive": "../lib/datatables/plugins/responsive/dataTables.responsive",
        "datatables.net-bs": "../lib/datatables/plugins/responsive/responsive.bootstrap",

        "timeout": "../lib/bootstrap/bootstrap-session-timeout",

        // Plugins
        "backbone.syphon": "../lib/backbone/backbone.syphon",
        "backbone.validation":"../lib/backbone/backbone-validation-amd",
        "backbone.nested":"../lib/backbone/backbone-nested",
        "backbone.paginator": "../lib/backbone/backbone.paginator",
        "text":"../lib/plugins/text",

        "jquery.validate": "../lib/jqueryvalidate/jquery.validate",
//        "jquery.validate_en" : "../lib/jqueryvalidate/localization/messages_en",
//        "jquery.validate_fr" : "../lib/jqueryvalidate/localization/messages_fr",
        "validation-config": "./utils/validation-config",


        "jquery-sortable": "../lib/plugins/jquery-sortable",
        "jquery.numeric": "../lib/plugins/jquery.numeric",
        "autoNumeric": "../lib/plugins/autoNumeric",
        "jquery.maskedinput": "../lib/maskedinput/jquery.maskedinput",

        "additional-methods": "../lib/jqueryvalidate/additional-methods",
        "jquery.select2full": "../lib/select2/select2.full",
        "jquery.select2": "../common/cscomponent/cs.jquery.select2",
        "file-upload": "../lib/file-upload/jquery.fileupload", //
        "jquery.ui.widget": "../lib/file-upload/vendor/jquery.ui.widget",
		'datepicker': "../lib/bootstrap-datepicker/bootstrap-datepicker",
		'clockpicker': "../lib/clockpicker/bootstrap-clockpicker",
		'jquery.slimscroll': "../lib/jquery-slimscroll/jquery.slimscroll.min",
		'fuelux': '../lib/fuelux/fuelux',
		"toastr": "../lib/toastr",
		'moment': '../lib/moment.min',

        // AdminguiLTE additions
        "adminguilte": "adminguilte/adminguilte",

        // Authorization components
        "rsa": "../lib/rsa",
        "jqauth": "../login/app/jqauth",

        // I18N stuff
        "handlebars-intl": "../lib/handlebars-intl/handlebars-intl",

        // Reusable Components
        "jquery.plugins": "../common/config/jquery.plugins",
        "CommonUtils": "../common/utils/CommonUtils",
        "ScrollIntoView": "../common/utils/ScrollIntoView",
        "HandlebarHelpers":	"../common/utils/HandlebarHelpers",
        "IsoLanguage": "../common/utils/IsoLanguage",
        "PreloadPartials": "../common/utils/PreloadPartials",
        "StateSaveUtils": "../common/utils/StateSaveUtils",
        "validation-config": "../common/utils/validation-config",

        "ValidationModel": "../common/models/ValidationModel",
        "PasswordChangeModel": "../common/models/PasswordChangeModel",
        "TemplateEngine":  "../common/config/TemplateEngine",
        "BaseAppRouter":	"../common/routers/BaseAppRouter",
        "BaseFrontController":	"../common/controllers/BaseFrontController",
    },

    // Format of map ->
    // map: {
    //    "for resource(s)" {
    //          "when requesting" : "give this resource"
    // }
    // * => All resources
	map: {
		"*" : {
			"utils/CommonUtils" : 		"CommonUtils",
			"utils/ScrollIntoView" : 	"ScrollIntoView",
			"utils/HandlebarHelpers" : 	"HandlebarHelpers",
			"utils/IsoLanguage" : 		"IsoLanguage",
			"utils/PreloadPartials" : 	"PreloadPartials",
			"utils/StateSaveUtils" : 	"StateSaveUtils",
			"utils/validation-config" : "validation-config",

			"models/ValidationModel" :	"ValidationModel",
			"models/PasswordChangeModel" :	"PasswordChangeModel",

			"config/TemplateEngine":	"TemplateEngine",

			"common/locale" : 			"i18n!../common/nls/common",
			"common/violations" :		"../common/nls/violations",
			"common/auth" :				"../common/nls/auth",

			"template/login" : 	"text!../login/templates/login.handlebars",
			"common/CommonPartials" : "text!../common/templates/CommonPartials.handlebars",			
			"App":	"../common/App",

			"views/DialogView": 		"../common/views/DialogView",
			"views/BreadCrumbView": 	"../common/views/BreadCrumbView",
			"views/PasswordChangeDialogView": 	"../common/views/PasswordChangeDialogView",
			"views/PasswordChangeNoEncryptDialogView": 	"../common/views/PasswordChangeNoEncryptDialogView",

			"config/start":	"../common/config/start"
		}
	},

    // Sets the configuration for your third party scripts that are not AMD compatible
    shim:{
       	"validation-config":["jquery.validate", "jquery.maskedinput", "jquery.numeric", "file-upload", "autoNumeric", "datepicker", "jquery-sortable"],
    	"adminguilte":{
    		"deps":["jquery", "bootstrap", "datatables.complete", "validation-config", "additional-methods", "jquery.select2", "timeout", "fuelux"],
    		"exports":"AdminLTE"
    	},
        "bootstrap":["jquery.plugins"],
    	"backbone-validator": ["backbone", "jquery.plugins"],
    	"datatables.complete":{
    		"deps":["jquery", "datatables", "backbone", "jquery.plugins"]
    	},
    	"jquery.plugins": {
    		"deps":["jquery"]
    	},
    	"jquery-sortable": ["jquery"],
    	"autoNumeric":{
    		"deps":["jquery"],
    		"exports": '$'
    	},
        "backbone":{
            "deps":["underscore", "jquery.plugins", "autoNumeric", "jquery.maskedinput"],
            // Exports the global window.Backbone object
            "exports":"Backbone"
        },
        "handlebars":{
            "exports":"Handlebars"
        },
        "marionette.lib":{
            deps:["underscore", "backbone", "jquery.plugins", "backbone.syphon","backbone.validation"],
            // Exports the global window.Marionette object
            exports:"Marionette"
        },
        "marionette":{
        	deps:["underscore", "marionette.lib"],
            // Exports the global window.Marionette object
            exports:"Marionette"
        },
        "rsa": ["jquery"],
        "jqauth": ["rsa", "jquery"],
		"datepicker": {
        	deps: ["jquery"],
			exports: 'datepicker'
		},
		"clockpicker": {
        	deps: ["jquery"],
			exports: 'clockpicker'
		},
		"file-upload": {
            deps: ["jquery", "jquery.ui.widget"],
            exports: '$'
        },
        "jquery.select2": {
            deps: ["jquery"],
            exports: '$'
        },
        "timeout": {
            deps: ["jquery"],
            exports: '$'
        },

        "handlebars-intl": {
        	deps: ["handlebars"],
        	exports: 'HandlebarsIntl'
        }

    }
});

//Dynamic Configuration
require.config({
    locale:csUserLanguage,

//    paths:{
//    	"jqueryvalidate/locale" : "../lib/jqueryvalidate/localization/messages_" + csUserLanguage
//    }
});

require(["config/start"], function () {
	// Do Not insert code here
});