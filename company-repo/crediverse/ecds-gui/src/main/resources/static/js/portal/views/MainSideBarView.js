define([ 'App', 'jquery', 'underscore', 'marionette', 'backbone'],
    function (App, $, _, Marionette, Backbone) {
        //ItemView provides some default rendering logic
        var MainSideBarView =  Marionette.ItemView.extend({
            template:'MainView#mainsidebar',
            // View Event Handlers
            events: {
            	'click .menuentry': 'processMenuItem'
            },

            homePage: "dashboard",
            
            setActive: function(item) {
            	var selectedItem = item;
            	if (typeof item === 'string') {
            		if (item.length > 0) {
            			selectedItem = this.$('a[href="#'+item+'"]').closest('li');
            		}
            		else {
            			selectedItem = this.$('a[href="#' + this.homePage + '"]').closest('li');
            		}
            	}
            	
            	if (selectedItem.length >= 1) {
            		this.$('.sidebar-menu li').removeClass('active');
            		this.$(selectedItem).addClass('active');
            	}
            },
            
            onRender: function() {
            	if (!_.isUndefined(this.model) && !_.isUndefined(this.model.attributes) && !_.isUndefined(this.model.attributes.currentLogo))
            		this.$('.customerLogo').attr('src', this.model.attributes.currentLogo);
			},
            
            processMenuItem: function(event) {
            	var currentTarget = $(event.target).closest('li');
            	var link = currentTarget.find('a');
            	var url = link.attr('href');
            	//this.$('.sidebar-menu li').removeClass('active');
            	
            	this.$('.sidebar-menu li').each(function(index, item){
            		if (!($(item).hasClass('treeview'))) $(item).removeClass('active');
            	})
            	
            	currentTarget.addClass('active');
            	App.vent.trigger('application:route', url.substring(1));
            	return false;
            },
            
            initialize: function() {
            	var self = this;
            	
            	$.get("papi/context", function(ctxt) {
            		var appVersion = ctxt.appVersion;
            		var githubTag = ctxt.githubTag;
					var branchName = ctxt.branchName;
					var buildNumber = ctxt.buildNumber;
					var buildDateTime = ctxt.buildDateTime;
					var commitRef = ctxt.commitRef;
					self.model = new Backbone.Model({
						appVersion: ctxt.appVersion,
						githubTag: ctxt.githubTag,
						branchName: ctxt.branchName,
						buildNumber: ctxt.buildNumber,
						buildDateTime: ctxt.buildDateTime,
						commitRef: ctxt.commitRef,
						currentLogo: ctxt.logoFilename
					});
            		self.render();
					var tooltipContent = '<ul>' +
							'<li><strong>GitHub Tag:</strong> ' + githubTag + '</li>' +
							'<li><strong>Branch Name:</strong> ' + branchName + '</li>' +
							'<li><strong>BuildDate and Time:</strong> ' + buildDateTime + '</li>' +
							'<li><strong>Commit Reference:</strong> ' + commitRef + '</li>' +
							'</ul>';
						self.$('.app-version').attr('data-toggle', 'tooltip');
						self.$('.app-version').attr('data-html', 'true');
						self.$('.app-version').attr('data-original-title', tooltipContent);
						self.$('.app-version').tooltip();
            	});
            }
        });
        return MainSideBarView;
    });
