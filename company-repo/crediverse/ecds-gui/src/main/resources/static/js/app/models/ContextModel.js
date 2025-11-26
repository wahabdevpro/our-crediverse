define(["App", "jquery", "backbone"],
    function (App, $, Backbone) {
        // Creates a new Backbone Model class object
        var ContextModel = Backbone.Model.extend({
        	url: 'api/context/',
        	defaults: {
			},
        	
        	events: {
    		},
    		
            initialize:function () {
            	var self = this;
            }
        });

        return ContextModel;
    }
);