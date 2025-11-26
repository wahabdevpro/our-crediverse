define(["jquery", "underscore", "backbone", "App", "models/ValidationModel", "moment"],
    function ($, _, Backbone, app, ValidationModel, moment) {
        // Creates a new Backbone Model class object
        var PromotionModel = ValidationModel.extend({
        	
        	url: "api/promos",
        	errorContext: "promotion",
            rules: {
				"name": {
		            maxlength: 80,
		            required: true
				},
				"startDateString": {
					required: true
				},
				"startTimeString": {
					time24: true,
					required: true
				},
				"endDateString": {
					required: true
				},
				"endTimeString": {
					time24: true,
					required: true
				},
				"rewardAmount": {
					required: true
				},
				"guiRewardPercentage": {
					required: true,
					numeric: true,
					places: 8,
					min: 0,
					max:100
				},
				"targetAmount": {
					required:  true
				}
			},
			
			masks: {
				"startDateString":	"9999-99-99",
				"startTimeString":	"99:99:99",
				"endDateString":	"9999-99-99",
				"endTimeString":	"99:99:99",
			},
			
			initialize: function() {
				ValidationModel.prototype.initialize.call(this);
        	},

        });

        return PromotionModel;
    }
);
