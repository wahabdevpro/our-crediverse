package hxc.services.numberplan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class NumberPlanService implements IService, INumberPlan
{
	final static Logger logger = LoggerFactory.getLogger(NumberPlanService.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	private static final String numberPatternList = "[0-9,\\,,x,X]*";
	private static final String validNumberPattern = "^[\\+]{0,1}\\d{1,28}$";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Patterns
	//
	// /////////////////////////////////
	private volatile boolean hasCompiledPatterns = false;
	private Pattern isOnnetPattern;
	private Pattern isMobilePattern;
	private Pattern isSpecialPattern;
	private Pattern isFixedPattern;
	private Pattern isNationalPattern;
	private Pattern isValidPattern;
	private Pattern isFullyQualifiedPattern;
	private Pattern[] migratingOnnetPatterns;
	private String[] migratingOnnetPrefixes;
	private boolean hasOnnetMigrations = false;

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		// Log Information
		logger.info("Number Plan Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("Number Plan Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (NumberPlanChangeConfig) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = {
			@Perm(name = "ViewNumberPlanChangeParameters", description = "View Number Plan Change Parameters", category = "NumberPlanChange", supplier = false),
			@Perm(name = "ChangeNumberPlanChangeParameters", implies = "ViewNumberPlanChangeParameters", description = "Change Number Plan Change Parameters", category = "NumberPlanChange", supplier = false) })
	public class NumberPlanChangeConfig extends ConfigurationBase
	{
		private String internationalAccessCodes = "00,+";
		private String nationalDailingCode = "237";
		private String onNetNumbers = "67xxxxxxx,7xxxxxxx,65xxxxxxx,5xxxxxxx";
		private String otherMobileNumbers = "8xxxxxxx,9xxxxxxx";
		private String specialNumbers = "1xx";
		private String fixedLineNumbers = "2xxxxxxx,3xxxxxxx";
		private String migratingOnnetNumbers = "";

		public String getInternationalAccessCodes()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return internationalAccessCodes;
		}

		public void setInternationalAccessCodes(String internationalAccessCodes) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(internationalAccessCodes, "[0-9,\\,,\\+]+", "InternationalAccessCodes");
			hasCompiledPatterns = false;
			this.internationalAccessCodes = internationalAccessCodes;
		}

		public String getNationalDailingCode()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return nationalDailingCode;
		}

		public void setNationalDailingCode(String nationalDailingCode) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(nationalDailingCode, "[0-9]{1,5}", "NationalDailingCode");
			hasCompiledPatterns = false;
			this.nationalDailingCode = nationalDailingCode;
		}

		public String getOnNetNumbers()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return onNetNumbers;
		}

		public void setOnNetNumbers(String onNetNumbers) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(onNetNumbers, numberPatternList, "OnNetNumbers");
			hasCompiledPatterns = false;
			this.onNetNumbers = onNetNumbers;
		}

		public String getOtherMobileNumbers()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return otherMobileNumbers;
		}

		public void setOtherMobileNumbers(String otherMobileNumbers) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(otherMobileNumbers, numberPatternList);
			hasCompiledPatterns = false;
			this.otherMobileNumbers = otherMobileNumbers;
		}

		public String getSpecialNumbers()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return specialNumbers;
		}

		public void setSpecialNumbers(String specialNumbers) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(specialNumbers, numberPatternList);
			hasCompiledPatterns = false;
			this.specialNumbers = specialNumbers;
		}

		public String getFixedLineNumbers()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return fixedLineNumbers;
		}

		public void setFixedLineNumbers(String fixedLineNumbers) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			ValidationException.validate(fixedLineNumbers, numberPatternList);
			hasCompiledPatterns = false;
			this.fixedLineNumbers = fixedLineNumbers;
		}

		public String getMigratingOnnetNumbers()
		{
			check(esb, "ViewNumberPlanChangeParameters");
			return migratingOnnetNumbers;
		}

		public void setMigratingOnnetNumbers(String migratingOnnetNumbers) throws ValidationException
		{
			check(esb, "ChangeNumberPlanChangeParameters");
			if (migratingOnnetNumbers != null && migratingOnnetNumbers.length() > 0)
			{
				ValidationException.validate(migratingOnnetNumbers, "^[0-9]+[x,X]+\\=[0-9]+[x,X]+(\\,[0-9]+[x,X]+\\=[0-9]+[x,X]+)*$");

				Pattern pairPattern = Pattern.compile("([0-9]+)([x,X]+)\\=([0-9]+)([x,X]+)");
				String[] migratingPairs = migratingOnnetNumbers.split("\\,");
				for (String migratingPair : migratingPairs)
				{
					Matcher matcher = pairPattern.matcher(migratingPair);
					if (!matcher.find() || !matcher.group(2).equalsIgnoreCase(matcher.group(4)))
					{
						throw new ValidationException("Invalid Migration: %s", migratingPair);
					}
				}

			}
			hasCompiledPatterns = false;
			this.migratingOnnetNumbers = migratingOnnetNumbers;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -9223372036854775806L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Number Plan Service";
		}

		@Override
		public void validate() throws ValidationException
		{
			hasCompiledPatterns = false;
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeNumberPlanChangeNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewNumberPlanChangeNotifications");
		}

		@Override
		public boolean save(IDatabaseConnection database, ICtrlConnector control)
		{
			hasCompiledPatterns = false;
			return super.save(database, control);
		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			hasCompiledPatterns = false;
			return super.load(databaseConnection);
		}

	}

	NumberPlanChangeConfig config = new NumberPlanChangeConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// INumberPlan Implementation
	//
	// /////////////////////////////////

	@Override
	public String getInternationalFormat(String number)
	{
		if (number == null || number.length() == 0)
			return number;

		if (!hasCompiledPatterns)
			compilePatterns();

		Matcher matcher = isNationalPattern.matcher(number);
		if (matcher.matches())
		{
			number = matcher.group(2);
			if (hasOnnetMigrations)
			{
				matcher = isOnnetPattern.matcher(number);
				if (matcher.matches())
				{
					return config.nationalDailingCode + migrated(matcher.group(2));
				}
			}

			return config.nationalDailingCode + number;
		}

		return number;
	}

	@Override
	public String getNationalFormat(String number)
	{
		if (number == null || number.length() == 0)
			return number;

		if (!hasCompiledPatterns)
			compilePatterns();

		Matcher matcher;
		if (hasOnnetMigrations)
		{
			matcher = isOnnetPattern.matcher(number);
			if (matcher.matches())
			{
				return migrated(matcher.group(2));
			}
		}

		matcher = isFullyQualifiedPattern.matcher(number);
		if (matcher.matches())
			return matcher.group(2);

		return number;
	}

	private String migrated(String oldNumber)
	{
		if (!hasOnnetMigrations)
			return oldNumber;

		for (int index = 0; index < migratingOnnetPatterns.length; index++)
		{
			Matcher matcher = migratingOnnetPatterns[index].matcher(oldNumber);
			if (matcher.matches())
				return migratingOnnetPrefixes[index] + matcher.group(1);
		}

		return oldNumber;
	}

	@Override
	public String[] getNationalFormat(String[] numbers)
	{
		String[] result = new String[numbers.length];
		for (int index = 0; index < numbers.length; index++)
		{
			result[index] = getNationalFormat(numbers[index]);
		}
		return result;
	}

	@Override
	public boolean isOnnet(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isOnnetPattern.matcher(number).find();
	}

	@Override
	public boolean isMobile(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isMobilePattern.matcher(number).find();
	}

	@Override
	public boolean isSpecial(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isSpecialPattern.matcher(number).find();
	}

	@Override
	public boolean isFixed(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isFixedPattern.matcher(number).find();
	}

	@Override
	public boolean isNational(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isNationalPattern.matcher(number).find();
	}

	@Override
	public boolean isValid(String number)
	{
		if (!hasCompiledPatterns)
			compilePatterns();

		return isValidPattern.matcher(number).find();
	}

	@Override
	public String getNationalDailingCode()
	{
		return config.getNationalDailingCode();
	}

	@Override
	public String[] getLegacyOnnetNumberRanges()
	{
		String numbers = config.getMigratingOnnetNumbers();
		if (numbers == null || numbers.length() == 0)
			return new String[0];
		String[] pairs = numbers.split("\\,");
		String[] result = new String[pairs.length];
		int index = 0;
		for (String pair : pairs)
		{
			String[] ranges = pair.split("\\=");
			result[index++] = ranges[1];
		}
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private synchronized void compilePatterns()
	{
		if (hasCompiledPatterns)
			return;

		String nationalPrefixes = "("; //
		for (String accessCode : config.internationalAccessCodes.split("\\,"))
		{
			nationalPrefixes += '|' + accessCode.replace("+", "\\+") + config.getNationalDailingCode();
		}
		nationalPrefixes += '|' + config.getNationalDailingCode() + ')';

		isOnnetPattern = join(nationalPrefixes, config.onNetNumbers);
		isMobilePattern = join(nationalPrefixes, config.onNetNumbers, config.otherMobileNumbers);
		isSpecialPattern = join(nationalPrefixes, config.specialNumbers);
		isFixedPattern = join(nationalPrefixes, config.fixedLineNumbers);
		isNationalPattern = join(nationalPrefixes, config.onNetNumbers, config.otherMobileNumbers, config.specialNumbers, config.fixedLineNumbers);
		isValidPattern = Pattern.compile(validNumberPattern);

		String a = config.internationalAccessCodes.replace("+", "\\+").replace(',', '|');
		String b = config.getNationalDailingCode();
		String c = join("", config.onNetNumbers, config.otherMobileNumbers, config.specialNumbers, config.fixedLineNumbers).toString();

		String fullyQualifiedPattern = String.format("^(%s|)%s(%s)", a, b, c.substring(1));
		isFullyQualifiedPattern = Pattern.compile(fullyQualifiedPattern);

		if (config.migratingOnnetNumbers == null || config.migratingOnnetNumbers.length() == 0)
		{
			hasOnnetMigrations = false;
			migratingOnnetPatterns = new Pattern[0];
			migratingOnnetPrefixes = new String[0];
		}
		else
		{
			hasOnnetMigrations = true;
			Pattern pairPattern = Pattern.compile("([0-9]+)([x,X]+)\\=([0-9]+)([x,X]+)");
			String[] migratingPairs = config.migratingOnnetNumbers.split("\\,");
			migratingOnnetPatterns = new Pattern[migratingPairs.length];
			migratingOnnetPrefixes = new String[migratingPairs.length];
			int index = 0;
			for (String migratingPair : migratingPairs)
			{
				Matcher matcher = pairPattern.matcher(migratingPair);
				if (matcher.find())
				{
					migratingOnnetPrefixes[index] = matcher.group(1);
					int length = matcher.group(4).length();
					Pattern pattern = Pattern.compile(String.format("^%s([\\d]{%d,%d})$", matcher.group(3), length, length));
					migratingOnnetPatterns[index++] = pattern;
				}
			}

		}

		hasCompiledPatterns = true;
	}

	private Pattern join(String nationalPrefixes, String... patterns)
	{
		StringBuilder regex = new StringBuilder('^' + nationalPrefixes + '(');
		boolean first = true;
		for (String pattern : patterns)
		{
			if (pattern == null | pattern.length() == 0)
				continue;

			String[] parts = pattern.split("\\,");
			for (String part : parts)
			{
				if (part.length() == 0)
					continue;
				if (first)
					first = false;
				else
					regex.append("|");
				regex.append(part);
			}
		}
		regex.append(")$");
		String patternString = regex.toString().replace("x", "\\d").replace("X", "\\d").replace(",", "|");
		Pattern result = Pattern.compile(patternString.length() == 0 ? "[^.]+" : patternString);
		return result;
	}

}
