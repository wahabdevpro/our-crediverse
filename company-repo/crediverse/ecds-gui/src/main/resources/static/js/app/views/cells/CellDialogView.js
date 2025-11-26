define( ['jquery', 'backbone', 'App', 'marionette', 'models/CellModel', 'utils/CommonUtils'],
    function($, BackBone, App, Marionette, CellModel, CommonUtils) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.cells;
        var CellDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
  		  	template: "ManageCells#singlecellview",
  		  	error: null,
            initialize: function (options) {
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) 
            		this.model = options.model;          	
            },

            onRender: function () {
            	var areas = [];            
            	if( !_.isUndefined(this.model) && !_.isUndefined(this.model.attributes.areas )){            		
	            	this.model.attributes.areas.forEach(function(area) {
	            		areas.push({id: area.id, text: area.name});
	                });
            	}
            	
            	var areasElement = this.$('#areas');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: areasElement,
  		  			url: "api/areas/dropdown",
  		  			placeholderText: i18ntxt.areas,
  		  			minLength: 0,
					isHtml: true
  		  		});
            	
				var cellGroups = [];            
            	if( !_.isUndefined(this.model) && !_.isUndefined(this.model.attributes.cellGroups )){            		
	            	this.model.attributes.cellGroups.forEach(function(cellGroup) {
	            		cellGroups.push({id: cellGroup.id, text: cellGroup.name});
	                });
            	}
            	
            	var cellGroupsElement = this.$('#cellGroups');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: cellGroupsElement,
  		  			url: "api/cellgroups/dropdown",
  		  			placeholderText: i18ntxt.cellGroups,
  		  			minLength: 0
  		  		});
            },
                        
            ui: {
                view: '',
                save: '.cellSaveButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveCell'
            },
            
            saveCell: function() {
            	var self = this;

				const data = Backbone.Syphon.serialize(this.$('.celldetails form'));
				// Areas is NOW limited to 1 entry ..... BUT, the backend still expects an Array
				data.areas = [data.areas];
            	this.model.save(data, {
            		success: function(ev){
            			var dialog = self.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		error: function(ev){
            			var dialog = self.$el.closest('.modal');
            			///dialog.modal('hide');
            		}
				});
            }
        });
        return CellDialogView;
    });
