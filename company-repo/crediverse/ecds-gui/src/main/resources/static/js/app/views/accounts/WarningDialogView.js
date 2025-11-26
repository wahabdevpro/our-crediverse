define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/AgentModel', 'jqauth'],
    function($, _, App, BackBone, Marionette, AgentModel) {
        var warningDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	template: "AgentAccounts#recycleDialog",
        });
        return warningDialogView;
    });
