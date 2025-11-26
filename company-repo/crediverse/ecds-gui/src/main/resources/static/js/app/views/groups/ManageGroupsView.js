define( ['jquery', 'underscore', 'App', 'marionette', 'views/groups/GroupDialogView',
		'collections/GroupCollection', 'collections/TierCollection', 'models/GroupModel',
         'utils/HandlebarHelpers', 'utils/CommonUtils', 'datatables', 'file-upload'],
    function($, _, App, Marionette, GroupDialogView,
			GroupCollection, TierCollection, GroupModel,
			HBHelper, CommonUtils) {
        //ItemView provides some default rendering logic
        var ManageGroupsView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ManageGroups#managegroups",
  		  	url: 'api/groups',
  		  	error: null,
  		  	collection: null,
  		  	
  		  	i18ntxt: App.i18ntxt.groups,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.groups;
  		  		return {
  		  			heading: txt.groupManagement,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.groups,
  		  				href: window.location.hash,
						iclass: "fa fa-reorder"
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
//            	if (!_.isUndefined(options)) this.i18ntxt = options;
            	this.collection = new GroupCollection();
            	try {
					HBHelper.registerSelect();
				} catch(err) {
					App.log(err);
				}
            },
            
            logEvents: function(ctrl, list) {
            	var self = this;
            	_.each(list, function(item, index) {
            		ctrl.on(item, $.proxy(function(event, data) {
            			App.log('Fired event '+item+' '+JSON.stringify(data, null, 2));
            		}, self));
            	})
            },
            
            onRender: function () {
            	var token = $("meta[name='_csrf']").attr("content");
        		var header = $("meta[name='_csrf_header']").attr("content");
            	var customeHeaders = {};
            	customeHeaders[header] = token;
            	var that = this;
            	
            	var self = this;
            	var table = this.$('.tableview');
            	
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
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
                    	   },
                    	   {
                    		   data: "name",
                    		   title: this.i18ntxt.name,
							   class: "all",
                    		   render: function(data, type, row, meta) {
                    			   return _.isUndefined(row)?data:'<a target="_blank" href="#group/'+row.id+'" class="routerlink">'+data+'</a>';
                    		   }
                    	   },
                    	   {
                    		   data: "description",
                    		   title: App.i18ntxt.global.description
                    	   },
                    	   {
                    		   data: "tierName",
                    		   title: App.i18ntxt.global.tier,
                    		   render: function(data, type, row, meta) {
                    			   if (App.hasPermission("Tier", "View")) {
                    				   return _.isUndefined(row)?data:'<a href="#tierrules/'+row.tierID+'" class="routerlink">'+data+'</a>';
                    			   } else {
                    				   return data;
                    			   }
                    		   }
                    	   },
                    	   {
                    		   data: "maxTransactionAmount",
                    		   title: App.i18ntxt.global.maxTransactionAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "maxDailyCount",
                    		   title: App.i18ntxt.global.maxDailyCount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "maxDailyAmount",
                    		   title: App.i18ntxt.global.maxDailyAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "maxMonthlyCount",
                    		   title: App.i18ntxt.global.maxMonthlyCount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "maxMonthlyAmount",
                    		   title: App.i18ntxt.global.maxMonthlyAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
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
                   	            	if (App.hasPermission("Group", "Update")) {
                   	            		buttons.push("<button class='btn btn-primary editGroupButton btn-xs'>"+App.i18ntxt.global.editBtn+"</button>");
                   	            	}
                   	            	if (App.hasPermission("Group", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deleteGroupButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            },
            
            ui: {
                create: '.createGroupButton',
                editGroup: '.editGroupButton',
                deleteGroup: '.deleteGroupButton',
                exportGroups: '.exportGroupsButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.create": 'addGroup',
            	"click @ui.editGroup": 'editGroup',
            	"click @ui.deleteGroup": 'deleteGroup',
            	"click @ui.exportGroups": 'exportGroups'
            },
            
            exportGroups: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            deleteGroup: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.groups.group,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.groups.group,
	        			name: data.name,
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

			loadTiers: function() {
				const self = this;
				const tiers = new TierCollection();            	

				return new Promise((resolve) => {
					tiers.fetch({
						success: function(ev) {
							resolve(tiers.toJSON());
						}
					});	
				})
			},
            
            editGroup: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var data = this.dataTable.row(row).data();
            	var model = new GroupModel({
            		url: this.url
            	});
            	model.set(data);
            	model.set('editMode', true);

				this.loadTiers().then((tierList) => {
					App.vent.trigger('application:dialog', {
						name: "viewDialog",
						view: GroupDialogView,
						title:CommonUtils.renderHtml(App.i18ntxt.groups.editModalTitle, {name: data.name}),
						hide: function() {
							self
							.dataTable.ajax.reload().draw();
						},
						params: {
							model: model,
							tierList
						}
					});
				});
            	return false;
            },
            
            addGroup: function(ev) {
            	var self = this;
            	var model = new GroupModel({
            		url: this.url
            	});
				
				this.loadTiers().then((tierList) => {
					App.vent.trigger('application:dialog', {
						name: "viewDialog",
						title: App.i18ntxt.groups.addModalTitle,
						hide: function() {
							self
							.dataTable.ajax.reload().draw();
						},
						view: GroupDialogView,
						params: {
							model: model,
							tierList
						}
					});
				});
            	return false;
            }
        });
        return ManageGroupsView;
    });
