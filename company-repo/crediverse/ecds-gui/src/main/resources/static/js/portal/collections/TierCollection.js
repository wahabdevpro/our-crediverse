define(["App", "jquery","backbone", "models/TierModel"], 
	function(App, $, Backbone, TierModel) {
	
	var TierCollection = Backbone.Collection.extend({
		model: TierModel,
		url: 'papi/tiers/filter',
		
		initialize: function() {
			/*App.vent.on('role:create', $.proxy(function(data) {
				this.create(data);
	    	}, this));*/
		},
		
		setActive: function(id) {
			_.each(this.models, function(model){
				var currId = model.get('id');
	    		model.set({active: (id == currId)?true:false}, {silent: true});
	    	});
		}
		
	});
	
	return TierCollection;
});