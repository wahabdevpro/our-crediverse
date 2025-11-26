define( ['jquery', 'underscore', 'App', 'backbone', 'marionette'],
    function($, _, App, BackBone, Marionette) {
        var TdrReverseView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	template: "Tdrs#reversalView",
        	
        	initialize: function (options) {
        	},
        	
        	ui: {
        		view: '',
//        		amountfull: '#amount_full',
//        		amountpartial: '#amount_partial',
//                reverseTransaction: '.reverseButton',
//                authorize: '.authorizeButton',
//                reason: '#reason'
        	},
        	events: {
//        		"click @ui.amountfull": 'changeReversalType',
//        		"click @ui.amountpartial": 'changeReversalType',
        		"click @ui.view": 'view',
//            	"click @ui.reverseTransaction": 'reverseTransaction',
//            	"keypress @ui.reason": 'capture'
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
            view: function(ev) {
            },
            
            onRender: function () {
            	var self = this;
            	App.log(self.model);
            }
            

        });
        return TdrReverseView;
    });
