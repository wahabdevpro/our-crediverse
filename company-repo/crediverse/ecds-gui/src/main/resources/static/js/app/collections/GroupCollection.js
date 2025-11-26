define(["App", "jquery","backbone", "models/GroupModel"], 
	function(App, $, Backbone, GroupModel) {
	
	var TierCollection = Backbone.Collection.extend({
		model: GroupModel,
		url: 'api/groups',
		autofetch: false,
		
		initialize: function() {

		}
		
	});
	
	return TierCollection;
});