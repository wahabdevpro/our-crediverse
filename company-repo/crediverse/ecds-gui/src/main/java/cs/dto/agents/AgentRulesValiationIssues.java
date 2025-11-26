package cs.dto.agents;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AgentRulesValiationIssues
{
	public static enum IssueType
	{
		NO_RULES_FOUND,
		TARGET_GROUP_DIFFERS,
		SERVICE_CLASS_DIFFERS,
		RULE_NOT_ALLOWED_ON,
		RULE_NOT_ALLOWED_BEFORE,
		RULE_NOT_ALLOWED_AFTER,
	}

	private int transferAllowed = 0;
	private List<Issue> ruleIssues = new ArrayList<>();

	public void addIssue(IssueType issueType, String rule, String dateTime)
	{
		ruleIssues.add( new Issue(issueType, rule, dateTime) );
	}

	public void addIssue(IssueType issueType)
	{
		this.addIssue(issueType, null, null);
	}

	public void addIssue(IssueType issueType, String rule)
	{
		this.addIssue(issueType, rule, null);
	}

	public void incrementAllowedRulesCount()
	{
		this.transferAllowed ++;
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	class Issue
	{
		private IssueType issue;
		private String rule;
		private String dateTime;
	}
}
