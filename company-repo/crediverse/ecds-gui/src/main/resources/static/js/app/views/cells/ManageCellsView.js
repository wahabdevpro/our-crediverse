define( ['jquery', 'underscore', 'App', 'marionette', 'views/cells/CellDialogView', 'models/CellModel', 'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, CellDialogView, CellModel, HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
        var ManageCellsView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageCells#managecells",
  		  	url: 'api/cells',
  		  	urlAreas: 'api/areas',
  		  	urlCellGroups: 'api/cellgroups',
  		  	
  		  	error: null,
  		  	//Models
  		  	model: new CellModel(),
		  	areasModel: null,
		  	cellGroupsModel: null,
  		  	
  		  	i18ntxt: App.i18ntxt.cells,
  		  	  		  	
  		  	breadcrumb: function() {
		  		var txt = App.i18ntxt.cells;
		  		return {
		  			heading: txt.cellManagement,
		  			defaultHome: false,
		  			breadcrumb: [{
		  				text: txt.sectionBC,
						iclass: "fa fa-key"
		  			}, {
		  				text: App.i18ntxt.navbar.cells,
		  				href: window.location.hash
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
            
            loadModels: function() {
            	var self = this;
            	this.areasModel = new Backbone.Model();
            	this.areasModel.url = this.urlAreas;
            	       		
        		$.when( this.areasModel.fetch() )
    			.done(function() {
    				// Add Default Item
    				self.areasModel.set( "areas", self.addDefaultItem(self.areasModel) );
    				self.render();
        		});
            	
				this.cellGroupsModel = new Backbone.Model();
            	this.cellGroupsModel.url = this.urlCellGroups;
            	       		
        		$.when( this.cellGroupsModel.fetch() )
    			.done(function() {
    				// Add Default Item
    				self.cellGroupsModel.set( "cellGroups", self.addDefaultItem(self.cellGroupsModel) );
    				self.render();
        		});
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
            			App.log('Fired event '+item+' '+JSON.stringify(data, null, 2));
            		}, self));
            	})
            },
            
            configureSearchForm: function() {
				var that = this;
            	CommonUtils.configureSelect2Control({
  		  			jqElement: that.$('#searchArea'),
  		  			url: "api/areas/dropdown",
  		  			placeholderText: that.i18ntxt.areas,
  		  			minLength: 0,
					isHtml: true
  		  		});
            	this.$("#searchArea").val("").trigger('change');
            	
				CommonUtils.configureSelect2Control({
  		  			jqElement: that.$('#searchCellGroup'),
  		  			url: "api/cellgroups/dropdown",
  		  			placeholderText: that.i18ntxt.cellGroups,
  		  			minLength: 0
  		  		});
            	this.$("#searchCellGroup").val("").trigger('change');
            },
            onRender: function () {
            	var token = $("meta[name='_csrf']").attr("content");
        		var header = $("meta[name='_csrf_header']").attr("content");
            	var customeHeaders = {};
            	customeHeaders[header] = token;
            	var self = this;
            	var table = this.$('.tableview');
            	if (this.areasModel == null || this.cellGroupsModel == null) {
            		this.loadModels();
            	} else {
	            	this.dataTable = table.DataTable( {
	        			serverSide: true,
	        			// data is params to send
						autoWidth: false,
						responsive: true,
						processing: true,
						language: {
			                url: "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
			            },
	          			"ajax": function(data, callback, settings) {
	          				var jqxhr = $.ajax(self.url, {
	          					data: data
	          				})
	                      	  .done(function(dataResponse) {
	                      	    callback(dataResponse);
	                      	  })
	                      	  .fail(function(dataResponse) {
	                      		  self.error = dataResponse;
	                      		App.error(dataResponse);
	                      	  })
	                      	  .always(function(data) {
	                      	  });
	                      },
	                      "columns": [
	                    	  {
	                    		  data: "id",
	                    		  title: App.i18ntxt.global.uniqueID,
	                    		  class: "all center",
	                    		  width: "80px",
	                    		  render: function(data, type, row, meta) {
	                    			  return '<a class="routerlink" href="#cell/' + row['id'] + '">' + data + '</a>';
	                    		  }
	                    	  },
	                    	  {
	                    		  data: "mobileCountryCode",
	                    		  title: this.i18ntxt.mobileCountryCode,                   		   
	                    	  },
	                    	  {
	                    		  data: "mobileNetworkCode",
	                    		  title: this.i18ntxt.mobileNetworkCode
	                    	  },
	                    	  {
	                    		  data: "localAreaCode",
	                    		  title: this.i18ntxt.localAreaCode,
	                    	  },
	                    	  {
	                    		  data: "cellID",
	                    		  title: this.i18ntxt.cellID,
	                    	  },
	                    	  {
	                    		  data: "latitude",
	                    		  title: this.i18ntxt.latitude,
	                    	  },
	                    	  {
	                    		  data: "longitude",
	                    		  title: this.i18ntxt.longitude,
	                    	  },
	                    	//   {
	                    	// 	  data: "cellGroups",
	                    	// 	  title: this.i18ntxt.cellGroups,
	                    	// 	  render: function(data, type, row, meta) {
	                    	// 		  var value = '';
							// 		  if ( row['cellGroups'] != null )
							// 		  {
							// 			  value += '<ul style="padding-left:15px; margin:0;">';
	                    	// 			  for (var i = 0; i < row['cellGroups'].length; i++) {
	                    	// 				  value += "<li>" + row['cellGroups'][i].name;
	                    	// 				  value += "</li>";
	                    	// 			  }
	                    	// 			  value += "</ul>";
							// 		  }  
	                    	// 		  return value;
	                    	// 	  }
	                    	//   },
//							  Perhaps a bit busy to add this column (with or without the unordered list)	                    	  
//	                    	  {
//	                    		  data: "areas",
//	                    		  title: this.i18ntxt.areas,
//	                    		  render: function(data, type, row, meta) {
//	                    			  var value = "<ul>";
//	                    			  for (var i = 0; i < row['areas'].length; i++) {
//	                    				  value += "<li>" + row['areas'][i].name;
//	                    				  //if(i < row['areas'].length - 1)
//	                    				  value += "</li>";
//	                    			  }
//	                    			  value += "</ul>";
//	                    			  return value;
//	                    		  }
//	                    	  },
						  {
								data: "Area",
								title: this.i18ntxt.areas,
								width: "200px",
								render: function(data, type, row, meta) {
									var value = '';
									if ( row['areas'] != null )
									{
										value += '<ul style="padding-left:0px;margin:0;">';
										for (var i = 0; i < row['areas'].length; i++) {
											value += "<li style='list-style:none;'>" + row['areas'][i].name;
											value += "<span class='align-right-type font-small-type' style='margin-top: 2px;'>"
													+ row['areas'][i].type
													+ "</span>"
											value += "</li>";
										}
										value += "</ul>";
									}  
									return value;
								}
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
	                    			  if (App.hasPermission("Cell", "Update")) {
	                    				  buttons.push("<button class='btn btn-primary editCellButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
	                    			  }
	                    			  if (App.hasPermission("Cell", "Delete")) {
	                    				  buttons.push("<button class='btn btn-danger deleteCellButton btn-xs'><i class='fa fa-times'></i></button>");
	                    			  }
	                    			  return buttons.join('');
	                    		  }
	                    	  }
	                    	  ],
	                      "order": [[1, 'asc']]
	            	} );
            	}
            	this.configureSearchForm();
            },
            
            ui: {
                create: '.createCellButton',
                editCell: '.editCellButton',
                deleteCell: '.deleteCellButton',
                exportCells: '.exportCellsButton',
                search: '.cellSearchButton',
                cellSearchExpand: '.cellSearchExpandButton',
        		cellSearchCancel: '.cellSearchCancelButton',
        		cellSearchReset: '.cellSearchResetButton',
        		viewCellsButton: '.viewCellsButton',
        		searchAreaSelect: '#searchArea',
        		searchCellGroupSelect: '#searchCellGroup'
            },

            // View Event Handlers
            events: {
            	"click @ui.create": 'addCell',
            	"click @ui.editCell": 'editCell',
            	"click @ui.deleteCell": 'deleteCell',
            	"click @ui.exportCells": 'exportCells',
            	"click @ui.search": 'cellSearch',
            	"click @ui.cellSearchExpand": 'displayAdvancedSearch',
				"click @ui.cellSearchCancel": 'cellSearchCancel',
				"click @ui.cellSearchReset": 'cellSearchReset',
				"click @ui.viewCellsButton": 'viewCells',
				"change @ui.searchAreaSelect": 'onChangeSearchArea',
				"change @ui.searchCellGroupSelect": 'onChangeSearchCellGroup'
            },
            
            exportCells: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            deleteCell: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.cells.cell,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.cells.cell,
	        			name: data.mobileCountryCode + "-" + data.mobileNetworkCode + "-" + data.localAreaCode + "-" + data.cellID,
	        			description: data.description
	        		},
	        		rowElement: row,
	        	}, {
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
            
            editCell: function(ev) {
            	var self = this;
            	var currentRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var cellData = _.extend({}, currentRow.data());             		
            	var model = new CellModel();
            	model.set(cellData);
            	model.set('editMode', true);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: CellDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.cells.editModalTitle, {
            			mobileCountryCode: cellData.mobileCountryCode,
            			mobileNetworkCode: cellData.mobileNetworkCode,
            			localAreaCode: cellData.localAreaCode,
            			cellID: cellData.cellID
            		}),
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
            
            addCell: function(ev) {
            	var self = this;            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.cells.addModalTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		view: CellDialogView,
            		params: {
            			model: new CellModel()
            		}
            	});
            	return false;
            },
            
            displayAdvancedSearch: function(ev) {
              	$('.advancedSearchInput').hide();
              	$('.advancedSearchForm').slideDown({
              		duration: 'slow',
              		easing: 'linear',
              		start: function() {
              		},
              		complete: function() {
              		}
              	});
    			App.appRouter.navigate(window.location.hash.replace('/asf!off','/asf!on'), {trigger: false, replace: true});
            },
      		  	
			hideAdvancedSearch: function(ev) {
				$('.advancedSearchInput').show().focus();
				$('.advancedSearchForm').slideUp({
					duration: 'slow',
					easing: 'linear',
					start: function() {
					},
					complete: function() {
					}
				});
				App.appRouter.navigate(window.location.hash.replace('/asf!on','/asf!off'), {trigger: false, replace: true});
			},
			
			cellSearchCancel: function(ev) {
            	var self = this;
				this.hideAdvancedSearch();
				self.model.formCleanup();
            	return false;
            },
			
			cellSearchReset: function(ev) {
            	var self = this;
            	self.enableSearchResetButton(false);
            	var form = self.$('form')[0];
				form.reset();
				self.$("#searchMobileCountryCode,#searchMobileNetworkCode,#searchLocalAreaCode,#searchCellID,#searchArea,#searchCellGroup").val(null).trigger("change");
            	var ajax = self.dataTable.ajax;
        		var url = self.url; //+'/search';
        		ajax.url(url).load( function(){
        			self.enableSearchResetButton(true);
        		}, true );
        		self.model.formCleanup();
			},
			
            enableSearchResetButton: function(isEnabled) {
            	self.$('.cellSearchResetButton').prop('disabled', !isEnabled);
            },

            cellSearch: function(ev) {
            	var self = this;
            	self.enableSearchButton(false);
            	if (self.$( "#searchform" ).valid()) {
	            	var ajax = this.dataTable.ajax;
	        		var url = self.url+'?'+self.getFormData();
	        		ajax.url(url).load( function(){
	        			self.enableSearchButton(true);
	        		}, true );
	        		$('.advancedSearchResults .dataTables_filter label').show();
	            }
            	return false;
            },
            
            enableSearchButton: function(isEnabled) {
            	if(isEnabled)
            		self.$('.cellSearchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            	else
            		self.$('.cellSearchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            },
            
            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize($('form'));
				App.log( criteria );
            	var args = "";
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            onChangeSearchArea: function() {
            	var searchAreaSelect = this.$("#searchArea");
            	var selection = searchAreaSelect.val(); 
            	var isAreasSelected = (!_.isUndefined(selection) && selection != null && selection.length > 0);
            	this.$("#recursive").attr("disabled", !isAreasSelected);
            },
            onChangeSearchCellGroup: function() {
            	var searchCellGroupSelect = this.$("#searchCellGroup");
            	var selection = searchCellGroupSelect.val(); 
            	var isCellGroupsSelected = (!_.isUndefined(selection) && selection != null && selection.length > 0);
            	this.$("#recursive").attr("disabled", !isCellGroupsSelected);
            }
        });
        return ManageCellsView;
    });
