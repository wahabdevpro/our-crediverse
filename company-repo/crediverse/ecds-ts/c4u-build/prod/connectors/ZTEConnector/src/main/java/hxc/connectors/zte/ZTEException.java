package hxc.connectors.zte;

import hxc.servicebus.ReturnCodes;

public class ZTEException extends Exception
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Response Codes
	//
	// /////////////////////////////////
	public final static int TIMEOUT = -1;
	public final static int INTERNAL_SERVER_ERROR = -500;
	public final static int AUTHORIZATION_FAILURE = -401;
	public final static int SUBSCRIBER_FAILURE = -102;
	public final static int INCOMPLETE_REQUEST = -400;
	public final static int FORBIDDEN = -403;
	public final static int SERVER_OVERLOAD = -1007;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////

	/**
	 * Compulsory Serial Version UID
	 */
	private static final long serialVersionUID = -6706944935739535351L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Public Properties
	//
	// /////////////////////////////////

	private int responseCode;

	/**
	 * ZTE Response Code
	 */
	public int getResponseCode()
	{
		return responseCode;
	}

	public ReturnCodes getReturnCode()
	{
		return getReturnCode(responseCode);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * @param responseCode
	 *            ZTE Response Code
	 */
	public ZTEException(int responseCode)
	{
		super(getResponseMessage(responseCode));
		this.responseCode = responseCode;
	}

	/**
	 * 
	 * @param responseCode
	 *            ZTE Response Code
	 * @param cause
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public ZTEException(int responseCode, Throwable cause)
	{
		super(getResponseMessage(responseCode), cause);
		this.responseCode = responseCode;
	}

	/**
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Inner Exception
	 */
	public ZTEException(String message, Throwable cause)
	{
		super(message, cause);
		this.responseCode = 100;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	/**
	 * 
	 * @param responseCode
	 *            the ZTE Response Code
	 * @return The message corresponding to the responseCode
	 */
	public static String getResponseMessage(int responseCode)
	{
		switch (responseCode)
		{
			case 0:
				return ("Successful");
			case 1:
				return ("Ok but supervision period exceeded");
			case 2:
				return ("Ok but service fee period exceeded");
			case 100:
				return ("Other Error");
			case 101:
				return ("Not used");
			case 102:
				return ("Subscriber not found");
			case 103:
				return ("Account barred from refill");
			case 104:
				return ("Temporary blocked");
			case 105:
				return ("Dedicated account not allowed");
			case 106:
				return ("Dedicated account negative");
			case 107:
				return ("Voucher status used by same");
			case 108:
				return ("Voucher status used by different");
			case 109:
				return ("Voucher status unavailable");
			case 110:
				return ("Voucher status expired");
			case 111:
				return ("Voucher status stolen or missing");
			case 112:
				return ("Voucher status damaged");
			case 113:
				return ("Voucher status pending");
			case 114:
				return ("Voucher type not accepted");
			case 115:
				return ("Refill not accepted");
			case 117:
				return ("Service class change not allowed");
			case 119:
				return ("Invalid voucher activation code");
			case 120:
				return ("Invalid refill profile");
			case 121:
				return ("Supervision period too long");
			case 122:
				return ("Service fee period too long");
			case 123:
				return ("Max credit limit exceeded");
			case 124:
				return ("Below minimum balance");
			case 126:
				return ("Account not active");
			case 127:
				return ("Accumulator not available");
			case 128:
				return ("Invalid PIN code");
			case 129:
				return ("Faf number does not exist");
			case 130:
				return ("Faf number not allowed");
			case 133:
				return ("Service class list empty");
			case 134:
				return ("Accumulator overflow");
			case 135:
				return ("Accumulator underflow");
			case 136:
				return ("Date adjustment error");
			case 137:
				return ("Get balance and date not allowed");
			case 138:
				return ("No PIN code registered");
			case 139:
				return ("Dedicated account not defined");
			case 140:
				return ("Invalid old Service Class");
			case 141:
				return ("Invalid language");
			case 142:
				return ("Subscriber already installed");
			case 143:
				return ("Invalid master subscriber");
			case 144:
				return ("Subscriber already activated");
			case 145:
				return ("Already linked subordinate");
			case 146:
				return ("Already linked as master");
			case 147:
				return ("Invalid old community list");
			case 148:
				return ("Invalid new community list");
			case 149:
				return ("Invalid promotion plan end date");
			case 150:
				return ("Invalid promotion plan");
			case 151:
				return ("Promotion plan not found");
			case 152:
				return ("Deblocking of expired account");
			case 153:
				return ("Dedicated account max credit limit exceeded");
			case 154:
				return ("Invalid old SC date");
			case 155:
				return ("Invalid new service class");
			case 156:
				return ("Delete subscriber failed");
			case 157:
				return ("Invalid account home region");
			case 158:
				return ("Not used");
			case 159:
				return ("Charged FaF not active for service class");
			case 160:
				return ("Operation not allowed from current location");
			case 161:
				return ("Failed to get location information");
			case 163:
				return ("Invalid dedicated account period");
			case 164:
				return ("Invalid dedicated account start date");
			case 165:
				return ("Offer not found");
			case 166:
				return ("Not used");
			case 167:
				return ("Invalid unit type");
			case 168:
				return ("Not used");
			case 176:
				return ("Refill denied, First IVR call not made");
			case 177:
				return ("Refill denied, Account not active");
			case 178:
				return ("Refill denied, Service fee period expired");
			case 179:
				return ("Refill denied, Supervision period expired");
			case 190:
				return ("The PAM service id provided in the request already exist");
			case 191:
				return ("The PAM service id provided in the request was out of range, or did not exist");
			case 192:
				return ("The old PAM class id provided in the request was incorrect or did not match the existing PAM class id for the account");
			case 193:
				return ("The PAM class id or new PAM class id provided in the request was incorrect");
			case 194:
				return ("The old schedule id provided in the request was incorrect or did not match the existing schedule id for the account");
			case 195:
				return ("The schedule id or new schedule id provided in the request was incorrect");
			case 196:
				return ("Invalid deferred to date");
			case 197:
				return ("Periodic account management evaluation failed");
			case 198:
				return ("Too many PAM services given in the sequence or the number of services on the account would be exceeded");
			case 199:
				return ("The PAM period, provided or calculated, could not be found in the schedule");
			case 200:
				return ("The PAM class id or new PAM class id provided in the request does not exist");
			case 201:
				return ("The schedule id or new schedule id provided in the request did not exist or no valid period found");
			case 202:
				return ("Invalid PAM indicator");
			case 203:
				return ("Subscriber installed but marked for deletion");
			case 204:
				return ("Inconsistency between given current value and Account Database state");
			case 205:
				return ("Max number of FaF indicators exceeded");
			case 206:
				return ("FaF indicator already exists");
			case 207:
				return ("Invalid accumulator end date");
			case 208:
				return ("Invalid accumulator service class");
			case 209:
				return ("Invalid dedicated account expiry date");
			case 210:
				return ("Invalid dedicated account service class");
			case 211:
				return ("Delete dedicated account failed");
			case 212:
				return ("Crop of composite dedicated account not allowed");
			case 213:
				return ("Sub dedicated account not defined");
			case 214:
				return ("One or several of the provided offers are not defined or there is a mismatch between provided offer type and the offer type definition");
			case 215:
				return ("Too many offers of the type Multi User Identification given in the sequence or the Multi User Identification offer is already activated for the subscriber");
			case 216:
				return ("Usage threshold not found in definition");
			case 217:
				return ("Usage counter not found in definition");
			case 218:
				return ("The usage threshold does not exist on the account");
			case 219:
				return ("Usage counter value out of bounds");
			case 220:
				return ("The supplied value type does not match the definition");
			case 221:
				return ("No subordinate subscribers connected to the account");
			case 222:
				return ("Dedicated account cannot be deleted because of it is in use");
			case 223:
				return ("Service failed because new offer date provided in the request was incorrect");
			case 224:
				return ("The old offer date provided in the request did not match the current date");
			case 225:
				return ("The offer start date cannot be changed because the offer is already active");
			case 226:
				return ("Invalid PAM Period Relative Dates Start PAM Period Indicator");
			case 227:
				return ("Invalid PAM Period Relative Dates Expiry PAM Period Indicator");
			case 230:
				return ("Not allowed to convert to other type of lifetime");
			case 232:
				return ("Not allowed to delete PAM service ID in use. The PAM Service ID is used by an Offer, DA, or sub-DA's Relative Dates");
			case 233:
				return ("Invalid PAM Service Priority");
			case 234:
				return ("Old PAM service priority provided in the request was incorrect or did not match the existing PAM service priority for the account");
			case 235:
				return ("Not allowed to connect the PAM Class to the PAM Service. The account already has a bill cycle service. Only one bill cycle per account is allowed");
			case 236:
				return ("PAM Service Priority is already used for some other PAM service");
			case 237:
				return ("Not allowed to add a Provider ID to another offer type than provider account offer");
			case 238:
				return ("Not allowed to create a provider account offer without providing a Provider ID");
			case 239:
				return ("The request failed because the given time restriction does not exist");
			case 240:
				return ("The request failed because the end time was before the start time");
			case 241:
				return ("The time zone could not be found in the time zone mapping table");
			case 242:
				return ("Discount not defined");
			case 243:
				return ("Missing associated party ID for provider owned personal usage counter");
			case 244:
				return ("Associated party ID not allowed for provider owned common usage counter");
			case 245:
				return ("Provider owned common usage counter cannot have personal usage threshold");
			case 246:
				return ("The common usage threshold does not exist on the account");
			case 247:
				return ("Product not found");
			case 248:
				return ("Shared account offer is not allowed on a subordinate subscriber");
			case 249:
				return ("The product id specified was invalid, or a product id was expected but not supplied");
			case 250:
				return ("Communication ID change failed. Insufficient funds");
			case 251:
				return ("Communication ID change failed. AIA/MSISDN mismatch");
			case 252:
				return ("Communication ID change failed. Invalid combination of values");
			case 253:
				return ("Communication ID change failed. Previous MSISDN Change pending");
			case 254:
				return ("Communication ID change failed. New MSISDN not available");
			case 255:
				return ("Operation not allowed on subordinate subscriber");
			case 256:
				return ("Attribute name does not exist");
			case 257:
				return ("Operation not allowed since End of Provisioning is set");
			case 258:
				return ("Attribute value does not exist");
			case 259:
				return ("Attribute value already exists");
			case 260:
				return ("Capability not available");
			case 261:
				return ("Invalid Capability combination");
			case 999:
				return ("Other Error No Retry");

			case TIMEOUT:
				return ("Timed Out");
			case INTERNAL_SERVER_ERROR:
				return ("Internal Server Error");
			case AUTHORIZATION_FAILURE:
				return ("Authorization Error");
			case SUBSCRIBER_FAILURE:
				return ("Subscriber Error");
			case INCOMPLETE_REQUEST:
				return ("Incomplete Request");
			case FORBIDDEN:
				return ("Forbidden");
			case SERVER_OVERLOAD:
				return ("Server Overload");

			default:
				return String.format("Unknown Error %d", responseCode);
		}

	}

	public static ReturnCodes getReturnCode(int responseCode)
	{
		switch (responseCode)
		{
		// Successful
			case 0:
				return ReturnCodes.success;

				// OK but supervision period exceeded
			case 1:
				return ReturnCodes.success;

				// OK but service fee period exceeded
			case 2:
				return ReturnCodes.success;

				// Other Error
			case 100:
				return ReturnCodes.technicalProblem;

				// Not used
			case 101:
				return ReturnCodes.technicalProblem;

				// Subscriber not found
			case 102:
				return ReturnCodes.invalidNumber;

				// Account barred from refill
			case 103:
				return ReturnCodes.technicalProblem;

				// Temporary blocked
			case 104:
				return ReturnCodes.temporaryBlocked;

				// Dedicated account not allowed
			case 105:
				return ReturnCodes.technicalProblem;

				// Dedicated account negative
			case 106:
				return ReturnCodes.technicalProblem;

				// Voucher status used by same
			case 107:
				return ReturnCodes.technicalProblem;

				// Voucher status used by different
			case 108:
				return ReturnCodes.technicalProblem;

				// Voucher status unavailable
			case 109:
				return ReturnCodes.technicalProblem;

				// Voucher status expired
			case 110:
				return ReturnCodes.technicalProblem;

				// Voucher status stolen or missing
			case 111:
				return ReturnCodes.technicalProblem;

				// Voucher status damaged
			case 112:
				return ReturnCodes.technicalProblem;

				// Voucher status pending
			case 113:
				return ReturnCodes.technicalProblem;

				// Voucher type not accepted
			case 114:
				return ReturnCodes.technicalProblem;

				// Refill not accepted
			case 115:
				return ReturnCodes.technicalProblem;

				// Service class change not allowed
			case 117:
				return ReturnCodes.technicalProblem;

				// Invalid voucher activation code
			case 119:
				return ReturnCodes.technicalProblem;

				// Invalid refill profile
			case 120:
				return ReturnCodes.technicalProblem;

				// Supervision period too long
			case 121:
				return ReturnCodes.technicalProblem;

				// Service fee period too long
			case 122:
				return ReturnCodes.technicalProblem;

				// Max credit limit exceeded
			case 123:
				return ReturnCodes.insufficientBalance;

				// Below minimum balance
			case 124:
				return ReturnCodes.insufficientBalance;

				// Account not active
			case 126:
				return ReturnCodes.technicalProblem;

				// Accumulator not available
			case 127:
				return ReturnCodes.technicalProblem;

				// Invalid PIN code
			case 128:
				return ReturnCodes.technicalProblem;

				// Faf number does not exist
			case 129:
				return ReturnCodes.technicalProblem;

				// Faf number not allowed
			case 130:
				return ReturnCodes.technicalProblem;

				// Service class list empty
			case 133:
				return ReturnCodes.technicalProblem;

				// Accumulator overflow
			case 134:
				return ReturnCodes.technicalProblem;

				// Accumulator underflow
			case 135:
				return ReturnCodes.technicalProblem;

				// Date adjustment error
			case 136:
				return ReturnCodes.technicalProblem;

				// Get balance and date not allowed
			case 137:
				return ReturnCodes.technicalProblem;

				// No PIN code registered
			case 138:
				return ReturnCodes.technicalProblem;

				// Dedicated account not defined
			case 139:
				return ReturnCodes.technicalProblem;

				// Invalid old Service Class
			case 140:
				return ReturnCodes.technicalProblem;

				// Invalid language
			case 141:
				return ReturnCodes.technicalProblem;

				// Subscriber already installed
			case 142:
				return ReturnCodes.technicalProblem;

				// Invalid master subscriber
			case 143:
				return ReturnCodes.cannotBeAdded;

				// Subscriber already activated
			case 144:
				return ReturnCodes.cannotBeAdded;

				// Already linked subordinate
			case 145:
				return ReturnCodes.alreadyAdded;

				// Already linked as master
			case 146:
				return ReturnCodes.cannotBeAdded;

				// Invalid old community list
			case 147:
				return ReturnCodes.technicalProblem;

				// Invalid new community list
			case 148:
				return ReturnCodes.technicalProblem;

				// Invalid promotion plan end date
			case 149:
				return ReturnCodes.technicalProblem;

				// Invalid promotion plan
			case 150:
				return ReturnCodes.technicalProblem;

				// Promotion plan not found
			case 151:
				return ReturnCodes.technicalProblem;

				// Deblocking of expired account
			case 152:
				return ReturnCodes.technicalProblem;

				// Dedicated account max credit limit exceeded
			case 153:
				return ReturnCodes.insufficientBalance;

				// Invalid old SC date
			case 154:
				return ReturnCodes.technicalProblem;

				// Invalid new service class
			case 155:
				return ReturnCodes.technicalProblem;

				// Delete subscriber failed
			case 156:
				return ReturnCodes.technicalProblem;

				// Invalid account home region
			case 157:
				return ReturnCodes.technicalProblem;

				// Not used
			case 158:
				return ReturnCodes.technicalProblem;

				// Charged FaF not active for service class
			case 159:
				return ReturnCodes.technicalProblem;

				// Operation not allowed from current location
			case 160:
				return ReturnCodes.technicalProblem;

				// Failed to get location information
			case 161:
				return ReturnCodes.technicalProblem;

				// Invalid dedicated account period
			case 163:
				return ReturnCodes.technicalProblem;

				// Invalid dedicated account start date
			case 164:
				return ReturnCodes.technicalProblem;

				// Offer not found
			case 165:
				return ReturnCodes.technicalProblem;

				// Not used
			case 166:
				return ReturnCodes.technicalProblem;

				// Invalid unit type
			case 167:
				return ReturnCodes.technicalProblem;

				// Not used
			case 168:
				return ReturnCodes.technicalProblem;

				// Refill denied, First IVR call not made
			case 176:
				return ReturnCodes.technicalProblem;

				// Refill denied, Account not active
			case 177:
				return ReturnCodes.technicalProblem;

				// Refill denied, Service fee period expired
			case 178:
				return ReturnCodes.technicalProblem;

				// Refill denied, Supervision period expired
			case 179:
				return ReturnCodes.technicalProblem;

				// The PAM service id provided in the request already exist
			case 190:
				return ReturnCodes.technicalProblem;

				// The PAM service id provided in the request was out of range, or did not exist
			case 191:
				return ReturnCodes.technicalProblem;

				// The old PAM class id provided in the request was incorrect or did not match the existing PAM class id for the account
			case 192:
				return ReturnCodes.technicalProblem;

				// The PAM class id or new PAM class id provided in the request was incorrect
			case 193:
				return ReturnCodes.technicalProblem;

				// The old schedule id provided in the request was incorrect or did not match the existing schedule id for the account
			case 194:
				return ReturnCodes.technicalProblem;

				// The schedule id or new schedule id provided in the request was incorrect
			case 195:
				return ReturnCodes.technicalProblem;

				// Invalid deferred to date
			case 196:
				return ReturnCodes.technicalProblem;

				// Periodic account management evaluation failed
			case 197:
				return ReturnCodes.technicalProblem;

				// Too many PAM services given in the sequence or the number of services on the account would be exceeded
			case 198:
				return ReturnCodes.technicalProblem;

				// The PAM period, provided or calculated, could not be found in the schedule
			case 199:
				return ReturnCodes.technicalProblem;

				// The PAM class id or new PAM class id provided in the request does not exist
			case 200:
				return ReturnCodes.technicalProblem;

				// The schedule id or new schedule id provided in the request did not exist or no valid period found
			case 201:
				return ReturnCodes.technicalProblem;

				// Invalid PAM indicator
			case 202:
				return ReturnCodes.technicalProblem;

				// Subscriber installed but marked for deletion
			case 203:
				return ReturnCodes.technicalProblem;

				// Inconsistency between given current value and Account Database state
			case 204:
				return ReturnCodes.technicalProblem;

				// Max number of FaF indicators exceeded
			case 205:
				return ReturnCodes.technicalProblem;

				// FaF indicator already exists
			case 206:
				return ReturnCodes.technicalProblem;

				// Invalid accumulator end date
			case 207:
				return ReturnCodes.technicalProblem;

				// Invalid accumulator service class
			case 208:
				return ReturnCodes.technicalProblem;

				// Invalid dedicated account expiry date
			case 209:
				return ReturnCodes.technicalProblem;

				// Invalid dedicated account service class
			case 210:
				return ReturnCodes.technicalProblem;

				// Delete dedicated account failed
			case 211:
				return ReturnCodes.technicalProblem;

				// Crop of composite dedicated account not allowed
			case 212:
				return ReturnCodes.technicalProblem;

				// Sub dedicated account not defined
			case 213:
				return ReturnCodes.technicalProblem;

				// One or several of the provided offers are not defined or there is a mismatch between provided offer type and the offer type definition
			case 214:
				return ReturnCodes.technicalProblem;

				// Too many offers of the type Multi User Identification given in the sequence or the Multi User Identification offer is already activated for the subscriber
			case 215:
				return ReturnCodes.technicalProblem;

				// Usage threshold not found in definition
			case 216:
				return ReturnCodes.technicalProblem;

				// Usage counter not found in definition
			case 217:
				return ReturnCodes.technicalProblem;

				// The usage threshold does not exist on the account
			case 218:
				return ReturnCodes.technicalProblem;

				// Usage counter value out of bounds
			case 219:
				return ReturnCodes.technicalProblem;

				// The supplied value type does not match the definition
			case 220:
				return ReturnCodes.technicalProblem;

				// No subordinate subscribers connected to the account
			case 221:
				return ReturnCodes.technicalProblem;

				// Dedicated account cannot be deleted because of it is in use
			case 222:
				return ReturnCodes.technicalProblem;

				// Service failed because new offer date provided in the request was incorrect
			case 223:
				return ReturnCodes.technicalProblem;

				// The old offer date provided in the request did not match the current date
			case 224:
				return ReturnCodes.technicalProblem;

				// The offer start date cannot be changed because the offer is already active
			case 225:
				return ReturnCodes.technicalProblem;

				// Invalid PAM Period Relative Dates Start PAM Period Indicator
			case 226:
				return ReturnCodes.technicalProblem;

				// Invalid PAM Period Relative Dates Expiry PAM Period Indicator
			case 227:
				return ReturnCodes.technicalProblem;

				// Not allowed to convert to other type of lifetime
			case 230:
				return ReturnCodes.technicalProblem;

				// Not allowed to delete PAM service ID in use. The PAM Service ID is used by an Offer, DA, or sub-DA's Relative Dates
			case 232:
				return ReturnCodes.technicalProblem;

				// Invalid PAM Service Priority
			case 233:
				return ReturnCodes.technicalProblem;

				// Old PAM service priority provided in the request was incorrect or did not match the existing PAM service priority for the account
			case 234:
				return ReturnCodes.technicalProblem;

				// Not allowed to connect the PAM Class to the PAM Service. The account already has a bill cycle service. Only one bill cycle per account is allowed
			case 235:
				return ReturnCodes.technicalProblem;

				// PAM Service Priority is already used for some other PAM service
			case 236:
				return ReturnCodes.technicalProblem;

				// Not allowed to add a Provider ID to another offer type than provider account offer
			case 237:
				return ReturnCodes.technicalProblem;

				// Not allowed to create a provider account offer without providing a Provider ID
			case 238:
				return ReturnCodes.technicalProblem;

				// The request failed because the given time restriction does not exist
			case 239:
				return ReturnCodes.technicalProblem;

				// The request failed because the end time was before the start time
			case 240:
				return ReturnCodes.technicalProblem;

				// The time zone could not be found in the time zone mapping table
			case 241:
				return ReturnCodes.technicalProblem;

				// Discount not defined
			case 242:
				return ReturnCodes.technicalProblem;

				// Missing associated party ID for provider owned personal usage counter
			case 243:
				return ReturnCodes.technicalProblem;

				// Associated party ID not allowed for provider owned common usage counter
			case 244:
				return ReturnCodes.technicalProblem;

				// Provider owned common usage counter cannot have personal usage threshold
			case 245:
				return ReturnCodes.technicalProblem;

				// The common usage threshold does not exist on the account
			case 246:
				return ReturnCodes.technicalProblem;

				// Product not found
			case 247:
				return ReturnCodes.technicalProblem;

				// Shared account offer is not allowed on a subordinate subscriber
			case 248:
				return ReturnCodes.technicalProblem;

				// The product id specified was invalid, or a product id was expected but not supplied
			case 249:
				return ReturnCodes.technicalProblem;

				// Communication ID change failed. Insufficient funds
			case 250:
				return ReturnCodes.technicalProblem;

				// Communication ID change failed. AIA/MSISDN mismatch
			case 251:
				return ReturnCodes.technicalProblem;

				// Communication ID change failed. Invalid combination of values
			case 252:
				return ReturnCodes.technicalProblem;

				// Communication ID change failed. Previous MSISDN Change pending
			case 253:
				return ReturnCodes.technicalProblem;

				// Communication ID change failed. New MSISDN not available
			case 254:
				return ReturnCodes.technicalProblem;

				// Operation not allowed on subordinate subscriber
			case 255:
				return ReturnCodes.technicalProblem;

				// Attribute name does not exist
			case 256:
				return ReturnCodes.technicalProblem;

				// Operation not allowed since End of Provisioning is set
			case 257:
				return ReturnCodes.technicalProblem;

				// Attribute value does not exist
			case 258:
				return ReturnCodes.technicalProblem;

				// Attribute value already exists
			case 259:
				return ReturnCodes.technicalProblem;

				// Capability not available
			case 260:
				return ReturnCodes.technicalProblem;

				// Invalid Capability combination
			case 261:
				return ReturnCodes.technicalProblem;

				// Other Error No Retry
			case 999:
				return ReturnCodes.technicalProblem;

			case TIMEOUT:
				return ReturnCodes.timedOut;

			case INTERNAL_SERVER_ERROR:
				return ReturnCodes.technicalProblem;

			case AUTHORIZATION_FAILURE:
				return ReturnCodes.authorizationFailure;

			case SUBSCRIBER_FAILURE:
				return ReturnCodes.memberNotEligible;

			case INCOMPLETE_REQUEST:
				return ReturnCodes.invalidArguments;

			case FORBIDDEN:
				return ReturnCodes.authorizationFailure;

			case SERVER_OVERLOAD:
				return ReturnCodes.technicalProblem;

			default:
				return ReturnCodes.technicalProblem;
		}

	}

}
