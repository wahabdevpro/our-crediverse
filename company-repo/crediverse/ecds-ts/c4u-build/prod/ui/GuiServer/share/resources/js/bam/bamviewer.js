////////////////////////////////////////////////////////////////////////////////////////
//
// Global Variables
//
// /////////////////////////////////

var inMetrics = false;
var numberOfPlaceholders = 504;

var minimumX = 5;
var minimumY = 4;

var settingsAnimationDuration = 250;
var bamCloseCheck = false;
var currentJson = null;
var saveInterval = null;

var widgetDataPollingRef = null;
var pollingRef = null;

var MAX_CONCURRENT_REQUESTS = 5;
var currentConcurrentRequests = 0;
var ajaxPool = [];

////////////////////////////////////////////////////////////////////////////////////////
//
// Setup Methods
//
// /////////////////////////////////

// When the document is ready
$(document).ready(function() {
	
	try {
		
		// Update the grid placeholders
		updatePlaceholder();
		
		// Update the sortable grids
		updateSortables();
		
		// Sets the widget panel up
		setupWidgetSettingsPanel();
		
		// Loads the users layout
		loadLayout();
		
		// Save interval for the layout
		saveInterval = setInterval(function() {
			
			try {
				
				// Ensures the bam conent still exists
				if ($("#bam-content").length) {
					
					// Gets the layout from the grid
					var json = LayoutToJson();
					
					// Ensures the current json is not the same as before
					if (currentJson != json) {
						
						// Saves the layout
						saveLayout();
						
					}
					
				}
				
			} catch (e) {
				
				console.log("bamviewer.ready: " + e);
				
			}
			
		}, 60000);
		
		// Checks bam when the page is closed
		bamCloseCheck = true;
		
		// Monitors the widget data
		widgetDataPoll();
		
		// Polls the backend for data for the widgets
		poll();
		
		// Shows the main section of the grid
		$(".main-section").show();
		
	} catch (e) {
		
		console.log("bamviewer.ready: " + e);
		
	}
	
});

