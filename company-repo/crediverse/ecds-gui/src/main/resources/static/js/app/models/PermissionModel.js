define(["jquery", "backbone", "App"],
    function ($, Backbone, app) {
        // Creates a new Backbone Model class object
        var PermissionModel = ValidationModel.extend ({
            initialize:function () {
            	
            },
            events: {

            },
        });

        return PermissionModel;
    }
);