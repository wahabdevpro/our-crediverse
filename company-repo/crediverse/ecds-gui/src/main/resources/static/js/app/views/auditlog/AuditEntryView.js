define( ['jquery', 'App', 'backbone', 'marionette', 'utils/CommonUtils', 'models/AuditLogModel'],
    function($, App, BackBone, Marionette, CommonUtils, AuditLogModel) {
        //ItemView provides some default rendering logic
        var AuditEntryView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "AuditEntry#auditentrydetails",
  		  	url: 'api/auditlog/',
  		  	error: null,
			id: null,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.auditEntry;
  		  		return {
  		  			heading: txt.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.auditLog.auditLog,
  		  				href: "#auditLogList",
						iclass: "fa fa-history"
  		  			}, {
  		  				text: txt.heading,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function () {
				this.retrieveAuditEntryData();
            },

            dateFieldNames : ['lastImsiChange', 'lastImeiUpdate'],

            // Update value to be displayed
            renderedValue: function(key, val) {
            	var self = this;
            	var result = val;
            	
            	if (_.isBoolean(val))
					result = CommonUtils.formatYesNo(val);
            	else if ((val == null) || (val == "null") || (val==""))
					result = App.i18ntxt.notSetDash;
				else if ((key.toLowerCase().indexOf("dateofbirth") >= 0) && (_.isNumber(val)))
					result = CommonUtils.formatTimeStampAsDate(val);
            	//TODO: This is dangerous: words such as retardate, candidate, update will be matched loosely, correct or not.
				else if (((key.toLowerCase().indexOf("date") >= 0) || (key.toLowerCase().indexOf("time") >= 0) || self.dateFieldNames.indexOf(key) > -1) && (_.isNumber(val)))
					result = CommonUtils.formatTimeStamp(val);
				else if ((key == "mobileNumber") || (key == "signature"))
					result = val;
				else if ((key == "rewardPercentage") && _.isNumber(val)) 
						result = (val * 100) + " %";
				else if (_.isNumber(val)) 
					result = val;//CommonUtils.formatNumber(val);
				
				return result;
            },
            
			renderValues: function(obj, div) {
            	var self = this;
            	
    			$.each(obj, function(key, val) {

					if ( key != "companyID" ) {
	    				var ref = "auditEntry." + key;
	                	var label = App.translate(ref, key);

	                	if ($.isPlainObject(val)) {
		            		var idiv = $("<div>").appendTo(div);
		            		var cdiv = $("<div>").css("margin-left", "15px").appendTo(idiv);
	            			self.renderValues(val, cdiv);
	        			} else if ($.isArray(val)) {
	        				var idiv = $("<div>").addClass('view-field').appendTo(div);
	        				$("<label>").text(label).appendTo(idiv);
	        				
	        				if (val.length == 0) {
	        					$("<div>").text("-").appendTo(idiv)
	        				} else  {
		        				$.each( val, function(index, value) {
		        					if ( (key == "permissions") && ($.isPlainObject(value)) ) {
		        						// Substitute _ for space and remove dashes
		        						var ref = value.description.replace(/ /g,"_");
		        						ref = ref.replace(/-/g,"");
		        						
		        						var text =  App.translate("enums.permissions." + ref, ref);
		        						$("<div>").text(text).appendTo(idiv);
		        					} else {
		        						self.renderValues(value, idiv);	
		        					}
		        				}); 
	        				}
	        				
	        			} else {
	        				var renderValue = self.renderedValue(key, val);
	        				
		            		var idiv = $("<div>").addClass('view-field').appendTo(div);
							$("<label>").text(label).appendTo(idiv);
							$("<div>").text(renderValue).appendTo(idiv);
	       			 	}
					}
                	
    			});
			},
            
            onRender: function () {
            	var self = this;

				var bcont = this.$('.fields-before');
				var acont = this.$('.fields-after');

				var bfields = self.model.attributes.oldValue ? $.parseJSON( self.model.attributes.oldValue ) : null;
				var afields = self.model.attributes.newValue ? $.parseJSON( self.model.attributes.newValue ) : null;
            
				if (bfields)
				{
					var div = $("<div>").css("margin-left", "0").appendTo(bcont);		
					this.renderValues(bfields, div);
				}	
				if (afields)
				{
					var div = $("<div>").css("margin-left", "0").appendTo(acont);		
					this.renderValues(afields, div);
				}	
            },
            
			retrieveAuditEntryData: function() {
            	var self = this;
            	this.model = new AuditLogModel({id: self.id});
            	this.model.fetch({
            		success: function(ev){
						self.render();
					},
				});
			},	

            ui: {
                transaction: '',
            },

            // View Event Handlers
            events: {
            	"click @ui.entry": 'viewEntry',
            },
        });
        return AuditEntryView;
    });
