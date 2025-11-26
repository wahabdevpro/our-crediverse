define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'models/TierModel', 'utils/CommonUtils', 'views/tiers/TierDialogView', 'datatables'],
    function($, App, _, Marionette, Handlebars, TierModel, CommonUtils, TierDialogView) {
        //ItemView provides some default rendering logic
        var TierRulesView  =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: 'row',
        	},
        	template: "TierRules#tierrulesmaster",
  		  	url: 'api/tiers/',
  		  	id: null,
  		  	dataTable: null,
  		  	model: null,
  		    i18ntxt: App.i18ntxt.tiers,
  		  	
  		  	// UI Aliases
            ui: {
            	editTier: 	'.editTierButton',
            	deleteTier: '.deleteTierButton'
            },
            
            // DOM Events
            events: {
            	"click @ui.editTier": 'editTier',
            	"click @ui.deleteTier": 'deleteTier'
            },
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.tiers;
  		  		return {
  		  			heading: txt.tierRulesPageBC,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.tierRulesSectionBC,
  		  				href: "#tiers",
						iclass: "fa fa-sitemap"
  		  			}, {
  		  				text: txt.tierRulesPageBC,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            	//this.retrieveTierData();
            	this.model = new TierModel();
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.model.url+=('/'+options.id);
            		}
            	}
            },
            
            onBeforeRender: function(){
            	switch(this.model.get("type")) {
					case "T":
						this.model.set("icon", "fa-shopping-cart");
						this.model.set("typeRef", "tiers.store");
						break;
					case "W":
						this.model.set("icon", "fa-archive");
						this.model.set("typeRef", "tiers.wholesaler");
						break;
					case "R":
						this.model.set("icon", "fa-shopping-bag");
						this.model.set("typeRef", "tiers.retailer");
						break;
					case "S":
						this.model.set("icon", "fa-users");
						this.model.set("typeRef", "tiers.subscriber");
						break;
					default:
						this.model.set("icon", "fa-money");
					this.model.set("typeRef", "tiers.root");
				}
            },
            
            onRender: function() {
            	if (this.model == null) {
            		this.retrieveTierData();
            	} else {
                	this.buildDataTable('.incomingRules', this.url + 'incoming/' + this.id, 'incoming_spinner');
                	this.buildDataTable('.outgoingRules', this.url + 'outgoing/' + this.id, 'outgoing_spinner');
            	}
            },
            
            deleteTier: function(ev) {
            	var self = this;
            	var data = this.model.attributes;
            	
            	if ((!_.isUndefined(data.permanent)) && (!data.permanent)) {
		        	CommonUtils.delete({
		        		url: self.url+'/'+data.id,
		        		itemType: App.i18ntxt.tiers.tier,
		        		context: {
		        			what: App.i18ntxt.tiers.tier,
		        			name: data.name,
		        			description: data.description
		        		},
		        		data: data
		        	}, {
		        		success: function(model, response) {
		        			App.appRouter.navigate('#tiers', {trigger: true, replace: true});
		        		},
		        		error: function(model, response) {
		        			App.error(response);
		        		}
		        	});
            	}
            },
            
            editTier: function(ev) {
            	var self = this;
            	var tierData = _.extend({}, this.model.attributes);
            	var dialogModel = new TierModel(tierData);
            	
            	App.vent.trigger('application:dialog', {
            		name: 'viewDialog',
            		class: 'modal-lg',
            		view: TierDialogView,
            		title: CommonUtils.renderHtml(self.i18ntxt.editTierTitle, {name: tierData.name}),
            		params: {
            			model: dialogModel
            		},
            		hide: function(data) {
            			self.model.set(dialogModel.attributes);
        				self.render();
            			/*dialogModel.save({
    						success: function(mdl, resp) {
    							dialog.modal('hide');
    							self.model.set(mdl.attributes);
    	        				self.render();
    						},
    						error: function(mdl, error) {
        				    	if (console) console.error("Save problem: " + error);
    						},
    						preprocess: function(data) {
    	            			data.maxDailyCount = $('#maxDailyCount').autoNumeric('get');
    	                    	//data.minimumAmount = $('#minimumAmount').autoNumeric('get');
    						}
    					});*/
            		}
            	});
            	return false;
            },
            
            thousandSeperate: function(num) {
                return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
            },
            
            buildDataTable: function(tableRef, url, spinner) {
            	self = this;
            	var table = this.$(tableRef);
            	this.dataTable = table.DataTable( {
            		"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
          				var jqxhr = $.ajax(url, {
          					data: data
          				})
          				.done(function(dataResponse) {
          					if (dataResponse.data.length == 0) {
          						$(tableRef).parents('div.dataTables_wrapper').first().hide();
          						$(tableRef + "NoData").removeClass("hide");
          					}
          					self.$("#" + spinner).hide();
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
                    		   data: "name",
                    		   title: App.translate("rules.ruleNameLabel")
                    	   },
                    	   {
                    		   data: "sourceTierName",
                    		   title: App.translate("rules.ruleSourceTier")
                    	   },
                    	   {
                    		   data: "targetTierName",
                    		   title: App.translate("rules.ruleTargetTier")
                    	   },
                    	   {
                    		   data: "buyerTradeBonusPercentage",
                    		   title: App.translate("rules.ruleBuyerTradeBonusPercentageLabel"),
							   class: "right",
							   render: function(data, type, row, meta) {
                    			   return CommonUtils.formatNumber(data * 100);
                    		   }
                    	   },
                    	   {
                    		   data: "currentDays",
                    		   title: App.translate("rules.ruleActiveDaysTableHeading"),
                    		   render: function(data, type, row, meta) {
                    			   var response = [];
                  	            	_.each(data, function(item, id){
                  	            		response.push('<span class="weekday label label-success">');
                  	            		switch(item) {
                  	            			case 'SUNDAY':
                  	            				response.push('Sun');
                  	            				break;
                  	            			case 'MONDAY':
                  	            				response.push('Mon');
                  	            				break;
                  	            			case 'TUESDAY':
                  	            				response.push('Tue');
                  	            				break;
                  	            			case 'WEDNESDAY':
                  	            				response.push('Wed');
                  	            				break;
                  	            			case 'THURSDAY':
                  	            				response.push('Thu');
                  	            				break;
                  	            			case 'FRIDAY':
                  	            				response.push('Fri');
                  	            				break;
                  	            			case 'SATURDAY':
                  	            				response.push('Sat');
                  	            				break;
                  	            		}
                  	            		response.push('</span>');
                  	            	})
                  	            	
                  	            	return response.join(' ');
           	            		}
                    	   },
                    	   {
                    		   data: "currentState",
                    		   title: App.translate("rules.ruleCurrentStateTableHeading"),
							   class: "center",
                    		   render: function(data, type, row, meta) {
                    			   var label = data.charAt(0).toUpperCase() + data.slice(1).toLowerCase();
                    			   var cssclass = "label-success";
                    			   if (data == "INACTIVE")
                    				   cssclass = "label-danger";
                    			   return '<span class="label ' + cssclass + '">' + label +'</span>';
                    		   }
                    	   }
                    	  ]
                  } );
            }
            
        });
        
        return TierRulesView;
});
