define([ 'App', 'jquery', 'underscore', 'marionette', 'backbone', 'models/FeatureBarModel',  'jquery.slimscroll'],
    function (App, $, _, Marionette, Backbone, FeatureBarModel) {
        //ItemView provides some default rendering logic
        var MainSideBarView =  Marionette.ItemView.extend({
            template:'Dashboard#mainsidebar',
            tagName: 'div',
        	attributes: {
        		id: "mainsidebar"
        	},

			featureBar: null,
            // View Event Handlers
            events: {
            	'click .menuentry': 'processMenuItem'
            },

			onRender: function() {
				if (!_.isUndefined(this.model) && !_.isUndefined(this.model.attributes) && !_.isUndefined(this.model.attributes.currentLogo))
					this.$('.customerLogo').attr('src', this.model.attributes.currentLogo);
				this.$('.sidebar').slimScroll({
					height: ($(window).height() - $(".main-header").height()) + "px",
					color: "rgba(0,150,204,1)",
					railColor: "#888888",
					railOpacity: "0.5",
					railVisible: true,
			    });
			},
            
            setActive: function(item) {
            	var selectedItem = item;
            	if (typeof item === 'string') {
            		if (item.length > 0) {
            			selectedItem = this.$('a[href="#'+item+'"]').closest('li');
            		}
            		else {
            			selectedItem = this.$('a[href="#accountList"]').closest('li');
            		}
            	}
            	
            	if (selectedItem.length >= 1) {
            		this.$('.sidebar-menu li').removeClass('active');
            		this.$(selectedItem).addClass('active');
            	}
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
            	$.get("api/context", function(ctxt) {
					self.featureBar = new FeatureBarModel();
					var appVersion = ctxt.appVersion;
					var githubTag = ctxt.githubTag;
					var branchName = ctxt.branchName;
					var buildNumber = ctxt.buildNumber;
					var buildDateTime = ctxt.buildDateTime;
					var commitRef = ctxt.commitRef;
					//            		self.model.set("appVersion", ctxt.appVersion);

					self.model = new Backbone.Model({
						appVersion: ctxt.appVersion,
						githubTag: ctxt.githubTag,
						branchName: ctxt.branchName,
						buildNumber: ctxt.buildNumber,
						buildDateTime: ctxt.buildDateTime,
						commitRef: ctxt.commitRef,
						currentLogo: ctxt.logoFilename
					});

					const featureName = 'viewReportsByAreaFeature';

					self.featureBar.setFeature(featureName);
					self.featureBar.fetch().then(isEnabled => {
						self.model.set(featureName, isEnabled ? "enabled" : "disabled");
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
				});
			}
		});
		return MainSideBarView;
    });
