package hxc.services.ecds.rest.ussd;

import java.util.Map;

import javax.persistence.EntityManager;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.IChannelTarget;
import hxc.services.ecds.util.RuleCheckException;

public interface IMenuProcessor extends IChannelTarget
{
	static final int OPTION_OPT_OUT = 1;

	public abstract Phrase menuName();

	public abstract Phrase[] menuCommandFields(EntityManager em, int companyID);

	public abstract Phrase[] menuInformationFields(EntityManager em, int companyID);
	
	public abstract String menuDescribeField(String fieldName);

	public MenuOption[] menuOptions(EntityManager em, Session session, String field);

	public abstract boolean menuMayExecute(EntityManager em, Session session) throws RuleCheckException;

	public abstract TransactionResponse menuExecute(EntityManager em, Session session, //
			IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException;

	public abstract String menuExpandField(String englishName, Session session, Map<String, String> valueMap);
}
