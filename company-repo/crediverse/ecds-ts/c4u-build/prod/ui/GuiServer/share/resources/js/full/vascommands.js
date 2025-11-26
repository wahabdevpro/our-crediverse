!function($) {

	'use strict';
	
	var VasCommandController = function(element, options, e) {
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}
		
		// DOM Controls
		this.$element = $(element);
		
		// Properties
		this.id = null; // This is the same name as the variable being modified
		this.field = options.field;			//session field
		this.servlet = options.servlet;		//servlet to handle page creation / update / remove
		this.title = "VAS Commands";
		this.dataservlet = options.dataservlet;
		this.useFilteredTable = options.useFilteredTable;
		this.vasCommandsInputCssClass = options.vasCommandsInputCssClass;
		this.$content = null;
		// Constructor
		this.init();
	};
	
	VasCommandController.prototype = {
		vasContectID : "vascontent",
		currentOption : null,	//"add", "edit", "view"
		currentIndex : -1,
		vasCommands : [],
			
		init : function() {
			var self = this, id = this.$element.attr("id");
			this.id = id;
			this.createControlTemplate();
			this.refreshHandlers();
			
			this.refreshTableContent();
			this.refreshProcessList();
			this.vasCommandExtractor();
		},
		
		vasCommandExtractor : function() {
			try {
				var self = this;
				var data2Send = "act=cmds";
				var status = $.ajax({
					type: "POST", 
					url: self.servlet, 
					async: true,
					dataType: "json",
					data: data2Send
				}).done(function(data) {
					if (data.status == "fail")
					{
						console.error("VAS command retrieval failed");
						console.error(data.message)
					}
					else
					{
						self.vasCommands = data.cmds;
						$.proxy(self.vasCommandsToInputs(), self);
					}
				});
			} catch(err) {
				console.error(err);
			}
		},
		
		vasCommandsToInputs : function() {
			//vasCommandsInputClass
			try {
				var data = new Array();
				var self = this;
			    var info = $.map(self.vasCommands ,function(value,i) {
			        return {id: i, 'name':'{' + self.vasCommands[i] + '}','desc': ""};
			    });
			    
				$(self.vasCommandsInputCssClass).atwho({ 
			        at: "{",
			        alias: "",
			        tpl: "<li data-value='${name}'>${name}<small>${desc}</small></li>",
			        'data':info,
			        limit: 20,
			        show_the_at: true,
			        'start_with_space': false,
					insert_tpl: "<span class='variable-formatting'>${name}</span>",
			        'callbacks': {
			            remote_filter: function (query, callback) {
			                callback(null);
			            },
			            filter: function (query, data, search_key) {
			                return this.call_default("filter", query, data, search_key);
			            }
			        }
			    });
				
				$(self.vasCommandsInputCssClass).keydown(function(event) {
					if (event.which) {
						if ((event.which == '8' || event.which == '46')) {
							dataUpdated();
						}
					}
				});
			    
			} catch(err) {
				console.error(err);
			}
		},
		
		refreshHandlers : function() {
			try {
				this.$element.on("click", ".btnvascmdadd", $.proxy(this.vasAddCommandHandler, this));
				$("#vasCommandModalSave").on("click", "", $.proxy(this.modalSaveHandler, this));
			} catch(err) {
				console.error(err);
			}
		},
		
		createControlTemplate : function() {
			var html = [];
			var $header = $(this.createTitle());
			
			this.$element.html($header);
			this.$content = this.$element.find("#" + this.vasContectID);
		},
		
		extraIndex : function(evntInfo) {
			try {
				var element = evntInfo.target;
				var id = $(element).parent().attr("id");
				var arr = id.split("_");
				return parseInt(arr[1]);
			} catch(err) {
				console.error(err);
			}
			return -1;
		},
		
		vasAddCommandHandler : function(evntInfo) {
			try {
				this.currentOption = "add";
				this.currentIndex = -1;
				this.showModal("Add Command", "add");
			} catch(err) {
				console.error(err);
			}
		},
		
		vasViewCommandHandler : function(evntInfo) {
			try {
				this.currentOption = "view";
				this.currentIndex = this.extraIndex(evntInfo);
				this.showModal("View Command", "view");
				componentInfoRequest(this.field, this.currentIndex, "vasCommandModal", updateConfigInfoDialog);
			} catch(err) {
				console.error(err);
			}
		},
		
		vasEditCommandHandler : function(evntInfo) {
			try {
				this.currentOption = "edit";
				this.currentIndex = this.extraIndex(evntInfo);
				this.showModal("Edit Command", "edit");
				componentInfoRequest(this.field, this.currentIndex, "vasCommandModal", updateConfigInfoDialog);
			} catch(err) {
				console.error(err);
			}
		},
		
		vasDeleteCommandHandler : function(evntInfo) {
			try {
				this.currentIndex = this.extraIndex(evntInfo);
				var self = this;
				this.confirmDelete( function (result) {
					if (result) {
						$.proxy(self.removeVasCommand(self.currentIndex), self);
					}
				});
			} catch(err) {
				console.error(err);
			}
		},
		
		modalSaveHandler : function(eventInfo) {
			try {
				this.persistModal();
			} catch(err) {
				console.error(err);
			}
		},
		
		persistModal : function() {
			try {
				var self = this;
				$("#vasCommandModalWaiting").removeClass("hide");
				var requestData = $("#vasCommandModalForm").serialize({ checkboxesAsBools: true });
				var data2Send = "act=upd&comp=" + this.field;
				if ((typeof this.currentIndex !== 'undefined') && (this.currentIndex != null)) {
					data2Send += "&index=" + this.currentIndex;
				}
				data2Send += "&" + requestData;
				
				var status = $.ajax({
					type: "POST", 
					url: self.dataservlet, 
					async: true,
					dataType: "json",
					data: data2Send
				}).done(function(data) {
					if (data.status == "fail")
					{
						$("#vasCommandModalError").removeClass("hide");
						$("#vasCommandModalError").html(data.message);
					}
					else
					{
						dataUpdated();
						$("#vasCommandModalError").addClass("hide");
						$("#vasCommandModal").modal('hide');
						self.refreshTableContent();
					}
				});
			} catch(err) {
				console.error("persistModalData: " + err);
			}
		},
		
		removeVasCommand : function(index) {
			var self = this;
			var data2Send = "act=del&index="+index+"&comp="+this.field;
			
			var status = $.ajax({
				type: "POST", 
				url: self.dataservlet, 
				async: true,
				dataType: "json",
				data: data2Send
			}).done(function(data) {
				try {
					resetSessionTimeout();
					if (data.status=="fail")
					{
						$("#delErrorMessage").html(data.message);
						$("#delErrorMessage").removeClass("hide");
					}
					else
					{
						dataUpdated();
						self.refreshTableContent();
					}	
				} catch(err) {
					console.error("validateAndDelete: " + err);
				}

			});
		},
		
		// Confirmation for delete
		confirmDelete : function(callback) {
			var bsdialog = new BootstrapDialog(
					{
						title : "Confirm Delete",
						message : "Confirm Vas Command removal?",
						type : BootstrapDialog.TYPE_DANGER,
						closable : true,
						data : {
							'callback' : callback
						},
						buttons : [
								{
									id : "btnCancel",
									label : "Cancel",
									action : function(dialog) {
										typeof dialog.getData('callback') === 'function'
												&& dialog.getData('callback')(
														false);
										dialog.close();
									}
								},
								{
									id : "btnRemove",
									label : "Delete",
									cssClass : 'btn-danger',
									action : function(dialog) {
										typeof dialog.getData('callback') === 'function'
												&& dialog.getData('callback')(
														true);
										dialog.close();
									}
								} ]
					}).open();

		},
		
		showModal : function(title, mode) {
			try {
				if (mode == "add") {
					$('#vasCommandModal select').val(1);
					$('.selectpicker').selectpicker('refresh')
				}
				
				$("#vasCommandModalWaiting").addClass("hide");
				$("#vasCommandModal span[id$='_error']").parent("div").removeClass("has-error");
				$("#vasCommandModal span[id$='_error']").addClass("hide");
				
				$("#vasCommandModalForm").trigger("reset"); //Form
				$("#vasCommandModalTitle").html(title);
				$("#vasCommandModalError").addClass("hide");
				
				if (mode == "view") {
					$("#vasCommandModal").find(".savebtn").hide();
				} else {
					$("#vasCommandModal").find(".savebtn").html("Save");
					$("#vasCommandModal").find(".savebtn").show();
				}
				$("#vasCommandModalForm :input").prop("disabled", (mode == "view"));
				$("#vasCommandModal").modal("show");
			} catch(err) {
				console.error("showModal: " + err);
			}
		},
		
		createTitle : function() {
			var html= [];
			html[html.length] = "<div class='row' >";
			html[html.length] = "<div class='col-xs-11 col-sm-5 col-md-7 user_heading' style='margin-left: 20px;'>";
			html[html.length] = this.title;
			html[html.length] = "</div>";
			html[html.length] = "<div class='col-xs-6 col-md-4' style='float:right; margin-right:20px' >";
			html[html.length] = "<button type='button' class='btn btn-success btn-xs btnvascmdadd' style='float:right;' data-toggle='modal' title='add vas comamnd'><span class='glyphicon glyphicon-plus'></span></button>";
			html[html.length] = "</div>";
			html[html.length] = "</div>";
			html[html.length] = "<div id='";
			html[html.length] = this.vasContectID;
			html[html.length] = "'></div>";
			return html.join("");
		},
		
		findElement : function(modalId, elementKey) {
			var lowerkey = elementKey.toLowerCase();
			var $element = $("#" + modalId + " *").filter(function() {
				if (typeof $(this).attr('id') === 'undefined')
					return false;
				else
				{
					var id = $(this).attr('id').toLowerCase();
					return (lowerkey == id);
				}
			});
			if ($element != null && $element.length > 0)
				return $element;
			else
				return null;
		},
		
		refreshProcessList : function() {
			try {
				var self = this;
				var status = $.ajax({
					type: "POST", 
					url: self.servlet,
					dataType : "json",
					async: true,
					data: "act=process&field=" + self.field
				}).done(function(data) {
					try {
						var html = self.createProcessList(data);
						var $processElement = self.findElement("vasCommandModal", "process");
						
						if ($processElement != null) {
							$processElement.html(html);
							$(".selectpicker").selectpicker("refresh");
						}
					} catch (err) {
						console.error("refreshProcessList: " + err);
					}
				}).fail(function(error) {

				});
			} catch(err) {
				console.error(err);
			}
		},
		
		createProcessList : function(data) {
			var html = [];
			try {
				var arr= [];
				for (var key in data) {
					arr[arr.length] = key;
				}
				arr.sort();
				for(var i=0; i<arr.length; i++) {
					html[html.length] = "<option value='";
					html[html.length] = arr[i];
					html[html.length] = "'>";
					html[html.length] = data[arr[i]];
					html[html.length] = "</option>";
				}
			} catch(err) {
				console.error(err);
			}
			return html.join("");
		},
		
		refreshTableContent : function() {
			try {
				var self = this;
				var status = $.ajax({
					type: "POST", 
					url: self.servlet, 
					async: true,
					data: "act=table&field=" + self.field
				}).done(function(data) {
					try {
						var $html = $(data);
						$html.on("click", ".vasviewcmd", $.proxy(self.vasViewCommandHandler, self));
						$html.on("click", ".vaseditcmd", $.proxy(self.vasEditCommandHandler, self));
						$html.on("click", ".vasdeletecmd", $.proxy(self.vasDeleteCommandHandler, self));
						self.$content.html($html);
					} catch (err) {
						console.error(err);
					}
					try {
						if (self.useFilteredTable) {
							updateDataTable("vascommandtable");
						}
					} catch(err) {}
				}).fail(function(error) {

				});
			} catch(err) {
				console.error("refreshTableContent:" + err);
			}
		}
	};
	
	$.fn.vasCommandController = function(option, event) {
		// get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this
				.each(function() {
					var $this = $(this), data = $this
							.data('resultCodeController'), options = typeof option == 'object'
							&& option;

					if (!data) {
						var mergedOptions = $.extend({},
								$.fn.vasCommandController.defaults, options, $(
										this).data());
						$this.data('resultCodeController',
								(data = new VasCommandController(this,
										mergedOptions, event)));
					}

					result = data;
					var elementId = $(this).attr("id");
				});
		return result;
	};

	// Model defaults
	$.fn.vasCommandController.defaults = {
		currentLanguageIndex : 0,
		updateMadeToPageMethod : dataUpdated,
		field : null,
		servlet : "",
		dataservlet : "",
		vasCommandsInputCssClass : ".vasinputor",
		useFilteredTable: true,
	};
	
}(jQuery, window, document);