// Executed before navigating away from the bam page
function beforeClosingNavigateAway(newmenu, newsystem) {
	
	try {
		
		// Set the flag to false
		bamCloseCheck = false;
		
		// Get the json layout
		var json = LayoutToJson();
		
		// Check if the current json is not equal to the saved json
		if (currentJson != json) {
			
			// Create a message asking if you want to save the layout
			var msg = [];
			msg[msg.length] = "<div style='height:70px;'><div style='float:left; width:80px;margin:10px auto; height:50px; padding-left:20px;'><img src='/img/timeout-orange.png'/></div>";
			msg[msg.length] = "<div style='float:left; width:400px; padding-left:30px; padding-top:10px;'>";
			msg[msg.length] = "<p>The layout of Business Activity Monitoring has not been saved.</p>";
			msg[msg.length] = "<p style='font-weight: bold'>Do you want to save the layout?</p>";
			msg[msg.length] = "</div></div>";
			
			// Show the dialog
			var dialog = BootstrapDialog.show({
		        type: BootstrapDialog.TYPE_DEFAULT,
		        title: "<b>Save Layout?</b>",
		        message: msg.join(""),
		        onhide: function(dialog){
		        },
				buttons: [{
				          label: "Yes",
				          cssClass: "btn-primary",
				          action : function(dialog) {
				        	  
				        	  // If the user clicked yes
				        	  
				        	  // Close the dialog
				        	  dialog.close();
				        	  
				        	  // Save the layout
				        	  saveLayout();
				        	  
				        	  // Navigate away
				        	  navigateAway(newmenu, newsystem);
				        		  
				          }
					}, {
				          label: 'No',
				          cssClass: "btn-danger",
				          action : function(dialog) {
				        	  
				        	  // If no was selected
				        	  
				        	  // Close the dialog
				        	  dialog.close();
				        	  
				        	  // Navigate away
				        	  navigateAway(newmenu, newsystem);
				          }
					}, {
						  label: "Stay On Page",
						  action: function(dialog) {
							  
							  // If stay on page was selected
							  
							  // Just close the dialog
							  dialog.close();
						  }
					}
				]
		    });
			
		} else {
			
			// Else just navigate away
			navigateAway(newmenu, newsystem);
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.beforeClosingNavigateAway: " + e);
		
	}
	
}

// Navigates away from the current bam page
function navigateAway(newmenu, newsystem) {
	
	try {
		
		// Closes all bam connections
		closeAllConnections();	
		
		// Navigates away
		navigateTo(newmenu, newsystem, true);
		
	} catch (e) {
		
		console.log("bamviewer.navigateAway: " + e);
		
	}
	
}

// Before the page is unloaded
window.onbeforeunload = function() {
	
	try {
		
		// Close all connections
		closeAllConnections();
		
	} catch (e) {
		
		console.log("bamviewer.onbeforeunload: " + e);
		
	}
	
}

// Closes the bam connections to the backend
function closeAllConnections() {
	
	try {
		
		// Iterate through each widget
		$(".bam-sortable li.metric-widget").each(function() {
			
			// Get the widget from the html element
			var widget = $(this).data("widget");
			
			// Check if the widget exists
			if (widget) {
				
				// Get the ajax reference
				if (widget.ajaxRef) {
					
					// Abort the ajax calls
					widget.ajaxRef.abort();
					
				}
				
				// Destroy the widget
				widget.destroy();
				widget = null;
				
				// Set the data to null
				$(this).data("widget", null);
				
			}
			
		});
		
		// Clear the intervals
		clearInterval(saveInterval);
		
		// Stop the timers
		clearTimeout(widgetDataPollingRef);
		clearTimeout(pollingRef);
		
		// Make it the light theme
		makeLightTheme();
		
	} catch (e) {
		
		console.log("bamviewer.closeAllConnections: " + e);
		
	}
}

// Updates the placeholders for the grid
function updatePlaceholder() {
	
	try {
		
		// Iterate through the number of placeholders
		for (var i = 0; i < numberOfPlaceholders; i++) {
			
			// Add the placeholder to the bam content
			$("#bam-content ul").append("<li class='sortable-placeholder' draggable='true'></li>");
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.updatePlaceholder: " + e);
		
	}
	
}

// Sets up the widget settings panel
function setupWidgetSettingsPanel() {
	
	try {
		
		// Get the list of graph types
		var propertiesList = $(".graph-type-list");
		
		// Iterate through the graph types
		for (var key in graphType) {
			
			// Add it to the list
			propertiesList.append("<li><a data-toggle='tooltip' data-placement='top' title='" + key.toLowerCase() + "'><img class='graph-type-icon' src='/img/bam/graph_types/" + key.toLowerCase() + "_chart.png'/></a></li>");
			
		}
		
		// Set the selector to 10
		$(".widget-selector").val("10");
		
		// Get the theme list
		var themesList = $(".widget-themes-list");
		
		// Empty the list
		$(themesList).empty();
		
		// Iterate through the themes
		for (var i = 0; i < themes.length; i++) {
			
			// Check if it is the current theme
			if (i == currentThemeIndex) {
				
				// Append the active theme to the list
				themesList.append("<li class='current-theme'><a href='#'>" + themes[i]["name"] + "</a></li>");
				
			} else {
				
				// Else just append a theme to the list
				themesList.append("<li><a href='#'>" + themes[i]["name"] + "</a></li>");
				
			}
			
		}
		
		// Add a function to the themes button
		$(".widget-themes-list li").click(function() {
			
			// Get the current theme index
			currentThemeIndex = $(this).index();
			
			// Get the list of themes
			var list = $(".widget-themes-list li");
			
			// Iterate through the themes
			for (var i = 0; i < themes.length; i++) {
				
				// If the theme is equal to the current index
				if (i == currentThemeIndex) {
					
					// Add the current theme class
					$(this).addClass("current-theme");
					
				} else {
					
					// Else remove the previous theme
					$($(list).get(i)).removeClass("current-theme");
					
				}
				
			}
			
			// Get the dark theme option
			var dark = themes[currentThemeIndex]["dark"];
			
			// If it is set to dark
			if (dark) {
				
				// Make the panel dark
				makeDarkTheme();		
				
			} else {
				
				// Else make it light
				makeLightTheme();	
				
			}
			
			// Iterate through the widgets
			$(".bam-sortable li.metric-widget").each(function() {
				
				// Get the widget
				var widget = $(this).data("widget");
				
				// Set the changed flag
				widget.changed = true;
				
				// Update the widget
				widget.update();
				
			});
			
		});
		
	} catch (e) {
		
		console.log("bamviewer.setupWidgetSettingsPanel: " + e);
		
	}
	
}

// Sets what elements are draggable and which are droppable
function updateSortables() {
	
	try {
		
		// Iterate through the draggables
		$(draggable).each(function(index) {
			
			// Add the drag and mouse functions to them
			$(this).off(DRAG_START, DragStarted).on(DRAG_START, DragStarted)
				   .off(DRAG_END, DragEnded).on(DRAG_END, DragEnded)
			 	   .off(DRAG_OVER, DragOver).on(DRAG_OVER, DragDropDefault)
			 	   .off(DRAG_ENTER, DragDropDefault).on(DRAG_ENTER, DragDropDefault)
			 	   .off(DRAG_EXITED, DragDropDefault).on(DRAG_EXITED, DragDropDefault)
			 	   .off(DROP, DragDropDefault).on(DROP, DragDropDefault)
			 	   .off(MOUSE_ENTER, MouseEntered).on(MOUSE_ENTER, MouseEntered)
			 	   .off(MOUSE_LEAVE, MouseLeave).on(MOUSE_LEAVE, MouseLeave);
			
		});
		
		// Iterate through the droppables
		$(droppable).each(function(index) {
			
			// Add the drop functions
			$(this).off(DRAG_OVER, DragOver).on(DRAG_OVER, DragOver)
				   .off(DROP, Drop).on(DROP, Drop)
			 	   .off(DRAG_ENTER, DragDropDefault).on(DRAG_ENTER, DragDropDefault)
			 	   .off(DRAG_EXITED, DragDropDefault).on(DRAG_EXITED, DragDropDefault)
			 	   .off(DRAG_START, DragDropDefault).on(DRAG_START, DragDropDefault)
				   .off(DRAG_END, DragDropDefault).on(DRAG_END, DragDropDefault);
			
		});
		
	} catch (e) {
		
		console.log("bamviewer.updateSortables: " + e);
		
	}
	
}

// Updates the draggables when resizing
function resizeDraggable(resize) {
	
	try {
		
		// Adds the drag functions to the draggables
		$(resize).off(DRAG_START, DragStarted).on(DRAG_START, DragStarted)
	 	   .off(DRAG_END, DragEnded).on(DRAG_END, DragEnded)
	 	   .off(DRAG_ENTER, DragDropDefault).on(DRAG_ENTER, DragDropDefault)
	 	   .off(DRAG_EXITED, DragDropDefault).on(DRAG_EXITED, DragDropDefault)
	 	   .off(DRAG_OVER, DragDropDefault).on(DRAG_OVER, DragDropDefault)
	 	   .off(DROP, DragDropDefault).on(DROP, DragDropDefault);
		
	} catch (e) {
		
		console.log("bamviewer.resizeDraggable: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Bam Viewer Interaction
//
// /////////////////////////////////

// Opens the bam view menu on the left
function openMenu() {

	try {
		
		// Calls an ajax call to get the list of available plugins
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
			data : "act=available",
			dataType : "json",
			success : function(data) {
				
				// Ensure the data is valid
				if (data && data.components.length) {

					// Remove the current components
					$(".components-menu li").remove();

					// Sore the components
					data.components.sort();
					
					// Iterate through the components
					for (var i = 0; i < data.components.length; i++) {
						
						// Add the component to the list of components
						$(".components-menu").append("<li id='component-to-metric" + i + "'>" + data.components[i] + 
								"<span class='component-glyphicon glyphicon glyphicon-chevron-right'></span></li>");
						
						// Add the click function to the component item
						$("#component-to-metric" + i).click(loadMetricsForComponent);
					}
				}
			}
		});

		// Set the minimum width for the grid
		$(".bam-sortable").css("min-width", $(".bam-sortable").outerWidth());
		
		// Set the size of the side menu
		$("#side-menu").removeClass("col-lg-0 col-md-0 col-xs-0");
		$("#side-menu").addClass("col-lg-2 col-md-2 col-xs-2");

		// Set the image of the '>'
		$(".toggler-glyph").removeClass("glyphicon-chevron-right");
		$(".toggler-glyph").addClass("glyphicon-chevron-left");
		$(".toggler").css("margin-left", "0px");
		$(".toggler").css("opacity", "1.0");
		
	} catch (e) {
		
		console.log("bamviewer.openMenu: " + e);
		
	}
	
}

// Closes the menu on the left
function closeMenu() {

	try {
		
		// Sets the minimum width to 0
		$(".bam-sortable").css("min-width", "0px");
		
		// Hides the side menu
		$("#side-menu").removeClass("col-lg-2 col-md-2 col-xs-2");
		$("#side-menu").addClass("col-lg-0 col-md-0 col-xs-0");

		// Sets the image of the toggler
		$(".toggler-glyph").removeClass("glyphicon-chevron-left");
		$(".toggler-glyph").addClass("glyphicon-chevron-right");
		$(".toggler").removeAttr("style");
		$(".toggler").css("margin-left", "-5px");

		// If in the metrics menu, the click the back button
		if (inMetrics)
			$(".side-menu-carousel-back").click();
		
	} catch (e) {
		
		console.log("bamviewer.closeMenu: " + e);
		
	}
	
}

// Loads the mtrics from the plugin
function loadMetricsForComponent() {
	
	try {
		
		// Calls the get metrics function
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
			data : "act=getmetric&comp=" + $(this).text(),
			dataType : "json",
			success : function(data) {
				try {
					
					// Ensure the data is valid
					if (data && data.metrics.length) {

						// Remove the metrics from the list
						$(".metrics-menu li").remove();
						
						// Sore the metrics
						data.metrics.sort();

						// Iterate through the metrics
						for (var i = 0; i < data.metrics.length; i++) {
							
							// Add the metrics to the list of metrics
							$(".metrics-menu").append(createWidgetListItem(data.uid, data.metrics[i]));
							
							// Create the widget
							var widget = new BamWidget($(".metrics-menu li").get(i), data.uid, data.metrics[i], $("li.sortable-placeholder"), {
								
								// Assign the update function
								update: function() {
									
									try {
										
										// Convert it to flot charts
										BamToFlot(this);
										
									} catch (e) {
										// Do Something																		
									}
									
								}
								
							});
							
							// Set the widget object to the metric item
							$($(".metrics-menu li").get(i)).data("widget", widget);
							
						}
						
						// Update the draggables and droppables
						updateSortables();
						
					}
					
				} catch (e) {
					// Do Something
				}
			}
		});
		
		// Show the metrics list
		$("#side-menu-carousel").carousel('next');
		
		// Set the flag that in the metrics
		inMetrics = true;
		
	} catch (e) {
		
		console.log("bamviewer.loadMetricsForComponent: " + e);
		
	}
	
}

// Register the widget to the backend
function registerWidget(widget) {
	
	try {
		
		// Call the register method
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
			data : "act=register&uid=" + widget.uid + "&met=" + widget.name,
			dataType : "json",
			success: function(result) {
				
				try {
					
					// Check if it was registered
					if (result.registered) {
						
						// Start the widget
						widget.start();
						
					}
					
				} catch (e) {
					// Do Something
				}
			}
		});
		
	} catch (e) {
		
		console.log("bamviewer.registerWidget: " + e);
		
	}
	
}

