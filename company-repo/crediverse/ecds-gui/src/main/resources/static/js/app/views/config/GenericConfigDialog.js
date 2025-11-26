define( ['jquery', 'underscore', 'App', 'marionette', 
         'codemirror',
         'codemirror/addon/hint/show-hint',
         ],
    function($, _, App, Marionette, CodeMirror) {
	
        //ItemView provides some default rendering logic
        var GenericConfigDialog =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	template: null,
        	
        	editorNames: [],
        	editors: [],
        	
        	variables: null,
        	textAreaHeight: 80,
        	dialogOnRender: null,
        	
        	ui: {
				trueButton: '.cond-true',
				falseButton: '.cond-false',
			},

        	events: {
        		'click .saveButton' : 'saveConfiguration',
				'click @ui.trueButton': 'changeWarning',
				'click @ui.falseButton': 'changeWarning',
        	},


			changeWarning : function(evt) {
				var el = document.getElementById("condWarning");
				if($(".cond-true").is(":checked")) {
					el.style.display="block";
				}
				else {
					el.style.display="none";
				}
			},
        	
            initialize: function(options) {
            	// Bind Register events
            	this.bindRenderMethods();
            	
            	if (!_.isUndefined(options)) {
                    if (!_.isUndefined(options.variables)) {
                    	this.variables = options.variables;
                    }
                    
                    if (!_.isUndefined(options.model)) {
                    	this.model = options.model;
                    }

                    if (!_.isUndefined(options.template)) {
                    	this.template = options.template;
                    }

                    if (!_.isUndefined(options.textAreaHeight)) {
                    	this.textAreaHeight = options.textAreaHeight;
                    }
                    
                    //dialogOnRender
                    if (!_.isUndefined(options.dialogOnRender)) {
                    	this.dialogOnRender = options.dialogOnRender;
                    }

            	}
                
                this.editorNames = [];
                this.editors = []
            },
            
            bindRenderMethods: function() {
                _.bindAll(this, 'beforeRender', 'render', 'afterRender'); 
                var self = this; 
                this.render = _.wrap(this.render, function(render) { 
                	self.beforeRender(); 
                    render(); 
                    self.afterRender(); 
                    return self; 
                });
            },

            beforeRender: function() { 
            },

            // Assumes that the name is of the format: variable[texts][en]
            extractNameLanguage: function(name) {
            	var variableName = name.substr(0, name.indexOf("\[texts"));
            	variableName = variableName.replace("\]", "");
            	variableName = variableName.replace("\[", ".");
            	
            	var lang = name.substr(name.length-3, 2);
            	return {
            		name: variableName,
            		lang: lang,
            	};
            },
            
            // Create notification Area
            registerHelper: function(textArea, nameLanguage) {
            	var name = nameLanguage.name;
            	
            	// Check if name contains a period
            	var pos =  nameLanguage.name.indexOf('.');
            	if (pos > 0) {
            		name = name.substring(0, pos);
            	}
            	
            	var vars = this.variables[name][nameLanguage.lang];
            	var modeName = nameLanguage.name + "_" + nameLanguage.lang;

                CodeMirror.defineMode(modeName, function() {
                    
                    return {
                        token: function(stream,state) {
                        	if (stream.match(/^{\w+\}/)) {
                        		return "variable";
                        	} else {
                            	stream.next();
                                return null;
                        	}
                        }
                    };                    
                });
                
                CodeMirror.registerHelper("hintWords", modeName, vars);
                
                return modeName;
            },
            
            createEditor: function(textArea, modeName) {
            	//Big hack since CodeMirror is horrible to work with...
            	//It hides your original component and inserts a bunch of
            	//hacky divs. Accessing the resultant div to alter 
            	//its styling is one big mess.  Split original notification
            	//code with new non-notification code for single line strings.            	
            	var codeMirrorDiv = null;
            	if(!$(textArea).hasClass("not-notification"))
    			{
	    			var editor = CodeMirror.fromTextArea(textArea, {
	    				lineWrapping: true,
	    			    mode: modeName,
	    			    extraKeys: {"Ctrl-Space": "autocomplete"},
	    			});
		    			
    				editor.setSize("70%",this.textAreaHeight + "px");
    				//Hack to give the Code Mirror div an actual form-id.  This was requested from QA for their automation testing tools.
    				codeMirrorDiv = $( textArea ).next();
    				var codeMirrorName = $(textArea).attr('name').replace(/\[/g, '_').replace(/]/g, '')  + 'CodeMirror';
	    			codeMirrorDiv.attr('id', codeMirrorName);	    			
        			// By inserting text after control linked, the \n 
        			// getting lost in textarea is solved
        			try {
            			var ref = modeName.replace("_", ".texts.");
            			var text = this.model.get(ref);
            			if(!_.isUndefined(text))
            				editor.setValue(text);
        			} catch(err) {
        				App.error(err);
        			}
    			} else {
    				var editor = CodeMirror.fromTextArea(textArea, {
	    				lineWrapping: false,
	    			    mode: modeName,
	    			    extraKeys: {"Ctrl-Space": "autocomplete"},
	    			    scrollbarStyle: null
	    			});
	    			editor.setSize("100%","53px");
	    			//Hack to give the Code Mirror div an actual form-id and to adjust margin alignment.
	    			codeMirrorDiv = $( textArea ).next();
	    			codeMirrorDiv.css('margin-left',0+'px');
	    			codeMirrorDiv.attr('id',  $(textArea).attr('id') + 'CodeMirror');
	    			editor.on("beforeChange", function(cm, changeObj) {
	    			    var typedNewLine = changeObj.origin == '+input' && typeof changeObj.text == "object" && changeObj.text.join("") == "";
	    			    if (typedNewLine) {
	    			        return changeObj.cancel();
	    			    }

	    			    var pastedNewLine = changeObj.origin == 'paste' && typeof changeObj.text == "object" && changeObj.text.length > 1;
	    			    if (pastedNewLine) {
	    			        var newText = changeObj.text.join(" ");
	    			        return changeObj.update(null, null, [newText]);
	    			    }
	    			    return null;
	    			});
    			}
    			
    			return editor;
            },

        	onRender: function(options) {
        		try {
        			var self = this;
        			this.$('.tagboxsetting').tagsinput();
        			
        			if (this.dialogOnRender != null && _.isFunction(this.dialogOnRender)) {
        				this.dialogOnRender(this.$el);
        			}
        			
        			if (this.editors.length == 0) {
        				this.codeMirrorAdded = true;
                		this.$el.find(".notifyArea").each(function() {
                			//Register Text Area details and drop down hint list
                			var nameLanguage;
                			if($(this).hasClass("not-notification")) {
                				nameLanguage = {name: this.name, lang: "en"};
                			} else {
	                			nameLanguage = self.extractNameLanguage(this.name);
                			}
                			var modeName = self.registerHelper(this, nameLanguage);
                			self.editorNames.push(nameLanguage);
                			//Create and save ref. to editor
                			var cmed = self.createEditor(this, modeName);
                			self.editors.push(cmed);
                		});
        			}
        			
        			var form = this.$el.find("form");
        			this.model.bind(form);
        		} catch(err) {
        			App.error(err);
        		}
        	},
            
        	refreshEditors: function(context) {
        		try {
            		var CTX = (_.isUndefined(context))? this : context;
            		for(var i=0; i<CTX.editors.length; i++) {
            			CTX.editors[i].refresh();	
                	}        		
        		} catch(err) {
        			App.error(err);
        		}
        	},
        	
            afterRender: function() {
            	var self = this;
            	this.$el.on('shown.bs.tab', 'a[data-toggle="tab"]', function() {
            		self.refreshEditors(self);
            	});
            	
            	setTimeout(function() {
            		self.refreshEditors(self);
            	}, 500);
            },
        	
            saveConfiguration: function(ev) {
            	var self = this;
				var canSave = true;
				if (!_.isUndefined(this.beforeSaveConfiguration) && _.isFunction(this.beforeSaveConfiguration)) {
					canSave = this.beforeSaveConfiguration();
				}
				if (canSave) {
					this.model.save({
						preprocess: function(data) {
							if (!_.isUndefined(self.model.processBeforeSave) && _.isFunction(self.model.processBeforeSave)) {
								self.model.processBeforeSave(data);
							}
							for(var i=0; i<self.editors.length; i++) {
								var nl = self.editorNames[i];
								var name = nl.name.split(".");
								//TODO: Cleanup!!!!    	            		
								if (name.length == 1) {
									if(!_.isUndefined(data[nl.name]["texts"]) && !_.isUndefined(data[nl.name]["texts"][nl.lang])) {
										data[nl.name]["texts"][nl.lang] = self.editors[i].getValue();
									} else { //for Code Mirror editors that aren't language specific, like TDR filenames and directories and USSD strings
										data[nl.name] = self.editors[i].getValue();
									}
								} else if (name.length == 2) {
									data[name[0]][name[1]]["texts"][nl.lang] = self.editors[i].getValue();
								}
									
							}
							return data;
						},
						success: function(mdl, resp) {
							var dialog = self.$el.closest('.modal');
							dialog.modal('hide');
						},
						error: function(mdl, error) {
							App.error(error);
						}
					});
				}
            }
        });
        
        return GenericConfigDialog;
	}
);
	
