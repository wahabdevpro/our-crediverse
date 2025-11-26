define(["App", "underscore", "jquery","backbone","models/CellGroupModel"],
  function(App, _, $, Backbone, CellGroupModel) {
    // Creates a new Backbone Collection class object
    var CellGroupCollection = Backbone.Collection.extend({
      
    	model: CellGroupModel,
    	
    	initialize: function() {
    	}
    
    });

    return CellGroupCollection;
  });


