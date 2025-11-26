define(["App", "underscore", "jquery","backbone","models/DepartmentModel"],
  function(App, _, $, Backbone, DepartmentModel) {
    // Creates a new Backbone Collection class object
    var DepartmentCollection = Backbone.Collection.extend({
      
    	model: DepartmentModel,
    	
    	initialize: function() {
    	}
    
    });

    return DepartmentCollection;
  });


