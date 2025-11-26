define( ['underscore', 'jquery', 'App', 'marionette', 'views/rules/TransferRuleDialogView', 'collections/TierCollection', 'models/RuleModel', 'utils/CommonUtils', 'models/FeatureBarModel', 'datatables'],
    function(_, $, App, Marionette, TransferRuleDialogView, TierCollection, RuleModel, CommonUtils, FeatureBarModel) {
        //ItemView provides some default rendering logic
        var TransferRulesView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "TransferRules#rulemaster",
  		  	i18ntxt: App.i18ntxt.rules,
  		  	url: 'api/transfer_rules',
  		  	error: null,
			featureBar: null,
  		  	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.rules;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.navbar.transferRules,
  		  				href: window.location.hash,
						iclass:"fa fa-random"
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function () {
				this.featureBar = new FeatureBarModel();
            },
            
            onRender: function () {
            	var that = this;
            	
            	var table = this.$('.tableview');
            	this.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
        			"scrollX": true,
            		"autoWidth": true,
					"width": "100%",
					"responsive": true,
					"processing": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
                      	
          				var jqxhr = $.ajax(that.url, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
                      	  })
                      	  .fail(function(dataResponse) {
                      		that.error = dataResponse;
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
                    	   },
                    	   {
                    		   data: "name",
                    		   title: that.i18ntxt.ruleNameTableHeading,
							   class: "all",
							   render: function(data, type, row, meta) {
									data = App.refactorString(data);
									return data;
							   }
                    	   },
                    	   {
                    		   data: "sourceTierName",
                    		   title: that.i18ntxt.ruleSourceTierTableHeading,
                    		   render: function(data, type, row, meta) {
							   		if(_.isUndefined(data)) return "-";
							   		if (App.hasPermission("Tier", "View")) {
							   			return _.isUndefined(row)?data:'<a href="#tierrules/'+row.sourceTierID+'" class="routerlink">'+data+'</a>';
							   		}
                    		   }
                    	   },
                    	   {
                    		   data: "groupName",
                    		   title: that.i18ntxt.ruleSourceGroupTableHeading,
                    		   render: function(data, type, row, meta) {
							   		if(_.isUndefined(data)) return "-";
							   		if (App.hasPermission("Group", "View")) {
										data = App.refactorString(data)
							   			return _.isUndefined(row)?data:'<a href="#group/'+row.groupID+'" class="routerlink">'+data+'</a>';
							   		} else {
							   			return data;
							   		}
                    		   }
                    	   },
                    	   {
                    		   data: "targetTierName",
                    		   title: that.i18ntxt.ruleTargetTierTableHeading,
                    		   render: function(data, type, row, meta) {
							   		if(_.isUndefined(data)) return "-";
							   		if (App.hasPermission("Tier", "View")) {
							   			return _.isUndefined(row)?data:'<a href="#tierrules/'+row.targetTierID+'" class="routerlink">'+data+'</a>';
							   		} else {
							   			return data;
							   		}
                    		   }
                    	   },
                    	   {
                    		   data: "targetGroupName",
                    		   title: that.i18ntxt.ruleTargetGroupTableHeading,
                    		   render: function(data, type, row, meta) {
							   		if(_.isUndefined(data)) return "-";
							   		if (App.hasPermission("Group", "View")) {
										data = App.refactorString(data);
							   			return _.isUndefined(row)?data:'<a href="#group/'+row.targetGroupID+'" class="routerlink">'+data+'</a>';
							   		} else {
							   			return data;
							   		}
                    		   }
                    	   },
                    	   {
                    		   data: "buyerTradeBonusPercentageString",
                    		   title: that.i18ntxt.ruleBuyerTradeBonusPercentTableHeading,
							   class: "right",
							   defaultContent: "-",
                    		   render: function(data, type, row, meta) {
                    			   var result = _.isUndefined(data)?"":data;
                    			   return result+' %';
                    		   }
                    	   },
                    	   {
                    		   data: "tradeBonusCumulativePercentageString",
                    		   title: that.i18ntxt.ruleTradeBonusCumulativePercentTableHeading,
							   class: "right",
							   defaultContent: "-",
                    		   render: function(data, type, row, meta) {
                    			   var result = _.isUndefined(data)?"":data;
                    			   return result?result+' %':'-';
                    		   }
                    	   },
                    	   {
                    		   data: "targetBonusPercentageString",
                    		   title: that.i18ntxt.ruleTargetBonusPercentTableHeading,
							   class: "right",
							   defaultContent: "-",
                    		   render: function(data, type, row, meta) {
                    			   var result = _.isUndefined(data)?"":data;
                    			   return result?result+' %':'-';
                    		   }
                    	   },
                    	   {
                    		   data: "targetBonusProfile",
                    		   title: that.i18ntxt.ruleTargetBonusProfileTableHeading,
							   class: "center",
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "minimumAmount",
                    		   title: that.i18ntxt.ruleMinAmountTableHeading,
							   class: "right",
							   defaultContent: "-",
                    		   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
                    	   {
                    		   data: "maximumAmount",
                    		   title: that.i18ntxt.ruleMaxAmountTableHeading,
							   class: "right",
							   defaultContent: "-",
                    		   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data);
                    		   }
                    	   },
//                    	   {
//                    		   data: "serviceClassName",
//                    		   title: "Service Class"
//                    	   },
							/*
                    	   {
                    		   data: "startTimeOfDayString",
                    		   title: that.i18ntxt.ruleFromTimeTableheading,
							   class: "center",
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "endTimeOfDayString",
                    		   title: that.i18ntxt.ruleToTimeTableheading,
							   class: "center",
							   defaultContent: "-",
                    	   },
						   */
                    	   {
                    		   data: "currentDays",
                    		   title: that.i18ntxt.ruleActiveDaysTableHeading,
                    		   render: function(serverData, type, row, meta) {
                    			   /*
                    			    * First map our values to the display string
                    			    * We can i18n here
                    			    */
                    			   var mapping = [
                    					   {
                    						   id:'SUNDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekSunday
                    					   },
                    					   {
                    						   id:'MONDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekMonday
                    					   },
                    					   {
                    						   id:'TUESDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekTuesday
                    					   },
                    					   {
                    						   id:'WEDNESDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekWednesday
                    					   },
                    					   {
                    						   id:'THURSDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekThursday
                    					   },
                    					   {
                    						   id:'FRIDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekFriday
                    					   },
                    					   {
                    						   id:'SATURDAY',
                    						   display:that.i18ntxt.ruleShortDayOfWeekSaturday
                    					   }
                    			   ];
                    			   
                    			   /*
                    			    * Now build weekdayList to map values correctly as start - end (NB. end is undefined for single days)
                    			    */
                    			   var weekdayList = [];
                    			   var current = null;
                    			   _.each(mapping, function(item, id){
                    				   if (_.contains(serverData, item.id)) {
                    					   if (_.isNull(current)) {
                    						   current = {
                    								 start: item.display
                    						   };
                    					   }
                    					   else {
                    						   current.end = item.display;
                    					   }
                    				   }
                    				   else {
                    					   if (!_.isNull(current)) {
                    						   weekdayList.push(current);
                    						   current = null;
                    					   }
                    				   }
                    			   });
                    			   
                    			   if (!_.isNull(current)) weekdayList.push(current);
                    			   
                    			   /*
                    			    * Now convert our weekdayList into display strings
                    			    */
                    			   var response = [];
                    			   _.each(weekdayList, function(item, id){
                    				   response.push('<span class="weekday label label-success">');
                    				   if (_.isUndefined(item.end)) {
                    					   response.push(item.start);
                    				   }
                    				   else {
                    					   response.push(item.start+' - '+item.end);
                    				   }
                    				   response.push('</span>');
                    			   });
                    			   return response.join(' ');
           	            		}
                    	   },
                    	   {
                    		   data: "currentState",
                    		   title: this.i18ntxt.ruleCurrentStateTableHeading,
							   class: "center",
                    		   render: function(data, type, row, meta) {
                    			   var response = [];
                    			   response.push('<span class="label');
                    			   if (data === 'ACTIVE') {
                    				   response.push('label-success">Active</span>');
                    			   }
                    			   else {
                    				   response.push('label-default">Inactive</span>');
                    			   }
                    			   return response.join(' ');
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
								class: "nowrap all right",
                   	            sortable: false,
                   	            render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if (!row.permanent) {
                   	            		if (App.hasPermission("TransferRule", "Update")) {
                   	            			buttons.push("<button class='btn btn-primary editRuleButton btn-xs'>Edit</button>");
                   	            		}
                   	            		if (App.hasPermission("TransferRule", "Delete")) {
                   	            			buttons.push("<button class='btn btn-danger deleteRuleButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            		}
                   	            	}
            	            		return buttons.join('');
            	            	}
                   	        }
                    	  ]
                  } );
            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            ui: {
                //role: '',
                edit: '.editRuleButton',
                del: '.deleteRuleButton',
                add: '.addRuleButton',
                exportRules: '.exportRuleButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.role": 'viewRules',
            	"click @ui.edit": 'editRule',
            	"click @ui.del": 'deleteRule',
            	"click @ui.add": 'addRule',
            	"click @ui.exportRules": 'exportRules'
            },
            
            exportRules: function(ev) {
            	var self = this;
				
				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
            },
            
            viewRules: function() {
            	return false;
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

            editRule: function(ev) {
            	var self = this;
            	var tableData = this.dataTable.row($(ev.currentTarget).closest('tr')).data();

				const { id, name } = tableData;

            	var model = new RuleModel({
            		url: `${this.url}/${id}`
            	});

           		model.set(tableData);

				this.loadTiers().then((tierList) => {
						App.vent.trigger('application:dialog', {
							name: "viewDialog",
							class: 'modal-lg modal-xl',
							view: TransferRuleDialogView,
							title:CommonUtils.renderHtml(self.i18ntxt.ruleEditDialogTitle, { name }),
							hide: function() {
								self.dataTable.ajax.reload().draw();
							},
							params: {
								model,
								tierList							}
						});
				})
            	//return false;
            },
            
            deleteRule: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.rules.ruleInUseError,
	        		url: self.url+'/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.rules.transferRule,
	        			name: data.name,
	        			description: data.sourceTierName +' => '+data.targetTierName
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
            	return false;
            },
            
            addRule: function(ev) {
            	var self = this;
            	var model = new RuleModel({
            		url: this.url
            	});

				this.loadTiers().then((tierList) => {
						App.vent.trigger('application:dialog', {
							name: "viewDialog",
							class: 'modal-lg modal-xl',
							title: this.i18ntxt.ruleAddDialogTitle,
							hide: function() {
								self
								.dataTable.ajax.reload().draw();
							},
							view: TransferRuleDialogView,
							params: {
								model: model,
								tierList
							}
						});
					});
            	return false;
            }
        });
        return TransferRulesView;
    });
