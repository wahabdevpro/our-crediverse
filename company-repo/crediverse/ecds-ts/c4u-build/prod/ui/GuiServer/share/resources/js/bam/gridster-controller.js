var GridsterController = function(elementId) {
	this.elementId = elementId || null;
	this._init();
};

GridsterController.prototype = {
	_init : function() {
		this.initializeGridster();
	},
	
	initializeGridster : function() {
		try {
			var self = this;
		    this.gridster = $(".gridster ul").gridster({
		        helper: 'clone',
		        resize: {
		          enabled: true,
		          start: function(e, ui, $widget) {
		        	  
		          },
		          stop: function(e, ui, $widget) {
		                var newHeight = this.resize_coords.data.height;
		                var newWidth = this.resize_coords.data.width;
		                console.log($widget);
		                console.log("resize: (" + newWidth + "," + newHeight + ")");
		          }
		        }
		    }).data('gridster');
		} catch(err) {
			console.error(err);
		}
	},
	
	addGridElement : function(id, $widget) {
		var $html = null;
		try {
			var $html = $("<li id='" + id + "' class='new'></li>");
			if ((typeof $widget !== 'undefined') && ($widget == null)) {
				$html.append($widget);				
			}
			this.gridster.add_widget($html, 2, 2);
		} catch(err) {
			console.error(err);
		}
		return $html;
	}
	
};
