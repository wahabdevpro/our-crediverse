define(["models/UserModel", "marionette"], function(UserModel, Marionette) {
	
	describe("User Model Unit Tests", function() {
		
//		var extractErrorFields = function(validationError) {
//			var errs = [];
//			for(var i in validationError) {
//				errs[i] = validationError[i].field;
//			}
//			return errs;
//		};
//		
//		// Tests goal: Ensure User Model is validated
//		it("Should validate User Model required fields", function () {
//			var data = {};
//			var model = new UserModel(data);
//
//			expect(model.isValid()).toBe(false);
//			expect(model.validationError).not.toBeNull();
//			
//			var valErrors = model.validationError;
//			expect(valErrors.length).toBe(7);
//			
//			var errFields = extractErrorFields(valErrors); 
//			var shouldHaveFields = [ "firstName", "surname", "initials", "mobileNumber", "accountNumber", "domainAccountName", "department" ];
//			
//			expect(_.difference(errFields, shouldHaveFields).length).toBe(0);
//		});
//		
//		// Tests goal: Ensure validated fields are removed from error list
//		it("Should validate User Model does provide error on field with no error", function () {
//			var data = {
//				"firstName": "John" 	
//			};
//			
//			var model = new UserModel(data);
//			expect(model.isValid()).toBe(false);
//			var valErrors = model.validationError;
//			expect(valErrors.length).toBe(6);
//			
//			var errFields = extractErrorFields(valErrors);
//			expect(errFields).not.toContain("firstName");
//		});
		
	});
	
});