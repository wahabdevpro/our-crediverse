package cs.dto.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.constants.MissingApiConstants;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.Violation;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class GuiViolation {
	private static final Logger logger = LoggerFactory.getLogger(GuiViolation.class);
	
	public enum ParameterType {
		match,
		min,
		minSize,
		maxSize,
		value,
		unknown
	}
	
	public enum ViolationType {
		cannotBeEmpty,
		cannotHaveValue,
		invalidValue,
		invalidValueGeneral,
		cantBeChanged,
		notSame,
		notSameGeneral,		// No matching value provided
		tooSmall,
		tooSmallGeneral,	// No Value Supplied
		tooLong,
		tooLongGeneral,		// No Value supplied
		tooShort,
		tooShortGeneral,	// No Value supplied
		recursive,

		cannotBeChanged,
		tooLarge,
		failedToSave,
		failedToDelete,
		notFound,
		forbidden,
		ambiguous,
		cannotAdd,
		cannotDelete,
		cannotDeleteSelf,
		unauthorized,
		tampered,
		dailyCountLimit,
		dailyAmountLimit,
		monthlyCountLimit,
		monthlyAmoutLimit,
		insufficientFunds,
		insufficientProvision,
		alreadyRegisterd,
		notRegistered,
		invalidPin,
		confirmPinDifferent,
		emptyPin,
		pinTooShort,
		pinTooLong,
		pinNotNumeric,
		repeatedPin,
		invalidChannel,
		invalidState,
		duplicateValue,
		resourceInUse,
		limitReached,

		invalidTimeFormat,
		invalidTimeHMFormat,
		invalidTimeHour,
		invalidTimeMinute,
		invalidTimeSecond,
		CO_AUTHORIZE,

		unknown,			// Overall Technical Error (we can't help you buddy!)
	}
	
	private String field;
	private String correlationId;

	private List<String> validations;
	private List<String> msgs;
	private Map<String, String> parameters;
	
	public GuiViolation(String property, String correlationId) {
		this.field = property;
		this.validations = new ArrayList<>();
		this.parameters = new HashMap<>();
		this.msgs = new ArrayList<>();
		this.correlationId = correlationId;
	}
	
	public static GuiViolation generalViolation(String correlationId, String msg)
	{
		ResponseHeader header = null;
		if (msg != null && msg.trim().indexOf("{") ==0) {
			header = extractReponseHeader(msg);
		}
		
		GuiViolation result = new GuiViolation(null, correlationId);
		
		if (header == null) 
		{
			String error = result.extractField(msg);
			result.validations.add(processGeneralMessage(error, msg));
			result.msgs.add(msg);
		}
		else
		{
			String error = result.extractField(header.getReturnCode());
			result.validations.add( processGeneralMessage(error, header.getAdditionalInformation()) );
			result.msgs.add(header.getAdditionalInformation());
		}
		
		return result;
	}

	public String extractField(String error)
	{
		int posSlash = error.indexOf("/");
		if (posSlash > 0)
		{
			String msg = error.substring(0, posSlash);
			this.field = error.substring(posSlash + 1);
			return msg;
		}
		return error;
	}
	
	public static ResponseHeader extractReponseHeader(String error)
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.setSerializationInclusion(Include.NON_NULL);
			ResponseHeader header = mapper.readValue(error, BasicECDSProtocolResponseHeader.class);
			return header;
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		return null;
	}
	/**
	 * 
	 * @param error	Error from Transaction Server (e.g. 406 ERROR_CODE) 
	 * @return	A Gui Validation Type which can be used in Javascript for a useful error message, currently these are referenced in the js/app/nls/violations.js
	 */
	public static String processGeneralMessage(String error, String additionalInformation)
	{
		if (error != null)
		{
			// Try something else ...
			String [] msgs = error.split(" ");
			if (msgs.length == 2) 
			{
				//Extract Field if present
				return extractViolationType(msgs[1], null, additionalInformation); 	// msgs[1] := errorCode
			}
			else
				return extractViolationType(error, null, additionalInformation);
			
		}
		return ViolationType.unknown.toString();
	}
	
	private static String extractViolationType(String returnCode, Object criterium, String additionalInformation) 
	{
		switch (returnCode) 
		{
			case MissingApiConstants.TOO_LONG:
				if (criterium != null)
					return ViolationType.tooLong.toString();
				else
					return ViolationType.tooLongGeneral.toString();
				
			case MissingApiConstants.NOT_SAME:
				if (criterium != null)
					return ViolationType.notSame.toString();
				else
					return ViolationType.notSameGeneral.toString();
				
			case MissingApiConstants.TOO_SMALL:
				if (criterium == null)
					return ViolationType.tooSmallGeneral.toString();
				else
					return ViolationType.tooSmall.toString();		
				
			case MissingApiConstants.CANNOT_BE_EMPTY:
				return ViolationType.cannotBeEmpty.toString();
			
			case MissingApiConstants.RECURSIVE:			
				return ViolationType.recursive.toString();
			
			case MissingApiConstants.INVALID_VALUE:
				if (criterium == null)
					return ViolationType.invalidValueGeneral.toString();	
				else
					return ViolationType.invalidValue.toString();

			case MissingApiConstants.CANNOT_HAVE_VALUE:	
				return ViolationType.cannotHaveValue.toString();
			
			case MissingApiConstants.CANT_BE_CHANGED:	
				return ViolationType.cannotBeChanged.toString();

			case MissingApiConstants.TOO_LARGE:			
				return ViolationType.tooLarge.toString();
			
			case MissingApiConstants.FAILED_TO_SAVE:		
				return ViolationType.failedToSave.toString();
				
			case MissingApiConstants.DUPLICATE_VALUE:
				return ViolationType.duplicateValue.toString();
			
			case MissingApiConstants.FAILED_TO_DELETE:
				return ViolationType.resourceInUse.toString();
				
			case MissingApiConstants.RESOURCE_IN_USE:
				return ViolationType.failedToDelete.toString();
			
			case MissingApiConstants.NOT_FOUND:			
				return ViolationType.notFound.toString();
			
			case MissingApiConstants.FORBIDDEN:			
				return ViolationType.forbidden.toString();
			
			case MissingApiConstants.AMBIGUOUS:			
				return ViolationType.ambiguous.toString();
			
			case MissingApiConstants.CANNOT_ADD:			
				return ViolationType.cannotAdd.toString();
			
			case MissingApiConstants.CANNOT_DELETE:		
				return ViolationType.cannotDelete.toString();
			
			case MissingApiConstants.CANNOT_DELETE_SELF:
				return ViolationType.cannotDeleteSelf.toString();
			
			case MissingApiConstants.UNAUTHORIZED:
				return ViolationType.unauthorized.toString();
			
			case MissingApiConstants.TAMPERED:			
				return ViolationType.tampered.toString();

//			case MissingApiConstants.TECHNICAL_PROBLEM:	
			
			case MissingApiConstants.DAY_COUNT_LIMIT:	
				return ViolationType.dailyCountLimit.toString();
			
			case MissingApiConstants.DAY_AMOUNT_LIMIT:	
				return ViolationType.dailyAmountLimit.toString();
			
			case MissingApiConstants.MONTH_COUNT_LIMIT:	
				return ViolationType.monthlyCountLimit.toString();
			
			case MissingApiConstants.MONTH_AMOUNT_LIMIT:	
				return ViolationType.monthlyAmoutLimit.toString();
			
			case MissingApiConstants.INSUFFICIENT_FUNDS:	
				return ViolationType.insufficientFunds.toString();
			
			case MissingApiConstants.INSUFFICIENT_PROVISION:	
				return ViolationType.insufficientProvision.toString();
			
			case MissingApiConstants.ALREADY_REGISTERED:	
				return ViolationType.alreadyRegisterd.toString();
			
			case MissingApiConstants.NOT_REGISTERED:		
				return ViolationType.notRegistered.toString();
			
			case MissingApiConstants.INVALID_PIN:	
				if (additionalInformation != null)
				{
					switch(additionalInformation) {
						case "Empty PIN":
							return ViolationType.emptyPin.toString();
						case "PIN too short":
							return ViolationType.pinTooShort.toString();
						case "PIN too long":
							return ViolationType.pinTooLong.toString();
						case "PIN not numeric":
							return ViolationType.pinNotNumeric.toString();
						case "Repeated PIN":
							return ViolationType.repeatedPin.toString();						
					}
				}
				return ViolationType.invalidPin.toString();

			case MissingApiConstants.CONFIRM_PIN_DIFF:
				return ViolationType.confirmPinDifferent.toString();

			case MissingApiConstants.INVALID_CHANNEL:	
				return ViolationType.invalidChannel.toString();

			case MissingApiConstants.INVALID_STATE:		
				return ViolationType.invalidState.toString();

			case MissingApiConstants.TOO_SHORT:
				return ViolationType.tooShort.toString();

			case MissingApiConstants.LIMIT_REACHED:
				return ViolationType.limitReached.toString();

			default:
				return returnCode;
		}
	}
	
	public void extractViolation(Violation violation) 
	{
		extractViolation(violation.getReturnCode(), violation.getCriterium(), violation.getAdditionalInformation());
	}
	
	public  void extractViolation(String returnCode, Object criterium, String additionalInformation) 
	{
		String svtype = extractViolationType(returnCode, criterium, additionalInformation);
		try {
			ViolationType vtype = ViolationType.valueOf(svtype);
			
			if (criterium != null)
			{
				switch(vtype)
				{
					case notSame:
						parameters.put(ParameterType.match.toString(), criterium.toString());
						break;
					case tooSmall:
						parameters.put(ParameterType.min.toString(), criterium.toString());
						break;
					case tooLong:
						parameters.put(ParameterType.maxSize.toString(), criterium.toString());
						break;
					case tooShort:
						parameters.put(ParameterType.minSize.toString(), criterium.toString());
						break;
					case invalidValue:
						parameters.put(ParameterType.value.toString(), criterium.toString());
						break;
					default:
						parameters.put("value", criterium.toString());
				}
			}
			validations.add(vtype.toString());
		} catch(Exception e) {
			parameters.put("value", criterium.toString());
			validations.add(returnCode);
		}
		
		msgs.add((additionalInformation != null)? additionalInformation : "");
	}
	
}
