// Base Require for this Unit Test
require.config({
  baseUrl: '/src/main/resources/static/js/app',
  
  paths: {
	"jquery": "../lib/jquery-1.11.3",
	"rsa": "../lib/rsa",
	"jasmine-jquery" : "../lib/test/jasmine-jquery",
	"mock-ajax" : "../lib/test/mock-ajax"		
  },
  
  urlArgs: 'bust=' + (new Date()).getTime()
});


define(function(require) {

	require('jquery');
	
	describe("Login Test suite", function() {
	    it("Simple Test", function () {
	    	var RESULT_MUST_EQUAL = 2;
	    	var RESULT = 1 + 1;
	    	console.info("Simple Test");
	    	expect(RESULT).toBe(RESULT_MUST_EQUAL, "Calculation should equal " + RESULT_MUST_EQUAL);
	    });
	})
});