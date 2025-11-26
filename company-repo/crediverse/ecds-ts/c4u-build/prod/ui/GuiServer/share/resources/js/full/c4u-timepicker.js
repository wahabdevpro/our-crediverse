/*
 * C4U Timepicker

<!--  Time picker Control -->

<style>
	.bottom-div {
	    z-index:999;
	    position:absolute;
	    background:#eee;
	    padding:10px;
	    border:1px solid #ccc;
	}
	.my-timepicker-popover {
	    background-clip: padding-box;
	    background-color: #FFFFFF;
	    border: 1px solid rgba(0, 0, 0, 0.15);
	    border-radius: 4px;
	    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.176);
	    float: left;
	    left: 20;
	    margin-top: 1px;
	    min-width: 100px;
	    position: absolute;
	    top: 100%;
	    z-index: 1000;
	    display: none;
	}
	
	.timeparam {
		width:140px;
	}
	
	.time-number-btn {
		cursor: pointer;
	}
	.time-number-btn:hover {
		background-color: #CCCCCC;
	}
	
	.time-separator {
		font-weight: bolder;
		font-size: larger;
		color: #333333;
		padding-top: 12px !important;
	}
	
</style>
<div class='row'>
	<div class='col-md-6'>
		<label for='testtime' class='control-label'>Some Time Thing</label>
		<div class='input-group' id='timePicker'>
			<input type='text' class='form-control' id='timeControl' name='timeControl'  placeholder='00:00' />
			<span class='input-group-addon glyphicon glyphicon-time time-number-btn'></span>
		</div>
		
		<div id='timepopup' class='my-timepicker-popover'>
			<table class='table' style='margin-bottom:0px !important;'>
				<tbody>
					<tr>
						<td class='timeparam hour'>
							<div class='input-group'>
								<input class='form-control bfh-number' data-min='0' data-max='23' data-zeros='true' data-wrap='true' type='text' />
								<span class='input-group-addon time-number-btn inc'>
									<span class='glyphicon glyphicon-chevron-up'></span>
								</span>
								<span class='input-group-addon time-number-btn dec'>
									<span class='glyphicon glyphicon-chevron-down'></span>
								</span>
							</div>
						</td>
						<td class='time-separator'>:</td>
						<td class='timeparam minute'>
							<div class='input-group'>
								<input class='form-control bfh-number' data-min='0' data-max='59' data-zeros='true' data-wrap='true' type='text' />
								<span class='input-group-addon time-number-btn inc'>
									<span class='glyphicon glyphicon-chevron-up'></span>
								</span>
								<span class='input-group-addon time-number-btn dec'>
									<span class='glyphicon glyphicon-chevron-down'></span>
								</span>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<span id='timeControl_error' class='error_message hide'></span>
	</div>
</div>

<!-- End Time Picker Control -->

 */

!function($) {
	
	'use strict';
	
	var C4UTimepicker = function(element, options, e) {

        if (e) {
            e.stopPropagation();
            e.preventDefault();
        }
        
        this.$element = $(element);
        this.init();
	};
	
	C4UTimepicker.prototype = {
		init: function() {
            var that = this, id = this.$element.attr('id');
            $("#" + id).html(this.createBaseControlHtml());
            var html = this.createPopup();
		},
		
		createBaseControlHtml: function() {
			var html = [];
			html[html.length] = "<input type='text' class='form-control' id='timeControl' name='timeControl'  placeholder='00:00' />";
			html[html.length] = "<span class='input-group-addon glyphicon glyphicon-time time-number-btn'></span>";
			
			return html.join("");
		},
		
		createPopup: function() {
			var html = [];
			html[html.length] = "<div id='timepopup' class='my-timepicker-popover'>";
			html[html.length] = "	<table class='table' style='margin-bottom:0px !important;'>";
			html[html.length] = "		<tbody>";
			html[html.length] = "			<tr>";
			html[html.length] = this.createTimeParam("hour", 23);
			html[html.length] = "<td class='time-separator'>:</td>";
			html[html.length] = this.createTimeParam("minute", 59);
			html[html.length] = "			</tr>";
			html[html.length] = "		</tbody>";
			html[html.length] = "	</table>";
			html[html.length] = "</div>";
			
			html[html.length] = "";
			return html.join("");
		},
		
		createTimeParam: function(parm, max) {
			var html = [];
			html[html.length] = "<td class='timeparam ";
			html[html.length] = parm;
			html[html.length] = "'>";
			html[html.length] = "	<div class='input-group'>";
			html[html.length] = "		<input class='form-control' data-min='0' data-max='";
			html[html.length] = max;
			html[html.length] = "' data-zeros='true' data-wrap='true' type='text' />";
			html[html.length] = this.createControlButton(true);
			html[html.length] = this.createControlButton(false);
			html[html.length] = "</div>";
			html[html.length] = "</td";
			return html.join("");
		},
		
		createControlButton: function(isUp) {
			var html = [];
			html[html.length] = "<span class='input-group-addon time-number-btn ";
			html[html.length] = (isUp)? "inc" : "dec";
			html[html.length] = "'><span class='glyphicon glyphicon-chevron-";
			html[html.length] = (isUp)? "up" : "down";
			html[html.length] = "'></span>";
			html[html.length] = "</span>";
			
			return html.join("");
		}
	};
	
	$.fn.c4utimepicker = function(option, event) {
		//get the args of the outer function..
		var args = arguments;
		var value;
		var chain = this.each(function() {
			var $this = $(this),
				data = $this.data('c4utimepicker'),
				options = typeof option == 'object' && option;
			
				if (!data) {
	                $this.data('c4utimepicker', (data = new C4UTimepicker(this, options, event)));
	            }
			
			var elementId = $(this).attr("id");
		});
	};
	
}(window.jQuery);