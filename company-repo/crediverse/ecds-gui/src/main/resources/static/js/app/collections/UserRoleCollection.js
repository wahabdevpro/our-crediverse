define(["App", "underscore", "jquery","backbone","models/UserRoleModel"],
  function(App, _, $, Backbone, UserRoleModel) {
    // Creates a new Backbone Collection class object
    var UserRoleCollection = Backbone.Collection.extend({
      
    	model: UserRoleModel,
    	
    	initialize: function() {
    	}
    });

    return UserRoleCollection;
  });
