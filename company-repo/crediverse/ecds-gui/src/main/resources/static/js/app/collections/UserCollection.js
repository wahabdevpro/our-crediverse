define(["App", "underscore", "jquery","backbone","models/UserValidationModel"],
  function(App, _, $, Backbone, UserValidationModel) {
    // Creates a new Backbone Collection class object
    var UserCollection = Backbone.Collection.extend({
      
    	model: UserValidationModel,
		url: '/api/wusers',
    	
    	initialize: function() {
    	}
    
    });

    return UserCollection;
  });


