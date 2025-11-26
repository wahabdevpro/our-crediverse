define( ['jquery', 'backbone', 'App', 'marionette', 'views/users/UserDialogView'],
    function($, BackBone, App, Marionette, UserDialogView) {

		var PermanentUserDialogView =  UserDialogView.extend( {
  		  	template: "ManageUsersTableView#permanentuserdialogview",

  		  	onRender: function () {
            },
            
        });
        
		return PermanentUserDialogView;
        
    });