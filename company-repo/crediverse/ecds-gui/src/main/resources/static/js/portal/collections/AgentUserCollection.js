define(["App", "underscore", "jquery","backbone","models/UserValidationModel"],
  function(App, _, $, Backbone, UserValidationModel) {
    // Creates a new Backbone Collection class object
    var AgentUserCollection = Backbone.Collection.extend({
      
    	model: UserValidationModel,
		url: '/papi/ausers',
    	
    	initialize: function() {
    	}
    
    });

    return AgentUserCollection;
  });


