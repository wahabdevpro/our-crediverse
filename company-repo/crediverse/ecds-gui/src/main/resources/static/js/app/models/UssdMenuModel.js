define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		//var GroupModel = ValidationModel.extend ({
		var UssdMenuModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'api/config/ussdmenu',
			mode: 'create',
			id: null,
			currentMenu:null,
			autofetch:true,
			
			defaults: {
				editMode: false,
				modelchanged: false,
				showCommandField: true,
			},
			initialize: function(options) {
				if (!_.isUndefined(options) && !_.isUndefined(options.url)) this.url = options.url;
			},
			rules: {
				'description': {
		            maxlength: 80,
		            required: true
				},
				'name': {
		            maxlength: 80,
		            required: true
				}								
			},
			getCurrentMenu: function() {
				if (_.isNull(this.currentMenu)) {
					this.currentMenu = new ValidationModel(_.first(this.attributes.menus));
				}
				return this.currentMenu;
			},
			setCurrentMenu: function(id) {
				this.currentMenu = id;
				return this;
			},
			reorderMenu: function(menuId, menuOffset, newOrder) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				var buttons = menuToChange.buttons;
				var reorderedButtons = [];
				
				for (var i=0; i<newOrder.length; i++) {
					var currentButton = _.find(buttons, function(button){
													return (_.isUndefined(button))?false:button.id === parseInt(newOrder[i]);
											})
					if (!_.isUndefined(currentButton)) reorderedButtons.push(currentButton);
				}
				menuToChange.buttons = reorderedButtons;
				this.set('menus', menus);
			},
			getMenuItem: function(menuOffset, buttonId) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				return _.find(menuToChange.buttons, function(button){
					if (_.isUndefined(button)) return false;
					return button.id === parseInt(buttonId);
				})
			},
			setMenuItem: function(menuOffset, buttonId, data) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				var buttonMatchId = parseInt(buttonId);
				for (var i=0; i<menuToChange.buttons.length; i++) {
					if (menuToChange.buttons[i].id === buttonMatchId) {
						menuToChange.buttons[i] = data;
						break;
					}
				}
			},
			getMenuProperty: function(menuOffset, name) {
				var menus = this.get('menus');
				return menus[menuOffset][name];
			},
			setMenuProperty: function(menuOffset, name, value) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				menuToChange[name] = value;
			},
			updateMenuNames: function(menuid, newName) {
				var menus = this.get('menus');
				for (var i=0; i<menus.length; i++) {
					var buttons = menus[i].buttons;
					if (!_.isUndefined(buttons)) {
						for (var j=0; j<buttons.length; j++) {
							if (parseInt(menuid) == buttons[j].nextMenuID) {
								buttons[j].nextMenuName = newName;
							}
						}
					}
				}
			},
			deleteButton: function(menuOffset, buttonId) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				var buttonMatchId = parseInt(buttonId);
				var buttonsAfter = [];
				for (var i=0; i<menuToChange.buttons.length; i++) {
					if (menuToChange.buttons[i].id !== buttonMatchId) {
						buttonsAfter.push(menuToChange.buttons[i]);
					}
				}
				menuToChange.buttons = buttonsAfter;
			},
			toggleButton: function(menuId, menuOffset, buttonId, state) {
				var menus = this.get('menus');
				var menuToChange = menus[menuOffset];
				var buttons = menuToChange.buttons;
				for (var key in buttons) {
					if (buttons.hasOwnProperty(key) && buttons[key].id === parseInt(buttonId)) {
						buttons[key].disabled = state;
						break;
					}
				}
				this.set('menus', menus);
			},
			firstMenuId: function() {
				var menus = this.get('menus');
				return menus[0].id;
			},
			lastMenuId: function() {
				var menus = this.get('menus');
				return menus[menus.length - 1].id;
			},
			isFirstMenuId: function(id) {
				var result = false;
				if (!_.isUndefined(id) && !_.isNull(id)) {
					var menus = this.get('menus');
					result = (menus[0].id === parseInt(id));
				}
				return result;
			},
			isLastMenuId: function(id) {
				var result = false;
				if (!_.isUndefined(id) && !_.isNull(id)) {
					var menus = this.get('menus');
					result = (menus[menus.length - 1].id === parseInt(id));
				}
				return result;
			},
			getEditorVariables: function() {
				if (!_.isUndefined(this.attributes.variableSet) && !_.isUndefined(this.attributes.variables) && !_.isUndefined(this.attributes.variables[this.attributes.variableSet])) {
					return this.attributes.variables[this.attributes.variableSet];
				}
				return {};
			}
		});
		
		return UssdMenuModel;
	}
);
