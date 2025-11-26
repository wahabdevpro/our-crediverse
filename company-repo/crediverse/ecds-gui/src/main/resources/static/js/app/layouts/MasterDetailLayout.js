define( [ 'App', 'marionette', 'layouts/MasterDetailLayout'],
    function( App, Marionette, MasterDetailLayout) {
	var MasterDetailLayout = Marionette.LayoutView.extend({
		tagName: "div",
    	attributes: {
			  class: "row"
		  	},
		template: 'MasterDetailLayout#layout',
		regions: {
			master: ".master",
			detail: ".detail"
		}
	});
	
	return MasterDetailLayout;
});