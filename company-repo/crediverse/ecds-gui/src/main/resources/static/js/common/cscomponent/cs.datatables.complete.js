define(["jquery", "underscore", "datatables", "App", 'utils/CommonUtils'],
		function ($, _, DataTables, App, CommonUtils) {
			"use strict";
			$.fn.dataTable.ext.errMode = 'none';
			
			function dataTableStateSave(settings, data) {
				if ( !this.is(':visible') ) return;
				var api = this.DataTable();
				var defHidden = this.attr('data-sshash-hidden');
				var defOrder = this.attr('data-sshash-order');
				var defPageLen = 10;
				var baseUrl = '#'+this.attr('data-sshash-base');
				defHidden = defHidden.split(',');
				defOrder = defOrder.split(',');
				var v = '';
				if (defPageLen != data.length)
					v += '/count~'+data.length;
				if (data.start) { v += '/start~'+data.start };
				if (data.order.length){
					var differs = false;
					var ov = '';
					for( var i = 0; i < data.order.length; ++i ) {
						var key = data.order[i][0] + (data.order[i][1]=='asc'?'':'d');
						if (defOrder.indexOf(key) != i) differs = true;
						if (ov!='') ov += ',';
						ov += key;
					}
					if (ov!='' && differs) v += '/order~'+ov;
				}
				if (data.search && (data.search.search!='')) {
					var sv = '';
					if (data.search.smart) sv += 's';
					if (data.search.regex) sv += 'r';
					if (data.search.caseInsensitive) sv += 'i';
					sv += '~' + encodeURIComponent(data.search.search);
					v += '/search~'+sv;
				}
				/*
				if (data.columns.length) {
					var cv = '';
					for( var i = 0; i < data.columns.length; ++i ) {
						if (data.columns[i].visible == false && defHidden.indexOf(""+i) == -1 ) {
							if (cv!='') cv += ',';
							cv += i+'h';
						}
					}
					if (cv!='') v += '/columns~'+cv;
				}
				*/
				//dhtmlHistory.add( baseUrl + v );
				App.appRouter.navigate(baseUrl + v + CommonUtils.urlEncodeForm(), {trigger: false, replace: true});
			};

			function dataTableStateLoad(settings) {
				var defHidden = this.attr('data-sshash-hidden');
				var defOrder = this.attr('data-sshash-order');
				var defPageLen = 10;
				var columnsCount = this.attr('data-sshash-columns');
				var baseUrl = '#'+this.attr('data-sshash-base');
				defHidden = defHidden.split(',');
				defOrder = defOrder.split(',');
				var data = {
					'time': Date.now(),
					'length': parseInt(defPageLen),
					'start': 0,
					'order': [[ 0, 'asc' ]],
					'search': {
						'search': '',
						'smart': true,
						'regex': false,
						'caseInsensitive': true,
					},
					'columns': [],
				};
				{
					var order = [];
					for( var i = 0; i < defOrder.length; ++i ) {
						var asc = true;
						if (defOrder[i].indexOf('d') >= 0) asc = false;
						var col = asc ? parseInt(defOrder[i]) : parseInt(defOrder[i].substr(0, defOrder[i].indexOf('d')));
						order.push([col,asc?'asc':'desc']);	
					}
					data.order = order;
				}
				{
					var cols = [];
					for( var i = 0; i < columnsCount; ++i ) {
						var visible = defHidden.indexOf(""+i) == -1;
						cols.push( { /*'visible': visible,*/ 'search': { 'search': '', 'smart': true, 'regex': false, 'caseInsensitive': true } } );
					}
					data.columns = cols;
				}

				var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
				for (var i = 0; i < pcs.length; ++i ) {
					var split = pcs[i].split('~');
					if (split.length >= 2) {
						if (split[0] == 'count') {
							data.length = parseInt(split[1]);
						} else if (split[0] == 'start') {
							data.start = parseInt(split[1]);
						} else if (split[0] == 'search') {
							data.search.search = decodeURIComponent(split[2]);
							if(split[1].indexOf('s')>=0) data.search.smart=true;
							if(split[1].indexOf('r')>=0) data.search.regex=true;
							if(split[1].indexOf('i')>=0) data.search.caseInsensitive=true;
						} else if (split[0] == 'order') {
							var order = [];
							var cols = split[1].split(',');
							for (var j = 0; j < cols.length; ++j) {
								var asc = true;
								if (cols[j].indexOf('d') >= 0) asc = false;
								var col = asc ? parseInt(cols[j]) : parseInt(cols[j].substr(0, cols[j].indexOf('d')));
								order.push([col,asc?'asc':'desc']);	
							}
							data.order = order;
						} else if (split[0] == 'columns') {
						}
					}
				}
				CommonUtils.urlDecodeForm(pcs);
				return data;
			};
			
			var _super = $.fn.DataTable;
			
			var _defaults = {
					
			};
			
			$.fn.DataTable = function(options) {
				
				if (_.isObject(options) && arguments.length === 1) {
					// Now lets merge things
					var _extendedDefaults = $.extend(true, {}, _defaults, options);
					
					
					options.language.url = "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json";
					
					if (options.stateSave == 'hash') {
						options.stateSave = true;
						options.stateSaveCallback = $.proxy( dataTableStateSave, this );
						options.stateLoadCallback = $.proxy( dataTableStateLoad, this );
						var hidden = '';
						for( var i = 0; i < options.columns.length; ++i ) {
							if (!options.columns[i].visible) {
								if (hidden!='') hidden += ',';
								hidden += ''  +i;
							}
						}
						var order = '';
						if (!_.isUndefined(options.order) && _.isArray(options.order)) {
							for( var i = 0; i < options.order.length; ++i ) {
								var ov = '' + options.order[i][0];
								if( options.order[i][1] == 'desc' )
									ov += 'd';
								if (order!='') order += ',';
								order += ov;
							}
						}
						this.attr( 'data-sshash-columns', options.columns.length );
						this.attr( 'data-sshash-hidden', hidden );
						this.attr( 'data-sshash-order', order );
						this.attr( 'data-sshash-pagelen', options.pageLength );

					}

					var args = [];
					args.push(_extendedDefaults);
					//$.proxy(_super.apply( this, args ), this);
					return _super.apply( this, arguments );
		        }

			     // call the original constructor
				//$.proxy(_super.apply( this, arguments ), this);
			    return _super.apply( this, arguments );
			};
			
			
			
			
			return $;
		}
	);