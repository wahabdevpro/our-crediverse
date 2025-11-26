define( ['jquery', 'App', 'marionette', 'utils/CommonUtils', 'utils/ScrollIntoView', 'datatables'],
    function($, App, Marionette, CommonUtils, ScrollIntoView) {
        //ItemView provides some default rendering logic
        var PermissionView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'api/permissions',
  		  	template: "ManageRoles#permissionview2",
  		  	error: null,
        	i18ntxt: App.i18ntxt.roleman,
			permissions: null,
        	
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.permHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.permSectionBC,
  		  				href: "#roles",
						iclass: "fa fa-key"
  		  			}, {
  		  				text: txt.permPageBC,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},

			loadPermList: function() {
				var self = this;

   				var jqxhr = $.ajax(self.url, {
   					data: {}
    			})
           	  	.done(function(dataResponse) {
					if(self.model.attributes.type == 'A') {
						dataResponse.data = $.grep(dataResponse.data, function(v){
							return v.agentAllowed;
						});
					}	
					self.permissions = dataResponse;
					self.render();
               	})
                .fail(function(dataResponse) {
                	self.error = dataResponse;
                   	App.error(dataResponse);
              	})
                .always(function(data) {
                });

			},

			getPermissionById: function(id) {
				var result = $.grep(this.permissions.data, function(v){
					return v.id == id;
				});
				return result && result.length > 0 ? result[0] : null;
			},

			setupPermList: function() {
				var self = this;

				var roleGroups = {};
				$.each(self.permissions.data, function(ix,perm) {
				    if(!(perm.group in roleGroups))
				      roleGroups[perm.group] = {};
				    roleGroups[perm.group][perm.name] = perm;
				});
				
				var groupIx = 1;
				$.each(roleGroups, function(group,roles){
					var g = self.$('.templates .group-container').clone();
					g.find('label').attr('for', 'group-'+groupIx);
					g.find('input').attr('id', 'group-'+groupIx);
					g.find('a').html(group + ' <span class="text-muted">('+_.size(roles)+')</span>');
					self.$('#permissions-list').append(g);
				
					var roleIx = 1;
					var allPerm = 0;
					var selPerm = 0;
					$.each(roles, function(role,info){
						var r = self.$('.templates .role-container').clone();
						r.find('.selector label').attr('for', 'role-'+groupIx+'-'+roleIx);
						r.find('.selector input').attr('id', 'role-'+groupIx+'-'+roleIx).data('perm-id', info.id);
						r.find('.selector span.name').text(role);
						r.find('.description').text(info.description);

						if ( !_.isUndefined(self.model)) {
							var existing = _.findWhere(self.model.attributes.permissions, {
						    	id: info.id
						    });
					      	if (!_.isUndefined(existing)) {
								r.find('.selector input').prop('checked', true);
								selPerm++;
							}	
			            }

						g.find('.permissions').append(r);
						roleIx++; allPerm++;
					});
					if ( allPerm && (selPerm == allPerm) )
						g.find('input[id="group-'+groupIx+'"]').prop('checked', true);
					else if ( allPerm && selPerm )
						g.find('input[id="group-'+groupIx+'"]').addClass('indeterminate');
				
				  	groupIx++;
				});
				
				  self.$('#permissions-list .group-name').on('click', function(){
				    $(this).closest('.group-container').find('.permissions').slideToggle(function(){
				      if($(this).is(":visible")) 
				        $(this).closest('.group-container').scrollintoview();
					  $(this).data('visible', $(this).is(":visible") ? '1' : '0');	
				    });
				  });
				
				  self.$('#permissions-list .group-selector').on('click', function(){
				    var checked = $(this).is(':checked');
				    $(this).removeClass('indeterminate');
				    $(this).closest('.group-container').find('.permissions .role-container:not(.filtered-out) input[type="checkbox"]').prop('checked', checked).each(function(){
            			self.model.updatePermission($(this).is(':checked'), self.getPermissionById($(this).data('perm-id')));
					});
				  });
				
				  self.$('#permissions-list .permission-selector').on('click', function(){
            		self.model.updatePermission($(this).is(':checked'), self.getPermissionById($(this).data('perm-id')));
				    var checked = 0, unchecked = 0;
				    $(this).closest('.permissions').find('input.permission-selector').each(function(){
				      if ($(this).is(':checked')) checked++; else unchecked++;
				    });
				    if (checked && !unchecked)
				      $(this).closest('.group-container').find('.group-selector').prop('checked', true).removeClass('indeterminate');
				    else if (!checked && unchecked)
				      $(this).closest('.group-container').find('.group-selector').prop('checked', false).removeClass('indeterminate');
				    else if (checked && unchecked)
				      $(this).closest('.group-container').find('.group-selector').addClass('indeterminate');
				  });
				  
				  self.$('button[role="select-all"]').on('click', function(){
				    self.$('#permissions-list .group-container:not(.filtered-out) > div > label > input[type="checkbox"]').removeClass('indeterminate').prop('checked', true);
				    self.$('#permissions-list .group-container:not(.filtered-out) .role-container:not(.filtered-out) input[type="checkbox"]').removeClass('indeterminate').prop('checked', true).each(function(){
            			self.model.updatePermission($(this).is(':checked'), self.getPermissionById($(this).data('perm-id')));
					});
				  });
				
				  self.$('button[role="clear-all"]').on('click', function(){
				    self.$('#permissions-list .group-container:not(.filtered-out) > div > label > input[type="checkbox"]').removeClass('indeterminate').prop('checked', false);
				    self.$('#permissions-list .group-container:not(.filtered-out) .role-container:not(.filtered-out) input[type="checkbox"]').removeClass('indeterminate').prop('checked', false).each(function(){
            			self.model.updatePermission($(this).is(':checked'), self.getPermissionById($(this).data('perm-id')));
					});
				  });
				
				  self.$('button[role="expand-all"]').on('click', function(){
				    self.$('#permissions-list .group-container .permissions').show().data('visible', '1');
				  });
				
				  self.$('button[role="collapse-all"]').on('click', function(){
				    self.$('#permissions-list .group-container .permissions').hide().data('visible', '0');
				  });
				  
				  self.$('#search').on('change keyup paste', function(ev) {
				    var value = $(this).val().toLowerCase();
				    self.$('#permissions-list .group-container').each(function(){
				      var groupMatch = $(this).find('a.group-name').text().toLowerCase().search(value) > -1 ? 1 : 0;
				      var permMatch = 0, permNoMatch = 0;
				      var permMatchSel = 0, permMatchUnsel = 0;
				      var permNoMatchSel = 0, permNoMatchUnsel = 0;
				      $(this).find('.permissions .role-container').each(function(){
				        if(groupMatch || $(this).find('span.name').text().toLowerCase().search(value) > -1 ||
				          $(this).find('div.description').text().toLowerCase().search(value) > -1 ) { 
				          permMatch++;
				          $(this).removeClass('filtered-out');
				          if($(this).find('input[type="checkbox"]').is(':checked'))
				            permMatchSel++;
				          else
				            permMatchUnsel++;
				        } else {
				          permNoMatch++;
				          $(this).addClass('filtered-out');
				          if($(this).find('input[type="checkbox"]').is(':checked'))
				            permNoMatchSel++;
				          else
				            permNoMatchUnsel++;
				        }
				      });
				      var checked = permMatchSel && !permMatchUnsel;
				      var indeterminate = permMatchSel && permMatchUnsel;
				      if (!groupMatch && !permMatch) {
				        $(this).addClass('filtered-out');
				      } else {
				        $(this).removeClass('filtered-out');
				        if(permMatch) {
						  if (value  != '')
					          $(this).find('.permissions').show();
						  else {
				        	if($(this).find('.permissions').data('visible') == '1')
							  $(this).find('.permissions').show();
							else
							  $(this).find('.permissions').hide();
						  }
				        } else {
				          $(this).find('.permissions').hide();
				          $(this).find('.permissions .role-container').removeClass('filtered-out');
				          var checked = (permMatchSel + permNoMatchSel) && !(permMatchUnsel + permNoMatchUnsel);
				          var indeterminate = (permMatchSel + permNoMatchSel) && (permMatchUnsel + permNoMatchUnsel);
				        } 
				      }
				      if (indeterminate)
				          $(this).find('.group-name-wrap input[type="checkbox"]').addClass('indeterminate');
				      else {
				          $(this).find('.group-name-wrap input[type="checkbox"]').removeClass('indeterminate')
				          $(this).find('.group-name-wrap input[type="checkbox"]').prop('checked', checked);
				      } 
				    });
				  });
			},
  		  	
            initialize: function (options) {
            },
            
            onRender: function () {
            	var self = this;
            	var html = CommonUtils.renderHtml(App.i18ntxt.roleman.permTitle, {role: this.model.attributes.name});
				self.$('.role-modify').html(html);

				if ( self.permissions == null )
				{
					self.loadPermList();
					return;
				}

				self.setupPermList();
			
				/*
            	//role-modify
            	var table = self.$('.permissiontable');
            	self.dataTable = table.DataTable( {
        			//serverSide: true,
        			// data is params to send
            		"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
            		"order": [[ 1, "asc" ]],
          			"ajax": function(data, callback, settings) {
                      	
          				var jqxhr = $.ajax(self.url, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
						  	if(self.model.attributes.type == 'A') {
								dataResponse.data = $.grep(dataResponse.data, function(v){
									return v.agentAllowed;
								});
							}	
                      	    callback(dataResponse);
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      			App.error(dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
                      columnDefs: [{
                          targets: 0,
                          orderable: false
                       }],
                      "columns": [
							{
						        data: null,
						        render: function ( data, type, row ) {
						            if ( type === 'display' && !_.isUndefined(self.model)) {
						            	var existing = _.findWhere(self.model.attributes.permissions, {
						              		  				id: data.id
						              					});
						            	
						            	var result = [];
					            		result.push('<input type="checkbox" class="editor-active"');
						            	if (!_.isUndefined(existing)) {
						            		result.push('checked="checked"');
						            	}
						            	result.push('>');
						                return result.join(' ');
						            }
						            return '';
						        }
						    },
							{
								   data: "group",
								   title: "Group"
							},
                    	   {
                    		   data: "description",
                    		   title: "Description",
                    		   render: function ( data, type, row ) {
						            return '<div class="dialog-description-cell">'+data+'</div>';
						        },
                    	   }
                    	  ]
                  } );
            	
            	table.on('click', 'input[type="checkbox"]', function(ev){
            		var $row = $(this).closest('tr');
            		self.model.updatePermission($(this).is(':checked'), self.dataTable.row($row).data());
            	});
				*/
            	
            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            ui: {
                view: '',
                save: '.saveButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.view": 'viewPermissions',
            	"click @ui.save": 'savePermissions'
            },
            
            viewPermissions: function() {
            	
            },
            
            savePermissions: function() {
            	var self = this;
            	this.model.save(null, {
            		success: function(ev){
            			var dialog = self.$el.closest('.modal');
            			dialog.modal('hide');
            		}
            	});
            }
        });
        return PermissionView;
    });