// Unregister the widget from the backend
function unregisterWidget(widget) {
	
	try {
		
		// Check to make sure there isn't any other widget that contains the same metric
		var otherSame = false;
		
		// Iterate through the widgets
		$(".bam-sortable li.metric-widget").each(function() {
			
			// Get the widget
			var w = $(this).data("widget");
			
			// Check the name of the widget
			if (w.uid == widget.uid && w.name == widget.name) {
				otherSame = true;
			}
			
		});
		
		// If there are other widgets with the same name then skip
		if (otherSame)
			return;
		
		// Call the unregister method
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
			data : "act=unregister&uid=" + widget.uid + "&met=" + widget.name,
			dataType : "json",
			success: function(result) {
			}
		});
		
	} catch (e) {
		
		console.log("bamviewer.unregisterWidget: " + e);
		
	}
	
}

// Updates the widget data
function widgetDataPoll() {
	
	try {
		
		// Set the timer
		widgetDataPollingRef = setTimeout(function () {
			
			// Ensure the bam content exists
			if ($("#bam-content")) {
				
				// Iterate through the widgets
				$(".bam-sortable li.metric-widget").each(function(index) {
					
					// Get the widget
					var widget = $(this).data("widget");
					
					// Check if the widget is busy polling
					if (!widget.busyPolling) {
						
						// If not, add it to the ajax pool and set the polling flag
						ajaxPool.push(widget);
						widget.busyPolling = true;
						
					} else {
						
						// Else update the widget
						if (widget) {
							widget.update();
						}
						
					}
					
				});
				
				// Call self
				widgetDataPoll();
				
			}
			
		}, 500);
		
	} catch (e) {
		
		console.log("bamviewer.widgetDataPoll: " + e);
		
	}
	
}

// Polls the widget for data from the backend
function poll() {
	
	try {
		
		// Set the timer
		pollingRef = setTimeout(function() {
			
			// Ensure there is less than the max concurrent connections
			if (currentConcurrentRequests < MAX_CONCURRENT_REQUESTS && ajaxPool.length > 0) {
				
				// Poll the widget 
				pollWidgetForData(ajaxPool.shift());
				
			}
			
			// Call self
			poll();
			
		}, 500);
		
	} catch (e) {
		
		console.log("bamviewer.poll: " + e);
		
	}
	
}

