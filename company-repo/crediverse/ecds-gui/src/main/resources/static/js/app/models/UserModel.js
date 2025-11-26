define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		//var GroupModel = ValidationModel.extend ({
		var UserModel = ValidationModel.extend ({
			url: 'api/wusers/',
			autofetch:true
		});
		
		return UserModel;
});