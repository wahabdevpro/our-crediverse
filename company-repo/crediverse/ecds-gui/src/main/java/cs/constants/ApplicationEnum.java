package cs.constants;

import hxc.ecds.protocol.rest.Batch;

public class ApplicationEnum
{
	/*
	 * Mapping to batch states defined by TS
	 */

	// States inferable by TS
	public static final String STATE_PROCESSING				= Batch.STATE_PROCESSING;
	public static final String STATE_COMPLETE 				= Batch.STATE_COMPLETED;
	public static final String STATE_ERROR 					= Batch.STATE_FAILED;

	// Other states not inferable by TS which can be set via BatchUploadRequest
	public static final String STATE_NEW 					= Batch.STATE_UNKNOWN;
	public static final String STATE_PENDING_AUTHORIZATION 	= Batch.STATE_AUTHORIZING;
	public static final String STATE_CANCELLED 				= Batch.STATE_CANCELLED;
	public static final String STATE_DECLINED 				= Batch.STATE_DECLINED;
	public static final String STATE_VERIFYING 				= Batch.STATE_UPLOADING;
	public static final String STATE_ONHOLD 				= Batch.STATE_SUSPENDED;

	/*
	 * Additional states not defined by TS
	 */
	public static final String STATE_QUEUED 				= "Q";
	public static final String STATE_READY_FOR_PROCESSING 	= "R";
	public static final String STATE_VERIFIED 				= "V";

	public enum BatchStatusEnum
	{
		NEW(STATE_NEW),
		QUEUED(STATE_QUEUED),
		VERIFYING(STATE_VERIFYING),
		VERIFIED(STATE_VERIFIED),
		PENDING_AUTHORIZATION(STATE_PENDING_AUTHORIZATION),
		READY_FOR_PROCESSING(STATE_READY_FOR_PROCESSING),
		ONHOLD(STATE_ONHOLD),
		PROCESSING(STATE_PROCESSING),
		COMPLETE(STATE_COMPLETE),
		CANCELLED(STATE_CANCELLED),
		DECLINED(STATE_DECLINED),
		ERROR(STATE_ERROR);

		private final String tsState;

		private BatchStatusEnum(String tsState)
		{
			this.tsState = tsState;
		}

		public String getTsState() {
			return tsState;
		}

		public static BatchStatusEnum fromTsValue(String newValue)
		{
			BatchStatusEnum result = NEW;
			switch(newValue)
			{
				case STATE_NEW:
					result =  NEW;
					break;
				case STATE_QUEUED:
					result =  QUEUED;
					break;
				case STATE_VERIFYING:
					result =  VERIFYING;
					break;
				case STATE_VERIFIED:
					result =  VERIFIED;
					break;
				case STATE_PENDING_AUTHORIZATION:
					result =  PENDING_AUTHORIZATION;
					break;
				case STATE_READY_FOR_PROCESSING:
					result =  READY_FOR_PROCESSING;
					break;
				case STATE_ONHOLD:
					result =  ONHOLD;
					break;
				case STATE_PROCESSING:
					result =  PROCESSING;
					break;
				case STATE_COMPLETE:
					result =  COMPLETE;
					break;
				case STATE_CANCELLED:
					result =  CANCELLED;
					break;
				case STATE_DECLINED:
					result =  DECLINED;
					break;
				case STATE_ERROR:
					result =  ERROR;
					break;
			}
			return result;
		}
	};

	/*
	 * Note that the Enum value must be less than 9 characters.
	 */
	public enum ClientStateContext{INTERNAL, BATCH, WORKFLOW, SESSION}
}
