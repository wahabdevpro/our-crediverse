//----------- Ussd Process Menu ---------------------------------
var processModel = null;

var lastNode = {
	id : '',
	update : false,
	type : ''
};

updatePropertyCallback = function() {
};

ussdMenuInitialize = function() {	
	try {
		processModel = $("#processOptions").c4uprocessgrid({
			retrieveMethod: retrievePropertyData,
			serverurl: pageurl,
			updatePropertyCallback: updatePropertyCallback
		});
	} catch(err) {
		alert("ussdMenuInitialize: " + err);
	}
};

checkAndSaveMessageData = function(callback)
{
	try {
		if (lastNode.update && (lastNode.id != '')) {
			saveLastNodeUpdates(callback);
			var oldText = getNodeText(lastNode.id);
			var updatedText = oldText.substring(0,oldText.indexOf('[')+1) + $("#menu_I").text() + "]";
			updateNodeText(lastNode.id, updatedText);
		} else if (callback != null) {
			callback();
		}
	} catch(err) {
		console.error("ussdMenu:checkAndSaveMessageData " + err);
	}

};

loadProcessModelTree = function(json) {
	try {
		if (json != null) {
			var job = JSON && JSON.parse(json) || $.parseJSON(json);
			// $('#processModel').jstree("destroy");
			$('#processModel').jstree({
				'core' : {
					'data' : job.data
				}
			});
		}
	} catch (err) {
		try {
			console.error("loading error :> " + err);
		} catch (ex) {
		}
	}

	$("#processModel").bind("select_node.jstree", function(e, data) {
		var ref = $('#processModel').jstree(true);
		var sel = ref.get_selected();
		loadContent(sel);
	});
};

updateProcessModel = function(jsonData) {
	$('#processModel').jstree("destroy");
	$('#processModel').jstree({
		'core' : {
			'data' : jsonData.data
		}
	});
};

updateMenuText = function(title, textArray) {
	$("#menu_config_title").html(title);
	for ( var i = 0; i < textArray.length; i++) {
		if (i == 0)
			$("#menu_I").html(textArray[i]);
		else {
			$("#menu_" + (i - 1)).html(textArray[i]);
		}
	}
	$("#menu_config").removeClass("hide");
};

sendNodeRequest = function(data2Send, callback) {
	var status = $.ajax({
		type : "POST",
		url : pageurl,
		async : true,
		dataType : "json",
		data : data2Send
	}).done(function(data) {
		if (callback!=null) {
			callback(data);
		}

	});
};

retrievePropertyData = function(nid, aid) {
	var data2Send = "aid=" + aid + "&act=propdata&nid=" + nid;	
	if (processModel != null) {
		sendNodeRequest(data2Send, processModel.updateDialog);
	}

};

/** Load language data **/
storeLanguageDetails = function(data) {
	if (processModel != null) {
		processModel.languageData = data;		
	}
};

loadLanguages = function() {
	var data2Send = "act=lang";
	sendNodeRequest(data2Send, storeLanguageDetails);
};

loadNodeIdContent = function(nodeId) {
	try {
		if (nodeId.indexOf("act") >= 0) {
			$("#processOptions").html(createLoadingIconHtml());
			var data2Send = "node=" + nodeId + "&act=data";
			sendNodeRequest(data2Send, updateRightContent);
		} else {
			updateRightContent(null);
		}
	} catch (err) {
		alert(err);
	}
};

loadContent = function(node) {
	try {
		var nodeId = node[0];
		loadNodeIdContent(nodeId);
	} catch (err) {
		alert(err);
	}
};

updateRightContent = function(data) {
	
	try {
		if (typeof processModel !== 'undefined' && processModel != null) {
			if (data != null) {
				processModel.update(data);
			}
			else
				processModel.update(data, true);
		} else {
			console.error("updateRightContent: processModel NULL");
		}
	} catch(err) {
		console.error("updateRightContent: " + err);
	}
};