package hxc.connectors.soap;

import hxc.servicebus.ReturnCodes;

public class EngReturnCodeTexts
{

	public static String getReturnCodeText(ReturnCodes returnCode)
	{
		switch (returnCode)
		{
			case alreadyAdded:
				return "Already Added";

			case alreadyMember:
				return "Already a Member";

			case alreadyOtherMember:
				return "Already someone else's Member";

			case alreadyOwner:
				return "Already an Owner";

			case cannotBeAdded:
				return "The offer cannot be added";

			case insufficientBalance:
				return "Insufficient Balance";

			case invalidQuota:
				return "Invalid Quota";

			case maxMembersExceeded:
				return "Maximum Number of Members have been exceeded";

			case notMember:
				return "Not a Member";

			case quotaNotSet:
				return "Quota not set";

			case timedOut:
				return "Operation Timed Out";

			case alreadySubscribed:
				return "You are already subscribed";

			case authorizationFailure:
				return "Supplied user/password has not been authorized";

			case cannotAddSelf:
				return "Cannot Add yourself";

			case cannotCallSelf:
				return "Cannot Call yourself";

			case cannotMigrateToSameVariant:
				return "Cannot change to same";

			case cannotReceiveCredit:
				return "Cannot receive Credit";

			case cannotTransferToSelf:
				return "Cannot transfer to yourself";

			case excessiveBalance:
				return "Your Balance is too high";

			case inactiveAParty:
				return "The A Party is not Active";

			case inactiveBParty:
				return "The B Party is not Active";

			case incomplete:
				return "Request incomplete";

			case invalidArguments:
				return "Invalid arguments have been supplied";

			case invalidNumber:
				return "An invalid number has been supplied";

			case invalidPin:
				return "Invalid PIN";

			case invalidService:
				return "Invalid Service";

			case invalidVariant:
				return "Invalid Variant";

			case malformedRequest:
				return "The request is malformed";

			case notEligible:
				return "You are not eligible for the service";

			case notSubscribed:
				return "The subscriber is not subscribed to the service";

			case notSupported:
				return "The operation is not supported for this service";

			case quantityTooBig:
				return "Quantity too large";

			case quantityTooSmall:
				return "Quantity too small";

			case quotaReached:
				return "You have reached your Quota";

			case serviceBusy:
				return "The Service is too Busy";

			case success:
				return "Success";

			case successfulTest:
				return "Successful Test";

			case suspended:
				return "Your Service has been Suspended";

			case technicalProblem:
				return "A Technical Problem has occurred. Please try again later";

			case temporaryBlocked:
				return "The Service is not currently available";

			case memberNotEligible:
				return "The member you have entered is not eligible for the service";

			case pinBlocked:
				return "Pin Blocked";

			case unregisteredPin:
				return "Unregistered Pin";

			case cannotBeSuspended:
				return "The Service cannot be Suspended";

			case cannotBeResumed:
				return "The Service cannot be Resumed";

			case maxAmountExceeded:
				return "Maximum allowed amount exceeded";

			case maxCountExceeded:
				return "Maximum allowed count exceeded";



			default:
				return null;
		}
	}

}
