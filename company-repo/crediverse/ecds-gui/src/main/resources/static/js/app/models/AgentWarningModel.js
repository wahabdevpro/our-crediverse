define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var AgentWarningModel = ValidationModel.extend ({
			//initialize: function() {},
			url: 'api/agents',
			
			mode: 'create',
			
			id: null,
			
			defaults: {
                agentId: 0,
                agentState: null,
                balance: 0,
                onHoldBalance:0,
                lastTransactionDate:null,
                activationDate:null
			},
		});
		
		return AgentWarningModel;
	}
);
