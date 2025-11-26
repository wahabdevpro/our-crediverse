
sendAjaxReceiveJson = function(pageurl, dataurl, callback)
{
	var status = $.ajax({
		type: "POST", 
		url: pageurl, 
		data: dataurl,
		async: true,
		dataType: "json",
		cache: false
	}).done(function(data) {
		try {
			if (data.status=='expired') {
				window.location.replace('/');
			} else {
				callback(data);	
			}
		} catch(err)
		{
			alert(err);
		}
	});
};
	
  // Override show method
  var _oldShow = $.fn.show;
  $.fn.show = function(speed, oldCallback) {
    return $(this).each(function() {
      var obj = $(this),
          newCallback = function() {
            if ($.isFunction(oldCallback)) {
              oldCallback.apply(obj);
            }
            obj.trigger('afterShow');
          };

      // you can trigger a before show if you want
      obj.trigger('beforeShow');

      // now use the old function to show the element passing the new callback
      _oldShow.apply(obj, [speed, newCallback]);
    });
  }

  //Need an endsWith?
  if (typeof String.prototype.endsWith !== 'function') {
	  String.prototype.endsWith = function(suffix) {
		  return this.indexOf(suffix, this.length - suffix.length) !== -1;
	  };
  }