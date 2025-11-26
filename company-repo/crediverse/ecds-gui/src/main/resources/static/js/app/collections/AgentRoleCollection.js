define(["App", "jquery","backbone", "models/RoleModel"], 
	function(App, $, Backbone, RoleModel) {
	
	var AgentRoleCollection = Backbone.Collection.extend({
		model: RoleModel,
		url: 'api/roles/agent',
		
		initialize: function() {
		},
	});
	
	return AgentRoleCollection;
});
