define(['jquery', 'rsa', 'loginControl', 'mock-ajax', 'jasmine-jquery'], function($, app) {

	describe("Login Test suite", function() {
		var TEST_ELEMENT_ID = "testcontent";
		var JQ_ELEMENT_REF = "#" + TEST_ELEMENT_ID;


		// Test Function
		function createBaseCidControl() {
	        var cid = 1;
	    	var control = $(JQ_ELEMENT_REF).loginController({"cid": cid});
	    	expect(jasmine.Ajax.requests.mostRecent().url).toBe('/auth?cid=1');
	    	return control;
		}

		/**
		 * Create HTML Element to attach to
		 * Create AJAX mechanism
		 */
	    beforeEach(function() {
			// Create containing element
			var container = document.createElement("div");
			container.setAttribute("id", TEST_ELEMENT_ID);
			document.body.appendChild(container);

			// Create Mock Ajax mechanism
			jasmine.clock().install();
			jasmine.Ajax.install();
	    })

	    /**
	     * Clean up after each test
	     */
    	afterEach(function() {
    		document.body.removeChild( document.getElementById(TEST_ELEMENT_ID));
    		jasmine.clock().uninstall();
    		jasmine.Ajax.uninstall();
    	});

	    /**
	     * This test must always pass ... else there is a problem with jasmine configuration
	     */
	    it("Simple Test", function () {
	    	var RESULT_MUST_EQUAL = 2;
	    	var RESULT = 1 + 1;
	    	console.info("Simple Test");
	    	expect(RESULT).toBe(RESULT_MUST_EQUAL, "Calculation should equal " + RESULT_MUST_EQUAL);
	    });

	    /**
	     * Example Ajax mock send ... receive
	     * This is test code dependant ... should always work
	     */
	    it("Mock Ajax Test", function () {

	    	var doneFn = jasmine.createSpy("success");

	    	// Create Ajax Call
	        var xhr = new XMLHttpRequest();
	        xhr.onreadystatechange = function(args) {
	          if (this.readyState == this.DONE) {
	            doneFn(this.responseText);
	          }
	        };
	        xhr.open("GET", "/some/cool/url");
	        xhr.send();

	        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/some/cool/url');
	        expect(doneFn).not.toHaveBeenCalled();

	        // Create Ajax Response
	        var request = jasmine.Ajax.requests.mostRecent();
	        request.respondWith({
	            "status": 200,
	            "contentType": 'text/plain',
	            "responseText": 'awesome response'
	        });

	        expect(doneFn).toHaveBeenCalledWith('awesome response');
	    });

	    /**
	     * Check that Login Component attaches to page DOM
	     */
	    it("Attach login control", function() {
	    	var cid = 1;
	    	var control = $(JQ_ELEMENT_REF).loginController({"cid": cid});
	    	expect($(JQ_ELEMENT_REF).length).toEqual(1, "Attach Login Control to page element");
	    	expect(control).not.toBeNull();
	    	expect(control.cid).toBe(1);
	    });

	    it("Validate GET call", function() {

	    	// Creating component with paramter calls auth REST GET immediately
	    	var control = createBaseCidControl();

	        // Create Ajax Response
	        var request = jasmine.Ajax.requests.mostRecent();
	        request.respondWith({
	            "status": 200,
	            "responseText": '{"auth":{"algorithm": "RSA","encodedKey": "-----BEGIN Public key-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2Yhra+IdlTJmRhB10OxBzeIlE673qExpZiBWU2UrpoZa8324pmwYSgihkI8DqJ6CqfXMxoYeL58m5n5R1kGppRZyid5xEv61VYDZPQJa4eW/KFwZQ2Ee49Ukr8sHIOgxbry42cWW5jJp5TdU+RHODn81cOVQFXnGNGB77YjIc6QIDAQAB-----END Public key-----","encodingFormat": "X.509","modulus":"b6621adaf887654c9991841d743b1073788944ebbdea131a59881594d94ae9a196bcdf6e299b061282286423c0ea27a0aa7d7331a1878be7c9b99f9475906a69459ca2779c44bfad5560364f4096b8796fca170650d847b8f5492bf2c1c83a0c5baf2e367165b98c9a794dd53e4473839fcd5c3954055e718d181efb62321ce9","exponent": "10001"},"data": "{\\"cid\\":\\"1\\",\\"state\\":\\"REQUIRE_UTF8_USERNAME\\"}"}'
	        });

	        expect(control.isError).toEqual(false, "No Error should have been created for good AJAX call");
	        expect(control.completed).toEqual(false, "Should not be Authorized");
	        expect(control.request).toEqual("USERNAME", "New Request should be for User Name");
	        expect(control.rsa.e).toEqual(65537, "RSA Exponent reflected from Ajax incorrectly");
	        expect(control.rsa.n).not.toBeNull();	// Cannot add message to NULL
	        expect(control.completedComponents[0].label).toEqual("Company ID", "Company ID Should be Completed");
	        expect(control.requiredComponents[0].label).toEqual("Username", "New component created for UserName");

	    });

        it('Validate GET call error', function () {
        	var control = createBaseCidControl();

	    	var request = jasmine.Ajax.requests.mostRecent();
	    	request.respondWith({
	            "status": 400,
	            "contentType": "text/plain"
	        });
	    	console.log(request.responseHeaders);
	        expect(control.isError).toEqual(true, "Error should be noted on for bad AJAX call");
	        expect(control.errMsg).not.toBeNull();
        });


        it('Should pass "timeout" statusText correctly', function () {

	        var cid = 1;
	    	var control = $(JQ_ELEMENT_REF).loginController({"cid": cid, "TIMEOUT": 50});
	    	expect(jasmine.Ajax.requests.mostRecent().url).toBe('/auth?cid=1');

        	jasmine.clock().tick(1000);
        	expect(control.errMsg).not.toBeNull();
        });
	});	// End: Login Test suite

});