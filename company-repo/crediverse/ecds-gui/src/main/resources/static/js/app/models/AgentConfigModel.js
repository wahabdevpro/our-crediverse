define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {
	
		// Note validate returning true causes problems with changing model
		var AgentConfigModel = ValidationModel.extend ({
			url: 'api/config/agents',
		});
		
		return AgentConfigModel;
	}
);