// Gets data from the backend for the widget
function pollWidgetForData(widget) {
	
	try {
		
		// Increment the concurrent connections
		currentConcurrentRequests++;
		
		// Call the metricdata method
		widget.ajaxRef = $.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
			data : "act=metricdata&uid=" + widget.uid + "&met=" + widget.name + "&force=" + widget.forceUpdate,
			dataType : "json",
			success: function(result) {
				
				try {
					
					// Ensure the data is valid and the widget
					if (result.dimensions && widget) {
						
						// Iterate through the dimensions
						for (var i = 0; i < result.dimensions.length; i++) {
								
							// Add the dimensions to the widget
							widget.addData(i, result.dimensions[i]);
								
						}
						
						// Ensure the widget is rendering
						if (widget && widget.render && widget.update != null) {
							
							// Update the widget
							widget.update();
							
						}
							
					}
					
				} catch (e) {
					
					console.log("bamviewer.pollWidgetForData.success: " + e);
					
				}
				
			},
			complete: function() {
				
				// If completed the request
				if (widget) {
					
					// Decrement the connections and set polling flag to false
					widget.busyPolling = false;
					currentConcurrentRequests--;
					
				}
				
			}
		});
		
	} catch (e) {
		
		console.log("bamviewer.pollWidgetForData: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// General Event Handlers
//
// /////////////////////////////////

// If the toggler is pressed
$(".toggler").click(function() {

	try {
		
		// If the side menu is hidden
		if ($("#side-menu").hasClass("col-lg-0")) {
			
			// Open the menu
			openMenu();
			
		// Else if the side menu is showing
		} else {
			
			// Close the menu
			closeMenu();
			
		}
		
		// Toggle the size of the content area
		$("#bam-content").toggleClass("col-lg-10 col-md-10 col-xs-10");
		
	} catch (e) {
		
		console.log("bamviewer.togglerClick: " + e);
		
	}

});

// If the user clicks the back button in the menu
$(".side-menu-carousel-back-metrics").click(function() {
	
	try {
		
		// Move to the previous menu
		$("#side-menu-carousel").carousel('prev');
		
		// Out of the metrics menu
		inMetrics = false;
		
	} catch(e) {
		
		console.log("bamviewer.side-menu-carousel-back-click: " + e);
		
	}
	
});

// When the bam content area resizes
$("div#bam-content").resize(function() {
	
	try {
		
		// Waits for the resize to stop
		waitForFinalEvent(function() {
			
			// Iterates through the widgets
			$(".bam-sortable li.metric-widget").each(function() {
					
				// Get the widget
				var widget = $(this).data("widget");
				
				// Immediately resize the widget
				widget.immediate();
				
			});
			
		}, 50, "Business Activity Monitoring");
		
	} catch (e) {
		
		console.log("bamviewer.resize: " + e);
		
	}
	
});

////////////////////////////////////////////////////////////////////////////////////////
//
// Widget Settings Event Handlers
//
// /////////////////////////////////

// When the user clicks the remove button on the widget
$(".widget-settings-panel ul li.remove").click(function () {
	
	try {
		
		// Get the item that contains the widget
		var item = $(".widget-settings-panel").data("item");
		
		// Ensure the item is valid
		if (item.length) {
			
			// Get the widget
			var widget = $(item).data("widget");
			
			// Unregister the widget
			unregisterWidget(widget);
			
			// Destroy the widget
			widget.destroy();
			
			// Remove the filled class from the parent of the widget element
			$($(".widget-settings-panel").data("parent")).removeClass("filled");
			
			// Remove the item
			$(item).remove();
			
			// Close the widget panel
			closePanel(item);
		}
		
	} catch (e) {
		
		console.log("bamviewer.widget-settings-panel-remove-click: " + e);
		
	}
	
});

// If a graph type was clicked
$(".widget-settings-panel ul li.graph-type").click(function () {
	
	try {
		
		// Get the item that contains the widget
		var item = $(".widget-settings-panel").data("item");
		
		// Ensure the item is valid
		if (item.length) {
			
			// Get the widget
			var widget = $(item).data("widget");
			
			// If the graphs list is showing
			if ($(this).hasClass("show")) {
				
				// Hide the list
				$(this).removeClass("show");
				$(".graph-type-list").addClass("hide");
				
				// Remove the current selection
				$(".graph-type-list li").removeClass("current");
				
			} else {
				
				// Else show the graphs
				$(this).addClass("show");
				$(".graph-type-list").removeClass("hide");
				
				// Show the current graph the widget is set to
				var current = $(".graph-type-list li").get(widget.type.index);
				$(current).addClass("current");
			}
			
			// Position the panel next to the widget
			positionPanel($(".widget-settings-panel").data("settings"));
		}
		
	} catch (e) {
		
		console.log("bamviewer.widget-settings-panel-graph-type-click: " + e);
		
	}
	
});

// When a graph is selected
$(".graph-type-list li").click(function() {
	
	try {
		
		// Get the item that contains the widget
		var item = $(".widget-settings-panel").data("item");
		
		// Ensure the item is valid
		if (item.length) {
			
			// Get the widget
			var widget = $(item).data("widget");
			
			// Get the current graph selected
			var index = $(this).index();
			
			// Change the widget
			widget.changeType(index);
			
			// Check if the widget now has time involved
			if (widget.type.hasTime) {
				
				// Set the interval
				$(".widget-max-data").removeClass("hide");
				$(".widget-selector").val(widget.interval);
				
			} else {
				
				// Else hide the max data
				$(".widget-max-data").addClass("hide");
			}
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.graph-type-list-click: " + e);
		
	}
	
});

// If the widget series is clicked
$(".widget-settings-panel ul li.widget-series").click(function () {
	
	try {
		
		// Get the item that contains the widget
		var item = $(".widget-settings-panel").data("item");
		
		// Ensure the item is valid
		if (item.length) {
			
			// Get the widget
			var widget = $(item).data("widget");
			
			// Check if the series are already shown
			if ($($(this).find(".series-checkboxes")).hasClass("show")) {
				
				// Hide the series list
				$($(this).find(".series-checkboxes")).removeClass("show");
				
			} else {
				
				// Else show the series list
				$($(this).find(".series-checkboxes")).addClass("show");
				
			}
			
			// Position the panel next to the widget
			positionPanel($(".widget-settings-panel").data("settings"));
		}
		
	} catch (e) {
		
		console.log("bamviewer.widget-settings-panel-widget-series-click: " + e);
		
	}
	
});

// Prevent the propogation of the default function called when clicking on the checkbox
$(".widget-settings-panel ul li.widget-series div.series-checkboxes").click(function(e) {
	
	try {
		
		// Stops the method from 
		e.stopPropagation();
		
	} catch (e) {
		
		console.log("bamviewer.widget-settings-panel-series-checkboxes-click: " + e);
		
	}
	
});

// When the widget interval has changed
$(".widget-settings-panel li.widget-max-data div.widget-selectpicker select.widget-selector").change(function() {
	
	try {
		
		// Get the item that contains the widget
		var item = $(".widget-settings-panel").data("item");
		
		// Ensure the item is valid
		if (item.length) {
			
			// Get the widget
			var widget = $(item).data("widget");
			
			// Set the widget interval
			widget.interval = $(this).val();
			widget.changed = true;
			
			// Update the widget
			widget.update();
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.widget-settings-panel-widget-max-data-change: " + e);
		
	}
	
});

////////////////////////////////////////////////////////////////////////////////////////
//
// Main Settings Event Handlers
//
// /////////////////////////////////

// If the save button has been clicked
$("#saveDashboard").click(function() {
	
	try {
		
		// Save the layout of the content
		saveLayout();
		
	} catch (e) {
		
		console.log("bamviewer.saveDashboard-click: " + e);
		
	}
	
});

// If the reload button has been clicked
$("#reloadDashboard").click(function() {

	try {
		
		// Clear the layout
		clearDashboard();
		
		// Load the layout
		loadLayout();
		
	} catch (e) {
		
		console.log("bamviewer.reloadDashboard-click: " + e);
		
	}
	
});

// If the clear button has been clicked
$("#clearDashboard").click(function() {
	
	try {
		
		// Clear the layout
		clearDashboard();
		
	} catch (e) {
		
		console.log("bamviewer.clearDashboard: " + e);
		
	}
	
});

////////////////////////////////////////////////////////////////////////////////////////
//
// Drag Event Handler Variables
//
// /////////////////////////////////

var draggable = ".metric-widget";
var droppable = ".sortable-placeholder";

var draggedItem = null;
var lastDraggedItem = null;

var startDragX = 0;
var startDragY = 0;
var numOfGridX = 0;
var numOfGridY = 0;

////////////////////////////////////////////////////////////////////////////////////////
//
// Drag Event Handler Constants
//
// /////////////////////////////////

var DRAG_START = "dragstart";
var DRAG_END = "dragend";
var DRAG_ENTER = "dragenter";
var DRAG_OVER = "dragover";
var DRAG_EXITED = "dragexit";
var DROP = "drop";

////////////////////////////////////////////////////////////////////////////////////////
//
// Drag Event Handlers
//
// /////////////////////////////////

// When a drag begins
function DragStarted(event) {
	
	try {
		// Get the item that was dragged
		lastDraggedItem = $(this);
		
		// If it is a widget
		if ($(event.target).hasClass("metric-widget") || $(event.target).hasClass("widget-handle") || $(event.target).attr("id") == "metrics-menu-image") {
			
			// Get the dragged item
			draggedItem = $(this);
		    
			// If it is being dragged by the handle
			if ($(event.target).hasClass("widget-handle")) {
				
				// Hide the settings and resize buttons
				$(draggedItem).find(".widget-settings").addClass("hide");
				$(draggedItem).find(".widget-resize").addClass("hide");
				
			}
			
			// Iterate through the droppable areas
		    $(droppable).each(function(i) {
		    	
		    	// Show the grid to the user
		    	$(this).addClass("sortable-placeholder-show");
		    	$(draggedItem).css("position", "inherit");
		    	
		    });
		    
		    // If it was dragged from the metric menu, then hide the metric menu
		    if ($(event.target).attr("id") == "metrics-menu-image") {
		    	
		    	$("#side-menu-carousel").carousel("prev");
		    	inMetrics = false;
		    	
		    }
			
		// If it is the resize button of the widget
		} else if ($(event.target).hasClass("widget-resize")) {
			
			// Get the item
			lastDraggedItem = $(event.target);
			
			// Get the widget from the item
			var widget = $($(lastDraggedItem).parent()).data("widget");
			
			// X
			startDragX = $(this).offset().left;
			numOfGridX = widget.numOfGridX;
			
			// Y
			startDragY = $(this).offset().top;
			numOfGridY = widget.numOfGridY;
			
			// Iterate through the droppable area
			$(droppable).each(function() {

				// Show grid to the user
				if (!$(this).hasClass("filled")) {
	            	$(this).addClass("sortable-placeholder-show");
				}
	        	
	        });
			
		}
		
		// Finally
		event.stopImmediatePropagation();
		
	} catch (e) {
		
		console.log("bamviewer.DragStarted: " + e);
		
	}
	
}

// When a drag ends
function DragEnded(event) {
	
	try {
		// Set the last dragged item to null
		lastDraggedItem = null;
		
		// If the dragged item was a widget
		if ($(event.target).hasClass("metric-widget") || $(event.target).hasClass("widget-handle") || $(event.target).attr("id") == "metrics-menu-image") {
			
			// If it was dragged by the widget handle
			if ($(event.target).hasClass("widget-handle")) {
				
				// Show the settings and resize buttons
				$(draggedItem).find(".widget-settings").removeClass("hide");
				$(draggedItem).find(".widget-resize").removeClass("hide");
				
			}
			
			// Iterate through the droppable area
			$(droppable).each(function(i) {
	         	
				// Hide the grid from the user
	         	$(this).removeClass("sortable-placeholder-show");
	         	$(draggedItem).css("position", "absolute");
	         	
			});
			 
			// If the widget was dragged from the metric menu
			if ($(event.target).attr("id") == "metrics-menu-image") {
				
				// Register the widget
				registerWidget($(draggedItem).data("widget"));
				 
			}
			
			// Iterate through the widgets
			$(".bam-sortable li.metric-widget").each(function() {
				
				// Get the widget
				var w = $(this).data("widget");
				
				// If it has a chart
				if (w.ref) {
					
					// Set the render flag to true
					w.render = true;
					
				}
				
			});
			
		// Else if it was resized
		} else if ($(event.target).hasClass("widget-resize")) {
			
			// Iterate through the droppable area
			$(droppable).each(function(i) {
	            
				// Hide the grid from the user
	    		$(this).removeClass("sortable-placeholder-show");
	            
	        });

			// Get the widget
			var widget = $($(event.target).parent()).data("widget");
			
			// Update the widget
			widget.update();
			
		}
		
		// Finally
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.DragEnded: " + e);
		
	}
	
}

// When a draggable item enters an area
function DragEntered(event) {
	
	try {
		// Prevent the default functions
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.DragEntered: " + e);
		
	}
	
}

// When an item is dragged over a droppable area
function DragOver(event) {
	
	try {
		// If the item is a widget
		if ($(lastDraggedItem).hasClass("metric-widget") && $(lastDraggedItem).data("widget").hasStarted()) {
			
			// Set the droppable area to filled
			draggedItem.parent(droppable).removeClass('filled');
	        $(this).filter(':not(.filled)')
	        	   .append(draggedItem)
	        	   .addClass('filled');
					
	    // Else if it is a resize
		} else if ($(lastDraggedItem).hasClass("widget-resize")) {
			
			// Iterate through the droppable area
			$(droppable).each(function(i) {
	            
				// Show the user the grid
	    		if ($(this).hasClass("filled") && !$(this).hasClass("sortable-placeholder-show")) {
		            $(this).addClass("sortable-placeholder-show");
				}
	            
			});
			
			// Get the widget
			var widget = $($(lastDraggedItem).parent()).data("widget");
			
			// Get the offset from the start of the drag
			var offset = $(this).offset().left - startDragX;
			
			// Get the grid width
			var gridWidth = $("li.sortable-placeholder").outerWidth(true);
		 
			// Calculate the number of grids to add
			var numOfGridXAdd = Math.ceil(offset / gridWidth);
			
			// If the number of grids is more, then change the number of grids to the widget
			if (numOfGridXAdd > -numOfGridX + (minimumX - 1)) {
				widget.numOfGridX = numOfGridX + numOfGridXAdd;
			}
		 
			// Update the offset for the y axis
			offset = $(this).offset().top - startDragY;
			
			// Get the grid height
			var gridHeight = $("li.sortable-placeholder").outerHeight();
		 
			// Calculate the number of grids to add heightwise
			var numOfGridYAdd = Math.ceil(offset / gridHeight);
			
			// If it is more, then adjust the widget height
			if (numOfGridYAdd > -numOfGridY + (minimumY - 1)) {
				widget.numOfGridY = numOfGridY + numOfGridYAdd;
			}
		 
			// Set the effect to immediate size adjustment
			widget.immediate();
			
			// Set the changed flag
			widget.changed = true;
			
		}
		
		// Finally
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.DragOver: " + e);
		
	}
	
}

// When a dragged item exists a droppable area
function DragExited(event) {
	
	try {
		
		// Prevents the default method from executing
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.DragExited: " + e);
		
	}
	
}

// When a dragged item is dropped
function Drop(event) {
	
	try {
		
		// If the item was a widget
		if ($(lastDraggedItem).hasClass("metric-widget")) {
			
			// Add the widget to the area
			draggedItem.parent(droppable).removeClass('filled');
	        $(this).filter(':not(.filled)')
	        	   .append(draggedItem)
	        	   .addClass('filled');
	        
	        // Setup the widget items
	        if (!$($(draggedItem).find("img.widget-settings")).length) {
	       	 	
	        	setupWidgetItem(draggedItem);
	       	 	
	        }
			
		} else if ($(lastDraggedItem).hasClass("widget-resize")) {
			
		}
		
		// Finally
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.Drop: " + e);
		
	}
	
}

// A default method for a drag or drop method
function DragDropDefault(event) {
	
	try {
		
		// Prevents the methods from executing
		event.preventDefault();
		
	} catch (e) {
		
		console.log("bamviewer.DragDropDefault: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Mouse Event Handling Constants
//
// /////////////////////////////////

var MOUSE_ENTER = "mouseenter";
var MOUSE_LEAVE = "mouseleave";

////////////////////////////////////////////////////////////////////////////////////////
//
// Mouse Event Handlers
//
// /////////////////////////////////

// When the mouse enters an area
function MouseEntered(event) {
	
	try {
		
		// If the item is an widget
		if ($(this).hasClass("metric-widget")) {
			
			// Get the widget
			var widget = $(this).data("widget");
			
			// Ensure the widget has started and it is not showing the legend
			if (widget.started && !widget.showLegend) {
				
				// Show the legend
				widget.showLegend = true;
				widget.changed = true;
				
				// Update the widget
				widget.update();
				
			}
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.MouseEntered: " + e);
		
	}
	
}

// When the mouse leaves an area
function MouseLeave(event) {
	
	try {
		
		// If the item is a widget
		if ($(this).hasClass("metric-widget")) {
			
			// Get the widget
			var widget = $(this).data("widget");
			
			// Ensure the widget has started and the legend is showing
			if (widget.started && widget.showLegend) {
				
				// Hide the legend
				widget.showLegend = false;
				widget.changed = true;
				
				// Update the widget
				widget.update();
				
			}
			
		}
		
	} catch (e) {
		
		console.log("bamviewer.MouseLeave: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Widget Settings Panel Methods
//
// /////////////////////////////////

// Sets up the widget items on the widget
function setupWidgetItem(widget) {
	
	try {
		
		// Adds the move handle to the widget
		$(widget).prepend("<img class='widget-handle' src='/img/bam/move-icon.png'/>");
	 	
		// Adds the settings button to the widget
		 var settingsLabel = "settings-" + new Date().getTime();
		 $(widget).prepend("<img class='widget-settings " + settingsLabel + "' src='/img/bam/properties-icon.png'/>");
		 
		 // Adds the click function to the settings button
		 $($(widget).find("." + settingsLabel)).click(function() {
	 		
			 // Check if the panel is open
		 	if ($(".overlay").length)
		 		
		 		// Hide the panel
		 		closePanel(this);
		 	
		 	else
		 		
		 		// Show the panel
		 		showPanel(this);
	 		
		 });
		 	
		 // Add the resize button to the widget
		 var resizeLabel = "resize-" + new Date().getTime();
		 $(widget).prepend("<img class='widget-resize " + resizeLabel + "' src='/img/bam/resize-icon.png'/>");
		 	
		 // Make the resize button draggable
		 resizeDraggable("." + resizeLabel);
		
	} catch (e) {
		
		console.log("bamviewer.setupWidgetItem: " + e);
		
	}
	
}

// Postions the panel to the widget
function positionPanel(widget) {
	
	try {
		
		// Get the panel
		var panel = $(".widget-settings-panel");
		
		// Get the x and y of the widget
		var x = $(widget).offset().left + 20;
		var y = $(widget).offset().top - 5;
		
		// Reference the panel width
		var panelWidth = 0;
		
		// Iterate through the panel list
		$(".widget-settings-panel ul li").each(function() {
			
			// Get the max width
			if ($(this).outerWidth() > panelWidth)
				panelWidth = $(this).outerWidth();
			
		});
		
		// Add 30 to the width
		panelWidth += 30;
		
		// Get the x coord to the right of the widget
		var panelRight = x + panelWidth;
		
		// Get the width of the window
		var windowWidth = $(window).outerWidth() - 25;
		
		// If the panel right is greater than the window
		if (panelRight > windowWidth) {
			
			// Change the panel to rtl
			$(panel).find("ul").css("direction", "rtl");
			
			// Adjust the x coord
			var widgetWidth = $($($(widget).parent()).data("widget").element).outerWidth();
			x = x - widgetWidth - panelWidth;
			
		} else {
			
			// Else, make the direction ltr
			$(panel).find("ul").css("direction", "ltr");
			
		}
		
		// Set the panel coords
		$(panel).css("left", x);
		$(panel).css("top", y);
		
		// Return the panel
		return panel;
		
	} catch (e) {
		
		console.log("bamviewer.positionPanel: " + e);
		
	}
	return null;
	
}

// Closes the panel
function closePanel(widget) {
	
	try {
		
		// Makes the overlay layer invisible
		$(".overlay").animate({
			
			opacity: 0
			
		}, settingsAnimationDuration, function() {
						
			// Removes the overlay layer
			$(".overlay").remove();
			
			// Set the widget to its normal z index
			if ($(widget).hasClass("metric-widget"))
				$(widget).css("z-index", 1);
			else
				$($(widget).parent()).css("z-index", 1);
			
		});
	
		// Animate the panel disappearing
		$(".widget-settings-panel").animate({
		
			opacity: 0
		
		}, settingsAnimationDuration, function() {
		
			//Hide the panel
			$(".widget-settings-panel").addClass("hide");
		
			// Position the panel to the widget
			var panel = positionPanel(widget);
 		
			// Remove the references on the panel
			$(panel).data("settings", null);
			$(panel).data("item", null);
			$(panel).data("parent", null);
		
			// Hide the widget panel items
			$(".widget-settings-panel ul li.graph-type").removeClass("show");
			$(".graph-type-list").addClass("hide");
	
			$(".graph-type-list li").removeClass("current");
		
			$(".series-checkboxes").removeClass("show");
		
		});
		
	} catch (e) {
		
		console.log("bamviewer.closePanel: " + e);
		
	}
	
}

// Shows the panel for the widget
function showPanel(widget) {
	
	try {
		
		// Set the widget to be above other elements
		$($(widget).parent()).css("z-index", 10);
		
		// Add the overlay layer to the main content
		$("#main_content").append("<div class='overlay'></div>");
		
		// Animate the opacity of the overlay
		$(".overlay").animate({
			
			opacity: 0.5
			
		}, settingsAnimationDuration, function() {
			
			// Add a click function to the overlay
			$(".overlay").click(function() {
				
				// Get the item containing the widget
				var item = $(".widget-settings-panel").data("item");
				
				// If the item is valid
				if (item.length) {
					
					// Close the panel
					closePanel(item);
					
				}
				
			});
			
		});
		
		// Hide the panel
		$(".widget-settings-panel").css("opacity", "0");
		$(".widget-settings-panel").removeClass("hide");

		// Position the panel
		var panel = positionPanel(widget);
		
		// Set the data for the panel
		$(panel).data("settings", widget);
		$(panel).data("item", $($(widget).parent()));
		$(panel).data("parent", $($($(widget).parent()).parent()));
		
		// Get the widget from the item
		var w = $($(widget).parent()).data("widget");
		
		// Create the checkboxes
		var checks = $(".series-checkboxes");
		$(checks).empty();
		
		// Iterate through the datasets
		for (var i = 0; i < w.datasets.length; i++) {
			
			// Get the title of the dataset
			var key = w.datasets[i].title;
			
			// Add the checkbox to the list
			$(checks).append("<input type='checkbox' name='" + key +
					"' " + (w.datasets[i].enabled ? "checked='checked'" : "") + " id='id" + key + "'></input>" +
					"<label for='id" + key + "' style='display:inline'>&nbsp;"
					+ key + "</label></br>");
			
		}
		
		// If the checkbox is clicked
		$(checks).find("input").click(function() {
			
			// Go through each check box
			$(checks).find("input").each(function () {
				
				// Get the name
				var key = $(this).attr("name");
				
				// Iterate through the checkboxes
				for (var i = 0; i < w.datasets.length; i++) {
					
					// Compare the names
					if (w.datasets[i].title == key) {
						
						// Enable the widgets dataset accordingly
						w.datasets[i].enabled = $(this).prop("checked");
						
					}
					
				}
				
			});
			
			// Update the widget
			w.changed = true;
			w.update();
			
		});
		
		// Check if the widget has time
		if (w.type.hasTime) {

			// Show the max data selection
			$(".widget-selector").val(w.interval);
			$(".widget-max-data").removeClass("hide");
			
		} else {
			
			// Else, hide the max data
			$(".widget-max-data").addClass("hide");
		}
		
		// Animate the panel to show
		$(".widget-settings-panel").animate({
			
			opacity: 1
			
		}, settingsAnimationDuration, function() {
			
		});
		
	} catch (e) {
		
		console.log("bamviewer.showPanel: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Layout Methods
//
// /////////////////////////////////

// Converts the layout to json
function LayoutToJson() {
	
	try {
		
		// Create the json string
		var json = {};
		
		// Get the theme
		json["theme"] = currentThemeIndex;
		
		// Iterate through each widget
		$(".bam-sortable li.sortable-placeholder").each(function(i) {
			
			// Check if a widget is in the grid
			if ($(this).hasClass("filled")) {
				
				// Get the widget json format
				json[i] = $($(this).find(".metric-widget")).data("widget").toJson();
				
			}
			
		});
		
		// Return a stringified json object
		return JSON.stringify(json);
		
	} catch (e) {
		
		console.log("bamviewer.LayoutToJson: " + e);
		
	}
	return "";
	
}

// Converts json to a layout with widgets
function JsonToLayout(savedJson) {
	
	try {
		
		// Parses the string into json object
		var json = JSON.parse(savedJson);
		
		// Set the current json
		currentJson = savedJson;
		
		// Get the json theme
		if (json["theme"]) {
			
			currentThemeIndex = json["theme"];
			
		}
		
		// Iterate through the grid
		$(".bam-sortable li.sortable-placeholder").each(function(i) {
			
			// Check if the grid contains the widget
			if (json[i]) {
				
				// Get the element
				var element = json[i];
				
				// Creates the widget list from the element
				var li = createWidgetListItem(element["uid"], element["name"]);
				
				// Add a filled class to the grid element
		        $(this).filter(':not(.filled)')
		        	   .append(li)
		        	   .addClass('filled');
		        
		        // Create the widget with the options
		        var widget = new BamWidget($(this).find("li.metric-widget"), element["uid"], element["name"], $(".bam-sortable li"), {
					
		        	numOfGridX: element["numOfGridX"],
					numOfGridY: element["numOfGridY"],
					type: element["type"],
					interval: element["interval"],
		        	
					update: function() {
						
						try {
							
							BamToFlot(this);
							
						} catch (e) {
							
							console.log("bamviewer.widget-update: " + e);
							
						}
						
					}
					
				});
		        
		        // Change the type to the one stored
		        widget.changeType(element["type"].index);
		        
		        // Get the dataset
		        var dataset = element["dataset"];
		        var datasets = [];
		        
		        // Iterate through the dataset
		        for (var i = 0; i < dataset.length; i++) {
		        	
		        	// Create the dataset
		        	datasets[i] = new Dataset(dataset[i].title);
		        	
		        	// Set whether it is enabled or not
		        	datasets[i].enabled = dataset[i].enabled;
		        	
		        }
		        
		        // Set the dataset
		        widget.datasets = datasets;
		        
		        // Set the widget to the element
		        $($(this).find("li.metric-widget")).data("widget", widget);

		        // Update the draggables and droppables
		        updateSortables();
		        
		        // Register the widget
		        registerWidget($(this).find("li.metric-widget").data("widget"));
		        
		        // Setup the widget items
		        setupWidgetItem($(this).find("li.metric-widget"));
		        
		        // Resize the widget
				widget.immediate();
		        
			}
			
		});
		
	} catch (e) {
		
		console.log("bamviewer.JsonToLayout: " + e);
		
	}
	
}

// Loads the layout from the backend
function loadLayout() {
	
	try {
		
		// Calls the loadlayout method
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
		    data: { act: "loadlayout" },
			dataType : "json",
		    success: function(result) {
		    	
		        try {
		        	
		        	// Convert the json to the layout
		        	JsonToLayout(result.layout);
		        	
		        } catch (e) {
		        	
		        	console.log("bamviewer.loadLayout: " + e);
		        	
		        }
		    }
		});
		
	} catch (e) {
		
		console.log("bamviewer.loadLayout: " + e);
		
	}
	
}

// Saves the layout to the backend
function saveLayout() {
	
	try {
		
		// Gets the layout
		var layout = LayoutToJson();
		
		// Call the savelayout method
		$.ajax({
			type : "POST",
			url : "/bamview",
			async : true,
		    data: { act: "savelayout", layout: layout },
			dataType : "json",
		    success: function(result) {
		    }
		});
		
	} catch (e) {
		
		console.log("bamviewer.saveLayout: " + e);
		
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Helper Methods
//
// /////////////////////////////////

// Waits for a specified amount of seconds
var waitForFinalEvent = (function () {
	  var timers = {};
	  return function (callback, ms, uniqueId) {
	    if (!uniqueId) {
	      uniqueId = "Don't call this twice without a uniqueId";
	    }
	    if (timers[uniqueId]) {
	      clearTimeout (timers[uniqueId]);
	    }
	    timers[uniqueId] = setTimeout(callback, ms);
	  };
})();

// Creates a widget
function createWidgetListItem(uid, name) {
	
	return "<li uid='" + uid + "' identifier='" + name + "' class='metric-widget'>"
	+ "<div class='widget-title'>"
	+ name 
	+ "</div>"
	+ "<div class='widget-display'>"
	+ "<img id='metrics-menu-image' src='/img/bam/widget-icon.png'/>"
	+ "</div>"
	+ "</li>";
	
}

// Clears the dashboard
function clearDashboard() {
	
	try {
		
		// Iterate through the content
		$(".bam-sortable li").each(function() {
			
			// If it has a widget clear it
			if ($(this).hasClass("filled")) {
				
				// Get the item
				var item = $(this).find(".metric-widget");
				
				// Get the widget
				var widget = $(item).data("widget");
				
				// Destroy the widget
				widget.destroy();
				
				// Remove the item
				$(item).remove();
				$(this).removeClass("filled");
				
			}
			
		});
		
	} catch (e) {
		
		console.log("bamviewer.clearDashboard: " + e);
		
	}
	
}

// Makes the background dark
function makeDarkTheme() {
	
	try {
		
		// Make the background black
		$("#main_content").css("background", "black");
		
		// Changes the writing to white
		$(".user_heading").css("color", "white");
		$(".toggler").css("background", "white");
		
		$(".widget-title").css("opacity", "0.3");
		
	} catch (e) {
		
		console.log("bamviewer.makeDarkTheme: " + e);
		
	}
	
}

// Makes the background light
function makeLightTheme() {
	
	try {
		
		// Makes the background white
		$("#main_content").css("background", "white");

		// Changes the writing to black
		$(".user_heading").css("color", "black");
		$(".toggler").css("background", null);
		
		$(".widget-title").css("opacity", null);
		
	} catch (e) {
		
		console.log("bamviewer.makeLightTheme: " + e);
		
	}
	
}