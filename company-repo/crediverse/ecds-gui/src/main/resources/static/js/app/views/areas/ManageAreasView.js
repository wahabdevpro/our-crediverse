define( ['jquery', 'underscore', 'App', 'marionette', 'views/areas/AreaDialogView', 'views/areas/AreaView', 'models/AreaModel', 'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, AreaDialogView, AreaView, AreaModel, HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
        var ManageAreasView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageAreas#manageareas",
  		  	url: 'api/areas',
  		  	currentFilter: {}, // Used to keep track of filter settings for use by export.
  		  			  	
  		  	error: null,
		  	// Models
  		  	model: null,
  		  	
  		  	i18ntxt: App.i18ntxt.areas,
  		  	
  		  	breadcrumb: function() {
		  		var txt = App.i18ntxt.areas;
		  		return {
		  			heading: txt.areaManagement,
		  			defaultHome: false,
		  			breadcrumb: [{
		  				text: txt.sectionBC,
						iclass:"fa fa-key"
		  			}, {
		  				text: App.i18ntxt.navbar.areas,
		  				href: window.location.hash,
		  			}]
		  		}
		  	},
  		  	
            initialize: function (options) {
            	try {
            		HBHelper.registerSelect();
            	} catch(err) {
            		App.log(err);
            	}
            },
            
            // Add "0" item to areas list
            addDefaultItem: function (model) {
				var data = model.get("data");
				data.unshift({id:0, name:App.i18ntxt.notSet, description:App.i18ntxt.notSet});
				return data;
            },
            
            logEvents: function(ctrl, list) {
            	var self = this;
            	_.each(list, function(item, index) {
            		ctrl.on(item, $.proxy(function(event, data) {
            			App.log('Fired event '+ item + ' '+ JSON.stringify(data, null, 2));
            		}, self));
            	})
            },
            
            onRender: function () {
            	var self = this;
            	var table = this.$('.tableview');
        		var tableSettings = {
        			searchBox: true,
        			newurl: self.url
        		};
            	
        		this.dataTable = table.DataTable({
        			"processing": 	true,
        			"autoWidth": 	false,
        			"responsive":   true,
        			"language": {
        				"url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
        			},
		  			"ajax": function(data, callback, settings) {			  				
		  				self.currentFilter.url = tableSettings.newurl;
		  				self.currentFilter.data = data;
		  				var jqxhr = $.ajax(tableSettings.newurl, {
		  					data: data
		  				})
		  				.done(function(dataResponse) {
		  					callback(dataResponse);
		  					table.DataTable()
		  					   .columns.adjust()
		  					   .responsive.recalc();
		  				})
		  				.fail(function(dataResponse) {
		  					self.error = dataResponse;
		  						App.error(dataResponse);
		  						App.vent.trigger('application:accountsterror', dataResponse);
		  				})
		  				.always(function(data) {
		  				});
		  			},		  			
	  			"order": [[ 0, "desc" ]],
	  			"columns": [
                	{
                	   	data: "id",
                	   	title: App.i18ntxt.global.uniqueID,
						class: "all center",
						width: "80px",
                	},
	  				{
	  					data: "name",
	  					title: this.i18ntxt.name,
	  					class: "all",
	  				},
	  				{
	  					data: "type",
	  					title: this.i18ntxt.type,
	  					//class: "all",
	  				},
	  				{
						data: "parentAreaName",
						title: this.i18ntxt.parentArea,
						//class: "all",  
						defaultContent: "-"
	  				},
	  				{
	        		   targets: -1,
	        		   data: null,
	        		   title: "",
	        		   sortable: false,
	        		   class: "nowrap right all",
	        		   width: "85px",
	        		   render: function(data, type, row, meta) {
	        			   var buttons = [];
	        			   if (App.hasPermission("Area", "Update")) {
	        				   buttons.push("<button class='btn btn-primary editAreaButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
	        			   }
	        			   if (App.hasPermission("Area", "Delete")) {
	        				   buttons.push("<button class='btn btn-danger deleteAreaButton btn-xs'><i class='fa fa-times'></i></button>");
	        			   }
	        			   return buttons.join('');
	        		   }
	  				}
	  				]
        		})
        	},
            
            ui: {
                create: '.createAreaButton',
                editArea: '.editAreaButton',
                deleteArea: '.deleteAreaButton',
                exportAreas: '.exportAreasButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.create": 'addArea',
            	"click @ui.editArea": 'editArea',
            	"click @ui.deleteArea": 'deleteArea',
            	"click @ui.exportAreas": 'exportAreas'
            },
            
            exportAreas: function(ev) {
            	var self = this;
            	var table = this.$('.tableview').DataTable();
            	CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            deleteArea: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.areas.area,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.areas.area,
	        			name: data.name,
	        			description: data.description
	        		},
	        		rowElement: row,
	        	},
	        	{
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
	        	
            },
            
            editArea: function(ev) {
            	var self = this;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var data = _.extend({}, currentRow.data()); 
            	var model = new AreaModel();       	
            	model.set(data);
            	model.set('editMode', true);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: AreaDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.areas.editModalTitle, {name: data.name}),
            		hide: function() {
	        			self
	        			.dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
            	return false;
            },
            
            addArea: function(ev) {
            	var self = this;
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.areas.addModalTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: AreaDialogView,
            		params: {
            			model: new AreaModel()
            		}
            	});
            	return false;
            }
        });
        return ManageAreasView;
    });
